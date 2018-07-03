package com.ym.materials.gson;

import com.ym.materials.gson.internal.ConstructorConstructor;
import com.ym.materials.gson.internal.Excluder;
import com.ym.materials.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.ym.materials.gson.internal.bind.ObjectTypeAdapter;
import com.ym.materials.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.ym.materials.gson.internal.bind.TypeAdapters;
import com.ym.materials.gson.reflect.TypeToken;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonToken;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ym on 2018/7/2.
 */
public class Gson {

    static final boolean DEFAULT_JSON_NON_EXECUTABLE = false;
    static final boolean DEFAULT_LENIENT = false;
    static final boolean DEFAULT_PRETTY_PRINT = false;
    static final boolean DEFAULT_ESCAPE_HTML = true;
    static final boolean DEFAULT_SERIALIZE_NULLS = false;
    static final boolean DEFAULT_COMPLEX_MAP_KEYS = false;
    static final boolean DEFAULT_SPECIALIZE_FLOAT_VALUES = false;

    private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache = new ConcurrentHashMap<TypeToken<?>, TypeAdapter<?>>();
    private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls
            = new ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>>();
    private static final TypeToken<?> NULL_KEY_SURROGATE = TypeToken.get(Object.class);

    final List<TypeAdapterFactory> factories;
    final List<TypeAdapterFactory> builderFactories;
    private final List<TypeAdapterFactory> builderHierarchyFactories;
    private final List<TypeAdapterFactory> factoriesToBeAdded;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory = null;
    private final Excluder excluder;
    private final FieldNamingStrategy fieldNamingStrategy;
    private final Map<Type, InstanceCreator<?>> instanceCreators;
    private final ConstructorConstructor constructorConstructor;
    private final boolean serializeNulls;
    private final boolean complexMapKeySerialization;
    private final boolean generateNonExecutableGson;
    private final boolean htmlSafe;
    private final boolean prettyPrinting;
    private final boolean lenient;
    private final boolean serializeSpecialFloatingPointValues;
    private final LongSerializationPolicy longSerializationPolicy;
    private final String datePattern;
    private final int dateStyle;
    private final int timeStyle;

    public Gson() {
        this(Excluder.DEFAULT,
                FieldNamingPolicy.INENTITY,
                Collections.<Type, InstanceCreator<?>>emptyMap(),
                DEFAULT_SERIALIZE_NULLS,
                DEFAULT_COMPLEX_MAP_KEYS,
                DEFAULT_JSON_NON_EXECUTABLE,
                DEFAULT_ESCAPE_HTML,
                DEFAULT_PRETTY_PRINT,
                DEFAULT_LENIENT,
                DEFAULT_SPECIALIZE_FLOAT_VALUES,
                LongSerializationPolicy.DEFAULT,
                null,
                DateFormat.DEFAULT,
                DateFormat.DEFAULT,
                Collections.<TypeAdapterFactory>emptyList(),
                Collections.<TypeAdapterFactory>emptyList(),
                Collections.<TypeAdapterFactory>emptyList());
    }


    Gson(final Excluder excluder,  // 排除策略
         final FieldNamingStrategy fieldNamingStrategy, // 命名策略
         final Map<Type, InstanceCreator<?>> instanceCreators, // 实例创建器
         boolean serializeNulls, // 是否序列化空值
         boolean complexMapKeySerialization,
         boolean generateNonExecutableGson,
         boolean htmlSafe,
         boolean prettyPrinting,
         boolean lenient,
         boolean serializeSpecialFloatingPointValues,
         LongSerializationPolicy longSerializationPolicy,
         String datePattern,
         int dateStyle,
         int timeStyle,
         List<TypeAdapterFactory> builderFactories,
         List<TypeAdapterFactory> builderHierarchyFactories,
         List<TypeAdapterFactory> factoriesToBeAdded) {

        this.excluder = excluder;
        this.fieldNamingStrategy = fieldNamingStrategy;
        this.instanceCreators = instanceCreators;
        this.constructorConstructor = new ConstructorConstructor(instanceCreators);
        this.serializeNulls = serializeNulls;
        this.complexMapKeySerialization = complexMapKeySerialization;
        this.generateNonExecutableGson = generateNonExecutableGson;
        this.htmlSafe = htmlSafe;
        this.prettyPrinting = prettyPrinting;
        this.lenient = lenient;
        this.serializeSpecialFloatingPointValues = serializeSpecialFloatingPointValues;
        this.longSerializationPolicy = longSerializationPolicy;
        this.datePattern = datePattern;
        this.dateStyle = dateStyle;
        this.timeStyle = timeStyle;
        this.builderFactories = builderFactories;
        this.builderHierarchyFactories = builderHierarchyFactories;
        this.factoriesToBeAdded = factoriesToBeAdded;

        List<TypeAdapterFactory> factories = new ArrayList<>();
        factories.add(TypeAdapters.JSON_ELEMENT_FACTORY);
        factories.add(ObjectTypeAdapter.FACTORY);
        factories.add(excluder);
        factories.addAll(factoriesToBeAdded);
        factories.add(TypeAdapters.STRING_FACTORY);
        factories.add(TypeAdapters.INTEGER_FACTORY);
        TypeAdapter<Number> longAdapter = longAdapter(longSerializationPolicy);
        factories.add(TypeAdapters.newFactory(long.class, Long.class, longAdapter));
        factories.add(new ReflectiveTypeAdapterFactory(constructorConstructor, fieldNamingStrategy, excluder, jsonAdapterFactory));
        this.factories = Collections.unmodifiableList(factories);
    }

    private static TypeAdapter<Number> longAdapter(LongSerializationPolicy longSerializationPolicy) {
        if (longSerializationPolicy == LongSerializationPolicy.DEFAULT) {
            return TypeAdapters.LONG;
        }
        return new TypeAdapter<Number>() {
            @Override
            public Number read(JsonReader in) throws IOException {
                JsonToken peek = in.peek();
                if (peek == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                return in.nextLong();
            }

            @Override
            public void write(JsonWriter out, Number value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                out.value(value);
            }
        };
    };


    public <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type) {
        if (!factories.contains(skipPast)) {
            skipPast = jsonAdapterFactory;
        }

        boolean skipPastFount = false;
        for (TypeAdapterFactory factory : factories) {
            if (!skipPastFount) {
                if (factory == skipPast) {
                    skipPastFount = true;
                }
                continue; // 跳过查询到的factory 使用下一个来创建
            }

            TypeAdapter<T> candidate = factory.create(this, type);
            if (candidate != null) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("gson cannot serialize " + type);
    }

    public <T> TypeAdapter<T> getAdapter(Class<T> type) {
        return getAdapter(TypeToken.get(type));
    }

    public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
        TypeAdapter<?> cached = typeTokenCache.get(type == null ? NULL_KEY_SURROGATE : type);
        if (cached != null) {
            return (TypeAdapter<T>) cached;
        }

        Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = calls.get();
        boolean requiresThreadLocalCleanup = false;
        if (threadCalls == null) {
            threadCalls = new HashMap<TypeToken<?>, FutureTypeAdapter<?>>();
            calls.set(threadCalls);
            requiresThreadLocalCleanup = true;
        }

        FutureTypeAdapter<T> ongoingCall = (FutureTypeAdapter<T>) threadCalls.get(type);
        if (ongoingCall != null) {
            return ongoingCall;
        }

        try {
            FutureTypeAdapter<T> call = new FutureTypeAdapter<T>();
            threadCalls.put(type, call);

            for (TypeAdapterFactory factory : factories) {
                TypeAdapter<T> candidate = factory.create(this, type);
                if (candidate != null) {
                    call.setDelegate(candidate);
                    typeTokenCache.put(type, candidate);
                    return candidate;
                }
            }
            throw new IllegalArgumentException("gson can not handle" + type);
        } finally {
            threadCalls.remove(type);
            if (requiresThreadLocalCleanup) {
                calls.remove();
            }
        }
    }

    private static class FutureTypeAdapter<T> extends TypeAdapter<T> {
        private TypeAdapter<T> delegate;

        public void setDelegate(TypeAdapter<T> delegate) {
            if (delegate == null) {
                throw new AssertionError();
            }
            this.delegate = delegate;
        }

        @Override
        public T read(JsonReader in) throws IOException {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            return delegate.read(in);
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            delegate.write(out, value);
        }
    }
}
