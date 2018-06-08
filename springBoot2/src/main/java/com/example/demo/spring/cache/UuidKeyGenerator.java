package com.example.demo.spring.cache;

import com.alibaba.fastjson.JSON;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sun.security.provider.MD5;

import java.lang.reflect.Method;
import java.util.UUID;

@Component
public class UuidKeyGenerator implements org.springframework.cache.interceptor.KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return JSON.toJSONString(params);
    }
}
