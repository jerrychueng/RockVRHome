package com.rockchip.vr.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rockchip.vr.home.util.RockLog;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {
    public ShutdownBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        RockLog.d("=============== ShutdownBroadcastReceiver - onReceive ======================");
        Intent intent1 = new Intent(context, VRMainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.setAction(intent.getAction());
        intent1.putExtra("action", intent1.getAction());
        context.startActivity(intent1);
    }
}
