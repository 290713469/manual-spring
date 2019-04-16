/**
 * 
 */
package com.yrk.spring.demo.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yrk.spring.annotation.YAutowired;
import com.yrk.spring.annotation.YController;
import com.yrk.spring.annotation.YRequestMapping;
import com.yrk.spring.annotation.YRequestParam;
import com.yrk.spring.demo.service.IModifyService;
import com.yrk.spring.demo.service.IQueryService;
import com.yrk.spring.webmvc.servlet.YModelAndView;

/**
 * @author runkaiyang
 *
 */
@YController
@YRequestMapping("/web")
public class MyAction {
	
	@YAutowired IQueryService queryService;
	@YAutowired IModifyService modifyService;

	@YRequestMapping("/query.json")
	public YModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@YRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@YRequestMapping("/add*.json")
	public YModelAndView add(HttpServletRequest request,HttpServletResponse response,
			   @YRequestParam("name") String name,@YRequestParam("addr") String addr){
		String result = null;
		try {
			result = modifyService.add(name,addr);
			return out(response,result);
		} catch (Exception e) {
//			e.printStackTrace();
			Map<String,Object> model = new HashMap<String,Object>();
			model.put("detail",e.getCause().getMessage());
//			System.out.println(Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			return new YModelAndView("500",model);
		}

	}
	
	@YRequestMapping("/remove.json")
	public YModelAndView remove(HttpServletRequest request,HttpServletResponse response,
		   @YRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@YRequestMapping("/edit.json")
	public YModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@YRequestParam("id") Integer id,
			@YRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private YModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
