package cn.zysung.miaosha.redis;
//封装通用缓存key
public interface KeyPrefix {

    public int expireSeconds();//key有效期

    public String getPrefix();


}
