package com.ym.materials;

import java.util.concurrent.CountDownLatch;

public class Test2 {

    private static int x = 0, y = 0;
    private static int a = 0, b =0;

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        for(;;) {
            i++;
            x = 0; y = 0; a = 0; b = 0;
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Thread one = new Thread(() -> {
                await(countDownLatch);
                a = 1;
                x = b;
            });

            Thread other = new Thread(() -> {
                await(countDownLatch);
                b = 1;
                y = a;
            });
            one.start();other.start();
            countDownLatch.countDown();
            one.join();other.join();
            String result = "第" + i + "次 (" + x + "," + y + "）";
            if(x == 0 && y == 0) {
                System.err.println(result);
                break;
            } else {
                System.out.println(result);
            }
        }
    }

    private static void await(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
