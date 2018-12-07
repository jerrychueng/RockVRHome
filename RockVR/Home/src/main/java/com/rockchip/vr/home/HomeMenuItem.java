package com.rockchip.vr.home;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;

import com.rockchip.vr.home.model.App;
import com.rockchip.vr.home.util.RockLog;
import com.rockchip.vr.home.util.SubString;
import com.rockchip.vr.ui.view.ImageView;
import com.rockchip.vr.ui.view.TextView;

import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.primitives.Plane;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by yhc on 16-6-6.
 */
public class HomeMenuItem {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_REMOVE = 1;

    private int mCurMode = 0;
    private ImageView mRemoveImageView;

    private Plane mPlane;
    private ImageView mItemIcon;
    private TextView mItemText;

    private App mApp;
    private boolean isLooking = false;

    private List<HomeMenuItem> mHomeMenuItemList;

    private Context mContext;

    public HomeMenuItem(Context context, App app) {
        mApp = app;
        mContext = context;
        mPlane = new Plane();
    }

    public Plane getPlane() {
        return mPlane;
    }

    public App getApp() {
        return mApp;
    }

    public void setItemList(List<HomeMenuItem> itemlist) {
        mHomeMenuItemList = itemlist;
    }

    public static long mLastTimeClick = 0;

    public void onTriger() {
        if(mCurMode == MODE_NORMAL) {
            long timeSpan = SystemClock.elapsedRealtime() - mLastTimeClick;
            RockLog.d("last click timeSpan: " + timeSpan);
            if(timeSpan > 600) {
                mLastTimeClick = SystemClock.elapsedRealtime();
                mApp.launch();
            } else {
                RockLog.d("last click timeSpan too short, ignore it");
            }
        } else if(mCurMode == MODE_REMOVE){
            mApp.uninstall();
        }
    }

    public void onLookingAt() {
        if(isLooking == false) {
            mPlane.setScale(1.3);
            isLooking = true;
        }
    }

    public void onNoLookingAt() {
        if(isLooking == true) {
            mPlane.setScale(1);
            isLooking = false;
        }
    }

    public void onMeasure() {
        if(mHomeMenuItemList != null) {
            int index = mHomeMenuItemList.indexOf(this);
            int pageIndex = index % HomeMenu.PAGE_ITEM_COUNT;
            int itemPosX = pageIndex % HomeMenu.PAGE_COL_ITEM_COUNT;
            int itemPosY = pageIndex / HomeMenu.PAGE_COL_ITEM_COUNT;
            mPlane.setPosition(HomeMenu.ITEM_COORD_TABLE[itemPosY][itemPosX]);
            mPlane.setLookAt(0f, 0f, 0f);
        }
    }

    public void showMode(int mode) {
        if (mRemoveImageView == null) {
            return;
        }
        if(mCurMode == MODE_NORMAL && mode == MODE_REMOVE) {
            if(null != mApp && mApp.canDelete()){
                mRemoveImageView.setVisible(true);
            }else if(null != mApp){
                mRemoveImageView.setVisible(false);
            }
        } else if(mCurMode == MODE_REMOVE && mode == MODE_NORMAL) {
            mRemoveImageView.setVisible(false);
        }
        mCurMode = mode;
    }

    public void onDraw() {
        if(mApp != null) {
            mPlane.isContainer(true);
            // app icon
            mItemIcon = new ImageView("menuItemIcon");
            try {
                mItemIcon.setImage(mApp.getIcon());
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
                try {
                    mItemIcon.setImage(mContext, R.mipmap.ic_launcher);
                } catch (ATexture.TextureException e1) {
                    e1.printStackTrace();
                }
            }
            mItemIcon.addToContainer(mPlane);
            // app name
            int appNameLen = SubString.length(mApp.getAppName());
            String appName = mApp.getAppName();
            if(appNameLen > 12) {
                try {
                    appName = SubString.getSubString(mApp.getAppName(), 12);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            mItemText = new TextView(appName);
            mItemText.setPosition(0f, -1f, -0.15);
            mItemText.setTextConfig(Color.WHITE, 16, 100, 100, 1);
            mItemText.setScale(1.4f, 1.4f, 1.0f);
            mItemText.addToContainer(mPlane);

            mRemoveImageView = new ImageView("removeFromParent");
            try {
                mRemoveImageView.setImage(mContext, R.drawable.icon_delete_03);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }
            mRemoveImageView.setPosition(0.4, 0.4, -0.15);
            mRemoveImageView.setScale(0.35, 0.35, 1);
            mRemoveImageView.setVisible(false);
            mRemoveImageView.addToContainer(mPlane);
        }
    }
}
