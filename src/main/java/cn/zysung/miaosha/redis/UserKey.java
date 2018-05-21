package cn.zysung.miaosha.redis;

public class UserKey extends BasePrefix {

    //设置UserKey的过期时间
    public static final int TOKEN_EXPIRE = 3600*24*2;

    private UserKey(int expireSeconds,String prefix) {
        super(expireSeconds,prefix);
    }

    public static UserKey userToken = new UserKey(TOKEN_EXPIRE,"tk");
    public static UserKey getById = new UserKey(0,"id");//对象缓存希望永久有效
//    public static UserKey getByName = new UserKey("name");

}
