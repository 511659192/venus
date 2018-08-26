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
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping(value = "var", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Map<String, Object> var() {
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

    @RequestMapping(value = "{page}")
    public String index(@PathVariable("page") String page) {
        return page;
    }

    @RequestMapping("thymeleaf1")
    @ResponseBody
    public String thymeleaf() {
        String text =
//                "{(header.html)}  \n" +
                "   <body>  \n" +
                "   \n" +
                "    <p>aaaaaa</p>\n" +
                "   \n" +
                "      {# 不转义变量输出 #}  \n" +
                "      姓名：{* string.upper(name) *}<br/>  \n" +
                "      {# 转义变量输出 #}  \n" +
                "      简介：{{description}}<br/>  \n" +
                "      {# 可以做一些运算 #}  \n" +
                "      年龄: {* age + 1 *}<br/>  \n" +
                "      {# 循环输出 #}  \n" +
                "      爱好：  \n" +
                "      {% for i, v in ipairs(hobby) do %}  \n" +
                "         {% if i > 1 then %}，{% end %}  \n" +
                "         {* v *}  \n" +
                "      {% end %}<br/>  \n" +
                "  \n" +
                "      成绩：  \n" +
                "      {% local i = 1; %}  \n" +
                "      {% for k, v in pairs(score) do %}  \n" +
                "         {% if i > 1 then %}，{% end %}  \n" +
                "         {* k *} = {* v *}  \n" +
                "         {% i = i + 1 %}  \n" +
                "      {% end %}<br/>  \n" +
                "      成绩2：  \n" +
                "      {% for i = 1, #score2 do local t = score2[i] %}  \n" +
                "         {% if i > 1 then %}，{% end %}  \n" +
                "          {* t.name *} = {* t.score *}  \n" +
                "      {% end %}<br/>  \n" +
                "      成绩3：  \n" +
                "      {% local i = 1; %}  \n" +
                "      {% for k, v in pairs(score2) do %}  \n" +
                "         {% if i > 1 then %}，{% end %}  \n" +
                "         {* v.name *} = {* v.score *}  \n" +
                "         {% i = i + 1 %}  \n" +
                "      {% end %}<br/>  \n" +
                "      \n" +
                "      {# 中间内容不解析 #}  \n" +
                "      {-raw-}{* description *}{-raw-}  \n" +
//                "{(footer.html)}\n" +
                "\n" ;
//                "<script>\n" +
//                "   alert(111)\n" +
//                "</script>";
        return text;
    }
}
