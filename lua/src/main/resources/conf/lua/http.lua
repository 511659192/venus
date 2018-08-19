    local http = require("resty.http")  
    --创建http客户端实例  
    local httpc = http.new()  
      
    local resp, err = httpc:request_uri("http://s.taobao.com", {  
        method = "GET",  
        path = "/search?q=hello",  
        headers = {  
        }  
    })  
      
    if not resp then  
        ngx.say("request error :", err)  
        return  
    end  
      
    --获取状态码  
    ngx.status = resp.status  
      
    --获取响应头  
    for k, v in pairs(resp.headers) do  
        if k ~= "Transfer-Encoding" and k ~= "Connection" then  
            ngx.header[k] = v  
        end  
    end  
    --响应体  
    ngx.say(resp.body)  
      
    httpc:close()  