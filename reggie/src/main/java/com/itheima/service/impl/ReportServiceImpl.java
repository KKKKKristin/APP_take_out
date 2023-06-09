package com.itheima.service.impl;

import com.itheima.mapper.ReportMapper;
import com.itheima.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public List<Map<String, Integer>> dish(Date beginTime, Date endTime) {
        List<Map<String, Integer>> dish = reportMapper.dish(beginTime, endTime);
        return dish;
    }

    @Override
    public List<Map<String, Integer>> setmeal(Date beginTime, Date endTime) {
        List<Map<String, Integer>> setmeal = reportMapper.setmeal(beginTime, endTime);
        return setmeal;
    }

    @Override
    public List<Map<String, Integer>> dish2(Date beginTime, Date endTime) {
        List<Map<String, Integer>> dish2 = reportMapper.dish2(beginTime, endTime);
        return dish2;
    }
}
