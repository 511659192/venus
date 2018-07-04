package com.ym.materials.gson.reflect;

import com.ym.materials.gson.internal.Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by ym on 2018/7/2.
 */
public class TypeToken<T> {

    final Class<? super T> rawType;
    final Type type;
    final int hashCode;

    public TypeToken() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = (Class<? super T>) Types.getRawType(this.type);
        this.hashCode = type.hashCode();
    }

    public TypeToken(Type type) {
        this.type = Types.canonicalize(type);
        this.rawType = (Class<? super T>) Types.getRawType(this.type);
        this.hashCode = type.hashCode();
    }

    private Type getSuperclassTypeParameter(Class<?> subClass) {
        Type superclass = subClass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("missing type parameter.");
        }
        ParameterizedType parameterizedType = (ParameterizedType) superclass;
        return Types.canonicalize(parameterizedType.getActualTypeArguments()[0]);
    }

    public Class<? super T> getRawType() {
        return rawType;
    }

    public Type getType() {
        return type;
    }

    public int getHashCode() {
        return hashCode;
    }

    public static <T> TypeToken<T> get(Class<T> type) {
        return new TypeToken<T>(type);
    }

    public static TypeToken get(Type type) {
        return new TypeToken<Object>(type);
    }
}
