function before(hook, param)
    local res = param:packageInfoInstallTimeSpoof(false)
    return res
end