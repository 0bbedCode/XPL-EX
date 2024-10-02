function after(hook, param)
	local res = param:getResult()
	if res == nil then
		param:setResult(null)
		return true
	end

	local lacString = param:getSetting("gsm.cell.location.lac")
	local cidString = param:getSetting("gsm.cell.location.cid")
	if lacString ~= nil and cidString ~= nil then
        local fakelac = tonumber(lacString)
        local fakecid = tonumber(cidString)
        if fakelac ~= nil and fakecid ~= nil then
            local fake = luajava.newInstance("android.telephony.gsm.GsmCellLocation")
            fake:setLacAndCid(fakelac, fakecid)
            log("Setting Fake LAC & CID::" .. fake:toString())
            param:setResult(fake)
            return true
        end
	else
		local setting = param:getSetting("LAC,CID")
    	if setting ~= nil then
    		local index = string.find(setting, ",", 1, true)
    		if index ~= nil then
    			local fakelac = tonumber(string.sub(setting, 1, index - 1))
    			local fakecid = tonumber(string.sub(setting, index + 1, -1))
    			if fakelac ~= nil and fakecid ~= nil then
    				local fake = luajava.newInstance("android.telephony.gsm.GsmCellLocation")
    				fake:setLacAndCid(fakelac, fakecid)
    				log("Setting Fake LAC & CID::" .. fake:toString())
    				param:setResult(fake)
    				return true
    			end
    		end
    	end
	end


	local cellinfo_cname = "android.telephony.CellLocation"
	if param:javaMethodExists(cellinfo_class, "getEmpty") then
		local cellinfo_class = luajava.bindClass(cellinfo_cname)
		log("Setting Empty CellLocation") 
		param:setResult(cellinfo_class:getEmpty())
		return true
	end

	param:setResult(null)
	return true
end