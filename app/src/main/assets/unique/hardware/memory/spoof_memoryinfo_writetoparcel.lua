function before(hook, param)
    local dp = param:getArgument(0)
    local ts = param:getThis()

    if dp == nil or ts == nil then
        return false
    end

    local total = param:getSetting("hardware.memory.total")
    local avail = param:getSetting("hardware.memory.available")
    if total == nil or avail == nil then
        return false
    end

    local t = tonumber(total)
    local a = tonumber(avail)
    if t == nil or a == nil then
        return false
    end

    log("Spoofing Memory => [TOTAL]:[" .. total .. "] [AVAIL]:[" .. avail .. "]")
    local fake_mem = param:getFakeMemoryInfo(t, a)

    log("Spoofing MemoryInfo.writeToParcel")
    local code = param:getSDKCode()
    log("Android SDK:" .. code)

    if code >= 34 then
        --dp:writeLong(fake_mem.advertisedMem) -- maybe +100 ?
        param:parcelWriteLong(dp, param:getFieldLong(fake_mem, "advertisedMem"))
    end

    --dp:writeLong(fake_mem.availMem)
    --dp:writeLong(fake_mem.totalMem)
    --dp:writeLong(fake_mem.threshold)
    param:parcelWriteLong(dp, param:getFieldLong(fake_mem, "availMem"))
    param:parcelWriteLong(dp, param:getFieldLong(fake_mem, "totalMem"))
    param:parcelWriteLong(dp, param:getFieldLong(fake_mem, "threshold"))

    if ts.lowMemory == true then
        dp:writeInt(1)
    else
        dp:writeInt(0)
    end

    --this is writing so in theory we can just take original values
    --this.fieldName we can do but we need to reintroduce the var for this

    param:parcelWriteLong(dp, "100000")     --hiddenAppThreshold
    param:parcelWriteLong(dp, "100000")     --secondaryServerThreshold
    param:parcelWriteLong(dp, "100000")     --visibleAppThreshold
    param:parcelWriteLong(dp, "100000")     --foregroundAppThreshold
    --dp:writeLong(1000)              --hiddenAppThreshold
    --dp:writeLong(1000)              --secondaryServerThreshold
    --dp:writeLong(1000)              --visibleAppThreshold
    --dp:writeLong(1000)              --foregroundAppThreshold
    return true
end

