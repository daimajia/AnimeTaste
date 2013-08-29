package com.zhan_dui.modal;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class DataFormat implements Serializable {

	private static final long serialVersionUID = -4028400856156956769L;
	private final String OriginVideoUrl;

	public final String Name;
	public final String HDVideoUrl;
	public final String CommonVideoUrl;
	public final String Author;
	public final String Year;
	public final String Brief;
	public final String HomePic;
	public final String DetailPic;
	public final String InsertTime;
	public final String UpdatedTime;

	public static final String NONE_VALUE = "-1";

	public DataFormat(String name, String videoUrl, String author, String year,
			String brief, String homePic, String detailPic, String insertTime,
			String updatedTime) {
		super();
		Name = name;
		OriginVideoUrl = videoUrl;
		CommonVideoUrl = getCommonVideoUrl(videoUrl);
		HDVideoUrl = getHDVideoUrl(videoUrl);
		Author = author;
		Year = year;
		Brief = brief;
		HomePic = homePic;
		DetailPic = detailPic;
		InsertTime = insertTime;
		UpdatedTime = updatedTime;
	}

	public DataFormat(JSONObject object) {
		Name = getValue(object, "Name");
		OriginVideoUrl = getValue(object, "VideoUrl");
		HDVideoUrl = getHDVideoUrl(OriginVideoUrl);
		CommonVideoUrl = getCommonVideoUrl(OriginVideoUrl);
		Author = getValue(object, "Author");
		Year = getValue(object, "Year");
		Brief = getValue(object, "Brief");
		HomePic = getValue(object, "HomePic");
		DetailPic = getValue(object, "DetailPic");
		UpdatedTime = getValue(object, "UpdatedTime");
		InsertTime = getValue(object, "InsertTime");
	}

	private static String getCommonVideoUrl(String url) {
		Long timestamp = (long) Math.ceil(System.currentTimeMillis() / 1000);
		return url.replace("type//", "type/flv/ts/" + timestamp
				+ "/useKeyframe/0/");
	}

	private static String getHDVideoUrl(String url) {
		Long timestamp = (long) Math.ceil(System.currentTimeMillis() / 1000);
		return url.replace("type//", "type/hd2/ts/" + timestamp
				+ "/useKeyframe/0/");
	}

	private static String getValue(JSONObject object, String key) {
		try {
			return object.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return NONE_VALUE;
	}
}
