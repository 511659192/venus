package com.ym.materials.guava.cache;

import java.util.concurrent.ExecutionException;

/**
 * Created by ym on 2018/8/4.
 */
public class CacheDemo {

    public static void main(String[] args) throws ExecutionException {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        LoadingCache<String, String> cache = builder.build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                System.out.println("加载创建key:" + key);
                return key;
            }
        });
        String name = cache.get("name");
        System.out.println(name);



    }
}
