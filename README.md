<p align="left">
	<h2 align="left"> Remind Me App </h2>
	<h4 align="left"> Remind Me is an android app which allows you to set location and time based reminder and helps you remember everything by adding notes. <h4>
</p>

---

## Functionalities
- [ ] Add location based reminder using Google Map Platform and Geofencing.
- [ ] Add time based reminder using Alarm Manager.
- [ ] Notes Section using Room persistance library.
<br>
	
## Instructions to run

* Pre-requisites:
	-  Android Studio v4.0+
	-  A working Android physical device or emulator with USB debugging enabled

* Directions to setup/install
	- Clone this repository to your local folder using Git bash:
	```bash
	git clone https://github.com/jhavidit/Remind-Me.git
	```
	- Open this project from Android Studio
	- Connect to an Android physical device or emulator
	- To install the app into your device, run the following using command line tools
	```bash
	gradlew installDebug
	```
	Now add your google maps api key to the local.properties in this format
	```bash
	MAPS_API_KEY="yourapikey"
	```

* Directions to execute
	-  To launch hands free, run the following using command line tools
	```bash
	adb shell monkey -p tech.jhavidit.remindme -c android.intent.category.LAUNCHER 1
	```
  

<br>

<br>
<br>
