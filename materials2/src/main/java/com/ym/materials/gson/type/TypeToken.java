package com.ym.materials.gson.type;

import com.ym.materials.gson.internal.Preconditions;
import org.junit.Assert;

import java.lang.reflect.Type;

/**
 * Created by ym on 2018/7/8.
 */
public class TypeToken<T> {

    private Type type;
    private Class<? super T> rawType;
    private int hashCode;

    private TypeToken(Type type) {
        Preconditions.checkNotNull(type);
        this.type = Types.canonicalize(type);
        this.rawType = (Class<? super T>) Types.getRawType(this.type);
        this.hashCode = type.hashCode();
    }

    public static TypeToken<?> get(Type type) {
        return new TypeToken<Object>(type);
    }

    public Type getType() {
        return type;
    }

    public Class<? super T> getRawType() {
        return rawType;
    }

    public int getHashCode() {
        return hashCode;
    }
}
