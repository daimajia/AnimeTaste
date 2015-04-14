package com.zhan_dui.sns;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.model.Animation;
import com.zhan_dui.utils.ShareButton;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ShareHelper {

    private static void shareAction(Context context, MaterialDialog dialog) {
        dialog.dismiss();
        Toast.makeText(context, "感谢分享哟！", Toast.LENGTH_SHORT).show();
    }

    public static void showUp(final Context context, final Animation animation) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_share, null, false);

        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(v, false)
                .build();
        dialog.show();
        ShareSDK.initSDK(context);
        ShareButton weibo = (ShareButton) v.findViewById(R.id.action_share_weibo);
        ShareButton wechat = (ShareButton) v.findViewById(R.id.action_share_wechat);
        ShareButton wechatTimeline = (ShareButton) v.findViewById(R.id.action_share_wechat_timeline);
        ShareButton qq = (ShareButton) v.findViewById(R.id.action_share_qq);
        ShareButton link = (ShareButton) v.findViewById(R.id.action_share_link);
        ShareButton qzone = (ShareButton) v.findViewById(R.id.action_share_qzone);

        TextView cancel = (TextView) v.findViewById(R.id.cancel);

        final Handler showMsg = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        final PlatformActionListener platformActionListener = new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> stringObjectHashMap) {
                Message msg = Message.obtain(showMsg);
                msg.obj = "(๑•̀ㅂ•́)و分享成功！Ye";
                msg.sendToTarget();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Log.e("Error", throwable.getMessage());
                Message msg = Message.obtain(showMsg);
                msg.obj = "(⊙﹏⊙) 好像出了错误";
                msg.sendToTarget();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Message msg = Message.obtain(showMsg);
                msg.obj = "o(TヘTo) 取消了耶...";
                msg.sendToTarget();
            }
        };


        weibo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SinaWeibo.ShareParams sp = new SinaWeibo.ShareParams();
                Platform pf = ShareSDK.getPlatform(context, SinaWeibo.NAME);
                String toSend = "「" + animation.Name + "」 " + animation.OriginVideoUrl + " " + animation.Brief;
                toSend = toSend.substring(0, 140);
                sp.setText(toSend);
                sp.setUrl(animation.OriginVideoUrl);
                sp.setImageUrl(animation.DetailPic);
                pf.setPlatformActionListener(platformActionListener);
                pf.share(sp);
                shareAction(context, dialog);
            }
        });

        wechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Platform plat = ShareSDK.getPlatform(context, Wechat.NAME);
                Wechat.ShareParams sp = new Wechat.ShareParams();
                sp.setTitle(animation.Name);
                sp.setText(animation.Brief);
                sp.setImageUrl(animation.HomePic);
                sp.setUrl(animation.getShareUrl());
                sp.setShareType(Platform.SHARE_WEBPAGE);
                plat.setPlatformActionListener(platformActionListener);
                plat.share(sp);
                shareAction(context, dialog);
            }
        });

        wechatTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Platform plat = ShareSDK.getPlatform(context, WechatMoments.NAME);
                WechatMoments.ShareParams sp = new WechatMoments.ShareParams();
                sp.title = animation.Name;
                sp.text = animation.Brief;
                sp.imageUrl = animation.HomePic;
                sp.url = animation.getShareUrl();
                sp.shareType = Platform.SHARE_WEBPAGE;
                plat.setPlatformActionListener(platformActionListener);
                plat.share(sp);
                shareAction(context, dialog);
            }
        });

        qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Platform plat = ShareSDK.getPlatform(context, QQ.NAME);
                QQ.ShareParams sp = new QQ.ShareParams();
                sp.setTitle(animation.Name);
                sp.setText(animation.Brief);
                sp.setImageUrl(animation.HomePic);
                sp.setTitleUrl(animation.getShareUrl());
                sp.setShareType(QQ.SHARE_WEBPAGE);
                plat.setPlatformActionListener(platformActionListener);
                plat.share(sp);
                shareAction(context, dialog);
            }
        });

        qzone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Platform plat = ShareSDK.getPlatform(context, QZone.NAME);
                QZone.ShareParams sp = new QZone.ShareParams();
                sp.setTitle(animation.Name);
                sp.setText(animation.Brief);
                sp.setImageUrl(animation.HomePic);
                sp.setTitleUrl(animation.getShareUrl());
                sp.setShareType(QZone.SHARE_WEBPAGE);
                plat.setPlatformActionListener(platformActionListener);
                plat.share(sp);
                shareAction(context, dialog);
            }
        });

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(animation.getShareUrl());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("data", animation.getShareUrl());
                    clipboard.setPrimaryClip(clip);
                }
                dialog.dismiss();
                Toast.makeText(context, "复制完成，快快分享给你的好朋友们！", Toast.LENGTH_LONG).show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }
}
