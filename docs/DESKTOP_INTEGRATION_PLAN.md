# Desktop Integration Plan for Farooq EyeCare Hospital App

Your current stack is Electron + React + Tailwind desktop UI, local NestJS API, Prisma + SQLite database.

## Required backend changes

### 1. Make local API available on LAN

Currently development may use:

```text
http://127.0.0.1:3777
```

For Android phone access, NestJS must listen on:

```text
0.0.0.0:3777
```

The phone will connect using laptop LAN IP:

```text
http://192.168.x.x:3777
```

Example NestJS main.ts:

```ts
await app.listen(3777, '0.0.0.0');
```

Windows Firewall must allow Node/NestJS on port `3777` for private network.

## Required database tables

Add:

```text
SmsOutbox
SmsBridgeDevice
SmsAuditLog
```

See `desktop-nestjs-snippets/prisma-sms-models.prisma`.

## Required desktop screens

### Message Center

In patient profile and appointment screen, add:

```text
Send SMS
- Appointment confirmation
- Appointment reminder
- Report ready
- Follow-up reminder
- Custom message, admin only
```

### SMS Outbox screen

Show:

```text
Pending
Sending
Sent
Delivered
Failed
Retry button
Cancel button
```

### SMS Bridge settings screen

Show:

```text
Pair Android phone
Generate pairing token / QR
Connected device name
Selected SIM from Android heartbeat
Last seen time
Enable/disable bridge
Daily SMS limit
```

## API endpoints

Use these endpoints:

```text
POST /mobile-bridge/pair
POST /mobile-bridge/heartbeat
GET  /mobile-bridge/sms/pending
POST /mobile-bridge/sms/status
```

## SMS sending policy

Do not send directly from the UI. Always create an outbox job:

```text
UI click → NestJS validates → sms_outbox row PENDING → Android sends → Android updates status
```

## Recommended safe defaults

```text
Max pending jobs returned to phone: 3
Minimum gap between SMS sends: 1.2 seconds
Max per minute: 20
Max per day: configurable
Allowed by default: appointment and follow-up messages
Marketing SMS: disabled unless consent exists
```
