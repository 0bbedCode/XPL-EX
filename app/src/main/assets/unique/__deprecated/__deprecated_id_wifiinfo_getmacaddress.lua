function after(hook, param)
	local ret = param:getResult()
	if ret == nil then 
		return false
	end

    local fake = param:getSetting("unique.network.mac.address", "00:1A:2B:3C:4D:5E")
    if fake == nil then
        return false
    end

    param:setResult(fake)
    return true, ret, fake
end