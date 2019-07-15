package com.example.demo.spring;

import com.google.common.collect.Maps;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Objects;

/**
 * Created by ym on 2018/2/1.
 */
public class ApplicationContextTest {

    public static void main(String[] args) {
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, Thread.currentThread().getContextClassLoader().getResource("").getPath());
        ApplicationContext context = new ClassPathXmlApplicationContext("aop.xml");
        TestBean bean = (TestBean) context.getBean("testBean");
        bean.test();
        InnerClassTest.InnerClass bean1 = context.getBean(InnerClassTest.InnerClass.class);
        bean1.say();
        EstimateActivityProcessor bean2 = context.getBean(EstimateActivityProcessor.class);
        bean2.getProcessor();
//        InnerHander innerHander = context.getBean(InnerHander.class);
//        innerHander.test();

        HandlerEnum handlerEnum = context.getBean(HandlerEnum.class);
        handlerEnum.runA();
    }
}
