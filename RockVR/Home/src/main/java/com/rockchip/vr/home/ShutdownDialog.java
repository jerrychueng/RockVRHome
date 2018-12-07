package com.rockchip.vr.home;

import android.app.Activity;
import android.graphics.Color;

import org.rajawali3d.materials.Material;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.scene.Scene;

public class ShutdownDialog {

    private VRMainRenderer mVRRenderer;
    private Activity mActivity;

    private Scene mPrevScene;
    private Scene mScene;

    private Plane mShutdownPlane;
    private Plane mRebootPlane;

//    private boolean mIsNeedShowShutdown = false;
    private boolean mIsShowingShutdown = false;

    public ShutdownDialog(VRMainRenderer vrRenderer, Activity activity) {
        mVRRenderer = vrRenderer;
        mActivity = activity;
        mScene = new Scene(mVRRenderer);
        mVRRenderer.addScene(mScene);
    }

    public void show() {
        mPrevScene = mVRRenderer.getCurrentScene();
        mVRRenderer.switchScene(mScene);
    }

    public void hidden() {
        if(mPrevScene != null) {
            mVRRenderer.switchScene(mPrevScene);
//            mVRRenderer.setNeedShowShutdownDialog();
        }
    }

    public void onDraw() {
        mScene.setBackgroundColor(Color.BLACK);
        //
        mShutdownPlane = new Plane();
        mShutdownPlane.setColor(Color.WHITE);
        mShutdownPlane.setPosition(0, 1, -8f);
        Material material1 = new Material();
        material1.setColor(Color.WHITE);
        mShutdownPlane.setMaterial(material1);
        mScene.addChild(mShutdownPlane);
        //
        mRebootPlane = new Plane();
        mRebootPlane.setColor(Color.WHITE);
        mRebootPlane.setPosition(0, -1f, -8f);
        Material material2 = new Material();
        material2.setColor(Color.WHITE);
        mRebootPlane.setMaterial(material2);
        mScene.addChild(mRebootPlane);
    }

    public boolean isCurrentScene() {
        return mVRRenderer.getCurrentScene().equals(mScene);
    }

    public void onRender(long elapsedTime, double deltaTime) {
//        if(mIsShowingShutdown) {
//            if(mVRRenderer.isLookingAtObject(mShutdownPlane)) {
//                mShutdownPlane.setScaleZ(1.5f);
//            } else if(mVRRenderer.isLookingAtObject(mRebootPlane)) {
//                mRebootPlane.setScaleZ(1.5f);
//            }
//        }
    }


    public boolean isShowingShutdown() {
        return mIsShowingShutdown;
    }

    public void setShowingShutdown(boolean showingShutdown) {
        mIsShowingShutdown = showingShutdown;
    }

    public boolean onEnter() {
        if(mVRRenderer.isLookingAtObject(mShutdownPlane)) {
            this.hidden();
            mActivity.finish();
        } else if(mVRRenderer.isLookingAtObject(mRebootPlane)) {
            this.hidden();
            mActivity.finish();
        }
        return true;
    }

    public Scene getScene() {
        return mScene;
    }

}
