package com.ym.materials.guava.concurrent;

import org.junit.Assert;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.ym.materials.guava.concurrent.Uninterruptibles.getUninterruptibly;

public class Futures extends GwtFuturesCatchingSpecialization{
    public static <V> V getDone(Future<V> future) throws ExecutionException {
        Assert.assertTrue(future.isDone());
        return getUninterruptibly(future);
    }
}
