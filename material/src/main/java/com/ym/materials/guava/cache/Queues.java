package com.ym.materials.guava.cache;

import com.google.common.collect.ImmutableSet;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by ym on 2018/8/5.
 */
public class Queues {

    static <E> Queue<E> discardingQueue() {
        return (Queue<E>) DISCARDING_QUEUE;
    }

    static final Queue<?> DISCARDING_QUEUE = new AbstractQueue<Object>() {
        @Override
        public Iterator<Object> iterator() {
            return ImmutableSet.of().iterator();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean offer(Object o) {
            return true;
        }

        @Override
        public Object poll() {
            return null;
        }

        @Override
        public Object peek() {
            return null;
        }
    };

    static final class WriteQueue<K, V> extends AbstractQueue<ReferenceEntry<K, V>> {

        @Override
        public Iterator<ReferenceEntry<K, V>> iterator() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean offer(ReferenceEntry<K, V> entry) {
            return false;
        }

        @Override
        public ReferenceEntry<K, V> poll() {
            return null;
        }

        @Override
        public ReferenceEntry<K, V> peek() {
            return null;
        }
    }
}
