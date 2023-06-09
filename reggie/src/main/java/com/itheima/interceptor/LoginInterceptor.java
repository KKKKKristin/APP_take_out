//package com.itheima.interceptor;
//
//import com.alibaba.fastjson.JSON;
//
//import com.itheima.common.R;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//
//@Slf4j
//@Component
//public class LoginInterceptor implements HandlerInterceptor {
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        log.info("-------拦截路径-------{}", request.getRequestURI());
//        if (request.getSession().getAttribute("employee") != null) {
//            Long empId = (Long) request.getSession().getAttribute("employee");
//
//            return true;
//        }
//        if (request.getSession().getAttribute("user") != null) {
//            Long userId = (Long) request.getSession().getAttribute("user");
//
//            return true;
//        }
//
//        //4.如果未登录就返回未登录结果 .通过输出流来响应页面数据.
//        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
//        return false;
//
//    }
//
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
//    }
//}
//
