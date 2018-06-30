package com.ym.materials.guava.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeToken <T> {

    final Class<? super T> rawType;
    final Type type;
    final int hashCode;

    public TypeToken() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = null;
        this.hashCode = type.hashCode();
    }

    private Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (subclass instanceof Class) {
            throw new RuntimeException("missing type paramer");
        }
        ParameterizedType parameterizedType = (ParameterizedType) superclass;
        return parameterizedType.getActualTypeArguments()[0];
    }

    public static void main(String[] args) {
        new TypeToken<String>();
    }
}
