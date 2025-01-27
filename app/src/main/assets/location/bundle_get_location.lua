function after(hook, param)
    local result = param:getResult()
    if param:getException() ~= nil or result == nil then
        return false
    end

    local key = param:getArgument(0)
    if key == nil then
        return false
    end

    if key ~= 'location' then
        return false
    end

    local provider = param:getSetting("location.provider")
    if provider == nil then
        provider = "privacy"
    end

    local fake = luajava.newInstance('android.location.Location', provider)
    local lng = param:getSetting("location.longitude")
    local lat = param:getSetting("location.latitude")
    if lng == nil or lat == nil then
        lng = "0"
        lat = "0"
    end

    local lngNum = tonumber(lng)
    local latNum = tonumber(lat)
    if lngNum == nil or latNum == nil then
        log("Converting long and latt to number failed")
        lngNum = 0
        latNum = 0
    end

    fake:setLatitude(latNum)
    fake:setLongitude(lngNum)
    param:setResult(fake)
    return true, result:toString(), fake:toString()
end
