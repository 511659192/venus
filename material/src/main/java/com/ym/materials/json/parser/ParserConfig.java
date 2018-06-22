package com.ym.materials.json.parser;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by ym on 2018/6/21.
 */
public class ParserConfig {
    public static void parserAllFieldToCache(Class<?> clazz, Map<String, Field> filedCacheMap) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String name = declaredField.getName();
            if (!filedCacheMap.containsKey(name)) {
                filedCacheMap.put(name, declaredField);
            }
        }

        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            parserAllFieldToCache(clazz.getSuperclass(), filedCacheMap);
        }
    }
}
