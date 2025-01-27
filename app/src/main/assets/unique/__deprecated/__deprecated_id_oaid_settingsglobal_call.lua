function after(hook, param)
    local fake = param:getSetting("unique.open.anon.advertising.id", "84630630-u4ls-k487-f35f-h37afe0pomwq")
    if fake ~= nil then
        if param:filterSettingsCall("oaid|pps_oaid_c", fake) then
            return true, "Spoofed:", fake
        end
    end
	return false
end