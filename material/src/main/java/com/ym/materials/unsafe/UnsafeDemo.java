package com.ym.materials.unsafe;

import com.google.common.base.Stopwatch;
import com.ym.materials.json.JSON;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UnsafeDemo {

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
            elementDataOffset = UNSAFE.objectFieldOffset(UnsafeDemo.class.getDeclaredField("elementData"));
            ARRAY_OFFSET = UNSAFE.arrayBaseOffset(Object[].class);
            INDEX_SCALE = UNSAFE.arrayIndexScale(Object[].class);
            ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(INDEX_SCALE);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private int outter = 100000;
    private int inner = 100000;
    private int capacity = 1024;
    private int capacity_mast = 1023;

    @Test
    public void test1() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        elementData = new Long[capacity];
        for (int i = 0; i < capacity; i++) {
            elementData[i] = Long.valueOf(i);
        }

        long cnt = 0;
        for (int i = 0; i < outter; i++) {
            for (int j = 0; j < inner; j++) {
                Object elementDatum = elementData[j & capacity_mast];
            }
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }

    @Test
    public void test() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        elementData = new Long[capacity];
        for (int i = 0; i < capacity; i++) {
            elementData[i] = Long.valueOf(i);
        }

        Object[] copy = (Object[]) UNSAFE.getObject(this, elementDataOffset);
        for (int i = 0; i < outter; i++) {
            for (int j = 0; j < inner; j++) {
                Object elementDatum = copy[j & capacity_mast];
            }
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));

    }

    @Test
    public void test3() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        elementData = new Long[capacity];
        for (int i = 0; i < capacity; i++) {
            elementData[i] = Long.valueOf(i);
        }

        Object[] copy = (Object[]) UNSAFE.getObject(this, elementDataOffset);
        for (int i = 0; i < outter; i++) {
            for (int j = 0; j < inner; j++) {
                Object object = UNSAFE.getObject(copy, ARRAY_OFFSET + ((j & capacity_mast) << ARRAY_SHIFT));
            }
        }
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));

    }
}
