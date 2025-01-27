function before(hook, param)
    log("[xweb] WebView is loading Data")
    local pOne = param:getArgument(0)
    log("[xweb] WebView is loading Data " .. pOne)
    return false
end