package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Category;
import com.itheima.service.CateGoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CateGoryController {
    @Autowired
    private CateGoryService cateGoryService;


    /**
     * 新增分类
     *
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {

        cateGoryService.save(category);
        log.info("category{}", category);
        return R.success("新增分类成功");
    }

    /**
     * 分页
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        //1设置分页参数
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //2lambdaQueraty,条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        cateGoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * s删除分类,判断字表有没有联系
     *
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id) {
        log.info("删除分类，id为：{}", id);

        //categoryService.removeById(id);
        cateGoryService.remove(id);

        return R.success("分类信息删除成功");
    }

    /**
     * 修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息{}", category);
        cateGoryService.updateById(category);
        return R.success("修改分类信息成功");
    }


    /**
     * 根据条件查询
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = cateGoryService.list(queryWrapper);
        return R.success(list);

    }


}
