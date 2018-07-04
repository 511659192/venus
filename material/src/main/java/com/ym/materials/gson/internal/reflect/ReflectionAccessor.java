package com.ym.materials.gson.internal.reflect;

import java.lang.reflect.AccessibleObject;

/**
 * Created by ym on 2018/7/3.
 */
public abstract class ReflectionAccessor {

    private static final ReflectionAccessor instance = new PreJava9ReflectionAccessor();

    public static ReflectionAccessor getInstance() {
        return instance;
    }

    public abstract void makeAccessible(AccessibleObject ao);
}
