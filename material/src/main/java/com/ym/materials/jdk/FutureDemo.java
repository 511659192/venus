package com.ym.materials.jdk;

import java.util.concurrent.*;

public class FutureDemo {


    /**
     * jdk 线程池 对关闭后的提交拒绝策略实现的并不怎么友好
     * 先关闭线程池 再提交 将会导致调用线程无限阻塞 或者 抛出异常
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService executorService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy());

        executorService.shutdownNow();
        Future<String> future = executorService.submit(() -> {
            return "aaaa";
        });

        System.out.println(future.get(1, TimeUnit.SECONDS));

    }
}
