package com.itheima.controller;


import com.itheima.common.R;
import com.itheima.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/dish")
    public R<List<Map<String,Integer>>> dish(Date beginTime, Date endTime){
        List<Map<String, Integer>> dish = reportService.dish(beginTime, endTime);
        return R.success(dish);
    }
    @GetMapping("/setmeal")
    public R<List<Map<String,Integer>>> setmeal(Date beginTime, Date endTime){
        List<Map<String, Integer>> setmeal = reportService.setmeal(beginTime, endTime);
        return R.success(setmeal);
    }
    @GetMapping("/dish2")
    public R<List<Map<String,Integer>>> dish2(Date beginTime, Date endTime){
        List<Map<String, Integer>> dish2 = reportService.dish2(beginTime, endTime);
        return R.success(dish2);
    }
}
