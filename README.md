<p align="left">
	<img width="240" src="https://raw.githubusercontent.com/dsckiet/resources/master/dsckiet-logo.png" />
	<h2 align="left"> Covid Tracker App </h2>
	<h4 align="left"> Covid Tracker is an Android App that provides to its users, information about current COVID-19 case counts at India as well as Global level using various forms of statistical data interpretation methods. <h4>
</p>

---

## Functionalities
- [ ] Add location based reminder using Google Map Platform and Geofencing.
- [ ] Add time based reminder using Alarm Manager.
- [ ] Add Notes Section using Room persistance library.
<br>
	
## Instructions to run

* Pre-requisites:
	-  Android Studio v4.0+
	-  A working Android physical device or emulator with USB debugging enabled

* Directions to setup/install
	- Clone this repository to your local folder using Git bash:
	```bash
	git clone https://github.com/dsckiet/covid-tracker-android-app.git
	```
	- Open this project from Android Studio
	- Connect to an Android physical device or emulator
	- To install the app into your device, run the following using command line tools
	```bash
	gradlew installDebug
	```

* Directions to execute
	-  To launch hands free, run the following using command line tools
	```bash
	adb shell monkey -p tech.jhavidit.remindme -c android.intent.category.LAUNCHER 1
	```
  

<br>

<br>
<br>
