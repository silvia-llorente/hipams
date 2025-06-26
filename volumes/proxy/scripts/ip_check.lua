local ip_deny_dict = ngx.shared.ip_deny
local client_ip = ngx.var.remote_addr

if ip_deny_dict:get(client_ip) then
    ngx.exit(ngx.HTTP_FORBIDDEN)  -- Deny access
end