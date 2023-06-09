package com.itheima.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    @Select("<script>" +
            "select c.name name,sum(od.number) value from order_detail od,dish d,category c,orders o " +
            "where od.dish_id = d.id and d.category_id =c.id and o.id = od.order_id " +
            "<if test='beginTime!=null'> and o.order_time &gt;= #{beginTime}</if> " +
            "<if test='endTime!=null'> and o.order_time &lt;= #{endTime}</if> " +
            "group by c.name" +
            "</script>")
    List<Map<String, Integer>> dish(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);


    @Select("<script>" +
            "select c.name name,sum(od.number) value from order_detail od,setmeal s,category c,orders o " +
            "where od.setmeal_id = s.id and s.category_id =c.id and o.id = od.order_id " +
            "<if test='beginTime!=null'> and o.order_time &gt;=#{beginTime}</if> " +
            "<if test='endTime!=null'> and o.order_time &lt;=#{endTime}</if> " +
            "group by c.name" +
            "</script>")
    List<Map<String, Integer>> setmeal(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    @Select("<script>" +
            "select d.name name,sum(od.number) value from order_detail od,dish d,category c,orders o " +
            "where od.dish_id = d.id and d.category_id =c.id and o.id = od.order_id " +
            "<if test='beginTime!=null'> and o.order_time &gt;= #{beginTime}</if> " +
            "<if test='endTime!=null'> and o.order_time &lt;= #{endTime}</if> " +
            "group by d.name" +
            "</script>")
    List<Map<String, Integer>> dish2(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);
}
