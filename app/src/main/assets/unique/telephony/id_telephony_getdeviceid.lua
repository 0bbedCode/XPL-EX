function after(hook, param) 
	local ths = param:getThis()
	local ret = param:getResult()
	if ths == nil or ret == nil then
		return false
	end

	local typ = ths:getPhoneType()
	local fake

	if typ == 1 then 
		fake = param:getSettingReMap("unique.gsm.imei", "value.imei")
		log("GSM [IMEI]")
	elseif typ == 2 then
		fake = param:getSettingReMap("unique.gsm.meid", "value.meid")
		log("CDMA [MEID]")
	end

	if fake == nil then
		return false
	end

	--log("Setting Device ID [" .. ret .. "] => " .. fake)
	param:setResult(fake)
	return true, ret, fake
end