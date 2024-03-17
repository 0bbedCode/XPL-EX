Comparison with XPrivacy
========================

The list below is [taken from](https://github.com/M66B/XPrivacy#restrictions) the XPrivacy documentation.

* **Bold** means that XPrivacyLua supports the restriction
* ~~Strike through~~ means that XPrivacyLua won't support the restriction

Before asking questions, please read [this FAQ](https://github.com/0bbedCode/XPL-EX/blob/new/FAQ.md#user-content-faq4).

<a name="accounts"></a>
* Accounts
	* ~~return an empty account list~~ see remark below
	* ~~return an empty account type list~~ see remark below
	* **return fake account info**
	* ~~return empty authorization tokens~~ user choice
	* **return an empty list of synchronizations**

Since account info can be faked, it is not really necessary to hide the account list, which can also cause apps to crash.

<a name="browser"></a>
* Browser
	* ~~return an empty bookmark list~~ see remark below
	* ~~return an empty download list~~ see remark below
	* ~~return empty search history~~ see remark below

Different browsers (stock, Chrome, Firefox, etc) have different content providers, so this is app specific.
Browser data is generally not accessible on recent Android versions anymore.

<a name="calendar"></a>
* Calendar
	* **return an empty calendar**

<a name="calling"></a>
* Calling
	* ~~prevent calls from being placed~~ user choice
	* ~~prevent SIP calls from being placed~~ user choice
	* **prevent SMS messages from being sent**
	* **prevent MMS messages from being sent**
	* **prevent data messages from being sent**
	* **return an empty call log**

<a name="clipboard"></a>
* Clipboard
	* **prevent paste from clipboard (both manual and from an application)**

<a name="contacts"></a>
* Contacts
	* **return an empty contact list**
		* **content://com.android.contacts**
		* **content://com.android.contacts/contacts**
		* **content://com.android.contacts/data**
		* **content://com.android.contacts/phone_lookup**
		* **content://com.android.contacts/profile**
		* **SIM card**

<a name="dictionary"></a>
* Dictionary
	* ~~return an empty user dictionary~~ not privacy related

<a name="email"></a>
* E-mail
	* ~~return an empty list of accounts, e-mails, etc (standard)~~ see remark below
	* ~~return an empty list of accounts, e-mails, etc (Gmail)~~ see remark below

Information about e-mail accounts and messages depends on the installed e-mail app and is therefore app specific.
E-mail data is generally not accessible on recent Android versions anymore.

<a name="identification"></a>
* Identification
	* **return a fake Android ID**
	* **return a fake device serial number**
	* ~~return a fake host name~~ not available on recent Android versions anymore
	* **return a fake Google services framework ID**
	* ~~return file not found for folder [/proc](http://linux.die.net/man/5/proc)~~ will result in crashes
	* **return a fake Google advertising ID**
	* ~~return a fake system property CID (Card Identification Register = SD-card serial number)~~ tracking related
	* ~~return file not found for /sys/block/.../cid~~ will result in crashes
	* ~~return file not found for /sys/class/.../cid~~ will result in crashes
	* ~~return a fake input device descriptor~~ tracking related
	* ~~return a fake USB ID/name/number~~ tracking related
	* ~~return a fake Cast device ID / IP address~~ tracking related

<a name="internet"></a>
* Internet
	* ~~revoke permission to internet access~~ will result in crashes
	* ~~revoke permission to internet administration~~ will result in crashes
	* ~~revoke permission to internet bandwidth statistics/administration~~ will result in crashes
	* ~~revoke permission to [VPN](http://en.wikipedia.org/wiki/Vpn) services~~ will result in crashes
	* ~~revoke permission to [Mesh networking](http://en.wikipedia.org/wiki/Mesh_networking) services~~ will result in crashes
	* **return fake extra info**
	* ~~return fake disconnected state~~ not privacy related
	* ~~return fake supplicant disconnected state~~ not privacy related

If you want to fake offline state, see [the example definitions](https://github.com/0bbedCode/XPL-EX/tree/new/examples) about how this can be done with a custom restriction definition.

<a name="IPC"></a>
* IPC
	* ~~Binder~~ will result in crashes
	* ~~Reflection~~ will result in crashes

<a name="location"></a>
* Location
	* **return a random or set location (also for Google Play services)**
	* **return empty cell location**
	* **return an empty list of (neighboring) cell info**
	* **prevents geofences from being set (also for Google Play services)**
	* **prevents proximity alerts from being set**
	* **prevents sending NMEA data to an application**
	* **prevent phone state from being sent to an application**
		* **Cell info changed**
		* **Cell location changed**
	* ~~prevent sending extra commands (aGPS data)~~ not privacy related
	* **return an empty list of Wi-Fi scan results**
	* **prevent [activity recognition](http://developer.android.com/training/location/activity-recognition.html)**

<a name="media"></a>
* Media
	* **prevent recording audio**
	* **prevent taking pictures**
	* **prevent recording video**
	* **you will be notified if an application tries to perform any of these actions**

<a name="messages"></a>
* Messages
	* **return an empty SMS/MMS message list**
	* **return an empty list of SMS messages stored on the SIM (ICC SMS)**
	* **return an empty list of voicemail messages**

<a name="network"></a>
* Network
	* ~~return fake IP's~~ see [this FAQ](https://github.com/0bbedCode/XPL-EX/blob/new/FAQ.md#user-content-faq4)
	* ~~return fake MAC's (network, Wi-Fi, bluetooth)~~ see remark below
	* **return fake BSSID/SSID**
	* **return an empty list of Wi-Fi scan results**
	* **return an empty list of configured Wi-Fi networks**
	* ~~return an empty list of bluetooth adapters/devices~~ tracking related

MAC addresses are [not available anymore](https://developer.android.com/training/articles/user-data-ids.html#version_specific_details_identifiers_in_m) on supported Android versions.

<a name="nfc"></a>
* NFC
	* ~~prevent receiving NFC adapter state changes~~ user choice
	* ~~prevent receiving NDEF discovered~~ user choice
	* ~~prevent receiving TAG discovered~~ user choice
	* ~~prevent receiving TECH discovered~~ user choice

<a name="notifications"></a>
* Notifications
	* **prevent applications from receiving [statusbar notifications](https://developer.android.com/reference/android/service/notification/NotificationListenerService.html) (Android 4.3+)**
	* ~~prevent [C2DM](https://developers.google.com/android/c2dm/) messages~~ use a firewall and block **xxx.mtalk.google.com:yyy** for Google Play services

<a name="overlay"></a>
* Overlay
	* ~~prevent draw over / on top~~ will result in [crashes](https://github.com/M66B/XPrivacy/issues/2374)

<a name="phone"></a>
* Phone
	* **return a fake own/in/outgoing/voicemail number**
	* **return a fake subscriber ID (IMSI for a GSM phone)**
	* **return a fake phone device ID (IMEI): 000000000000000**
	* ~~return a fake phone type: GSM (matching IMEI)~~ not privacy related
	* ~~return a fake network type: unknown~~ not privacy related
	* ~~return an empty ISIM domain~~ not available in user space
	* **return an empty IMPI/IMPU**
	* **return a fake MSISDN**
	* return fake mobile network info
		* Country: XX
		* Operator: 00101 (test network)
		* Operator name: fake
	* return fake SIM info
		* Country: XX
		* Operator: 00101
		* Operator name: faket
		* **Serial number (ICCID): fake**
	* ~~return empty [APN](http://en.wikipedia.org/wiki/Access_Point_Name) list~~ not privacy related
	* ~~return no currently used APN~~ not privacy related
	* ~~prevent phone state from being sent to an application~~ not privacy related
		* ~~Call forwarding indication~~
		* ~~Call state changed (ringing, off-hook)~~
		* ~~Mobile data connection state change / being used~~
		* ~~Message waiting indication~~
		* ~~Service state changed (service/no service)~~
		* ~~Signal level changed~~
	* **return an empty group identifier level 1**

<a name="sensors"></a>
* Sensors
	* **return an empty default sensor**
	* **return an empty list of sensors**
	* restrict individual sensors: might be implemented as pro feature
		* acceleration
		* gravity
		* heartrate
		* humidity
		* light
		* magnetic
		* motion
		* orientation
		* pressure
		* proximity
		* rotation
		* step
		* temperature

<a name="shell"></a>
* Shell
	* ~~return I/O exception for Linux shell~~ will result in crashes
	* ~~return I/O exception for Superuser shell~~ will result in crashes
	* ~~return unsatisfied link error for load/loadLibrary~~ will result in crashes

<a name="storage"></a>
* Storage
	* ~~revoke permission to the [media storage](http://www.doubleencore.com/2014/03/android-external-storage/)~~ will result in crashes
	* ~~revoke permission to the external storage (SD-card)~~ will result in crashes
	* ~~revoke permission to [MTP](http://en.wikipedia.org/wiki/Media_Transfer_Protocol)~~ will result in crashes
	* ~~return fake unmounted state~~ not privacy related
	* ~~prevent access to provided assets (media, etc.)~~ will result in crashes

The supported Android versions provide the [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider.html),
which apps should use to open files instead of opening files directly.
Moreover, the supported Android versions provide [runtime permissions](https://developer.android.com/training/permissions/requesting.html),
so you can always choose to not grant storage permission to an app.

If you want to confine apps to their own folder, see [the example definitions](https://github.com/0bbedCode/XPL-EX/tree/new/examples) about how this can be done with a custom restriction definition.

<a name="system"></a>
* System
	* **return an empty list of installed applications**
	* **return an empty list of recent tasks**
	* **return an empty list of running processes**
	* **return an empty list of running services**
	* **return an empty list of running tasks**
	* **return an empty list of widgets**
	* ~~return an empty list of applications (provider)~~ not available on recent Android versions anymore
	* **prevent package add, replace, restart, and remove notifications**

<a name="view"></a>
* View
	* ~~prevent links from opening in the browser~~ not privacy related
	* return fake browser user agent string (WebView)
		* *Mozilla/5.0 (Linux; U; Android; en-us) AppleWebKit/999+ (KHTML, like Gecko) Safari/999.9*
