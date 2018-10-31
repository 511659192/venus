package com.ym.materials;

import com.alibaba.fastjson.JSON;
import com.ym.materials.jdk.reOrder.Main;
import com.ym.materials.unsafe.Array;
import jdk.internal.util.xml.impl.Input;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.ToIntFunction;

/**
 * Created by ym on 2018/10/19.
 */
public class result {

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
//        String input = "1 2 3 # # 6 7 # # # # # # # 8 9 10";
        String input = in.nextLine();
        String[] split = input.split(" ");
        // 遍历高度
        List<Integer> result = new ArrayList<>();
        // i 当前下标值 h 遍历层高
        outer:
        for (int i = 0, h = 0, len = split.length; i < len; h++) {
            // 当前层元素个数
            int num = 1 << h;
            // 遍历当前层
            for (int j = 0; j < num; j++) {
                // i = j 元素实际下标值note
                if (!"#".equals(split[i + j])){
                    result.add(Integer.valueOf(split[i + j]));
                    // 略过当前层元素
                    i += num;
                    continue outer;
                }
            }
            // 全层为空 下标值也要增加
            i += num;
        }
    }
}
