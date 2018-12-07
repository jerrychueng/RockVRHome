package com.rockchip.vr.globalactions.util;

/**
 * Created by yhc on 16-7-5.
 */
public class RockLog {

    public static final boolean DEBUG = true;
    public static final String TAG = "RockVRGlobalAction";

    public static void d(String str) {
        if(DEBUG) {
            android.util.Log.d(TAG, str);
        }
    }

    public static void e(String str) {
        if(DEBUG) {
            android.util.Log.e(TAG, str);
        }
    }
}
