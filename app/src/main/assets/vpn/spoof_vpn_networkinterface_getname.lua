function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    local context = param:getApplicationContext()
    local selfcall = param:getValue('VD.VPN.selfCall.name.' .. param:getPackageName(), context)

    if selfcall ~= nil and selfcall == '1' then
        return false
    end

    if string.find(ret, "^tun%d*$") or string.find(ret, "^pptp%d*$") or string.find(ret, "^ppp%d*$") then
        log("VPN Interface Spoofing...")
        param:setResult("dummy1")
        return true, ret, "dummy1"
    end

    return false
end
