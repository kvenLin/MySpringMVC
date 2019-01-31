package com.clf.demo;

import com.clf.demo.service.DemoService;
import com.clf.mvcframework.annotation.MyAutowired;
import com.clf.mvcframework.annotation.MyController;
import com.clf.mvcframework.annotation.MyRequestMapping;
import com.clf.mvcframework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: clf
 * @Date: 19-1-31
 * @Description:
 */
@MyController
@MyRequestMapping("/demo")
public class DemoAction {

    @MyAutowired
    private DemoService demoService;

    @MyRequestMapping("/query")
    public void query(HttpServletRequest request,
                      HttpServletResponse response,
                      @MyRequestParam("/name") String name){
        String result = demoService.get(name);
    }

    @MyRequestMapping("/add")
    public void add(HttpServletRequest request,
                    HttpServletResponse response,
                    @MyRequestParam("a") Integer a,
                    @MyRequestParam("b") Integer b){
    }

    @MyRequestMapping("/remove")
    public void remove(HttpServletRequest request,
                       HttpServletResponse response,
                       @MyRequestParam("id") Integer id){
    }
}
