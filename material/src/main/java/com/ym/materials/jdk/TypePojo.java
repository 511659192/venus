package com.ym.materials.jdk;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class TypePojo <E> {

    private int primitive_type;
    private Map<String, ? extends Integer> parameterized_type;
    private E type_variable;
    private E[] array_type;

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
