function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    local arrayClass = luajava.bindClass('java.lang.reflect.Array')
    local NetworkInfoClass = luajava.bindClass('android.net.NetworkInfo')
    local lst = luajava.newInstance('java.util.ArrayList')
    local has_type_vpn = false

    for index = ret['length'], 1, -1 do
        local itm = ret[index]
        if itm:getType() == 0x11 then
            has_type_vpn = true
        else
            lst:add(itm)
        end
    end

    if has_type_vpn == false then
        return false
    end

    local arr = arrayClass:newInstance(NetworkInfoClass, lst:size())
    for i = 0, lst:size() - 1 do
        arr[i + 1] = lst:get(i)
    end

    param:setResult(arr)
    return true
end
