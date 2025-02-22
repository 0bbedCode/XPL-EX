function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    local ths = param:getThis()
    if ths == nil then
        return false
    end

    local DetailedStateClass = luajava.bindClass('android.net.NetworkInfo$DetailedState')
    local DetailedStateEnum = DetailedStateClass.DISCONNECTED

    if ths:getType() == 0x11 and ret ~= DetailedStateEnum then
        param:setResult(DetailedStateEnum)
        return true, tostring(ret), 'DISCONNECTED'
    end

    return false
end
