package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.entity.Category;


public interface CateGoryService extends IService<Category> {

    public void remove(Long id);
}
