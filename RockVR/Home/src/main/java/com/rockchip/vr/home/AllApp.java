package com.rockchip.vr.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.rockchip.vr.home.model.App;

import java.util.ArrayList;
import java.util.List;

public class AllApp {
    List<App> mAppList;
    List<String> mPackageNameList;

    Context mContext;

    public void AllApp(Context context) {
        mContext = context;
        mPackageNameList = getAllAppPackageNameList();
        mAppList = getAllAppInfo(mPackageNameList);
    }

    public List<App> getAllAppInfo(List<String> packageNameList) {
        List<App> applist = new ArrayList<>();
        for(String packageName : packageNameList) {
            App app = null;
            app = App.getApp(mContext, packageName);
            if (app != null) {
                applist.add(app);
            }
        }
        return applist;
    }

    public List<String> getAllAppPackageNameList() {
        List<String> packageNameList = new ArrayList<>();
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
//        intent.addCategory("com.google.intent.category.CARDBOARD");
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveinfo : resolveInfos) {
            ActivityInfo activityinfo = resolveinfo.activityInfo;
            String packageName = activityinfo.packageName;
            if(mContext.getPackageName().equals(packageName)) {
                continue;
            }
            packageNameList.add(packageName);
        }
        return packageNameList;
    }

    public List<App> getAppList() {
        return mAppList;
    }

    public void updateAllApp() {

    }
}
