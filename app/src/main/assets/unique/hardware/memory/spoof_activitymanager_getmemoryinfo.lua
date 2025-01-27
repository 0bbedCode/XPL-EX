function after(hook, param)
    local m = param:getArgument(0)
    if m == nil then
        return false
    end

    local total = param:getSetting("hardware.memory.total")
    local avail = param:getSetting("hardware.memory.available")
    if total == nil or avail == nil then
        return false
    end

    local t = tonumber(total)
    local a = tonumber(avail)
    if t == nil or a == nil then
        return false
    end

    param:populateMemoryInfo(m, t, a)
	return true, "N/A", "total=" .. total .. " avail=" .. avail
end