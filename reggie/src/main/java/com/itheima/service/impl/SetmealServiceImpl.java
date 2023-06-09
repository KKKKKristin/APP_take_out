package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套菜基本信息
        this.save(setmealDto);

        //保存菜品和套餐的关系
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }

        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void deleteWithDish(List<Long> ids) {
        if (ids.size() == 0) {
            throw new CustomException("请勾选后在删除!");
        }
        //1.判断是否还有在售卖
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("套菜正在售卖中");
        }

        //可以删除,先删除套餐表中的数据
        this.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lambdaQueryWrapper);

        //或者使用级联删除
    }

    /**
     * 回显数据
     *
     * @param id
     * @return
     */
    @Override
    public Setmeal getByWithGategory(Long id) {
        //查询菜品信息
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setmeal, setmealDto);
        //将套餐菜品表中的信息查询出来
        //select * from SetmealDish where SetmealId in ();  有个套餐对应多个菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        //先删除套餐关联的菜品表信息，再进行插入
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //在重新写入套餐菜品数据
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        //将list写入
        for (SetmealDish setmealDish : list) {
//            setmealDish.setDishId(setmealDto.getId());
            setmealDish.setSetmealId(setmealDto.getId());
        }


        setmealDishService.saveBatch(list);

    }
}
