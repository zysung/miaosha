package cn.zysung.miaosha.controller;

import cn.zysung.miaosha.access.AccessLimit;
import cn.zysung.miaosha.po.MiaoshaOrder;
import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.rabbitmq.MQSender;
import cn.zysung.miaosha.rabbitmq.MiaoshaMessage;
import cn.zysung.miaosha.redis.AccessKey;
import cn.zysung.miaosha.redis.GoodsKey;
import cn.zysung.miaosha.redis.RedisService;
import cn.zysung.miaosha.result.CodeMsg;
import cn.zysung.miaosha.result.Result;
import cn.zysung.miaosha.service.GoodsService;
import cn.zysung.miaosha.service.MiaoshaService;
import cn.zysung.miaosha.service.OrderService;
import cn.zysung.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    //设置内存标记减少10个之后的redis访问
    private Map<Long,Boolean> localOverMap = new HashMap<Long, Boolean>();

    Logger logger = LoggerFactory.getLogger(MiaoshaController.class);

//  1、系统初始化将商品库存加载到redis中
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList==null) return;
        for (GoodsVo goods:goodsList){
            int stockCount  = goods.getStockCount();
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goods.getId(),stockCount);
            localOverMap.put(goods.getId(),false);//初始化时标记商品秒杀没结束
        }
    }
    //未优化
    //5000个并发，QPS=57.3(windows) ,加了redis缓存订单区别不大（57.8）,接口优化之后（redis预减库存，rabbitMQ异步插入）QPS为250.4
    //1000个并发，QPS=30.7(Centos)而且程序当掉
//    get、post的区别
//    get是幂等的，代表从服务端获取数据，无论调用多少次，不会对服务端数据产生任何影响
//    POST代表向服务端传送数据，服务端数据会发生变化
    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, User u, @RequestParam("goodsId") long goodsId, @PathVariable("path") String path){
        model.addAttribute("user",u);
        if(u==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //验证path
        boolean check = miaoshaService.checkPath(u,goodsId,path);
        if(!check) return Result.error(CodeMsg.REQUEST_ILLEGAL);

        //10个之后的操作没必要访问redis,做标记
        boolean over = localOverMap.get(goodsId);
        if(over){
            Result.error(CodeMsg.MIAOSHA_OVER);
        }

        //2、收到请求时，Redis减少库存，库存不足，立即返回

        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);//此处是返回decr后的值,
        if(stock<0){
            localOverMap.put(goodsId,true);//库存为0之后的操作没必要访问redis,做标记，后面的请求都不再访问redis
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //判断是否秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(u.getId(),goodsId); //此处查的是redis中的订单，同样不阻塞
        if(order!=null){//重复秒杀判断
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //3、库存足够，请求入队，立即返回排队中
        MiaoshaMessage message = new MiaoshaMessage();
        message.setUser(u);
        message.setGoodsId(goodsId);

        mqSender.sendMiaoshaMessage(message);
        logger.info("秒杀请求入队成功");
        return Result.success(0);//0代表排队中


//        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，一个用户发两个请求req1,req2
//        //判断库存
//        int stock =  goodsVo.getStockCount();
//        if(stock<=0){
//            return Result.error(CodeMsg.MIAOSHA_OVER);
//        }
//        //判断是否秒杀到了
//        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(u.getId(),goodsId);
//        if(order!=null){//重复秒杀判断
//            return Result.error(CodeMsg.REPEATE_MIAOSHA);
//        }
//        //秒杀操作：减库存，下订单，写入秒杀订单（事务）
//        OrderInfo orderInfo = miaoshaService.miaosha(u,goodsVo);
//        return Result.success(orderInfo); //订单详情页

    }

    /**
     * 客户端轮询的方法
     * @param model
     * @param u
     * @param goodsId
     * @return
     */
    @AccessLimit(seconds=5,maxCount=10,needLogin=true)
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, User u, @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user",u);
        if(u==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        logger.info("浏览器调轮询");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //秒杀成功，返回订单ID，失败返回-1，排队中返回0
        long result = miaoshaService.getMiaoshaResult(u.getId(),goodsId);
        logger.info("查询秒杀结果：result="+result);
        return Result.success(result);
    }


    //秒杀接口地址隐藏,在此处做验证码校验

    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    @AccessLimit(seconds=5,maxCount=5,needLogin=true)
    public Result<String> getMiaoshaPath(HttpServletRequest request, User u, @RequestParam("goodsId") long goodsId, @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
        if(u==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
//        //防刷限流，将访问次数记录到redis,并设置有效期，每次访问都从redis中查询访问次数（优化，用拦截器通用化）
//        Integer count = redisService.get(AccessKey.access,""+request.getRequestURI()+"_"+u.getId(),Integer.class);
//        if(count==null){
//            redisService.set(AccessKey.access,""+request.getRequestURI()+"_"+u.getId(),1);
//        }else if(count<30){
//            redisService.incr(AccessKey.access,""+request.getRequestURI()+"_"+u.getId());
//        }else {
//            return Result.error(CodeMsg.ACCESS_LIMIT);
//        }
        boolean checkVerifyCode = miaoshaService.checkVerifyCode(u,goodsId,verifyCode);
        if(!checkVerifyCode) return Result.error(CodeMsg.REQUEST_ILLEGAL);


        String path = miaoshaService.createMiaoshaPath(u,goodsId);
        return Result.success(path);
    }


    //生成图形验证码接口
    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getVerifyCode(Model model, User u, @RequestParam("goodsId") long goodsId, HttpServletResponse response) {
        model.addAttribute("user",u);
        if(u==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = miaoshaService.createMiaoshaVerifyCode(u,goodsId);


        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"JPEG",outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAILED);
        }

        return null;


    }


}
