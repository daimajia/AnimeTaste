package com.zhan_dui.modal;

import org.json.JSONObject;

public class FavDataFormat {

	public final Integer VideoID;
	public final Long AddTime;

	public FavDataFormat(JSONObject object, Long addTime) {
		AddTime = addTime;
		VideoID = VideoDataFormat.build(object).Id;
	}

	public FavDataFormat(VideoDataFormat video, Long addTime) {
		AddTime = addTime;
		VideoID = video.Id;
	}

	public FavDataFormat(VideoDataFormat video) {
		VideoID = video.Id;
		AddTime = System.currentTimeMillis();
	}
}
