package eurekademo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by ym on 2018/8/22.
 */
@Controller
public class ViewsController {

    @RequestMapping("/views/index")
    public String index() {
        return "views/index";
    }
}
