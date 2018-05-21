package cn.zysung.miaosha.result;

public class CodeMsg {
    private int code;
    private String msg;


    //通用异常定义（静态变量的方式）
    public static CodeMsg SUCCESS =  new CodeMsg(0,"success");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100,"服务端异常");
    public static CodeMsg BIND_ERROR = new CodeMsg(500101,"参数校验异常:%s");
    public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(500102,"请求非法");
    public static CodeMsg ACCESS_LIMIT = new CodeMsg(500103,"访问过于频繁");
    //登录模块异常5002XX

    public static CodeMsg SESSION_ERROR = new CodeMsg(500200,"session过期");
    public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500201,"密码不能为空");
    public static CodeMsg MOBILE_EMPTY = new CodeMsg(500202,"手机号不能为空");
    public static CodeMsg MOBILE_ERROR = new CodeMsg(500203,"手机号格式错误");
    public static CodeMsg MOBILE_NOTEXIST = new CodeMsg(500204,"手机号不存在");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(500205,"密码错误");
    //商品模块异常5003XX

    //订单模块异常5004XX
    public static  CodeMsg ORDER_NOT_EXIST =  new CodeMsg(500400,"订单不存在");
    //秒杀模块异常5005XX
    public static  CodeMsg MIAOSHA_OVER =  new CodeMsg(500500,"商品已经秒杀完毕");
    public static  CodeMsg REPEATE_MIAOSHA =  new CodeMsg(500501,"不能重复秒杀");
    public static  CodeMsg MIAOSHA_FAILED =  new CodeMsg(500502,"秒杀失败");


    public CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public CodeMsg fillArgs(Object ... args){
        int code = this.code;
        String msg = String.format(this.msg,args);
        return new CodeMsg(code,msg);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
