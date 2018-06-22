package com.ym.materials.unsafe;

import com.google.common.base.Stopwatch;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class UnsafeDemo2 {

    private final static Unsafe UNSAFE;
    private final static long elementDataOffset;
    private Object[] elementData;
    private static final long ARRAY_OFFSET;
    private static final int ARRAY_SHIFT;
    private static final int INDEX_SCALE;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
            elementDataOffset = UNSAFE.objectFieldOffset(UnsafeDemo2.class.getDeclaredField("elementData"));
            ARRAY_OFFSET = UNSAFE.arrayBaseOffset(Object[].class);
            INDEX_SCALE = UNSAFE.arrayIndexScale(Object[].class);
            ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(INDEX_SCALE);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Test
    public void test1() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        elementData = new Long[16];
        for (int i = 0; i < 16; i++) {
            elementData[i] = Long.valueOf(i);
        }

        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < 10000; j++) {
                Object elementDatum = elementData[j & 15];
            }
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }

    @Test
    public void test() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        elementData = new Long[16];
        for (int i = 0; i < 16; i++) {
            elementData[i] = Long.valueOf(i);
        }

        Object[] copy = (Object[]) UNSAFE.getObject(this, elementDataOffset);
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < 10000; j++) {
                Object elementDatum = copy[j & 15];
            }
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));

    }

    @Test
    public void test3() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        elementData = new Long[16];
        for (int i = 0; i < 16; i++) {
            elementData[i] = Long.valueOf(i);
        }

        Object[] copy = (Object[]) UNSAFE.getObject(this, elementDataOffset);
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < 10000; j++) {
                Object object = UNSAFE.getObject(copy, ARRAY_OFFSET + ((j & 15) << ARRAY_SHIFT));
            }
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));

    }
}
