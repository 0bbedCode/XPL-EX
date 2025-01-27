function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local resAcc = res
    local resb1 = math.floor(resAcc / 256 / 256 / 256)
    resAcc = resAcc - resb1 * 256
    local resb2 = math.floor(resAcc / 256 / 256)
    resAcc = resAcc - resb2 * 256 * 256
    local resb3 = math.floor(resAcc / 256)
    local resb4 = math.floor(resAcc - resb3 * 256)

    -- Assuming luaJ's ints are little-endian, wich does not make sense
    -- but seems to be the case

    local fake, fakeStr, resultStr
    fake = 0x0100007F
    fakeStr = "127.0.0.1"
    resultStr = string.format("%d.%d.%d.%d", resb4, resb3, resb2, resb1)

    param:setResult(fake)
    return true, resultStr, fakeStr
end
