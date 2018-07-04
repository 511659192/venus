package com.ym.materials.gson;

import org.junit.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by ym on 2018/7/2.
 */
public class FieldAttributes {

    final Field field;

    public FieldAttributes(Field field) {
        Assert.assertNotNull(field);
        this.field = field;
    }

    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    public String getName() {
        return field.getName();
    }

    public Type getDeclaredType() {
        return field.getGenericType();
    }

    public Class<?> getDeclardType() {
        return field.getType();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return field.getAnnotation(annotation);
    }

    public Collection<Annotation> getAnnotations() {
        return Arrays.asList(field.getAnnotations());
    }

    public boolean hasModifier(int modifier) {
        return (field.getModifiers() & modifier) != 0;
    }

    Object get(Object instance) throws IllegalAccessException {
        return field.get(instance);
    }

    boolean isSynthetic() {
        return field.isSynthetic();
    }
}
