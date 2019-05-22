// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.jvm;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-03-09 21:36
 **/
public class HProfTest {
    public void slowMethod(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void slowerMethod(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void fastMethod(){
        try {
            Thread.yield();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        while (true) {
            HProfTest hProfTest = new HProfTest();
            hProfTest.fastMethod();
            hProfTest.slowMethod();
            hProfTest.slowerMethod();
        }
    }
}