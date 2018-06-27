package com.ym.materials.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * Created by ym on 2018/6/24.
 */
public class CglibDynamicProxyDemo {

    static class SampleClass {
        public void print(){
            System.out.println("hello world");
        }
    }

    public static void main(String[] args) {
        SampleClass sampleClass = new SampleClass();
        SampleClass sample = createCglibDynamicProxy(sampleClass);
        sample.print();
    }

    private static SampleClass createCglibDynamicProxy(SampleClass delegate) {
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(new CglibInterceptor(delegate));
        enhancer.setSuperclass(SampleClass.class);
        return (SampleClass) enhancer.create();
    }

    private static class CglibInterceptor implements MethodInterceptor {

        private Object delegate;

        public CglibInterceptor(Object delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, net.sf.cglib.proxy.MethodProxy methodProxy) throws Throwable {
            return methodProxy.invoke(delegate, objects);
        }
    }
}
