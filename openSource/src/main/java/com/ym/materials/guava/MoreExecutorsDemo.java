package com.ym.materials.guava;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.swing.plaf.TableHeaderUI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class MoreExecutorsDemo {
    InheritableThreadLocal<Integer> local = new InheritableThreadLocal<Integer>() {

        @Override
        protected Integer initialValue() {
            return new Random().nextInt();
        }
    };

    @Test
    public void testThreadLocal() {

        System.out.println(local.get());

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 5; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println(local.get());
                }
            });
        }

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));

        local.remove();
        System.out.println(local.get());
        for (int i = 0; i < 5; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println(local.get());
                }
            });
        }

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        ExecutorService executorService2 = Executors.newFixedThreadPool(2);

        System.out.println("main thread " + Thread.currentThread().getId());
        ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(executorService);
        for (int i = 0; i < 10; i++) {
            ListenableFuture<String> future = listeningExecutorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
                    System.out.println("call thread " + Thread.currentThread().getId());
                    return "ret";
                }
            });

            Futures.addCallback(future, new FutureCallback<String>() {
                @Override
                public void onSuccess(@Nullable String result) {
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(4));
                    System.out.println("callback thread " + Thread.currentThread().getId());
                }

                @Override
                public void onFailure(Throwable t) {

                }
            });
        }

        System.out.println("shutdown");

        listeningExecutorService.shutdown();

    }


    @Test
    public void ab() throws Exception {
        ArrayListMultimap<Integer, Integer> multimap = ArrayListMultimap.<Integer, Integer>create();
        for (int i = 0; i < 20; i++) {
            multimap.put(i % 4, i);
        }

        for (Map.Entry<Integer, Collection<Integer>> entry : multimap.asMap().entrySet()) {
            Collection<Integer> value = entry.getValue();
            System.out.println(((List<Integer>) value));
        }

        System.out.println(multimap.values());

    }

        @Test
    public void aa() throws Exception {

        ExecutorService executorService = new ThreadPoolExecutor(4, 4, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
            ((ThreadPoolExecutor) executorService).allowCoreThreadTimeOut(true);
        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executorService);
        for (int i = 0; i < 10; i++) {
            final Integer aa = i;
            completionService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    TimeUnit.SECONDS.sleep(aa * 3);
                    return aa;
                }
            });
        }

        for (int i = 0; i < 10; i++) {
            Future<Integer> future = completionService.take();
            System.out.println(future == null ? null : future.get());
        }
    }

    @Test
    public void cc() throws Exception {

    }
}
