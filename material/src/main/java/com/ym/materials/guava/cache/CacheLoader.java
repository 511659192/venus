package com.ym.materials.guava.cache;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by ym on 2018/8/4.
 */
public abstract class CacheLoader<K, V> {

    protected CacheLoader() {
    }

    public abstract V load(K key) throws Exception;

    public ListenableFuture<V> reload(K key, V oldValue) throws Exception {
        checkNotNull(key);
        checkNotNull(oldValue);
        return Futures.immediateFuture(load(key));
    }
}
