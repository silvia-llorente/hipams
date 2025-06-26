-- Load the shared dict
local ip_deny_dict = ngx.shared.ip_deny

-- Load the ngx_lua module
local ngx = require "ngx"

-- Function to read IP addresses from the config file
local function read_ip()
    ngx.req.read_body()
    local request_body = ngx.req.get_body_data()
    -- ip=X.X.X.X - suppose body can have more dummy data
    local params = {}
    for param in request_body:gmatch("([^&]+)") do
        local key, value = param:match("([^=]*)=([^=]*)")
        params[key] = value
    end

    return params["ip"]
end

-- Function to update Nginx deny rules
local function update_ip_deny_rules()
    -- Get IP from body
    local ip = read_ip()
    -- Delete deny rule
    ip_deny_dict:delete(ip)
    if ip_deny_dict:get(ip) then
        return false
    else
        return true
    end
end

local ok = update_ip_deny_rules()
if ok then
    ngx.status = 200
    ngx.say("IP DISCARDED")
    ngx.exit(ngx.HTTP_OK)
else
    ngx.status = 500
    ngx.say("IP NOT DISCARDED")
    ngx.exit(ngx.HTTP_OK)
end