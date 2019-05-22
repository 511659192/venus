// Copyright (C) 2019 Meituan
// All rights reserved
package com.ym.materials.guava;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-03-12 18:57
 **/
public class StopWatchDemo {

    public static void main(String[] args) throws Exception {
        Stopwatch started = Stopwatch.createStarted();
        TimeUnit.SECONDS.sleep(1L);
        long elapsed = started.elapsed(TimeUnit.SECONDS);
        System.out.println(elapsed);

        TimeUnit.SECONDS.sleep(2L);elapsed = started.elapsed(TimeUnit.SECONDS);
        System.out.println(elapsed);




    }
}