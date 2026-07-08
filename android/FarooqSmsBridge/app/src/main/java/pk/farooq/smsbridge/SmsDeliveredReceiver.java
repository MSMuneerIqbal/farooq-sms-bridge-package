package pk.farooq.smsbridge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsDeliveredReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String smsId = intent.getStringExtra("smsId");
        if (smsId == null) return;
        if (getResultCode() == Activity.RESULT_OK) {
            StatusReporter.report(context, smsId, "DELIVERED", null);
        }
    }
}
