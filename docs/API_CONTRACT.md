# API Contract

## Pair device

`POST /mobile-bridge/pair`

Request:

```json
{
  "pairingToken": "123456",
  "deviceName": "Samsung SM-A528B"
}
```

Response:

```json
{
  "deviceId": "device_cuid",
  "deviceSecret": "generated_secret",
  "clinicName": "Farooq EyeCare Hospital"
}
```

## Heartbeat

`POST /mobile-bridge/heartbeat`

Headers:

```text
X-Device-Id: device_cuid
X-Device-Secret: generated_secret
```

Request:

```json
{
  "selectedSubId": 3,
  "selectedSimLabel": "SIM Slot 1 | Jazz | subId=3",
  "status": "ONLINE",
  "androidTimeMillis": 1783510000000
}
```

Response:

```json
{ "ok": true }
```

## Get pending SMS

`GET /mobile-bridge/sms/pending`

Headers:

```text
X-Device-Id: device_cuid
X-Device-Secret: generated_secret
```

Response:

```json
[
  {
    "id": "sms_cuid",
    "phoneNumber": "+923001234567",
    "messageBody": "Your appointment is confirmed for 10 July 2026 at 5:00 PM."
  }
]
```

## Update SMS status

`POST /mobile-bridge/sms/status`

Headers:

```text
X-Device-Id: device_cuid
X-Device-Secret: generated_secret
```

Request:

```json
{
  "smsId": "sms_cuid",
  "status": "SENT",
  "errorMessage": null,
  "androidTimeMillis": 1783510000000
}
```

Statuses:

```text
SENDING
SENT
DELIVERED
FAILED
```
