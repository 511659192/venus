package com.ym.materials.gson;

import com.google.gson.JsonIOException;
import com.ym.materials.gson.adapter.TypeAdapter;
import com.ym.materials.gson.adapter.TypeAdapterFactory;
import com.ym.materials.gson.internal.ObjectConstructor;
import com.ym.materials.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.ym.materials.gson.internal.bind.StringTypeAdapterFactory;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;
import com.ym.materials.gson.type.TypeToken;
import org.apache.log4j.pattern.BridgePatternParser;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ym.materials.gson.internal.Preconditions.checkNotNull;

/**
 * Created by ym on 2018/7/8.
 */
public class Gson {

    private Map<TypeToken, TypeAdapter> typeTokenCache = new HashMap<>();
    private ThreadLocal<Map<TypeToken, FutureTypeAdapter>> calls = new ThreadLocal<>();
    private static final ObjectConstructor objectConstructor = ObjectConstructor.instance;
    private static final List<TypeAdapterFactory> factories;

    private boolean serializeNulls = true;

    static {
        factories = new ArrayList<>();
        factories.add(StringTypeAdapterFactory.STRING_FACTORY);
        factories.add(new ReflectiveTypeAdapterFactory(objectConstructor));
    }

    public String toJson(Object src) {
        checkNotNull(src);
        return toJson(src, src.getClass());
    }

    private String toJson(Object src, Type typeOfSrc) {
        StringWriter writer = new StringWriter();
        toJson(src, typeOfSrc, writer);
        return writer.toString();
    }

    private void toJson(Object src, Type typeOfSrc, StringWriter writer) {
        try {
            JsonWriter jsonWritter = newJsonWritter(writer);
            toJson(src, typeOfSrc, jsonWritter);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    private JsonWriter newJsonWritter(StringWriter writer) {
        JsonWriter jsonWritter = new JsonWriter(writer);
        jsonWritter.setSerializeNulls(serializeNulls);
        return jsonWritter;
    }

    public <T> T fromJson(String json, Class<T> classOfJson) {
        return fromJson(json, ((Type) classOfJson));
    }

    public <T> T fromJson(String json, Type typeOfJson) {
        StringReader stringReader = new StringReader(json);
        return fromJson(typeOfJson, stringReader);

    }

    private <T> T fromJson(Type typeOfJson, StringReader stringReader) {
        JsonReader jsonReader = newJsonReader(stringReader);
        T result = fromJson(typeOfJson, jsonReader);
        return result;
    }

    private <T> T fromJson(Type typeOfJson, JsonReader reader) {
        boolean oldLenient = reader.isLenient();
        reader.setLenient(true);
        try {
//            reader.peek();
            TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(typeOfJson);
            TypeAdapter<T> adapter = getAdapter(typeToken);
            T result = adapter.read(reader);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            reader.setLenient(oldLenient);
        }
    }

    private JsonReader newJsonReader(StringReader stringReader) {
        JsonReader jsonReader = new JsonReader(stringReader);
        return jsonReader;
    }


    private void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws IOException {
        TypeAdapter<?> typeAdapter = getAdapter(TypeToken.get(typeOfSrc));
        ((TypeAdapter<Object>) typeAdapter).write(writer, src);
    }

    public <T> TypeAdapter<T> getAdapter(TypeToken<T> typeToken) {
        TypeAdapter typeAdapter = typeTokenCache.get(typeToken);
        if (typeAdapter != null) {
            return typeAdapter;
        }

        boolean needClean = false;
        Map<TypeToken, FutureTypeAdapter> threadCalls = calls.get();
        if (threadCalls == null) {
            threadCalls = new HashMap<>();
            calls.set(threadCalls);
            needClean = true;
        }

        FutureTypeAdapter futureTypeAdapter = threadCalls.get(typeToken);
        if (futureTypeAdapter != null) {
            return futureTypeAdapter;
        }

        try {
            futureTypeAdapter = new FutureTypeAdapter();
            threadCalls.put(typeToken, futureTypeAdapter);

            for (TypeAdapterFactory factory : factories) {
                if (factory.accept(typeToken)) {
                    typeAdapter = factory.create(this, typeToken);
                    checkNotNull(typeAdapter);
                    futureTypeAdapter.setDelegate(typeAdapter);
                    typeTokenCache.put(typeToken, typeAdapter);
                    return typeAdapter;
                }
            }

            throw new IllegalArgumentException("typeAdapter not found" + typeToken);
        } finally {
            threadCalls.remove(typeToken);
            if (needClean) {
                calls.remove();
            }
        }
    }

    static class FutureTypeAdapter<T> extends TypeAdapter<T> {

        private TypeAdapter<T> delegate;

        public FutureTypeAdapter() {
        }

        public void setDelegate(TypeAdapter<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            checkNotNull(delegate);
            delegate.write(out, value);
        }

        @Override
        public T read(JsonReader in) throws IOException {
            checkNotNull(delegate);
            return delegate.read(in);
        }
    }

    public static void main(String[] args) {
        Person person = new Person();
        person.setName("person");
        Info info = new Info();
        info.setAddress("address");
//        person.setInfo(info);
        Gson gson = new Gson();
        String s = gson.toJson(person);
        System.out.println(s);

//        Gson gson = new Gson();
        String json = "{\"name\":\"person\"}";
        Person person1 = gson.fromJson(json, Person.class);
        System.out.println(person1);
    }

    static class Person {
        String name;
        Info info;

        public Info getInfo() {
            return info;
        }

        public void setInfo(Info info) {
            this.info = info;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", info=" + info +
                    '}';
        }
    }

    static class Info {
        String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "address='" + address + '\'' +
                    '}';
        }
    }
}
