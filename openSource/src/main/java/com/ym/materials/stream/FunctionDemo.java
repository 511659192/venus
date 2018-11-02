package com.ym.materials.stream;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FunctionDemo {
    public static void main(String[] args) {
        BiConsumer<Integer, Integer> biConsumer = (i, j) -> {
            System.out.println(i + j);
        };
        biConsumer.accept(1, 2);
    }
}
