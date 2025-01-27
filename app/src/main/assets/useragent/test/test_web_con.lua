-- This file is part of XPrivacyLua.

-- XPrivacyLua is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.

-- XPrivacyLua is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.

-- You should have received a copy of the GNU General Public License
-- along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

-- Copyright 2017-2019 Marcel Bokhorst (M66B)

function after(h, param)
    local this = param:getThis()
    if this == nil then
        return false
    end

    log("[xweb] WebView Constructor")

    local ua = param:getSetting("user.agent")
    if ua == nil then
        return false
    end

    local hooked = param:getValue('hooked', this)
    if hooked then
        return false
    else
        param:putValue('hooked', true, this)
    end

    local settings = this:getSettings()
    if settings == nil then
        return false
    else
        --public static Globals getGlobals
        --DebugLib = 	void callHook(LuaThread.State s, LuaValue type, LuaValue arg)
        --local ua = 'Mozilla/5.0 (Linux; U; Android; en-us) AppleWebKit/999+ (KHTML, like Gecko) Safari/999.9'
        --hook is a Global var declared
        --(1) arg is the class
        --(2) arg is the method
        --(3) arg is the Function that will be invoked like a call back
        --(4) arg is the Value the fake arg to hand it
        --LuaHook.java is where it gets handed off to
        hook(settings, 'setUserAgentString', setUserAgentString, ua)
        hook()
        settings:setUserAgentString('dummy')
        return true
    end
end

function setUserAgentString(when, param, ua)
    if when == 'before' then
        log("[xweb] Setting UA=" .. ua)
        if param:getArgument(0) ~= ua then
            log('[xweb] Setting ua=' .. ua)
            param:setArgument(0, ua)
        end
    end
end




--getJavaScriptEnabled
--setJavaScriptEnabled
--setJavaScriptEnabled(boolean flag)
--setGeolocationEnabled(boolean flag)
--getSaveFormData()