-- Load the shared dict
local ip_deny_dict = ngx.shared.ip_deny

-- Load the ngx_lua module
local ngx = require "ngx"

-- Function to read IP addresses from the config file
local function read_ip()
    ngx.req.read_body()
    local request_body = ngx.req.get_body_data()
    ngx.say(request_body)
    -- ip=X.X.X.X&time=XXX - suppose body can have more dummy data
    local params = {}
    for param in request_body:gmatch("([^&]+)") do
        local key, value = param:match("([^=]*)=([^=]*)")
        params[key] = value
    end

    local ip = nil
    if params["ip"] ~= nil then
        ip = params["ip"]
    end

    local time = 0
    if params["time"] ~= nil then
        time = params["time"]
    end

    return ip, tonumber(time)
end

-- Function to update Nginx deny rules
local function update_ip_deny_rules()
    local success, err, forcible
    
    -- Get IP from body
    local ip, time = read_ip()
    if ip == nil then
        success, err, forcible = false, 'IP NOT SENT', ''
    else
        -- Add new deny rule
        success, err, forcible = ip_deny_dict:safe_add(ip, true, time)
    end

    return success, err
end

local ok, err = update_ip_deny_rules()
if ok then
    ngx.status = 200
    ngx.say("IP STORED")
    ngx.exit(ngx.HTTP_OK)
else
    ngx.status = 500
    ngx.say(err)
    ngx.exit(ngx.HTTP_OK)
end