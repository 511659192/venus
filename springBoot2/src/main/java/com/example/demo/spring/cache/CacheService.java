package com.example.demo.spring.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Cacheable(cacheNames = "defaultCache", keyGenerator = "uuidKeyGenerator")
    public String getName(String name, String name2) {
        System.out.println("from db");
        return name;
    }
}
