package com.rockchip.vr.home.statusbar;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.rockchip.vr.home.R;
import com.rockchip.vr.home.VRMainRenderer;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.ui.view.ImageView;

import org.rajawali3d.materials.textures.ATexture;

public class HeadsetStatus extends StatusBarItem {

    private static final String NAME = "HeadsetStatus";
    private ImageView mHeadsetImage;
    private Context mContext;
    private AudioManager mAudioManager;

    public HeadsetStatus(Context context, VRMainRenderer vrMainRenderer) {
        super(context, vrMainRenderer, NAME);
        mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        //intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        setBroadcast(intentFilter);
    }

    @Override
    public void onUpdate(Intent intent) {
        RockLog.d(NAME + " - onUpdate");
        if (mHeadsetImage != null) {
            if (null == mAudioManager) {
                mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            }
            showHeadsetIcon(mAudioManager.isWiredHeadsetOn());
            /*if (null != intent && intent.hasExtra("microphone")) {
                if (intent.getIntExtra("state", 0) == 1) {
                    showHeadsetIcon(false);
                } else if (intent.getIntExtra("state", 0) == 0) {
                    showHeadsetIcon(true);
                }
            } else {
                if (null == mAudioManager) {
                    mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                }
                showHeadsetIcon(mAudioManager.isWiredHeadsetOn());
            }*/
        }
    }

    @Override
    public void onDraw() {
        mPlane.setScale(0.45f, 0.45f, 1f);
        mPlane.isContainer(true);
        mHeadsetImage = new ImageView("headset");
        try {
            mHeadsetImage.setImage(mContext, R.drawable.icon_headset);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mHeadsetImage.addToContainer(mPlane);
    }

    private void showHeadsetIcon(boolean show) {
        if (null != mHeadsetImage) {
            if (mHeadsetImage.getBasePlane().isVisible() && !show) {
                mHeadsetImage.setVisible(false);
            } else if (!mHeadsetImage.getBasePlane().isVisible() && show) {
                mHeadsetImage.setVisible(true);
            }
        }
    }
}
