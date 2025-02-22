function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    local ths = param:getThis()
    if ths == nil then
        return false
    end

    local StateClass = luajava.bindClass('android.net.NetworkInfo$State')
    local StateEnum = StateClass.DISCONNECTED

    if ths:getType() == 0x11 and ret ~= StateEnum then
        param:setResult(StateEnum)
        return true, tostring(ret), 'DISCONNECTED'
    end

    return false
end
