// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.data;

import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-07-10 14:44
 **/
public class DoubleTest {

    public static void main(String[] args) {

        double d = 3.01d;
        System.out.println(d * d);
        System.out.println(format(d * d));
        d = 3d;
        System.out.println(d * d);
        System.out.println(format(d * d));


    }

    public static double format(Number number) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.UP);
        String format = nf.format(number);
        return Double.valueOf(format);
    }
}