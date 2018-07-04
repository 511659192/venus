package com.ym.materials.gson.internal;

import com.ym.materials.gson.*;
import com.ym.materials.gson.reflect.TypeToken;
import com.ym.materials.gson.stream.JsonReader;
import com.ym.materials.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.List;

/**
 * Created by ym on 2018/7/2.
 */
public final class Excluder implements TypeAdapterFactory, Cloneable {

    public static final Excluder DEFAULT = new Excluder();
    private boolean serializeInnerClasses = true;
    private List<ExclusionStrategy> serializationStrategies = Collections.emptyList();
    private List<ExclusionStrategy> deserializationStrategies = Collections.emptyList();
    private int transientOrStatic = Modifier.TRANSIENT | Modifier.STATIC;
    private boolean requireExpose;

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        boolean excludeClass = excludeClassChecks(rawType);
        boolean skipSerialize = excludeClass || excludeClassInStrategy(rawType, true);
        boolean skipDeserialize = excludeClass || excludeClassInStrategy(rawType, false);
        if (!skipSerialize && !skipDeserialize) {
            return null;
        }

        return new TypeAdapter<T>() {
            private TypeAdapter<T> delegate;

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (skipSerialize) {
                    out.nullValue();
                    return;
                }
                delegate().write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                if (skipDeserialize) {
                    in.skipValue();
                    return null;
                }
                return delegate().read(in);
            }

            private TypeAdapter<T> delegate() {
                TypeAdapter<T> delegate = this.delegate;
                return delegate != null ? delegate : gson.getDelegateAdapter(Excluder.this, type);
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }
        };
    }

    private boolean excludeClassInStrategy(Class<?> rawType, boolean serialize) {
        List<ExclusionStrategy> list = serialize ? serializationStrategies : deserializationStrategies;
        for (ExclusionStrategy exclusionStrategy : list) {
            if (exclusionStrategy.shouldSkipClass(rawType)) {
                return true;
            }
        }
        return false;
    }

    private boolean excludeClassChecks(Class<?> clazz) {
        if (!serializeInnerClasses && isInnerClass(clazz)) {
            return true;
        }

        if (isAnonymousOrLocal(clazz)) {
            return true;
        }

        return false;
    }

    private boolean isAnonymousOrLocal(Class<?> clazz) {
        return !Enum.class.isAssignableFrom(clazz) && (clazz.isAnonymousClass() || clazz.isLocalClass());
    }

    private boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !isStatic(clazz);
    }

    private boolean isStatic(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers());
    }

    @Override
    protected Excluder clone() throws CloneNotSupportedException {
        try {
            return (Excluder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public boolean exculdeClass(Class<?> clazz, boolean serialize) {
        return excludeClassChecks(clazz) || excludeClassInStrategy(clazz, serialize);
    }

    public boolean exculdeField(Field field, boolean serialize) {
        Class<?> declaredClass = field.getType();
        if ((transientOrStatic & field.getModifiers()) != 0 || field.isSynthetic() ||
                (serializeInnerClasses && isInnerClass(declaredClass)) || isAnonymousOrLocal(declaredClass)) {
            return true;
        }

        List<ExclusionStrategy> list = serialize ? serializationStrategies : deserializationStrategies;
        if (!list.isEmpty()) {
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            for (ExclusionStrategy exclusionStrategy : list) {
                if (exclusionStrategy.shouldSkipField(fieldAttributes)) {
                    return true;
                }
            }
        }
        return false;
    }
}
