package pk.farooq.smsbridge;

import android.content.Context;

import org.json.JSONObject;

public class StatusReporter {
    public static void report(Context context, String smsId, String status, String errorMessage) {
        new Thread(() -> {
            try {
                String baseUrl = Prefs.getBaseUrl(context);
                String deviceId = Prefs.getDeviceId(context);
                String deviceSecret = Prefs.getDeviceSecret(context);
                if (baseUrl.isEmpty() || deviceId.isEmpty() || deviceSecret.isEmpty()) return;

                JSONObject body = new JSONObject();
                body.put("smsId", smsId);
                body.put("status", status);
                if (errorMessage != null) body.put("errorMessage", errorMessage);
                body.put("androidTimeMillis", System.currentTimeMillis());
                NetworkClient.postJson(baseUrl + "/mobile-bridge/sms/status", body, deviceId, deviceSecret);
            } catch (Exception ignored) { }
        }).start();
    }
}
