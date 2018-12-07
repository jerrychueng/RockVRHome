package com.rockchip.vr.ui.control;

import com.rockchip.vr.ui.layout.GridLayout;
import com.rockchip.vr.ui.view.ViewGroup;

import java.util.List;

/**
 * Created by yhc on 16-7-26.
 */
public class MenuPage {

    private List<MenuItem> mPageItems;
    private GridLayout mLayout;
    private int mPageCol;
    private int mPageRow;
    private ViewGroup mMenuBaseView;
    private boolean mIsShowing = false;

    public MenuPage(ViewGroup menuBaseView, int pageCol, int pageRow) {
        mMenuBaseView = menuBaseView;
        mPageCol = pageCol;
        mPageRow = pageRow;
    }

    public void onMeasure() {
        if(mIsShowing) {
            this.hidden();
        }
        mLayout.removeAllViews();
        for (MenuItem item : mPageItems) {
            mLayout.addView(item.getBaseView());
        }
        mLayout.onMeasure();
    }

    public void onDraw() {
        mLayout.onDraw();
    }

    public void addMenuItem(List<MenuItem> items) {
        mPageItems = items;
    }

    public void show() {
        if(!mIsShowing) {
            mIsShowing = true;
            mMenuBaseView.addView(mLayout);
        }
    }

    public void hidden() {
        if(mIsShowing) {
            mIsShowing = false;
            mMenuBaseView.removeView(mLayout);
        }
    }

    public boolean IsShowing() {
        return mIsShowing;
    }
}
