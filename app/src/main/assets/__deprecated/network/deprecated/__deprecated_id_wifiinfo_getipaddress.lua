function after(hook, param)
	local ret = param:getResult()
	if ret == nil then 
		return false
	end

    local fake = param:getFakeIpAddressInt()
    param:setResult(fake)
    return true, tostring(ret), tostring(fake)
end