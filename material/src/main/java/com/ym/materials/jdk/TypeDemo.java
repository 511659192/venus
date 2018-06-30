package com.ym.materials.jdk;

import org.junit.Test;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeDemo {


    @Test
    public void test() throws Exception {
        List<String> list = new ArrayList<>();
        Method method = list.getClass().getDeclaredMethod("add", new Class[]{Object.class});
        method.invoke(list, 1);
        method.invoke(list, 2);
        for (Object s : list) {
            System.out.println(s);
        }
        System.out.println(list.get(0));
    }

    class Person {

    }

    static class ParameterizedTypeBean {
        // 下面的 field 的 Type 属于 ParameterizedType
        Map<String, Person> map;
        Set<String> set1;
        Class<?> clz;
        Holder<String> holder;
        List<String> list;
        // Map<String,Person> map 这个 ParameterizedType 的 getOwnerType() 为 null，
        // 而 Map.Entry<String, String> entry 的 getOwnerType() 为 Map 所属于的 Type。
        Map.Entry<String, String> entry;
        // 下面的 field 的 Type 不属于ParameterizedType
        String str;
        Integer i;
        Set set;
        List aList;

        static class Holder<V> {

        }
    }

    @Test
    public void testParameterizedType() throws Exception {
        Field f = null;
        try {
            Field[] fields = ParameterizedTypeBean.class.getDeclaredFields();
            // 打印出所有的 Field 的 TYpe 是否属于 ParameterizedType
            for (int i = 0; i < fields.length; i++) {
                f = fields[i];
                System.out.println(f.getName() + " getGenericType() instanceof ParameterizedType " + (f.getGenericType() instanceof ParameterizedType));
            }
            getParameterizedTypeMes("map");
            getParameterizedTypeMes("entry");


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testTypeVariable() throws Exception {
        class TypeVariableBean<K extends InputStream & Closeable, V> {
            // K 的上边界是 InputStream
            K key;
            // 没有指定的话 ，V 的 上边界 属于 Object
            V value;
            // 不属于 TypeTypeVariable
            V[] values;
            String str;
            List<K> kList;
        }

        TypeVariableBean bean = new TypeVariableBean<FileInputStream, String>();
        Field fk = TypeVariableBean.class.getDeclaredField("key");
        TypeVariable keyType = (TypeVariable) fk.getGenericType();
        System.out.println(keyType.getName());
        System.out.println(keyType.getGenericDeclaration());

    }

    @Test
    public void testGenericArrayType() throws Exception {
        class GenericArrayTypeBean<T> {
            public void test(List<String>[] pTypeArray, T[] vTypeArray, List<String> list, String[] strings, Person[] ints) {
            }
        }
        Method method = GenericArrayTypeBean.class.getDeclaredMethods()[0];
        System.out.println(method);
        // public void test(List<String>[] pTypeArray, T[]
        // vTypeArray,List<String> list, String[] strings, Person[] ints)
        Type[] types = method.getGenericParameterTypes(); // 这是 Method 中的方法
        for (Type type : types) {
            System.out.println(type instanceof GenericArrayType);// 依次输出true，true，false，false，false
        }
    }

    @Test
    public void testWildcardType() throws Exception {
        class WildcardTypeBean {
            private List<? extends Number> a;  // a没有下界,
            private List<? super String> b;
            private List<String> c;
            private Class<?> aClass;
        }
        try {
            Field[] fields = WildcardTypeBean.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                Type type = field.getGenericType();
                String nameString = field.getName();
                System.out.print("下面开始打印" + nameString + "是否具有通配符");
                if (!(type instanceof ParameterizedType)) {
                    System.out.print("---------------------------");
                    continue;
                }
                ParameterizedType parameterizedType = (ParameterizedType) type;
                type = parameterizedType.getActualTypeArguments()[0];
                if (!(type instanceof WildcardType)) {
                    System.out.print("---------------------------");
                    continue;
                }
                WildcardType wildcardType = (WildcardType) type;
                Type[] lowerTypes = wildcardType.getLowerBounds();
                if (lowerTypes != null) {
                    System.out.print("下边界");
                    System.out.println(lowerTypes);
                }
                Type[] upTypes = wildcardType.getUpperBounds();
                if (upTypes != null) {
                    System.out.print("上边界");
                    System.out.println(upTypes);
                }
                System.out.print("---------------------------");

            }
            Field fieldA = WildcardTypeBean.class.getDeclaredField("a");
            Field fieldB = WildcardTypeBean.class.getDeclaredField("b");
            // 先拿到范型类型
            System.out.print(fieldA.getGenericType() instanceof ParameterizedType);
            System.out.print(fieldB.getGenericType() instanceof ParameterizedType);
            ParameterizedType pTypeA = (ParameterizedType) fieldA.getGenericType();
            ParameterizedType pTypeB = (ParameterizedType) fieldB.getGenericType();
            // 再从范型里拿到通配符类型
            System.out.print(pTypeA.getActualTypeArguments()[0] instanceof WildcardType);
            System.out.print(pTypeB.getActualTypeArguments()[0] instanceof WildcardType);
            WildcardType wTypeA = (WildcardType) pTypeA.getActualTypeArguments()[0];
            WildcardType wTypeB = (WildcardType) pTypeB.getActualTypeArguments()[0];
            // 方法测试
            System.out.println(wTypeA.getUpperBounds()[0]);
            System.out.println(wTypeB.getLowerBounds()[0]);
            // 看看通配符类型到底是什么, 打印结果为: ? extends java.lang.Number
            System.out.println(wTypeA);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }



    private void getParameterizedTypeMes(String fieldName) throws NoSuchFieldException {
        Field f;
        f = ParameterizedTypeBean.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        System.out.println(f.getGenericType());
        boolean b = f.getGenericType() instanceof ParameterizedType;
        System.out.print(b + " ");
        if (b) {
            ParameterizedType pType = (ParameterizedType) f.getGenericType();
            System.out.print(pType.getRawType());
            for (Type type : pType.getActualTypeArguments()) {
                System.out.print(type);
            }
            System.out.print(pType.getOwnerType()); // null
        }
    }
}
