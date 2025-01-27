function after(hook, param)
    local result = param:getResult()
    local fake = param:getSettingReMap("unique.bluetooth.address", "bluetooth.id", "00:00:00:00:00:00")
    if fake ~= nil and result ~= nil then
        if param:filterSettingsSecure("bluetooth_name", fake) then
            return true, result, fake
        end
    end
	return false
end