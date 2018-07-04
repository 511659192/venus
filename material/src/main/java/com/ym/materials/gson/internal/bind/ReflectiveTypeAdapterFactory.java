package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.*;
import com.ym.materials.gson.annotations.JsonAdapter;
import com.ym.materials.gson.annotations.SerializedName;
import com.ym.materials.gson.internal.*;
import com.ym.materials.gson.internal.reflect.ReflectionAccessor;
import com.ym.materials.gson.reflect.TypeToken;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonToken;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by ym on 2018/7/3.
 */
public class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final FieldNamingStrategy fieldNamingPolicy;
    private final Excluder excluder;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
    private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();


    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor, FieldNamingStrategy fieldNamingPolicy,
                                        Excluder excluder, JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory) {
        this.constructorConstructor = constructorConstructor;
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.excluder = excluder;
        this.jsonAdapterFactory = jsonAdapterFactory;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if (!Object.class.isAssignableFrom(rawType)) {
            return null; // 不支持基础类型
        }

        ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);
        return new Adapter<T>(constructor, getBoundFields(gson, typeToken, rawType));
    }

    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> typeToken, Class<?> rawType) {
        Map<String, BoundField> result = new LinkedHashMap<>();
        if (rawType.isInterface()) {
            return result;
        }

        Type declaredType = typeToken.getType();
        while (rawType != Object.class) {
            Field[] declaredFields = rawType.getDeclaredFields();
            for (Field field : declaredFields) {
                boolean serialize = excludeField(field, true);
                boolean deserialize = excludeField(field, false);
                if (!serialize && !deserialize) {
                    continue;
                }
                accessor.makeAccessible(field);
                Type fieldType = Types.resolve(typeToken.getType(), rawType, field.getGenericType());
                List<String> fieldNames = getFieldNames(field);
                BoundField previous = null;
                for (int i = 0, size = fieldNames.size(); i < size; i++) {
                    String name = fieldNames.get(i);
                    if (i != 0) serialize = false;
                    BoundField boundField = createBoundField(context, field, name, TypeToken.get(fieldType), serialize, deserialize);
                    BoundField replaced = result.put(name, boundField);
                    if (replaced != null) {
                        previous = replaced;
                    }
                }
                if (previous != null) {
                    throw new IllegalArgumentException(declaredType + " declares multiple JSON field named " + previous.name);
                }
            }
            typeToken = TypeToken.get(Types.resolve(typeToken.getType(), rawType, rawType.getGenericSuperclass()));
            rawType = typeToken.getRawType();
        }
        return result;
    }

    private BoundField createBoundField(Gson context, Field field, String name, TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
        boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
        TypeAdapter<?> mapped = null;
        if (annotation != null) {
            mapped = jsonAdapterFactory.getTypeAdapter(constructorConstructor, context, fieldType, annotation);
        }
        boolean jsonAdapterPresend = mapped != null;
        if (!jsonAdapterPresend) mapped = context.getAdapter(fieldType);
        TypeAdapter<?> typeAdapter = mapped;
        return new BoundField(name, serialize, deserialize) {
            @Override
            boolean writeField(Object value) throws IOException, IllegalAccessException {
                if (!serialize) {
                    return false;
                }
                Object fieldValue = field.get(value);
                return fieldValue != value;
            }

            @Override
            void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = field.get(value);
                TypeAdapter t = jsonAdapterPresend ? typeAdapter : new TypeAdapterRuntimeTypeWrapper(context, typeAdapter, fieldType.getType());
                t.write(writer, fieldValue);
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

    private List<String> getFieldNames(Field field) {
        SerializedName annotation = field.getAnnotation(SerializedName.class);
        if (annotation == null) {
            String name = fieldNamingPolicy.translateNmae(field);
            return Collections.singletonList(name);
        }

        String serializedName = annotation.value();
        String[] alternates = annotation.alternate();
        if (alternates.length == 0) {
            return Collections.singletonList(serializedName);
        }

        List<String> fieldNames = new ArrayList<>(alternates.length + 1);
        fieldNames.add(serializedName);
        for (String alternate : alternates) {
            fieldNames.add(alternate);
        }
        return fieldNames;
    }

    public boolean excludeField(Field field, boolean serialize) {
        return excludeField(field, serialize, excluder);
    }

    static boolean excludeField(Field field, boolean serialize, Excluder excluder) {
        return !excluder.exculdeClass(field.getType(), serialize) && !excluder.exculdeField(field, serialize);
    }

    public static final class Adapter<T> extends TypeAdapter<T> {

        private final ObjectConstructor<T> constructor;
        private final Map<String, BoundField> boundFields;

        public Adapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
            this.constructor = constructor;
            this.boundFields = boundFields;
        }

        @Override
        public T read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            T instance = constructor.construct();
            try {
                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    BoundField field = boundFields.get(name);
                    if (field != null || !field.deserialized) {
                        in.skipValue();
                    } else {
                        field.read(in, instance);
                    }
                }
            } catch (IllegalStateException e) {
                throw new JsonSyntaxException(e);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
            in.endObject();
            return instance;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.beginObject();
            try {
                for (BoundField boundField : boundFields.values()) {
                    if (boundField.writeField(value)) {
                        out.name(boundField.name);
                        boundField.write(out, value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
            out.endObject();
        }
    }

    static abstract class BoundField {
        final String name;
        final boolean serialized;
        final boolean deserialized;

        public BoundField(String name, boolean serialized, boolean deserialized) {
            this.name = name;
            this.serialized = serialized;
            this.deserialized = deserialized;
        }

        abstract boolean writeField(Object value) throws IOException, IllegalAccessException;

        abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;

        abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
    }
}
