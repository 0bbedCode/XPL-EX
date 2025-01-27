function after(hook, param)
	--local ths = param:getThis()
	local ret = param:getResult()
	if ret == nil then
	    return false
	end

	local fake = param:getSettingReMap("unique.gsm.imei", "value.imei", "000")
	if fake == nil then
	    return false
	end

	param:setResult(fake)
	return true, ret, fake
end