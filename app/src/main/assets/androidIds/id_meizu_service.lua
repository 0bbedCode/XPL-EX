function after(hook, param)
    local auth = 'com.meizu.flyme.openidsdk'
    if param:isAuthority(auth) then
        local setting = param:getSetting('unique.open.anon.advertising.id', '84630630-u4ls-k487-f35f-h37afe0pomwq')
        if setting ~= nil then
            if param:queryFilterAfter(auth, 'oaid', setting) then
                return true, 'Spoofed:', setting
            end
        end
    end
	return false
end
