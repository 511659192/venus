package com.ym.materials.gson.adapter;

import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by ym on 2018/7/8.
 */
public abstract class TypeAdapter<T> {
    public abstract void write(JsonWriter out, T value) throws IOException;

    public abstract T read(JsonReader in) throws IOException;

}
