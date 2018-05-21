package cn.zysung.miaosha.controller;

import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.rabbitmq.MQReceiver;
import cn.zysung.miaosha.rabbitmq.MQSender;
import cn.zysung.miaosha.redis.RedisService;
import cn.zysung.miaosha.redis.UserKey;
import cn.zysung.miaosha.result.CodeMsg;
import cn.zysung.miaosha.result.Result;
import cn.zysung.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    @Autowired
    MQReceiver receiver;

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
        sender.send("hello,rabbitMQ");
        return Result.success("hello");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic(){
        sender.sendTopic("hello,rabbitMQ topic");
        return Result.success("hello");
    }
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout(){
        sender.sendFanout("hello,rabbitMQ fanout");
        return Result.success("hello");
    }
    @RequestMapping("/mq/headers")
    @ResponseBody
    public Result<String> headers(){
        sender.sendHeader("hello,rabbitMQ header");
        return Result.success("hello");
    }

//    @RequestMapping("/redisget")
//    @ResponseBody
//    public Result<User> redisGet(){
//        User user = redisService.get(UserKey.getById,""+1,User.class);  //UserKey:id1
//        return Result.success(user);
//    }
//    @RequestMapping("/redisset")
//    @ResponseBody
//    public Result<Boolean> redisSet(){
//        User user = new User();
//        boolean res =  redisService.set(UserKey.getById,""+1,user);
//        return Result.success(res);
//    }


    @RequestMapping("/dbget")
    @ResponseBody
    public Result<User> dbGet(){
        User u = userService.getById(1);
        return Result.success(u);
    }
    @RequestMapping("/dbtransaction")
    @ResponseBody
    public Result<Boolean> dbTx(){
//        userService.txTest();
        return Result.success(true);
    }


    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!";
    }
    //1.restful api输出json（responsebody）（封装result（code,msg,data）做约定）  2.页面

    //api封装
    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello,zysung");
    }
    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError() {
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    //集成页面模板
    @RequestMapping("/thymeleaf")
    public String thymeleafTest(Model model) {
        model.addAttribute("name","zysung");
        return "hello";
    }
}
