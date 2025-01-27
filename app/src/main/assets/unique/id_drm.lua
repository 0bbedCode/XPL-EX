function after(hook, param)
	local arg = param:getArgument(0)
	if arg == nil then
	    return false
	end

	if arg ~= "deviceUniqueId" then
	    log("MEDIA DRM Not unique ID Flag .. " .. arg)
	    return false
	end

    log("MEDIA DRM Hook being Invoked Flag = " .. arg)
    local ret = param:getResult()
    local fakeString = "0000000000000000000000000000000000000000000000000000000000000000"
    if ret == nil or ret.length == 0 then
        log("MEDIA DRM ID Appears to be null spoofing...")
        ret = param:stringToRawBytes(fakeString)
    end

    local drm256 = param:bytesToSHA256Hash(ret)
    local drmHex = param:rawBytesToHexString(ret)
    log("MEDIA DRM ID Return result (SHA256) was=" .. drm256 .. " DRM ID Return result (HEX STRING) was=" .. drmHex)
    local drmFake = param:getSetting("unique.drm.id", fakeString)
    if drmFake == nil then
        return false
    end

    if drmFake == nil then
        -- check length ensure its 32 ? string of length 32 (since we are working with 16 bytes)
        log("DRM Error Setting value is NULL make sure its 32 char alpha numeric String. setting to: " .. fakeString)
        drmFake = fakeString
    end

    local fakeBys = param:stringToRawBytes(drmFake)
    local fake256 = param:bytesToSHA256Hash(fakeBys)
    local fakeHex = param:rawBytesToHexString(fakeBys)
    log("MEDIA DRM fake=" .. drmFake .. " fake (SHA256)=" .. fake256 .. " fake (HEX STRING)=" .. fakeHex)
    param:setResultBytes(fakeBys)
    return true, drm256, fake256
end