package com.ym.materials.gson.internal.bind;

import com.ym.materials.gson.*;
import com.ym.materials.gson.internal.LazilyParsedNumber;
import com.ym.materials.gson.reflect.TypeToken;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonToken;
import com.ym.materials.gson.stream.JsonWriter;
import org.omg.CORBA.PUBLIC_MEMBER;

import static com.ym.materials.gson.stream.JsonToken.*;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ym on 2018/7/2.
 */
public class TypeAdapters {

    public static final TypeAdapter<JsonElement> JSON_ELEMENT = new TypeAdapter<JsonElement>() {
        @Override
        public void write(JsonWriter out, JsonElement value) throws IOException {
            if (value == null || value.isJsonNull()) {
                out.nullValue();
                return;
            }

            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    out.value(primitive.getAsNumber());
                    return;
                }

                if (primitive.isBoolean()) {
                    out.value(primitive.getAsBoolean());
                    return;
                }

                out.value(primitive.getAsString());
                return;
            }

            if (value.isJsonArray()) {
                out.beginArray();
                for (JsonElement jsonElement : value.getAsJsonArray()) {
                    write(out, jsonElement);
                }
                out.endArray();
                return;
            }

            if (value.isJsonObject()) {
                out.beginObject();
                for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                    out.name(entry.getKey());
                    write(out, entry.getValue());
                }
                out.endObject();
                return;
            }

            throw new IllegalArgumentException("could not write " + value.getClass());
        }

        @Override
        public JsonElement read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case STRING:
                    return new JsonPrimitive(in.nextString());
                case NUMBER:
                    return new JsonPrimitive(new LazilyParsedNumber(in.nextString()));
                case BOOLEAN:
                    return new JsonPrimitive(in.nextBoolean());
                case NULL:
                    in.nextNull();
                    return JsonNull.INSTANCE;
                case BEGIN_ARRAY:
                    JsonArray array = new JsonArray();
                    in.beginArray();
                    while (in.hasNext()) {
                        array.add(read(in));
                    }
                    in.endArray();
                    return array;
                case BEFIN_OBJECT:
                    JsonObject jsonObject = new JsonObject();
                    in.beginObject();
                    while (in.hasNext()) {
                        jsonObject.add(in.nextName(), read(in));
                    }
                    in.endObject();
                    return jsonObject;
                case END_DOCUMENT:
                case NAME:
                case END_OBJECT:
                case END_ARRAY:
                default:
                    throw new IllegalArgumentException();

            }
        }
    };

    public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
        @Override
        public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            if (peek == JsonToken.BOOLEAN) {
                return Boolean.toString(in.nextBoolean());
            }

            return in.nextString();
        }
    };

    public static final TypeAdapter<Number> INTEGER = new TypeAdapter<Number>() {
        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }

        @Override
        public Number read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            try {
                return in.nextInt();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }
    };

    public static final TypeAdapter<Number> LONG = new TypeAdapter<Number>() {
        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }

        @Override
        public Number read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            try {
                return in.nextLong();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }
    };

    public static final TypeAdapterFactory JSON_ELEMENT_FACTORY = newTypeHierarchyFactory(JsonElement.class, JSON_ELEMENT);
    public static final TypeAdapterFactory STRING_FACTORY = newFactory(String.class, STRING);
    public static final TypeAdapterFactory INTEGER_FACTORY = newFactory(int.class, Integer.class, INTEGER);

    public static <T> TypeAdapterFactory newTypeHierarchyFactory(final Class<T> clazz, final TypeAdapter<T> typeAdapter) {
        return new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tTypeToken) {
                return null;
            }
        };
    }

    public static <T> TypeAdapterFactory newFactory(Class<T> unboxed, Class<T> boxed, TypeAdapter<? super T> typeAdapter) {
        return new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                return rawType == unboxed || rawType == boxed ? (TypeAdapter<T>) typeAdapter : null;
            }
        };
    }

    public static <T> TypeAdapterFactory newFactory(Class<T> type, TypeAdapter<T> typeAdapter) {
        return new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                return typeToken.getRawType() == type ? (TypeAdapter<T>) typeAdapter : null;
            }
        };
    }
}
