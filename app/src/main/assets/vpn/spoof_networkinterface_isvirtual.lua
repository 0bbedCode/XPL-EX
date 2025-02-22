--VDInfo
function after(hook, param)
    local ret = param:getResult()

    if ret == nil or ret == false then
        return false
    end

    param:setResult(false)
    return true, 'true', 'false'
end
