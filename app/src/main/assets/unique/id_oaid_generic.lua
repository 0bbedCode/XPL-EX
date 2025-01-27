function after(hook, param)
    local res = param:getResult()
    if res ~= nil then
        local fake = param:getSetting("unique.open.anon.advertising.id", "84630630-u4ls-k487-f35f-h37afe0pomwq")
        if fake ~= nil then
            param:setResult(fake)
            return true, res, fake
        end
    end
    return false
 end