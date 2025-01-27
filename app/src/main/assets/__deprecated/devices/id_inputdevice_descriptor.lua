function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local fake = param:generateRandomString(40)
    param:setResult(fake)
    return true, res, fake
end