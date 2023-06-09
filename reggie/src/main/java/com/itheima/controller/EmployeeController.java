package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Employee;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpSession;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     *
     * @param session
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpSession session, @RequestBody Employee employee) {
        //1.将页面传输的密码MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据用户名查询数据库(也可以分开,2次判断,用户名和密码)
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        queryWrapper.eq(Employee::getPassword, password);
        Employee emp = employeeService.getOne(queryWrapper);

        //3.如果没查到返回登录失败
        if (emp == null) {
            return R.error("密码或用户名错误");
        }
        //4.根据状态登录
        if (emp.getStatus() == 0) {
            return R.error("账户已禁用");
        }
        //6.登陆成功,存入session,返回成功登录
        session.setAttribute("employee", emp.getId());
        return R.success(emp);

    }

    /**
     * 退出
     *
     * @param session
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpSession session) {
        session.removeAttribute("employee");//移除session
        // session.invalidate();销毁session
        return R.success("退出成功");
    }

    /**
     * 新增员工
     *
     * @param session
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpSession session, @RequestBody Employee employee) {
        log.info("新增员工,员工信息");
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //获取当前登录用户的id
//        Long empId = (Long) session.getAttribute("employee");
//
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);
        //构造分页器
        Page<Employee> pageInfo = new Page(page, pageSize);
        //过滤条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //分页查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);

    }

    /**
     * 根据id修改员工信息
     *
     * @param session
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpSession session, @RequestBody Employee employee) {
        Long id = Thread.currentThread().getId();
        log.info("线程id{}", id);
        log.info(employee.toString());
//        Long empId = (Long) session.getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());//修改时间
//        employee.setUpdateUser(empId);//修改人
        employeeService.updateById(employee);
        return R.success("员工修改成功");
    }

    /**
     * 根据id查询信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee employeeServiceById = employeeService.getById(id);
        if (employeeServiceById == null) {
            return R.error("没有查询到对应员工信息");
        }
        return R.success(employeeServiceById);


    }

}
