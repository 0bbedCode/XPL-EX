function after(hook, param)
    local auth = 'com.google.android.gsf.gservices'
    if param:isAuthority(auth) then
        local setting = param:getSetting('unique.gsf.id', 'FMZIYEVGXZDCENRO')
        if setting ~= nil then
            if param:queryFilterAfter(auth, 'android_id', setting) then
                return true, 'Spoofed:', setting
            end
        end
    end
	return false
end