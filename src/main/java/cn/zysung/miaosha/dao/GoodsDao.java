package cn.zysung.miaosha.dao;

import cn.zysung.miaosha.po.Goods;
import cn.zysung.miaosha.po.MiaoshaGoods;
import cn.zysung.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    @Select("select g.*,mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date from miaosha_goods mg left join goods g on mg.goods_id=g.id ")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*,mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date from miaosha_goods mg left join goods g on mg.goods_id=g.id where g.id = #{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);


    @Update("update miaosha_goods set stock_count = stock_count -1 where goods_id = #{goodsId} and stock_count > 0")//更新会自动加排他锁
    public int reduceStock(MiaoshaGoods g);
}
