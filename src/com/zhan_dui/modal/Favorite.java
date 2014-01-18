package com.zhan_dui.modal;

import org.json.JSONObject;

public class Favorite {

	public final Integer VideoID;
	public final Long AddTime;

	public Favorite(JSONObject object, Long addTime) {
		AddTime = addTime;
		VideoID = Animation.build(object).Id;
	}

	public Favorite(Animation video, Long addTime) {
		AddTime = addTime;
		VideoID = video.Id;
	}

	public Favorite(Animation video) {
		VideoID = video.Id;
		AddTime = System.currentTimeMillis();
	}
}
