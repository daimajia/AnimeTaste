package com.zhan_dui.modal;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Animation implements Parcelable {

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
    public final String Youku;
    public final String UHD;
    public final String HD;
    public final String SD;

    private boolean IsFav;
    private boolean IsWatched;
    private static final String EMPTY = "NOT EXSIST";
    public static final String NONE_VALUE = "-1";

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Animation> CREATOR = new Creator<Animation>() {
        @Override
        public Animation createFromParcel(Parcel parcel) {
            return new Animation(parcel);
        }

        @Override
        public Animation[] newArray(int size) {
            return new Animation[size];
        }
    };

    public Animation(Parcel in){
        this(in.readInt(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readInt()==0);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(Id);
        parcel.writeString(Name);
        parcel.writeString(OriginVideoUrl);
        parcel.writeString(Author);
        parcel.writeString(Year);
        parcel.writeString(Brief);
        parcel.writeString(HomePic);
        parcel.writeString(DetailPic);
        parcel.writeString(UHD);
        parcel.writeString(HD);
        parcel.writeString(SD);
        parcel.writeInt(IsFav?0:1);
    }

	private Animation(Integer id, String name, String videoUrl,
                      String author, String year, String brief, String homePic,
                      String detailPic,String uhd,String hd,String sd,Boolean isFav) {
		super();
		Id = id;
		Name = name;
		OriginVideoUrl = videoUrl;
		Author = author;
		Year = year;
		Brief = brief;
		HomePic = homePic;
		DetailPic = detailPic;
		IsFav = isFav;
		IsWatched = false;
		Youku = OriginVideoUrl;
        UHD = uhd;
        HD = hd;
        SD = sd;
        CommonVideoUrl = getCommonVideoUrl();
        HDVideoUrl = getHDVideoUrl();
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

	public static Animation build(JSONObject object) {
        int id = Integer.valueOf(getValue(object, "Id"));
        String name = getValue(object, "Name");
        String originVideoUrl = getValue(object, "VideoUrl");
        String author = getValue(object, "Author");
        String year = getValue(object, "Year");
        String brief = getValue(object, "Brief");
        String homePic = getValue(object, "HomePic");
        String detailPic = getValue(object, "DetailPic");
        Boolean isFav = false;
        String uhd = EMPTY,hd = EMPTY,sd = EMPTY;
        try {
            JSONObject videoSourceObject = object.getJSONObject("VideoSource");
            uhd = videoSourceObject.has("uhd") ? videoSourceObject.getString("uhd") : EMPTY;
            hd = videoSourceObject.has("hd") ? videoSourceObject.getString("hd") : EMPTY;
            sd = videoSourceObject.has("sd") ? videoSourceObject.getString("sd") : EMPTY;
        } catch (JSONException e) {
        }finally {
            return new Animation(id,name,originVideoUrl,author,year,brief,homePic,detailPic,uhd,hd,sd,isFav);
        }
	}

	public static Animation build(Cursor cursor) {
		int id = cursor.getInt(cursor.getColumnIndex("id"));
		String name = cursor.getString(cursor.getColumnIndex("name"));
		String videoUrl = cursor.getString(cursor.getColumnIndex("videourl"));
		String author = cursor.getString(cursor.getColumnIndex("author"));
		String year = cursor.getString(cursor.getColumnIndex("year"));
		String brief = cursor.getString(cursor.getColumnIndex("brief"));
		String homePic = cursor.getString(cursor.getColumnIndex("homepic"));
		String detailPic = cursor.getString(cursor.getColumnIndex("detailpic"));
		Boolean isFav = Boolean.valueOf(cursor.getString(cursor
				.getColumnIndex("isfav")));
		return new Animation(id, name, videoUrl, author, year, brief,
				homePic, detailPic, EMPTY,EMPTY,EMPTY, isFav);
	}

    public static ArrayList<Animation> build(JSONArray animationArray){
        ArrayList<Animation> animations = new ArrayList<Animation>();
        for(int i=0;i<animationArray.length();i++){
            try{
                animations.add(Animation.build(animationArray.getJSONObject(i)));
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
        return animations;
    }

	private String getCommonVideoUrl() {
        return SD;
	}

	private String getHDVideoUrl() {
        if(!UHD.equals(EMPTY)){
            return UHD;
        }else{
            return SD;
        }
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
