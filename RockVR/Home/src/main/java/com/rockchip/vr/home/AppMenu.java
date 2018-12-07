package com.rockchip.vr.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.rockchip.vr.home.model.App;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.ui.control.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yhc on 16-7-26.
 */
public class AppMenu extends Menu {

    private Context mContext;


    public AppMenu(Context context, int colNum, int rowNum) {
        super(colNum, rowNum);
        mContext = context;
    }

    private List<App> getAllAppInfo() {
        List<App> applist = new ArrayList<>();
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
            App app = App.getApp(mContext, packageName);
            if (app != null) {
                applist.add(app);
            }
        }
        return applist;
    }

    public class AppUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            RockLog.d("AppUpdateReceiver - onReceive()");
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                String pkgName = intent.getDataString();
                pkgName = pkgName.replace("package:", "");
                RockLog.d("add package: " + pkgName);
                App app = App.getApp(mContext, pkgName);
                if(app != null) {
//                    HomeMenuItem item = new HomeMenuItem(app);
//                    mHomeMenuList.add(item);
//                    item.setItemList(mHomeMenuList);
//                    item.onMeasure();
//                    item.onDraw();
//                    prepareAllMenuPage();
//                    setPage(mCurPageNum);
                }
            } else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                String pkgName = intent.getDataString();
                pkgName = pkgName.replace("package:", "");
                RockLog.d("removeFromParent package: " + pkgName);
//                for (int i = 0; i < mHomeMenuList.size(); i++) {
//                    HomeMenuItem item = mHomeMenuList.get(i);
//                    App app = item.getApp();
//                    if (app.getPackageName().equals(pkgName)) {
//                        mHomeMenuList.removeFromParent(i);
//                        prepareAllMenuPage();
//                        updateMenuPos();
//                        setPage(mCurPageNum);
//                        break;
//                    }
//                }
            }
        }
    }
}
