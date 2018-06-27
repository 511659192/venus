package com.ym.materials.jdk.hsdis;

import com.ym.materials.unsafe.UnsafeUtils;
import sun.misc.Unsafe;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ym on 2018/6/24.
 */
public class PutOrderedDemo {

    volatile int a;
    int b;
    final static Unsafe unsafe = UnsafeUtils.getUnsafe();
    final static long offset;
    final static long offset2;

    static {
        try {
            offset = unsafe.objectFieldOffset(PutOrderedDemo.class.getDeclaredField("a"));
            offset2 = unsafe.objectFieldOffset(PutOrderedDemo.class.getDeclaredField("b"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) {
//        PutOrderedDemo demo = new PutOrderedDemo();
//        for (int i = 0; i < 1; i++) {
//            demo.exec();
//        }

        System.out.println(2 ^ 2);
    }

    private void exec() {
        a = 2;
        int c = a;
        unsafe.putOrderedInt(this, offset, 5);
        int d = a;
        unsafe.putIntVolatile(this, offset2, 5);
        int e = a;
        Object object = new Object();
        synchronized (object) {

        }
    }

}
