function after(hook, param)
    local res = param:getResult()
    if res ~= nil then
        local fake = param:randomUUID()
        param:setResult(fake)
        return true, res, fake
    end
    return false
end



function before(hook, param)
    local str = param:getArgument(0)
    local two = param:getArgument(0)
    if str ~= nil and two ~= nil then
        log("[TEEMMUU MAP PUT] a=" .. str .. " b=" .. two)
    end

    return false
end