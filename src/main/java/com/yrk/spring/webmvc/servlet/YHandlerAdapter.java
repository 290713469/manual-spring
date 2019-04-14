package com.yrk.spring.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Tom on 2019/4/13.
 */
public class YHandlerAdapter {
    public boolean supports(Object handler){ return (handler instanceof YHandlerMapping);}


    YModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        return null;
    }
}
