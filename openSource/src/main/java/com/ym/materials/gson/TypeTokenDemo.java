package com.ym.materials.gson;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import sun.net.www.content.text.Generic;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by ym on 2018/7/3.
 */
public class TypeTokenDemo<E> {

    public static void main(String[] args) throws Exception {
//        TypeToken<Map<? extends Number, ? extends String>> token = new TypeToken<Map<? extends Number, ? extends String>>(){};
//        System.out.println($Gson$Types.getCollectionElementType(token.getType(), token.getRawType()));
//        System.out.println("done");
        new TypeTokenDemo<String>().test();
    }

    @Test
    public void test() throws Exception {
        TypeToken<Map<E, ? extends Number>> token = new TypeToken<Map<E, ? extends Number>>(){};
        System.out.println($Gson$Types.resolve(token.getType(), token.getRawType(), getGenericSupertype(token.getType(), token.getRawType(), Number.class)));
        System.out.println("done");
    }

    static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) {
            return context;
        }

        // we skip searching through interfaces if unknown is an interface
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        // check our supertypes
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        // we can't resolve this further
        return toResolve;
    }
}
