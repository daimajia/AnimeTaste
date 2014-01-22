/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */


package cn.sharesdk.onekeyshare;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import cn.sharesdk.framework.FakeActivity;

/** 查看编辑页面中图片的例子 */
public class PicViewer extends FakeActivity implements OnClickListener {
	private ImageView ivViewer;
	private Bitmap pic;

	/** 设置图片用于浏览 */
	public void setImageBitmap(Bitmap pic) {
		this.pic = pic;
		if (ivViewer != null) {
			ivViewer.setImageBitmap(pic);
		}
	}

	public void onCreate() {
		ivViewer = new ImageView(activity);
		ivViewer.setScaleType(ScaleType.CENTER_INSIDE);
		ivViewer.setBackgroundColor(0xc0000000);
		ivViewer.setOnClickListener(this);
		activity.setContentView(ivViewer);
		if (pic != null && !pic.isRecycled()) {
			ivViewer.setImageBitmap(pic);
		}
	}

	public void onClick(View v) {
		finish();
	}

}
