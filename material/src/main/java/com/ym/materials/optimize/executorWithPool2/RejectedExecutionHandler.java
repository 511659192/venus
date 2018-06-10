package com.ym.materials.optimize.executorWithPool2;

import java.util.concurrent.*;

/**
 * Created by ym on 2018/6/10.
 */
public interface RejectedExecutionHandler {

    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
