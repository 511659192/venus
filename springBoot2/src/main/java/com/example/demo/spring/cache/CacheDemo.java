package com.example.demo.spring.cache;

import org.junit.Test;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CacheDemo {

    @Test
    public void test() {
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY,Thread.currentThread().getContextClassLoader().getResource("").getPath());
        ApplicationContext context = new ClassPathXmlApplicationContext("cache.xml");
        CacheService service = (CacheService) context.getBean("cacheService");
        service.getName("name");
        service.getName("name");
        service.getName("name");
    }
}
