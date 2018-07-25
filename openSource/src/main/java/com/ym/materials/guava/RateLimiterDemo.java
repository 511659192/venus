package com.ym.materials.guava;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterDemo {
    public static void main(String[] args) {
        RateLimiter rateLimiter = RateLimiter.create(10);
        double acquire = rateLimiter.acquire(20);
        System.out.println(acquire);
        acquire = rateLimiter.acquire(1);
        System.out.println(acquire);
    }
}
