package com.rockchip.vr.home;

import com.rockchip.vr.home.model.App;
import com.rockchip.vr.ui.control.MenuItem;

/**
 * Created by yhc on 16-7-26.
 */
public class AppMenuItem extends MenuItem {

    private App mApp;

    public AppMenuItem(App app) {
        mApp = app;
    }

    @Override
    public void onMeasure() {

    }

    @Override
    public void onDraw() {

    }
}
