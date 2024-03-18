Defining hooks
==============

Introduction
------------

XPrivacyLua allows you do define Xposed hooks without developing an Xposed module.

Defined hooks can be added and updated at run time,
with the big advantage that no reboot is required to test a new or changed hook
(with the exception of persistent system apps, which are clearly marked in XPrivacyLua).
To apply an updated definition an app just needs to be stopped (force closed) and started again.
An easy way to do this is by toggling a definition off/on in XPrivacyLua.

XPrivacyLua allows you to select which apps a definition should be applied to.

You can edit hook definitions for free with the XPrivacyLua [pro companion app](https://github.com/0bbedCode/XPL-EX/releases).

Definition
----------

Hook definitions describe where to hook and what to do when the hook executes.

The *where to hook* is described as:

* Which [class](https://developer.android.com/reference/java/lang/Class.html)
* Which [method](https://developer.android.com/reference/java/lang/reflect/Method.html)

In the well documented [Android API](https://developer.android.com/reference/packages.html) you can find class and method names.
For more advanced hooks, see [here](https://github.com/rovo89/XposedBridge/wiki/Development-tutorial#exploring-your-target-and-finding-a-way-to-modify-it).

The *what to do* is described in the form of a [Lua](https://www.lua.org/pil/contents.html) script.

An exported definition in [JSON](https://en.wikipedia.org/wiki/JSON) format looks like this:

```JSON
{
  "collection": "Privacy",
  "group": "Read.Telephony",
  "name": "TelephonyManager\/getDeviceId",
  "author": "M66B",
  "description": "Let the telephony manager return NULL as device ID",
  "className": "android.telephony.TelephonyManager",
  "methodName": "getDeviceId",
  "parameterTypes": [],
  "returnType": "java.lang.String",
  "minSdk": 1,
  "maxSdk": 999,
  "enabled": true,
  "optional": false,
  "usage": true,
  "notify": false,
  "settings": [
	"setting_name1",
	"setting_name2"
  ],
  "luaScript": "function after(hook, param)\n    local result = param:getResult()\n    if result == nil then\n        return false\n    end\n\n    param:setResult(null)\n    return true\nend\n"
}
```

<br>

Note that you can conveniently edit hook definitions in the pro companion app, so there is no need to edit JSON files.

<br>

* The *collection* and *name* attributes are used to uniquely identify a hook
* For convenience XPrivacyLua applies hooks by *group*
* The attributes *minSdk* and *maxSdk* determine for which [Android versions](https://source.android.com/setup/build-numbers) (API level) the hook should be used
* Setting *enabled* to *false* will switch the hook off (default *true*)
* Setting *optional* to *true* will suppress error messages about the class or method not being found (default *false*)
* Setting *usage* to *false* means that executing the hook will not be reported (default *true*)
* Setting *notify* to *true* will result in showing notifications when the hook is applied (default *false*)
* Settings (custom values) can be set using the pro companion app and read using *param:getSetting('setting_name1')*

The pro companion app allows you to select which *collection*s of hooks XPrivacyLua should use.

See [here](https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/types.html#wp276) about Java type signatures (names).

The Lua script from the above definition without the JSON escapes looks like this:

```Lua
function after(hook, param)
	local result = param:getResult()
	if result == nil then
		return false
	end

	param:setResult(null)
	return true
end
```

There should be a *before* and/or an *after* function, which will be executed before/after the hooked method is/has been executed.
These functions always have exactly two parameters, in the example named *hook* and *param*.
The *hook* parameter can be used to get meta information about the hook.
The *param* parameter can be used to get or set the current arguments and the current result.
The current arguments are typically modified *before* the hooked method is executed.
The result can be set *before* the hooked method is executed,
which will result in the actual method not executing (thus replacing the method),
or can be set *after* the hooked method has been executed.

The most important functions of *hook* are:

* *hook:getName()*
* *hook:getClassName()*
* For other functions, see [here](https://github.com/0bbedCode/XPL-EX/blob/new/app/src/main/java/eu/faircode/xlua/api/hook/XLuaHookBase.java) for the available public methods

The most important functions of *param* are:

* *param:getApplicationContext()*: see remarks below
* *param:getThis()*: the current object instance or *nil* if the method is static
* *param:getArgument(index)*: get the argument at the specified index (zero based)
* *param:setArgument(index, value)*
* *param:getResult()*: only available after the hooked method has been executed
* *param:setResult(value)*
* For other functions, see [here](https://github.com/0bbedCode/XPL-EX/blob/new/app/src/main/java/eu/faircode/xlua/XParam.java) for the available public methods

The before/after function **must** return *true* when something was done and **must** return *false* otherwise.
XPrivacyLua will show the last date/time of the last time *true* was returned.

Special cases
-------------

You can hook into a constructor by omitting the method name.

You can modify field values in an *after* function by prefixing the method name with a # character, for example:

```JSON
  "methodName": "#SERIAL"
```

Note that *final static* fields (constants) might be optimized 'away' at compile time,
so setting such a field might not work because it doesn't exist at run time anymore.

Another special case is hooking a method of a field using the syntax *[field name]:[method name]*, for example:

```JSON
  "methodName": "CREATOR:createFromParcel"
```

Remarks
-------

A common problem when developing an Xposed module is getting [a context](https://developer.android.com/reference/android/content/Context.html).
With XPrivacyLua you'll never have to worry about this because you can simply get a context like this:

```Lua
	local context = param:getApplicationContext()
```

You can write to the Android logcat using the *log* function:

```Lua
	log('hello world')
	log(some_object) -- will call toString()
```

A log line starts with the word *Log* followed by the package name and uid of the app and the name of the hook
and ends with the log text. This way it is always clear which hook/app logging belongs to.

An error in the definition, like class or method not found, or a compile time or run time error of/in the Lua script will result in a status bar notification.
By tapping on the error notification you can navigate to the app settings where you can tap on the corresponding **!**-icon to see the details of the error.

[LuaJ](http://www.luaj.org/luaj/3.0/README.html) globals are not thread safe.
If you need global caching, you can use something like this:

```Lua
	local scope = param:getApplicationContext()
	param:putValue(name, value, scope)
	local value = param:getValue(name, scope)
```

Using the pro companion app you can edit built-in definitions, which will result in making a copy of the definition.
You could for example enable usage notifications or change returned fake values.
Deleting copied definitions will restore the built-in definitions.

The pro companion app can upload defintions to and download definitions from a [hook definition repository](https://github.com/0bbedCode/XPL-EX/tree/new/repo),
making it easy to share hook definitions with others and to use hook definitions provided by others.
Note that you cannot upload hook definitions with the author name to set to someone else.

You can find some example definitions [here](https://github.com/0bbedCode/XPL-EX/tree/new/examples)
and the definitions built into XPrivacyLua [here](https://github.com/0bbedCode/XPL-EX/tree/new/app/src/main/assets).

If you have questions, you can ask them in [this XDA thread](https://forum.xda-developers.com/xposed/modules/xposed-developing-module-t3741692).
