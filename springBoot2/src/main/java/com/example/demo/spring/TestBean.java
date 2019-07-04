package com.example.demo.spring;

import org.springframework.stereotype.Component;

/**
 * Created by ym on 2018/4/10.
 */
@Component
public class TestBean {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void test() {
        System.out.println("test");
    }
}
