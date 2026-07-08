# Security Checklist

## Minimum security for first clinic deployment

- Pairing token expires in 2 minutes.
- Pairing token can be used one time only.
- Store only hash of device secret in SQLite.
- Device can be disabled from desktop settings.
- Every SMS has audit log.
- Every SMS stores exact message body.
- Every SMS stores operator user id.
- Every SMS validates patient consent.
- Android service shows permanent notification.
- Do not expose API on public internet.
- Bind to LAN only.
- Add Windows Firewall private network rule only.

## Better production security

Replace simple secret header with HMAC:

```text
X-Device-Id
X-Timestamp
X-Nonce
X-Body-Hash
X-Signature
```

Signature:

```text
HMAC_SHA256(deviceSecret, method + path + timestamp + nonce + bodyHash)
```

Reject requests when:

```text
Timestamp older than 2 minutes
Nonce already used
Device disabled
Signature invalid
```
