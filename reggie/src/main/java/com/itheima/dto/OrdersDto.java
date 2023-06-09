package com.itheima.dto;

import com.itheima.entity.OrderDetail;
import com.itheima.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;//手机号

    private String address;//地址

    private String consignee;//收货人

    private List<OrderDetail> orderDetails;

}
