package com.zhan_dui.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AnimeTasteDB extends SQLiteOpenHelper {

	public static final int VERSION = 1;
	public static final String NAME = "AnimeTaste";

	private static final String TABLE_VIDEO_NAME = "Video";
	private static final String TABLE_FAV_NAME = "Fav";
	private static final String TABLE_WATCHED_NAME = "Watched";

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

	private static final String DATABASE_FAV_WATCHED = "create table Watched("
			+ "_id integer primary key autoincrement," + "vid integer UNIQUE)";

	public AnimeTasteDB(Context context, String name, CursorFactory factory,
                        int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_VIDEO_CREATE);
		db.execSQL(DATABASE_FAV_CREATE);
		db.execSQL(DATABASE_FAV_WATCHED);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
