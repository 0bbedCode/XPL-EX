function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local isAdId = param:isAdvertisingId(res)
    if isAdId == nil then
        return false
    end

    if isAdId == true then
        local setting = param:getSettingReMap("unique.google.advertising.id", "ad.id")
        if setting == nil then
            return false
        end

        param:setResult(setting)
        return true, res, setting
    end

    return false
end