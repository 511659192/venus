package com.ym.materials.gson.internal;

import com.ym.materials.gson.InstanceCreator;
import com.ym.materials.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by ym on 2018/7/2.
 */
public class ConstructorConstructor {

    private Map<Type, InstanceCreator<?>> instanceCreators;

    public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators) {
        this.instanceCreators = instanceCreators;
    }

    public <T> ObjectConstructor<T> get(TypeToken<T> typeToken) {
        return null;
    }
}
