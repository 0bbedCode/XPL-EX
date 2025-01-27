function after(hook, param)
    local filter = param:interceptOpen()
    if filter ~= nil and filter == true then
        return true, param:getOldResult(), param:getNewResult()
    end
    return false
end