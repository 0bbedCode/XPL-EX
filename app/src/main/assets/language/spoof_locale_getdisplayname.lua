function after(hook, param)
	local res = param:getResult()
    if res == nil then
        return false
    end

	local country = param:getSetting("region.country", "Iceland")
	local language = param:getSetting("region.language", "Icelandic")
	if country == nil or language == nil then
	    return false
	end

	local fake = language .. " (" .. country .. ")"
	param:setResult(fake)
	return true, res, fake
end