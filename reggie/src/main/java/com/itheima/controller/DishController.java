package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.service.CateGoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
@Transactional
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CateGoryService cateGoryService;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 添加菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId();
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品的分页
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //1. 查询菜品信息 (其中Dish没有分类名称)
        Page<Dish> dishPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage, queryWrapper);

        //2. 处理分页对象数据
        Page<DishDto> dishDtoPage = new Page<>();  // 无需分页查询, 不需要查询数据库.
        //2.1 拷贝分页数据
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");  // List<T> records; 集合中是Dish数据, 没有分类名称, 所以records属性不需要拷贝

        //2.2 处理分页集合数据
        List<Dish> dishList = dishPage.getRecords();
        List<DishDto> dishDtoList = new ArrayList<>();

        for (Dish dish : dishList) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            //根据菜品的分类id, 去查询分类信息
            Category c = cateGoryService.getById(dish.getCategoryId());
            dishDto.setCategoryName(c.getName());
            dishDtoList.add(dishDto);
        }


        //3. 返回处理后的数据
        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);

    }

    /**
     * 修改回显数据
     *
     * @param id
     * @return
     */
    @Transactional
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByWithFlavorsId(id);
        return R.success(dishDto);
    }


    /**
     * 根据条件查询
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;
        //获取key
        String key = "dish_" + dish.getCategoryId();//dish_1398138141471541_1

        //先从redis获取数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在,直接返回,无需查询数据库
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }
        //不存在,先查询数据库,将查询数据缓存到redis

        //构造条件查查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件,查询状态为1,也就是起售
        queryWrapper.eq(Dish::getStatus, 1);
        //添加排序
        queryWrapper.orderByDesc(Dish::getSort).orderByAsc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);


        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = cateGoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());
        //如果redis不存在,将查询的菜品数据存入redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);


        return R.success(dishDtoList);
    }


    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        //需要判断是否有连接;
        dishService.removeWithFlavors(ids);
        return R.success("删除成功");
    }

    /**
     * 批量禁用
     *
     * @param ids
     * @return
     */

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") Integer status, @RequestParam("ids") List<Long> ids) {
        dishService.updateStatus(status, ids);
        return R.success("修改状态成功");
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {

        log.info(dishDto.toString());
        dishService.updateWithFlavors(dishDto);
        //清理菜品的缓存
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId();
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /*  *//**
     * 根据条件查询
     *
     * @param dish
     * @return
     */
    /*
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {


        //构造条件查查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件,查询状态为1,也就是起售
        queryWrapper.eq(Dish::getStatus, 1);
        //添加排序
        queryWrapper.orderByDesc(Dish::getSort).orderByAsc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        log.info(list.toString());

        return R.success(list);
    }*/
}
