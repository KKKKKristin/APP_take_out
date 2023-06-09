package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.entity.Setmeal;
import com.itheima.mapper.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品,同时保存口味
     */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //1.保存菜品信息到菜品表dish
        this.save(dishDto);
        Long dishId = dishDto.getId();//菜品Id
        //2.保存口味信息到口味表
        //将dishId传入集合
        List<DishFlavor> flavors = dishDto.getFlavors();//菜品口味
        //遍历集合为DishId赋值
      /*  //方法一:
       flavors=flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        */
        //方法二
        for (int i = 0; i < flavors.size(); i++) {
            DishFlavor df = flavors.get(i);
            df.setDishId(dishId);
        }

     /*   //方法三:
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        */


        dishFlavorService.saveBatch(dishDto.getFlavors());

    }

    /**
     * 回显数据
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByWithFlavorsId(Long id) {

        //查询菜品信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();

        BeanUtils.copyProperties(dish, dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    public void updateWithFlavors(DishDto dishDto) {
        //更新菜品表的数据
        this.updateById(dishDto);


        //2.清理当前的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);


        //3.将提交过来的数据进行更新
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }

        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据菜品id删除菜品和菜品口味信息
     *
     * @param ids
     */
    @Override
    public void removeWithFlavors(@RequestParam List<Long> ids) {
        //需要判断是否在售卖;
        ////select count(*) from dish where id in (1,2,3) and status = 1
        //1.创建查询器

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.in(Dish::getId, ids);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("菜品正在售卖中，不能删除");
        }
        //删除菜品信息
        this.removeByIds(ids);
        //删除菜品关联口味信息
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(lambdaQueryWrapper);


    }

    /**
     * 更新状态
     * @param status
     * @param ids
     */
    @Override
    public void updateStatus(Integer status, List<Long> ids) {

        //1.查询是否还有套餐在售卖菜品,如果有提示还有套餐在售卖
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("售卖套餐中包含该菜品，不能停售");
        }


        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Dish::getStatus, status);
        updateWrapper.in(Dish::getId, ids);
        this.update(new Dish(), updateWrapper);
    }



}
