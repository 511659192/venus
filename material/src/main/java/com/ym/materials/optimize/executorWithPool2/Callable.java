package com.ym.materials.optimize.executorWithPool2;

/**
 * Created by ym on 2018/6/10.
 */
public interface Callable<T> {
    T call() throws Exception;
}
