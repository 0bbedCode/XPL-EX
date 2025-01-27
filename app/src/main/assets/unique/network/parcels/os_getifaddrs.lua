function after(hook, param)
    local res = param:interceptGetifaddrs()
    return res
end