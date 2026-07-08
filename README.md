# Farooq SMS Bridge Package

This package contains a starter Android companion app plus NestJS desktop integration snippets for sending SMS from an Android phone SIM.

## What this package contains

```text
android/FarooqSmsBridge/
  Native Android Java project. Open this folder in Android Studio and build APK.

desktop-nestjs-snippets/
  Prisma models and NestJS controller/service snippets for your local API.

docs/
  Integration plan, API contract, testing checklist, and security checklist.
```

## Important limitation

The APK is not precompiled in this package. Build it using Android Studio on a machine with Android SDK/Gradle installed.

## Fast build steps

1. Open Android Studio.
2. File → Open → `android/FarooqSmsBridge`.
3. Let Android Studio sync Gradle.
4. Connect Android phone with USB debugging enabled.
5. Click Run to install a debug app.
6. For APK file: Build → Build Bundle(s) / APK(s) → Build APK(s).

## First test flow

1. Start your desktop NestJS API on LAN, not only `127.0.0.1`.
2. Use a URL like `http://192.168.1.50:3777` on the Android phone.
3. Pair phone using a temporary pairing token.
4. Grant SMS and phone-state permissions.
5. Refresh SIM list.
6. Select SIM 1 or SIM 2.
7. Start SMS Bridge Service.
8. Create one `sms_outbox` row from desktop.
9. Phone fetches the job and sends the SMS.
10. Phone posts SENT/FAILED status back to desktop.

## Production advice

- Use only for patient-consented appointment/follow-up messages.
- Keep foreground notification visible on Android.
- Add daily/per-minute limits.
- Do not use for spam or marketing without consent.
- Replace the simple header secret with HMAC request signing before production.
