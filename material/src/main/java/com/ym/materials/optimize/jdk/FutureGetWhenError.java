package com.ym.materials.optimize.jdk;

import java.util.concurrent.*;

public class FutureGetWhenError {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
//                return "norm";
                return new RuntimeException("exception");
            }
        });

        System.out.println(future.get() instanceof Exception);
    }
}
