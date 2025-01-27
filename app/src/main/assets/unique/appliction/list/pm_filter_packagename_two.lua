function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local p = param:getArgument(0)
    if p == nil then
        return false
    end

    local good = param:isPackageAllowed(p)
    if good == true then
        return false
    end

    local clsFileNotFound = luajava.bindClass("java.lang.IllegalArgumentException")
    local fake = luajava.new(clsFileNotFound, p)
    param:setResult(fake)
    return true
end