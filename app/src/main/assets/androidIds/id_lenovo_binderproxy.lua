function after(hook, param)
    local filter = param:filterBinderProxyAfter("levad")
    if filter ~= nil then
        param:setResult(true)
        local newResult = param:getSetting("unique.open.anon.advertising.id")
        if newResult ~= nil then
            return true, filter, newResult
        end
        return true, "Spoofed:", filter
    end

    return false
end