package cn.zysung.miaosha.controller;

import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.redis.GoodsKey;
import cn.zysung.miaosha.redis.RedisService;
import cn.zysung.miaosha.result.Result;
import cn.zysung.miaosha.service.GoodsService;
import cn.zysung.miaosha.service.UserService;
import cn.zysung.miaosha.vo.GoodsDetailVo;
import cn.zysung.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);

    @Autowired
    UserService userService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    //运行后可以看到response把cookie写到浏览器，跳转此页时request请求中上传同样cookie到服务器
//    @RequestMapping("/to_list")
//    public String toList(Model model,HttpServletResponse response,
//                         @CookieValue(value =UserService.COOKIE_NAME_TOKEN,required = false) String cookieToken,
//                         @RequestParam(value = UserService.COOKIE_NAME_TOKEN,required = false)String paramToken,
//                            User u){
//
//        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
//            return "/login/to_login";
//        }
//        String token = StringUtils.isEmpty(paramToken)? cookieToken : paramToken;
        //跳转的页面拿到token之后，就可以从redis中取得用户信息
//        User u = userService.getByToken(response,token);
//        model.addAttribute("user",u);
//        return "goods_list";
//
//    }

    //每个方法都要加Token参数做判断，考虑将获取cookie与判断cookie代码抽取出来,使用mvc框架中的ArgumentResolver做
    //假如以后获取session的方法改变了，只需要在UserArgumentResolver中做调整，所有业务代码无需调整

    //使用JMeter压测1000个并发，吞吐量（QPS）为88.2/s (centOS)  本机为90.4


    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request,HttpServletResponse response,Model model, User u){
        model.addAttribute("user",u);
        //redis做页面缓存(防止瞬间访问量过大)
        //若redis中存了返回的html代码，取了返回
        String html = redisService.get(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //查询商品列表
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsVoList);

//        return "goods_list";
        SpringWebContext context = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        //若redis中没有，手动渲染
       html = thymeleafViewResolver.getTemplateEngine().process("goods_list",context);
        if(!StringUtils.isEmpty(html)){  //保存html模板到redis，下次直接从redis中拿
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    //使用页面静态化，只返回一个Result的json
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> toDetail(HttpServletRequest request, HttpServletResponse response, Model model, User u, @PathVariable long goodsId){

        //只留业务逻辑，不用model传值
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        //秒杀的开始与结束时间
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();

        logger.info(goods.getStartDate().toString());

        long now= System.currentTimeMillis();

        int miaoshaStatus = 0;//0未开始，2已结束，1秒杀中
        int remainSeconds= 0;//剩多久开始
        if(now<startAt){//秒杀未开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt-now)/1000);
        }else if(now>endAt){//秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo  = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setMiaoshaStatus(miaoshaStatus);
        vo.setRemainSeconds(remainSeconds);
        vo.setUser(u);
        return Result.success(vo);
    }


    //实现页面缓存到redis
    @RequestMapping(value = "/to_detail2/{goodsId}",produces = "text/html")
    @ResponseBody
    public String toDetail2(HttpServletRequest request,HttpServletResponse response,Model model, User u, @PathVariable long goodsId){
        model.addAttribute("user",u);

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);

        //秒杀的开始与结束时间
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();

        logger.info(goods.getStartDate().toString());

        long now= System.currentTimeMillis();

        int miaoshaStatus = 0;//0未开始，2已结束，1秒杀中
        int remainSeconds= 0;//剩多久开始
        if(now<startAt){//秒杀未开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt-now)/1000);
        }else if(now>endAt){//秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        //  return "goods_detail";
        //redis做页面缓存(防止瞬间访问量过大)
        //若redis中存了返回的html代码，取了返回
        String html = redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        SpringWebContext context = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        //若redis中没有，手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",context);
        if(!StringUtils.isEmpty(html)){  //保存html模板到redis，下次直接从redis中拿
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
    }


}
