package pk.farooq.smsbridge;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String NAME = "farooq_sms_bridge";

    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static String getBaseUrl(Context context) {
        return get(context).getString("baseUrl", "");
    }

    public static String getDeviceId(Context context) {
        return get(context).getString("deviceId", "");
    }

    public static String getDeviceSecret(Context context) {
        return get(context).getString("deviceSecret", "");
    }

    public static int getSelectedSubId(Context context) {
        return get(context).getInt("selectedSubId", -1);
    }

    public static String getSelectedSimLabel(Context context) {
        return get(context).getString("selectedSimLabel", "Not selected");
    }
}
