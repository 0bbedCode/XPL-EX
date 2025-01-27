function after(hook, param)
    log("Test INPUT DEVS")
    local res = param:interceptRemoveExternalDeviceIds()
    if res ~= nil and res == true then
        return true, param:getOldResult(), param:getNewResult()
    end
    return false
end