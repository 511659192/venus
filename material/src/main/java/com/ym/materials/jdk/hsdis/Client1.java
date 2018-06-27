package com.ym.materials.jdk.hsdis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ym on 2018/6/24.
 */
public class Client1 {
    public static void main(String[] args) {
        for (int i = 0; i < 5000; i++) {
            main2(args);

        }
    }

    public static void main2(String[] args) {
        List<Object> list = new ArrayList<Object>();
        Object obj = new Object();
        // 填充数据
        for (int i = 0; i < 1; i++) {
            list.add(obj);
        }
        long start;

        start = System.nanoTime();
        // 初始化时已经计算好条件
        for (int i = 0, n = list.size(); i < n; i++) {
        }
        System.out.println("判断条件中计算：" + (System.nanoTime() - start) + " ns");

        start = System.nanoTime();
        // 在判断条件中计算
        for (int i = 0; i < list.size(); i++) {
        }
        System.out.println("判断条件中计算：" + (System.nanoTime() - start) + " ns");
    }
}
