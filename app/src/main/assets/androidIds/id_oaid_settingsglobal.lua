function after(hook, param)
    local result = param:getResult()
    local fake = param:getSetting("unique.open.anon.advertising.id", "84630630-u4ls-k487-f35f-h37afe0pomwq")
    if fake ~= nil and result ~= nil then
        if param:filterSettingsSecure("oaid|pps_oaid_c", fake) then
            return true, result, fake
        end
    end
	return false
end