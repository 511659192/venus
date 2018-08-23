package com.ym.materials.jdk;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by ym on 2018/8/23.
 */
public class ProxyDemo {

    public interface IHello {
        void sayHello();
    }

    static class Hello implements IHello {
        public void sayHello() {
            System.out.println("Hello world!!");
        }
    }

    //自定义InvocationHandler
    static class HWInvocationHandler implements InvocationHandler {
        //目标对象
        private Object target;

        public HWInvocationHandler(Object target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("------插入前置通知代码-------------");
            //执行相应的目标方法
            Object rs = method.invoke(target, args);
            System.out.println("------插入后置处理代码-------------");
            return rs;
        }
    }

    public static void main(String[] args) throws Exception {
        //生成$Proxy0的class文件
//        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        //获取动态代理类
        Class proxyClazz = Proxy.getProxyClass(IHello.class.getClassLoader(), IHello.class);
        //获得代理类的构造函数，并传入参数类型InvocationHandler.class
        Constructor constructor = proxyClazz.getConstructor(InvocationHandler.class);
        //通过构造函数来创建动态代理对象，将自定义的InvocationHandler实例传入
        IHello iHello = (IHello) constructor.newInstance(new HWInvocationHandler(new Hello()));
        //通过代理对象调用目标方法
        iHello.sayHello();
    }

    @Test
    public void testJdkProxy() throws Exception {
        Hello hello = new Hello();
        Object instance = Proxy.newProxyInstance(hello.getClass().getClassLoader(), hello.getClass().getInterfaces(), new HWInvocationHandler(hello));
        Class<?>[] interfaces = instance.getClass().getInterfaces();
        for (Class<?> item : interfaces) {
            System.out.println(item.getName());
        }

        Class<?> superclass = instance.getClass().getSuperclass();
        System.out.println(superclass.getName());
        ((IHello) instance).sayHello();
        System.out.println();
    }
}
