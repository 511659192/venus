package com.ym.materials.json.util;

import com.google.common.collect.Maps;
import com.sun.org.apache.regexp.internal.REUtil;
import com.ym.materials.json.JSON;
import com.ym.materials.json.PropertyNamingStrategy;
import com.ym.materials.json.annotation.JSONType;
import com.ym.materials.json.parser.ParserConfig;
import com.ym.materials.json.serializer.SerializeBeanInfo;
import org.apache.tools.ant.taskdefs.Classloader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.IllegalFormatFlagsException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ym on 2018/6/21.
 */
public class TypeUtils {

    public static SerializeBeanInfo buildBeanInfo(Class<?> beanType, Map<String, String> aliasMap, PropertyNamingStrategy propertyNamingStrategy, boolean fieldBased) {
        JSONType jsonType = getAnnotation(beanType, JSONType.class);
        String[] orders = null;
        int features;
        String typeName = null, typeKey = null;
        if (jsonType != null) {

        } else {
            features = 0;
        }

        Map<String, Field> fieldCacheMap = Maps.newHashMap();
        ParserConfig.parserAllFieldToCache(beanType, fieldCacheMap);
        computeGetters(beanType, jsonType, aliasMap, fieldCacheMap, false, propertyNamingStrategy);

        return null;
    }

    private static void computeGetters(Class<?> clazz, JSONType jsonType, Map<String, String> aliasMap, Map<String, Field> fieldCacheMap, boolean sorted, PropertyNamingStrategy propertyNamingStrategy) {
        Map<String, FieldInfo> fieldInfoMap = new LinkedHashMap<>();
        Constructor[] constructors = null;

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getReturnType().equals(Void.TYPE)) {
                continue;
            }
            if (method.getParameterTypes().length != 0) {
                continue;
            }
            if (method.getReturnType() == Classloader.class) {
                continue;
            }

        }

    }

    private static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass) {
        T annotation = clazz.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        if (clazz.getAnnotations().length > 0) {
            for (Annotation a : clazz.getAnnotations()) {
                annotation = a.annotationType().getAnnotation(annotationClass);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }
}
