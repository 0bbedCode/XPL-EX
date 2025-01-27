function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local fake = param:getSetting("hardware.gps.model.name", param:generateRandomString(20, 60))
    param:setResult(fake)
    return true, res, fake
end