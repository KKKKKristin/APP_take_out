package com.itheima.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ReportService {

    List<Map<String,Integer>> dish(Date beginTime, Date endTime);

    List<Map<String, Integer>> setmeal(Date beginTime, Date endTime);

    List<Map<String, Integer>> dish2(Date beginTime, Date endTime);
}
