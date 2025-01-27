function after(hook, param)
	local res = param:getResult()
	if res == nil then
	    return false
	end

    local fake = param:getSettingReMap("region.timezone", "zone.tzshort", "GMT+0")
    if fake == nil then
        return false
    end

    local tzcl = luajava.bindClass("java.util.TimeZone")
    local tz = tzcl:getTimeZone(fake)
	param:setResult(tz)
	return true, "N/A", fake
end