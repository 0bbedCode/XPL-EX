function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    if string.find(ret, '^name:tun%d* %(tun%d*%)$') or
       string.find(ret, '^name:pptp%d* %(pptp%d*%)$') or
       string.find(ret, '^name:ppp%d* %(ppp%d*%)$') then
        log("VPN Interface Spoofing toString...")
        param:setResult("name:dummy1 (dummy1)")
        return true, ret, "name:dummy1 (dummy1)"
    end

    return false
end
