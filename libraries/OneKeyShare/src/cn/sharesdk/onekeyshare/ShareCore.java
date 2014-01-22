/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */

package cn.sharesdk.onekeyshare;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;

/**
 * ShareCore是快捷分享的实际出口，此类使用了反射的方式，配合传递进来的HashMap，
 *构造{@link ShareParams}对象，并执行分享，使快捷分享不再需要考虑目标平台
 */
public class ShareCore {
	private ShareContentCustomizeCallback customizeCallback;

	/** 设置用于分享过程中，根据不同平台自定义分享内容的回调 */
	public void setShareContentCustomizeCallback(ShareContentCustomizeCallback callback) {
		customizeCallback = callback;
	}

	/**
	 * 向指定平台分享内容
	 * <p>
	 * <b>注意：</b><br>
	 * 参数data的键值需要严格按照{@link ShareParams}不同子类具体字段来命名，
	 *否则无法反射此字段，也无法设置其值。
	 */
	public boolean share(Platform plat, HashMap<String, Object> data) {
		if (plat == null || data == null) {
			return false;
		}

		Platform.ShareParams sp = null;
		try {
			sp = getShareParams(plat, data);
		} catch(Throwable t) {
			sp = null;
		}

		if (sp != null) {
			if (customizeCallback != null) {
				customizeCallback.onShare(plat, sp);
			}
			plat.share(sp);
		}
		return true;
	}

	private Platform.ShareParams getShareParams(Platform plat,
			HashMap<String, Object> data) throws Throwable {
		String className = plat.getClass().getName() + "$ShareParams";
		Class<?> cls = Class.forName(className);
		if (cls == null) {
			return null;
		}

		Object sp = cls.newInstance();
		if (sp == null) {
			return null;
		}

		for (Entry<String, Object> ent : data.entrySet()) {
			try {
				Field fld = cls.getField(ent.getKey());
				if (fld != null) {
					fld.setAccessible(true);
					fld.set(sp, ent.getValue());
				}
			} catch(Throwable t) {}
		}

		return (Platform.ShareParams) sp;
	}

	/** 判断指定平台是否使用客户端分享 */
	public static boolean isUseClientToShare(Context context, String platform) {
		if ("Wechat".equals(platform) || "WechatMoments".equals(platform)
				|| "ShortMessage".equals(platform) || "Email".equals(platform)
				|| "GooglePlus".equals(platform) || "QQ".equals(platform)
				|| "Pinterest".equals(platform) || "Instagram".equals(platform)) {
			return true;
		} else if ("Evernote".equals(platform)) {
			Platform plat = ShareSDK.getPlatform(context, platform);
			if ("true".equals(plat.getDevinfo("ShareByAppClient"))) {
				return true;
			}
		}

		return false;
	}

	/** 判断指定平台是否可以用来授权 */
	public static boolean canAuthorize(Context context, String platform) {
		if ("Wechat".equals(platform) || "WechatMoments".equals(platform)
				|| "ShortMessage".equals(platform) || "Email".equals(platform)
				|| "GooglePlus".equals(platform) || "QQ".equals(platform)
				|| "Pinterest".equals(platform)) {
			return false;
		}
		return true;
	}

}
