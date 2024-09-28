function before(hook, param)
    local fake = param:generateRandomInt(500, 99999);
    param:setResult(fake)
    return true, "Spoofed:", fake
end