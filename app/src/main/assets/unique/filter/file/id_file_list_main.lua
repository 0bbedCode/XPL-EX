function after(hook, param)
    local filter = param:interceptFileList()
    if filter ~= nil and filter == true then
        return true, param:getOldResult(), param:getNewResult()
    end
    return false
end