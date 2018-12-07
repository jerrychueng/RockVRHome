package com.rockchip.vr.home.statusbar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.rockchip.vr.home.R;
import com.rockchip.vr.home.VRMainRenderer;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.home.util.StorageUtils;
import com.rockchip.vr.ui.view.ImageView;

import org.rajawali3d.materials.textures.ATexture;

import java.io.File;

public class StorageStatus extends StatusBarItem {

    private static final String NAME = "StorageStatus";
    private ImageView mSdcardImage;
    private ImageView mUsbImage;
    private Context mContext;

    public StorageStatus(Context context, VRMainRenderer vrMainRenderer) {
        super(context, vrMainRenderer, NAME);
        mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addDataScheme("file");
        setBroadcast(intentFilter);
    }

    @Override
    public void onUpdate(Intent intent) {
        RockLog.d(NAME + " - onUpdate");
        String[] paths = StorageUtils.getStoragePath(mContext);
        showSdcardIcon(null != paths[1] && new File(paths[1]).exists());
        showUsbIcon(null != paths[2] && new File(paths[2]).exists());
    }

    @Override
    public void onDraw() {
        mPlane.setScale(0.45f, 0.45f, 1f);
        mPlane.isContainer(true);
        mSdcardImage = new ImageView("sdcardstatus");
        try {
            mSdcardImage.setImage(mContext, R.drawable.icon_sdcard);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mSdcardImage.addToContainer(mPlane);
        //usb
        mUsbImage = new ImageView("usbstatus");
        mUsbImage.setScale(0.74f, 0.74f, 1f);
        mUsbImage.setPosition(0.65, 0, 0);
        try {
            mUsbImage.setImage(mContext, R.drawable.icon_usb);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mUsbImage.addToContainer(mPlane);
    }

    private void showSdcardIcon(boolean show) {
        if (null != mSdcardImage) {
            if (mSdcardImage.getBasePlane().isVisible() && !show) {
                mSdcardImage.setVisible(false);
            } else if (!mSdcardImage.getBasePlane().isVisible() && show) {
                mSdcardImage.setVisible(true);
            }
        }
    }

    private void showUsbIcon(boolean show) {
        if (null != mUsbImage) {
            if (mUsbImage.getBasePlane().isVisible() && !show) {
                mUsbImage.setVisible(false);
            } else if (!mUsbImage.getBasePlane().isVisible() && show) {
                mUsbImage.setVisible(true);
            }
        }
    }
}
