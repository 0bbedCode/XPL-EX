function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local fake = param:getSetting("unique.google.game.id", "FFP3")
    if fake == nil then
        return false
    end

    log("Google Game ID. old=" .. res .. " new=" .. fake)
    param:setResult(fake)
    return true, res, fake
end