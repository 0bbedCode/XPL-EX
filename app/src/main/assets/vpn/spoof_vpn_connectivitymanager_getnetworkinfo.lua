function after(hook, param)
    local arg = param:getArgument(0)
    if arg == nil then
        return false
    end

    if arg == 0x11 then
        log("TYPE_VPN Check 0x11")
        param:setResult(nil)
        return true, 'TYPE_VPN', 'null'
    end

    return false
end
