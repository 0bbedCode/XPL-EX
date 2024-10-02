XLUA API
==============


Filtering APIs
------------
Alot of these functions will never need to be explicitly used as most of the filtering is handled in XPLEX provided via Hooks

```Java


//Filters out a Property in replacement of a spoofed one
//You can Define the Spoof Value within the App UI
String filterBuildProperty(String property)

//Filters Params from Settings$Secure the Setting from there Second Param will be the Value to Replace it with if found
//Returns True if the Fake Value was Set, From the Lua Script just return True else False
boolean filterSettingsSecure(String setting, String newValue)

//Filters ".query" Commands from "ContentResolver" First Param will be the Package and or Service Name, Second Param will be the Column to Find and Modify, and the Third Param will be the Value to Modify as
boolean queryFilterAfter(String serviceName, String columnName, String newValue)

//Filter IBinder / BinderProxy
//This is to help Filer (IPC) Inner Process Communication though it currently only supports one kind of Filter "adid" or advertising id
//This will now set the "transact" result to "true" assuming this is coming from that Binder Function. There for you will have to set the value to "true" if it was modified or filtered
//If the funciton returns a valid string value, then that indicates it was modified else if null or nil then it was not modified and or filtered.
boolean filterBinderProxyAfter(String filterKind)

//Filinter out Properties such as "ro.build" what not can be done using 'filterBuildProperty'
//This will take your configured settings and check if a setting representing the property is set there for can be replaced
//If a Property setting was applied versus a setting value representing the property, it will then act on the behaviour of that property setting.
//Property Settings are as follow (PROP_HIDE) will hide that Target Property, (PROP_SKIP) Will in a way ignore the property / skip it, (PROP_FORCE) Will display the Value from the represented settting even if the property does not exist.
//If this returns the constant "NOT_BLACKLISTED" that is "NotBlacklisted" then it tells the caller of this function that the property is not listed as a property to intercept
String filterBuildProperty(String property)

//To Filter out Shell Command or Command executed via "exec" what not you can use the following functions
//Using the following functions you either pass in a Command, Array of Commands and or a List of Commands, and if a new value returns then that idicates the command was intercepted else null / nil
//This function will set the result of the targeted hook function if the return type of the function is not "android.os.Process" then it will not set it but instead return the intercepted value
//You then can do what you please with the new Mock Value, in the general case it will create a "echo" command with the fake data then set the result as that
public String interceptCommand(String command)
public String interceptCommandArray(String[] commands)
public String interceptCommandList(List<String> commands)

```

```LUA

	local propertyParam = param:getArgument(0)
	local filtered = param:filterBuildProperty(propertyParam)

	if filtered == false then
		log("Property Intercepted & Filtered")
		return true
	end

	log("Property is alight to Pass")
	return false

```

```LUA

	local filtered = param:filterSettingsSecure("android_id", param:getSettingReMap("unique.android.id", "value.android_id", "0000000000000000"))
	if filtered == true then
		log("Secure Setting was Intercepted & Filtered")
		return true
	end

	log("Secure Setting is clean good to go")
	return false

```

```LUA

	local filtered = param:queryFilterAfter("com.google.android.gsf.gservices", "android_id", param:getSetting("unique.gsf.id"))

	if filtered == true then
		log("Query Intercepted & Filtered")
		return true
	end

	log("Query is alight to Pass")
	return false
```

```LUA
	local result = param:filterBinderProxyAfter("adid")
	if result ~= nil then
		log("Advertising ID was filtered! New value:" .. result)
		param:setResult(true)
		return true, "N/A", result
	end
	
	log("Binder Transact Looks clean!")
	return false
```

```LUA
	local propName = "ro.build" --or argument of hooked function
	local filtered = param:filterBuildProperty("ro.build")
	if filtered == 'NotBlacklisted' then
		log("Property is not black listed:" .. propName)
		return false
	end
	log("Property:" .. propName .. " was intercepted with now value of:" .. filtered)
	param:setResult(filtered)
	return true, propName, filtered
```

Evidence APIs
------------
These apis will determine if an File and or Package is Considered evidence of either Emulator Trace or Root Trace
When using said function you pass in the following codes to determine the type of evidence you want to look for

```JAVA
int FILTER_EMULATOR = 0x1; 	//(1) Check for Emulator Traces
int FILTER_ROOT = 0x2;		//(2) Check for Root Traces
int FILTER_EMULATOR_ROOT = 0x3;	//(3) Check for Emulator AND Root Traces

//Create a File Array with all the Evidence it found removed
File[] fileArrayHasEvidence(File[] files, int code)

//Create a File List with the Evidence it found removed
List<File> fileListHasEvidence(List<File> files, int code)

//Create a String Array with the Evidence it found removed
String[] stringArrayHasEvidence(String[] file, int code)

//Clean Stack Trace from any Evidence of Hooking / Hooked Functions
StackTraceElement[] stackHasEvidence(StackTraceElement[] elements)
```

```LUA
	--In theory you would get these from a Result of a Function or Paramter
	local files = { "/su" , "/system/xbin/su", "/sdcard/someCool/file.txt", "/system/wow/k", "/system/bin/failsafe/su" }
 	--Filter for any Root (2) Traces now the result will just be { "/sdcard/someCool/file.txt", "/system/wow/k" }
	local cleanedFiles = param:stringArrayHasEvidence(files, 2)
	param:setResult(cleanedFiles)
```

```JAVA
//Is Package Part of Evidence (Root Related package or Emulator)
boolean packageNameHasEvidence(String packageName, int code)

//Is File a Root or Emulator Related file
boolean fileIsEvidence(String file, int code)

//Is File a Root or Emulator Related file
boolean fileIsEvidence(File file,  int code)

//Is File a Root or Emulator Related file, fileFull is the full file path including name and or you can just specify file Name with the second param leave the firt null
boolean fileIsEvidence(String fileFull, String fileName, int code)
```

```LUA
	local fileClass = luajava.bindClass("java.io.File")
	local fileFull = "/system/xbin/su"
	local fileName = "su"
	local fileObj = fileClass.newInstance(fileClass, fileFull)

	local isEvOne = param:fileIsEvidence(fileFull, 2)
	local isEvTwo = param:fileIsEvidence(fileObj, 2)
	local isEvThe = param:fileIsEvidence(null, fileName, 2)	    --Do Strict checks on just the File Name
	local isEvFor = param:fileIsEvidence(fileFull, fileName, 2) --Do Strict checks on both the Full Path with Name and just the Name

	--All Shall equal to be (true) since SU and "/system/xbin/su" is one of the Directories that is flagged for evidence
	log("Is evidence (1)=" .. tostring(isEvOne) .. " (2)=" .. tostring(isEvTwo) .. " (3)=" .. tostring(isEvThe) .. " (4)=" .. tostring(isEvFor))

	local pkgOne = "com.some.cool.app"
	local pkgTwo = "io.github.vvb2060.magisk"
	local pkgThe = "com.bignox.appcenter"

	--Check if its a Root or Emulator Package (3)
	local isEvOne = param:packageNameHasEvidence(pkgOne, 3)	--False, Code (3) being Root and Emulator Packages and "com.some.cool.app" is neither a Root or Emulator Package
	--Check if its a Emulator Package (1)
	local isEvTwo = param:packageNameHasEvidence(pkgTwo, 1)	--False, Code (1) being Emulator Packages, its not a emulator package but a root package
	--Check if its a Root Package (2)	
	local isEvThe = param:packageNameHasEvidence(pkgTwo, 2) --True, Code (2) being Root Packages and Magisk is a Root package
	--Check if its a Root or Emulator Package (3)
	local isEvFor = param:packageNameHasEvidence(pkgThe, 3) --True, Code (3) being Root and Emulator and "com.bignox.appcenter" is a Emulator Package

```


Memory/CPU
------------


```Java
//Returns a Fake /proc/meminfo File as 'java.io.File'
File createFakeMeminfoFile(int totalGigabytes, int availableGigabytes)

//Returns a 'java.io.FileDescriptor' Pointing to the Fake /proc/meminfo File
FileDescriptor createFakeMeminfoFileDescriptor(int totalGigabytes, int availableGigabytes)

//Sets the Fields for Java 'MemoryInfo' Structure Holding Memory Information
void populateMemoryInfo(ActivityManager.MemoryInfo memoryInfo, int totalMemoryInGB, int availableMemoryInGB)

//Instead of Populating, Return a 'MemoryInfo' Structure with preset fake values passed from params
ActivityManager.MemoryInfo getFakeMemoryInfo(int totalMemoryInGB, int availableMemoryInGB)

//Returns a Fake /proc/cpuinfo File as 'java.io.File'
//Specific CPU Map can be set in the UI
//If nothing is set, it will Randomize through the 43 Maps
File createFakeCpuinfoFile()

//Returns a Fake /proc/cpuinfo File as 'java.io.FileDescriptor'
//Specific CPU Map can be set in the UI
//If nothing is set, it will Randomize through the 43 Maps
FileDescriptor createFakeCpuinfoFileDescriptor()

```

```LUA
	-- 90GB / 50GB RAM/Memory
	local fakeMemFL = param:createFakeMeminfoFile(99, 50)
	local fakeMemFD = param:createFakeMeminfoFileDescriptor(99, 50)

	-- Populating the MemoryInfo Struct passed through
	local memoryInfoInstance = param:getArgument(0)
	param:populateMemoryInfo(memoryInfoInstance, 99, 50)

	local fakeMemoryInfo = param:getFakeMemoryInfo(99, 50)

	local fakeCpuFL = param:createFakeCpuinfoFile()
	local fakeCpuFD = param:createFakeCpuinfoFileDescriptor()
```


Network
------------

```Java

//Convert Ip String to Bytes
//Needs 4 decimals like "127.0.0.1" keep it numeric
byte[] getIpAddressBytes(String ipAddress)

//Generates a Random Fake IpAddress Returns the Bytes
//A preset value can be defined Via LUAPRO UI as a setting 'net.host_address'
byte[] getFakeIpAddressBytes()

//Generates a Random Fake IpAddress Returns the value but as a int
//A preset value can be defined Via LUAPRO UI as a setting 'net.host_address'
int getFakeIpAddressInt()

//Generates a Random Fake MAC Address Returns the bytes of it
//A preset value can be defined Via LUAPRO UI as a setting 'net.mac'
byte[] getFakeMacAddressBytes() 

```

```LUA

	local fakeIpBytes = param:getIpAddressBytes("127.0.0.1")
	local fakeIpBytes2 = param:getFakeIpAddressBytes()

	local fakeIpInt = param:getFakeIpAddressInt()

	local fakeMacBytes = param:getFakeMacAddressBytes()

```

Utils
------------

```Java

//Gets the Context / 'android.content.Context'
Context getApplicationContext()

//Get the Name of the Package Hook is in
String getPackageName()

//Get the UID Group of the Package / Hook
int getUid()

//Get the Xposed Scope Param
Object getScope()

//Gets the 'this' Instance of the Hooked Function as if you are the Function
Object getThis()

//Get the Runtime SDK of the App
int getSDKCode()

//Prints the Contents in a File onto the CatLog Line by Line
void printFileContents(String filePath)

//Prints a Stack Trace
void printStack()

//Gets a Stack Trace in String Format
String getStackTraceString()

//Gets a Array of StackTraceElement
StackTraceElement[] getStackTrace()

//Determine if a String is a String made up of only Numbers
boolean isNumericString(String s)

//Get the size of an Array, List, Set, Collection, Map etc...
int getContainerSize(Object o)

//Join String Array Elements into one String using a space as the delimiter
String joinArray(String[] array)

//Join String Array of Elements into one  String using argument as the delimiter
String joinArray(String[] array, String delimiter)

//Join String List Elements into one String using space as the delimiter
String joinList(List<String> list)

//Join String List Elements into one String using argument as the delimiter
String joinList(List<String> list, String delimiter)

//Convert a String into a List of String Elements splitting by the given Delimiter
List<String> stringToList(String s, String delimiter)

//Check if each list element to see if it contains the String passed through 's'
boolean listHasString(List<String> lst, String s)

//Convert a String into UTF8 Bytes
byte[] stringToUTF8Bytes(String str)

//Convert UTF8 Bytes into a UTF8 String
String bytesToUTF8String(byte[] bys)

//Convert Bytes into a Hex String of Bytes , makes it easy to print out the Bytes (does not encode or decode)
String rawBytesToHexString(byte[] bys)

//Convert Bytes into a Sha256 Hash
String bytesToSHA256Hash(byte[] bys)

//Get the length of a String return -1 if the string is NULL
int stringLength(String s)

```

REFLECTION
------------

```Java

//Returns Type Class 'java.lang.Byte'
Class<Byte> getByteType()

//Returns Type Class 'java.lang.Integer'
Class<Integer> getIntType()

//Returns Type Class 'java.lang.Character'
Class<Character> getCharType()

//Returns a Java Byte Array
byte[] createByteArray(int size)

//Returns a Java Integer Array
int[] createIntArray(int size)

//Returns a Java Character Array
Character[] createCharArray(int size)

//Pass in a Class Path with Name , Pass a Method to check for if it exists in that Class
boolean javaMethodExists(String className, String methodName)

//Get the Type Class From a Class Path and Name String 'classForname'
Class<?> getClassType(String className)

//Create a Java Array Passing in Class Path and Name of the Element Type
Object createReflectArray(String className, int size)

//Create a Java Array Passing in Type Class of the Element Type
Object createReflectArray(Class<?> classType, int size)

//Check if a Function Exists in the "this" Object of the Instance / Hook
boolean hasFunction(String function)

//Check if a Function exists within a Class
boolean hasFunction(String classPath, String function)

//Check if a Field Exists in the "this" Object of the Instance / Hook
boolean hasField(String field)

//Check if a Field exists within a Class
boolean hasField(String classPath, String field)

```

```LUA

	local bytePath = "java.lang.Byte"
	local byteClass = param:getClassType(bytePath)
	local byteArray = param:createReflectArray(byteClass, 6)
	-- Create a Byte[] (array) size of 6
	-- Or

	local byteClass = param:getByteType()
	local byteArray = param:createReflectArray(byteClass, 6)
	-- Or

	local byteArray = param:createByteArray(6)

	local strClass = "java.lang.String"
	local strArray = param:createReflectArray(strClass, 6)
	-- Create a String[] (String) size of 6

```



LONG HELPERS
------------

These functions are to handle the issue of the LUA Version not supporting BIG INT / LONG


```Java

//Uses Reflection to get a Field of Type Long and Convert it into a String to the LUA Layer
String getFieldLong(Object instance, String fieldName)

//Uses Reflection to Set a Field Value Type of Long from a String of a Long Value
void setFieldLong(Object instance, String fieldName, String longValue) 

//Takes a String of a Long Value to the Java Layer Parsing String to Long
//Takes Parcel Instance to Set the long Value in
void parcelWriteLong(Parcel parcel, String longValue)

//Reads Long Value from a Parcel then Converts it to a String Before lifting it to the LUA Layer
String parcelReadLong(Parcel parcel)

//Takes a String of a Long Value to the Java Layer Parsing String to Long
//Takes Bundle Instance & key to Set the long Value in
void bundlePutLong(Bundle bundle, String key, String longValue)

//Reads Long Value from a Bundle then Converts it to a String Before lifting it to the LUA Layer
String bundleGetLong(Bundle bundle, String key)

//Set the Result of the Hooked Function as a Long , Parsing the Given String to a Long in the Java Layer
void setResultToLong(String long_value)

//Gets the Result of the Hooked Function as a Long then Converting it to a String before lifting it to the LUA Layer
String getResultLong()

```

```LUA

	local memInfo = luajava.newInstance("android.app.ActivityManager$MemoryInfo")
	-- set the Field 'availMem' to 999999999
	param:setFieldLong(memInfo, "availMem", "999999999")

	-- get the Field 'availMem' as a String long "999999999"
	local availMemory = param:getFieldLong(memInfo, "availMem")


	local parcel = luajava.newInstance("android.os.Parcel")
	-- write to parcel a Long Value represented as a String in LUA
	param:parcelWriteLong(parcel, "7777777777777")

	-- read from parcel a Long Value represented as a String in LUA
	local longBack = param:parcelReadLong(parcel)


	local bundle = luajava.newInstance("android.os.Bundle")
	-- put to bundle a Long Value represented as a String in LUA
	param:bundlePutLong(bundle, "SomeKey", "555555555555")

	-- get from bundle a Long Value represented as a String in LUA
	local longBack = param:bundleGetLong(bundle, "SomeKey")

```


Arguments / Return / Result
------------

```Java

//Getting / Setting Arguments of Hooked Function
//Do note the NON Long Function helpers are not needed as the "setResult" and "setArgument" functions were improved to handle targeted types
//For ensurance the specific type function helpers are still provided

Object getArgument(int index)

void setArgument(int index, Object value)

void setArgumentString(int index, Object value)

void setArgumentString(int index, String value)

//Getting / Setting Result of Hooked Function

Object getResult()

void setResult(Object result)

void setResultString(Object result)

void setResultString(String result)

void setResultByteArray(Byte[] result)

Throwable getException()

```


Settings / Globals
------------

```Java

//Get a XPL-EX Setting
String getSetting(String name)

//Get a XPL-EX Setting , if setting does not exist return Default Value
String getSetting(String name, String defaultValue)

//This will Get the setting but if it does not exist it will use an alternate version of that setting if it exist under that old name
String getSettingReMap(String name, String oldName)

//This will Get the setting but if it does not exist it will use an alternate version of that setting if it exist under that old name
//If all fails then return Default Value
String getSettingReMap(String name, String oldName, String defaultValue)

//Get a XPL-EX Setting as int, if setting does not exist return Default Value
int getSettingInt(String name, int defaultValue)

//Get a Global Value for that Instance of Target Hook App
Object getValue(String name, Object scope)

//Put a Global Value for that Instance of Target Hook App
void putValue(String name, Object value, Object scope)
```



Sorry English not good will get better over time <3 much love
