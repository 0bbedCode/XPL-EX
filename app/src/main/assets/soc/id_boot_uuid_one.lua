--createFakeUUIDFileDescriptor
function before(hook, param)
    --Hook version one a BEFORE Hook
    --We can replace the param to point to the TEMP File
    --this we attack the lowest in this case libcore.io.IoBridge

    local f = param:getArgument(0)
    if f == nil then
        return false
    end
    if string.match(f, "boot_id") then
        local m = param:getArgument(1)
        log("App is trying to Open boot_id File ... Spoofing....")
        local fake = param:createFakeUUIDFile()
        if fake == nil then
            log("Error NIL File Object")
            return false
        end

        local path = fake:getPath()
        if path == nil then
            log("Error NIL Path Object")
            return false
        end

        log("Setting Parameter to Point to Path: " .. path)
        param:setArgumentString(0, path)
        return true
    end
    return false
end