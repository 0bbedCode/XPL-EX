function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end

    local sz = param:getContainerSize(res)
    if sz == 0 then
        log("Available Wifi Networks Returned 0....")
        return false
    end

    log("Available Wifi List Size=" .. sz)
    --filterDevices(Set<BluetoothDevice> devices, List<String> allowList)
    local setting = param:getSetting("network.allowed.list", "*")
    if setting == nil then
        log("Available Wifi Allow list is NULL...")
        return false
    end

    log("Allowed Wifi Networks=" .. setting)
    local allowed = param:stringToList(setting, ",")

    local filtered = param:filterWifiScanResults(res, allowed)
    log("Done filtering Available Wifi Networks")
    local newSz = param:getContainerSize(filtered)
    log("Setting Filtered Available Wifi Network list size=" .. newSz)
    param:setResult(filtered)
    return true
end