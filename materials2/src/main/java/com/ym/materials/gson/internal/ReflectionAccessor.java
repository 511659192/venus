package com.ym.materials.gson.internal;

import java.lang.reflect.AccessibleObject;

/**
 * Created by ym on 2018/7/8.
 */
public abstract class ReflectionAccessor {

    private ReflectionAccessor() {
    }

    public static final ReflectionAccessor instance = new DirectReflectionAccessor();

    public abstract void makeAccessible(AccessibleObject ao);

    private static class DirectReflectionAccessor extends ReflectionAccessor {
        @Override
        public void makeAccessible(AccessibleObject ao) {
            ao.setAccessible(true);
        }
    }

    public static ReflectionAccessor getInstance() {
        return instance;
    }
}
