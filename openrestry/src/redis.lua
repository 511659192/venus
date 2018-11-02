ngx.say("hello redis111")

local redis = require "resty.redis" 
local key = "ngx.var.key" 
local value = "ngx.var.value"
ngx.say("key", key)
local red = redis:new()
red:set_timeout(10000)
ok, err = red:connect("192.168.171.31",5360)
ngx.say(ok)
if not ok then 
ngx.say("failed to connect: ", err) 
return 
end 
local auth, err = red:auth("jim://2581594387536019499/80000013")
ngx.say(auth)
if not auth then 
ngx.say("failed to authenticate: ", err) 
return 
end 
local getRes, err = red:get(key)
ngx.say(getRes) 


local getRes, err = red:get("key") 
ngx.say("key ", getRes) 

local getRes, err = red:get("key1") 
ngx.say("key1 ", getRes) 


local getRes, err = red:get("key2") 
ngx.say("key2 ", getRes) 


local getRes, err = red:get("key3") 
ngx.say("key3 ", getRes) 

local getRes, err = red:get("key4") 
ngx.say("key4 ", getRes) 


local ok, err = red:set_keepalive(10000, 100)
if not ok then 
ngx.say("failed to set keepalive: ", err) 
return 
end
