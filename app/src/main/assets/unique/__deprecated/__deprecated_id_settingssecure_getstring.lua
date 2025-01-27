function after(hook, param)
    local result = param:getResult()
    local fake = param:getSettingReMap("unique.android.id", "value.android_id", "0000000000000000")
    if fake ~= nil and result ~= nil then
        if param:filterSettingsSecure("android_id", fake) then
            return true, result, fake
        end
    end
	return false
end