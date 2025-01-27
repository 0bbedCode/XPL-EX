function after(hook, param)
    local result = param:getResult()
    if result == nil then
        return false
    end

    log("Nulling out DHCP Info...")
    param:setResult(null)
    return true, "N/A", "NULL"
end