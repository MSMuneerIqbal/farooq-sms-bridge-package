# Testing Checklist

## Network test

- Laptop and phone are on same Wi-Fi.
- Desktop API listens on `0.0.0.0:3777`.
- Phone browser can open `http://LAPTOP_IP:3777/health` if you add a health endpoint.
- Windows Firewall allows the app on private network.

## Permission test

- Android app has SEND_SMS permission.
- Android app has READ_PHONE_STATE permission.
- Android 13+ notification permission granted.

## SIM test

- SIM list shows both SIMs.
- Select SIM 1 and send test SMS.
- Select SIM 2 and send test SMS.
- Remove selected SIM and verify bridge pauses/fails safely.

## SMS test

- One short English SMS.
- One long English SMS that becomes multipart.
- One Urdu/Unicode SMS.
- Invalid number.
- Phone has no balance/package.
- Airplane mode/no service.

## Desktop test

- Message row created as PENDING.
- Android fetches row.
- Row becomes SENDING.
- Row becomes SENT or FAILED.
- Retry failed message.
- Audit log is written.

## Safety test

- Duplicate message not sent twice.
- Bulk messages are rate-limited.
- Marketing message blocked without consent.
- Disabled device cannot fetch jobs.
