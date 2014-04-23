package com.zhan_dui.animetaste;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.text.InputFilter;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import com.avos.avoscloud.ParseException;
import com.avos.avoscloud.ParseObject;
import com.avos.avoscloud.ParseQuery;
import com.avos.avoscloud.SaveCallback;
import com.baidu.cyberplayer.core.BVideoView;
import com.baidu.cyberplayer.core.BVideoView.OnCompletionListener;
import com.baidu.cyberplayer.core.BVideoView.OnErrorListener;
import com.baidu.cyberplayer.core.BVideoView.OnPreparedListener;
import com.basv.gifmoviewview.widget.GifMovieView;
import com.github.johnpersano.supertoasts.SuperToast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.auth.SocialPlatform;
import com.zhan_dui.auth.User;
import com.zhan_dui.data.AnimeTasteDB;
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.modal.Comment;
import com.zhan_dui.utils.OrientationHelper;
import com.zhan_dui.utils.Screen;
import com.zhan_dui.utils.SwipeBackAppCompatActivity;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class PlayActivity extends SwipeBackAppCompatActivity implements OnClickListener,
		Target, OnPreparedListener, OnCompletionListener, OnErrorListener,
		OnTouchListener {

	private TextView mTitleTextView;
	private TextView mContentTextView;
	private TextView mAuthorTextView;
	private android.support.v7.widget.ShareActionProvider mShareActionProvider;
	private ImageView mDetailImageView;
	private ImageButton mPrePlayButton;
	private GifMovieView mLoadingGif;

	private int mCurrentScape;

	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private AnimeTasteDB mAnimeTasteDB;

	private View mVideoAction;

	private Animation mAnimation;

	private OrientationEventListener mOrientationEventListener;



	private MenuItem mFavMenuItem;
	private Bitmap mDetailPicture;
	private LinearLayout mComments, mRecomendList;
	private LayoutInflater mLayoutInflater;
	private RelativeLayout mHeaderWrpper;
	private View mLoadMoreComment;
	private View mRecommandView;
	private Button mLoadMoreButton;
	private Button mZoomButton;

	private boolean mCommentFinished;
	private User mUser;

	private final String mDir = "AnimeTaste";
	private final String mShareName = "animetaste-share.jpg";
	private int mCommentCount;

	private int mSkip = 0;
	private int mStep = 5;
	private int mLastPos = 0;
	private final int UI_EVENT_UPDATE_CURRPOSITION = 1;

	private PrettyTime mPrettyTime;
	private BVideoView mVV = null;
	private RelativeLayout mViewHolder = null;
	private RelativeLayout mController = null;
	private SeekBar mProgress = null;
	private TextView mDuration = null;
	private TextView mCurrPostion = null;
	private Button mPlaybtn = null;
	private EditText mCommentEditText;
	private String AK = "TrqQtzMhuoKhyLmNsfvwfWDo";
	private String SK = "UuhbIKiNfr8SA3NM";

	private Typeface mRobotoBold, mRobotoThin;

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		mPrettyTime = new PrettyTime();
		mContext = this;
		mAnimeTasteDB = new AnimeTasteDB(mContext, AnimeTasteDB.NAME, null, AnimeTasteDB.VERSION);

		if (getIntent().getExtras().containsKey("Animation")) {
            mAnimation = getIntent().getParcelableExtra("Animation");
		}

		if (savedInstance != null && savedInstance.containsKey("Animation")) {
			mAnimation = savedInstance.getParcelable("Animation");
			mLastPos = savedInstance.getInt("LastPosition");
		}

		mUser = new User(mContext);

		setContentView(R.layout.activity_play);
		mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		mCurrentScape = OrientationHelper.PORTRAIT;
		mTitleTextView = (TextView) findViewById(R.id.title);
		mContentTextView = (TextView) findViewById(R.id.content);
		mDetailImageView = (ImageView) findViewById(R.id.detailPic);
		mVideoAction = (View) findViewById(R.id.VideoAction);
		mAuthorTextView = (TextView) findViewById(R.id.author);
		mPrePlayButton = (ImageButton) findViewById(R.id.pre_play_button);
		mLoadingGif = (GifMovieView) findViewById(R.id.loading_gif);
		mComments = (LinearLayout) findViewById(R.id.comments);
		mRecommandView = findViewById(R.id.recommand_view);
		mPlaybtn = (Button) findViewById(R.id.play_btn);
		mProgress = (SeekBar) findViewById(R.id.media_progress);
		mDuration = (TextView) findViewById(R.id.time_total);
		mCurrPostion = (TextView) findViewById(R.id.time_current);
		mController = (RelativeLayout) findViewById(R.id.controlbar);
		mViewHolder = (RelativeLayout) findViewById(R.id.view_holder);
		mVV = (BVideoView) findViewById(R.id.video_view);
		mCommentEditText = (EditText) findViewById(R.id.comment_edit_text);
		mHeaderWrpper = (RelativeLayout) findViewById(R.id.header_wrapper);
		mZoomButton = (Button) findViewById(R.id.zoom_btn);
		mRecomendList = (LinearLayout) findViewById(R.id.recommend_list);
		mRobotoBold = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Bold.ttf");
		mRobotoThin = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Thin.ttf");
		initPlayer();
		initContent();
        mAnimation.recordWatch();
		ApiConnector.instance().getRandom(5, mRandomHandler);
		new CommentsTask().execute();
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("LastPosition", mLastPos);
        outState.putParcelable("Animation", mAnimation);
	}

    private class RandomRecommendTask extends AsyncTask<Void,Void,Void>{

        private JSONArray mRandomJsonArray;
        private LinearLayout mRandomLayout;

        public RandomRecommendTask(JSONArray recommendJsonArray){
            mRandomJsonArray = recommendJsonArray;
            mRandomLayout = new LinearLayout(mContext);
            mRandomLayout.setOrientation(LinearLayout.VERTICAL);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < mRandomJsonArray.length(); i++) {
                LinearLayout recommend_item = (LinearLayout) mLayoutInflater
                        .inflate(R.layout.recommend_item, null);
                ImageView recommendThumb = (ImageView) recommend_item
                        .findViewById(R.id.thumb);
                TextView recommendTitle = (TextView) recommend_item
                        .findViewById(R.id.recommand_title);
                TextView recommendContent = (TextView) recommend_item
                        .findViewById(R.id.recommand_content);
                try{
                JSONObject animationObject = mRandomJsonArray.getJSONObject(i);
                Animation animation = Animation
                        .build(animationObject);
                Picasso.with(mContext).load(animation.HomePic)
                        .placeholder(R.drawable.placeholder_thumb)
                        .error(R.drawable.placeholder_fail)
                        .into(recommendThumb);
                recommendTitle.setText(animation.Name);
                recommendContent.setText(animation.Brief);
                recommend_item.setTag(animation);
                recommend_item.setOnClickListener(PlayActivity.this);
                View line = recommend_item
                        .findViewById(R.id.divide_line);
                if (i == mRandomJsonArray.length() - 1 && line != null) {
                    recommend_item.removeView(line);
                }
                mRandomLayout.addView(recommend_item);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mRecomendList.addView(mRandomLayout);
        }
    }

	private JsonHttpResponseHandler mRandomHandler = new JsonHttpResponseHandler() {
		public void onSuccess(int statusCode, org.json.JSONObject response) {
			if (statusCode == 200) {
				try {
					JSONArray animations = response.getJSONObject("data").getJSONObject("list").getJSONArray("anime");
                    new RandomRecommendTask(animations).execute();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	};

	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		String shareTitle = getString(R.string.share_video_title);
		shareTitle = String.format(shareTitle, mAnimation.Name);
		String shareContent = getString(R.string.share_video_body);
		intent.setType("image/*");
		shareContent = String.format(shareContent, mAnimation.Name,
				mAnimation.Youku);
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
        mShareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);

		mShareActionProvider
				.setOnShareTargetSelectedListener(new android.support.v7.widget.ShareActionProvider.OnShareTargetSelectedListener() {
                    @Override
                    public boolean onShareTargetSelected(android.support.v7.widget.ShareActionProvider shareActionProvider, Intent intent) {
                        MobclickAgent.onEvent(mContext, "share");
                        pausePlay();
                        return true;
                    }
				});
		mFavMenuItem = menu.findItem(R.id.action_fav);
		mShareActionProvider.setShareIntent(getDefaultIntent());
        mAnimation.checkIsFavorite(new Animation.UpdateFinishCallback() {
            @Override
            public void onUpdateFinished(Animation.Method method, Message msg) {
                if(msg.arg1 == 1){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mFavMenuItem.setIcon(R.drawable.ab_fav_active);
                        }
                    });
                }
            }
        });
		return true;
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
			AlertDialog.Builder commentDialog = new AlertDialog.Builder(
					mContext).setTitle(R.string.publish_comment)
					.setView(editText)
					.setNegativeButton(R.string.cancel_publish, null)
					.setPositiveButton(R.string.publish, null);
			final AlertDialog dialog = commentDialog.create();
			dialog.show();
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
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
								new CommentThread(content).start();
								dialog.dismiss();
							}
						}
					});

		}
	}

	private class CommentThread extends Thread {
		private String mContent;

		private CommentThread(String commentContent) {
			mContent = commentContent;
		}

		@Override
		public void run() {
			super.run();
			ParseObject parseComment = new ParseObject("Comments");
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Users");
			try {
				ParseObject user = query.get(mSharedPreferences.getString(
						"objectid", "0"));
				parseComment.put("vid", mAnimation.AnimationId);
				parseComment.put("uid", user);
				parseComment.put("content", mContent);
				parseComment.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException err) {
						if (err == null) {
							PlayActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(mContext,
											R.string.comment_success,
											Toast.LENGTH_SHORT).show();
									View commentItem = mLayoutInflater.inflate(
											R.layout.comment_item, null);
									ImageView avatar = (ImageView) commentItem
											.findViewById(R.id.avatar);
									TextView name = (TextView) commentItem
											.findViewById(R.id.name);
									TextView contentTextView = (TextView) commentItem
											.findViewById(R.id.content);
									contentTextView.setText(mContent);
									name.setText(mUser.getUsername());
									Picasso.with(mContext)
											.load(mUser.getAvatar())
											.into(avatar);
									mComments.addView(commentItem, 1);
								}
							});

						} else {

							PlayActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(mContext,
											R.string.comment_failed,
											Toast.LENGTH_SHORT).show();
								}
							});

						}
					}
				});
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.pre_play_button:
			mPrePlayButton.setVisibility(View.INVISIBLE);
			mVideoAction.setVisibility(View.INVISIBLE);
			mPlaybtn.performClick();
			startPlay(mLastPos);
			updateControlBar(false);
			if (mSharedPreferences.getBoolean("use_hd", true)) {
				mVV.setVideoPath(mAnimation.HDVideoUrl);
			} else {
				mVV.setVideoPath(mAnimation.CommonVideoUrl);
			}
			break;
		case R.id.play_btn:
			if (mVV.isPlaying()) {
				mPlaybtn.setBackgroundResource(R.drawable.play_btn_style);
				mVV.pause();
			} else {
				mPlaybtn.setBackgroundResource(R.drawable.pause_btn_style);
				mVV.resume();
			}
			mController.setVisibility(View.INVISIBLE);
			break;
		case R.id.comment_edit_text:
			comment();
			break;
		case R.id.recommend_item:
			stopPlay();
			Animation animation = (Animation) v.getTag();
			Intent intent = new Intent(mContext, PlayActivity.class);
			intent.putExtra("Animation", animation);
			mContext.startActivity(intent);
			MobclickAgent.onEvent(mContext, "recommend");
			finish();
			break;
		case R.id.zoom_btn:
			if (mCurrentScape == OrientationHelper.LANDSCAPE) {
				setMinSize();
			} else {
				setMaxSize();
			}
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_fav:
			if (mAnimation.isFavorite()) {
                mAnimation.removeFromFavorite(new Animation.UpdateFinishCallback() {
                    @Override
                    public void onUpdateFinished(Animation.Method method,Message msg) {
                        Toast.makeText(mContext, R.string.fav_del_success,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                item.setIcon(R.drawable.ab_fav_normal);
			} else {
                mAnimation.addToFavorite(new Animation.UpdateFinishCallback() {
                    @Override
                    public void onUpdateFinished(Animation.Method method,Message msg) {
                        Toast.makeText(mContext, R.string.fav_success,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                item.setIcon(R.drawable.ab_fav_active);
			}
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
		mPrePlayButton.setVisibility(View.VISIBLE);
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
			query.whereEqualTo("vid", mAnimation.AnimationId);
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
				mCommentCount += commentList.size();
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
			if (mCommentFinished == false) {
				mLoadMoreComment = mLayoutInflater.inflate(
						R.layout.comment_load_more, null);
				mLoadMoreButton = (Button) mLoadMoreComment
						.findViewById(R.id.load_more_comment_btn);
				mComments.addView(mLoadMoreComment);
				mLoadMoreButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						new CommentsTask().execute();
						MobclickAgent.onEvent(mContext, "more_comment");
					}
				});
			} else {
				if (mCommentCount > 5) {
					mLoadMoreButton.setText(R.string.no_more_comments);
					mLoadMoreButton.setEnabled(false);
				}
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

	@SuppressLint("HandlerLeak")
	Handler mUIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UI_EVENT_UPDATE_CURRPOSITION:
				int currPosition = mVV.getCurrentPosition();
				int duration = mVV.getDuration();
				updateTextViewWithTimeFormat(mCurrPostion, currPosition);
				updateTextViewWithTimeFormat(mDuration, duration);
				mProgress.setMax(duration);
				mProgress.setProgress(currPosition);

				mUIHandler.sendEmptyMessageDelayed(
						UI_EVENT_UPDATE_CURRPOSITION, 200);
				break;
			default:
				break;
			}
		}
	};

	private void updateTextViewWithTimeFormat(TextView view, int second) {
		int hh = second / 3600;
		int mm = second % 3600 / 60;
		int ss = second % 60;
		String strTemp = null;
		if (0 != hh) {
			strTemp = String.format(Locale.CHINA, "%02d:%02d:%02d", hh, mm, ss);
		} else {
			strTemp = String.format(Locale.CHINA, "%02d:%02d", mm, ss);
		}
		view.setText(strTemp);
	}

	public void updateControlBar(boolean show) {
		if (show) {
			if (mController.getVisibility() == View.INVISIBLE) {
				mController.setVisibility(View.VISIBLE);
				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						PlayActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								updateControlBar(false);
							}
						});
					}
				}, 4000);
			}
		} else {
			mController.setVisibility(View.INVISIBLE);
		}
	}

	private void initPlayer() {
		BVideoView.setAKSK(AK, SK);
		mZoomButton.setOnClickListener(this);
		mVV.setVideoScalingMode(BVideoView.VIDEO_SCALING_MODE_SCALE_TO_FIT);
		mPlaybtn.setOnClickListener(this);
		mVV.setOnPreparedListener(this);
		mVV.setOnCompletionListener(this);
		mVV.setOnErrorListener(this);
		registerCallbackForControls();
	}

	private void initContent() {
		mTitleTextView.setTypeface(mRobotoBold);
		mAuthorTextView.setTypeface(mRobotoThin);
		mTitleTextView.setText(mAnimation.Name);
		mContentTextView.setText(mAnimation.Brief);
		mAuthorTextView.setText(mAnimation.Author + " · " + mAnimation.Year);
		mPrePlayButton.setOnClickListener(this);
		mViewHolder.setOnTouchListener(this);
		mCommentEditText.setOnClickListener(this);
		if (getShareFile() != null) {
			getShareFile().delete();
		}
		Picasso.with(mContext).load(mAnimation.DetailPic)
				.placeholder(R.drawable.big_bg).into(this);
	}

	private void startPlay(int from) {
		setMaxSize();
		mVV.seekTo(from);
		mVV.start();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void stopPlay() {
		if (mVV.isPlaying() == false)
			return;
		mLastPos = mVV.getCurrentPosition();
		mVV.stopPlayback();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void pausePlay() {
		if (mVV.isPlaying() == false)
			return;
		mLastPos = mVV.getCurrentPosition();
		mVV.pause();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@SuppressLint("InlinedApi")
	private void setMaxSize() {
		if (Build.VERSION.SDK_INT >= 9) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(Build.MODEL.equals("M040")){
            if(!mSharedPreferences.getBoolean("Meizu",false)){
                SuperToast superToast = new SuperToast(this);
                superToast.setDuration(12000);
                superToast.setText("魅族某些版本固件可能存在兼容性问题，建议您升级到最新固件");
                superToast.setIconResource(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                superToast.show();
                mSharedPreferences.edit().putBoolean("Meizu",true).commit();
            }
        }
        getSupportActionBar().hide();
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
				Screen.getScreenWidth(getWindowManager()),
				Screen.getScreenHeight(getWindowManager()));
		mHeaderWrpper.setLayoutParams(param);
		mVV.setLayoutParams(param);
		mZoomButton.setBackgroundResource(R.drawable.screensize_zoomin_button);
		mCurrentScape = OrientationHelper.LANDSCAPE;

	}

	private void setMinSize() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getSupportActionBar().show();
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
				Screen.getScreenWidth(getWindowManager()), getResources()
						.getDimensionPixelSize(R.dimen.player_height));
		mHeaderWrpper.setLayoutParams(param);
		mVV.setLayoutParams(param);
		mZoomButton.setBackgroundResource(R.drawable.screensize_zoomout_button);
		mCurrentScape = OrientationHelper.PORTRAIT;
	}

	private void registerCallbackForControls() {

		OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateTextViewWithTimeFormat(mCurrPostion, progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				mUIHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				int iseekPos = seekBar.getProgress();
				mVV.seekTo(iseekPos);
				mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
			}
		};
		mProgress.setOnSeekBarChangeListener(seekBarChangeListener);
	}

	@Override
	public boolean onError(int arg0, int arg1) {
		PlayActivity.this.runOnUiThread(new Runnable() {
		            @Override
		            public void run() {
		                Toast.makeText(mContext, R.string.play_error, Toast.LENGTH_SHORT)
		                        .show();
		            }
	        	});
		return true;
	}

	@SuppressLint("HandlerLeak")
	private Handler mCompleteHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mPrePlayButton.setVisibility(View.VISIBLE);
			mVideoAction.setVisibility(View.VISIBLE);
		};
	};

	@Override
	public void onCompletion() {
		mCompleteHandler.sendEmptyMessage(0);
		mLastPos = 0;
		int playcount = mSharedPreferences.getInt("playcount", 0);
		mSharedPreferences.edit().putInt("playcount", playcount + 1).commit();
		if (mCurrentScape == OrientationHelper.LANDSCAPE) {
			PlayActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					setMinSize();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mLastPos != 0) {
			startPlay(mLastPos);
		}
		MobclickAgent.onResume(mContext);
	}

	@Override
	protected void onPause() {
		super.onPause();
		pausePlay();
		MobclickAgent.onPause(mContext);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopPlay();
		if (mOrientationEventListener != null)
			mOrientationEventListener.disable();
	}

	@Override
	public void onPrepared() {
		mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		updateControlBar(true);
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mCurrentScape == OrientationHelper.LANDSCAPE) {
				setMinSize();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
};
