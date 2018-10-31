package com.ym.materials;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiFunction;

/**
 * Created by ym on 2018/10/19.
 */
public class result2 {


    static Integer aa(BiFunction<Integer, Double, Integer> bb) {
        return bb.apply(3, 10.2);
    }

    public static void main(String[] args) throws Exception {
        String[] a = new String[10];
        System.out.println(a.length);
        System.out.println(a[8]);
    }

    public int reverseInteger(int x) {
        int res = 0;
        while (x != 0) {
            int temp = res * 10 + x % 10;
            x = x / 10; //不断取前几位
            if (temp / 10 != res) {
                res = 0;
                break;
            }
            res = temp;
        }
        return res;
    }
}
