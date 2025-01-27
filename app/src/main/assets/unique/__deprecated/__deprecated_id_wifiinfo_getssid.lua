function after(hook, param)
    local result = param:getResult()
    if result == nil then
        return false
    end

    local setting = param:getSetting("unique.network.ssid")
    if setting == nil then
        setting = "private"
    end

    param:setResult(setting)
    return true, result, setting
end