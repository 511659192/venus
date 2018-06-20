package com.ym.materials;

public class Test {

    static int x = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread one = new Thread(new Runnable() {
            public void run() {
                x = 1;
            }
        });
        one.start();
        one.join();
        System.out.println(x);
    }
}
