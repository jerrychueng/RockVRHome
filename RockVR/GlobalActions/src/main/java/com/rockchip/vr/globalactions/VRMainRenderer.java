package com.rockchip.vr.globalactions;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.PowerManager;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.rockchip.vr.globalactions.util.RockLog;
import com.rockchip.vr.globalactions.util.TextureHelper;
import com.rockchip.vr.ui.view.TextView;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.renderer.VRRenderer;

import java.lang.reflect.Method;

public class VRMainRenderer extends VRRenderer {
    private CardboardView cbv;
    private float[] mHeadView = new float[16];
    private int cursorTex;
    private float[] invView = new float[16];
    private float[] view = new float[16];
    private float[] modelView = new float[16];
    private float[] modelViewProjection = new float[16];
    private float[] modelPoint = new float[16];
    private float[] perspective = new float[16];

    public VRMainRenderer(Context context, CardboardView cbv) {
        super(context);
        this.cbv = cbv;
    }

    private final double MAXX = 0.25;
    private final double MAXY = 0.09;
    private int BASE_COLOR = 0x2196F3;
    private int LOOKING_COLOR = 0XFFC107;

    private Plane mShutdownPlane;
    private Plane mRebootPlane;
    private Material mShutdownMaterial;
    private Material mRebootMaterial;
    Sphere mCursor;

    @Override
    public void initScene() {
        getCurrentCamera().setFarPlane(100);
        getCurrentScene().setBackgroundColor(Color.DKGRAY);

        try {
            Thread.sleep(430);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //
        mShutdownPlane = new Plane();
        mShutdownPlane.isContainer(true);
        mShutdownPlane.setPosition(0, 1f, -8f);
        mShutdownMaterial = new Material();
        mShutdownMaterial.setColor(BASE_COLOR);
        Plane shutdownbg = new Plane(4f, 1.5f, 1, 1);
        shutdownbg.setMaterial(mShutdownMaterial);
        mShutdownPlane.addChild(shutdownbg);
        TextView shutdownntext = new TextView(mContext.getString(R.string.shutdown_button));
        shutdownntext.setTextConfig(Color.WHITE, 25, 120, 100, 1);
        shutdownntext.setPosition(0, -0.45f, 0.1f);
        shutdownntext.setScale(2, 2, 1);
        shutdownntext.addToContainer(mShutdownPlane);
        getCurrentScene().addChild(mShutdownPlane);
//        mShutdownPlane.setLookAt(0, 0, 0);
        //
        mRebootPlane = new Plane();
        mRebootPlane.isContainer(true);
        mRebootPlane.setPosition(0, -1f, -8f);
        mRebootMaterial = new Material();
        mRebootMaterial.setColor(BASE_COLOR);
        Plane rebootbg = new Plane(4f, 1.5f, 1, 1);
        rebootbg.setMaterial(mRebootMaterial);
        mRebootPlane.addChild(rebootbg);
        TextView rebootntext = new TextView(mContext.getString(R.string.reboot_button));
        rebootntext.setTextConfig(Color.WHITE, 25, 120, 100, 1);
        rebootntext.setPosition(0, -0.45f, 0.1f);
        rebootntext.setScale(2, 2, 1);
        rebootntext.addToContainer(mRebootPlane);
//        mRebootPlane.setLookAt(0, 0, 0);
        //
        getCurrentScene().addChild(mRebootPlane);


        // create center cursor
        /*mCursor = new Sphere(0.06f, 12, 12);
        Material sphereMaterial = new Material();
        sphereMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        mCursor.setMaterial(sphereMaterial);
        mCursor.setColor(Color.WHITE);
        mCursor.setPosition(0, 0, -10);
        getCurrentScene().addChild(mCursor);*/

    }

    private boolean isLookingAtShutdown = false;
    private boolean isLookingAtReboot = false;

    @Override
    public void onRender(long elapsedTime, double deltaTime) {
        //GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        super.onRender(elapsedTime, deltaTime);
        centerObject(mCursor);
        if (isLookingAtObject(mShutdownPlane, MAXX, MAXY)) {
            if (!isLookingAtShutdown) {
                isLookingAtShutdown = true;
                mShutdownMaterial.setColor(LOOKING_COLOR);
            }
        } else if (isLookingAtShutdown) {
            isLookingAtShutdown = false;
            mShutdownMaterial.setColor(BASE_COLOR);
        }
        if (isLookingAtObject(mRebootPlane, MAXX, MAXY)) {
            if (!isLookingAtReboot) {
                isLookingAtReboot = true;
                mRebootMaterial.setColor(LOOKING_COLOR);
            }
        } else if (isLookingAtReboot) {
            isLookingAtReboot = false;
            mRebootMaterial.setColor(BASE_COLOR);
        }
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);
        headTransform.getHeadView(this.mHeadView, 0);
    }

    public void onDrawCursor(Eye eye, float zValue, float scale) {
        if (this.cursorTex == 0) {
            // tex
            this.cursorTex = TextureHelper.loadTexture(mContext, R.drawable.ic_brightness_down);
            // perspective mat
        }
        perspective = eye.getPerspective((float) getCurrentCamera().getNearPlane(), (float) getCurrentCamera().getFarPlane());

        // model mat
        Matrix.setIdentityM(modelPoint, 0);
        Matrix.translateM(modelPoint, 0, 0, 0, zValue);
        Matrix.scaleM(modelPoint, 0, scale, scale, scale);

        Matrix.invertM(invView, 0, this.mHeadView, 0);
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, invView, 0);
        Matrix.multiplyMM(modelView, 0, view, 0, modelPoint, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        if (eye.getType() == 1)
            this.cbv.setCursorInAtw(true, this.cursorTex, modelViewProjection);
        else
            this.cbv.setCursorInAtw(false, this.cursorTex, modelViewProjection);
    }

    private Vector3 mForwardVec = new Vector3();
    private Vector3 mHeadTranslation = new Vector3();

    public boolean isLookingAtObject(Object3D target, double maxX, double maxY) {
        this.mHeadViewQuaternion.fromMatrix(this.mHeadViewMatrix);
        this.mHeadViewQuaternion.inverse();
        this.mForwardVec.setAll(0.0D, 0.0D, 1.0D);
        this.mForwardVec.transform(this.mHeadViewQuaternion);
        this.mHeadTranslation.setAll(this.mHeadViewMatrix.getTranslation());
        this.mHeadTranslation.subtract(target.getPosition());
        this.mHeadTranslation.normalize();
        double dividerX = Math.abs(mHeadTranslation.x - mForwardVec.x);
        double dividerY = Math.abs(mHeadTranslation.y - mForwardVec.y);
        double angle = mHeadTranslation.angle(this.mForwardVec);
        return dividerX < maxX && dividerY < maxY && angle < 50;
    }

    @Override
    public void onDrawEye(Eye eye) {
        getCurrentCamera().setPerspective(eye.getPerspective((float) getCurrentCamera().getNearPlane(), (float) getCurrentCamera().getFarPlane()));
        super.onDrawEye(eye);
        onDrawCursor(eye, -5.0f, 0.1f);
    }

    @Override
    public boolean isLookingAtObject(Object3D target, float maxAngle) {
        RockLog.d("isLookingAtObject");
        return super.isLookingAtObject(target, 3.0f);
    }

    public void centerObject(Object3D obj) {
        if (null != obj) {
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

    public boolean onEnter() {
        RockLog.d("VRMainRenderer - onEnter");
        if (isLookingAtObject(mShutdownPlane, MAXX, MAXY)) {
            shutdown();
            return true;
        } else if (isLookingAtObject(mRebootPlane, MAXX, MAXY)) {
            reboot();
            return true;
        }
        return false;
    }

    private void shutdown() {
        RockLog.d("shutdown....");
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_SHUTDOWN);
//        mContext.sendBroadcast(intent);
        try {

            //获得ServiceManager类
            Class<?> ServiceManager = Class
                    .forName("android.os.ServiceManager");

            //获得ServiceManager的getService方法
            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);

            //调用getService获取RemoteService
            Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);

            //获得IPowerManager.Stub类
            Class<?> cStub = Class
                    .forName("android.os.IPowerManager$Stub");
            //获得asInterface方法
            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
            //调用asInterface方法获取IPowerManager对象
            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
            //获得shutdown()方法
            Method shutdown = oIPowerManager.getClass().getMethod("shutdown", boolean.class, boolean.class);
            //调用shutdown()方法
            shutdown.invoke(oIPowerManager, false, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reboot() {
        RockLog.d("reboot....");
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        pm.reboot(null);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}
