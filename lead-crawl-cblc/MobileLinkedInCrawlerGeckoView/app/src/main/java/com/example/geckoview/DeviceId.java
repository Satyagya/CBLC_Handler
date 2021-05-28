package com.example.geckoview;

import android.content.Context;
import android.provider.Settings;

public class DeviceId {

    public static String deviceId(Context myContext) {
        return Settings.Secure.getString(myContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}
