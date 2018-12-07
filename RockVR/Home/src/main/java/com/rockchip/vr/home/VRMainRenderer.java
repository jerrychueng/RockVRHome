package com.rockchip.vr.home;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.rockchip.vr.home.model.ProtocolXWYF000Normal;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.home.util.TextureHelper;
import com.rockchip.vr.ui.RockVRRenderer;
import com.rockchip.vr.ui.util.BitmapUtil;
import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.scene.Scene;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VRMainRenderer extends RockVRRenderer {

    public Lock senceLock = new ReentrantLock();
    private static final String[] BACKGROUND_PICTURE_PATH = {
            "/system/media/RockVR/background.png",
            "/system/media/RockVR/background.jpg",
            "/system/media/RockVR/background.bmp"
    };

    HomeMenu mHomeMenu;
    Sphere mCursor;

    // skybox background
    private Sphere mBackgroundSphere;
    private Texture mBackgroundTexture;
    private int mBackgroundLastTextureId;
    private Material mBackgroundMaterial;

    private Scene mDefScene;
    private CardboardView cbv;
    private float[] mHeadView = new float[16];
    private int cursorTex;
    private float[] invView = new float[16];
    private float[] view = new float[16];
    private float[] modelView = new float[16];
    private float[] modelViewProjection = new float[16];
    private float[] modelPoint = new float[16];
    private float[] perspective = new float[16];
    private Bitmap mBitmapTheme;

    private Activity mAttachActivity;

    public VRMainRenderer(Activity activity,CardboardView cbv) {
        super(activity);
        this.cbv = cbv;
        mAttachActivity = activity;
        mContext = activity.getApplicationContext();
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);
        headTransform.getHeadView(this.mHeadView, 0);
        ProtocolXWYF000Normal protocolXWYF000Normal = ((VRMainActivity)mAttachActivity).getProtocol();
        if(protocolXWYF000Normal != null){
            Quaternion mLocalOrientation = Quaternion.getIdentity();
            mLocalOrientation.fromEuler(protocolXWYF000Normal.yaw,protocolXWYF000Normal.pitch,protocolXWYF000Normal.roll);
//            getCurrentCamera().setCameraOrientation(mLocalOrientation);
//            getCurrentCamera().setCameraPitch(protocolXWYF000Normal.pitch);
//            getCurrentCamera().setCameraRoll(protocolXWYF000Normal.roll);
//            getCurrentCamera().setCameraYaw(protocolXWYF000Normal.yaw);
        }
    }

    @Override
    public void onDrawEye(Eye eye) {
        getCurrentCamera().setPerspective(eye.getPerspective((float) getCurrentCamera().getNearPlane(), (float) getCurrentCamera().getFarPlane()));
        super.onDrawEye(eye);
        /*String p = SystemProperties.get("sys.vr.z","-5");
        float z = Float.parseFloat(p);
        p = SystemProperties.get("sys.vr.scale","0.10");
        float scale = Float.parseFloat(p);*/
//        onDrawCursor(eye, -5.0f, 0.1f);
//        onDrawCursor(eye, -2f, 0.1f);
        onDrawCursor(eye);
    }

    //绘制对应的交点信息
    public void onDrawCursor(Eye eye){
         if(this.cursorTex == 0 ){
             this.cursorTex = TextureHelper.loadTexture(mContext,R.drawable.handler_pointer);
         }
        Matrix.setIdentityM(modelPoint, 0);
        //将modelPoint 平移 zValue单位
        Matrix.translateM(modelPoint, 0, 0, 0, -2f);
        //将modelPoint进行缩放 scale
        Matrix.scaleM(modelPoint, 0, 0.1f, 0.1f, 0.1f);
        Matrix.rotateM(modelPoint,0,180,1,0,0);

        ProtocolXWYF000Normal protocolXWYF000Normal = ((VRMainActivity)mAttachActivity).getProtocol();
        if(protocolXWYF000Normal != null){
            Quaternion mLocalOrientation = Quaternion.getIdentity();
            mLocalOrientation.fromEuler(protocolXWYF000Normal.yaw,protocolXWYF000Normal.pitch,protocolXWYF000Normal.roll);
            Matrix4 matrix4 =  new Matrix4();
            matrix4 = matrix4.rotate(mLocalOrientation);
            Matrix.invertM(invView, 0, matrix4.getFloatValues(), 0);
            Log.d("TAG","do the cursor....."+protocolXWYF000Normal);
        }

        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, invView, 0);

        Matrix.multiplyMM(modelView, 0, view, 0, modelPoint, 0);
        Matrix.multiplyMM(modelViewProjection, 0, eye.getPerspective((float)getCurrentCamera().getNearPlane(),15f),
                0, modelView, 0);
//        if(eye.getType()==1) //单屏
//            this.cbv.setCursorInAtw(true, this.cursorTex, modelViewProjection);
//        else
//            this.cbv.setCursorInAtw(false, this.cursorTex, modelViewProjection);
    }


    public void onDrawCursor(Eye eye, float zValue, float scale){
        if(this.cursorTex==0) {
            // tex
//            this.cursorTex = TextureHelper.loadTexture(mContext, R.drawable.ic_brightness_down);
            this.cursorTex = TextureHelper.loadTexture(mContext, R.drawable.handler_pointer);
            // perspective mat
        }
        //透视图矩阵
        perspective = eye.getPerspective((float)getCurrentCamera().getNearPlane(),(float)getCurrentCamera().getFarPlane());
        Log.d("TAG","the "+mHeadView[0]+":"+mHeadView[1]+":"+mHeadView[2]+":"+mHeadView[3]+":"+
                mHeadView[4]+":"+mHeadView[5]+":"+mHeadView[6]+":"+mHeadView[7]+":"+
                perspective[8]+":"+perspective[9]+":"+perspective[10]+":"+mHeadView[11]+":"
        +mHeadView[12]+":"+mHeadView[13]+":"+mHeadView[14]+":"+mHeadView[15]);
        // model mat  modelPoint模型矩阵设为单位矩阵
        Matrix.setIdentityM(modelPoint, 0);
        //将modelPoint 平移 zValue单位
        Matrix.translateM(modelPoint, 0, 0, 0, zValue);
        //将modelPoint进行缩放 scale
        Matrix.scaleM(modelPoint, 0, scale, scale, scale);
        Matrix.rotateM(modelPoint,0,180,1,0,0);

        ProtocolXWYF000Normal protocolXWYF000Normal = ((VRMainActivity)mAttachActivity).getProtocol();
        if(protocolXWYF000Normal != null){
//            Matrix.setRotateEulerM(matrix,0,protocolXWYF000Normal.yaw,
//                    protocolXWYF000Normal.roll,protocolXWYF000Normal.pitch);
//            Quaternion mLocalOrientation = Quaternion.getIdentity();
//            Matrix4 matrix4 =  new Matrix4();
//            mLocalOrientation.fromEuler(protocolXWYF000Normal.yaw,protocolXWYF000Normal.pitch,protocolXWYF000Normal.roll);
//            matrix4 = matrix4.rotate(mLocalOrientation);
//            Matrix.multiplyMM(matrix,0,modelPoint,0,matrix4.getFloatValues(),0);
           // Matrix.multiplyMM(modelPoint, 0, view, 0, matrix, 0);
//            Log.d("TAG","the protocol is not null");
        }
//        Matrix.translateM(modelPoint,0, ((VRMainActivity)mAttachActivity).indexX, ((VRMainActivity)mAttachActivity).indexY,0);
//        x = ((VRMainActivity)mAttachActivity).indexX;
//        y = ((VRMainActivity)mAttachActivity).indexY;
//        倒装mHeadView  生成对应的invView
        Matrix.invertM(invView, 0, this.mHeadView, 0);
//        将eye矩阵和invView矩阵结合 给view
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, invView, 0);
        //将view 和modelPoint 矩阵结合 给modelView
        Matrix.multiplyMM(modelView, 0, view, 0, modelPoint, 0);
        //将perspective和modelView矩阵结合 给modelViewProjection
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
//        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelPoint, 0);

//        if(eye.getType()==1) //单屏
//            this.cbv.setCursorInAtw(true, this.cursorTex, modelViewProjection);
//        else
//            this.cbv.setCursorInAtw(false, this.cursorTex, modelViewProjection);
    }

    private int x, y;
    private float[] matrix=new float[16];

    @Override
    public boolean isLookingAtObject(Object3D target) {
        return super.isLookingAtObject(target, 3.0f);
    }

    @Override
    public void initScene() {
        RockLog.d("VRMainRenderer - initScene");
        getCurrentCamera().setFarPlane(1000);
        getCurrentScene().setBackgroundColor(0xdddddd);
        try {
            // -- create sky box (better)
            //getCurrentScene().setSkybox(R.drawable.right, R.drawable.left, R.drawable.top, R.drawable.bottom, R.drawable.front, R.drawable.back);
            // -- create sky sphere
            mBackgroundSphere = new Sphere(500, 64, 64);
            if (Settings.Global.getString(mContext.getContentResolver(), "themechange") != null && new File(Settings.Global.getString(mContext.getContentResolver(), "themechange")).exists()) {
                mBackgroundMaterial = new Material();
                mBitmapTheme = BitmapUtil.decodeSampledBitmapFromPath(Settings.Global.getString(mContext.getContentResolver(), "themechange"), 3000, 1500);
                mBackgroundTexture = new Texture("skySphere", mBitmapTheme);
                mBackgroundTexture.setMipmap(false);
                if (Settings.Global.getInt(mContext.getContentResolver(), "angle", 500) != 500) {
                    mBackgroundSphere.setRotY(Settings.Global.getInt(mContext.getContentResolver(), "angle", 500));
                } else {
                    mBackgroundSphere.setRotY(180);
                }
                mBackgroundLastTextureId = mBackgroundTexture.getTextureId();
            } else {
                mBackgroundMaterial = new Material();
                Bitmap bg = getBackground();
                if (bg != null) {
                    mBackgroundTexture = new Texture("skySphere", bg);
                } else {
                    mBackgroundTexture = new Texture("skySphere", R.drawable.bg2);
                    mBackgroundTexture.setMipmap(false);
                    if (Settings.Global.getInt(mContext.getContentResolver(), "angle", 500) != 500) {
                        mBackgroundSphere.setRotY(Settings.Global.getInt(mContext.getContentResolver(), "angle", 500));
                    } else {
                        mBackgroundSphere.setRotY(180);
                    }
                    mBackgroundLastTextureId = mBackgroundTexture.getTextureId();
                }
            }

            try {
                mBackgroundMaterial.addTexture(mBackgroundTexture);
            } catch (ATexture.TextureException e) {
                RockLog.e("VRMainRenderer - initScene - add sky sphere error !!!");
                e.printStackTrace();
            }
            mBackgroundMaterial.setColorInfluence(0);
            mBackgroundSphere.setMaterial(mBackgroundMaterial);
            mBackgroundSphere.setDoubleSided(true);
            getCurrentScene().addChild(mBackgroundSphere);
            //----------------
        } catch (Exception e) {
            e.printStackTrace();
        }
        // create main home menu
        mHomeMenu = new HomeMenu(mContext, this);
        // create center cursor
        /*mCursor = new Sphere(0.06f, 12, 12);
        Material sphereMaterial = new Material();
        sphereMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        mCursor.setMaterial(sphereMaterial);
        mCursor.setColor(Color.WHITE);
        mCursor.setPosition(0, 0, -10);
        getCurrentScene().addChild(mCursor);*/
        mDefScene = getCurrentScene();
    }

    int count = 1;
    private boolean mIsCursorLookingObjLastTime = false;
    @Override
    public void onRender(long elapsedTime, double deltaTime) {

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        senceLock.lock();
        try {
            super.onRender(elapsedTime, deltaTime);
            if(getCurrentScene().equals(mDefScene) && mHomeMenu != null) {
                if (++count > 10) {
                    count = 1;
                    boolean isLookingAtObjThisTime = mHomeMenu.processLookingAt(x,y);
                    mIsCursorLookingObjLastTime = isLookingAtObjThisTime;
                }
            }
            centerObject(mCursor);
        } finally {
            senceLock.unlock();
        }

    }

//    @Override
//    public void onSurfaceChanged(int width, int height) {
//        senceLock.lock();
//        try {
//            super.onSurfaceChanged(width, height);
//        } finally {
//            senceLock.unlock();
//        }
//    }


    public boolean onEnter() {
        if(mHomeMenu != null) {
            return mHomeMenu.processTriger();
        }
        return false;
    }

    public boolean onReturn() {
        if(getCurrentScene().equals(mDefScene)) {
            return true;
        }
        return false;
    }

    public void centerObject(Object3D obj) {
        if(mCursor != null) {
            float[] newPosition4 = new float[4];
            float[] posVec4 = {0f, 0, -6f, 3.0f};
            float[] headViewMatrix_inv = new float[16];
            Matrix4 headViewMatrix4 = new Matrix4();
            headViewMatrix4.setAll(mHeadViewMatrix);
            headViewMatrix4 = headViewMatrix4.inverse();
            headViewMatrix4.toFloatArray(headViewMatrix_inv);
            Matrix.multiplyMV(newPosition4, 0, headViewMatrix_inv, 0, posVec4, 0);
            obj.setPosition(newPosition4[0], newPosition4[1], newPosition4[2]);
            obj.setLookAt(getCurrentCamera().getPosition());
        }
    }


    public void resume() {
        RockLog.d("VRMainRenderer - onResume");
        super.onResume();
        HomeMenuItem.mLastTimeClick = SystemClock.elapsedRealtime();
        if(mHomeMenu != null) {
            mHomeMenu.onResume();
        }
    }

    public void pause() {
        RockLog.d("VRMainRenderer - onPause");
        super.onPause();
        if(mHomeMenu != null) {
            mHomeMenu.onPause();
        }
    }

    public void destory() {
        RockLog.d("VRMainRenderer - onDestory");
        if(mHomeMenu != null) {
            mHomeMenu.onDestory();
        }
    }

    @Override
    public void onRendererShutdown() {
        RockLog.d("VRMainRenderer - onRendererShutdown");
        super.onRendererShutdown();
    }

    private Bitmap getBackground() {
        String usePath = null;
        for (String path : BACKGROUND_PICTURE_PATH) {
            File file = new File(path);
            if(file.exists()) {
                usePath = path;
                RockLog.d("VRMainRenderer - found background pic: " + usePath);
                break;
            }
        }
        if(usePath == null) {
            RockLog.d("VRMainRenderer - there are not background pic in: /system/media/RockVR");
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(usePath);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            RockLog.d("VRMainRenderer - use background pic: " + usePath);
            return bitmap;
        } catch (FileNotFoundException e) {
            RockLog.d("VRMainRenderer - " + usePath + " not found, use default");
            return null;
        }
    }
    public void changeTheme(String path, int id) {
        if (id == 0) {
            mBackgroundMaterial.getTextureList().clear();
            if (mBitmapTheme != null) {
                mBitmapTheme.recycle();
            }
            mBitmapTheme = BitmapUtil.decodeSampledBitmapFromPath(path, 3000, 1500);
            Settings.Global.putString(mContext.getContentResolver(), "themechange", path);
            Texture bgTexture = new Texture("skySphere1", mBitmapTheme);
            bgTexture.setMipmap(false);
            try {
                mBackgroundMaterial.addTexture(bgTexture);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }
            mBackgroundSphere.setRotY(Settings.Global.getInt(mContext.getContentResolver(), "angle", 500));
        } else {

            mBackgroundMaterial.getTextureList().clear();
            if (mBitmapTheme != null) {
                mBitmapTheme.recycle();
            }
            Texture bgTexture = new Texture("skySphere2", id);
            bgTexture.setMipmap(false);
            try {
                mBackgroundMaterial.addTexture(bgTexture);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                 int yPixelOffset) {
    }
    @Override
    public void onTouchEvent(MotionEvent event){}
}

