function after(hook, param)
	local fil = param:getArgument(0)
	if fil == nil then 
		return false
	end

	log("Libcore.io.access has been Invoked, Filtering: " .. fil)
	if param:isDriverFile(fil) then 
		log("Libcore.io [access] Driver Directory!")
		param:setResult(false)
		return true
	end

	return false
end