package com.rockchip.vr.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Switch;

import com.rockchip.vr.home.model.App;
import com.rockchip.vr.home.statusbar.StatusBar;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.home.util.SystemProperties;
import com.rockchip.vr.ui.view.ImageView;
import com.rockchip.vr.ui.view.SwitchImageView;
import com.rockchip.vr.ui.view.View;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public class HomeMenu {

    public static final float ITEM_RADIUS_X = 10f;
    public static final float ITEM_RADIUS_Z = 7.3f;
    public static final float ITEM_COORD_Y_SPACE = 2f;
    public static final double ITEM_MAX_ANGLE = Math.PI / 6;

    public final static int PAGE_ROW_ITEM_COUNT = 2;
    public final static int PAGE_COL_ITEM_COUNT = 4;
    public final static int PAGE_ITEM_COUNT =  PAGE_ROW_ITEM_COUNT * PAGE_COL_ITEM_COUNT;
    private int mCurMode = HomeMenuItem.MODE_NORMAL;
    private Plane mBottomBar;

    public static Vector3[][] calItemCoordTable() {
        Vector3[][] itemCoordTable = new Vector3[PAGE_ROW_ITEM_COUNT][PAGE_COL_ITEM_COUNT];
        final double EACH_ITEM_ANGLE = ITEM_MAX_ANGLE / (PAGE_COL_ITEM_COUNT-1);
        final double START_ITEM_ANGLE = -ITEM_MAX_ANGLE/2;
        for (int i = 0; i < PAGE_ROW_ITEM_COUNT; i++) {
            for (int j = 0; j < PAGE_COL_ITEM_COUNT; j++) {
                itemCoordTable[i][j] = new Vector3(
                        ITEM_RADIUS_X * Math.sin(START_ITEM_ANGLE + EACH_ITEM_ANGLE * j),
                        ITEM_COORD_Y_SPACE * (PAGE_ROW_ITEM_COUNT - i - 1) - 1f,
                        -ITEM_RADIUS_Z * Math.cos(START_ITEM_ANGLE + EACH_ITEM_ANGLE * j)
                        );
            }
        }
        return itemCoordTable;
    }

    public static final Vector3[][] ITEM_COORD_TABLE = calItemCoordTable();

    private VRMainRenderer mVRMainRenderer;
    private List<HomeMenuItem> mHomeMenuList;
    private int mCurPageNum = 0;
    private Context mContext;

    private List<Plane> mMenuPages;
    private List<Plane> mMenuPagesOld;

    private ImageView mNextPageButton;
    private ImageView mPrevPageButton;
    private Plane mCurPagePlane;
    private StatusBar mStatusBar;

    private ImageView mMenuBG;

    private AppUpdateReceiver mAppUpdateReceiver;

    private ImageView mTrash;
    private boolean mIsPause;

    public HomeMenu(Context context, VRMainRenderer vrRenderer) {
        mContext = context;
        mVRMainRenderer = vrRenderer;
        mHomeMenuList = new ArrayList<>();
        mMenuPages = new ArrayList<>();
        addMenuBG();
        initMenu();
        addCurPageToScene();
        addControlToScene();
        addStatusBar();
        //SystemProperties.set("installapk","false");
    }

    private void addMenuBG() {
        if(mMenuBG != null) {
            return;
        }
        mMenuBG = new ImageView("box_bg");
        try {
            mMenuBG.setImage(mContext, R.drawable.box_02_bg);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mMenuBG.setPosition(0, -0.5f, -15f);
        mMenuBG.setScale(18f, 13f, 1f);
        mMenuBG.addToScence(mVRMainRenderer.getCurrentScene());
    }

    private void initMenu() {
        // get all app info
        List<App> apps = getAllAppInfo();
        // create menu item for each app
        for (App appinfo : apps) {
            HomeMenuItem item = new HomeMenuItem(mContext, appinfo);
            mHomeMenuList.add(item);
            item.setItemList(mHomeMenuList);
        }
        for (HomeMenuItem item : mHomeMenuList) {
            item.onMeasure();
            item.onDraw();
        }
        // generate page
        prepareAllMenuPage();

        regBroadcast();
    }

    IntentFilter mPackageUpdateFilter;
    private boolean mIsRegAppUpdateReceiver = false;

    public void regBroadcast() {
        if(!mIsRegAppUpdateReceiver) {
            mIsRegAppUpdateReceiver = true;
            if (mPackageUpdateFilter == null) {
                mPackageUpdateFilter = new IntentFilter();
                mPackageUpdateFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
                mPackageUpdateFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                mPackageUpdateFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
                mPackageUpdateFilter.addDataScheme("package");
            }
            if (mAppUpdateReceiver == null) {
                mAppUpdateReceiver = new AppUpdateReceiver();
            }
            mContext.registerReceiver(mAppUpdateReceiver, mPackageUpdateFilter);
            RockLog.d("HomeMenu - register broadcast receiver of app onUpdate");
        }
    }

    private void unregBroadcast() {
        if(mIsRegAppUpdateReceiver) {
            mIsRegAppUpdateReceiver = false;
            mContext.unregisterReceiver(mAppUpdateReceiver);
            RockLog.d("HomeMenu - unregister broadcast receiver of app onUpdate");
        }
    }

    public void onResume() {
        RockLog.d("HomeMenu - onResume");
        //regBroadcast();
        mIsPause = false;

        if("true".equals(SystemProperties.get("installapk"))){
            SystemProperties.set("installapk","false");
            refreshAppMenuItem();
        }
//        initMenu();
//        addCurPageToScene();
//        refreshAppMenuItem();

        if(mStatusBar != null) {
            mStatusBar.onResume();
        }
    }

    public void onPause() {
        RockLog.d("HomeMenu - onPause");
        //unregBroadcast();
        mIsPause = true;
        if(mStatusBar != null) {
            mStatusBar.onPause();
        }
        setMode(HomeMenuItem.MODE_NORMAL);
    }

    public void onDestory(){
        RockLog.d("HomeMenu - onDestory");
        unregBroadcast();
    }

    public void updateMenuPos() {
        for (HomeMenuItem item : mHomeMenuList) {
            item.onMeasure();
        }
    }

    public List<HomeMenuItem> getMenuItemsOnPage(int pageNum) {
        int showItemCount = mHomeMenuList.size() - pageNum * PAGE_ITEM_COUNT;
        if(showItemCount > PAGE_ITEM_COUNT) {
            showItemCount = PAGE_ITEM_COUNT;
        }
        int curPageItemStartIndex = pageNum * PAGE_ITEM_COUNT;
        int curPageItemEndIndex = curPageItemStartIndex + showItemCount;
        return mHomeMenuList.subList(curPageItemStartIndex, curPageItemEndIndex);
    }

    public void prepareAllMenuPage() {
        mMenuPagesOld = mMenuPages;
        mMenuPages = new ArrayList<>();
        int pageCount = getPageCount();
        for (int i = 0; i < pageCount; i++) {
            Plane pagePlane = new Plane();
            pagePlane.isContainer(true);
            List<HomeMenuItem> curPageMenuItems = getMenuItemsOnPage(i);
            for (HomeMenuItem item : curPageMenuItems) {
                pagePlane.addChild(item.getPlane());
            }
            mMenuPages.add(pagePlane);
        }
    }

    public void addCurPageToScene() {
        if(getPageCount() == 0) {
            return;
        }
        Scene scene = mVRMainRenderer.getCurrentScene();
        if(mCurPagePlane != null) {
            scene.removeChild(mCurPagePlane);
            mCurPagePlane = null;
        }
        mCurPagePlane = mMenuPages.get(mCurPageNum);
        scene.addChild(mCurPagePlane);
    }

    public void addControlToScene() {
        Scene scene = mVRMainRenderer.getCurrentScene();
        mBottomBar = new Plane();
        mBottomBar.isContainer(true);
        mBottomBar.setPosition(0f, -3.5f, -8f);
        addPageControl(mBottomBar);
        addTrash(mBottomBar);
//        scene.addChild(mBottomBar);
    }

    private void addPageControl(Object3D parent) {
        // next page button
        mNextPageButton = new ImageView("nextpage");
        try {
            mNextPageButton.setImage(mContext, R.drawable.icon_right_01);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
//        mNextPageButton.setPosition(0.8f, 0f, 0f);
        mNextPageButton.setPosition(0.8f, -2.8f, -7.5f);
        mNextPageButton.setScale(0.5, 0.5, 1);
        mNextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public boolean onClick(View view) {
                nextPage();
                return true;
            }
        });
        mNextPageButton.setOnLookingListener(new View.OnLookingListener() {
            @Override
            public void onLookingAt() {
                mNextPageButton.updateImage(mContext, mVRMainRenderer, R.drawable.icon_right_02);
            }

            @Override
            public void onLookingOut() {
                mNextPageButton.updateImage(mContext, mVRMainRenderer, R.drawable.icon_right_01);
            }
        });
        // prev page button
        mPrevPageButton = new ImageView("prevpage");
        try {
            mPrevPageButton.setImage(mContext, R.drawable.icon_left_01);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
//        mPrevPageButton.setPosition(-0.8f, 0f, 0f);
        mPrevPageButton.setPosition(-0.8f, -2.8f, -7.5f);
        mPrevPageButton.setScale(0.5, 0.5, 1);
        mPrevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public boolean onClick(View view) {
                prevPage();
                return true;
            }
        });
        mPrevPageButton.setOnLookingListener(new View.OnLookingListener() {
            @Override
            public void onLookingAt() {
                mPrevPageButton.updateImage(mContext, mVRMainRenderer, R.drawable.icon_left_02);
            }

            @Override
            public void onLookingOut() {
                mPrevPageButton.updateImage(mContext, mVRMainRenderer, R.drawable.icon_left_01);
            }
        });
//        mNextPageButton.addToContainer(parent);
//        mPrevPageButton.addToContainer(parent);
        mNextPageButton.addToScence(mVRMainRenderer.getCurrentScene());
        mPrevPageButton.addToScence(mVRMainRenderer.getCurrentScene());
    }

    private void addStatusBar() {
        Scene scene = mVRMainRenderer.getCurrentScene();
        mStatusBar = new StatusBar(mContext, mVRMainRenderer);
        mStatusBar.addToScene(scene);
    }

    private void addTrash(Object3D parent) {
        mTrash = new ImageView("trash");
//        int[] ids = {R.drawable.icon_delete_01, R.drawable.icon_delete_02};
        try {
            mTrash.setImage(mContext, R.drawable.icon_delete_01);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
//        mTrash.setPosition(3f, 0f, 0f);
        //, 0.75f, 0.75f, 1, 1
        mTrash.setPosition(2f, -2.8f, -7.5f);
        mTrash.setScale(0.8, 0.8, 1);
        mTrash.setOnClickListener(new View.OnClickListener(){

            @Override
            public boolean onClick(View view) {
                if(mCurMode == HomeMenuItem.MODE_NORMAL) {
                    mCurMode = HomeMenuItem.MODE_REMOVE;
                } else {
                    mCurMode = HomeMenuItem.MODE_NORMAL;
                }
                setMode(mCurMode);
                return true;
            }
        });
        mTrash.setOnLookingListener(new View.OnLookingListener() {
            @Override
            public void onLookingAt() {
                mTrash.setScale(1, 1, 1);
            }

            @Override
            public void onLookingOut() {
                mTrash.setScale(0.8, 0.8, 1);
            }
        });
//        mTrash.addToContainer(parent);
        mTrash.addToScence(mVRMainRenderer.getCurrentScene());
    }

    public boolean processLookingAt(int x, int y) {
//        if(!mVRMainRenderer.isLookingAtObject(mMenuBG.getBasePlane())) {
//            return false;
//        }
        List<HomeMenuItem> curPageMenuItems = getMenuItemsOnPage(mCurPageNum);
        boolean isLookingAtObj = false;
        for (HomeMenuItem item : curPageMenuItems) {
            Plane plane =  item.getPlane();
            Object3D tempPlan = (Object3D) plane.clone();
            tempPlan.setPosition(plane.getPosition().x,plane.getPosition().y,plane.getPosition().z);
            boolean result = mVRMainRenderer.isLookingAtObject(tempPlan);
            if (result) {
//            if (mVRMainRenderer.isLookingAtObject(item.getPlane())) {
                isLookingAtObj = true;
                item.onLookingAt();
            } else {
                item.onNoLookingAt();
            }
        }
        if(isLookingAtObj) {
            return true;
        }
        if(mTrash != null) {
            isLookingAtObj = mTrash.processLooking(mVRMainRenderer);
            if(isLookingAtObj) {
                return true;
            }
        }
        if(mPrevPageButton != null && mNextPageButton != null) {
            isLookingAtObj = mPrevPageButton.processLooking(mVRMainRenderer);
            if(isLookingAtObj) {
                return true;
            }
            isLookingAtObj = mNextPageButton.processLooking(mVRMainRenderer);
            if(isLookingAtObj) {
                return true;
            }
        }
        return isLookingAtObj;
    }

    public boolean processTriger() {
        List<HomeMenuItem> curPageMenuItems = getMenuItemsOnPage(mCurPageNum);
        for (HomeMenuItem item : curPageMenuItems) {
            if (mVRMainRenderer.isLookingAtObject(item.getPlane())) {
                item.onTriger();
                return true;
            }
        }
        if(mNextPageButton.isBeingLookAt(mVRMainRenderer)) {
            mNextPageButton.onClick();
            return true;
        } else if(mPrevPageButton.isBeingLookAt(mVRMainRenderer)) {
            mPrevPageButton.onClick();
            return true;
        } else if(mTrash.isBeingLookAt(mVRMainRenderer)) {
            mTrash.onClick();
            return true;
        }
//        setMode(HomeMenuItem.MODE_NORMAL);
        return false;
    }

    public void setMode(int mode) {
        mCurMode = mode;
        if(mHomeMenuList!= null) {
            for (HomeMenuItem item : mHomeMenuList) {
                item.showMode(mode);
            }
        }
        if(mTrash != null) {
            mTrash.delayVisible(50);
            if (mode == HomeMenuItem.MODE_NORMAL) {
                mTrash.updateImage(mContext, mVRMainRenderer, R.drawable.icon_delete_01);
//                mTrash.switchView(0);
            } else if (mode == HomeMenuItem.MODE_REMOVE) {
                mTrash.updateImage(mContext, mVRMainRenderer, R.drawable.icon_delete_02);
//                mTrash.switchView(1);
            }
        }
    }

    public int nextPage() {
        RockLog.d("===== nextPage() =====");
        Plane oldPagePlane = mCurPagePlane;
        int pageCount = getPageCount();
        if (++mCurPageNum > pageCount-1) {
            mCurPageNum = pageCount-1;
            RockLog.d("already last page");
            return mCurPageNum;
        }
        RockLog.d("cur page: " + (mCurPageNum+1) + "/" + pageCount);
        mCurPagePlane = mMenuPages.get(mCurPageNum);
        mVRMainRenderer.getCurrentScene().replaceChild(oldPagePlane, mCurPagePlane);
        RockLog.d("replace page done");
        return mCurPageNum;
    }

    public int prevPage() {
        RockLog.d("===== prevPage() =====");
        Plane oldPagePlane = mCurPagePlane;
        if (--mCurPageNum < 0 ) {
            mCurPageNum = 0;
            RockLog.d("already first page");
            return mCurPageNum;
        }
        RockLog.d("cur page: " + (mCurPageNum+1) + "/" + getPageCount());
        mCurPagePlane = mMenuPages.get(mCurPageNum);
        mVRMainRenderer.getCurrentScene().replaceChild(oldPagePlane, mCurPagePlane);
        RockLog.d("replace page done");
        return mCurPageNum;
    }

    public int setPage(int num) {
        RockLog.d("===== setPage() =====");
        Plane oldPagePlane = mCurPagePlane;
        int pageCount = getPageCount();
        if(num < 0) {
            num = 0;
        }
        if(num > (pageCount - 1)) {
            num = pageCount - 1;
        }
        mCurPageNum = num;
        RockLog.d("cur page: " + (mCurPageNum+1) + "/" + getPageCount());
        mCurPagePlane = mMenuPages.get(mCurPageNum);
        mVRMainRenderer.getCurrentScene().replaceChild(oldPagePlane, mCurPagePlane);
        RockLog.d("replace page done");
        return mCurPageNum;
    }

    public int getPageCount() {
        return (int)Math.ceil((double)mHomeMenuList.size()/ PAGE_ITEM_COUNT);
    }

    private List<App> getAllAppInfo() {
        List<App> applist = new ArrayList<>();
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
//        intent.addCategory("com.google.intent.category.CARDBOARD");
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        List<String> packageNameList = new ArrayList<>();
        for (ResolveInfo resolveinfo : resolveInfos) {
            ActivityInfo activityinfo = resolveinfo.activityInfo;
            String packageName = activityinfo.packageName;
            if(mContext.getPackageName().equals(packageName)
                    || "com.android.inputmethod.latin".equals(packageName)
                    || packageNameList.contains(packageName)) {
                continue;
            }
            App app = null;
            app = App.getApp(mContext, packageName);
            if (app != null) {
                applist.add(app);
                packageNameList.add(packageName);
            }
        }
        return applist;
    }

    public class AppUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            RockLog.d("AppUpdateReceiver - onReceive():"+intent.getAction());
            mVRMainRenderer.senceLock.lock();
            RockLog.d("AppUpdateReceiver - onReceive - enter lock");
            try {
                if(mIsPause){
                    SystemProperties.set("installapk","true");
                }else if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                    String pkgName = intent.getDataString();
                    pkgName = pkgName.replace("package:", "");
                    RockLog.d("add package: " + pkgName);
                    App app = App.getApp(mContext, pkgName);
                    if(app != null) {
                        HomeMenuItem item = new HomeMenuItem(mContext, app);
                        mHomeMenuList.add(item);
                        item.setItemList(mHomeMenuList);
                        item.onMeasure();
                        item.onDraw();
                        prepareAllMenuPage();
                        setPage(mCurPageNum);
                    }
                    setMode(HomeMenuItem.MODE_NORMAL);
                } else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                    String pkgName = intent.getDataString();
                    pkgName = pkgName.replace("package:", "");
                    RockLog.d("removeFromParent package: " + pkgName);
                    for (int i = 0; i < mHomeMenuList.size(); i++) {
                        HomeMenuItem item = mHomeMenuList.get(i);
                        App app = item.getApp();
                        if (app.getPackageName().equals(pkgName)) {
                            mHomeMenuList.remove(i);
                            prepareAllMenuPage();
                            updateMenuPos();
                            setPage(mCurPageNum);
                            break;
                        }
                    }
                    if (mMenuPagesOld != null) {
                        mMenuPagesOld.clear();
                        mMenuPagesOld = null;
                    }
                    setMode(HomeMenuItem.MODE_NORMAL);
                }
            } finally {
                mVRMainRenderer.senceLock.unlock();
                RockLog.d("AppUpdateReceiver - onReceive - exit lock");
            }
            RockLog.d("AppUpdateReceiver - onReceive() -end");
        }
    }

    public void refreshAppMenuItem() {
        mVRMainRenderer.senceLock.lock();
        try {
            Plane oldPagePlane = mCurPagePlane;
//            for (HomeMenuItem item : mHomeMenuList) {
//                item.destroy();
//            }
            mHomeMenuList.clear();
            initMenu();
//        addCurPageToScene();
            mCurPagePlane = mMenuPages.get(mCurPageNum);
            mVRMainRenderer.getCurrentScene().replaceChild(oldPagePlane, mCurPagePlane);
        } finally {
            mVRMainRenderer.senceLock.unlock();
        }
    }
}
