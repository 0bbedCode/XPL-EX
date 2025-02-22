function after(hook, param)
    local ret = param:getResult()
    if ret == nil then
        return false
    end

    local arrayClass = luajava.bindClass("java.lang.reflect.Array")
    local intClass = luajava.bindClass("java.lang.Integer")
    local intType = intClass.TYPE

    local lst = {}
    local has_net_capability_not_vpn = false

    for index = ret["length"], 1, -1 do
        local itm = ret[index]
        table.insert(lst, itm)
        if itm == 0xf then
            has_net_capability_not_vpn = true
        end
    end

    if has_net_capability_not_vpn == false then
        table.insert(lst, 15)
    end

    local arr = arrayClass:newInstance(intType, #lst)
    for i = 1, #lst do
        arr[i] = lst[i]
    end

    param:setResult(arr)
    return true, "NET_CAPABILITY_NOT_VPN"
end
