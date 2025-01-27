function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local p = param:getArgument(1)
    if p == nil then
        return false
    end

    log("GPU QUERY STRING: " .. p)

    if p == 12371 then
        local vendor = param:getSetting("gpu.gl.vendor")
        if vendor == nil then
            return false
        end

        param:setResult(vendor)
        return true, res, vendor
    end

    if p == 12372 then
        local ver = param:getSetting("soc.gpu.open.gles.version")
        if ver == nil then
            return false
        end

        param:setResult(ver)
        return true, res, ver
    end

    return false
end