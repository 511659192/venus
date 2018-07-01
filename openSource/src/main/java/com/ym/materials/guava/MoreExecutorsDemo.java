package com.ym.materials.guava;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoreExecutorsDemo {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(executorService);
        ListenableFuture<String> future = listeningExecutorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "ret";
            }
        });
        future.addListener(new Runnable() {
            @Override
            public void run() {
                System.out.println("11111");
            }
        }, executorService);
    }
}
