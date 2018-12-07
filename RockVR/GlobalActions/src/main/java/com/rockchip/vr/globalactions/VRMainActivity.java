package com.rockchip.vr.globalactions;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.rockchip.vr.globalactions.util.RockLog;

import org.rajawali3d.vr.VRActivity;

public class VRMainActivity extends VRActivity {

    private VRMainRenderer mVRMainRenderer;

    public VRMainActivity() {
        RockLog.d("===== VRMainActivity =====");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        RockLog.d("VRMainActivity - onCreate");
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

//        getSurfaceView().setZOrderOnTop(true);
//        getSurfaceView().setEGLConfigChooser(8, 8, 8, 8, 16, 0);
////        getSurfaceView().getHolder().setFormat(PixelFormat.RGBA_8888);
//        getSurfaceView().getHolder().setFormat(PixelFormat.TRANSLUCENT);
//        getSurfaceView().setBackgroundColor(Color.TRANSPARENT);
//        getSurfaceView().gatherTransparentRegion(new Region(-1, 1, 1, -1));

        mVRMainRenderer = new VRMainRenderer(this, getSurfaceView());
        setRenderer(mVRMainRenderer);

        setConvertTapIntoTrigger(true);

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        RockLog.d("onKeyDown - keycode: " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (mVRMainRenderer != null) {
                mVRMainRenderer.onEnter();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MOVE_HOME) {
            getCardboardView().resetHeadTracker();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCardboardTrigger() {
        RockLog.d("onCardboardTrigger");
        if (mVRMainRenderer != null) {
            mVRMainRenderer.onEnter();
        }
        super.onCardboardTrigger();

    }
}
