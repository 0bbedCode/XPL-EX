function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

	local fake = param:getSetting("unique.gsm.subscription.id", "93939251723998531487")
	param:setResult(fake)
	return true, ret, fake
end