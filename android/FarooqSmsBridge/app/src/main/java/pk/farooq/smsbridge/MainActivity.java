package pk.farooq.smsbridge;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST = 1001;
    private TextView statusText;
    private EditText baseUrlInput;
    private EditText pairingTokenInput;
    private RadioGroup simGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        updateStatus();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Farooq SMS Bridge");
        title.setTextSize(24);
        root.addView(title);

        statusText = new TextView(this);
        statusText.setTextSize(15);
        statusText.setPadding(0, 24, 0, 24);
        root.addView(statusText);

        baseUrlInput = new EditText(this);
        baseUrlInput.setHint("Desktop API URL e.g. http://192.168.1.50:3777");
        baseUrlInput.setSingleLine(true);
        baseUrlInput.setText(Prefs.get(this).getString("baseUrl", ""));
        root.addView(baseUrlInput);

        pairingTokenInput = new EditText(this);
        pairingTokenInput.setHint("Pairing token from desktop QR/manual screen");
        pairingTokenInput.setSingleLine(true);
        root.addView(pairingTokenInput);

        Button permissionBtn = new Button(this);
        permissionBtn.setText("1. Request Permissions");
        permissionBtn.setOnClickListener(v -> requestNeededPermissions());
        root.addView(permissionBtn);

        Button pairBtn = new Button(this);
        pairBtn.setText("2. Pair With Desktop");
        pairBtn.setOnClickListener(v -> pairWithDesktop());
        root.addView(pairBtn);

        TextView simTitle = new TextView(this);
        simTitle.setText("3. Select SIM for Sending SMS");
        simTitle.setTextSize(18);
        simTitle.setPadding(0, 24, 0, 8);
        root.addView(simTitle);

        simGroup = new RadioGroup(this);
        root.addView(simGroup);

        Button refreshSimsBtn = new Button(this);
        refreshSimsBtn.setText("Refresh SIM List");
        refreshSimsBtn.setOnClickListener(v -> loadSims());
        root.addView(refreshSimsBtn);

        Button saveSimBtn = new Button(this);
        saveSimBtn.setText("Save Selected SIM");
        saveSimBtn.setOnClickListener(v -> saveSelectedSim());
        root.addView(saveSimBtn);

        Button startBtn = new Button(this);
        startBtn.setText("4. Start SMS Bridge Service");
        startBtn.setOnClickListener(v -> startBridgeService());
        root.addView(startBtn);

        Button stopBtn = new Button(this);
        stopBtn.setText("Stop SMS Bridge Service");
        stopBtn.setOnClickListener(v -> stopService(new Intent(this, BridgeService.class)));
        root.addView(stopBtn);

        setContentView(scroll);
    }

    private void requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Build.VERSION.SDK_INT >= 33 ? Manifest.permission.POST_NOTIFICATIONS : Manifest.permission.INTERNET
            }, PERMISSION_REQUEST);
        }
    }

    private boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < 23 || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void pairWithDesktop() {
        String baseUrl = baseUrlInput.getText().toString().trim();
        String token = pairingTokenInput.getText().toString().trim();
        if (baseUrl.isEmpty() || token.isEmpty()) {
            toast("Enter desktop API URL and pairing token first.");
            return;
        }

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("pairingToken", token);
                body.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL);
                String response = NetworkClient.postJson(baseUrl + "/mobile-bridge/pair", body, null, null);
                JSONObject json = new JSONObject(response);

                SharedPreferences.Editor editor = Prefs.get(this).edit();
                editor.putString("baseUrl", baseUrl);
                editor.putString("deviceId", json.getString("deviceId"));
                editor.putString("deviceSecret", json.getString("deviceSecret"));
                editor.apply();

                runOnUiThread(() -> {
                    toast("Paired successfully.");
                    updateStatus();
                });
            } catch (Exception e) {
                runOnUiThread(() -> toast("Pairing failed: " + e.getMessage()));
            }
        }).start();
    }

    private void loadSims() {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            toast("READ_PHONE_STATE permission is required.");
            requestNeededPermissions();
            return;
        }
        simGroup.removeAllViews();
        try {
            SubscriptionManager sm = (SubscriptionManager) getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> sims = sm.getActiveSubscriptionInfoList();
            if (sims == null || sims.isEmpty()) {
                toast("No active SIM found.");
                return;
            }
            int savedSubId = Prefs.getSelectedSubId(this);
            for (SubscriptionInfo sim : sims) {
                RadioButton rb = new RadioButton(this);
                String label = "SIM Slot " + (sim.getSimSlotIndex() + 1)
                        + " | " + sim.getCarrierName()
                        + " | subId=" + sim.getSubscriptionId();
                rb.setText(label);
                rb.setTag(sim.getSubscriptionId() + "|" + label + "|" + sim.getSimSlotIndex());
                if (sim.getSubscriptionId() == savedSubId) rb.setChecked(true);
                simGroup.addView(rb);
            }
        } catch (SecurityException e) {
            toast("Permission denied for SIM list.");
        }
    }

    private void saveSelectedSim() {
        int checkedId = simGroup.getCheckedRadioButtonId();
        if (checkedId == View.NO_ID) {
            toast("Select a SIM first.");
            return;
        }
        RadioButton rb = findViewById(checkedId);
        String[] parts = rb.getTag().toString().split("\\|", 3);
        int subId = Integer.parseInt(parts[0]);
        String label = parts[1];
        int slot = Integer.parseInt(parts[2]);
        Prefs.get(this).edit()
                .putInt("selectedSubId", subId)
                .putString("selectedSimLabel", label)
                .putInt("selectedSlot", slot)
                .apply();
        toast("Selected: " + label);
        updateStatus();
    }

    private void startBridgeService() {
        if (!hasPermission(Manifest.permission.SEND_SMS)) {
            toast("SEND_SMS permission is required.");
            requestNeededPermissions();
            return;
        }
        if (Prefs.getDeviceId(this).isEmpty() || Prefs.getSelectedSubId(this) == -1) {
            toast("Pair with desktop and select SIM first.");
            return;
        }
        Intent intent = new Intent(this, BridgeService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent); else startService(intent);
        toast("SMS Bridge started.");
    }

    private void updateStatus() {
        String status = "Desktop URL: " + (Prefs.getBaseUrl(this).isEmpty() ? "Not paired" : Prefs.getBaseUrl(this))
                + "\nDevice ID: " + (Prefs.getDeviceId(this).isEmpty() ? "Not paired" : Prefs.getDeviceId(this))
                + "\nSelected SIM: " + Prefs.getSelectedSimLabel(this);
        statusText.setText(status);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
