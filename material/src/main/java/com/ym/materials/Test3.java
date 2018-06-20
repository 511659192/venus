package com.ym.materials;

import java.util.concurrent.CountDownLatch;

public class Test3 {

    private static int x = 0;
    private static int a = 0, b =0;

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        for(;;) {
            i++;
            x = 0;
            Thread one = new Thread(() -> {
                x = 1;
            });
            one.start();
            one.join();
            String result = "第" + i + "次 (" + x + "）";
            if(x == 0) {
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
