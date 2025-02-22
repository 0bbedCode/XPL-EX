function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    if string.find(ret, '%%tun%d*$') or string.find(ret, '%%pptp%d*$') or string.find(ret, '%%ppp%d*$') then
        log("VPN Address Spoofing...")
        local fake = string.gsub(ret, '%%.*', '%%dummy1')
        param:setResult(fake)
        return true, ret, fake
    end

    return false
end
