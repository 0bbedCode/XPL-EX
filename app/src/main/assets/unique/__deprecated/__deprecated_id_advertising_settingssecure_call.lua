function after(hook, param)
    local fake = param:getSettingReMap("unique.google.advertising.id", "ad.id", "84630630-u4ls-k487-f35f-h37afe0pomwq")
    if fake ~= nil then
        if param:filterSettingsCall("advertising_id|ad_aaid|oaid|aid|aaid", fake) then
            return true, "Spoofed:", fake
        end
    end
	return false
end