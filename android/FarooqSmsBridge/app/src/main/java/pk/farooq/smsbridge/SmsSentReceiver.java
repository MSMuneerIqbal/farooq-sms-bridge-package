package pk.farooq.smsbridge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SmsSentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String smsId = intent.getStringExtra("smsId");
        if (smsId == null) return;

        int result = getResultCode();
        if (result == Activity.RESULT_OK) {
            StatusReporter.report(context, smsId, "SENT", null);
        } else {
            String error;
            switch (result) {
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE: error = "Generic failure"; break;
                case SmsManager.RESULT_ERROR_NO_SERVICE: error = "No mobile service"; break;
                case SmsManager.RESULT_ERROR_NULL_PDU: error = "Null PDU"; break;
                case SmsManager.RESULT_ERROR_RADIO_OFF: error = "Radio off"; break;
                default: error = "SMS failed resultCode=" + result;
            }
            StatusReporter.report(context, smsId, "FAILED", error);
        }
    }
}
