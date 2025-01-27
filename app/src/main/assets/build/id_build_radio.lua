function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local fake = param:getSetting("soc.baseband.board.radio.version", "M8994F-2.6.36.2.20")
    if fake == nil then
        return false
    end
    param:setResult(fake)
    return true, ret, fake
end