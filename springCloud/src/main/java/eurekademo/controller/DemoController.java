package eurekademo.controller;

import eurekademo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by ym on 2018/8/22.
 */
@Controller
@RequestMapping(value = "run")
public class DemoController {

    @Autowired
    private CacheService cacheService;

    @RequestMapping("test")
    @ResponseBody
    public String test() {
        User user = new User();
        user.setName("user");
        cacheService.getById(user);
        return "test";
    }
}
