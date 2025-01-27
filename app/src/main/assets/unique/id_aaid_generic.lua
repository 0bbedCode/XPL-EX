function after(hook, param)
    local res = param:getResult()
    if res ~= nil then
        local fake = param:getSetting("unique.app.anon.id")
        if fake ~= nil then
            param:setResult(fake)
            return true, result, fake
        end
    end
    return false
end