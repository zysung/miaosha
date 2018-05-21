package cn.zysung.miaosha.access;

import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.redis.AccessKey;
import cn.zysung.miaosha.redis.RedisService;
import cn.zysung.miaosha.result.CodeMsg;
import cn.zysung.miaosha.result.Result;
import cn.zysung.miaosha.service.UserService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;


    Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);
    @Override  //方法执行前拦截
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){



            //获取登陆user后将user存到ThreadLocal中
            User user = getUser(request,response);
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class); //拿到方法上的注解

            if(accessLimit == null) return true;

            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            logger.info(seconds+"-"+maxCount+"-"+needLogin);

            String key = request.getRequestURI();
            if(needLogin){
                if(user==null){
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                logger.info("进了needLogin判断");
                key+= "_"+user.getId();
                logger.info("进了needLogin判断"+key);
            }else {
                //do nothing
            }

            //防刷限流，将访问次数记录到redis,并设置有效期，每次访问都从redis中查询访问次数（优化，用拦截器通用化）
            Integer count = redisService.get(AccessKey.withExpire(seconds),key,Integer.class);
            AccessKey prefix = AccessKey.withExpire(seconds);
            if(count==null){
                redisService.set(prefix,key,1);
            }else if(count<maxCount){
                redisService.incr(prefix,key);
            }else {
                render(response,CodeMsg.ACCESS_LIMIT);
                return false;
            }
        }

        return true;
    }

    private void render(HttpServletResponse response, CodeMsg codeMsg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream outputStream = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(codeMsg));
        outputStream.write(str.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }


    private User getUser(HttpServletRequest request, HttpServletResponse response){

        String paramToken = request.getParameter(UserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,UserService.COOKIE_NAME_TOKEN);


        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)? cookieToken : paramToken;
        // 跳转的页面拿到token之后，就可以从redis中取得用户信息
        User u = userService.getByToken(response,token);
        return u;
    }

    private String getCookieValue(HttpServletRequest request,String cookieName) {
        Cookie[] cookies = request.getCookies();
        if(cookies==null || cookies.length==0){
            return null;
        }
        for (Cookie cookie : cookies
                ) {
            if (cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }

}
