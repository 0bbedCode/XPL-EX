function after(hook, param)
    local result = param:getResult()
    if result == nil then
        return false
    end

    local setting = param:getSetting("unique.network.bssid")
    if setting == nil then
        setting = "00:00:00:00:00:00"
    end

    param:setResult(setting)
    return true, result, setting
end