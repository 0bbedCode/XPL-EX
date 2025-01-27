function after(hook, param)
    local auth = 'com.facebook.katana.provider.AttributionIdProvider'
    if param:isAuthority(auth) then
        local setting = param:getSetting('unique.facebook.advertising.id', '84630630-u4ls-k487-f35f-h37afe0pomwq')
        if setting ~= nil then
            if param:queryFilterAfter(auth, 'aid', setting) then
                return true, 'Spoofed:', setting
            end
        end
    end
	return false
end


