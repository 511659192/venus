package com.ym.materials.optimize.executor.thread;

/**
 * Created by ym on 2018/6/6.
 */
public class InterruptFlag {

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(new Worker());

        Thread thread = Thread.currentThread();
        thread.interrupt();
        System.out.println(thread.isInterrupted());
        System.out.println(Thread.interrupted());
        System.out.println(Thread.interrupted());

        t.start();

        Thread.sleep(200);
        t.interrupt();

        System.out.println("Main thread stopped.");
    }

    public static class Worker implements Runnable {
        public void run() {
            System.out.println("Worker started.");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Worker IsInterrupted: " + Thread.currentThread().isInterrupted());
            }

            System.out.println("Worker stopped.");
        }
    }
}
