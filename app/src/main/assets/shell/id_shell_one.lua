function after(hook, param)
    --log("Runtime.exec(command) Hook Invoked")
	--local arg = param:getArgument(0)
	--if arg == nil then
	    --log("Runtime.exec(command) input argument is NULL...")
	--	return false
	--end

    --log("Runtime.exec(" .. arg .. ")")
	--local comRes = param:interceptCommand(arg)
	--if comRes == nil then
        --log("Command Interception returned NULL. Command is Clean! arg=" .. arg)
	--	return false
	--end

    --log("Command was intercepted=" .. arg .. " !!")
	--return true, arg, comRes
	local sh = param:createShellContext(false)
	if sh == nil then
        log("Create Shell Context is NIL")
        param:isNullError(hook)
        return false
    end

	local rt = param:isCommandBad(sh)
    if rt == nil then
        log("Command Bad Return is NIL")
        param:isNullError(hook)
        return false
    end

	if rt == true then
        return true, param:getOldResult(), param:getNewResult(), param:getSettingResult()
    end
	return false
end