function test()
    ngx.say("test")
end

local function main()
    local share_mem_cache  = ngx.shared.share_mem_cache
    local a, b = share_mem_cache:add("test", "test");
    ngx.say(a)
    ngx.say("<br>")
    ngx.say(b)
    ngx.say("<br>")
    ngx.say("hello world, i am a test")
end
main()

