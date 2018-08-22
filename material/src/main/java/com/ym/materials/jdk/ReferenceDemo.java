package com.ym.materials.jdk;

import org.junit.Test;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Created by ym on 2018/8/21.
 */
public class ReferenceDemo {

    @Test
    public void testStrong() throws Exception {
        LinkedList<byte[]> list = new LinkedList<>();
        for(int i=0; i<1024; i++){
            list.add(new byte[1024*1024]);
        }
    }

    @Test
    public void testSoft() throws Exception {
        TimeUnit.SECONDS.sleep(10);
        long beginTime = System.nanoTime();
        LinkedList<SoftReference<byte[]>> list = new LinkedList<>();
        for (int i = 0; i < 1024; i++) {
            list.add(new SoftReference<>(new byte[1024 * 1024]));
            TimeUnit.MICROSECONDS.sleep(1);
        }
        long endTime = System.nanoTime();
        System.out.println(endTime - beginTime);
        TimeUnit.SECONDS.sleep(1000);
    }

    @Test
    public void testWeak() throws Exception {
        TimeUnit.SECONDS.sleep(10);
        long beginTime = System.nanoTime();
        LinkedList<WeakReference<byte[]>> list = new LinkedList<>();
        for (int i = 0; i < 1024; i++) {
            list.add(new WeakReference<>(new byte[1024 * 1024]));
            TimeUnit.MICROSECONDS.sleep(1);
        }
        long endTime = System.nanoTime();
        System.out.println(endTime - beginTime);
        TimeUnit.SECONDS.sleep(1000);
    }
}
