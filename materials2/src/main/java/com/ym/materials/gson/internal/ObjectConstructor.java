package com.ym.materials.gson.internal;

import com.ym.materials.gson.type.TypeToken;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ym on 2018/7/8.
 */
public abstract class ObjectConstructor {

    public final static ObjectConstructor instance = new ObjectConstructor() {
        @Override
        public <T> T construct() {
            return null;
        }
    };

    private ReflectionAccessor accessor = ReflectionAccessor.getInstance();

    public abstract <T> T construct();

    public <T> ObjectConstructor get(TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        ObjectConstructor constructor = newDefaultObjectConstructor(rawType);
        if (constructor != null) {
            return constructor;
        }

        constructor = newDefaultImplementationConstructor(typeToken, rawType);
        if (constructor != null) {
            return constructor;
        }

        throw new RuntimeException("constructor can not create");
    }

    private <T> ObjectConstructor newDefaultImplementationConstructor(TypeToken<T> typeToken, Class<? super T> rawType) {
        if (Collection.class.isAssignableFrom(rawType)) {
            if (List.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor() {
                    @Override
                    public <T> T construct() {
                        return (T) new ArrayList<Object>();
                    }
                };
            }

            throw new RuntimeException("not accept now");
        }

        if (Map.class.isAssignableFrom(rawType)) {

        }

        return null;
    }

    private <T> ObjectConstructor newDefaultObjectConstructor(Class<? super T> rawType) {
        try {
            Constructor<? super T> declaredConstructor = rawType.getDeclaredConstructor();
            if (!declaredConstructor.isAccessible()) {
                accessor.makeAccessible(declaredConstructor);
            }

            return new ObjectConstructor() {
                @Override
                public <T> T construct() {
                    try {
                        Object[] args = null;
                        return (T) declaredConstructor.newInstance(args);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
