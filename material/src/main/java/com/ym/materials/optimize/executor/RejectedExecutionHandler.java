package com.ym.materials.optimize.executor;

/**
 * Created by ym on 2018/6/3.
 */
public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable r, MyThreaPoolExecutor executor);

    static class AbortPolicy implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, MyThreaPoolExecutor executor) {
            // do nothing
        }
    }
}
