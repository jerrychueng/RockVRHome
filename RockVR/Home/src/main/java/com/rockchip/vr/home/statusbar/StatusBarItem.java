package com.rockchip.vr.home.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureStroke;

import com.rockchip.vr.home.VRMainRenderer;
import com.rockchip.vr.home.util.RockLog;

import org.rajawali3d.primitives.Plane;

public abstract class StatusBarItem {

    protected Plane mPlane;
    protected BroadcastReceiver mBroadcastReceiver;
    protected IntentFilter mIntentFilter;

    private String mName;
    private Context mContext;
    protected VRMainRenderer mVRMainRenderer;
    private boolean mIsRegBroadcast = false;

    public StatusBarItem(Context context, VRMainRenderer vrMainRenderer, String name) {
        mContext = context;
        mVRMainRenderer = vrMainRenderer;
        mName = name;
        mPlane = new Plane();
        onDraw();
    }

    protected void setBroadcast(IntentFilter intentFilter) {
        mIntentFilter = intentFilter;
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                RockLog.d(mName + " - Broadcast onReceive - action:" + intent.getAction());
                RockLog.dumpIntent(intent);
                onUpdate(intent);
            }
        };
        regBroadcast();
    }

    public void onResume() {
        RockLog.d(mName + " - onResume");
        regBroadcast();
    }

    public void onPause() {
        RockLog.d(mName + " - onPause");
        unregBroadcast();
    }

    public abstract void onUpdate(Intent intent);

    public abstract void onDraw();

    private void regBroadcast() {
        if (!mIsRegBroadcast) {
            mIsRegBroadcast = true;
            RockLog.d(mName + " - register broadcast");
            if(mBroadcastReceiver == null) {

            }
            Intent intent = mContext.registerReceiver(mBroadcastReceiver, mIntentFilter);
            onUpdate(intent);
        }
    }

    private void unregBroadcast() {
        if(mIsRegBroadcast) {
            mIsRegBroadcast = false;
            RockLog.d(mName + " - unregister broadcast");
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public Plane getPlane() {
        return mPlane;
    }

    public void setPosition(double x, double y, double z) {
        mPlane.setPosition(x, y, z);
    }

}
