package com.ym.materials.gson;

import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.*;

/**
 * Created by ym on 2018/7/2.
 */
public abstract class TypeAdapter<T> {

    public abstract T read(JsonReader in) throws IOException;
    public abstract void write(JsonWriter out, T value) throws IOException;

    public final void toJson(Writer out, T value) throws IOException {
        JsonWriter writer = new JsonWriter(out);
        write(writer, value);
    }

    public final String toJson(T value) {
        StringWriter stringWriter = new StringWriter();
        try {
            toJson(stringWriter, value);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return stringWriter.toString();
    }

    public final T fromJson(Reader in) throws IOException {
        JsonReader reader = new JsonReader(in);
        return read(reader);
    }

    public final T fromJson(String json) throws IOException {
        return fromJson(new StringReader(json));
    }
}
