package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {

    //新增套餐
    void saveWithDish(SetmealDto setmealDto);

    //删除套餐
    void deleteWithDish(List<Long> ids);

    //回显数据
    Setmeal getByWithGategory(Long id);

    //更新套餐
    void updateWithDish(SetmealDto setmealDto);

}
