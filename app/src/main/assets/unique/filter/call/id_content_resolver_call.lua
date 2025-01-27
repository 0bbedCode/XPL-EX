function after(hook, param)
    local res = param:isSettingsContentBad(true)
    if res ~= nil and res == true then
        return true, param:getOldResult(), param:getNewResult()
    end
    if res == nil then
        param:isNullError(hook)
    end
    return false
end