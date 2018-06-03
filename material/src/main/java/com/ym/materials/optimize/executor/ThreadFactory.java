package com.ym.materials.optimize.executor;

/**
 * Created by ym on 2018/6/3.
 */
public interface ThreadFactory {

    Thread newThread(Runnable runnable);
}
