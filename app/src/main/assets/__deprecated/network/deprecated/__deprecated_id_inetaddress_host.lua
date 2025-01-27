function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local fake = param:getSetting("network.host.address", "127.0.0.1")
    if fake == nil then
        return false
    end

    param:setResult(fake)
    return true, res, fake
end
