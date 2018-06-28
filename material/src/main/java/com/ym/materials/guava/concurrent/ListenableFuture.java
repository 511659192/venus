package com.ym.materials.guava.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface ListenableFuture<T> extends Future<T> {

    void addListener(Runnable listener, Executor executor);
}
