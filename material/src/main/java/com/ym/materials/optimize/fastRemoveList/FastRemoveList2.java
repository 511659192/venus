package com.ym.materials.optimize.fastRemoveList;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by ym on 2018/6/3.
 */
public class FastRemoveList2<T> {

    private List<T> list;
    private BitSet bitSet;

    public FastRemoveList2(List<T> list) {
        this.list = list;
        this.bitSet = new BitSet(list.size());
    }

    public boolean remove(T o) {
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            if (t.equals(o)) {
                bitSet.set(i);
                return true;
            }
        }
        return false;
    }

    public List<T> getRemainList2() {
        List<T> removedList = new ArrayList<T>(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (!bitSet.get(i)) {
                removedList.add(list.get(i));
            }
        }
        return removedList;
    }

    public static void main(String[] args) throws InterruptedException {
        int cnt = 10;
        int cnt2 = cnt;
        test1(cnt, cnt2);
        test2(cnt, cnt2);
//        main2(args);
        test4(cnt);
        test3(cnt);
    }

    public static void test2(int cnt, int cnt2) {
        List<Vo> list = new ArrayList<>();
        Set<Vo> set = Sets.newHashSet();
        for (int i = 0; i < cnt; i++) {
            Vo vo = new Vo(String.valueOf(i));
            list.add(vo);
            set.add(vo);
        }

        FastRemoveList2<Vo> fastRemoveList = new FastRemoveList2<>(list);

        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Vo vo : set) {
            fastRemoveList.remove(vo);
        }
        System.out.println(fastRemoveList.getRemainList2());
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }


    public static void test1(int cnt, int cnt2) {
        List<Vo> list = new ArrayList<>();
        Set<Vo> set = Sets.newHashSet();
        for (int i = 0; i < cnt; i++) {
            Vo vo = new Vo(String.valueOf(i));
            list.add(vo);
            set.add(vo);
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Vo vo : set) {
            list.remove(vo);
        }
        System.out.println(list);
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }

    public static void test3(int len) {
        List<Vo> sourceList = new ArrayList<>();
        Set<Vo> set = new HashSet<>();
        for (int i = 0; i < len; i++) {
            Vo vo = new Vo(String.valueOf(i));
            sourceList.add(vo);
            set.add(vo);
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Vo vo : set) {
            sourceList.remove(vo);
        }

        System.out.println(JSON.toJSONString(sourceList));
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }

    public static void test4(int len) {
        List<Vo> sourceList = new ArrayList<>();
        Set<Vo> set = new HashSet<>();
        for (int i = 0; i < len; i++) {
            Vo vo = new Vo(String.valueOf(i));
            sourceList.add(vo);
            set.add(vo);
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        FastRemoveList2<Vo> fastRemoveList = new FastRemoveList2<>(sourceList);
        for (Vo vo : set) {
            fastRemoveList.remove(vo);
        }
        System.out.println(JSON.toJSONString(fastRemoveList.getRemainList2()));
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }
}
