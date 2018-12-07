package com.rockchip.vr.home.statusbar;

import android.content.Context;

import com.rockchip.vr.home.VRMainRenderer;
import com.rockchip.vr.home.util.RockLog;

import org.rajawali3d.primitives.Plane;
import org.rajawali3d.scene.Scene;

public class StatusBar {

    private Plane mPlane;
    private BatteryStatus mBatteryStatus;
    private NetworkStatus mNetworkStatus;
    private BluetoothStatus mBluetoothStatus;
    private TimeStatus mTimeStatus;
    private HeadsetStatus mHeadsetStatus;
    private StorageStatus mStorageStatus;

    public StatusBar(Context context, VRMainRenderer vrMainRenderer) {
        mBatteryStatus = new BatteryStatus(context, vrMainRenderer);
        mBatteryStatus.setPosition(1.6f, -0.6f, 0f);
        mNetworkStatus = new NetworkStatus(context, vrMainRenderer);
        mNetworkStatus.setPosition(-1.7f, -0.6f, 0f);
        mTimeStatus = new TimeStatus(context, vrMainRenderer);
        mTimeStatus.setPosition(0f, -1f, 0f);
        mBluetoothStatus = new BluetoothStatus(context, vrMainRenderer);
        mBluetoothStatus.setPosition(-1.25f, -0.6f, 0f);
        mHeadsetStatus = new HeadsetStatus(context, vrMainRenderer);
        mHeadsetStatus.setPosition(-2.5f, -0.6f, 0f);
        mStorageStatus = new StorageStatus(context, vrMainRenderer);
        mStorageStatus.setPosition(2.4f, -0.6f, 0f);
        onDraw();
    }

    public void onDraw() {
        mPlane = new Plane();
        mPlane.isContainer(true);
        mPlane.setPosition(0f, 3.0f, -8f);
        if(mBatteryStatus != null) {
            mPlane.addChild(mBatteryStatus.getPlane());
        }
        if(mNetworkStatus != null) {
            mPlane.addChild(mNetworkStatus.getPlane());
        }
        if(mTimeStatus != null) {
            mPlane.addChild(mTimeStatus.getPlane());
        }
        if(mBluetoothStatus != null){
            mPlane.addChild(mBluetoothStatus.getPlane());
        }
        if(null != mHeadsetStatus){
            mPlane.addChild(mHeadsetStatus.getPlane());
        }
        if(null != mStorageStatus){
            mPlane.addChild(mStorageStatus.getPlane());
        }
    }

    public void addToScene(Scene scene) {
        scene.addChild(mPlane);
    }

    public void onResume() {
        RockLog.d("StatusBar - onResume");
        if(mBatteryStatus!=null) {
            mBatteryStatus.onResume();
        }
        if(mNetworkStatus!=null) {
            mNetworkStatus.onResume();
        }
        if (mTimeStatus != null) {
            mTimeStatus.onResume();
        }
        if(mBluetoothStatus != null){
            mBluetoothStatus.onResume();
        }
        if(null != mHeadsetStatus){
            mHeadsetStatus.onResume();
        }
        if(null != mStorageStatus){
            mStorageStatus.onResume();
        }
    }

    public void onPause() {
        RockLog.d("StatusBar - onPause");
        if(mBatteryStatus!=null) {
            mBatteryStatus.onPause();
        }
        if(mNetworkStatus!=null) {
            mNetworkStatus.onPause();
        }
        if (mTimeStatus != null) {
            mTimeStatus.onPause();
        }
        if(mBluetoothStatus != null){
            mBluetoothStatus.onPause();
        }
        if(null != mHeadsetStatus){
            mHeadsetStatus.onPause();
        }
        if(null != mStorageStatus){
            mStorageStatus.onPause();
        }
    }
}
