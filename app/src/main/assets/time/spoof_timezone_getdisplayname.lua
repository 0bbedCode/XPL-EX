function after(hook, param)
	local res = param:getResult()
	if res == nil then
	    return false
	end

	local fake = param:getSettingReMap("region.timezone.display.label", "zone.displayname", "Greenwich Mean Time")
	if fake == nil then
	    return false
	end

	param:setResult(fake)
	return true, res, fake
end