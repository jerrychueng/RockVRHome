package com.rockchip.vr.home.statusbar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;

import com.rockchip.vr.home.R;
import com.rockchip.vr.home.VRMainRenderer;
import com.rockchip.vr.ui.view.ImageView;
import com.rockchip.vr.ui.view.TextView;
import com.rockchip.vr.home.util.RockLog;

import org.rajawali3d.materials.textures.ATexture;

public class BatteryStatus extends StatusBarItem {

    private static final String NAME = "BatteryStatus";

    private int mLevel;
    private int mScale;
    private boolean mIsCharging = false;
    private static int mBatteryPercent = 100;

    private TextView mBatteryPctTextView;

    private ImageView mBatteryImage;
    private ImageView mBatteryChargeImage;

//    private int[] BATTERY_LEVEL_PIC = {R.drawable.ic_battery, R.drawable.ic_battery_1,
//            R.drawable.ic_battery_2, R.drawable.ic_battery_3, R.drawable.ic_battery_4,
//            R.drawable.ic_battery_5, R.drawable.ic_battery_6, R.drawable.ic_battery_7,
//            R.drawable.ic_battery_8, R.drawable.ic_battery_9, R.drawable.ic_battery_10,
//            R.drawable.ic_battery_10};

    private Context mContext;

    public BatteryStatus(Context context, VRMainRenderer vrMainRenderer) {
        super(context, vrMainRenderer, NAME);
        mContext = context;
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        setBroadcast(intentFilter);
    }

    @Override
    public void onUpdate(Intent batteryStatus) {
        if (batteryStatus == null) {
            RockLog.e("onUpdate - intent is null!");
            return;
        }
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == -1) {
            return;
        }
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
        if(status == BatteryManager.BATTERY_STATUS_FULL){
            int chargeType = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            if(BatteryManager.BATTERY_PLUGGED_AC == chargeType
                    || BatteryManager.BATTERY_PLUGGED_USB == chargeType
                    || BatteryManager.BATTERY_PLUGGED_WIRELESS == chargeType){
                isCharging = true;
            }
        }
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return;
        }
        update(isCharging, level, scale);
    }

    public int getBatteryIcon(boolean isCharging, int batteryPercent) {
        if (batteryPercent < 10) {
            return isCharging ? R.drawable.icon_battery_04 : R.drawable.icon_battery_empty;
        } else if (batteryPercent < 40) {
            return isCharging ? R.drawable.icon_battery_05 : R.drawable.icon_battery_01;
        } else if (batteryPercent < 80) {
            return isCharging ? R.drawable.icon_battery_06 : R.drawable.icon_battery_02;
        } else {
            return isCharging ? R.drawable.icon_battery_07 : R.drawable.icon_battery_03;
        }
    }

    public void update(boolean isCharging, int level, int scale) {
        int batteryPercent = 100 * level / scale;
        RockLog.d("BatteryStatus - onUpdate isCharging:"+isCharging
                + ", level: " + level + ", scale:"+scale + ", batteryPercent:"+batteryPercent);
        if (mBatteryPctTextView != null && batteryPercent != mBatteryPercent) {
            mBatteryPctTextView.setText(String.valueOf(batteryPercent));
//            mBatteryPctTextView.delayVisible(10);
        }
        if (mBatteryImage != null) {
            if (isCharging && !mBatteryChargeImage.getBasePlane().isVisible()) {
                RockLog.d("BatteryStatus - battery icon show power on icon");
                //mBatteryImage.updateImage(mContext, mVRMainRenderer, getBatteryIcon(isCharging, batteryPercent));
                //mBatteryImage.updateImage(mContext, mVRMainRenderer, R.drawable.ic_battery_01);
                //mBatteryImage.delayVisible(10);
                mBatteryChargeImage.setVisible(true);
            } else if (!isCharging && mBatteryChargeImage.getBasePlane().isVisible()) {
                RockLog.d("BatteryStatus - battery icon show battery level");
                //mBatteryImage.updateImage(mContext, mVRMainRenderer, R.drawable.ic_battery_00);
                //mBatteryImage.updateImage(mContext, mVRMainRenderer, getBatteryIcon(isCharging, batteryPercent));
                //mBatteryImage.delayVisible(10);
                mBatteryChargeImage.setVisible(false);
            }
        }
        mIsCharging = isCharging;
        mLevel = level;
        mScale = scale;
        mBatteryPercent = batteryPercent;
        RockLog.d("BatteryStatus - onUpdate - " + this.toString());
    }

    @Override
    public String toString() {
        return "BatteryPecent:" + mBatteryPercent + "(" + mLevel + "/" + mScale + ")"
                + " ischarging:" + mIsCharging;
    }

    @Override
    public void onDraw() {
        mPlane.setScale(0.65f, 0.65f, 1f);
//        mPlane.setPosition(2f, 0f, 0f);
        mPlane.isContainer(true);
        //
        mBatteryImage = new ImageView("batteryicon");
        try {
            mBatteryImage.setImage(mContext, R.drawable.ic_battery_00);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mBatteryImage.setPosition(0f, 0, 0f);
//        mBatteryImage.setScale(1.1, 1.1, 1);
        mBatteryImage.addToContainer(mPlane);
        //充电闪电图标
        mBatteryChargeImage = new ImageView("batteryCharge");
        try {
            mBatteryChargeImage.setImage(mContext, R.drawable.ic_battery_charge);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mBatteryChargeImage.setPosition(-0.65f, -0.05, 0f);
        mBatteryChargeImage.setScale(0.25, 0.8, 1);
        mBatteryChargeImage.addToContainer(mPlane);
        mBatteryChargeImage.setVisible(false);
        //
        mBatteryPctTextView = new TextView("batterytext", String.valueOf(mBatteryPercent));
        mBatteryPctTextView.setTextConfig(Color.WHITE, 50, 120, 150, 1);
        mBatteryPctTextView.setPosition(-0.05f, -0.32f, 0f);
//        mBatteryPctTextView.setScale(0.8f, 0.8f, 1f);
        mBatteryPctTextView.addToContainer(mPlane);
    }

}
