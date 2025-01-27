function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local sz = param:getContainerSize(res)
    if sz == 0 then
        log("Saved Wifi Networks Returned 0....")
        return false
    end

    log("Saved Wifi List Size=" .. sz)
    --filterDevices(Set<BluetoothDevice> devices, List<String> allowList)
    local setting = param:getSetting("network.allowed.list", "*")
    if setting == nil then
        log("Saved Wifi Allow list is NULL...")
        return false
    end

    log("Saved Wifi Networks=" .. setting)
    local allowed = param:stringToList(setting, ",")

    local filtered = param:filterSavedWifiNetworks(res, allowed)
    log("Done filtering Saved Wifi Networks")
    local newSz = param:getContainerSize(filtered)
    log("Setting Filtered Saved Wifi Network list size=" .. newSz)
    param:setResult(filtered)
    return true
end