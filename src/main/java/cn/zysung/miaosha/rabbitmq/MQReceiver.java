package cn.zysung.miaosha.rabbitmq;

import cn.zysung.miaosha.po.MiaoshaOrder;
import cn.zysung.miaosha.po.OrderInfo;
import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.redis.RedisService;
import cn.zysung.miaosha.result.CodeMsg;
import cn.zysung.miaosha.result.Result;
import cn.zysung.miaosha.service.GoodsService;
import cn.zysung.miaosha.service.MiaoshaService;
import cn.zysung.miaosha.service.OrderService;
import cn.zysung.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)  //指定接收者监听的队列名
    public void receiveMiaosha(String message){
        logger.info("receive msg:"+message);
        MiaoshaMessage mm = RedisService.stringToBean(message,MiaoshaMessage.class);
        User user= mm.getUser();
        long goodsId = mm.getGoodsId();
        //4、请求出队，生成订单减库存
        //判断库存(DB)
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock<=0){
            return;
        }
        //判断重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if(order!=null){//重复秒杀判断
            return;
        }
        //秒杀操作：减库存，下订单，写入秒杀订单（事务）
        logger.info("receiver进行秒杀操作:"+message);
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
    }

    @RabbitListener(queues = MQConfig.QUEUE_NAME)  //指定接收者监听的队列名
    public void receive(String message){
        logger.info("receive msg:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)  //指定接收者监听的队列名
    public void receiveTopic1(String message){
        logger.info("receive topic/fanout queue1 msg:"+message);
    }
    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)  //指定接收者监听的队列名
    public void receiveTopic2(String message){
        logger.info("receive topic/fanout queue2 msg:"+message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)  //指定接收者监听的队列名
    public void receiveHeadersQueue(byte[] message){
        logger.info("receive headers queue msg:"+new String(message));
    }



}
