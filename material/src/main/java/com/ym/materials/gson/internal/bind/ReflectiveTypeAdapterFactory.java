package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.FieldNamingStrategy;
import com.ym.materials.gson.Gson;
import com.ym.materials.gson.TypeAdapter;
import com.ym.materials.gson.TypeAdapterFactory;
import com.ym.materials.gson.internal.ConstructorConstructor;
import com.ym.materials.gson.internal.Excluder;
import com.ym.materials.gson.reflect.TypeToken;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by ym on 2018/7/3.
 */
public class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final FieldNamingStrategy fieldNamingPolicy;
    private final Excluder excluder;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor, FieldNamingStrategy fieldNamingPolicy,
                                        Excluder excluder, JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory) {
        this.constructorConstructor = constructorConstructor;
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.excluder = excluder;
        this.jsonAdapterFactory = jsonAdapterFactory;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        return null;
    }

    public static final class Adapter<T> extends TypeAdapter<T> {

        @Override
        public T read(JsonReader in) throws IOException {
            return null;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {

        }
    }
}
