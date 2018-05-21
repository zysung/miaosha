package cn.zysung.miaosha.service;

import cn.zysung.miaosha.dao.GoodsDao;
import cn.zysung.miaosha.po.Goods;
import cn.zysung.miaosha.po.MiaoshaOrder;
import cn.zysung.miaosha.po.OrderInfo;
import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.redis.MiaoshaKey;
import cn.zysung.miaosha.redis.RedisService;
import cn.zysung.miaosha.utils.MD5Util;
import cn.zysung.miaosha.utils.UUIDUtil;
import cn.zysung.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    Logger logger = LoggerFactory.getLogger(MiaoshaService.class);

    @Transactional
    public OrderInfo miaosha(User u, GoodsVo goodsVo) {
        //秒杀操作：减库存，下订单，写入秒杀订单（事务）

        boolean success = goodsService.reduceStock(goodsVo);  //减库存
        if (success) {   //减库存成功才生成订单
            return orderService.createorder(u, goodsVo); //生成订单
        } else {
            setGoodsOver(goodsVo.getId());  //设置标记判断库存是否完
            return null;
        }

    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver, "" + goodsId, true);//如果商品卖完，往redis中写标记
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver, "" + goodsId);
    }

    /**
     * 从redis中查询秒杀订单
     *
     * @param userId
     * @param goodsId
     * @return
     */
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(userId, goodsId);
        if (order != null) { //秒杀成功返回订单ID
            logger.info("查询秒杀result:成功");
            return order.getOrderId();
        } else {
            boolean isOver = getGoodsOver(goodsId);  //判断库存是否已经卖完
            if (isOver) {  //已卖完
                logger.info("查询秒杀result:失败");
                return -1;
            } else {  //没买完但查不到订单，仍在处理中
                logger.info("查询秒杀result:排队中");
                return 0;
            }
        }
    }


    public boolean checkPath(User u, long goodsId, String path) {
        if (u == null || path == null) return false;
        String pathInRedis = redisService.get(MiaoshaKey.getMiaoshaPath, "" + u.getId() + "_" + goodsId, String.class);
        return path.equals(pathInRedis);
    }

    public String createMiaoshaPath(User u, long goodsId) {
        String str = MD5Util.md5(UUIDUtil.getUuid() + "123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, "" + u.getId() + "_" + goodsId, str);
        return str;
    }

    public BufferedImage createMiaoshaVerifyCode(User u, long goodsId) {
        if (u == null || goodsId <= 0) return null;
        int width = 80;
        int height = 30;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);

        Random rdm = new Random();
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        //验证码，上面是框框和干扰
        String verifyCode = createVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();

        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode,""+u.getId() + "_" + goodsId, rnd);
        return image;
    }


    //根据表达式计算结果
    private static int calc(String exp) {
        try {
            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine engine = sem.getEngineByName("javascript");
            return (Integer) engine.eval(exp);
        } catch (ScriptException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[]{'+', '-', '*'};

    //生成验证码中的数学公式字符串
    private String createVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }


    public boolean checkVerifyCode(User u, long goodsId, int verifyCode) {
        if (u == null || goodsId <= 0) return false;
        Integer codeInRedis = redisService.get(MiaoshaKey.getMiaoshaVerifyCode,""+u.getId()+"_"+goodsId,Integer.class);
        if(codeInRedis==null || codeInRedis-verifyCode!=0){
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode,""+u.getId()+"_"+goodsId);
        return true;
    }
}