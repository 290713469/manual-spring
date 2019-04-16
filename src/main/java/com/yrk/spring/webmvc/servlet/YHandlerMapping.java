package com.yrk.spring.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;


public class YHandlerMapping {

    private Object controller;	//保存方法对应的实�?
    private Method method;		//保存映射的方�?
    private Pattern pattern;    //URL的正则匹�?

    public YHandlerMapping(Pattern pattern,Object controller, Method method) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
