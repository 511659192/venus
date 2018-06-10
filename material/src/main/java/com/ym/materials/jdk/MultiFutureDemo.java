package com.ym.materials.jdk;

import java.util.concurrent.*;

/**
 * Created by ym on 2018/6/9.
 */
public class MultiFutureDemo {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        final Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "aaa";
            }
        });

        for (int i = 0; i < 10; i++) {
            final int index = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(index + " " + future.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executorService.shutdown();
    }
}
