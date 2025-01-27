--unique.open.anon.advertising.id
function after(hook, param)
    local result = param:getResult()
    local fake = param:getSetting("unique.guid.uuid", "c651fde4-6ea1-4a41-882c-59bc2e94571d")
    if fake ~= nil and result ~= nil then
        if param:filterSettingsSecure("guid_uuid|uuid|guid", fake) then
            return true, result, fake
        end
    end
	return false
end