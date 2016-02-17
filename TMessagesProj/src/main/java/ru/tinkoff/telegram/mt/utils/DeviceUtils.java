package ru.tinkoff.telegram.mt.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


import java.util.UUID;


/**
 * @author Igor Bubelov
 */
public class DeviceUtils {
    private static final String TAG = DeviceUtils.class.getName();

    private static final String INSTALLATION_ID_KEY = "installation_id";

    private static final String[] androidIdBlacklist = new String[] { "9774d56d682e549c" };

    private static String deviceId;

    public static String getDeviceId(Context context) {
        if(deviceId == null) {
            deviceId = resolveDeviceId(context);
        }
        return deviceId;
    }

    /**
     * Requires runtime permission {@link android.Manifest.permission#READ_PHONE_STATE}
     */
    private static String resolveDeviceId(Context context) {

        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (!TextUtils.isEmpty(androidId) && !isBlacklisted(androidId)) {
            return androidId;
        }

        String imei = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();

        } catch (SecurityException e) {
            // ignore
        }

        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }

        String installationId = getInstallationId(context);

        return installationId;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            manufacturer = Character.toUpperCase(manufacturer.charAt(0)) + manufacturer.substring(1);
            return manufacturer + " " + model;
        }
    }

    private static boolean isBlacklisted(String androidId) {
        for (String blacklistedId : androidIdBlacklist) {
            if (androidId.equals(blacklistedId)) {
                return true;
            }
        }

        return false;
    }

    private static String getInstallationId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("did", Context.MODE_PRIVATE);

        String installId = prefs.getString(INSTALLATION_ID_KEY, null);
        if(installId == null) {
            installId = UUID.randomUUID().toString();
            prefs.edit().putString(INSTALLATION_ID_KEY, installId);
        }
        return installId;
    }
}
