package cn.zysung.miaosha.rabbitmq;

import cn.zysung.miaosha.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {

   private static Logger logger = LoggerFactory.getLogger(MQSender.class);

    @Autowired
    AmqpTemplate amqpTemplate;

    public void send(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("send msg:"+msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE_NAME,msg); //发送消息
    }

    public void sendTopic(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("send topic msg:"+msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key1",msg+"1"); //q1,q2都能收到
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key2",msg+"2"); //只有q2能收到
    }
    public void sendFanout(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("send fanout msg:"+msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",msg);
    }

    public void sendHeader(Object message) {
        String msg = RedisService.beanToString(message);
        logger.info("send headers msg:" + msg);

        MessageProperties props = new MessageProperties();
        props.setHeader("header1","val1");
        props.setHeader("header2","val2");
        Message obj = new Message(msg.getBytes(),props); //发送时是原始数据（转成字节数组）+头部信息（校验）
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "",obj);
    }


    public void sendMiaoshaMessage(MiaoshaMessage message) { //direct模式
        String msg = RedisService.beanToString(message);
        logger.info("send miaosha msg:" + msg);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);

    }
}
