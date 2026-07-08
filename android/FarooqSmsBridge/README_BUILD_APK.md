# Build APK

This folder is an Android Studio project.

## Option 1: Android Studio

1. Open Android Studio.
2. File → Open → select this `FarooqSmsBridge` folder.
3. Wait for Gradle sync.
4. Build → Build Bundle(s) / APK(s) → Build APK(s).

## Option 2: Command line after Gradle wrapper exists

```powershell
.\gradlew.bat assembleDebug
```

Debug APK output:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Install APK:

```powershell
adb install app\build\outputs\apk\debug\app-debug.apk
```
