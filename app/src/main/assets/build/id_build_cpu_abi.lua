function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local fake = param:getSetting("soc.cpu.abi", "arm64-v8a")
    if fake == nil then
        return false
    end

    param:setResult(fake)
    return true, ret, fake
end