package com.google.common.base;

import java.util.Iterator;
import java.util.List;

/**
 * Created by ym on 2018/7/15.
 */
public class SplitterTest {

    public static void main(String[] args) {
        Iterable<String> list = Splitter.on(",").omitEmptyStrings().trimResults().limit(2).split(" abc ,, b1, b333 ");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            System.out.println(next);
        }
    }
}
