package com.ym.materials.guava.cache;

/**
 * Created by ym on 2018/8/4.
 */
public interface Weigher<K, V> {

    int weigh(K key, V value);
}
