package com.ym.materials.optimize.fastRemoveList;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Long.SIZE;

/**
 * Created by ym on 2018/6/3.
 */
public class FastRemoveList<T> {

    private final static int PER_WORD_OFFSET = 6; // long型位移量
    private final static int BIT_INDEX_MASK = 63;
    private static final long WORD_MASK = -1L; // 补码
    private long[] words;
    private int wordSize;

    private List<T> targetList;
    private int originalSize;

    public static <T> FastRemoveList<T> of(List<T> targetList) {
        FastRemoveList<T> fastRemoveList = new FastRemoveList<>();
        fastRemoveList.targetList = targetList;
        fastRemoveList.originalSize = targetList.size();
        fastRemoveList.initWords(); // 计算words长度
        return fastRemoveList;
    }

    public FastRemoveList() {
    }

    private void initWords() {
        int lastItemIndex = originalSize - 1;
        int wordIndex = wordIndex(lastItemIndex);
        int wordSize = wordIndex  + 1;
        this.wordSize = wordSize;
        this.words = new long[wordSize];
    }

    public boolean remove(T t) {
        for (int i = 0; i < originalSize; i++) {
            T item = targetList.get(i);
            if (Objects.equals(t, item)) {
                set(i);
                return true;
            }
        }
        return false;
    }

    private void set(int index) {
        int wordIndex = wordIndex(index);
        long word = words[wordIndex]; // 现有值
        word |= 1L << index; // 计算新值
        words[wordIndex] = word; // 替换现有值
    }

    private int wordIndex(int index) {
        return index >> PER_WORD_OFFSET;
    }

    public boolean isEmpty() {
        // 最后一组是否为空
        long last = words[wordSize - 1];
        long lastWordMask = WORD_MASK >>> -originalSize; // 最后一组补码
        if ((last ^ lastWordMask) != 0) {
            return false;
        }

        if (wordSize == 1) {
            return true;
        }

        for (int i = 0; i < wordSize - 1; i++) {
            long word = words[i];
            if ((word ^ WORD_MASK) != 0) {
                return false;
            }
        }
        return true;
    }

    public List<T> getRemainList() {
        List<T> remaidList = Lists.newArrayList();
        if (isEmpty()) {
            return remaidList;
        }
        long word = 0;
        long offset;

        int i = 0;
        Iterator<T> iterator = targetList.iterator();
        while (iterator.hasNext()) {
            if ((i & BIT_INDEX_MASK) == 0) { // 取余
                int wordIndex = wordIndex(i); // 取模
                word = words[wordIndex];
            }
            T next = iterator.next();
            offset = 1L << i;
            if ((word & offset) != offset) {
                remaidList.add(next);
            }
            i++;
        }
        return remaidList;
    }

    public List<T> getRemainList2() {
        List<T> remaidList = Lists.newArrayList();
        if (isEmpty()) {
            return remaidList;
        }

        int loop = 0;
        for (int i = 0; i < wordSize - 1; i++) {
            long word = words[i];
            long offset;
            for (int index = 0; index < SIZE; index++) {
                offset = 1L << index;
                if ((word & offset) != offset) {
                    remaidList.add(targetList.get(loop * SIZE + index));
                }
            }
            loop++;
        }

        long word = words[wordSize - 1];
        long offset;
        long limit = originalSize & (SIZE - 1);
        for (int index = 0; index < limit; index++) {
            offset = 1L << index;
            if ((word & offset) != offset) {
                remaidList.add(targetList.get(loop * SIZE + index));
            }
        }

        return remaidList;
    }

    @Test
    public void test1() {
        int cnt = 100;
        List<Vo> list = new ArrayList<>();
        Set<Vo> set = Sets.newHashSet();
        for (int i = 0; i < cnt; i++) {
            list.add(new Vo(String.valueOf(i)));
            set.add(new Vo(String.valueOf(i)));
        }

        FastRemoveList<Vo> fastRemoveList = FastRemoveList.of(list);

        Stopwatch stopwatch = Stopwatch.createStarted();
        Iterator<Vo> iterator = set.iterator();
        while (iterator.hasNext()) {
            Vo next = iterator.next();
            fastRemoveList.remove(next);
        }

//        System.out.println(fastRemoveList.getRemainList());
//        stopwatch.stop();
//        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
//        stopwatch = Stopwatch.createStarted();
        System.out.println(fastRemoveList.getRemainList2());
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.MICROSECONDS));
    }


    @Test
    public void test2() {
        long max = 0;
        long min = 0;
        long total = 0;
        int loop = 10000;

        for (int i = 0; i < loop; i++) {
            long time = test();
            max = Math.max(time, max);
            min = min == 0 ? time : Math.min(min, time);
            total += time;
        }

        System.out.println("max:" + max);
        System.out.println("min:" + min);
        System.out.println("total:" + total);
        System.out.println("avg:" + max / loop);
    }

    public long test() {
        int cnt = 40;
        List<Vo> list = new ArrayList<>();
        Set<Vo> set = Sets.newHashSet();
        for (int i = 0; i < cnt; i++) {
            list.add(new Vo(String.valueOf(i)));
            set.add(new Vo(String.valueOf(i)));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        Iterator<Vo> iterator = set.iterator();
        while (iterator.hasNext()) {
            Vo next = iterator.next();
            list.remove(next);
        }
        stopwatch.stop();
        long elapsed = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        return elapsed;
    }
}
