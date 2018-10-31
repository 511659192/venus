package com.ym.materials;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by ym on 2018/10/19.
 */
public class result21 {

    public static void main(String[] args) throws Exception {
//        Scanner in = new Scanner(System.in);
//        String input = in.nextLine();
        String input = "4 2 7 # 3 6 9";
        String[] split = input.split(" ");
        List<String> list = new ArrayList<>();
        list.stream().collect(new Collector<String, Object, Object>() {
            @Override
            public Supplier<Object> supplier() {
                return null;
            }

            @Override
            public BiConsumer<Object, String> accumulator() {
                return null;
            }

            @Override
            public BinaryOperator<Object> combiner() {
                return null;
            }

            @Override
            public Function<Object, Object> finisher() {
                return null;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return null;
            }
        });
    }
}
