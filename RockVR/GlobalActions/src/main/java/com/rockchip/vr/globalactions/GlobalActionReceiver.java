package com.rockchip.vr.globalactions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rockchip.vr.globalactions.util.RockLog;


public class GlobalActionReceiver extends BroadcastReceiver {
    public GlobalActionReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        RockLog.d("=============== GlobalActionReceiver - onReceive ======================");
        Intent intent1 = new Intent(context, VRMainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.setAction(intent.getAction());
        intent1.putExtra("action", intent1.getAction());
        context.startActivity(intent1);
    }
}
