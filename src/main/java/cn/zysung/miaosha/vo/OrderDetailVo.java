package cn.zysung.miaosha.vo;

import cn.zysung.miaosha.po.Goods;
import cn.zysung.miaosha.po.OrderInfo;

import java.util.Date;


//将商品表与秒杀商品表的信息合成
public class OrderDetailVo extends Goods {

    private GoodsVo goodsVo;
    private OrderInfo orderInfo;

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }

    public OrderInfo getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }
}
