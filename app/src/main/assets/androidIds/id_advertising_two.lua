function before(hook, param)
    local res = param:filterBinder("adid")
    if res == nil then
        return false
    end

    param:setResult(true)
    return true
end