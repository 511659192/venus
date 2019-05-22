// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.jvm;

import java.math.BigDecimal;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-02-28 23:15
 **/
public class Jstack {

//    private final static Unsafe UNSAFE;
//    private final static long offset;
//
//    static {
//        try {
//            Field field = Unsafe.class.getDeclaredField("theUnsafe");
//            field.setAccessible(true);
//            UNSAFE = (Unsafe) field.get(null);
//            Field o1 = Suo.class.getField("o1");
//            o1.setAccessible(true);
//            offset = UNSAFE.staticFieldOffset(o1);
//        } catch (Exception e) {
//            throw new Error(e);
//        }
//
//
//    }

    public static void main(String[] args) {

        Thread t1 = new Thread(new DeadLockclass(true));//建立一个线程
        Thread t2 = new Thread(new DeadLockclass(false));//建立另一个线程
        t1.start();//启动一个线程
        t2.start();//启动另一个线程
    }
}

class DeadLockclass implements Runnable {
    public boolean falg;// 控制线程
    DeadLockclass(boolean falg) {
        this.falg = falg;
    }
    public void run() {
        /**
         * 如果falg的值为true则调用t1线程
         */
        if (falg) {
            while (true) {
                synchronized (Suo.o1) {
                    System.out.println("o1 " + Thread.currentThread().getName());
                    synchronized (Suo.o2) {
                        System.out.println("o2 " + Thread.currentThread().getName());
                    }
                }
            }
        }
        /**
         * 如果falg的值为false则调用t2线程
         */
        else {
            while (true) {
                synchronized (Suo.o2) {
                    System.out.println("o2 " + Thread.currentThread().getName());
                    synchronized (Suo.o1) {
                        System.out.println("o1 " + Thread.currentThread().getName());
                    }
                }
            }
        }
    }
}

class Suo {
    public static BigDecimal o1 = new BigDecimal(1);
    public static BigDecimal o2 = new BigDecimal(1);
}