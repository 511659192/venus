package com.ym.materials.jdk;

import java.util.HashMap;

/**
 * Created by ym on 2018/6/30.
 */
public class MapTableSizeFor {

static final int MAXIMUM_CAPACITY = 1 << 30;

static final int tableSizeFor(int cap) {
    System.out.println(Integer.toBinaryString(cap));
    int n = cap - 1;
    System.out.println(Integer.toBinaryString(n));
    n |= n >> 1;
    System.out.println(Integer.toBinaryString(n));
    n |= n >> 2;
    System.out.println(Integer.toBinaryString(n));
    n |= n >> 4;
    System.out.println(Integer.toBinaryString(n));
    n |= n >> 8;
    System.out.println(Integer.toBinaryString(n));
    n |= n >> 16;
    System.out.println(Integer.toBinaryString(n));
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}

public static void main(String[] args) {
    tableSizeFor(MAXIMUM_CAPACITY + 1);
    new HashMap<>(13);
}
}
