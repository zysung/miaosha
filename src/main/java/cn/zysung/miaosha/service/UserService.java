package cn.zysung.miaosha.service;

import cn.zysung.miaosha.dao.UserDao;
import cn.zysung.miaosha.exception.GlobalException;
import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.redis.RedisService;
import cn.zysung.miaosha.redis.UserKey;
import cn.zysung.miaosha.result.CodeMsg;
import cn.zysung.miaosha.utils.MD5Util;
import cn.zysung.miaosha.utils.UUIDUtil;
import cn.zysung.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired(required = false)
    UserDao userDao;

    //引入redis做分布式Session
    @Autowired
    RedisService redisService;

    public static final String COOKIE_NAME_TOKEN = "token";

    public User getById(long id){

        //加入对象级缓存
        //取缓存，有就返回
        User user = redisService.get(UserKey.getById,""+id,User.class);
        if(user!=null){
            return user;
        }
        //缓存查不到查数据库,然后加到缓存中
        user = userDao.getById(id);
        if(user!=null) {
            redisService.set(UserKey.getById, "" + id, user);
        }
        return user;
    }

    public boolean updatePassword(long id,String password,String token){
        User user = getById(id);
        if(user == null) throw new GlobalException(CodeMsg.MOBILE_NOTEXIST);
        //注意顺序不能变，会导致数据库中的数据与缓存中不一致
        //更新数据库
        User tobeUpdate = new User();//局部更新
        tobeUpdate.setId(id);
        tobeUpdate.setPassword(password);
        userDao.update(tobeUpdate);
        //更新缓存(删对象缓存，更新token)
        redisService.delete(UserKey.getById,""+id);
        user.setPassword(tobeUpdate.getPassword());
        redisService.set(UserKey.userToken,token,user);

        return true;

    }


    public String login(HttpServletResponse response,LoginVo loginVo) {
        if(loginVo==null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile  = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        User u  = getById(Long.parseLong(mobile));
        if(u==null){
//            return CodeMsg.MOBILE_NOTEXIST;
            throw new GlobalException(CodeMsg.MOBILE_NOTEXIST);
        }
        //验证密码
        String dbPass = u.getPassword();
        String salt = u.getSalt();
        boolean isPass = dbPass.equals(MD5Util.formPassToDBPass(formPass,salt));
        if(!isPass){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        //登录成功之后生成token写到cookie中回传客户端实现分布式session
          String token  = UUIDUtil.getUuid();
//        redisService.set(UserKey.userToken,token,u);//1、存用户信息到redis
//        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
//        cookie.setMaxAge(UserKey.userToken.expireSeconds());//设置cookie有效期与redis中key的过期时间相同
//        cookie.setPath("/");   //设置cookie的作用域
//        response.addCookie(cookie); //回传浏览器
        addCookie(response,token,u);
        //运行后可以看到response把cookie写到浏览器，跳转时request请求中上传同样cookie到服务器

        return token;
    }
    //通过Token获取用户信息
    public User getByToken(HttpServletResponse response,String token) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        User u  = redisService.get(UserKey.userToken,token,User.class);
        //每次访问延长有效期
        if(u!=null) {
            addCookie(response, token,u);
        }
        return u;
    }
    //往redis里写token,u对，然后把token写进cookie回传客户端，登录后页面根据此token到redis中查登录后的用户信息
    private void addCookie(HttpServletResponse response,String token,User u){
        redisService.set(UserKey.userToken,token,u);//1、存用户信息到redis
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(UserKey.userToken.expireSeconds());//设置cookie有效期与redis中key的过期时间相同
        cookie.setPath("/");   //设置cookie的作用域
        response.addCookie(cookie); //cookie传浏览器
    }

//    @Transactional  //此注解实现方法级别的事务，相当于0配置实现AOP，加了此注解，出错整个方法回滚
//    public boolean txTest(){
//        User u1 = new User();
//        u1.setId(2);
//        u1.setName("2b");
//        userDao.insert(u1);
//
//        User u2 = new User();
//        u2.setId(1);
//        u2.setName("1b");
//        userDao.insert(u2);
//
//        return true;
//    }

    public List<User> getUsers(){

        for (User u : userDao.getUsers()
             ) {
            System.out.println(u.getId());
        }
        return userDao.getUsers();
    }

}
