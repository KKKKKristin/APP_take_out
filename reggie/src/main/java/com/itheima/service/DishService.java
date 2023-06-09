package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;

import java.util.List;


public interface DishService extends IService<Dish> {

    //新增菜品,同时插入菜品对应的口味的数据,需要操作2张表,dish,dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //回显数据
    public DishDto getByWithFlavorsId(Long id);

    //更新菜品
    public void updateWithFlavors(DishDto dishDto);


    //删除菜品
    public void removeWithFlavors(List<Long> ids);

    //禁用启用
    public void updateStatus(Integer status,List<Long> ids);
}
