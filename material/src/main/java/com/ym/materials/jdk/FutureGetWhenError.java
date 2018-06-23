package com.ym.materials.jdk;

import java.util.concurrent.*;

public class FutureGetWhenError {

    /**
     * 线程池内部抛出异常 需要catch 并不是通过结果来返回的
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
//                return "norm";
                throw  new RuntimeException("exception");
            }
        });

        System.out.println(future.get() instanceof Exception);
    }
}
