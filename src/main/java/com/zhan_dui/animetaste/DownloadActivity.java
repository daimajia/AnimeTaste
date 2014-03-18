package com.zhan_dui.animetaste;

import android.os.Bundle;
import com.zhan_dui.utils.SwipeBackAppCompatActivity;
import me.imid.swipebacklayout.lib.SwipeBackLayout;

/**
 * Created by daimajia on 14-2-11.
 */
public class DownloadActivity extends SwipeBackAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }
}
