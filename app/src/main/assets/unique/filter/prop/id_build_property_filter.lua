function after(hook, param)
    local res = param:ensurePropertyIsSafe()
    if res ~= nil and res == true then
        return true, param:getOldResult(), param:getNewResult(), param:getSettingResult()
    end
    if res == nil then
        param:isNullError(hook)
    end
    return false
end