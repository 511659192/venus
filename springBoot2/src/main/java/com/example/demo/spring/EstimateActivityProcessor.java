// Copyright (C) 2019 Meituan
// All rights reserved
package com.example.demo.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
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
public class EstimateActivityProcessor implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    EstimateActivityProcessor getProcessor() {
        System.out.println("---------");
        return null;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Processor> beansOfType = applicationContext.getBeansOfType(Processor.class);
        for (Map.Entry<String, Processor> entry : beansOfType.entrySet()) {
            System.out.println(entry.getKey());
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    abstract class Processor {

    }

    @Component
    class UserProcessor extends Processor {

    }

    @Component
    class WaybillTotalCntProcessor extends Processor {

    }
}