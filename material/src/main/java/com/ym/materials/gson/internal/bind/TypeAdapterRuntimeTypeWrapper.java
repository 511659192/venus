package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.Gson;
import com.ym.materials.gson.TypeAdapter;
import com.ym.materials.gson.reflect.TypeToken;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Created by ym on 2018/7/3.
 */
public class TypeAdapterRuntimeTypeWrapper<T> extends TypeAdapter<T> {

    private final Gson context;
    private final TypeAdapter<T> delegate;
    private final Type type;

    TypeAdapterRuntimeTypeWrapper(Gson context, TypeAdapter<T> delegate, Type type) {
        this.context = context;
        this.delegate = delegate;
        this.type = type;
    }

    @Override
    public T read(JsonReader in) throws IOException {
        return delegate.read(in);
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        TypeAdapter chosen = delegate;
        Type runtimeType = getRuntimeTypeIfMoreSpecific(type, value);
        if (runtimeType != type) { // 集合类型 可能有多重类型
            TypeAdapter runtimeTypeAdapter = context.getAdapter(TypeToken.get(runtimeType));
            if (!(runtimeTypeAdapter instanceof ReflectiveTypeAdapterFactory.Adapter)) { // 用户注册具体类型
                chosen = runtimeTypeAdapter;
            } else if (!(delegate instanceof ReflectiveTypeAdapterFactory.Adapter)) { // 用户注册声明类型
                chosen = delegate;
            } else {
                chosen = runtimeTypeAdapter; // 反射类型 或者非用户注册类型
            }
        }
        chosen.write(out, value);
    }

    private Type getRuntimeTypeIfMoreSpecific(Type type, Object value) {
        if (value != null && (type == Object.class || type instanceof TypeVariable<?> || type instanceof Class<?>)) {
            type = value.getClass();
        }
        return type;
    }
}
