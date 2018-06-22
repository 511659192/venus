package com.ym.materials.json.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class IdentityHashMap<K, V> {

    private final Entry<K, V>[] buckets;
    private final int indexMask;
    public final static int DEFAULT_SIZE = 8192;

    public IdentityHashMap() {
        this(DEFAULT_SIZE);
    }

    public IdentityHashMap(int tableSize) {
        this.indexMask = tableSize - 1;
        this.buckets = new Entry[tableSize];
    }

    public final V get(K key) {
        int hash = System.identityHashCode(key);
        int bucket = hash & indexMask;
        for (Entry<K, V> entry = buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                return entry.value;
            }
        }
        return null;
    }

    public Class findClass(String keyString) {
        for (int i = 0; i < buckets.length; i++) {
            Entry<K, V> bucket = buckets[i];
            if (bucket == null) {
                continue;
            }

            for (Entry<K, V> entry = bucket; bucket != null; bucket = bucket.next) {
                K key = bucket.key;
                if (key instanceof Class) {
                    Class<K> clazz = (Class<K>) key;
                    String clazzName = clazz.getName();
                    if (StringUtils.equals(keyString, clazzName)) {
                        return clazz;
                    }
                }
            }
        }
        return null;
    }

    public V put(K key, V value) {
        int hash = System.identityHashCode(key);
        int bucket = hash & indexMask;
        for (Entry<K, V> entry = buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
        }

        Entry<K, V> entry = new Entry<K, V>(key, value, hash, buckets[bucket]);
        buckets[bucket] = entry; // 并发是处理时会可能导致缓存丢失，但不影响正确性 怎么可能呢？
        return null;
    }

    protected final static class Entry<K, V> {
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

    public void clear() {
        Arrays.fill(buckets, null);
    }

    public static void main(String[] args) {
        int tableSize = 16;
        int leftMostOnePos = 31 - Integer.numberOfLeadingZeros(tableSize) + 1; // 最左侧1的位置
        tableSize = (1 << leftMostOnePos);
        System.out.println(tableSize);

        System.out.println(Integer.toBinaryString(100));
        System.out.println(Integer.toBinaryString(-100));
    }
}
