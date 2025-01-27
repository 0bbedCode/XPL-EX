function before(hook, param)
    local ths = param:getThis()
    if ths == nil then
        return false
    end

    local path = ths:getPath()
    if path == nil then
        return false
    end

    if param:isDriverFile(path) then
        log("File.exists() IS Driver, Returning False... [" .. path .. "]")
        param:setResult(false)
        return true
    end

    return false
end