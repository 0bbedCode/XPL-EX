function after(hook, param)
    local res = param:cleanStructStat()
    if res ~= nil and res == true then
        return true, param:getOldResult(), param:getNewResult()
    end
    return false
end