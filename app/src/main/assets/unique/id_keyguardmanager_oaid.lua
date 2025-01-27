function after(hook, param)
   local res = param:getResult()
   if res ~= nil then
        local fake = param:getSetting("unique.open.anon.advertising.id", "a9f9a2f8-4bac-4a3f-be5a-07c6d32d6682")
        if fake ~= nil then
            param:setResult(fake)
            return true, res, fake
        end
   end
    return false
end