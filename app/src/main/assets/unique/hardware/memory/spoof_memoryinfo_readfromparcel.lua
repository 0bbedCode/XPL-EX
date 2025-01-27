function after(hook, param)
	local ts = param:getThis()
	local a = "9000000000"
	local m = "90000000000"
	local t = "1000000"
	ts.advertisedMem = param:toLongFromStr(a)
	ts.availMem = param:toLongFromStr(a)
	ts.threshold = param:toLongFromStr(t)
	ts.lowMemory = false
	ts.hiddenAppThreshold = param:toLongFromStr(t)
	ts.secondaryServerThreshold = param:toLongFromStr(t)
	ts.visibleAppThreshold = param:toLongFromStr(t)
	ts.foregroundAppThreshold = param:toLongFromStr(t)
	--log("[MemoryInfo].avilMem=" .. a .. " => [max] => " .. m .. " => [thr] => " .. t)
	--local mem = param:getFakeMemoryInfo(a, m, t, false)
	--param:setResult(mem)
	return true, "N/A", "avail=" .. a .. " max=" .. m
end