package com.ym.materials.guava.cache;

import java.util.AbstractMap;

/**
 * Created by ym on 2018/8/5.
 */
public class RemovalNotification<K, V> extends AbstractMap.SimpleImmutableEntry<K, V> {

    private final RemovalCause cause;

    public static <K, V> RemovalNotification<K, V> create(K key, V value, RemovalCause cause) {
        return new RemovalNotification(key, value, cause);
    }

    private RemovalNotification(K key, V value, RemovalCause cause) {
        super(key, value);
        this.cause = cause;
    }
}
