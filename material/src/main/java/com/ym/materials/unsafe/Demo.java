package com.ym.materials.unsafe;

import sun.misc.Unsafe;

public class Demo {

    private static final Unsafe UNSAFE = UnsafeUtils.getUnsafe();
    private final static long longArrOffset;
    private final static int longScale;
    private static final int longShift;

    static {
        longArrOffset = UNSAFE.arrayBaseOffset(long[].class);
        longScale = UNSAFE.arrayIndexScale(long[].class);
        longShift = 31 - Integer.numberOfLeadingZeros(longScale);
        System.out.println("longArrOffset " + longArrOffset);
        System.out.println("longScale " + longScale);
        System.out.println("longShift " + longShift);
    }

    public static void main(String[] args) {
        long[] arr = new long[20];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }

        long aLong = UNSAFE.getLong(arr, longArrOffset + 1L * 8 *4);
        System.out.println(aLong);
    }

}
