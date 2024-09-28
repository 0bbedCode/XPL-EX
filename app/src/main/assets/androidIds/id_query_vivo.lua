function after(hook, param)
    local auth = 'com.vivo.vms.IdProvider'
    if param:isAuthority(auth) then
        local fake = param:getSetting("unique.open.anon.advertising.id", "84630630-u4ls-k487-f35f-h37afe0pomwq")
        if fake ~= nil then
            if param:queryFilterAfter(auth, 'IdentifierId|OAID', fake) then
                return true, 'Spoofed:', setting
            end
        end
    end
	return false
end