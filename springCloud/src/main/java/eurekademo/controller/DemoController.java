package eurekademo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.Name;

/**
 * Created by ym on 2018/8/22.
 */
@Controller
@RequestMapping(value = "test")
public class DemoController {

    @Autowired
    private CacheService cacheService;


    @RequestMapping("test")
    @ResponseBody
    public String test() {
        Long lv = Long.valueOf(1);
        cacheService.getById(lv);
        cacheService.getById(lv);
        cacheService.getById(lv);
        return "test";
    }
}
