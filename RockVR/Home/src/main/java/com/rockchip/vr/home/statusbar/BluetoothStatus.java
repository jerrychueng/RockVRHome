package com.rockchip.vr.home.statusbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.rockchip.vr.home.R;
import com.rockchip.vr.home.VRMainRenderer;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.ui.view.ImageView;

import org.rajawali3d.materials.textures.ATexture;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

public class BluetoothStatus extends StatusBarItem {

    private static final String NAME = "BluetoothStatus";

    private final boolean SHOW_ONLY_OPEN = false;
    private ImageView mBluetoothImage;
    private Context mContext;

    private boolean mIsBluetoothOpenOld = false;
    private BluetoothAdapter mAdapter;

    public BluetoothStatus(Context context, VRMainRenderer vrMainRenderer) {
        super(context, vrMainRenderer, NAME);
        mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //ACTION_CONNECTION_STATE_CHANGED不一定要有
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        setBroadcast(intentFilter);
    }

    @Override
    public void onUpdate(Intent intent) {
        RockLog.d("BluetoothStatus - onUpdate");
        if(null != intent){
            RockLog.d("BluetoothStatus - intent "+intent.getAction());
        }
        if (mBluetoothImage != null) {
            if (null == mAdapter) {
                mAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            Set<BluetoothDevice> lists = mAdapter.getBondedDevices();
            boolean isOpen = false;
            if (SHOW_ONLY_OPEN) {
                isOpen = BluetoothAdapter.STATE_ON == mAdapter.getState();
            } else {
                Iterator<BluetoothDevice> it = lists.iterator();
                if (null != it) {
                    while (it.hasNext()) {
                        BluetoothDevice device = it.next();
                        int deviceClass = device.getBluetoothClass().getMajorDeviceClass();
                        if (BluetoothClass.Device.Major.COMPUTER != deviceClass
                                && BluetoothClass.Device.Major.PHONE != deviceClass) {
                            if (isDeviceConnected(device)) {
                                isOpen = true;
                            }
                            break;
                        }
                    }
                }
            }
            if (mIsBluetoothOpenOld && !isOpen) {
                mIsBluetoothOpenOld = isOpen;
                RockLog.d("BluetoothStatus - onUpdate - icon show bluetooth close state");
                mBluetoothImage.updateImage(mContext, mVRMainRenderer, R.drawable.icon_bluetooth_off);
                mBluetoothImage.delayVisible(10);
            } else if (!mIsBluetoothOpenOld && isOpen) {
                RockLog.d("BluetoothStatus - onUpdate - icon show bluetooth open state");
                mIsBluetoothOpenOld = isOpen;
                mBluetoothImage.updateImage(mContext, mVRMainRenderer, R.drawable.icon_bluetooth_on);
                mBluetoothImage.delayVisible(10);
            }
        }
    }

    @Override
    public void onDraw() {
        mPlane.setScale(0.45f, 0.45f, 1f);
        mPlane.isContainer(true);
        mBluetoothImage = new ImageView("bluetooth");
        try {
            mIsBluetoothOpenOld = false;
            mBluetoothImage.setImage(mContext, R.drawable.icon_bluetooth_off);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mBluetoothImage.addToContainer(mPlane);
    }

    private boolean isDeviceConnected(BluetoothDevice device) {
        try {
            Class clz = device.getClass();
            Method connectMedhod = clz.getMethod("isConnected");
            boolean isConnected = (boolean) connectMedhod.invoke(device, new Object[]{});
            return isConnected;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
