package cn.zysung.miaosha.controller;

import cn.zysung.miaosha.po.OrderInfo;
import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.result.CodeMsg;
import cn.zysung.miaosha.result.Result;
import cn.zysung.miaosha.service.GoodsService;
import cn.zysung.miaosha.service.OrderService;
import cn.zysung.miaosha.service.UserService;
import cn.zysung.miaosha.vo.GoodsVo;
import cn.zysung.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;


    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, User user, @RequestParam long orderId){
        if(user == null) return Result.error(CodeMsg.SESSION_ERROR);
        OrderInfo order = orderService.getOrderById(orderId);
        if(order==null) return Result.error(CodeMsg.ORDER_NOT_EXIST);
        long goodsId = order.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoodsVo(goodsVo);
        orderDetailVo.setOrderInfo(order);
        return Result.success(orderDetailVo);
    }

}
