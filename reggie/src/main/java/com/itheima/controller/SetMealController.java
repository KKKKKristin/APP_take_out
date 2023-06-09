package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.CustomException;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Setmeal;
import com.itheima.service.CateGoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CateGoryService cateGoryService;


    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("添加套菜成功");
    }

    /**
     * 套餐分页
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoInfo = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);
        //2.处理分页数据
        //2.1先拷贝
        BeanUtils.copyProperties(pageInfo, setmealDtoInfo, "records");

        //处理其他数据
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> setmealDtoList = new ArrayList<>();
        for (Setmeal record : records) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(record, setmealDto);
            //获取categoryId
            Long categoryId = record.getCategoryId();
            //根据套餐id,查询信息
            Category category = cateGoryService.getById(categoryId);

            setmealDto.setCategoryName(category.getName());

            setmealDtoList.add(setmealDto);

        }
        setmealDtoInfo.setRecords(setmealDtoList);
        return R.success(setmealDtoInfo);

    }


    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);

        setmealService.deleteWithDish(ids);
        return R.success("套餐数据删除成功");
    }


    /**
     * 更新菜品状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") Integer status, @RequestParam("ids") List<Long> ids) {
        //1.创建条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Setmeal::getId, ids);

        List<Setmeal> list = setmealService.list(queryWrapper);
        if (list.size() == 0) {
            throw new CustomException("修改失败,请重试");
        }
        //不为空，对每一个套餐状态进行修改
        for (Setmeal setmeal : list) {
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("菜品状态修改成功");
    }

    @GetMapping("/{id}")
    public R<Setmeal> getById(@PathVariable Long id) {
        Setmeal byId = setmealService.getByWithGategory(id);
        return R.success(byId);

    }


    /**
     * 修改菜品
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功");
    }


    /**
     * 根据条件查询套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }
}
