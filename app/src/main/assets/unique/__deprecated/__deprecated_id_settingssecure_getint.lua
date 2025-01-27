function before(hook, param)
    local arg = param:getArgument(1)
    if arg == "limit_ad_tracking" then
        param:setResult(1)
        return true
    end
    return false
end