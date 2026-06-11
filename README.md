# XPL-EX   d
Hooking + Privacy Framework For Android 6.0 +<br>

![photo_2025-04-18_17-47-14](https://github.com/user-attachments/assets/a8ba4b6d-7ba0-4371-9f27-578ae5a16234)


XPL-EX
------------
XPL-EX (XPrivacy Lua EX) based on original (XPrivacy Lua) by M66B will prevent most if not all Installed Applications on your Device from tracking and or Identifying you. You have the ability to Define your hook Hook Definitions onto Applications and utilizing LUA Scripts to define the behaviour of the Hook allowing you to have Full Control over your Applications. Powwered by the Community it will forever remain Full Open Source (FOSS), Never bundled with Trackers, Analytics, or Advertisements lastly never ever needing to connect or to communicate to any Server.<br><br>

When Comparing with GrapheneOS like Projects, XPL-EX will give you more Anti Tracking Abilities. Say bye bye to Data Brokers, buying and Selling of useful user Data in their activities.
> Running such Tracking Applications Such as  [Fingerprint Pro](https://play.google.com/store/apps/details?id=com.fingerprintjs.android.fpjs_pro_demo&hl=en_US) will have a very high success Rate at tracking users on GOS and compared to other Solutions as well. XPL-EX was the first too achieve a Visitor Never Found result from FingerprintPro ensuring you are not to be tracked.

<br>

> * *Supports Android 6+ (Android Marshmellow+, SDK 23+)*, *Virtual / ThaiChi Supported (Rootless Solution Not Stable)*,  *Requires NO Internet Connection, No Logins*

<br>

Motivation
------------

<details>
<summary>Click here for Blah Blah ....  </summary>
  Some reason I have always been Schizo from a young age. That being Said when Growing up I always wanted an Application for Xposed that would be the ultimate All in One Solutions for manipulating your Device Identifiers from Applications. During these Times many years ago I would Layer as Many Xposed Modules over to Spoof as Many IDs, rendering a lot of instability, all the while these Applications to Spoof your Device Identifiers also had Advertisements and Tracking built into the App how Ironic.<br><br>
Many years later here I am, following in the path of the Computer, Ive become less paranoid now as I am trying to be a more open person. I still had a vision and when I Seen M66B will Discontinue XPrivacyLua my I seized the moment to use this app for a base of what I visioned.<br>
Now with the support from the Community, I shall build a Powerful Application that will ensure tracking from Applications, all the while being FOSS, Free, No Analytics, No Ads...<br>
Many changes were made (100k+ Lines of Code Contributed many more Modified) to make it into the monster it is Now. If an Application Somehow Slips through, I spend many Hours and Days Reverse Engineering these Applications to ensure XPL-EX will always succeed in making you un identifiable.<br><br><br>

<div align="center"> My name is ObbedCode, join me on this Adventure giving Privacy back to the Users, and giving a big F*ck you to Big Data, and the Three letter agencies. Much love <3 </div><br>
</details>

<br><br>
Installation / Requirements
------------

* Install [Magisk](https://www.xda-developers.com/how-to-install-magisk/)
* Android 6-7: download, install, and activate [Xposed](http://forum.xda-developers.com/xposed)
* Android 8.1-12: download, install, and activate [LSPosed](https://forum.xda-developers.com/t/lsposed-xposed-framework-8-0-11-0-edxposed-alternetive.4228973/) ~~or [EdXposed](https://forum.xda-developers.com/t/official-edxposed-the-successor-of-xposed-oreo-pie-q-r-2020-07-19.4070199)~~
* Download, install, and activate [XPL-EX]()

LSPosed: please make sure that any app you want to restrict is [in the 'scope'](https://github.com/LSPosed/LSPosed/wiki/How-to-use-it#install--activate-modules) of LSposed.
<br><br>
Targeted APIs:

<details>

<summary style="font-size: 54px;"> Click here to See the Identifier Coverage</summary>

<br><br>These are the list of SOME of the Data that is and can be Spoofed via XPL-EX. Within each Item being Spoofed will also tell you at what Level the Hooks are at.<br>

- Android ID
    - Api Hook
        - Settings (Secure)
    - Cache Hook
        - Named Value Cache
    - IPC Hook
        - Content Resolver (Call + Query)
- Advertising ID (Google)
    - Api Hook
        - Constructor & "getId"
    - IPC Hook
        - Binder Proxy
- GSF ID (Google Services Framework ID)
    - IPC Hook
        - Content Resolver (query)
- AppSetId
    - Api Hook
        - Constructor & "getId"
    - IPC Hook
        - Binder "execTransact"
- DRM
    - Api Hook
        - MediaDRM
- Open Anonymous IDs (OAID) and (VAID, AAID, UDID)
    - Api Hook
        - Settings (Secure, System, Global) & MiuiSettings
        - KeyguardManager
    - IPC Hook
        - IdProviderImpl
        - Facebook
        - Amazon
        - Samsung
        - Asus
        - Lenovo
        - Xiaomi
- Boot ID (proc/sys/kernel/random/boot_id)
    - Api Hook
        - Libcore
        - Shell "exec" with "cat"
- Serial Numbers
    - Api Hook
        - build.prop Properties (shell & apis)
        - Build Field
- MAC Address, SSID, BSSID, IP Addresses (IPv4 & IPv6) and Network Info
    - Parcel and Constructor
- IMEI/MEID, SIM Serial, ICC ID, Phone Numbers (LAC & CID) (Currently Unavailable in the 1.5.3 Version)
    - Service Hook
- Time Stamps (File and Application Install & Update TimeStamps)
    - STAT (Shell & Apis)
    - File (Android & Java NIO)
- Sensors (Hide)
    - For more Control please Refer to use [Sensor Disabler](https://github.com/wardellbagby/sensor-disabler)
- UUID Named Files with UUIDs
- Charging Cycles
- Boot Count



<br><br>Many More things as well not yet defined!!!<br><br>



To have a more Fine Tuning over Controlling the List of Applications that Certain apps Can see please refer to use [HMA (HideMyAppList)](https://github.com/Dr-TSNG/Hide-My-Applist)<br>
To Spoof your GPS Location please use [GPS Setter](https://github.com/jqssun/android-gps-setter)<br>
For best Android Ad Blocker [AdAway](https://adaway.org/)<br>
For great Firewall Solution stronger than as Simple Permission Based Firewall (cough cough *GOS*) use [InviZible](https://github.com/Gedsh/InviZible)<br>
For the best App Manager (Block Services, Startup and Much More) use [AppManager](https://github.com/MuntashirAkon/AppManager)<br>
To Freeze and or Disable Apps / Block use [Hail](https://github.com/aistra0528/Hail)<br>
To Block Microphone Access [PilferShush Jammer](https://github.com/kaputnikGo/PilferShushJammer)<br>
To Turn off All Sensors on the Device at Once use [Sensors Off](https://github.com/LinerSRT/SensorsOff)<br>
To Check Information on your Apps including Tracking SDK and Libraries use [LibChecker](https://github.com/LibChecker/LibChecker)<br>
Chromium based Browser [Cromite](https://github.com/uazo/cromite)<br>
Favorite Wallet (No KYC and has Swaps) [Cake Wallet](https://github.com/cake-tech/cake_wallet)<br>
For the best Secure Chatting App (Signal Fork) [Molly](https://molly.im/)<br>
PlayStore Alternative (Aurora Store)[https://github.com/whyorean/AuroraStore]
Modifying your Router Firmware use of course [OpenWRT](https://openwrt.org/)<br>


</details>


<br><br><br>


Demo/Pictures
------------
<img src="https://github.com/user-attachments/assets/52bf2d05-05ae-42a7-aa41-be357c6e88b5" width="311" height="640">
<img src="https://github.com/user-attachments/assets/ec717fb0-c1f4-47f9-ac36-b6c71bd6538c" width="311" height="640">
<img src="https://github.com/user-attachments/assets/f06808fa-bb44-4c84-801b-8a0590c6f702" width="311" height="640">
<img src="https://github.com/user-attachments/assets/c4f7580b-d183-4baf-96e0-fc1ed5877b69" width="311" height="640">
<img src="https://github.com/user-attachments/assets/06553558-1a92-4db2-b9ea-04fbeab48c7b" width="311" height="640">
<img src="https://github.com/user-attachments/assets/e20caed3-87cc-4fb6-b65d-0656c9714321" width="311" height="640">


Donations
------------
<br>

*BTC:*
<br>
bc1q0znz3vqcpg6q9c34w78uk7yqsjz3gu2sh6apg7
<br><br>

*LTC:*
<br>
ltc1qvy7e4k26gwuj0kvuugsc8mextj9vkyu3tlrefy
<br><br>

*ETH:*
<br>
0x4282Bc864B29Dbd62B9A9960A71e8343eDA44707
<br><br>

*XMR:*
<br>
89WiRBYaYcKa947MD8SNNe7Jkag5mvZruRFnVEDmqD5rRonTsEMr7KJ4PeYByDZAGjM2XJUgAsMniLiwcq4vSirDQvf8sT9
<br><br>

*SOL*
<br>
2Y7fAzrByFCUaAP8JMYBgbfN1YveVVX1xgfktFtvUmBx
<br><br>

*TRX*
<br>
TRk5a1C4U5fTgMbZQBi7wRM1hjvguPnqBb
<br><br>

*BCH:*
<br>
bitcoincash:qz5scaha4gyh92pjw5z5uhuy33sm2rdh5v0tpw2ggm
<br><br>


Resources
------------
*FAQ, See [here](https://github.com/0bbedCode/XPL-EX/blob/master/FAQ.md) for a list of often asked questions. (XPrivacy-Lua)* <br>
*API Usage, See [here](https://github.com/0bbedCode/XPL-EX/blob/new/APIHELP.md)*

<br>

*Telegram, [Announcement Channel](https://t.me/xpl_ex)*
<br>
*Telegram, [Discussion Group](https://t.me/XPL_EX_CHAT)*
<br><br>

Credits / Help
------------

<br>

*Original Dev [M66B](https://github.com/M66B)*
<br>
*VDInfos Dev [VD-8](https://github.com/VD171/VD-Infos)*
<br>
*Detections research etc. [HUBERTH](https://t.me/HubertHub)*




