package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.mapper.CateGoryMapper;
import com.itheima.service.CateGoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CateGoryServiceImpl extends ServiceImpl<CateGoryMapper, Category> implements CateGoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id delete 分类
     */
    @Override
    public void remove(Long id) {
        //1.查询当前分类是否关联菜品,如果已经关联,抛出一个异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if (count1 > 0) {
            //关联菜品,抛出异常
            throw new CustomException("当前分类下,关联了菜品,无法删除");
        }

        //2.查询当前分类是否关联套餐,如果已经关联,抛出异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询信息
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0) {
            throw new CustomException("当前分类下关联了套餐，不能删除"); //关联套餐,抛出异常
        }
        super.removeById(id);
    }
}
