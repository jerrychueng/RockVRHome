package com.rockchip.vr.home.statusbar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;

import com.rockchip.vr.home.VRMainRenderer;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.ui.view.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeStatus extends StatusBarItem {

    private static final String NAME = "TimeStatus";
    private String mCurTime;
    private TextView mTimeTextView;

    public TimeStatus(Context context, VRMainRenderer vrMainRenderer) {
        super(context, vrMainRenderer, NAME);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        setBroadcast(intentFilter);
    }

    @Override
    public void onUpdate(Intent intent) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
//        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        Date curDate = new Date(System.currentTimeMillis());
        mCurTime = formatter.format(curDate);
        if(mTimeTextView != null) {
            mTimeTextView.setText(mCurTime);
            mTimeTextView.delayVisible(15);
            System.gc();
            RockLog.d("TimeStatus - onUpdate time textview:" + mCurTime);
        }
    }

    @Override
    public void onDraw() {
        mPlane.isContainer(true);
        mPlane.setScale(1.3f, 1.3f, 1f);
        //mPlane.setPosition(0f, -1f, -5f);
        mTimeTextView = new TextView("Time", "");
        mTimeTextView.setTextConfig(Color.WHITE, 50, 130, 150, 0);
        mTimeTextView.addToContainer(mPlane);
    }

}
