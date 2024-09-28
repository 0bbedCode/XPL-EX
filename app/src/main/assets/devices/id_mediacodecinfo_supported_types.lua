function after(hook, param)
    local res = param:getResult()
    if res ~= nil then
        local fake = param:generateMediaCodecSupportedTypeList()
        param:setResult(fake)
        return true, "Spoofed", "Coded Supported Types"
    end
    return false
end