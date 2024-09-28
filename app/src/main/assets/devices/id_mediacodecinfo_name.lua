function after(hook, param)
    local res = param:getResult()
    if res ~= nil then
        local fake = param:generateMediaCodecName()
        param:setResult(fake)
        return true, res, fake
    end
    return false
end