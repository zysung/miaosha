package cn.zysung.miaosha.service;

import cn.zysung.miaosha.dao.GoodsDao;
import cn.zysung.miaosha.po.Goods;
import cn.zysung.miaosha.po.MiaoshaGoods;
import cn.zysung.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;
    private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);


    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);

    }

    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int ret = goodsDao.reduceStock(g);
        return ret>0;
    }
}
