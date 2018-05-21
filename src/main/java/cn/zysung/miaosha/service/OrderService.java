package cn.zysung.miaosha.service;

import cn.zysung.miaosha.dao.GoodsDao;
import cn.zysung.miaosha.dao.OrderDao;
import cn.zysung.miaosha.po.MiaoshaOrder;
import cn.zysung.miaosha.po.OrderInfo;
import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.redis.OrderKey;
import cn.zysung.miaosha.redis.RedisService;
import cn.zysung.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {


    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    Logger logger = LoggerFactory.getLogger(OrderService.class);


    //小优化：将秒杀订单存到redis中
    public MiaoshaOrder getMiaoshaOrderByUserIdAndGoodsId(Long userId, long goodsId) {
        //改成直接从redis查
        MiaoshaOrder msOrder = redisService.get(OrderKey.getMiaoshaOrderByUidAndGid,""+userId+"_"+goodsId,MiaoshaOrder.class);
        if(msOrder!=null){
            logger.info("查询到秒杀订单："+msOrder.getOrderId());
        }else {
            logger.info("查询不到秒杀订单");
        }
        return msOrder;
//      return orderDao.getMiaoshaOrderByUserIdAndGoodsId(id, goodsId);
    }

    @Transactional
    public OrderInfo createorder(User u, GoodsVo goodsVo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goodsVo.getId());
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsPrice(goodsVo.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(u.getId());
        orderDao.insertOrder(orderInfo);

        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goodsVo.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(u.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);

        //下单成功写入数据库后，将秒杀订单写到redis中
        redisService.set(OrderKey.getMiaoshaOrderByUidAndGid,""+u.getId()+"_"+goodsVo.getId(),miaoshaOrder);
        logger.info("写秒杀订单成功");

        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);


    }
}
