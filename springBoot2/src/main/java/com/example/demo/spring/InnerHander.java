// Copyright (C) 2019 Meituan
// All rights reserved
package com.example.demo.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-07-02 17:22
 **/
@Component
public class InnerHander {

    @Autowired
    private InnerHander innerHander;


    private static EstimateActivityProcessor estimateActivityProcessor;

    @Autowired
    public void setEstimateActivityProcessor(EstimateActivityProcessor estimateActivityProcessor) {
        InnerHander.estimateActivityProcessor = estimateActivityProcessor;
    }

    @Component
    enum Inner {
        //
        Test(1){
            @Override
            void run() {
                estimateActivityProcessor.getProcessor();
            }
        }
        ;


        private int code;

        Inner() {
        }

        Inner(int code) {
            this.code = code;
        }

        public static Inner getByCode(int code) {
            for (Inner value : Inner.values()) {
                if (value.code == code) {
                    return value;
                }
            }

            return null;
        }

        abstract void run();



    }

    public void test() {
        Inner inner = Inner.getByCode(1);
        inner.run();
    }
}