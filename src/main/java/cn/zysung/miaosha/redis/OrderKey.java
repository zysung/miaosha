package cn.zysung.miaosha.redis;

public class OrderKey extends BasePrefix {

    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getMiaoshaOrderByUidAndGid = new OrderKey("moug");


}
