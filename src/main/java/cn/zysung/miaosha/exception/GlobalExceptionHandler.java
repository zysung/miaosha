package cn.zysung.miaosha.exception;

import cn.zysung.miaosha.result.CodeMsg;
import cn.zysung.miaosha.result.Result;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import java.util.List;

//全局异常处理器拦截并输出异常，作用在Controller层
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    //拦截的异常类
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest req,Exception e){
        e.printStackTrace();
        if (e instanceof GlobalException){
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCm());
        }else if(e instanceof BindException){
            BindException ex = (BindException) e;
            List<ObjectError> errors = ex.getAllErrors();
            String msg = errors.get(0).getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));

        }else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }


}
