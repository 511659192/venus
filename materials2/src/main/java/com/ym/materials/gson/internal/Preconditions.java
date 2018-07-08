package com.ym.materials.gson.internal;

/**
 * Created by ym on 2018/7/8.
 */
public class Preconditions {

    private Preconditions() {

    }

    public static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    public static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }



}
