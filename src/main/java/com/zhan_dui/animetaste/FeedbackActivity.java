package com.zhan_dui.animetaste;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.avos.avoscloud.ParseObject;
import com.zhan_dui.utils.SwipeBackAppCompatActivity;
import me.imid.swipebacklayout.lib.SwipeBackLayout;

public class FeedbackActivity extends SwipeBackAppCompatActivity {
	private EditText mFeedback;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		mContext = this;
		mFeedback = (EditText) findViewById(R.id.suggestion);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.feedback, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_feedback) {
			String feedback = mFeedback.getText().toString();
			if (feedback.length() == 0) {
				Toast.makeText(mContext, R.string.empty, Toast.LENGTH_SHORT)
						.show();
			} else {
                ParseObject feed = new ParseObject("Feedback");
                feed.put("content",feedback);
				feed.put("phone", android.os.Build.MODEL);
				feed.put("os", android.os.Build.VERSION.SDK_INT);
				feed.saveInBackground();
				Toast.makeText(mContext, R.string.thanks, Toast.LENGTH_SHORT)
						.show();
				finish();
			}
			return true;
		}
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
