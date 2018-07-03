package com.ym.materials.gson;

import java.lang.reflect.Type;

/**
 * Created by ym on 2018/7/2.
 */
public interface InstanceCreator<T> {

    T createInstance(Type type);
}
