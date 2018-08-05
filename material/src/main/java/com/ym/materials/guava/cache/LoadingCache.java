package com.ym.materials.guava.cache;

import java.util.concurrent.ExecutionException;

/**
 * Created by ym on 2018/8/4.
 */
public interface LoadingCache<K, V> extends Cache<K, V> {

    V get(K key) throws ExecutionException;
}
