// Copyright (C) 2019 Meituan
// All rights reserved
package com.example.demo.spring;

import org.springframework.stereotype.Component;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-07-02 17:05
 **/
@Component
public class InnerClassTest {

    @Component
    class InnerClass {

        public InnerClass() {
        }

        public void say() {
            System.out.println("------------------");
        }

    }
}