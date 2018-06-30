package com.ym.materials.jdk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class TypeClazz<T1, T2 extends Number> {

    public static void main(String ...args) {
        printlnFieldType("member");
        printlnFieldType("member2");
        printlnFieldType("collection");
        printlnFieldType("collection2");
        printlnFieldType("array");
        printlnMethodReturnType("method");
        printlnMethodParamTypes("method");
    }

    public T2 member;
    public T1 member2;
    public Collection<? extends Number> collection;
    public Collection<T2> collection2;
    public T2[] array;
    public <T extends Type> void method(T p1, T2 p2) {}

    public static Type printlnFieldType(String name) {
        System.out.println("name:"+name);
        Class clazzType = TypeClazz.class;
        Type type = null;
        try {
            Field field = clazzType.getDeclaredField(name);
            type = field.getGenericType();
            printlnType(field.getGenericType());
        } catch (Exception e) {}
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType)type;
            Type[] types = ptype.getActualTypeArguments();
            for (Type t:types) {
                System.out.print(">>");
                printlnType(t);
            }
        }
        return type;
    }

    public static void printlnMethodReturnType(String name) {
        System.out.println("printlnMethodReturnType name:"+name);
        Class clazzType = TypeClazz.class;
        try {
            Method[] ms = clazzType.getDeclaredMethods();
            Method method = null;
            for (Method m:ms) {
                if(m.getName().equals(name)) {
                    method = m;
                    break;
                }
            }
            printlnType(method.getGenericReturnType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printlnMethodParamTypes(String name) {
        System.out.println("printlnMethodParamTypes name:"+name);
        Class clazzType = TypeClazz.class;
        try {
            Method[] ms = clazzType.getDeclaredMethods();
            Method method = null;
            for (Method m:ms) {
                if(m.getName().equals(name)) {
                    method = m;
                    break;
                }
            }
            Type[] types = method.getGenericParameterTypes();
            for (Type t:types) {
                printlnType(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printlnType(Type t) {
        System.out.println("class:" + t.getClass() + " type:" + t.getTypeName());
    }
}