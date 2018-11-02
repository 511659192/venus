package com.ym.materials.stream;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupDemo {

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        Object obj = list.stream().map(x -> {
            System.out.println(x);
            return x;
        }).collect(Collectors.toList());
        System.out.println(JSON.toJSONString(obj));


    }
}
