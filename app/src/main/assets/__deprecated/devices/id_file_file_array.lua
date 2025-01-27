function after(hook, param)
    local ths = param:getThis()
    local res = param:getResult()
    if res == nil then
        return false
    end

    if param:isDriverFile(ths:getPath()) then
        log("Is Diriver Folder Root: " .. ths:getPath())
        param:setResult(null)
        return true
    end

    log("Cleaning out File Array")
    local fakes = param:filterFilesArray(res)
    if fakes == nil then
        log("Setting Results to NULL")
        param:setResult(null)
        return true
    else
        log("Setting Filtered Results")
        param:setResult(fakes)
        return true
    end

    return false
end