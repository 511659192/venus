package eurekademo.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.Name;
import java.util.Map;

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
        cacheService.getById2(lv);
        cacheService.getById2(lv);
        return "test";
    }

    @RequestMapping(value = "templlate", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Map<String, Object> templlate() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("title", "测试");
        map.put("name", "张三");
        map.put("age", 20);
        map.put("description", "<script>alert(1);</script>");
//        map.put("description", "description");
        map.put("hobby", Lists.newArrayList("电影", "音乐", "阅读"));
        map.put("score", ImmutableMap.of("语文", 90, "数学", 80, "英语", 70));
        map.put("score2", Lists.newArrayList(ImmutableMap.of("name", "语文", "score", 90),
                ImmutableMap.of("name", "数学", "score", 80),
                ImmutableMap.of("name", "英语", "score", 70)));
        System.out.println(JSON.toJSONString(map));
        return map;
    }
}
