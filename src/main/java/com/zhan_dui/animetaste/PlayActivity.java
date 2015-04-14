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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
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
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.download.DownloadHelper;
import com.zhan_dui.model.Animation;
import com.zhan_dui.model.Comment;
import com.zhan_dui.model.DownloadRecord;
import com.zhan_dui.sns.ShareHelper;
import com.zhan_dui.utils.NetworkUtils;
import com.zhan_dui.utils.OrientationHelper;
import com.zhan_dui.utils.Screen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;

public class PlayActivity extends ActionBarActivity implements OnClickListener,
        Target, OnPreparedListener, OnCompletionListener, OnErrorListener,
        OnTouchListener {

    private TextView mTitleTextView;
    private TextView mContentTextView;
    private TextView mAuthorTextView;
    //    private android.support.v7.widget.ShareActionProvider mShareActionProvider;
    private ImageView mDetailImageView;
    private ImageButton mPrePlayButton;
    private GifMovieView mLoadingGif;

    private int mCurrentScape;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private View mVideoAction;

    private Animation mAnimation;

    private OrientationEventListener mOrientationEventListener;

    private MenuItem mFavMenuItem;
    private Bitmap mDetailPicture;
    private LinearLayout mComments, mRecomendView;
    private LayoutInflater mLayoutInflater;
    private RelativeLayout mHeaderWrapper;
    private View mLoadMoreComment;
    private View mRecommendView;
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
    private TextView mCurPosition = null;
    private Button mPlayBtn = null;
    private EditText mCommentEditText;
    private String AK = "TrqQtzMhuoKhyLmNsfvwfWDo";
    private String SK = "UuhbIKiNfr8SA3NM";

    private Typeface mRobotoBold, mRobotoThin;

    private DownloadHelper mDownloadHelper;
    private PLAY_STATE mPlayState;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);


        AVOSCloud.initialize(this,
                "w43xht9daji0uut74pseeiibax8c2tnzxowmx9f81nvtpims",
                "86q8251hrodk6wnf4znistay1mva9rm1xikvp1s9mhp5n7od");

        mPrettyTime = new PrettyTime();
        mContext = this;

        if (getIntent().getExtras().containsKey("Animation")) {
            mAnimation = getIntent().getParcelableExtra("Animation");
        }
        mDownloadHelper = new DownloadHelper(this);
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
        mVideoAction = findViewById(R.id.VideoAction);
        mAuthorTextView = (TextView) findViewById(R.id.author);
        mPrePlayButton = (ImageButton) findViewById(R.id.pre_play_button);
        mLoadingGif = (GifMovieView) findViewById(R.id.loading_gif);
        mComments = (LinearLayout) findViewById(R.id.comments);
        mRecommendView = findViewById(R.id.recommand_view);
        mPlayBtn = (Button) findViewById(R.id.play_btn);
        mProgress = (SeekBar) findViewById(R.id.media_progress);
        mDuration = (TextView) findViewById(R.id.time_total);
        mCurPosition = (TextView) findViewById(R.id.time_current);
        mController = (RelativeLayout) findViewById(R.id.controlbar);
        mViewHolder = (RelativeLayout) findViewById(R.id.view_holder);
        mVV = (BVideoView) findViewById(R.id.video_view);
        mCommentEditText = (EditText) findViewById(R.id.comment_edit_text);
        mHeaderWrapper = (RelativeLayout) findViewById(R.id.header_wrapper);
        mZoomButton = (Button) findViewById(R.id.zoom_btn);
        mRecomendView = (LinearLayout) findViewById(R.id.recommend_list);
        mRobotoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Bold.ttf");
        mRobotoThin = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");
        initPlayer();
        initContent();
        mAnimation.recordWatch();
        ApiConnector.instance().getRandom(5, mRandomHandler);
        new CommentsTask().execute();
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        showWhatsNew();
    }

    public void showWhatsNew() {
        int shown = mSharedPreferences.getInt("new_share_tips", 0);
        if (shown < 2) {
            Toast.makeText(this, "分享功能全面更新啦！", Toast.LENGTH_LONG).show();
            ShareHelper.showUp(this, mAnimation);
            mSharedPreferences.edit().putInt("new_share_tips", shown + 1).apply();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("LastPosition", mLastPos);
        outState.putParcelable("Animation", mAnimation);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("Animation")) {
            mAnimation = savedInstanceState.getParcelable("Animation");
            mLastPos = savedInstanceState.getInt("LastPosition");
        }
    }

    private class RandomRecommendTask extends AsyncTask<Void, Void, Void> {

        private JSONArray mRandomJsonArray;
        private LinearLayout mRandomLayout;

        public RandomRecommendTask(JSONArray recommendJsonArray) {
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
                try {
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
                    View line = mRecommendView
                            .findViewById(R.id.divide_line);
                    if (i == mRandomJsonArray.length() - 1 && line != null) {
                        recommend_item.removeView(line);
                    }
                    mRandomLayout.addView(recommend_item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mRecomendView.addView(mRandomLayout);
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
        }

        ;
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
        mFavMenuItem = menu.findItem(R.id.action_fav);
        mAnimation.checkIsFavorite(new Animation.UpdateFinishCallback() {
            @Override
            public void onUpdateFinished(Animation.Method method, Message msg) {
                if (msg.arg1 == 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mFavMenuItem.setIcon(R.drawable.ic_action_action_favorite);
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
                            new String[]{getString(R.string.weibo),
                                    getString(R.string.qq)},
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
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(
                    250)});
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
            AVObject parseComment = new AVObject("Comments");
            AVQuery<AVObject> query = new AVQuery<AVObject>("Users");
            try {
                AVObject user = query.get(mSharedPreferences.getString(
                        "objectid", "0"));
                parseComment.put("vid", mAnimation.AnimationId);
                parseComment.put("uid", user);
                parseComment.put("content", mContent);
                parseComment.saveInBackground(new SaveCallback() {

                    @Override
                    public void done(AVException err) {
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
            } catch (AVException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pre_play_button:
                startPlayAnimation(mLastPos, mAnimation);
                break;
            case R.id.play_btn:
                if (mVV.isPlaying()) {
                    mPlayBtn.setBackgroundResource(R.drawable.play_btn_style);
                    pausePlay();
                } else {
                    mPlayBtn.setBackgroundResource(R.drawable.pause_btn_style);
                    restorePlay();
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

    private void startPlayAnimation(final int start, Animation animation) {
        final String playUrl = mSharedPreferences.getBoolean("use_hd", true) ? mAnimation.HDVideoUrl : mAnimation.CommonVideoUrl;
        DownloadRecord record = DownloadRecord.getFromAnimation(mAnimation, true);
        if (record != null) {
            String path = record.SaveDir + record.SaveFileName;
            File f = new File(path);
            if (f.isFile() && f.exists()) {
                mPrePlayButton.setVisibility(View.INVISIBLE);
                mVideoAction.setVisibility(View.INVISIBLE);
                mVV.setVideoPath(path);
                mVV.seekTo(start);
                mPlayBtn.setBackgroundResource(R.drawable.pause_btn_style);
                mVV.start();
                hideControls();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                return;
            } else {
                Toast.makeText(mContext, R.string.offline_file_missing, Toast.LENGTH_LONG).show();
            }
        }
        startPlayAnimationFromNet(playUrl, mLastPos, animation);
    }

    private void startPlayAnimationFromNet(final String url, final int start, Animation animation) {
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            if (NetworkUtils.isWifiConnected(mContext)) {
                play(url, start);
            } else {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.tip)
                        .setMessage(R.string.no_wifi_force_play)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                play(url, start);
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .create()
                        .show();
            }
        } else {
            Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_LONG).show();
        }
    }

    public void play(String url, int start) {
        mPrePlayButton.setVisibility(View.INVISIBLE);
        mVideoAction.setVisibility(View.INVISIBLE);
        mVV.setVideoPath(url);
        mVV.seekTo(start);
        mVV.start();
        mPlayBtn.setBackgroundResource(R.drawable.pause_btn_style);
        hideControls();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
                        public void onUpdateFinished(Animation.Method method, Message msg) {
                            Toast.makeText(mContext, R.string.fav_del_success,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    item.setIcon(R.drawable.ic_action_action_favorite_outline);
                } else {
                    mAnimation.addToFavorite(new Animation.UpdateFinishCallback() {
                        @Override
                        public void onUpdateFinished(Animation.Method method, Message msg) {
                            Toast.makeText(mContext, R.string.fav_success,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    item.setIcon(R.drawable.ic_action_action_favorite);
                }
                return true;
            case R.id.action_download:
                mDownloadHelper.startDownload(mAnimation);
                break;
            case R.id.menu_item_share:
                ShareHelper.showUp(mContext, mAnimation);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBitmapFailed() {
        mLoadingGif.setVisibility(View.INVISIBLE);
        mPrePlayButton.setVisibility(View.VISIBLE);
        mVideoAction.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, LoadedFrom arg1) {
        mDetailImageView.setImageBitmap(bitmap);
        mDetailPicture = bitmap;
        mLoadingGif.setVisibility(View.INVISIBLE);
        mPrePlayButton.setVisibility(View.VISIBLE);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mDetailPicture.compress(CompressFormat.JPEG, 100, bytes);

        File file = new File(mContext.getCacheDir(), "toshare.jpg");
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class CommentsTask extends AsyncTask<Void, LinearLayout, Boolean> {

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
        protected Boolean doInBackground(Void... params) {
            AVQuery<AVObject> query = new AVQuery<AVObject>(
                    "Comments");
            query.whereEqualTo("vid", mAnimation.AnimationId);
            query.setLimit(mStep);
            query.setSkip(mSkip);
            query.include("uid");
            query.orderByDescending("updatedAt");
            try {
                List<AVObject> commentList = query.find();
                if (commentList.size() < mStep) {
                    mCommentFinished = true;
                }
                ArrayList<LinearLayout> commentsLayout = new ArrayList<LinearLayout>();
                for (AVObject comment : commentList) {
                    AVObject user = comment.getAVObject("uid");
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
                return true;
            } catch (AVException e) {
                return false;
            }
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
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
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
            } else {
                if (mLoadMoreComment != null) {
                    mLoadMoreComment.findViewById(R.id.load_progressbar)
                            .setVisibility(View.INVISIBLE);
                    mLoadMoreComment.findViewById(R.id.load_more_comment_btn)
                            .setVisibility(View.VISIBLE);
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
        }

        ;
    };

    @SuppressLint("HandlerLeak")
    Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UI_EVENT_UPDATE_CURRPOSITION:
                    int currPosition = mVV.getCurrentPosition();
                    int duration = mVV.getDuration();
                    updateTextViewWithTimeFormat(mCurPosition, currPosition);
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

    private Timer mt;

    public void touchControlBar() {
        if (mController.getVisibility() == View.INVISIBLE) {
            mController.setVisibility(View.VISIBLE);
            mt = new Timer();
            mt.schedule(new TimerTask() {
                @Override
                public void run() {
                    PlayActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mController.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }, 6000);
        } else {
            if (mt != null) {
                mt.cancel();
            }
            mController.setVisibility(View.INVISIBLE);
        }
    }

    public void hideControls() {
        mController.setVisibility(View.INVISIBLE);
    }

    private void initPlayer() {
        BVideoView.setAKSK(AK, SK);
        mZoomButton.setOnClickListener(this);
        mVV.setVideoScalingMode(BVideoView.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        mPlayBtn.setOnClickListener(this);
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
        new Thread() {
            @Override
            public void run() {
                super.run();
                DownloadRecord record = DownloadRecord.getFromAnimation(mAnimation, true);
                if (record != null) {
                    File file = new File(record.SaveDir + record.SaveFileName);
                    if (file.exists() && file.isFile()) {
                        toastHandler.sendEmptyMessage(0);
                    }
                }
            }
        }.start();
    }

    private Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(mContext, R.string.play_offline, Toast.LENGTH_SHORT).show();
        }
    };

    private void stopPlay() {
        if (!mVV.isPlaying())
            return;
        mLastPos = mVV.getCurrentPosition();
        mVV.stopPlayback();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pausePlay() {
        if (!mVV.isPlaying())
            return;
        mLastPos = mVV.getCurrentPosition();
        mVV.pause();
        mPlayState = PLAY_STATE.PAUSE;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void restorePlay() {
        if (mVV != null && !mVV.isPlaying()) {
            mVV.resume();
            mPlayState = PLAY_STATE.PLAYING;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @SuppressLint("InlinedApi")
    private void setMaxSize() {
        if (Build.VERSION.SDK_INT >= 9) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.MODEL.equals("M040")) {
            if (!mSharedPreferences.getBoolean("Meizu", false)) {
                SuperToast superToast = new SuperToast(this);
                superToast.setDuration(12000);
                superToast.setText("魅族某些版本固件可能存在兼容性问题，建议您升级到最新固件");
                superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                superToast.show();
                mSharedPreferences.edit().putBoolean("Meizu", true).apply();
            }
        }
        getSupportActionBar().hide();
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                Screen.getScreenWidth(getWindowManager()),
                Screen.getScreenHeight(getWindowManager()));
        mHeaderWrapper.setLayoutParams(param);
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
        mHeaderWrapper.setLayoutParams(param);
        mVV.setLayoutParams(param);
        mZoomButton.setBackgroundResource(R.drawable.screensize_zoomout_button);
        mCurrentScape = OrientationHelper.PORTRAIT;
    }

    private void registerCallbackForControls() {

        OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                updateTextViewWithTimeFormat(mCurPosition, progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                mUIHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                int seekPosition = seekBar.getProgress();
                mVV.seekTo(seekPosition);
                mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
            }
        };
        mProgress.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    @Override
    public boolean onError(int arg0, int arg1) {
        errorHandler.sendEmptyMessage(0);
        return true;
    }

    private Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setMinSize();
            mPrePlayButton.setVisibility(View.VISIBLE);
            mVideoAction.setVisibility(View.VISIBLE);
            mDetailImageView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, R.string.play_error, Toast.LENGTH_SHORT)
                    .show();
            Intent intent = new Intent(mContext, BrowserPlayerActivity.class);
            intent.putExtra("animation", mAnimation);
            startActivity(intent);
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mCompleteHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mPrePlayButton.setVisibility(View.VISIBLE);
            mVideoAction.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onCompletion() {

        if (mPlayState == PLAY_STATE.PAUSE) {
            return;
        }

        mCompleteHandler.sendEmptyMessage(0);
        mLastPos = 0;
        mPlayState = PLAY_STATE.NONE;
        int playCount = mSharedPreferences.getInt("playCount", 0);
        mSharedPreferences.edit().putInt("playCount", playCount + 1).commit();
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
            startPlayAnimation(mLastPos, mAnimation);
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

        mDownloadHelper.unbindDownloadService();
    }

    @Override
    public void onPrepared() {
        mPlayState = PLAY_STATE.PLAYING;
        mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchControlBar();
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

    static enum PLAY_STATE {
        NONE, PAUSE, PLAYING
    }
}
