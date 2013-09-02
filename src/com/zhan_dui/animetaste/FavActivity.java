package com.zhan_dui.animetaste;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.adapters.VideoListAdapter;
import com.zhan_dui.data.VideoDB;
import com.zhan_dui.modal.VideoDataFormat;

public class FavActivity extends ActionBarActivity implements
		OnItemLongClickListener {

	private Context mContext;
	private ListView mFavListView;
	private VideoListAdapter mFavListAdapter;
	private VideoDB mVideoDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_fav);
		mVideoDB = new VideoDB(mContext, VideoDB.NAME, null, VideoDB.VERSION);
		mFavListView = (ListView) findViewById(R.id.videoList);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		mFavListView.setOnItemLongClickListener(this);
		new LoadAsyncTask().execute();
	}

	private class LoadAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Cursor cursor = mVideoDB.getAllFav();
			mFavListAdapter = VideoListAdapter.build(mContext, cursor, false);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mFavListView.setAdapter(mFavListAdapter);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		if (item.getItemId() == R.id.action_delete_all) {
			if (mFavListAdapter.getCount() != 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
						.setTitle(R.string.delete_title)
						.setMessage(R.string.delete_all_tip)
						.setNegativeButton(R.string.delete_cancel, null)
						.setPositiveButton(R.string.delete_ok,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										if (mVideoDB.removeAllFavs() > 0) {
											new LoadAsyncTask().execute();
											Toast.makeText(mContext,
													R.string.delete_success,
													Toast.LENGTH_SHORT).show();
										} else {
											Toast.makeText(mContext,
													R.string.delete_fail,
													Toast.LENGTH_SHORT).show();
										}
									}
								});
				builder.create().show();
			} else {
				Toast.makeText(mContext, R.string.delete_null,
						Toast.LENGTH_SHORT).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		new LoadAsyncTask().execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(mContext);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(mContext);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.fav, menu);
		return true;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View item,
			int position, long id) {
		final VideoDataFormat videoData = (VideoDataFormat) mFavListAdapter
				.getItem(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
				.setTitle(R.string.delete_title)
				.setMessage(R.string.delete_body)
				.setNegativeButton(R.string.delete_cancel, null)
				.setPositiveButton(R.string.delete_ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mVideoDB.removeFav(videoData) > 0) {
							new LoadAsyncTask().execute();
							Toast.makeText(mContext, R.string.delete_success,
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(mContext, R.string.delete_fail,
									Toast.LENGTH_SHORT).show();
						}
					}
				});
		builder.create().show();
		return false;
	}
}
