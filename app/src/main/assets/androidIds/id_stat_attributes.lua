function after(hook, param)
    local newRes = param:cleanStructStat()
    param:setResult(newRes)
    return true
end