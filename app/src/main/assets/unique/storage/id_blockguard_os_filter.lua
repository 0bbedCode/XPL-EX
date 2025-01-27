-- Fif.BlockGuardOs.open is a Lua hook definition designed to work
-- with XPrivacyLua.

-- Fif.BlockGuardOs.open is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.

-- Fif.BlockGuardOs.open is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.

-- You should have received a copy of the GNU General Public License
-- along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

-- Copyright 2017-2018 Marcel Bokhorst (M66B)
-- Copyright (C) 2018-2021 Philippe Troin (Fif_ on XDA)
function before(hook, param)
    local context = param:getApplicationContext()
    local WhitelistPrefixes = param:getValue('OBC.BlockGuardOs.open.WhitelistPrefixes', context)

    if WhitelistPrefixes == nil then
        local ai = context:getApplicationInfo()
        local clsOsBuild = luajava.bindClass('android.os.Build')
        local clsFile = luajava.bindClass('java.io.File')
        local clsEnvironment = luajava.bindClass('android.os.Environment')
        local packageName = context:getPackageName()

        WhitelistPrefixes = {
            '/data/data/' .. packageName .. '/',
            ai.dataDir .. '/',
            '/vendor/lib',
            '/vendor/lib64',
            '/vendor/bin',
            '/system/product/framework',
            '/system/product/bin',
            '/system/product/lib',
            '/system/product/lib64',
            '/product/framework',
            '/product/bin',
            '/product/lib',
            '/product/lib64',
            '/system/vendor/lib64',
            '/system/vendor/lib',
            '/system/vendor/bin',
            '/system/vendor/bin/hw',
        --    '/system',
        --    '/vendor/',
        --    '/product/',
        --    '/data/misc/',
        --    '/etc/textclassifier/',
        --    '/etc/timezone',
        --    '/dev/',
        --    luajava.new(clsFile, "/proc/self"):getCanonicalPath() .. '/',
            '/proc/meminfo',
        --    '/proc/self/',
        --    '/proc/vmstat',
        --    '/proc/zoneinfo',
        }

        if param:getSettingBool("storage.allow.proc.self.bool", false) then
            table.insert(WhitelistPrefixes, luajava.new(clsFile, "/proc/self"):getCanonicalPath() .. '/')
            table.insert(WhitelistPrefixes, '/proc/self/')
        end

        if param:getSettingBool("storage.allow.data.misc.bool", false) then
            table.insert(WhitelistPrefixes, '/data/misc/')
        end

        if param:getSettingBool("storage.allow.devices.bool", false) then
            table.insert(WhitelistPrefixes, '/dev/')
        end

        if param:getSettingBool("storage.allow.security.cacerts.bool", false) then
            table.insert(WhitelistPrefixes, '/system/etc/security/cacerts/')
        end

        if param:getSettingBool("storage.allow.textclassifier.bool", false) then
            table.insert(WhitelistPrefixes, '/etc/textclassifier/')
        end

        if param:getSettingBool("storage.allow.etc.timezone.bool", false) then
            table.insert(WhitelistPrefixes, '/etc/timezone')
        end

        if param:getSettingBool("storage.allow.proc.vmstat.bool", false) then
            table.insert(WhitelistPrefixes, '/proc/vmstat')
        end

        if param:getSettingBool("storage.allow.proc.zoneinfo.bool", false) then
            table.insert(WhitelistPrefixes, '/proc/zoneinfo')
        end

        if (clsOsBuild.VERSION.SDK_INT >= 30) then
            -- No need to list all allowed source dir prefixes in Android 11 (SDK 30), they're made hard to guess with random prefixes
            table.insert(WhitelistPrefixes, '/data/app/')
        else
            -- Android 10 and below use a list of allowable apps, mostly Google components
            local pkgSourceDir = luajava.new(clsFile, ai.sourceDir):getParent()
            local allSourceDir = luajava.new(clsFile, pkgSourceDir):getParent()
            table.insert(WhitelistPrefixes, pkgSourceDir .. '/')
            table.insert(WhitelistPrefixes, allSourceDir .. '/com.google.android.gms-')
            table.insert(WhitelistPrefixes, allSourceDir .. '/com.google.android.webview-')
            table.insert(WhitelistPrefixes, allSourceDir .. '/com.android.webview-')
            table.insert(WhitelistPrefixes, allSourceDir .. '/com.android.chrome-')
            table.insert(WhitelistPrefixes, allSourceDir .. '/com.google.ar.core-')
        end

        if ai.deviceProtectedDataDir ~= nil then
            table.insert(WhitelistPrefixes, ai.deviceProtectedDataDir .. '/')
            table.insert(WhitelistPrefixes, luajava.new(clsFile, ai.deviceProtectedDataDir):getParent() .. '/com.google.android.gms/')
        end

        local clsArray = luajava.bindClass('java.lang.reflect.Array')
        local pathJarray = clsEnvironment:buildExternalStorageAppFilesDirs(packageName)
        for i = 0, pathJarray.length - 1 do
            table.insert(WhitelistPrefixes, clsArray:get(pathJarray, i):getParent() .. '/')
        end

        pathJarray = clsEnvironment:buildExternalStorageAppMediaDirs(packageName)
        for i = 0, pathJarray.length - 1 do
            table.insert(WhitelistPrefixes, clsArray:get(pathJarray, i):getAbsolutePath() .. '/')
        end

        pathJarray = clsEnvironment:buildExternalStorageAppObbDirs(packageName)
        for i = 0, pathJarray.length - 1 do
            table.insert(WhitelistPrefixes, clsArray:get(pathJarray, i):getAbsolutePath() .. '/')
        end

        param:putValue('OBC.BlockGuardOs.open.WhitelistPrefixes', WhitelistPrefixes, context)
        log('WhitelistPrefixes: ' .. table.concat(WhitelistPrefixes, ', '))
    end

    local found = false
    local path = param:getArgument(0)
    if path == nil then
        found = true
    else
        for _, prefix in ipairs(WhitelistPrefixes) do
            if string.sub(path, 1, #prefix) == prefix then
                found = true
                break
            end
        end
    end

    if found then
        log('Allow ' .. path)
        return false
    else
        log('Deny ' .. path)
        local clsFileNotFound = luajava.bindClass('java.io.FileNotFoundException')
        local fake = luajava.new(clsFileNotFound, path)
        param:setResult(fake)
        return true, path
    end
end