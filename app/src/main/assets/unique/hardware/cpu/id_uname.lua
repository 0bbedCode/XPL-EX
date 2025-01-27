function after(hook, param)
	local uname = "android.system.StructUtsname"
	local s = param:getSetting("android.kernel.sys.name", "Unix")
	local n = param:getSetting("android.kernel.node.name", "localhost")
	local r = param:getSetting("android.kernel.release", "Kool-Kernel-RS3899238")
	local v = param:getSetting("android.kernel.version", "#1 SMP PREEMP Wen Jan 21 11:28:12 AEST 1970")
	local m = param:getSetting("soc.cpu.architecture", "x86_64")

	if s == nil or n == nil or r == nil or v == nil or m == nil then
	    return false
	end

	local fake = luajava.newInstance(uname, s, n, r, v, m)
	log("[sysname]: " .. s .. "\n[nodename]: " .. n .. "\n[release]: " .. r .. "\n[version]: " .. v .. "\n[machine]: " .. m)
	param:setResult(fake)

	return true
end