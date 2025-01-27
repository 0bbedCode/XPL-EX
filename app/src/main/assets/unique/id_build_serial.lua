function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local fake = param:getSetting("unique.serial.no", "unknown")
    if fake == nil or fake == 'unknown' then
        return false
    end
    param:setResult(fake)
    return true, ret, fake
end