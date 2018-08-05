package com.ym.materials.guava.cache;

import com.google.common.cache.RemovalNotification;

/**
 * Created by ym on 2018/8/5.
 */
public interface RemovalListener<K, V> {

    void onRemoval(RemovalNotification<K, V> notification);

    enum NullListener implements RemovalListener<Object, Object> {
         INSTANCE;

        @Override
        public void onRemoval(RemovalNotification<Object, Object> notification) {

        }
    }
}
