package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.Gson;
import com.ym.materials.gson.adapter.TypeAdapter;
import com.ym.materials.gson.adapter.TypeAdapterFactory;
import com.ym.materials.gson.internal.ObjectConstructor;
import com.ym.materials.gson.internal.ReflectionAccessor;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;
import com.ym.materials.gson.type.TypeToken;
import com.ym.materials.gson.type.Types;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ym on 2018/7/8.
 */
public class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {

    private final ObjectConstructor constructor;
    private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();

    public ReflectiveTypeAdapterFactory(ObjectConstructor constructor) {
        this.constructor = constructor;
    }

    @Override
    public <T> boolean accept(TypeToken<T> typeToken) {
        return typeToken.getRawType() instanceof Object;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        ObjectConstructor objectConstructor = constructor.get(typeToken);
        Map<String, BoundField> boundFields = getBoundFields(gson, typeToken, typeToken.getRawType());
        return new Adapter<>(objectConstructor, boundFields);
    }

    private static final class Adapter<T> extends TypeAdapter<T> {

        private final ObjectConstructor constructor;
        private final Map<String, BoundField> boundFields;

        public Adapter(ObjectConstructor constructor, Map<String, BoundField> boundFields) {
            this.constructor = constructor;
            this.boundFields = boundFields;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            out.beginObject();
            for (BoundField boundField : boundFields.values()) {
                String name = boundField.name;
                out.writeName(name);
                try {
                    boundField.write(out, value);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
            out.endObject();
        }

        @Override
        public T read(JsonReader in) throws IOException {
            return null;
        }
    }


    private Map<String, BoundField> getBoundFields(Gson gson, TypeToken<?> typeToken, Class<?> rawType) {
        Map<String, BoundField> result = new HashMap<>();
        if (rawType.isInterface()) {
            return result;
        }
        while (rawType != Object.class) {
            Field[] declaredFields = rawType.getDeclaredFields();
            for (Field field : declaredFields) {
                accessor.makeAccessible(field);
                Type fieldType = Types.resolve(typeToken.getType(), rawType, field.getGenericType());
                String name = field.getName();
                BoundField boundField = createBoundField(gson, field, name, TypeToken.get(fieldType));
                BoundField old = result.put(name, boundField);
                if (old != null) {
                    throw new RuntimeException("filed has exists");
                }
            }
            typeToken = TypeToken.get(Types.resolve(typeToken.getType(), rawType, rawType.getGenericSuperclass()));
            rawType = typeToken.getRawType();
        }
        return result;
    }

    private BoundField createBoundField(Gson gson, Field field, String name, TypeToken<?> typeToken) {
        TypeAdapter typeAdapter = gson.getAdapter(typeToken);
        Class<?> rawType = typeToken.getRawType();
        boolean isPrimitive = rawType.isPrimitive();
        return new BoundField(name) {
            @Override
            boolean writeField(Object value) throws IOException {
                return true;
            }

            @Override
            void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = field.get(value);
                typeAdapter.write(writer, fieldValue);
            }

            @Override
            void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = typeAdapter.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    field.set(value, fieldValue);
                }
            }
        };
    }

    private static abstract class BoundField {

        final String name;

        public BoundField(String name) {
            this.name = name;
        }

        abstract boolean writeField(Object value) throws IOException;

        abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;

        abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
    }
}
