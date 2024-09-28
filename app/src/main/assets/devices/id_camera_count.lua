function after(hook, param)
    local res = param:getResult()
    local fak = param:getSetting("hardware.camera.count", "10")
    if fak ~= nil then
        local num = tonumber(fak)
        if num ~= nil then
            param:setResult(num)
            return true, res, fak
        end
    end
    return false
end