package com.rockchip.vr.home.model;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.IntentCompat;

import com.rockchip.vr.home.util.RockLog;

import java.io.IOException;

/**
 * Created by yhc on 16-6-7.
 */
public class App {
    private Context mContext;

    private String mPackageName;
    private String mAppName;
    private Bitmap mIcon;
//    private String mLauncherActivityName;
    private boolean mCanDelete;

    public App(Context context, String packageName, String appName,
               Bitmap icon, boolean canDelete) {
        mContext = context;
        this.mPackageName = packageName;
        this.mAppName = appName;
        this.mIcon = icon;
        this.mCanDelete = canDelete;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getAppName() {
        return mAppName;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public void launch() {
        RockLog.d("App - launch " + mPackageName);
        PackageManager pm = mContext.getPackageManager();
        Intent intent=pm.getLaunchIntentForPackage(mPackageName);
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(componentName);
        mContext.startActivity(mainIntent);
    }

//    public void uninstall() {
//        RockLog.d("App - uninstall" + mPackageName);
//        Uri uri=Uri.parse("package:"+mPackageName);
//        Intent intent=new Intent(Intent.ACTION_DELETE, uri);
//        mContext.startActivity(intent);
//    }

    public boolean uninstall() {
        if(!mCanDelete){
            RockLog.d("App - not to uninstall " + mPackageName);
            return false;
        }
        RockLog.d("App - uninstall" + mPackageName);
        PackageManager packageManger = mContext.getPackageManager();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PackageInstaller packageInstaller = packageManger.getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName(mPackageName);
            int sessionId = 0;
            try {
                sessionId = packageInstaller.createSession(params);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            packageInstaller.uninstall(mPackageName, PendingIntent.getBroadcast(mContext, sessionId,
                    new Intent("android.intent.action.MAIN"), 0).getIntentSender());
            return true;
        }
        RockLog.e("old sdk");
        return false;
    }

    public static App getApp(Context context, String packageName) {
        String appName;
        Drawable d;
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo;
        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(packageName, 0);
            appInfo = packageInfo.applicationInfo;//pm.getApplicationInfo(packageName, 0);
            appName = pm.getApplicationLabel(appInfo).toString();
            d = pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        //boolean canDelete = !isSystemPackage(pm, packageInfo);
        boolean canDelete = (appInfo.flags & appInfo.FLAG_SYSTEM) <= 0;
        Bitmap icon = ((BitmapDrawable)d).getBitmap();
        App app = new App(context, packageName, appName, icon, canDelete);
        return app;
    }

    public boolean canDelete(){
        return mCanDelete;
    }

    public static boolean isSystemPackage(PackageManager pm, PackageInfo pkg) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{ getSystemSignature(pm) };
        }
        System.out.println("sSystemSignature[0]===="+sSystemSignature[0]);
        return sSystemSignature[0] != null && sSystemSignature[0].equals(getFirstSignature(pkg));
    }

    private static Signature[] sSystemSignature;

    private static Signature getFirstSignature(PackageInfo pkg) {
        System.out.println("getFirstSignature=="+pkg+","+pkg.signatures);
        if (pkg != null && pkg.signatures != null && pkg.signatures.length > 0) {
            System.out.println("getFirstSignature="+pkg.signatures[0]);
            return pkg.signatures[0];
        }
        System.out.println("getFirstSignature null");
        return null;
    }

    private static Signature getSystemSignature(PackageManager pm) {
        try {
            final PackageInfo sys = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            return getFirstSignature(sys);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }
}
