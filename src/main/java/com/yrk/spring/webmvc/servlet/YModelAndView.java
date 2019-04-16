package com.yrk.spring.webmvc.servlet;

import java.util.Map;

public class YModelAndView {
	
	private String viewName;
    private Map<String,?> model;

    public YModelAndView(String viewName) { this.viewName = viewName; }

    public YModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }


    public Map<String, ?> getModel() {
        return model;
    }

}
