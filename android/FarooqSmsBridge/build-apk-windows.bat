@echo off
REM Run this from the FarooqSmsBridge folder after opening/syncing in Android Studio.
REM If gradlew exists, this builds the debug APK.
if exist gradlew.bat (
  gradlew.bat assembleDebug
) else (
  echo gradlew.bat not found. Open this project in Android Studio first, let Gradle sync, then use Build APK.
)
pause
