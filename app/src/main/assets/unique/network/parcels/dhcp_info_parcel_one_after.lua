function after(hook, param)
    local res = param:interceptDhcpInfo(false)
    if res ~= nil and res == true then
        return true, param:getOldResult(), param:getNewResult()
    end
    return false
end