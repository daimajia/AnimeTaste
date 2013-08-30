package com.zhan_dui.animetaste;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.CenterLayout;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.basv.gifmoviewview.widget.GifMovieView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.data.VideoDB;
import com.zhan_dui.modal.VideoDataFormat;
import com.zhan_dui.utils.OrientationHelper;

public class PlayActivity extends ActionBarActivity implements OnClickListener,
		OnPreparedListener, OnInfoListener, Target {

	private TextView mTitleTextView;
	private TextView mContentTextView;
	private TextView mAutherTextView;
	private ShareActionProvider mShareActionProvider;
	private CenterLayout mVideoWrapper;
	private VideoView mVideoView;
	private ImageView mDetailImageView;
	private ImageButton mPlayButton;
	private GifMovieView mLoadingGif;
	private MediaController mVideoControls;

	private RelativeLayout mHeaderWrapper;

	private int mCurrentScape;

	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private VideoDB mVideoDB;

	private View mVideoAction;

	private VideoDataFormat mVideoInfo;

	private OrientationEventListener mOrientationEventListener;
	private MenuItem mFavMenuItem;

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		mContext = this;
		mVideoDB = new VideoDB(mContext, VideoDB.NAME, null, VideoDB.VERSION);
		mVideoInfo = (VideoDataFormat) (getIntent().getExtras()
				.getSerializable("VideoInfo"));
		setContentView(R.layout.activity_play);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mVideoControls = (MediaController) findViewById(R.id.media_play_controler);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setMediaController(mVideoControls);
		mVideoView.setOnInfoListener(this);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setCanBePlayed(false);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		if (mSharedPreferences.getBoolean("use_hd", false)) {
			mVideoView.setVideoPath(mVideoInfo.HDVideoUrl);
		} else {
			mVideoView.setVideoPath(mVideoInfo.CommonVideoUrl);
		}

		mCurrentScape = OrientationHelper.PORTRAIT;
		mTitleTextView = (TextView) findViewById(R.id.title);
		mContentTextView = (TextView) findViewById(R.id.content);
		mDetailImageView = (ImageView) findViewById(R.id.detailPic);
		mVideoAction = (View) findViewById(R.id.VideoAction);
		mAutherTextView = (TextView) findViewById(R.id.author);
		mPlayButton = (ImageButton) findViewById(R.id.play_button);
		mVideoWrapper = (CenterLayout) findViewById(R.id.play_view);
		mLoadingGif = (GifMovieView) findViewById(R.id.loading_gif);
		mHeaderWrapper = (RelativeLayout) findViewById(R.id.HeaderWrapper);
		Picasso.with(mContext).load(mVideoInfo.DetailPic)
				.placeholder(R.drawable.big_bg).into(this);

		Typeface tfTitle = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Bold.ttf");
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Thin.ttf");
		mTitleTextView.setTypeface(tfTitle);
		mAutherTextView.setTypeface(tf);
		mTitleTextView.setText(mVideoInfo.Name);
		mContentTextView.setText(mVideoInfo.Brief);
		mAutherTextView.setText(mVideoInfo.Author + " · " + mVideoInfo.Year);
		mPlayButton.setOnClickListener(this);

		mOrientationEventListener = new OrientationEventListener(mContext) {

			@SuppressLint("NewApi")
			@Override
			public void onOrientationChanged(int orientation) {
				if (mVideoView.isPlaying()) {
					int tending = OrientationHelper.userTending(orientation,
							mCurrentScape);
					if (tending != OrientationHelper.NOTHING) {
						if (tending == OrientationHelper.LANDSCAPE) {
							mVideoControls.hide();
							if (Build.VERSION.SDK_INT >= 9) {
								setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
							} else {
								setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
							}
							setPlayerWindowSize(FULL_WIDTH, FULL_HEIGHT, false);
						} else if (tending == OrientationHelper.PORTRAIT) {
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							setPlayerWindowSize(
									FULL_WIDTH,
									getResources().getDimensionPixelSize(
											R.dimen.player_height), true);
						}
						mCurrentScape = tending;
					}
				}
			}
		};

		if (mOrientationEventListener.canDetectOrientation()) {
			mOrientationEventListener.enable();
		}
		mVideoInfo.setFav(mVideoDB.isFav(mVideoInfo.Id));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("VideoPosition", mVideoView.getCurrentPosition());
	}

	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, "分享啦！");

		intent.setType("*/*");

		return intent;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.play, menu);
		MenuItem item = menu.findItem(R.id.menu_item_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(item);
		mShareActionProvider.setShareIntent(getDefaultIntent());
		mFavMenuItem = menu.findItem(R.id.action_fav);
		new CheckIsFavorite().execute();
		return true;
	}

	private final int FULL_WIDTH = -1;
	private final int FULL_HEIGHT = -1;

	private void setPlayerWindowSize(int width, int height,
			boolean actionbarVisibility) {
		if (actionbarVisibility) {
			getSupportActionBar().show();
		} else {
			getSupportActionBar().hide();
		}
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		RelativeLayout.LayoutParams headerParams = (LayoutParams) mHeaderWrapper
				.getLayoutParams();
		CenterLayout.LayoutParams videoParams = (io.vov.vitamio.widget.CenterLayout.LayoutParams) mVideoView
				.getLayoutParams();
		if (width == FULL_WIDTH) {
			headerParams.width = metrics.widthPixels;
			videoParams.width = metrics.widthPixels;
		} else {
			headerParams.width = width;
			videoParams.width = width;
		}
		if (height == FULL_HEIGHT) {
			headerParams.height = metrics.heightPixels;
			videoParams.height = metrics.heightPixels;
		} else {
			headerParams.height = height;
			videoParams.height = height;
		}
		mHeaderWrapper.setLayoutParams(headerParams);
		mHeaderWrapper.requestLayout();

		mVideoView.setLayoutParams(videoParams);
		mVideoView.requestFocus();
		mVideoView.requestLayout();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.play_button) {
			v.setVisibility(View.INVISIBLE);
			mVideoView.setCanBePlayed(true);
			mVideoAction.setVisibility(View.INVISIBLE);
			mVideoView.start();
			setPlayerWindowSize(FULL_WIDTH, getResources()
					.getDimensionPixelSize(R.dimen.player_height), true);
		}
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				mLoadingGif.setVisibility(View.INVISIBLE);
				mPlayButton.setVisibility(View.VISIBLE);
			} else {
				mVideoControls.hide();
			}
		}
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.setPlaybackSpeed(1.0f);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			prepareStop();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// mOrientationEventListener.disable();

	}

	/**
	 * 这是播放器的一个Bug,要是直接退出就会出现杂音，一定要在播放状态退出 才不会有杂音
	 */
	private void prepareStop() {
		mVideoView.setCanBePlayed(true);
		mVideoView.setVolume(0f, 0f);
		mVideoView.start();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			prepareStop();
			finish();
			return true;
		case R.id.action_fav:
			if (mVideoInfo.isFavorite()) {
				if (mVideoDB.removeFav(mVideoInfo) > 0) {
					Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
					item.setIcon(R.drawable.ab_fav_normal);
					mVideoInfo.setFav(false);
				} else {
					Toast.makeText(mContext, "删除失败", Toast.LENGTH_SHORT).show();
				}
			} else {
				if (mVideoDB.insertFav(mVideoInfo) > 0) {
					Toast.makeText(mContext, "收藏成功", Toast.LENGTH_SHORT).show();
					item.setIcon(R.drawable.ab_fav_active);
					mVideoInfo.setFav(true);
				} else {
					Toast.makeText(mContext, "收藏失败", Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(mContext);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(mContext);
	}

	@Override
	public void onBitmapFailed() {

	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, LoadedFrom arg1) {
		mDetailImageView.setImageBitmap(bitmap);
		mLoadingGif.setVisibility(View.INVISIBLE);
		mPlayButton.setVisibility(View.VISIBLE);
	}

	private class CheckIsFavorite extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			return mVideoDB.isFav(mVideoInfo.Id);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mVideoInfo.setFav(result);
			if (result) {
				mFavMenuItem.setIcon(R.drawable.ab_fav_active);
			}
		}

	}
}