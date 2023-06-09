package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.dto.OrdersDto;
import com.itheima.entity.Dish;
import com.itheima.entity.OrderDetail;
import com.itheima.entity.Orders;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.OrderDetailService;
import com.itheima.service.OrderService;
import com.itheima.service.ShoppingCartService;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 提交订单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders, HttpSession session) {
        orderService.submit(orders,session);
        return R.success("下单成功");
    }



    /**
     * 手机端订单分页
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userpage(int page, int pageSize) {

        log.info("分页查询page{},pageSize{}/*,name{}*/", page, pageSize);
        //组装分页条件
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //查询和排序的条件
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        //分页查询
        orderService.page(pageInfo, lambdaQueryWrapper);
        //结果封装完善
        Page<OrdersDto> ordersDtoPage = new Page<>();
        //赋值total 前端需要解析的字段
        //赋值 records 到  List<OrdersDto>
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> ordersDtos = new ArrayList<>();
        for (Orders record : records) {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(record, ordersDto);
            LambdaQueryWrapper<OrderDetail> detailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            detailLambdaQueryWrapper.eq(OrderDetail::getOrderId, record.getId());
            //通过dish_id与dishid 相等 得到口味
            List<OrderDetail> orderDetails = orderDetailService.list(detailLambdaQueryWrapper);

            ordersDto.setOrderDetails(orderDetails);
            ordersDtos.add(ordersDto);
        }

        ordersDtoPage.setRecords(ordersDtos);
        return R.success(ordersDtoPage);
    }


    /**
     * 后台订单分页
     *
     * @param number
     * @param page
     * @param pageSize
     * @param start
     * @param end
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(String number, int page, int pageSize, @DateTimeFormat(pattern = "yyyy-MM-dd HH-mm-ss") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd HH-mm-ss") Date end) {

        log.info("分页查询page{},pageSize{},name{}", page, pageSize);
        //组装分页条件
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //查询和排序的条件
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(number), Orders::getNumber, number);
        lambdaQueryWrapper.between(start != null && end != null, Orders::getOrderTime, start, end);
        lambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        //分页查询
        orderService.page(pageInfo, lambdaQueryWrapper);
        //结果封装完善
        Page<OrdersDto> ordersDtoPage = new Page<>();
        //赋值total 前端需要解析的字段
        //赋值 records 到  List<OrdersDto>
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> ordersDtos = new ArrayList<>();
        for (Orders record : records) {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(record, ordersDto);
            ordersDtos.add(ordersDto);
        }

        ordersDtoPage.setRecords(ordersDtos);
        return R.success(ordersDtoPage);
    }

    @PutMapping
    public R<Orders> update(@RequestBody Orders orders) {
        log.info("修改信息{}", orders);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(orders.getId() != null, Orders::getId, orders.getId());

        orderService.update(orders, queryWrapper);
        return R.success(orders);

    }


    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {

        //获取当前用户的id
        Long currentId = BaseContext.getCurrentId();

        //根据订单的id获取该订单下的订单细节
        LambdaQueryWrapper<OrderDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(OrderDetail::getOrderId, orders.getId());
        List<OrderDetail> list = orderDetailService.list(lqw);

        //清空购物车中的数据
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(qw);

        //将所有的菜品放入购物车中，进行再次购买
        List<ShoppingCart> shoppingCartList = list.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            //为购物车设置数据
            //进行数据的拷贝
            BeanUtils.copyProperties(item, shoppingCart);
            shoppingCart.setId(IdWorker.getId());
            shoppingCart.setUserId(currentId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartService.saveBatch(shoppingCartList);
        return R.success("再次购买成功");
    }

}
