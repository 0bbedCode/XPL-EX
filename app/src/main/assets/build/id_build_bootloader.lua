function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local fake = param:getSetting("device.bootloader", "G1HZK00500")
    if fake == nil or fake == 'unknown' then
        return false
    end

    param:setResult(fake)
    return true, ret, fake
end