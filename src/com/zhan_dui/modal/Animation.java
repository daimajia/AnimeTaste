package com.zhan_dui.modal;

import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@Table(name="Animations")
public class Animation extends Model implements Parcelable {

    @Column(name="AnimationId")
	public  Integer AnimationId;
    @Column(name="Name")
	public  String Name;
    @Column(name="OriginVideoUrl")
    public  String OriginVideoUrl;
    @Column(name="HdUrl")
	public  String HDVideoUrl;
    @Column(name="CommonUrl")
	public  String CommonVideoUrl;
    @Column(name="Author")
	public  String Author;
    @Column(name="Year")
	public  String Year;
    @Column(name="Brief")
	public  String Brief;
    @Column(name="HomePic")
	public  String HomePic;
    @Column(name="DetailPic")
	public  String DetailPic;
    @Column(name="Youku")
    public  String Youku;
    @Column(name="UHD")
    public  String UHD;
    @Column(name="HD")
    public  String HD;
    @Column(name="SD")
    public  String SD;

    @Column(name="IsFavorite")
    public boolean IsFav;
    @Column(name="IsWatched")
    public boolean IsWatched;

    private static final  String EMPTY = "NOT EXSIST";
    public static final  String NONE_VALUE = "-1";

    @Override
    public int describeContents() {
        return 0;
    }

    public static final  Creator<Animation> CREATOR = new Creator<Animation>() {
        @Override
        public Animation createFromParcel(Parcel parcel) {
            return new Animation(parcel);
        }

        @Override
        public Animation[] newArray(int size) {
            return new Animation[size];
        }
    };


    public Animation(){
        
    }

    public Animation(Parcel in){
        this(in.readInt(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readString(),in.readInt()==0,in.readInt()==0);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(AnimationId);
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
        parcel.writeInt(IsWatched?0:1);
    }

	private Animation(Integer animationID, String name, String videoUrl,
                      String author, String year, String brief, String homePic,
                      String detailPic,String uhd,String hd,String sd,Boolean isFav,Boolean isWatched) {
		super();
		AnimationId = animationID;
		Name = name;
		OriginVideoUrl = videoUrl;
		Author = author;
		Year = year;
		Brief = brief;
		HomePic = homePic;
		DetailPic = detailPic;
		IsFav = isFav;
		IsWatched = isWatched;
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

    public Long addToFavorite(){
        IsFav = true;
        return save();
    }

    public Long removeFromFavorite(){
        IsFav = false;
        return save();
    }

    public void recordWatch(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                Select select = new Select();
                WatchRecord record =
                        select.from(WatchRecord.class)
                                .where("aid=?", AnimationId)
                                .executeSingle();
                if(record == null){
                    WatchRecord watchRecord  = new WatchRecord(AnimationId,true);
                    watchRecord.save();
                    IsWatched = true;
                }
            }
        }.start();
    }

    /**
     * 通过JSONObject开始构建Animation对象,大量数据时，只能在线程中执行
     * @param object    Animation JsonObject
     * @return  Animation对象
     */
	public static Animation build(JSONObject object) {
        
        if(Looper.myLooper() == Looper.getMainLooper()){
            Throwable warn = new Throwable("Please do not execute Animation.build(JSONObject object) in Main thread");
            Log.w("Animation Warning",warn);
        }
        
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
        boolean isWatched = false;
        try {
            isWatched = new Select().from(WatchRecord.class).where("aid=?",id).executeSingle() == null ? false : true;
            JSONObject videoSourceObject = object.getJSONObject("VideoSource");
            uhd = videoSourceObject.has("uhd") ? videoSourceObject.getString("uhd") : EMPTY;
            hd = videoSourceObject.has("hd") ? videoSourceObject.getString("hd") : EMPTY;
            sd = videoSourceObject.has("sd") ? videoSourceObject.getString("sd") : EMPTY;
        } catch (JSONException e) {
        }finally {
            return new Animation(id,name,originVideoUrl,author,year,brief,homePic,detailPic,uhd,hd,sd,isFav,isWatched);
        }
	}

    /***
     *
     * @param animationArray
     * @return
     */
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
