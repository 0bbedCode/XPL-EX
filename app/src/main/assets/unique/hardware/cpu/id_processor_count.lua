function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

	local fake = param:getSettingInt("soc.cpu.processor.count", 99)
	if fake == nil then
	    return false
	end
	--local fake = 99
	--if setting ~= nil then
	--	local n = tonumber(setting)
	--	if n ~= nil then
	--		fake = n
	--	end
	--end

	param:setResult(fake)
	return true, tostring(ret), tostring(fake)
end