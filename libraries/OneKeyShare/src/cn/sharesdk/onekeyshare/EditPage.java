/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */

package cn.sharesdk.onekeyshare;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import cn.sharesdk.framework.FakeActivity;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.TitleLayout;
import cn.sharesdk.framework.utils.UIHandler;

/** 执行图文分享的页面，此页面不支持微信平台的分享 */
public class EditPage extends FakeActivity implements OnClickListener, TextWatcher, Callback {
	private static final int MAX_TEXT_COUNT = 140;
	private static final int MSG_PLATFORM_LIST_GOT = 1;
	private HashMap<String, Object> reqData;
	private OnekeyShare parent;
	private LinearLayout llPage;
	private TitleLayout llTitle;
	// 文本编辑框
	private EditText etContent;
	// 字数计算器
	private TextView tvCounter;
	// 别针图片
	private ImageView ivPin;
	// 输入区域的图片
	private ImageView ivImage;
	private Bitmap image;
	private boolean shareImage;
	private LinearLayout llPlat;
	private LinearLayout llAt;
	// 平台列表
	private Platform[] platformList;
	private View[] views;
	// 设置显示模式为Dialog模式
	private boolean dialogMode;

	public void setShareData(HashMap<String, Object> data) {
		reqData = data;
	}

	public void setParent(OnekeyShare parent) {
		this.parent = parent;
	}

	/** 设置显示模式为Dialog模式 */
	public void setDialogMode() {
		dialogMode = true;
	}

	public void setActivity(Activity activity) {
		super.setActivity(activity);
		if (dialogMode) {
			activity.setTheme(android.R.style.Theme_Dialog);
			activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
	}

	public void onCreate() {
		if (reqData == null) {
			finish();
			return;
		}

		initPageView();
		activity.setContentView(llPage);
		onTextChanged(etContent.getText(), 0, etContent.length(), 0);

		// 获取平台列表并过滤微信
		new Thread(){
			public void run() {
				platformList = ShareSDK.getPlatformList(activity);
				if (platformList == null) {
					return;
				}

				ArrayList<Platform> list = new ArrayList<Platform>();
				for (Platform plat : platformList) {
					String name = plat.getName();
					if (ShareCore.isUseClientToShare(activity, name)) {
						continue;
					}
					list.add(plat);
				}
				platformList = new Platform[list.size()];
				for (int i = 0; i < platformList.length; i++) {
					platformList[i] = list.get(i);
				}

				UIHandler.sendEmptyMessage(MSG_PLATFORM_LIST_GOT, EditPage.this);
			}
		}.start();
	}

	private void initPageView() {
		llPage = new LinearLayout(getContext());
		llPage.setBackgroundColor(0xff323232);
		llPage.setOrientation(LinearLayout.VERTICAL);

		// 标题栏
		llTitle = new TitleLayout(getContext());
		llTitle.setBackgroundResource(R.drawable.title_back);
		llTitle.getBtnBack().setOnClickListener(this);
		llTitle.getTvTitle().setText(R.string.multi_share);
		llTitle.getBtnRight().setVisibility(View.VISIBLE);
		llTitle.getBtnRight().setText(R.string.share);
		llTitle.getBtnRight().setOnClickListener(this);
		llTitle.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		llPage.addView(llTitle);

		FrameLayout flPage = new FrameLayout(getContext());
		LinearLayout.LayoutParams lpFl = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lpFl.weight = 1;
		flPage.setLayoutParams(lpFl);
		llPage.addView(flPage);

		// 页面主体
		LinearLayout llBody = new LinearLayout(getContext());
		llBody.setOrientation(LinearLayout.VERTICAL);
		FrameLayout.LayoutParams lpLl = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		lpLl.gravity = Gravity.LEFT | Gravity.TOP;
		llBody.setLayoutParams(lpLl);
		flPage.addView(llBody);

		// 别针图片
		ivPin = new ImageView(getContext());
		ivPin.setImageResource(R.drawable.pin);
		int dp_80 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 80);
		int dp_36 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 36);
		FrameLayout.LayoutParams lpPin = new FrameLayout.LayoutParams(dp_80, dp_36);
		lpPin.topMargin = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 6);
		lpPin.gravity = Gravity.RIGHT | Gravity.TOP;
		ivPin.setLayoutParams(lpPin);
		flPage.addView(ivPin);

		ImageView ivShadow = new ImageView(getContext());
		ivShadow.setBackgroundResource(R.drawable.title_shadow);
		ivShadow.setImageResource(R.drawable.title_shadow);
		FrameLayout.LayoutParams lpSd = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		ivShadow.setLayoutParams(lpSd);
		flPage.addView(ivShadow);

		LinearLayout llInput = new LinearLayout(getContext());
		llInput.setMinimumHeight(cn.sharesdk.framework.utils.R.dipToPx(getContext(), 150));
		llInput.setBackgroundResource(R.drawable.edittext_back);
		LinearLayout.LayoutParams lpInput = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		int dp_3 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 3);
		lpInput.setMargins(dp_3, dp_3, dp_3, dp_3);
		lpInput.weight = 1;
		llInput.setLayoutParams(lpInput);
		llBody.addView(llInput);

		// 平台Logo列表
		LinearLayout llToolBar = new LinearLayout(getContext());
		llToolBar.setBackgroundResource(R.drawable.share_tb_back);
		LinearLayout.LayoutParams lpTb = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		int dp_4 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 4);
		lpTb.setMargins(dp_4, 0, dp_4, dp_4);
		llToolBar.setLayoutParams(lpTb);
		llBody.addView(llToolBar);

		LinearLayout llContent = new LinearLayout(getContext());
		llContent.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams lpEt = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		lpEt.weight = 1;
		llContent.setLayoutParams(lpEt);
		llInput.addView(llContent);

		// 文字输入区域
		etContent = new EditText(getContext());
		etContent.setGravity(Gravity.LEFT | Gravity.TOP);
		etContent.setBackgroundDrawable(null);
		etContent.setText(String.valueOf(reqData.get("text")));
		etContent.addTextChangedListener(this);
		etContent.setLayoutParams(lpEt);
		llContent.addView(etContent);

		String platform = String.valueOf(reqData.get("platform"));
		checkAtMth(llContent, platform);

		int dp_74 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 74);
		int dp_16 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 16);
		String imagePath = String.valueOf(reqData.get("imagePath"));
		if(!TextUtils.isEmpty(imagePath) && new File(imagePath).exists()){
			LinearLayout llRight = new LinearLayout(getContext());
			llRight.setOrientation(LinearLayout.VERTICAL);
			llRight.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
			llInput.addView(llRight);

			// 输入区域的图片
			ivImage = new ImageView(getContext());
			ivImage.setBackgroundResource(R.drawable.btn_back_nor);
			ivImage.setScaleType(ScaleType.CENTER_INSIDE);
			ivImage.setVisibility(View.GONE);
			ivImage.setVisibility(View.VISIBLE);
			try {
				shareImage = true;
				image = cn.sharesdk.framework.utils.R.getBitmap(imagePath);
				ivImage.setImageBitmap(image);
			} catch(Throwable t) {
				System.gc();
				try {
					image = cn.sharesdk.framework.utils.R.getBitmap(imagePath, 2);
					ivImage.setImageBitmap(image);
				} catch(Throwable t1) {
					t1.printStackTrace();
					shareImage = false;
				}
			}

			ivImage.setPadding(dp_4, dp_4, dp_4, dp_4);
			LinearLayout.LayoutParams lpImage = new LinearLayout.LayoutParams(dp_74, dp_74);
			int dp_8 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 8);
			lpImage.setMargins(0, dp_16, dp_8, 0);
			ivImage.setLayoutParams(lpImage);
			llRight.addView(ivImage);
			if(!shareImage){
				ivPin.setVisibility(View.GONE);
				ivImage.setVisibility(View.GONE);
			}
			ivImage.setOnClickListener(this);
		}else {
			shareImage = false;
			ivPin.setVisibility(View.GONE);
		}

		// 输入区域的图片
		if(shareImage){
			Button btn = new Button(getContext());
			btn.setTag("img_cancel");
			btn.setOnClickListener(this);
			btn.setBackgroundResource(R.drawable.img_cancel);
			int dp_20 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 20);
			int dp_83 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 83);
			int dp_13 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 13);
			FrameLayout.LayoutParams lpBtn = new FrameLayout.LayoutParams(dp_20, dp_20);
			lpBtn.topMargin = dp_83;
			lpBtn.rightMargin = dp_13;
			lpBtn.gravity = Gravity.RIGHT | Gravity.TOP;
			btn.setPadding(dp_4, dp_4, dp_4, dp_4);
			btn.setLayoutParams(lpBtn);
			flPage.addView(btn);
		}

		// 字数统计
		tvCounter = new TextView(getContext());
		tvCounter.setText(String.valueOf(MAX_TEXT_COUNT));
		tvCounter.setTextColor(0xffcfcfcf);
		tvCounter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		tvCounter.setTypeface(Typeface.DEFAULT_BOLD);
		FrameLayout.LayoutParams lpCounter = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lpCounter.bottomMargin = dp_74;
		lpCounter.gravity = Gravity.RIGHT | Gravity.BOTTOM;

		tvCounter.setPadding(0, 0, dp_16, 0);
		tvCounter.setLayoutParams(lpCounter);
		flPage.addView(tvCounter);

		TextView tvShareTo = new TextView(getContext());
		tvShareTo.setText(R.string.share_to);
		tvShareTo.setTextColor(0xffcfcfcf);
		tvShareTo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		int dp_9 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 9);
		LinearLayout.LayoutParams lpShareTo = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lpShareTo.gravity = Gravity.CENTER_VERTICAL;
		lpShareTo.setMargins(dp_9, 0, 0, 0);
		tvShareTo.setLayoutParams(lpShareTo);
		llToolBar.addView(tvShareTo);

		HorizontalScrollView sv = new HorizontalScrollView(getContext());
		sv.setHorizontalScrollBarEnabled(false);
		sv.setHorizontalFadingEdgeEnabled(false);
		LinearLayout.LayoutParams lpSv = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lpSv.setMargins(dp_9, dp_9, dp_9, dp_9);
		sv.setLayoutParams(lpSv);
		llToolBar.addView(sv);

		llPlat = new LinearLayout(getContext());
		llPlat.setLayoutParams(new HorizontalScrollView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
		sv.addView(llPlat);
	}

	// 进新浪微博、腾讯微博、Facebook和Twitter支持At功能
	private void checkAtMth(LinearLayout llInput, String platform) {
		if ("SinaWeibo".equals(platform) || "TencentWeibo".equals(platform)
				|| "Facebook".equals(platform) || "Twitter".equals(platform)) {
			llAt= new LinearLayout(getContext());
			FrameLayout.LayoutParams lpAt = new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lpAt.leftMargin = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 10);
			lpAt.bottomMargin = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 10);
			lpAt.gravity = Gravity.LEFT | Gravity.BOTTOM;
			llAt.setLayoutParams(lpAt);
			llAt.setOnClickListener(this);
			llInput.addView(llAt);

			TextView tvAt = new TextView(getContext());
			tvAt.setBackgroundResource(R.drawable.btn_back_nor);
			int dp_32 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 32);
			tvAt.setLayoutParams(new LinearLayout.LayoutParams(dp_32, dp_32));
			tvAt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			tvAt.setText("@");
			int dp_2 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 2);
			tvAt.setPadding(0, 0, 0, dp_2);
			tvAt.setTypeface(Typeface.DEFAULT_BOLD);
			tvAt.setTextColor(0xff000000);
			tvAt.setGravity(Gravity.CENTER);
			llAt.addView(tvAt);

			TextView tvName = new TextView(getContext());
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			tvName.setTextColor(0xff000000);
			String text = getContext().getString(R.string.list_friends, getName(platform));
			tvName.setText(text);
			LinearLayout.LayoutParams lpName = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lpName.gravity = Gravity.CENTER_VERTICAL;
			tvName.setLayoutParams(lpName);
			llAt.addView(tvName);
		}
	}

	private String getName(String platform) {
		if (platform == null) {
			return "";
		}

		int resId = cn.sharesdk.framework.utils.R.getStringRes(getContext(), platform);
		return getContext().getString(resId);
	}

	public void onClick(View v) {
		if (v.equals(ivImage)) {
			if (image != null && !image.isRecycled()) {
				PicViewer pv = new PicViewer();
				pv.setImageBitmap(image);
				pv.show(activity, null);
			}
			return;
		}

		if (v.equals(llTitle.getBtnBack())) {
			Platform plat = null;
			for (int i = 0; i < views.length; i++) {
				if (views[i].getVisibility() == View.INVISIBLE) {
					plat = platformList[i];
					break;
				}
			}

			// 取消分享的统计
			if (plat != null) {
				ShareSDK.logDemoEvent(5, plat);
			}
			finish();
			return;
		}

		// 取消分享的统计
		if (v.equals(llTitle.getBtnRight())) {
			String text = etContent.getText().toString();
			reqData.put("text", text);
			if(!shareImage){
				reqData.put("imagePath", null);
			}

			HashMap<Platform, HashMap<String, Object>> editRes
					= new HashMap<Platform, HashMap<String,Object>>();
			boolean selected = false;
			for (int i = 0; i < views.length; i++) {
				if (views[i].getVisibility() != View.VISIBLE) {
					editRes.put(platformList[i], reqData);
					selected = true;
				}
			}

			if (selected) {
				if (parent != null) {
					parent.share(editRes);
				}
				finish();
			}
			else {
				Toast.makeText(getContext(), R.string.select_one_plat_at_least,
						Toast.LENGTH_SHORT).show();
			}
			return;
		}

		if (v.equals(llAt)) {
			FollowList subPage = new FollowList();
			String platform = String.valueOf(reqData.get("platform"));
			subPage.setPlatform(ShareSDK.getPlatform(activity, platform));
			subPage.setBackPage(this);
			subPage.show(activity, null);
			return;
		}

		// 取消分享图片
		if("img_cancel".equals(v.getTag())){
			v.setVisibility(View.GONE);
			ivPin.setVisibility(View.GONE);
			ivImage.setVisibility(View.GONE);
			shareImage = false;
		}

		if (v instanceof FrameLayout) {
			((FrameLayout) v).getChildAt(1).performClick();
			return;
		}

		if (v.getVisibility() == View.INVISIBLE) {
			v.setVisibility(View.VISIBLE);
		}
		else {
			v.setVisibility(View.INVISIBLE);
		}
	}

	public boolean handleMessage(Message msg) {
		switch(msg.what) {
			case MSG_PLATFORM_LIST_GOT: {
				afterPlatformListGot();
			}
			break;
		}
		return false;
	}

	/** 显示平台列表 */
	public void afterPlatformListGot() {
		String name = String.valueOf(reqData.get("platform"));
		int size = platformList == null ? 0 : platformList.length;
		views = new View[size];

		final int dp_36 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 36);
		LinearLayout.LayoutParams lpItem = new LinearLayout.LayoutParams(dp_36, dp_36);
		final int dp_9 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 9);
		lpItem.setMargins(0, 0, dp_9, 0);
		FrameLayout.LayoutParams lpMask = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		lpMask.gravity = Gravity.LEFT | Gravity.TOP;
		int selection = 0;
		for (int i = 0; i < size; i++) {
			FrameLayout fl = new FrameLayout(getContext());
			fl.setLayoutParams(lpItem);
			if (i >= size - 1) {
				fl.setLayoutParams(new LinearLayout.LayoutParams(dp_36, dp_36));
			}
			llPlat.addView(fl);
			fl.setOnClickListener(this);

			ImageView iv = new ImageView(getContext());
			iv.setScaleType(ScaleType.CENTER_INSIDE);
			iv.setImageBitmap(getPlatLogo(platformList[i]));
			iv.setLayoutParams(new FrameLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			fl.addView(iv);

			views[i] = new View(getContext());
			views[i].setBackgroundColor(0xcfffffff);
			views[i].setOnClickListener(this);
			if (name != null && name.equals(platformList[i].getName())) {
				views[i].setVisibility(View.INVISIBLE);
				selection = i;

				// 编辑分享内容的统计
				ShareSDK.logDemoEvent(3, platformList[i]);
			}
			views[i].setLayoutParams(lpMask);
			fl.addView(views[i]);
		}

		final int postSel = selection;
		UIHandler.sendEmptyMessageDelayed(0, 333, new Callback() {
			public boolean handleMessage(Message msg) {
				HorizontalScrollView hsv = (HorizontalScrollView)llPlat.getParent();
				hsv.scrollTo(postSel * (dp_36 + dp_9), 0);
				return false;
			}
		});
	}

	private Bitmap getPlatLogo(Platform plat) {
		if (plat == null) {
			return null;
		}

		String name = plat.getName();
		if (name == null) {
			return null;
		}

		String resName = "logo_" + plat.getName();
		int resId = cn.sharesdk.framework.utils.R.getResId(R.drawable.class, resName);
		return BitmapFactory.decodeResource(activity.getResources(), resId);
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		int remain = MAX_TEXT_COUNT - etContent.length();
		tvCounter.setText(String.valueOf(remain));
		tvCounter.setTextColor(remain > 0 ? 0xffcfcfcf : 0xffff0000);
	}

	public void afterTextChanged(Editable s) {

	}

	public void onResult(ArrayList<String> selected) {
		StringBuilder sb = new StringBuilder();
		for (String sel : selected) {
			sb.append('@').append(sel).append(' ');
		}
		etContent.append(sb.toString());
	}

}
