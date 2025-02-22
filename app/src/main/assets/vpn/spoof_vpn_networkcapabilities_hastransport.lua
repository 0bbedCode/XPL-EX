function after(hook, param)
    local arg = param:getArgument(0)
    if arg == nil then
        return false
    end

    if arg == 0x4 then
        log("TRANSPORT_VPN Check 0x4")
        param:setResult(false)
        return true, 'TRANSPORT_VPN', 'false'
    end

    return false
end
