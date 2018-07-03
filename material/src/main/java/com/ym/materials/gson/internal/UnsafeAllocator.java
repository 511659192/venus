package com.ym.materials.gson.internal;

import sun.misc.Unsafe;

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by ym on 2018/7/3.
 */
public abstract class UnsafeAllocator {

    public abstract <T> T newInstance(Class<T> clazz) throws Exception;

    public static UnsafeAllocator create() {
        try {
            Class<Unsafe> unsafeClass = Unsafe.class;
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);
            Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
            return new UnsafeAllocator() {
                @Override
                public <T> T newInstance(Class<T> clazz) throws Exception {
                    assertInstantiable(clazz);
                    return (T) allocateInstance.invoke(unsafe, clazz);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Method getConstructorId = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
            getConstructorId.setAccessible(true);
            int constructorId = (Integer) getConstructorId.invoke(null, Object.class);
            Method newInstance = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, int.class);
            newInstance.setAccessible(true);
            return new UnsafeAllocator() {
                @Override
                public <T> T newInstance(Class<T> clazz) throws Exception {
                    assertInstantiable(clazz);
                    return (T) newInstance.invoke(null, clazz, constructorId);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Method newInstance = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
            newInstance.setAccessible(true);
            return new UnsafeAllocator() {
                @Override
                public <T> T newInstance(Class<T> clazz) throws Exception {
                    assertInstantiable(clazz);
                    return (T) newInstance.invoke(null, clazz, Object.class);
                }
            };
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        return new UnsafeAllocator() {
            @Override
            public <T> T newInstance(Class<T> clazz) throws Exception {
                throw new RuntimeException("cannot allocate " + clazz);
            }
        };
    }

    static void assertInstantiable(Class<?> clazz) {
        int modifiers = clazz.getModifiers();
        if (Modifier.isInterface(modifiers)) {
            throw new RuntimeException("interface can not be instantiated name is " + clazz);
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new RuntimeException("abstract can not be instantiated name is " + clazz);
        }

    }
}
