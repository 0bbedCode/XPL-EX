--function after(hook, param)
--	local m = "90000000000"
--	log("[Runtime].maxMemory() => " .. m)
--	param:setReturnToLongFromStr(m)
--	return true
--end

function after(hook, param)
	local ret = param:getResultLong()
	if ret == nil then
		return false
	end

    local total = param:getSetting("hardware.memory.total")
    if total == nil then
        return false
    end

    local t = tonumber(total)
    if t == nil then
        return false
    end

    --log("Spoofing maxMemory => [MAX]:[" .. total .. "]")
    local fake = param:gigabytesToBytesString(t)
    param:setResultToLong(fake)
    --param:setResult(fake)
	return true, ret, fake
end