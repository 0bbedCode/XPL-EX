function after(hook, param)
    local list = param:getResult()
    if list == nil then
        return false
    end


    local p = param:getArgument(1)
    if p == nil then
        return false
    end

    local good = param:isPackageAllowed(p)
    if good == true then
        return false
    end


    local array = luajava.bindClass('java.lang.reflect.Array')
    if array:getLength(list:toArray()) == 0 then
        return false
    end

    local fake = luajava.newInstance('java.util.ArrayList')
    param:setResult(fake)
    return true
end