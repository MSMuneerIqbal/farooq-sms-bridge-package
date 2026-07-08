package pk.farooq.smsbridge;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BridgeService extends Service {
    private static final String CHANNEL_ID = "farooq_sms_bridge_channel";
    private ScheduledExecutorService executor;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification("Connected. Selected SIM: " + Prefs.getSelectedSimLabel(this)));
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::pollPendingSms, 2, 5, TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void pollPendingSms() {
        try {
            String baseUrl = Prefs.getBaseUrl(this);
            String deviceId = Prefs.getDeviceId(this);
            String deviceSecret = Prefs.getDeviceSecret(this);
            int selectedSubId = Prefs.getSelectedSubId(this);

            if (baseUrl.isEmpty() || deviceId.isEmpty() || deviceSecret.isEmpty() || selectedSubId == -1) return;

            String heartbeatBody = new JSONObject()
                    .put("selectedSubId", selectedSubId)
                    .put("selectedSimLabel", Prefs.getSelectedSimLabel(this))
                    .put("status", "ONLINE")
                    .put("androidTimeMillis", System.currentTimeMillis())
                    .toString();
            NetworkClient.postJson(baseUrl + "/mobile-bridge/heartbeat", new JSONObject(heartbeatBody), deviceId, deviceSecret);

            String response = NetworkClient.getJson(baseUrl + "/mobile-bridge/sms/pending", deviceId, deviceSecret);
            JSONArray arr = new JSONArray(response);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject job = arr.getJSONObject(i);
                String smsId = job.getString("id");
                String phone = job.getString("phoneNumber");
                String body = job.getString("messageBody");

                StatusReporter.report(this, smsId, "SENDING", null);
                sendSms(smsId, phone, body, selectedSubId);
                Thread.sleep(1200); // basic anti-spam pacing; tune from desktop settings
            }
        } catch (Exception ignored) { }
    }

    private void sendSms(String smsId, String phoneNumber, String messageBody, int selectedSubId) {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            StatusReporter.report(this, smsId, "FAILED", "SEND_SMS permission not granted");
            return;
        }

        try {
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= 31) {
                smsManager = getSystemService(SmsManager.class).createForSubscriptionId(selectedSubId);
            } else {
                smsManager = SmsManager.getSmsManagerForSubscriptionId(selectedSubId);
            }

            ArrayList<String> parts = smsManager.divideMessage(messageBody);
            if (parts.size() <= 1) {
                PendingIntent sentPi = makePendingIntent(SmsSentReceiver.class, "SMS_SENT", smsId, 10);
                PendingIntent deliveredPi = makePendingIntent(SmsDeliveredReceiver.class, "SMS_DELIVERED", smsId, 20);
                smsManager.sendTextMessage(phoneNumber, null, messageBody, sentPi, deliveredPi);
            } else {
                ArrayList<PendingIntent> sentPis = new ArrayList<>();
                ArrayList<PendingIntent> deliveredPis = new ArrayList<>();
                for (int i = 0; i < parts.size(); i++) {
                    sentPis.add(makePendingIntent(SmsSentReceiver.class, "SMS_SENT", smsId, 100 + i));
                    deliveredPis.add(makePendingIntent(SmsDeliveredReceiver.class, "SMS_DELIVERED", smsId, 200 + i));
                }
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentPis, deliveredPis);
            }
        } catch (Exception e) {
            StatusReporter.report(this, smsId, "FAILED", e.getMessage());
        }
    }

    private PendingIntent makePendingIntent(Class<?> receiverClass, String action, String smsId, int requestOffset) {
        Intent intent = new Intent(this, receiverClass);
        intent.setAction(action);
        intent.putExtra("smsId", smsId);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(this, smsId.hashCode() + requestOffset, intent, flags);
    }

    private Notification buildNotification(String text) {
        Intent intent = new Intent(this, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(this, 2, intent, flags);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return builder
                .setContentTitle("Farooq SMS Bridge")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Farooq SMS Bridge",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }
}
