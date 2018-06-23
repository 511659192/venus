package com.ym.materials.jdk;

public class InterruptDemo {

    /**
     * 线程终端是标记中断 而不是强制退出
     * 这段代码在下标越界前 不会退出
     * @param args
     */
    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i > 0; i++) {
                    System.out.println(i);
                }
            }
        });

        thread.start();
        thread.interrupt();
        System.out.println("main thread done");
    }
}
