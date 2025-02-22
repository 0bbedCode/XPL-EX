function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    local int = param:getArgument(0)
    if int == 0xf then
        log("NET_CAPABILITY_NOT_VPN Check 0xf")
        param:setResult(true)
        return true, 'NET_CAPABILITY_NOT_VPN', 'true'
    end

    return false
end
