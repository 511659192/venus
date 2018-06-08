package com.ym.materials.optimize.jdk;

public class BitMoveDemo {

    public static void main(String[] args) {
        Long l1 = -1L;
        System.out.println(Long.toBinaryString(l1));
        System.out.println(Long.toBinaryString(l1 >>> 3));
        System.out.println(Long.toBinaryString(l1 >>> -3));
        System.out.println(Long.toBinaryString(l1 << 3));
        System.out.println(Long.toBinaryString(l1 << -3));
    }
}
