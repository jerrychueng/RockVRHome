package com.rockchip.vr.home.util;

import android.content.Intent;
import android.os.Bundle;

import java.util.Set;

/**
 * Created by yhc on 16-7-5.
 */
public class RockLog {

    public static final boolean DEBUG = true;
    public static final String TAG = "RockVRHome";

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

    /**
     * 打印Intent的内容
     */
    public static void dumpIntent(Intent i){
        if(i == null) {
            return;
        }
        Bundle bundle = i.getExtras();
        d("-----------------------dump intent---------------------------");
        d("action: " + i.getAction());
        d("-------------------------------------------------------------");
        d("extras:");
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            for (String key : keys) {
                d("    " + key + "=" + bundle.get(key));
            }
        } else {
            d("    null");
        }
        d("-------------------------------------------------------------");
    }
}
