package com.ym.materials.lock;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 有界队列锁，使用一个volatile数组来组织线程
 * 缺点是得预先知道线程的规模n，所有线程获取同一个锁的次数不能超过n
 * 假设L把锁，那么锁的空间复杂度为O(Ln)
 * **/
public class ArrayLock implements Lock {
    // 使用volatile数组来存放锁标志， flags[i] = true表示可以获得锁
    private volatile boolean[] flags;

    // 执行新加入的节点的最后一个位置
    private AtomicInteger tail;

    private final int capacity;

    private ThreadLocal<Integer> slotIndex = new ThreadLocal<Integer>(){
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public ArrayLock(int capacity) {
        this.capacity = capacity;
        flags = new boolean[capacity];
        tail = new AtomicInteger(0);
        flags[0] = true;
    }

    @Override
    public void lock() {
        int slot = tail.getAndIncrement() % capacity;
        slotIndex.set(slot);
        while (!flags[slot]) {

        }
    }

    @Override
    public void unlock() {
        Integer slot = slotIndex.get();
        flags[slot] = false;
        flags[(slot + 1) % capacity] = true;
    }
}
