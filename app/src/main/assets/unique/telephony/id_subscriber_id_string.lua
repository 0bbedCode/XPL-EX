function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

	local setting = param:getSettingReMap("unique.gsm.subscription.id", "phone.subscriberid")
	if setting == nil then
	    return false
	end

	if param:isNumericString(setting) then
        param:setResult(setting)
        return true, tostring(ret), setting
	end

	local mcc = param:getSettingReMap("gsm.operator.mcc", "phone.mcc", "274")
	local mnc = param:getSetting("gsm.operator.mnc", "phone.mnc", "299")
	--next line is 9 chars MSIN
	local msin = param:getSettingReMap("unique.gsm.operator.msin", "phone.msin", "842762952")
    if mcc == nil or mnc == nil or msin == nil then
        return false
    end


	--Returns the unique subscriber ID, for example, the IMSI for a GSM phone.
	if param:isNumericString(mcc) and param:isNumericString(mnc) and param:isNumericString(msin) then
	    local fake = mcc .. mnc .. msin
	    param:setResult(fake)
	    return true, ret, fake
	end

	return false
end