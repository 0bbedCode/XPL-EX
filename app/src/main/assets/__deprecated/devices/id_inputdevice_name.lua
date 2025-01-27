function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local fake = param:generateRandomString(4, 8) .. "-" .. param:generateRandomString(4, 8)
    if string.match(res, "Headset Jack$") then
        fake = fake .. " Headset Jack"
    elseif string.match(res, "Button Jack$") then
        fake = fake .. " Button Jack"
    elseif string.match(res, "touchpanel") then
        fake = fake .. "-touchpanel"
    elseif string.match(res, "motor") then
        fake = fake .. "-motor"
    elseif string.match(res, "haptics$") then
        fake = param:generateRandomString(4, 8) .. "-haptics"
    end

    param:setResult(fake)
    return true, res, fake
end