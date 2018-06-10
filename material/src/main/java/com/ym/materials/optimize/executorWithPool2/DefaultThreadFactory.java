package com.ym.materials.optimize.executorWithPool2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ym on 2018/6/10.
 */
public class DefaultThreadFactory implements ThreadFactory {

    private final static AtomicInteger poolNum = new AtomicInteger(0);
    private final static AtomicInteger threadNum = new AtomicInteger(0);
    private ThreadGroup group;
    private String namePrex;

    public DefaultThreadFactory() {
        group = Thread.currentThread().getThreadGroup();
        namePrex = "pool2-" + poolNum.getAndAdd(1) + "-thread-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(group, runnable, namePrex, 0);
        thread.setDaemon(false);
        thread.setPriority(Thread.NORM_PRIORITY);
        return null;
    }
}
