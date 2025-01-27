function before(hook, param)
    local a = param:getArgument(0)
    if a == nil then
        return false
    end

    local al = string.lower(a)
    if al == "dd_android_advertising_id" then
        local gid = param:getSetting("unique.google.advertising.id")
        if gid ~= nil then
            param:setArgument(1, gid)
            return true
        end
    end
    
    if al == "dd_device_id" or al == "dd_android_id" then
        local aid = param:getSetting("unique.android.id")
        if aid ~= nil then
            param:setArgument(1, aid)
            return true
        end
    end


    return false
end