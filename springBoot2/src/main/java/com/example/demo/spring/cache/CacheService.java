package com.example.demo.spring.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Cacheable(cacheNames = "defaultCache")
    public String getName(String name) {
        System.out.println("from db");
        return name;
    }
}
