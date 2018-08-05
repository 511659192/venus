package com.ym.materials.guava.cache;

/**
 * Created by ym on 2018/8/4.
 */
public abstract class CacheLoader<K, V> {

    protected CacheLoader() {
    }

    public abstract V load(K key) throws Exception;
}
