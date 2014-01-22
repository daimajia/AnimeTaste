/*
 * 官网地站:http://www.ShareSDK.cn
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 ShareSDK.cn. All rights reserved.
 */

package cn.sharesdk.onekeyshare;

import java.util.ArrayList;
import java.util.HashMap;
import cn.sharesdk.framework.FakeActivity;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.TitleLayout;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

/** 获取好友或关注列表 */
public class FollowList extends FakeActivity implements OnClickListener, OnItemClickListener {
	private TitleLayout llTitle;
	private Platform platform;
	private FollowAdapter adapter;
	private EditPage page;

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}

	public void onCreate() {
		LinearLayout llPage = new LinearLayout(getContext());
		llPage.setBackgroundColor(0xfff5f5f5);
		llPage.setOrientation(LinearLayout.VERTICAL);
		activity.setContentView(llPage);

		// 标题栏
		llTitle = new TitleLayout(getContext());
		llTitle.setBackgroundResource(R.drawable.title_back);
		llTitle.getBtnBack().setOnClickListener(this);
		llTitle.getTvTitle().setText(R.string.multi_share);
		llTitle.getBtnRight().setVisibility(View.VISIBLE);
		llTitle.getBtnRight().setText(R.string.finish);
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

		// body-list
		ListView followList = new ListView(getContext());
		followList.setCacheColorHint(0);
		FrameLayout.LayoutParams lpLv = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		followList.setLayoutParams(lpLv);
		flPage.addView(followList);
		adapter = new FollowAdapter(getContext());
		adapter.setPlatform(platform);
		followList.setAdapter(adapter);
		followList.setOnItemClickListener(this);

		ImageView ivShadow = new ImageView(getContext());
		ivShadow.setBackgroundResource(R.drawable.title_shadow);
		FrameLayout.LayoutParams lpSd = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		ivShadow.setLayoutParams(lpSd);
		flPage.addView(ivShadow);
	}

	public void onClick(View v) {
		String name = platform.getName();
		if (v.equals(llTitle.getBtnRight())) {
			ArrayList<String> selected = new ArrayList<String>();
			if ("SinaWeibo".equals(name)) {
				for (int i = 0, size = adapter.getCount(); i < size; i++) {
					if (adapter.getItem(i).checked) {
						selected.add(adapter.getItem(i).screeName);
					}
				}
			} else if ("TencentWeibo".equals(name)) {
				for (int i = 0, size = adapter.getCount(); i < size; i++) {
					if (adapter.getItem(i).checked) {
						selected.add(adapter.getItem(i).uid);
					}
				}
			} else if ("Facebook".equals(name)) {
				for (int i = 0, size = adapter.getCount(); i < size; i++) {
					if (adapter.getItem(i).checked) {
						selected.add("[" + adapter.getItem(i).uid + "]");
					}
				}
			} else if ("Twitter".equals(name)) {
				for (int i = 0, size = adapter.getCount(); i < size; i++) {
					if (adapter.getItem(i).checked) {
						selected.add(adapter.getItem(i).uid);
					}
				}
			}
			page.onResult(selected);
		}

		finish();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Following following = adapter.getItem(position);
		following.checked = !following.checked;
		adapter.notifyDataSetChanged();
	}

	public void setBackPage(EditPage page) {
		this.page = page;
	}

	private static class FollowAdapter extends BaseAdapter
			implements PlatformActionListener, Callback {
		private Context context;
		private Platform platform;
		private int curPage;
		private ArrayList<Following> follows;
		private HashMap<String, Following> map;
		private Handler handler;
		private boolean hasNext;

		public FollowAdapter(Context context) {
			this.context = context;
			curPage = -1;
			hasNext = true;
			map = new HashMap<String, Following>();
			follows = new ArrayList<Following>();
			handler = new Handler(this);
		}

		public int getCount() {
			return follows.size();
		}

		public void setPlatform(Platform platform) {
			this.platform = platform;
			platform.setPlatformActionListener(this);
			next();
		}

		private void next() {
			if (hasNext) {
				platform.listFriend(5, curPage + 1, null);
			}
		}

		public Following getItem(int position) {
			return follows.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LinearLayout llItem = new LinearLayout(parent.getContext());
				convertView = llItem;

				LinearLayout llText = new LinearLayout(parent.getContext());
				int dp_15 = cn.sharesdk.framework.utils.R.dipToPx(parent.getContext(), 15);
				int dp_10 = cn.sharesdk.framework.utils.R.dipToPx(parent.getContext(), 10);
				llText.setPadding(dp_15, dp_10, dp_10, dp_10);
				llText.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lpText.weight = 1;
				llText.setLayoutParams(lpText);
				llItem.addView(llText);

				TextView tvName = new TextView(parent.getContext());
				tvName.setGravity(Gravity.CENTER);
				tvName.setTextColor(0xff000000);
				tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
				tvName.setSingleLine();
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lp.weight = 1;
				tvName.setLayoutParams(lp);
				llText.addView(tvName);

				TextView tvSign = new TextView(parent.getContext());
				tvSign.setTextColor(0x7f000000);
				tvSign.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				tvSign.setSingleLine();
				llText.addView(tvSign);

				ImageView ivCheck = new ImageView(parent.getContext());
				ivCheck.setPadding(0, 0, dp_15, 0);
				LinearLayout.LayoutParams lpCheck = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lpCheck.gravity = Gravity.CENTER_VERTICAL;
				ivCheck.setLayoutParams(lpCheck);
				llItem.addView(ivCheck);
			}

			Following following = getItem(position);
			LinearLayout llItem = (LinearLayout) convertView;
			LinearLayout llText = (LinearLayout) llItem.getChildAt(0);
			TextView tvName = (TextView) llText.getChildAt(0);
			tvName.setText(following.screeName);
			TextView tvSign = (TextView) llText.getChildAt(1);
			tvSign.setText(following.description);
			ImageView ivCheck = (ImageView) llItem.getChildAt(1);
			ivCheck.setImageResource(following.checked ?
					R.drawable.auth_follow_cb_chd : R.drawable.auth_follow_cb_unc);

			if (position == getCount() - 1) {
				next();
			}
			return convertView;
		}

		public void onCancel(Platform plat, int action) {
			handler.sendEmptyMessage(-1);
		}

		public void onComplete(Platform plat, int action, HashMap<String, Object> res) {
			if (parseList(res)) {
				curPage++;
				handler.sendEmptyMessage(1);
			}
		}

		public void onError(Platform plat, int action, Throwable t) {
			t.printStackTrace();
		}

		private boolean parseList(HashMap<String, Object> res) {
			if (res == null || res.size() <= 0) {
				return false;
			}

			boolean hasData = false;
			if ("SinaWeibo".equals(platform.getName())) {
				// users[id, name, description]
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, Object>> users
						= (ArrayList<HashMap<String,Object>>) res.get("users");
				for (HashMap<String, Object> user : users) {
					Following following = new Following();
					following.uid = String.valueOf(user.get("id"));
					following.screeName = String.valueOf(user.get("name"));
					following.description = String.valueOf(user.get("description"));
					if (!map.containsKey(following.uid)) {
						map.put(following.uid, following);
						follows.add(following);
						hasData = true;
					}
				}
				hasNext = (Integer) res.get("total_number") > follows.size();
			}
			else if ("TencentWeibo".equals(platform.getName())) {
				hasNext = ((Integer)res.get("hasnext") == 0);
				// info[nick, name, tweet[text]]
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, Object>> infos
						= (ArrayList<HashMap<String,Object>>) res.get("info");
				for (HashMap<String, Object> info : infos) {
					Following following = new Following();
					following.screeName = String.valueOf(info.get("nick"));
					following.uid = String.valueOf(info.get("name"));
					@SuppressWarnings("unchecked")
					ArrayList<HashMap<String, Object>> tweets
							= (ArrayList<HashMap<String,Object>>) info.get("tweet");
					for (int i = 0; i < tweets.size();) {
						HashMap<String, Object> tweet = tweets.get(i);
						following.description = String.valueOf(tweet.get("text"));
						break;
					}
					if (!map.containsKey(following.uid)) {
						map.put(following.uid, following);
						follows.add(following);
						hasData = true;
					}
				}
			}
			else if ("Facebook".equals(platform.getName())) {
				// data[id, name]
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, Object>> datas
						= (ArrayList<HashMap<String,Object>>) res.get("data");
				for (HashMap<String, Object> data : datas) {
					Following following = new Following();
					following.uid = String.valueOf(data.get("id"));
					following.screeName = String.valueOf(data.get("name"));
					if (!map.containsKey(following.uid)) {
						map.put(following.uid, following);
						follows.add(following);
						hasData = true;
					}
				}
				@SuppressWarnings("unchecked")
				HashMap<String, Object> paging = (HashMap<String, Object>) res.get("paging");
				hasNext = paging.containsKey("next");
			}
			else if ("Twitter".equals(platform.getName())) {
				// users[screen_name, name, description]
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, Object>> users
						= (ArrayList<HashMap<String,Object>>) res.get("users");
				for (HashMap<String, Object> user : users) {
					Following following = new Following();
					following.uid = String.valueOf(user.get("screen_name"));
					following.screeName = String.valueOf(user.get("name"));
					following.description = String.valueOf(user.get("description"));
					if (!map.containsKey(following.uid)) {
						map.put(following.uid, following);
						follows.add(following);
						hasData = true;
					}
				}
			}
			return hasData;
		}

		public boolean handleMessage(Message msg) {
			if (msg.what < 0) {
				((Activity) context).finish();
			}
			else {
				notifyDataSetChanged();
			}
			return false;
		}

	}

	private static class Following {
		public boolean checked;
		public String screeName;
		public String description;
		public String uid;
	}

}
