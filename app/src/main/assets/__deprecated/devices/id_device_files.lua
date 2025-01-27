function before(hook, param)
	local fil = param:getArgument(0)
	if fil == nil then
		return false
	end

	log("Libcore.io.open has been Invoked, Filtering: " .. fil)
	if param:isDriverFile(fil) then
		log("Libcore.io is [open] Driver Directory!")
		local clsFileNotFound = luajava.bindClass("java.io.FileNotFoundException")
		local fake = luajava.new(clsFileNotFound, fil)
		param:setResult(fake)
		return true, fil
	end

	return false
end