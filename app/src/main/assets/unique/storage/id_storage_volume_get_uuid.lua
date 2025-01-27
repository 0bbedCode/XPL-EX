function after(hook, param)
    local res = param:getResult()
    if res ~= nil then
        local fake = param:randomUUIDString()
        param:setResult(fake)
        return true, res, fake
    end
    return false
end