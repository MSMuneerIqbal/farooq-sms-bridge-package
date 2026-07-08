# Android SMS Bridge Developer Instructions

## App name

`Farooq SMS Bridge`

## Purpose

The Android app receives SMS jobs from the hospital desktop application and sends them through the selected Android SIM.

## Permissions

The app uses:

```xml
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Screens in starter app

1. Request permissions.
2. Enter desktop API URL and pairing token.
3. Pair with desktop.
4. Refresh SIM list.
5. Select SIM.
6. Start/stop foreground bridge service.

## Dual SIM sending

The app lists SIMs with `SubscriptionManager.getActiveSubscriptionInfoList()` and stores the selected `subscriptionId`.

When sending SMS, it uses:

```java
SmsManager smsManager;
if (Build.VERSION.SDK_INT >= 31) {
    smsManager = getSystemService(SmsManager.class).createForSubscriptionId(selectedSubId);
} else {
    smsManager = SmsManager.getSmsManagerForSubscriptionId(selectedSubId);
}
```

This is the key part that allows sending from a specific SIM.

## Background bridge service

`BridgeService` runs as a foreground service and polls the desktop API every 5 seconds:

```text
GET /mobile-bridge/sms/pending
```

For every pending job:

```text
1. Mark status SENDING
2. Send SMS using selected SIM
3. SmsSentReceiver posts SENT or FAILED
4. SmsDeliveredReceiver posts DELIVERED if delivery callback is received
```

## Build APK

Open `android/FarooqSmsBridge` in Android Studio and build:

```text
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

Debug APK path is normally:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Before production

Replace simple `X-Device-Secret` header authentication with HMAC signing:

```text
X-Device-Id
X-Timestamp
X-Nonce
X-Signature = HMAC_SHA256(deviceSecret, method + path + bodyHash + timestamp + nonce)
```

Also add:

- token expiry for pairing
- one-time pairing tokens
- rate limits
- audit logs
- patient consent checks
- duplicate prevention
- template approval
