package com.rockchip.vr.home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;

import com.rockchip.vr.home.model.ProtocolXWYF000Normal;
import com.rockchip.vr.home.util.RockLog;

import org.rajawali3d.vr.VRActivity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VRMainActivity extends VRActivity {

    private static final float MAX_ANGLE = 180;
    private VRMainRenderer mVRMainRenderer;
    private ThemeReceiver mReceiver;

    private BluetoothAdapter mBluetoothAdapter;
    public static final String WXYF000_SERVICE_NORMAL = "0000fe55-0000-1000-8000-00805f9b34fb";
    public static final String WXYF001_CHAR_NORMAL_NOTIFY = "00000001-1000-1000-8000-00805f9b34fb";
    public static final String WXYF002_CHAR_NORMAL_WRITE = "00000001-1000-1000-8000-00805f9b34fb";
    public static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private BluetoothGatt bluetoothGatt;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        RockLog.d("VRMainActivity - onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        android.provider.Settings.System.putInt(this.getContentResolver(),
//                android.provider.Settings.System.SCREEN_BRIGHTNESS, 125);

        mVRMainRenderer = new VRMainRenderer(this,getSurfaceView());
        setRenderer(mVRMainRenderer);

        setConvertTapIntoTrigger(true);
        IntentFilter filter = new IntentFilter(); //theme 广播
        filter.addAction("com.rockchip.setting.themechange");
        filter.addAction("com.rockchip.setting.themedelete");
        mReceiver = new ThemeReceiver();
        registerReceiver(mReceiver, filter);

        final BluetoothManager bluetoothManager = // bluetooth
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private class GattConnectCallback extends BluetoothGattCallback {
        private BluetoothGatt mGatt;
        private List<BluetoothGattService> mServices;
        public void onConnectionStateChange(BluetoothGatt gatt,
                                            int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            }
        }
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mGatt = gatt;
            mServices = mGatt.getServices();
            for (BluetoothGattService s : mServices) {
                String uuid_s = s.getUuid().toString();
                if (uuid_s.equals(WXYF000_SERVICE_NORMAL)) {
                    for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                        if (c.getUuid().toString().equals(WXYF001_CHAR_NORMAL_NOTIFY)) {
                            mGatt.setCharacteristicNotification(c, true);
                            BluetoothGattDescriptor descriptor = c.getDescriptor(
                                    CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            Boolean Passed = mGatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();
            ProtocolXWYF000Normal protocol = new ProtocolXWYF000Normal(data);
            VRMainActivity.this.pro = protocol;
            movePointer(pro.pitch,protocol.roll);
        }
    }

    private ProtocolXWYF000Normal pro ;
    public ProtocolXWYF000Normal getProtocol(){
        return pro;
    }

    private void movePointer(float pitch ,float roll){
        if((Math.abs(roll) <= MAX_ANGLE)){
            int deltaX = (int) ((roll<0 ?22:-22) * roll / MAX_ANGLE);
            indexX = deltaX;
        } else if (roll > MAX_ANGLE) {
            indexX = 22;
        } else {
            indexX = -22;
        }

        if((Math.abs(pitch) <= MAX_ANGLE)){
            int deltaY = (int) ((pitch<0 ?22:-22) * pitch / MAX_ANGLE);
            indexY = deltaY;
        } else if (pitch > MAX_ANGLE) {
            indexY = 22;
        } else {
            indexY = -22;
        }
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
        RockLog.d("onCardboardTrigger");
        if (mVRMainRenderer != null) {
            mVRMainRenderer.onEnter();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        RockLog.d("onKeyDown - keycode: " + keyCode);
        RockLog.d("onKeyDown - keyevent: " + event.toString());
        if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (mVRMainRenderer != null) {
                mVRMainRenderer.onEnter();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mVRMainRenderer.onReturn()) {
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_MOVE_HOME){
            getCardboardView().resetHeadTracker();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        RockLog.d("VRMainActivity - onPause");
        super.onPause();
        if (mVRMainRenderer != null) {
            mVRMainRenderer.pause();
        }
        if(bluetoothGatt!= null){
            bluetoothGatt.disconnect();
//            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        RockLog.d("VRMainActivity - onResume");
        if(mVRMainRenderer != null) {
            mVRMainRenderer.resume();
        }

        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice d : devices) {
            bluetoothGatt = d.connectGatt(this, true, new GattConnectCallback());
        }
    }

    public int  indexX = 0 ;
    public int indexY = 0 ;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("TAG","keycode"+ event.getKeyCode());
        switch(event.getKeyCode()){
            case 131: //右
                indexX -- ;
                break;
            case 21: //左
                indexX++;
                break;
            case 20://下
                indexY--;
                break;
            case 19://上
                indexY++;
                break;
        }
        Log.d("TAG","the index x"+ indexX + " index y "+ indexY);
        return super.onKeyUp(keyCode, event);
    }


    //    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        RockLog.d("VRMainActivity - onNewIntent");
//        if(intent != null ) {
//            String action = intent.getAction();
//            RockLog.d("VRMainActivity - action:" + action);
//            if(ACTION_START_SHUTDOWN_DIALOG.equals(action)) {
////                if(mVRMainRenderer != null) {
////                    mVRMainRenderer.setNeedShowShutdownDialog();
////                }
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        RockLog.d("VRMainActivity - onDestroy");
        super.onDestroy();
        if(mVRMainRenderer != null) {
            mVRMainRenderer.destory();
        }
    }
    public class ThemeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.rockchip.setting.themechange".equals(action)) {
                mVRMainRenderer.changeTheme(Settings.Global.getString(getContentResolver(), "themechange"), 0);
            }
            if ("com.rockchip.setting.themedelete".equals(action)) {
                Log.d("THEMEDELETE", "themedelete is receive ");

                mVRMainRenderer.changeTheme(Settings.Global.getString(getContentResolver(), "themechange"), R.drawable.bg2);
            }
        }
    }

}
