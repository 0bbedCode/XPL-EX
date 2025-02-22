function after(hook, param)
    local ret = param:getResult()
    if ret == nil then 
        return false
    end

    if ret ~= 0 then
        param:setResult(0)
        return true, tostring(ret), '0'
    end

    return false
end
