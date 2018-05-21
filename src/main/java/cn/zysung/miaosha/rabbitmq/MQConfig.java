package cn.zysung.miaosha.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class MQConfig {

    public static final String QUEUE_NAME  = "queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String  TOPIC_QUEUE2 = "topic.queue2";
    public static final String TOPIC_EXCHANGE = "topicExchange";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String  HEADERS_QUEUE = "headers.queue2";
    public static final String HEADERS_EXCHANGE = "headersExchange";
    public static final String MIAOSHA_QUEUE = "miaosha.queue";

    @Bean
    public Queue miaoshaQueue(){
        return  new Queue(MIAOSHA_QUEUE,true);
    }

    @Bean
    public Queue queue(){
      return  new Queue(QUEUE_NAME,true);//持久化设置为true的话，即使服务崩溃也不会丢失队列
    }

    /**topic模式
     *
     * @return
     */
    @Bean
    public Queue topicQueue1(){
        return new Queue(TOPIC_QUEUE1,true);
    }
    @Bean
    public Queue topicQueue2(){
        return new Queue(TOPIC_QUEUE2,true);
    }
    @Bean   //相当于消息路由，先到达exchange再到queue
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Binding topicBinding1(){
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
    }

    @Bean
    public Binding topicBinding2(){
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
    }
    /**
     * Fanout模式（广播）
     */
    @Bean   //相当于消息路由，先到达exchange再到queue
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding fanoutBinding1(){
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2(){
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }

    /**
     * Header模式
     */
    @Bean
    public  Queue headersQueue(){
        return new Queue(HEADERS_QUEUE,true);
    }
    @Bean
    public HeadersExchange headersExchange(){
        return  new HeadersExchange(HEADERS_EXCHANGE);
    }

    @Bean
    public Binding headBinding(){
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("header1","val1");
        map.put("header2","val2");
        //只有当Msg对象里的header与上面完全匹配，才会加到queue中
        return BindingBuilder.bind(headersQueue()).to(headersExchange()).whereAll(map).match();
    }





}
