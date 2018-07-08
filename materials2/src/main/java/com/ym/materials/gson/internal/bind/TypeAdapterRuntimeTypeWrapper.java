package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.Gson;
import com.ym.materials.gson.adapter.TypeAdapter;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by ym on 2018/7/8.
 */
public class TypeAdapterRuntimeTypeWrapper<T> extends TypeAdapter<T> {

    private final Gson gson;
    private final TypeAdapter<T> delegate;
    private final Type type;

    public TypeAdapterRuntimeTypeWrapper(Gson gson, TypeAdapter<T> delegate, Type type) {
        this.gson = gson;
        this.delegate = delegate;
        this.type = type;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        delegate.write(out, value);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        return delegate.read(in);
    }
}
