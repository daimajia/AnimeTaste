package com.zhan_dui.animetaste;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.CenterLayout;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;

import com.avos.avoscloud.ParseException;
import com.avos.avoscloud.ParseObject;
import com.avos.avoscloud.ParseQuery;
import com.avos.avoscloud.SaveCallback;
import com.basv.gifmoviewview.widget.GifMovieView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.auth.SocialPlatform;
import com.zhan_dui.auth.User;
import com.zhan_dui.data.VideoDB;
import com.zhan_dui.modal.Comment;
import com.zhan_dui.modal.DataHandler;
import com.zhan_dui.modal.VideoDataFormat;
import com.zhan_dui.utils.OrientationHelper;

public class PlayActivity extends ActionBarActivity implements OnClickListener,
		OnPreparedListener, OnInfoListener, Target {

	private TextView mTitleTextView;
	private TextView mContentTextView;
	private TextView mAutherTextView;
	private ShareActionProvider mShareActionProvider;
	private VideoView mVideoView;
	private Button mZoomButton;
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
	private Long mPreviousPlayPosition = 0l;
	private Bitmap mDetailPicture;
	private ImageView mRecommandThumb;
	private TextView mRecommandTitle, mRecommandContent;
	private LinearLayout mComments;
	private LayoutInflater mLayoutInflater;
	private View mLoadMoreComment;
	private View mRecommandView;
	private Button mLoadMoreButton;

	private boolean mCommentFinished;
	private User mUser;

	private final String mDir = "AnimeTaste";
	private final String mShareName = "animetaste-share.jpg";

	private int mSkip = 0;
	private int mStep = 5;
	private PrettyTime mPrettyTime;

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		mPrettyTime = new PrettyTime();
		mContext = this;
		mVideoDB = new VideoDB(mContext, VideoDB.NAME, null, VideoDB.VERSION);
		mVideoInfo = (VideoDataFormat) (getIntent().getExtras()
				.getSerializable("VideoInfo"));
		mUser = new User(mContext);
		setContentView(R.layout.activity_play);
		mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		if (mSharedPreferences.getBoolean("use_hd", true)) {
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
		mLoadingGif = (GifMovieView) findViewById(R.id.loading_gif);
		mHeaderWrapper = (RelativeLayout) findViewById(R.id.HeaderWrapper);
		mZoomButton = (Button) findViewById(R.id.screen_btn);
		mRecommandContent = (TextView) findViewById(R.id.recommand_content);
		mRecommandTitle = (TextView) findViewById(R.id.recommand_title);
		mRecommandThumb = (ImageView) findViewById(R.id.thumb);
		mComments = (LinearLayout) findViewById(R.id.comments);
		mRecommandView = findViewById(R.id.recommand_view);
		mZoomButton.setOnClickListener(this);

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

		if (getShareFile() != null) {
			getShareFile().delete();
		}
		Picasso.with(mContext).load(mVideoInfo.DetailPic)
				.placeholder(R.drawable.big_bg).into(this);

		mOrientationEventListener = new OrientationEventListener(mContext) {

			@Override
			public void onOrientationChanged(int orientation) {
				if (mVideoView.isPlaying()) {
					int tending = OrientationHelper.userTending(orientation,
							mCurrentScape);
					if (tending != OrientationHelper.NOTHING) {
						if (tending == OrientationHelper.LANDSCAPE) {
							setFullScreenPlay();
						} else if (tending == OrientationHelper.PORTRAIT) {
							setSmallScreenPlay();
						}
					}
				}
			}
		};

		if (mOrientationEventListener.canDetectOrientation()) {
			mOrientationEventListener.enable();
		}
		mVideoInfo.setFav(mVideoDB.isFav(mVideoInfo.Id));

		findViewById(R.id.comment_edit_text).setOnClickListener(this);

		DataHandler.instance().getRandom(1, mRandomeHandler);
		new CommentsTask().execute();
	}

	private JsonHttpResponseHandler mRandomeHandler = new JsonHttpResponseHandler() {
		public void onSuccess(int statusCode, org.json.JSONObject response) {
			if (statusCode == 200) {
				try {
					JSONArray randomList = response.getJSONArray("list");
					JSONObject video = randomList.getJSONObject(0);
					VideoDataFormat videoDataFormat = VideoDataFormat
							.build(video);
					Picasso.with(mContext).load(videoDataFormat.HomePic)
							.placeholder(R.drawable.placeholder_thumb)
							.error(R.drawable.placeholder_fail)
							.into(mRecommandThumb);
					mRecommandTitle.setText(videoDataFormat.Name);
					mRecommandContent.setText(videoDataFormat.Brief);
					mRecommandView.setTag(videoDataFormat);
					mRecommandView.setOnClickListener(PlayActivity.this);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		public void onFailure(Throwable e, org.json.JSONArray errorResponse) {

		};
	};

	@SuppressLint("InlinedApi")
	private void setFullScreenPlay() {
		mVideoControls.hide();
		if (Build.VERSION.SDK_INT >= 9) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		setPlayerWindowSize(FULL_WIDTH, FULL_HEIGHT, false);
		mCurrentScape = OrientationHelper.LANDSCAPE;
		mZoomButton.setBackgroundResource(R.drawable.screensize_zoomin_button);
	}

	private void setSmallScreenPlay() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setPlayerWindowSize(FULL_WIDTH,
				getResources().getDimensionPixelSize(R.dimen.player_height),
				true);
		mCurrentScape = OrientationHelper.PORTRAIT;
		mZoomButton.setBackgroundResource(R.drawable.screensize_zoomout_button);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("VideoPosition", mVideoView.getCurrentPosition());
	}

	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		String shareTitle = getString(R.string.share_video_title);
		shareTitle = String.format(shareTitle, mVideoInfo.Name);
		String shareContent = getString(R.string.share_video_body);
		intent.setType("image/*");
		shareContent = String.format(shareContent, mVideoInfo.Name,
				mVideoInfo.Youku);
		intent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
		File file = getShareFile();
		if (file != null) {
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	private File getShareFile() {
		String path = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + mDir + File.separator + mShareName;
		File file = new File(path);
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.play, menu);
		MenuItem item = menu.findItem(R.id.menu_item_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(item);
		mFavMenuItem = menu.findItem(R.id.action_fav);
		mShareActionProvider.setShareIntent(getDefaultIntent());
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
			headerParams.height = metrics.heightPixels - getStatusBarHeight();
			videoParams.height = metrics.heightPixels - getStatusBarHeight();
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

	public void comment() {
		MobclickAgent.onEvent(mContext, "comment");
		if (mUser.isLogin() == false) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.choose_login)
					.setItems(
							new String[] { getString(R.string.weibo),
									getString(R.string.qq) },
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										new SocialPlatform(mContext).auth(
												SinaWeibo.NAME, mAuthHandler);
										break;
									case 1:
										new SocialPlatform(mContext).auth(
												QZone.NAME, mAuthHandler);
										break;
									default:
										break;
									}
								}
							}).setNegativeButton(R.string.cancel_login, null)
					.show();
		} else {
			final EditText editText = new EditText(mContext);
			editText.setHeight(mContext.getResources().getDimensionPixelSize(
					R.dimen.comment_edit_height));
			editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
					250) });
			editText.setGravity(Gravity.LEFT | Gravity.TOP);
			new AlertDialog.Builder(mContext)
					.setTitle(R.string.publish_comment)
					.setView(editText)
					.setNegativeButton(R.string.cancel_publish, null)
					.setPositiveButton(R.string.publish,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									final String content = editText.getText()
											.toString();
									if (content.length() == 0) {
										Toast.makeText(mContext,
												R.string.comment_nothing,
												Toast.LENGTH_SHORT).show();
									} else if (content.length() < 5) {
										Toast.makeText(mContext,
												R.string.comment_too_short,
												Toast.LENGTH_SHORT).show();
									} else {
										new Thread() {
											public void run() {
												ParseObject parseComment = new ParseObject(
														"Comments");
												ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
														"Users");
												try {
													ParseObject user = query
															.get(mSharedPreferences
																	.getString(
																			"objectid",
																			"0"));
													parseComment.put("vid",
															mVideoInfo.Id);
													parseComment.put("uid",
															user);
													parseComment.put("content",
															content);
													parseComment
															.saveInBackground(new SaveCallback() {

																@Override
																public void done(
																		ParseException err) {
																	if (err == null) {
																		PlayActivity.this
																				.runOnUiThread(new Runnable() {

																					@Override
																					public void run() {
																						Toast.makeText(
																								mContext,
																								R.string.comment_success,
																								Toast.LENGTH_SHORT)
																								.show();
																						View commentItem = mLayoutInflater
																								.inflate(
																										R.layout.comment_item,
																										null);
																						ImageView avatar = (ImageView) commentItem
																								.findViewById(R.id.avatar);
																						TextView name = (TextView) commentItem
																								.findViewById(R.id.name);
																						TextView contentTextView = (TextView) commentItem
																								.findViewById(R.id.content);
																						contentTextView
																								.setText(content);
																						name.setText(mUser
																								.getUsername());
																						Picasso.with(
																								mContext)
																								.load(mUser
																										.getAvatar())
																								.into(avatar);
																						mComments
																								.addView(
																										commentItem,
																										1);
																					}
																				});

																	} else {

																		PlayActivity.this
																				.runOnUiThread(new Runnable() {

																					@Override
																					public void run() {
																						Toast.makeText(
																								mContext,
																								R.string.comment_failed,
																								Toast.LENGTH_SHORT)
																								.show();
																					}
																				});

																	}
																}
															});
												} catch (ParseException e) {
													e.printStackTrace();
												}
											};
										}.start();

									}
								}
							}).show();
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.play_button:
			v.setVisibility(View.INVISIBLE);
			mVideoView.setCanBePlayed(true);
			mVideoAction.setVisibility(View.INVISIBLE);
			mVideoView.start();
			setPlayerWindowSize(FULL_WIDTH, getResources()
					.getDimensionPixelSize(R.dimen.player_height), true);
			break;
		case R.id.screen_btn:
			if (mOrientationEventListener != null) {
				mOrientationEventListener.disable();
			}

			if (mCurrentScape == OrientationHelper.LANDSCAPE) {
				setSmallScreenPlay();
			} else if (mCurrentScape == OrientationHelper.PORTRAIT) {
				setFullScreenPlay();
			}
			break;
		case R.id.comment_edit_text:
			comment();
			break;
		case R.id.recommand_view:
			prepareStop();
			mVideoView.stopPlayback();
			VideoDataFormat videoDataFormat = (VideoDataFormat) v.getTag();
			Intent intent = new Intent(mContext, PlayActivity.class);
			intent.putExtra("VideoInfo", videoDataFormat);
			mContext.startActivity(intent);
			MobclickAgent.onEvent(mContext, "recommend");
			finish();
			break;
		default:
			break;
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
		mp.setPlaybackSpeed(0.999999f);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mCurrentScape == OrientationHelper.LANDSCAPE) {
				setSmallScreenPlay();
				return true;
			} else {
				prepareStop();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mOrientationEventListener != null)
			mOrientationEventListener.disable();
	}

	/**
	 * 这是播放器的一个Bug,要是直接退出就会出现杂音，一定要在播放状态退出 才不会有杂音
	 */
	private void prepareStop() {
		mVideoView.setVolume(0.0f, 0.0f);
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
					Toast.makeText(mContext, R.string.fav_del_success,
							Toast.LENGTH_SHORT).show();
					item.setIcon(R.drawable.ab_fav_normal);
					mVideoInfo.setFav(false);
				} else {
					Toast.makeText(mContext, R.string.fav_del_fail,
							Toast.LENGTH_SHORT).show();
				}
			} else {
				if (mVideoDB.insertFav(mVideoInfo) > 0) {
					Toast.makeText(mContext, R.string.fav_success,
							Toast.LENGTH_SHORT).show();
					item.setIcon(R.drawable.ab_fav_active);
					mVideoInfo.setFav(true);
				} else {
					Toast.makeText(mContext, R.string.fav_fail,
							Toast.LENGTH_SHORT).show();
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
		mVideoView.setVolume(1.0f, 1.0f);
		mVideoView.seekTo(mPreviousPlayPosition);
		MobclickAgent.onResume(mContext);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVideoView.isPlaying() == false) {
			mVideoView.setVolume(0.0f, 0.0f);
		} else {
			mPreviousPlayPosition = mVideoView.getCurrentPosition();
		}
		MobclickAgent.onPause(mContext);
	}

	@Override
	public void onBitmapFailed() {
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(getDefaultIntent());
		}
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, LoadedFrom arg1) {
		mDetailImageView.setImageBitmap(bitmap);
		mDetailPicture = bitmap;
		mLoadingGif.setVisibility(View.INVISIBLE);
		mPlayButton.setVisibility(View.VISIBLE);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		mDetailPicture.compress(CompressFormat.JPEG, 100, bytes);
		File dir = new File(Environment.getExternalStorageDirectory()
				+ File.separator + mDir);
		if (dir.exists() == false || dir.isDirectory() == false)
			dir.mkdir();

		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + mDir + File.separator + mShareName);
		try {
			file.createNewFile();
			FileOutputStream fo = new FileOutputStream(file);
			fo.write(bytes.toByteArray());
			fo.close();
			if (mShareActionProvider != null) {
				mShareActionProvider.setShareIntent(getDefaultIntent());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class CommentsTask extends AsyncTask<Void, LinearLayout, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mLoadMoreComment != null) {
				mLoadMoreComment.findViewById(R.id.load_progressbar)
						.setVisibility(View.VISIBLE);
				mLoadMoreComment.findViewById(R.id.load_more_comment_btn)
						.setVisibility(View.GONE);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
					"Comments");
			query.whereEqualTo("vid", mVideoInfo.Id);
			query.setLimit(mStep);
			query.setSkip(mSkip);
			query.include("uid");
			query.orderByDescending("updatedAt");
			try {
				List<ParseObject> commentList = query.find();
				if (commentList.size() < mStep) {
					mCommentFinished = true;
				}
				ArrayList<LinearLayout> commentsLayout = new ArrayList<LinearLayout>();
				for (ParseObject comment : commentList) {
					ParseObject user = comment.getParseObject("uid");
					Comment commentInformation = new Comment(
							user.getString("username"),
							user.getString("avatar"),
							user.getString("platform"), comment.getUpdatedAt(),
							comment.getString("content"));
					LinearLayout commentItem = (LinearLayout) mLayoutInflater
							.inflate(R.layout.comment_item, null);
					TextView content = (TextView) commentItem
							.findViewById(R.id.content);
					content.setText(commentInformation.Content);
					ImageView avatar = (ImageView) commentItem
							.findViewById(R.id.avatar);
					Picasso.with(mContext).load(commentInformation.Avatar)
							.into(avatar);
					TextView username = (TextView) commentItem
							.findViewById(R.id.name);
					username.setText(commentInformation.Username);
					TextView date = (TextView) commentItem
							.findViewById(R.id.time);
					date.setText(mPrettyTime.format(commentInformation.Date));
					commentsLayout.add(commentItem);
				}
				mSkip += mStep;
				publishProgress(commentsLayout
						.toArray(new LinearLayout[commentList.size()]));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(LinearLayout... values) {
			super.onProgressUpdate(values);
			if (mLoadMoreComment != null) {
				mComments.removeView(mLoadMoreComment);
			}
			for (LinearLayout commentView : values) {
				mComments.addView(commentView);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			mLoadMoreComment = mLayoutInflater.inflate(
					R.layout.comment_load_more, null);
			mLoadMoreButton = (Button) mLoadMoreComment
					.findViewById(R.id.load_more_comment_btn);

			mComments.addView(mLoadMoreComment);
			if (mCommentFinished == false) {
				mLoadMoreButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						new CommentsTask().execute();
						MobclickAgent.onEvent(mContext, "more_comment");
					}
				});
			} else {
				mLoadMoreButton.setText(R.string.no_more_comments);
				mLoadMoreButton.setEnabled(false);
			}
		}
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

	@SuppressLint("HandlerLeak")
	private Handler mAuthHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SocialPlatform.AUTH_SUCCESS:
				Toast.makeText(mContext, R.string.login_success,
						Toast.LENGTH_SHORT).show();
				break;
			case SocialPlatform.AUTH_FAILED:
				Toast.makeText(mContext, R.string.login_failed,
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
}