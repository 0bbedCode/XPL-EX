function before(hook, param)
    param:setResult(param:generateIntArray())
    return true, "Spoofed", "Input IDs"
end