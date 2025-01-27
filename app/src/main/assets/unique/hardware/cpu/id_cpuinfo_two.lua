function after(hook, param)
    --this will RETURN a fake File Descriptor
    --becareful with this as setFdOwner acts odd with this behaviour
    --Attempt to take ownership of an already owned file descriptor will be produced :()
    local f = param:getArgument(0)
    if f == nil then
        return false
    end

    if string.match(f, "cpuinfo$") then
        local m = param:getArgument(1)
        if m ~= nil then
            log("CPUINFO FLAGS::" .. tostring(m))
        end

        log("App is trying to Open CPUINFO File Descriptor ... Spoofing....")
        local fake = param:createFakeCpuinfoFileDescriptor()
        if fake == nil then
            log("File Descriptor is NIL Error")
            return false
        end

        --Have set for access flags as well to match them
        param:setResult(fake)
        return true
    end

    return false

end