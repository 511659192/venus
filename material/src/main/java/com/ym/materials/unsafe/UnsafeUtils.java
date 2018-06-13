package com.ym.materials.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by ym on 2018/6/13.
 */
public class UnsafeUtils {

    public static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
