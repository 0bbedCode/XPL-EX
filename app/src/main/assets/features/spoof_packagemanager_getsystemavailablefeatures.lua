function after(hook, param)
    param:printInetAddress()
    local res = param:getResult()
    if res == nil then
        return false
    end

    log("Spoofing System Features Array...");

    local refArrayClass = luajava.bindClass("java.lang.reflect.Array")
    local classClass = luajava.bindClass("java.lang.Class")
    local featureType = classClass:forName("android.content.pm.FeatureInfo")

    local fake = refArrayClass:newInstance(featureType, 1)
    fake[1] = res[1]
    param:setResult(fake)
    return true
end
