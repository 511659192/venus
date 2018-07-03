package com.ym.materials.gson.internal.reflect;

import java.lang.reflect.AccessibleObject;

/**
 * Created by ym on 2018/7/3.
 */
public abstract class ReflectionAccessor {

    private static final ReflectionAccessor insatence = new PreJava9ReflectionAccessor();

    public abstract void makeAccessible(AccessibleObject ao);

    public static ReflectionAccessor getInsatence() {
        return insatence;
    }
}
