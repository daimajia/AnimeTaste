package com.zhan_dui.modal;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;

public class VideoDataFormat implements Serializable {

	private static final long serialVersionUID = -4028400856156956769L;

	public final String OriginVideoUrl;
	public final Integer Id;
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
	public final String Youku;

	private boolean IsFav;
	private boolean IsWatched;

	private final String YoukuFormat = "http://v.youku.com/v_show/id_%s.html";
	public static final String NONE_VALUE = "-1";

	private VideoDataFormat(Integer id, String name, String videoUrl,
			String author, String year, String brief, String homePic,
			String detailPic, String insertTime, String updatedTime,
			Boolean isFav) {
		super();
		Id = id;
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
		IsFav = isFav;
		IsWatched = false;
		int start = OriginVideoUrl.indexOf("vid/") + 4;
		int end = OriginVideoUrl.indexOf("/type");
		Youku = String
				.format(YoukuFormat, OriginVideoUrl.substring(start, end));
	}

	private VideoDataFormat(JSONObject object) {
		Id = Integer.valueOf(getValue(object, "Id"));
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
		IsFav = false;
		int start = OriginVideoUrl.indexOf("vid/") + 4;
		int end = OriginVideoUrl.indexOf("/type");
		Youku = String
				.format(YoukuFormat, OriginVideoUrl.substring(start, end));
		IsWatched = false;

	}

	public boolean isFavorite() {
		return IsFav;
	}

	public boolean isWatched() {
		return IsWatched;
	}

	public void setFav(Boolean fav) {
		IsFav = fav;
	}

	public void setWatched(Boolean watch) {
		IsWatched = watch;
	}

	public static VideoDataFormat build(JSONObject object) {
		return new VideoDataFormat(object);
	}

	public static VideoDataFormat build(Cursor cursor) {
		int id = cursor.getInt(cursor.getColumnIndex("id"));
		String name = cursor.getString(cursor.getColumnIndex("name"));
		String videoUrl = cursor.getString(cursor.getColumnIndex("videourl"));
		String author = cursor.getString(cursor.getColumnIndex("author"));
		String year = cursor.getString(cursor.getColumnIndex("year"));
		String brief = cursor.getString(cursor.getColumnIndex("brief"));
		String homePic = cursor.getString(cursor.getColumnIndex("homepic"));
		String detailPic = cursor.getString(cursor.getColumnIndex("detailpic"));
		String insertTime = cursor.getString(cursor
				.getColumnIndex("inserttime"));
		String updatedTime = cursor.getString(cursor
				.getColumnIndex("updatetime"));

		Boolean isFav = Boolean.valueOf(cursor.getString(cursor
				.getColumnIndex("isfav")));
		return new VideoDataFormat(id, name, videoUrl, author, year, brief,
				homePic, detailPic, insertTime, updatedTime, isFav);
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
