package com.rockchip.vr.home.statusbar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.rockchip.vr.home.R;
import com.rockchip.vr.home.VRMainRenderer;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.ui.view.ImageView;

import org.rajawali3d.materials.textures.ATexture;

public class NetworkStatus extends StatusBarItem {

    private static final String NAME = "NetworkStatus";
    private ImageView mNetworkImage;

    private Context mContext;

    private boolean mIsConnected = false;
    private int mConnectType;
    private String mConnectTypeName;
    private String mWifiSSID;
    private int mWifiSignalLevel;
    private int mWifiSpeed;
    private String mWifiSpeedUnits;

    public NetworkStatus(Context context, VRMainRenderer vrMainRenderer) {
        super(context, vrMainRenderer, NAME);
        mContext = context;
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        setBroadcast(intentFilter);
    }

    @Override
    public void onUpdate(Intent intent) {
        RockLog.d("NetworkStatus - onUpdate");
        ConnectivityManager cm =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnectedOld = mIsConnected;
        int wifiSignalLevelOld = mWifiSignalLevel;
        if(activeNetwork != null) {
            mIsConnected = activeNetwork.isConnectedOrConnecting();
            mConnectType = activeNetwork.getType();
            mConnectTypeName = activeNetwork.getTypeName();
        } else {
            mIsConnected = false;
            mConnectTypeName = "";
        }
        if (mIsConnected && mConnectType == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getBSSID() != null) {
                mWifiSSID = wifiInfo.getSSID();
                mWifiSignalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
                mWifiSpeed = wifiInfo.getLinkSpeed();
                mWifiSpeedUnits = WifiInfo.LINK_SPEED_UNITS;
            }
        }
        RockLog.d("NetworkStatus - onUpdate - " + this.toString());
        if(mNetworkImage != null) {
            if (mIsConnected && !isConnectedOld) {
                RockLog.d("NetworkStatus - onUpdate - icon show connected state");
                if (mConnectType == ConnectivityManager.TYPE_WIFI) {
                    RockLog.d("NetworkStatus - onUpdate - network type is wifi, show wifi signal level");
                    mNetworkImage.updateImage(mContext, mVRMainRenderer, getWifiIcon(mIsConnected, mWifiSignalLevel));
                    mNetworkImage.delayVisible(10);
                } else {
                    RockLog.d("NetworkStatus - onUpdate - network type is others, show full signal level");
                    mNetworkImage.updateImage(mContext, mVRMainRenderer, getWifiIcon(mIsConnected, mWifiSignalLevel));
                    mNetworkImage.delayVisible(10);
                }
            } else if (!mIsConnected && isConnectedOld) {
                RockLog.d("NetworkStatus - onUpdate - icon show disconnected state");
                mNetworkImage.updateImage(mContext, mVRMainRenderer, getWifiIcon(mIsConnected, mWifiSignalLevel));
                mNetworkImage.delayVisible(10);
            } else if (mIsConnected && mConnectType == ConnectivityManager.TYPE_WIFI
                    && mWifiSignalLevel != wifiSignalLevelOld) {
                RockLog.d("NetworkStatus - onUpdate - icon onUpdate wifi signal level");
                mNetworkImage.updateImage(mContext, mVRMainRenderer, getWifiIcon(mIsConnected, mWifiSignalLevel));
                mNetworkImage.delayVisible(10);
            }
        }
    }

    @Override
    public String toString() {
        String str = "";
        str += " IsConnected:" + mIsConnected;
        str += " ConnectType: " + mConnectTypeName;
        if(mIsConnected && mConnectType == ConnectivityManager.TYPE_WIFI) {
            str += " WiFiInfo: ssid=" + mWifiSSID + ", signalLevel=" + mWifiSignalLevel
                    + ", speed=" + mWifiSpeed + " " + mWifiSpeedUnits;
        }
        return str;
    }

    @Override
    public void onDraw() {
        mPlane.setScale(0.47f, 0.47f, 1f);
//        mPlane.setPosition(-2f, 0f, 0f);
        mPlane.isContainer(true);
        mNetworkImage = new ImageView("network");
        try {
            mNetworkImage.setImage(mContext, R.drawable.icon_wifi_off);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mNetworkImage.addToContainer(mPlane);
    }

//    private int[] WIFI_LEVEL_PIC = {
//            R.drawable.ic_wifi_off, R.drawable.ic_wifi_1, R.drawable.ic_wifi_2,
//            R.drawable.ic_wifi_3, R.drawable.ic_wifi_4
//    };

    private int getWifiIcon(boolean isConnected, int wifiSignalLevel) {
        if(isConnected) {
            if(wifiSignalLevel <= 1) {
                return R.drawable.icon_wifi_on03;
            } else if(wifiSignalLevel <= 2) {
                return R.drawable.icon_wifi_on02;
            } else {
                return R.drawable.icon_wifi_on01;
            }
        } else {
            return R.drawable.icon_wifi_off;
        }
    }

}
