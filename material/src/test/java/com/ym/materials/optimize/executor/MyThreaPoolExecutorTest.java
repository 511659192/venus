package com.ym.materials.optimize.executor;

import org.junit.Test;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by ym on 2018/6/6.
 */
public class MyThreaPoolExecutorTest {
    @Test
    public void submit() throws Exception {
        MyThreaPoolExecutor executor = new MyThreaPoolExecutor(1, 10, 0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>());
        executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println("aaaaaaaa");
                return null;
            }
        });
    }

}