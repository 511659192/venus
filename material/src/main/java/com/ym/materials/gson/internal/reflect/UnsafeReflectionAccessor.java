package com.ym.materials.gson.internal.reflect;

import com.ym.materials.gson.JsonIOException;
import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by ym on 2018/7/3.
 */
public class UnsafeReflectionAccessor extends ReflectionAccessor {

    private static Class unsafeClass;
    private final Object theUnsafe = getUnsafeInstance();
    private final Field overrideField = getOverrideField();

    @Override
    public void makeAccessible(AccessibleObject ao) {
        boolean success = makeAccessibleWithUnsafe(ao);
        if (!success) {
            try {
                ao.setAccessible(true);
            } catch (SecurityException e) {
                throw new JsonIOException("Gson couldn't modify fields for " + ao
                        + "\nand sun.misc.Unsafe not found.\nEither write a custom type adapter,"
                        + " or make fields accessible, or include sun.misc.Unsafe.", e);
            }
        }
    }

    private boolean makeAccessibleWithUnsafe(AccessibleObject ao) {
        if (theUnsafe != null && overrideField != null) {
            try {
                Method method = unsafeClass.getMethod("objectFieldOffset", Field.class);
                long overrideOffset = (long) method.invoke(theUnsafe, overrideField);
                Method putBooleanMethod = unsafeClass.getMethod("putBoolean", Object.class, long.class, boolean.class);
                putBooleanMethod.invoke(theUnsafe, ao, overrideOffset, true);
                return true;
            } catch (Exception ignore) {
            }
        }
        return false;
    }

    public static Object getUnsafeInstance() {
        try {
            unsafeClass = Unsafe.class;
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }

    public Field getOverrideField() {
        try {
            return AccessibleObject.class.getDeclaredField("override");
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
