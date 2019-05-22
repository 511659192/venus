package eurekademo.controller;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by ym on 2018/8/22.
 */
@Controller
public class ViewsController {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/views/index")
    public String index() {
        return "views/index";
    }

    @RequestMapping(value = "/test", params = "a=b")
    @ResponseBody
    public String testRequest(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        StringBuilder builder = new StringBuilder();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            builder.append("&").append(key).append("=").append(request.getParameter(key));
        }
        logger.error(builder.toString().substring(1));
        return "views/index" + request.getParameter("b");
    }

    @RequestMapping(value = "/test1")
    @ResponseBody
    public String test11(HttpServletRequest request) {
        return "views/index";
    }
}
