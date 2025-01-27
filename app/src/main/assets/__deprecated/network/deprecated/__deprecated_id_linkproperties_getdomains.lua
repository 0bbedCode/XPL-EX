function after(hook, param)
    local result = param:getResult()
    if result == nil then
        return false
    end

    local fake = param:getSetting("network.dns", "1.1.1.1")
    if fake == nil then
        return false
    end

    param:setResult(fake)
    return true, result, fake
end