package com.ym.materials.jdk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UserMethod {

    interface SuperClass<T> {
        void method(T t);
    }

    static class AClass implements SuperClass<String> {

        @Override
        public void method(String s) {
            System.out.println(s);
        }
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AClass obj = new AClass();
        Method m = AClass.class.getMethod("method", String.class);
        m.invoke(obj, "XXXXXXXXXXXXXXXXXX");
        System.out.println(m.isBridge());
        m = AClass.class.getMethod("method", Object.class);
        m.invoke(obj, "##################");
        System.out.println(m.isBridge());
    }
}
