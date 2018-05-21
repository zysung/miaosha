package cn.zysung.miaosha.vo;

import cn.zysung.miaosha.po.User;

public class GoodsDetailVo {

    private GoodsVo goods;
    private int miaoshaStatus = 0 ;//0未开始，2已结束，1秒杀中
    private int remainSeconds = 0;//剩多久开始
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User u) {
        this.user = u;
    }

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public int getMiaoshaStatus() {
        return miaoshaStatus;
    }

    public void setMiaoshaStatus(int miaoshaStatus) {
        this.miaoshaStatus = miaoshaStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }
}
