package cn.zysung.miaosha.dao;

import cn.zysung.miaosha.po.MiaoshaOrder;
import cn.zysung.miaosha.po.OrderInfo;
import cn.zysung.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderDao {


    @Select("select * from miaosha_order where user_id = #{userId} and goods_id = #{goodsId}")
    MiaoshaOrder getMiaoshaOrderByUserIdAndGoodsId(@Param("userId") Long id,@Param("goodsId") long goodsId);

    @Insert("insert into order_info(user_id,goods_id,goods_name,goods_count,goods_price,order_channel,status,create_date) values(#{userId},#{goodsId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")//返回id
    public long  insertOrder(OrderInfo orderInfo);

    @Insert("insert into miaosha_order(user_id,goods_id,order_id) values(#{userId},#{goodsId},#{orderId})")
    public int insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);


    @Select("select * from order_info where id = #{orderId}")
    public OrderInfo getOrderById(@Param("orderId") long orderId);
}
