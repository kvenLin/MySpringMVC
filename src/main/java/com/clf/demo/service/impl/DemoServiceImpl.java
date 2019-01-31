package com.clf.demo.service.impl;

import com.clf.demo.service.DemoService;
import com.clf.mvcframework.annotation.MyService;

/**
 * @Author: clf
 * @Date: 19-1-31
 * @Description:
 */
@MyService
public class DemoServiceImpl implements DemoService {
    @Override
    public String get(String name) {
        return "success, My name is " + name;
    }
}
