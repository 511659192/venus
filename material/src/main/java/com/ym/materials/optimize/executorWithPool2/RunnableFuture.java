package com.ym.materials.optimize.executorWithPool2;

import java.util.concurrent.Future;

/**
 * Created by ym on 2018/6/10.
 */
public interface RunnableFuture<T> extends Runnable, Future<T> {
}
