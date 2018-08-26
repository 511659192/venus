local http = require("resty.http")
local cjson = require("cjson")
local ngx_print = ngx.print

local function pl()
    ngx_print("<br/>")
end

--创建http客户端实例
local httpc = http.new()
local resp, err = httpc:request_uri("http://127.0.0.1:8761", {
    method = "POST",
    path = "/test/var",
})

local context = cjson.decode(resp.body)


resp, err = httpc:request_uri("http://127.0.0.1:8761", {
    method = "POST",
    path = "/test/thymeleaf1",
})
local html = resp.body

local template = require("template")
template.render(html, context)

ngx_print("--------------------------------++++")
httpc:close()