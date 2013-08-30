package com.zhan_dui.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.zhan_dui.modal.FavDataFormat;
import com.zhan_dui.modal.VideoDataFormat;

public class VideoDB extends SQLiteOpenHelper {

	public static final int VERSION = 1;
	public static final String NAME = "AnimeTaste";

	public static final String TABLE_VIDEO_NAME = "Video";
	public static final String TABLE_FAV_NAME = "Fav";

	private static final String DATABASE_VIDEO_CREATE = "create table Video(_id integer primary key autoincrement, "
			+ "id text not null UNIQUE,"
			+ "name text not null,"
			+ "videourl text not null,"
			+ "author text not null,"
			+ "year text not null,"
			+ "brief text not null,"
			+ "homepic text not null,"
			+ "detailpic text not null,"
			+ "updatetime text not null,"
			+ "isfav text not null,"
			+ "inserttime text not null" + ");";

	private static final String DATABASE_FAV_CREATE = "create table Fav("
			+ "_id integer primary key autoincrement," + "vid integer UNIQUE,"
			+ "addtime text not null" + ")";

	public VideoDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_VIDEO_CREATE);
		db.execSQL(DATABASE_FAV_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void insertVideo(JSONObject video) {
		insertVideo(VideoDataFormat.build(video), false);
	}

	public void insertVideos(JSONArray videos) {
		for (int i = 0; i < videos.length(); i++) {
			try {
				insertVideo(videos.getJSONObject(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public long insertVideo(VideoDataFormat video, boolean isFav) {
		ContentValues values = new ContentValues();
		values.put("id", video.Id);
		values.put("name", video.Name);
		values.put("videourl", video.OriginVideoUrl);
		values.put("author", video.Author);
		values.put("year", video.Year);
		values.put("brief", video.Brief);
		values.put("homepic", video.HomePic);
		values.put("detailpic", video.DetailPic);
		values.put("updatetime", video.UpdatedTime);
		values.put("inserttime", video.InsertTime);
		values.put("isfav", String.valueOf(isFav));
		return getWritableDatabase().insertWithOnConflict(TABLE_VIDEO_NAME,
				null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public void insertFav(JSONObject video) {
		insertFav(VideoDataFormat.build(video));
	}

	public long insertFav(VideoDataFormat video) {
		insertVideo(video, true);
		ContentValues values = new ContentValues();
		FavDataFormat favDataFormat = new FavDataFormat(video);
		values.put("vid", favDataFormat.VideoID);
		values.put("addtime", favDataFormat.AddTime);
		return getWritableDatabase().insertWithOnConflict(TABLE_FAV_NAME, null,
				values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public VideoDataFormat getFavDetail(int vid) {
		Cursor cursor = getReadableDatabase().query(TABLE_VIDEO_NAME, null,
				"vid=" + vid, null, null, null, null, "1");
		if (cursor != null && cursor.getCount() != 0) {
			cursor.moveToFirst();
			return VideoDataFormat.build(cursor);
		} else {
			return null;
		}
	}

	public VideoDataFormat getVideoDetail(int _id) {
		Cursor cursor = getReadableDatabase().query(TABLE_VIDEO_NAME, null,
				"_id=?", new String[] { String.valueOf(_id) }, null, null,
				null, "1");
		if (cursor != null && cursor.getCount() != 0) {
			cursor.moveToFirst();
			return VideoDataFormat.build(cursor);
		} else {
			return null;
		}
	}

	public long getVideosCount() {
		SQLiteStatement statement = getReadableDatabase().compileStatement(
				"select count(*) from " + TABLE_VIDEO_NAME);
		return statement.simpleQueryForLong();
	}

	public long getFavCount() {
		SQLiteStatement statement = getReadableDatabase().compileStatement(
				"select count(*) from " + TABLE_FAV_NAME);
		return statement.simpleQueryForLong();
	}

	public int removeAllVideos() {
		return getWritableDatabase().delete(TABLE_VIDEO_NAME, null, null);
	}

	public int removeAllVideosWithoutFav() {
		return getWritableDatabase().delete(TABLE_VIDEO_NAME, " isfav=?",
				new String[] { String.valueOf(false) });
	}

	public Boolean isFav(int vid) {
		Cursor cursor = getReadableDatabase().query(TABLE_VIDEO_NAME,
				new String[] { "isfav" }, "id=?",
				new String[] { String.valueOf(vid) }, null, null, null);
		if (cursor.moveToNext()) {
			if (Boolean
					.valueOf(cursor.getString(cursor.getColumnIndex("isfav")))) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public int removeFav(VideoDataFormat video) {
		return removeFav(video.Id);
	}

	public int removeFav(int vid) {
		ContentValues values = new ContentValues();
		values.put("isfav", false);
		getWritableDatabase().update(TABLE_VIDEO_NAME, values, "id=?",
				new String[] { String.valueOf(vid) });
		return getWritableDatabase().delete(TABLE_FAV_NAME, "vid=?",
				new String[] { String.valueOf(vid) });
	}

	public Cursor getVideos(int count) {
		return getReadableDatabase().query(TABLE_VIDEO_NAME, null, null, null,
				null, null, null, String.valueOf(count));
	}
}
