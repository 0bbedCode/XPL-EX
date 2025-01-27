function after(hook, param)
    local fake = param:getSettingReMap("unique.bluetooth.address", "bluetooth.id", "00:00:00:00:00:00")
    if fake ~= nil then
        if param:filterSettingsCall("bluetooth_name", fake) then
            return true, "Spoofed:", fake
        end
    end
	return false
end