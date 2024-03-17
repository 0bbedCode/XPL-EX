# XPL-EX
Hooking + Privacy Framework For Android 6.0 +



XPL-EX (Long & Boring)
------------

XPL-EX is an Android application designed to enhance complete app privacy. It is an extension of "XPrivacy-Lua" by "M66B." This application is entirely open-source, free from advertisements, analytics, and does not require any financial contribution. It's a fully free solution with a strict policy of no logs and no server connections. Currently, XPL-EX is in the early beta stage of this fork. Its primary function is to safeguard your device information, ensuring that other installed applications cannot access it beyond necessary limits. The app employs various hooks to target methods that retrieve data, whether through outdated, deprecated, or new techniques, it will be hooked.

In today's world, the sale and misuse of personal data have become commonplace, often leading to resigned acceptance of constant surveillance. However, XPL-EX challenges this norm, offering a pathway to complete privacy and a way to evade the intrusive eyes of 'Big Brother.' This app is the first installment in a series of upcoming documents aimed at achieving a secure and completely private device. Adherence to specific standards can place users beyond the reach of major surveillance agencies. This multi-part project will be freely accessible, thoroughly documented, and open to the global community. 

I am getting old my self , I slowly show, but for the future generations or just for those who fear this will be for them.

Intercepting apps at the API level of your device can effectively obscure the type of device on which they are running. To achieve this, it's crucial to address both Native and Java components. Native hooking is a feature set to be introduced to XPL-EX, which currently supports Java through the 'XPOSED' Framework for hooking purposes.

The concept is straightforward: to apps, your device appears as something entirely different. For example, an app might perceive your device as a "Samsung Galaxy from Iceland with a SIM carrier named 'LeetPrivacy,' boasting 400 GB of RAM, a 90-core CPU, an Android ID of 0000000000000000, and an IMEI of 000." In reality, however, it could be a "Google Pixel from America with a Verizon SIM, 8 GB of RAM, and an 8-core CPU." Extreme examples like the "400 GB of RAM" are used to illustrate the point, although they might make the device more conspicuous.

The overarching aim is to limit the information apps can access or gather about our devices, even those allowed through our firewall.

Additionally, there are emerging techniques like "keystroke dynamics" used to identify individuals based on their typing patterns. While not widely adopted or entirely accurate, they are still utilized. It's important to note that nearly every app, especially those from major social media platforms like TikTok, Facebook, and Instagram, records data on your phone.


XPL-EX (Simple)
------------
XPL-EX Paired with the Pro Companion app, it delivers an app that can dynamically define Xposed Hooks without having to compile a whole APK. Hooking apps at runtime will allow you to modify your target app in ways such as translating in-app text to different languages.
Bypassing features, disabling popups, or providing privacy. The requirements are the Xposed Framework and Android 6.0 or higher. Root is not a requirement unless the Xposed Framework you choose requires it.
This app will only protect you from apps at runtime getting your device information, from device IDs to screen resolution. The goal of this app is to provide complete in-app privacy control for any and all apps.

In App
------------

<img src="https://github.com/0bbedCode/XPL-EX/assets/114315756/d32115bd-2c6b-4a57-93bb-b7f8d523448c" width="311" height="640">
<img src="https://github.com/0bbedCode/XPL-EX/assets/114315756/a160a7c1-b4ce-4c89-8989-ce96fca7a474" width="311" height="640">
<img src="https://github.com/0bbedCode/XPL-EX/assets/114315756/d51e9c78-6efb-48f2-876a-ef5c69e48fd6" width="311" height="640">
<img src="https://github.com/0bbedCode/XPL-EX/assets/114315756/25b82519-309f-4b0e-9d12-87091ec699d6" width="311" height="640">


To Come
------------
XPL-EX is not just an application designed for privacy; it also harbors potential for future enhancements in areas such as reverse engineering and concealing elements like Xposed, root access, and more. However, the primary focus remains steadfastly on privacy, with other features set to follow in due course.

This repository will offer a modified version of the Pro Companion app, which will facilitate free communication with XPL-EX. In time, the plan is to integrate all these functionalities into a single, unified application. This strategic approach ensures that while we expand the app's capabilities, its core mission of ensuring user privacy remains at the forefront.

Help
------------

[XPL DEFINE](https://github.com/0bbedCode/XPL-EX/blob/new/DEFINE.md) <br>
[XPL-EX API](https://github.com/0bbedCode/XPL-EX/blob/new/APIHELP.md) <br>
[XPL-EX Examples](https://github.com/0bbedCode/XPL-EX/blob/new/LUAHELP.md) <br>

Do note for every Hook that is not re-created or added within the 'PrivacyEx' or XPL-EX Collection (it will be added) but you can use both Collections at once.<br>
You cen switch between the two collections in the "XPrivacy-LUA Pro Companion App". Please Avoid Selecting Hook Groups with similar Hooks

If the Hooks do not appear , nor the Collection appears in the Pro Companion App even after a Reboot. <br>
Use a root file explorer navigate to the Directory "/data/system/xlua/" Backup any File in there (xlua) database, then delete it. Restart the Device <br>
Later versions this will be 'fixed' / alot easier to work with within the UI etc <br>

### Telegram Group
https://t.me/xplexex

### Telegram
https://t.me/obbedcode

### Telegram Discussion Chat
https://t.me/XPLEXCHAT

### XDA Thread
https://xdaforums.com/t/xpl-ex-xprivacylua-ex-android-privacy-manager-hooking-manager-extended.4652573/

### Discord , Nulled (ObbedCode)<br><br>

Images
------------

## XPL-EX Development Roadmap

### Completed Features
- [X] Hook Java APIs
- [X] Compatibility with Xposed, LSPosed, EdXposed, ThaiChi, VXP
- [X] Dynamic Privacy Features (Free and Open Source Software)
- [X] Modification of Hook Behavior using LUA Scripts
- [X] Built-in Pre-defined Hooks for Enhanced Privacy
- [X] UI Component for Intercepting System Properties (`System.getProperty` / `SystemProperties.get`)
- [X] UI Component to Select CPU Maps for the System Information File `/proc/cpuinfo`
- [X] Expanded API Functions for Use in LUA Scripts
- [X] Updated LUA Core (Note: Current version does not support BIG INT still...)
- [X] Enhanced and Optimized Hooks for Android 14 and or Just Enhanced
- [X] Dark Mode Feature in Main Application

### Planned Enhancements
- [ ] Add new Text to different languages (Tanslation)
- [ ] Class Finder to Identify Classes Inheriting Specific Interfaces (e.g., `javax.microedition.khronos.opengles.GL10`)
- [ ] Protection Against Inner Process Communication
- [ ] Adding Icons and Refining Text & Names for CPUs
- [ ] Updating and Fixing LUA Code for More Hooks
- [ ] Secure Communication between Hook and XPL-EX
- [ ] Code Cleanup (Focus on UI Development)
- [ ] Hook Native APIs
- [ ] Target Native APIs (e.g., `__system_property_get`, `popen`, `fopen`, `open`, `exec`) for Enhanced Privacy
- [ ] Additional API Functions for LUA Scripts
- [ ] Integration of 'M66B's "XPrivacyLua-Pro Companion App" Features into Main App "XPL-EX"
- [ ] UI for Modifying/Mocking Specific Device Sensors
- [ ] Built-in Pre-defined Hooks for Reverse Engineering, Logging, Bypassing/Hiding, and Disabling Ad Libraries in Apps
- [ ] Mode/View Switcher (e.g., "Reverse Engineering Mode," "Privacy Mode")
- [ ] System for Selecting Phone Configurations for Spoofing
- [ ] Support for Frida
- [ ] Floating Menu in Hooked Apps for UI Inspection (Similar to "Reflect Master")
- [ ] Incorporation of Engaging Visual Elements and Progress Indicators
- [ ] Documentation Refinement and Language Corrections

**Note:** This roadmap is subject to change based on project development and user feedback.

* From https://github.com/M66B/XPrivacyLua

Notes
-----

* Some apps will start the camera app to take pictures. This cannot be restricted and there is no need for this, because only you can take pictures in this scenario, not the app.
* Some apps will use [OpenSL ES for Android](https://developer.android.com/ndk/guides/audio/opensl-for-android.html) to record audio, an example is WhatsApp. Xposed cannot hook into native code, so this cannot be prevented.
* The get applications restriction will not restrict getting information about individual apps for stability and performance reasons.
* The telephony data restriction will result in apps seeing a fake IMEI. However, this doesn't change the IMEI address of your device.
* Restricting activity recognition (location) results for recent Google Maps versions and possibly other apps in the error *... java.lang.ClassNotFoundException ...* for unknown reasons.

Compatibility
-------------

XPL-EX is supported on Android 6.0 Marshmallow and later.
For Android 4.0.3 KitKat to Android 5.1.1 Lollipop you can use [XPrivacy](https://github.com/M66B/XPrivacy/blob/master/README.md) (unsupported though).

XPL-EX is supported for smartphones and tablets only. XPL-EX is not supported on emulators.

Hooking *com.google.android.gms.location.ActivityRecognitionResult.extractResult* (restriction *Determine activity*)
is known to fail with *script:25 vm error: java.lang.ClassNotFoundException: com.google.android.gms.location.DetectedActivity*
and *script:28 attempt to call nil* for some apps, like Google Maps and NetFlix, for yet unknown reasons. (maybe I can fix this OBC)

Installation
------------

* Install [Magisk](https://www.xda-developers.com/how-to-install-magisk/)
* Android 6-7: download, install, and activate [Xposed](http://forum.xda-developers.com/xposed)
* Android 8.1-12: download, install, and activate [LSPosed](https://forum.xda-developers.com/t/lsposed-xposed-framework-8-0-11-0-edxposed-alternetive.4228973/) ~~or [EdXposed](https://forum.xda-developers.com/t/official-edxposed-the-successor-of-xposed-oreo-pie-q-r-2020-07-19.4070199)~~
* Download, install, and activate [XPL-EX]()

LSPosed: please make sure that any app you want to restrict is [in the 'scope'](https://github.com/LSPosed/LSPosed/wiki/How-to-use-it#install--activate-modules) of LSposed.

Certificate fingerprints:

* MD5: N/A
* SHA1: N/A
* SHA256: N/A

Frequently Asked Questions
--------------------------

See [here](https://github.com/0bbedCode/XPL-EX/blob/new/FAQ.md) for a list of often asked questions. (XPrivacy-Lua)


Donate ;)
------------

https://lua.xprivacy.eu/<br>
https://github.com/M66B/XPrivacyLua

Thanks M66B for what you have left us now I will continue <3 


More From Obbed
------------

This app alone is not enough to "hide" you more so combine it with a Complex Security System. XPL-EX will only protect apps from identifying you in any way.

***[AFWall+]*** with AFWall Anti Leak Module (Dont allow anything through that firewall not even the kernel) <br>
***[AppOps]*** rikka.appops to have Permission Control over your Apps (further restrict them)<br>
***[OVPN/Mullvad]*** Super Private <3 or Just use TOR ORBot (AFWalls has ability block those apps with allowed internet to only use the Internet if TOR is Connected)<br>
***[Hail]*** Disable / Freeze System Apps such as Google Play Services, Google Play etc <br>
***[Greenify]*** 5.0 Beta (because it is still useful all apps need to die in the background no matter)<br>
***[AdAway]*** Open Source System Wide AdBlocker allows you to add Repos to Block custom or more Trackers / Ads. (SYSTEM WIDE) so its super cool<br>
***[BromiteWebview]*** Modifed System Webview with More Pirvacy<br>
***[Sensor Disabler]*** Sensors Matter , Disable them all or Mock them ! Ps newer versions of Android has a Disable all Sensors on the Device Button<br>
***[Camera/Mic Blocker]*** Or you have have a phyisical pop up camera like the one seen on the Oneplus7 Pro to truly know if your being peeped on. Newer versions of Android also notifys you when your MIC or CAM are being used<br>
***[SD Maid]*** Version before there new version is good at being a System UTIL but also shows you all apps that start when the Phone Starts allowing you to disable them<br>
***[Hide My Applist]*** Best Xposed App to Hide Apps from other Apps<br>
***[Custom Router Firmware w VPN & Firewall & AdBlock]***  OpenWRT <3<br>
***[Custom ROM + Custom kernel]*** Open Source Cool ROM with a Cool String Kernel >:)<br><br>

Once again do not let anything through that firewall not even on that list (AdAway, VPN, Webview) sure but the rest dont even let those through the great wall
Ps its not poor security having root if the user isnt the user to get click jacked or tricked into installing fake modules... dont be dumb<br><br>

Cool Band


![292759617-b59f1a2a-a4b5-4276-97f1-796175aee834](https://github.com/0bbedCode/XPL-EX/assets/114315756/17a59b9d-6e1e-43e9-9aa8-0cccb81a1164)
