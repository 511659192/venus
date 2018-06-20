package com.ym.materials.jdk;

import org.junit.Test;

public class BitMoveDemo {

    public static void main(String[] args) {
        Long l1 = -1L;
        System.out.println(Long.toBinaryString(l1));
        System.out.println(Long.toBinaryString(l1 >>> 3));
        System.out.println(Long.toBinaryString(l1 >>> -3));
        System.out.println(Long.toBinaryString(l1 << 3));
        System.out.println(Long.toBinaryString(l1 << -3));
    }

    @Test
    public void test() {
        System.out.println(Integer.toBinaryString(-1));
        System.out.println(Integer.toBinaryString(Integer.MAX_VALUE));
    }


}
