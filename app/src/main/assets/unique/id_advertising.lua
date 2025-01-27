function after(hook, param)
	local ret = param:getResult()
	if ret == nil then 
		return false
	end

    --local fake = param:getSetting("google.advertisingid")
    local fake = param:getSettingReMap("unique.google.advertising.id", "ad.id")
    if fake == nil then
        return false
    end
    if fake == nil then 
    	local i, digit
    	fake = ""
    	for i = 1, 36 do 
    		if i == 9 or i == 14 or i == 19 or i == 24 then
    			fake = fake .. "-"
    		else
    			digit = math.random(0, 15)
    			if digit < 10 then
    				fake = fake .. string.char(string.byte('0') + digit) 
    			else
    				fake = fake .. string.char(string.byte('a') + digit - 10)
    			end
    		end
    	end
    end

    param:setResult(fake)
	return true, ret, fake
end