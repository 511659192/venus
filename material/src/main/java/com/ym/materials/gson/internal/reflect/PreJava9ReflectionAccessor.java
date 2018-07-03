package com.ym.materials.gson.internal.reflect;

import java.lang.reflect.AccessibleObject;

/**
 * Created by ym on 2018/7/3.
 */
public class PreJava9ReflectionAccessor extends ReflectionAccessor {
    @Override
    public void makeAccessible(AccessibleObject ao) {
        ao.setAccessible(true);
    }
}
