function after(hook, param)
    local fake = param:getSetting("unique.guid.uuid", "c651fde4-6ea1-4a41-882c-59bc2e94571d")
    if fake ~= nil then
        if param:filterSettingsCall("guid_uuid|uuid|guid", fake) then
            return true, "Spoofed:", fake
        end
    end
	return false
end