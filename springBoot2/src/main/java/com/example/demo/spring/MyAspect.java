package com.example.demo.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by ym on 2018/4/10.
 */
@Aspect
public class MyAspect {
    @Pointcut("execution(* com.example.demo.spring..test(..))")
    public void test() {
    }

    @Before("test()")
    public void before() {
        System.out.println("before");
    }

    @Around("test()")
    public Object around(ProceedingJoinPoint p) {
        System.out.println("before around");
        Object val = null;
        try {
            val = p.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println("after around");
        return val;
    }
}
