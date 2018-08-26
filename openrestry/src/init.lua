--初始化耗时的模块
local redis = require 'resty.redis'
local cjson = require 'cjson'

--全局变量，不推荐
count = 1

--共享全局内存
local shared_data = ngx.shared.shared_data
shared_data:set("count", 1)

--package.path =  package.path .. '../../../idea/venus/openrestry/src/lualib/?.lua;'    --搜索lua模块


props = {
    host = "127.0.0.1",
    port = 3306,
    database = "mysql",
    user = "root",
    password = ""
}

globalVar = {
    "a", "b", "c"
}