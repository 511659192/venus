package eurekademo.controller;

import eurekademo.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by ym on 2018/8/22.
 */
@Service
@CacheConfig(cacheNames = "demo")
public class CacheService {

    @Cacheable(key = "#user.name")
    public User getById(User user) {
        user.setName("user ret");
        return user;
    }
}
