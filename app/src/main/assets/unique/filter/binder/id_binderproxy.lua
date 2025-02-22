function after(hook, param)
    local filter = param:ensureIpcIsSafe(true)
    if filter ~= nil and filter == true then
        return true, param:getOldResult(), param:getNewResult(), param:getSettingResult()
    end
    return false
end