package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.entity.Orders;

import javax.servlet.http.HttpSession;

public interface OrderService extends IService<Orders> {

    void submit(Orders orders, HttpSession session);
}
