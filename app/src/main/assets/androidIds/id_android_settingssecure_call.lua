function after(hook, param)
    local fake = param:getSettingReMap("unique.android.id", "value.android_id", "0000000000000000")
    if fake ~= nil then
        if param:filterSettingsCall("android_id", fake) then
            return true, "Spoofed:", fake
        end
    end
	return false
end