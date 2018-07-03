package com.ym.materials.gson;

/**
 * Created by ym on 2018/7/2.
 */
public interface ExclusionStrategy {

    boolean shouldSkipClass(Class<?> clazz);

    boolean shouldSkipField(FieldAttributes fieldAttributes);
}
