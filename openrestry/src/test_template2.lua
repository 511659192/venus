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
    path = "/test/templlate",
})

local context = cjson.decode(resp.body)
local template = require("resty.template")
template.render("html/example/templates/template.html", context)

