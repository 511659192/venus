package com.ym.materials.jdk;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TypePojo <E> {

    private int primitive_type;
    private Map<String, ? extends Integer> parameterized_type;
    private E type_variable;
    private E[] array_type;
    private List<String> list1;
    private List<Integer> list2;
    private List<String> list3;

    @Test
    public void testTypeEqual() throws Exception {
        Field field1 = TypePojo.class.getDeclaredField("list1");
        Field field2 = TypePojo.class.getDeclaredField("list2");
        Field field3 = TypePojo.class.getDeclaredField("list3");

        Type genericType = field1.getGenericType();
        Type genericType1 = field2.getGenericType();
        Type genericType2 = field3.getGenericType();
        System.out.println(genericType);
        System.out.println(genericType1);
        System.out.println(genericType2);
        System.out.println(genericType.equals(genericType2));
    }

    public static void main(String[] args) {
        Class pojoClass = TypePojo.class;
        try {
            // get primitive type
            System.out.println("get primitive type-->");
            Field primitive_type_field = pojoClass.getDeclaredField("primitive_type");
            Type primitive_type = primitive_type_field.getGenericType();
            System.out.println("Type Class: " + primitive_type.getClass() + " Type: " + primitive_type);
            // get parameterized type
            System.out.println("get parameterized type-->");
            Field parameterized_type_field = pojoClass.getDeclaredField("parameterized_type");
            Type parameterized_type = parameterized_type_field.getGenericType();
            System.out.println("Type Class: " + parameterized_type.getClass() + " Type: " + parameterized_type);
            // get WildcardType
            System.out.println("get actual types-->");
            ParameterizedType real_parameterized_type = (ParameterizedType) parameterized_type;
            Type[] actualTypes = real_parameterized_type.getActualTypeArguments();
            for (Type type : actualTypes) {
                System.out.println("Type Class: " + type.getClass() + " Type: " + type);
            }
            // get type variables
            System.out.println("get type variables-->");
            Field type_variable_field = pojoClass.getDeclaredField("type_variable");
            Type type_variable = type_variable_field.getGenericType();
            System.out.println("Type Class: " + type_variable.getClass() + " Type: " + type_variable);
            // get array type
            System.out.println("get array type-->");
            Field array_type_field = pojoClass.getDeclaredField("array_type");
            Type array_type = array_type_field.getGenericType();
            System.out.println("Type Class: " + array_type.getClass() + " Type: " + array_type);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
