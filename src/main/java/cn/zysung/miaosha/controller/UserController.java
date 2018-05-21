package cn.zysung.miaosha.controller;

import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.result.Result;
import cn.zysung.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;


    @RequestMapping("/info")
    @ResponseBody
    public Result<User> info(Model model, User user){
        return Result.success(user);
    }

}
