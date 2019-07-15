// Copyright (C) 2019 Meituan
// All rights reserved
package com.example.demo.spring;

import org.springframework.stereotype.Component;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-07-02 17:22
 **/
@Component
public enum HandlerEnum {

    //
    Test(1){
        @Override
        public void runA() {
            System.out.println("22222222");
        }
    }
    ;


    private int code;

    HandlerEnum(int code) {
        this.code = code;
    }

    public static HandlerEnum getByCode(int code) {
        for (HandlerEnum value : HandlerEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }

        return null;
    }

    public void runA() {
        System.out.println("111111");
    };


}