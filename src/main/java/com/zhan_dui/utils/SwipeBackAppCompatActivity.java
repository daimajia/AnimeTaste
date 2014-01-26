package com.zhan_dui.utils;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class SwipeBackAppCompatActivity extends ActionBarActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;
    final int MY_EDGE_SIZE = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        final float density = getResources().getDisplayMetrics().density;
        getSwipeBackLayout().setEdgeSize((int) (MY_EDGE_SIZE * density + 0.5f));//10dp
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_ALL);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v != null)
            return v;
        return mHelper.findViewById(id);
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }
    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
