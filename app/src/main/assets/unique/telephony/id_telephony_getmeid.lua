function after(hook, param)
	--local ths = param:getThis()
	local ret = param:getResult()
	if ret == nil then
	    return false
	end
	--if ths == nil or ret == nil then
	--	return false
	--end

	--local typ = ths:getPhoneType()
	--local fake = "000"

	--if typ == 2 then
	--	fake = param:getSettingReMap("unique.gsm.meid", "value.meid", "000")
	--	log("CDMA [MEID]")
	--end

	--if fake == nil then
	--	fake = "000"
	--end
	local fake = param:getSettingReMap("unique.gsm.meid", "value.meid", "000")
	if fake == nil then
	    return false
	end
	--log("Setting MEID [" .. res .. "] => " .. fake)
	param:setResult(fake)
	return true, ret, fake
end