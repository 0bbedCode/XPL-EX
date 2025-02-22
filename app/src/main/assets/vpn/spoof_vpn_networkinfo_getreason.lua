function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    param:setResult(nil)
    return true, tostring(ret), 'Null'
end
