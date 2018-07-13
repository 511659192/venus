package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.adapter.TypeAdapter;
import com.ym.materials.gson.adapter.TypeAdapterFactory;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by ym on 2018/7/8.
 */
public class StringTypeAdapterFactory extends PrimitiveTypeAdapterFacroty {

    public static final TypeAdapterFactory STRING_FACTORY = newFactory(String.class, new TypeAdapter<String>() {
        @Override
        public void write(JsonWriter out, String value) throws IOException {
            out.writeValue(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            return new StringBuffer(in.nextString()).toString();
        }
    });
}
