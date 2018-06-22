package com.ym.materials.optimize.fastRemoveList;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by ym on 2018/6/3.
 */
public class FastRemoveList01<T> {

    private final static Unsafe UNSAFE;
    private final static long arrayListEleDataOffset;
    private final static long arrayListSizeOffset;


    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
            arrayListEleDataOffset = UNSAFE.objectFieldOffset(ArrayList.class.getDeclaredField("elementData"));
            arrayListSizeOffset = UNSAFE.objectFieldOffset(ArrayList.class.getDeclaredField("size"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private final static int PER_WORD_OFFSET = 6; // long型位移量
    private int wordSize;
    private long[] words;
    private boolean singleWord = true; // 是否只有一个word
    private long firstWord; // 多数场景下 只有一个word 缓存以提升性能

    private List<T> originalArrayList;
    private int originalSize; // 原始列表size
    private Object[] elementData; // 原始列表内部元素数组
    private int size;
    private SimpleMap<Object, Integer> index;

    private final static ArrayList EMPTY = new ArrayList();

    public static <T> FastRemoveList01<T> of(List<T> originalArrayList) {
        if (!(originalArrayList instanceof ArrayList)) {
            throw new RuntimeException("not support");
        }
        FastRemoveList01<T> fastRemoveList = new FastRemoveList01<>();
        fastRemoveList.originalArrayList = originalArrayList;
        int size = originalArrayList.size();
        fastRemoveList.originalSize = fastRemoveList.size = size;
        Object[] originObjectArr = (Object[]) UNSAFE.getObject(originalArrayList, arrayListEleDataOffset); // 获取原始数组
        fastRemoveList.elementData = originObjectArr;
        SimpleMap<Object, Integer> index = new SimpleMap<Object, Integer>(size);
        fastRemoveList.index = index;
        for (int i = 0; i < size; i++) {
            index.put(originObjectArr[i], i);
        }
        fastRemoveList.initWords(); // 计算words长度
        return fastRemoveList;
    }

    private final static class SimpleMap<K, V> {

        private final Entry<K, V>[] buckets;
        private final int indexMask;

        private SimpleMap() {
            throw new RuntimeException("not support");
        }

        public SimpleMap(int tableSize) {
            if ((tableSize & (tableSize - 1)) != 0) { // 2的次方
                int leftMostOnePos = Integer.SIZE - Integer.numberOfLeadingZeros(tableSize); // 最左侧1的位置
                tableSize = (1 << leftMostOnePos);
            }
            this.indexMask = tableSize - 1;
            this.buckets = new Entry[tableSize];
        }

        public final V get(K key) {
            int hash = key.hashCode();
            int bucket = hash & indexMask;
            for (Entry<K, V> entry = buckets[bucket]; entry != null; entry = entry.next) {
                if (key.equals(entry.key)) {
                    return entry.value;
                }
            }
            return null;
        }

        public V put(K key, V value) {
            int hash = key.hashCode();
            int bucket = hash & indexMask;
            for (Entry<K, V> entry = buckets[bucket]; entry != null; entry = entry.next) {
                if (key == entry.key) {
                    V oldValue = entry.value;
                    entry.value = value;
                    return oldValue;
                }
            }

            Entry<K, V> entry = new Entry<K, V>(key, value, hash, buckets[bucket]);
            buckets[bucket] = entry;
            return null;
        }

        final static class Entry<K, V> {
            public final int hashCode;
            public final K key;
            public V value;
            public final Entry<K, V> next;

            public Entry(K key, V value, int hashCode, Entry<K, V> next) {
                this.hashCode = hashCode;
                this.key = key;
                this.value = value;
                this.next = next;
            }
        }
    }

    public FastRemoveList01() {
    }

    private void initWords() {
        int lastEleIdx = originalSize - 1;
        int wordIndex = wordIndex(lastEleIdx);
        this.wordSize = wordIndex + 1;
        if (wordSize == 1) {
            firstWord = 0;
        } else {
            this.words = new long[wordSize];
            singleWord = false;
        }
    }

    public boolean remove(T t) {
        Integer index = this.index.get(t);
        if (index != null) {
            set(index);
            return true;
        }
        return false;
    }

    private void set(int index) {
        if (singleWord) {
            firstWord |= (1L << index);
        } else {
            int wordIndex = wordIndex(index);
            long word = words[wordIndex]; // 现有值
            word |= 1L << index; // 计算新值
            words[wordIndex] = word; // 替换现有值
        }
        size--;
    }

    private int wordIndex(int index) {
        return index >> PER_WORD_OFFSET;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public List<T> getRemainList() {
        if (isEmpty()) {
            return EMPTY;
        }

        Object[] remainEles = new Object[size];
        long offset;
        int next = 0;
        if (singleWord) {
            for (int i = 0; i < originalSize; i++) {
                offset = (1L << i);
                if ((firstWord & offset) != offset) { // 未被标记删除
                    remainEles[next++] = elementData[i];
                }
            }
        } else {
            for (int i = 0; i < originalSize; i++) {
                int wordIndex = wordIndex(i);
                long word = words[wordIndex];
                offset = (1L << i);
                if ((word & offset) != offset) { // 未被标记删除
                    remainEles[next++] = elementData[i];
                }
            }
        }

        ArrayList<T> remainList = new ArrayList<>(0);
        UNSAFE.putInt(remainList, arrayListSizeOffset, size);
        UNSAFE.putObject(remainList, arrayListEleDataOffset, remainEles);
        return remainList;
    }

    public long test() {
        int cnt = 10000;
        ArrayList<Vo> list = new ArrayList<>();
        Set<Vo> set = Sets.newHashSet();
        for (int i = 0; i < cnt; i++) {
            list.add(new Vo(String.valueOf(i)));
            if (i % 10 == 0) {
                continue;
            }
            set.add(new Vo(String.valueOf(i)));
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        FastRemoveList01<Vo> fastRemoveList = FastRemoveList01.of(list);
        Iterator<Vo> iterator = set.iterator();
        while (iterator.hasNext()) {
            Vo next = iterator.next();
            fastRemoveList.remove(next);
        }
        List<Vo> remainList = fastRemoveList.getRemainList();
        stopwatch.stop();
        long time = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        return time;
    }

    @Test
    public void test1() {
        long max = 0;
        long min = 0;
        long total = 0;
        int loop = 100;
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

    @Test
    public void test3() throws Exception {
        Integer[] arr = new Integer[16];
        for (int i = 0; i < 16; i++) {
            arr[i] = i;
        }

        ArrayList<Integer> integers = new ArrayList<>(32);
        UNSAFE.putInt(integers, arrayListSizeOffset, 16);
        UNSAFE.putObject(integers, arrayListEleDataOffset, arr);
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(integers));
    }

    @Test
    public void test4() throws Exception {
        List<Integer> list = new ArrayList<>(0);
        list.add(1);

        List<Integer> list2 = new ArrayList<>(0);
        list2.add(1);
    }
}
