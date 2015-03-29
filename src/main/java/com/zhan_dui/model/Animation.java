package com.zhan_dui.model;

import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Table(name = "Animations")
public class Animation extends Model implements Parcelable {

    @Column(name = "AnimationId", unique = true, onUniqueConflict = Column.ConflictAction.IGNORE, onUniqueConflicts = Column.ConflictAction.IGNORE)
    public Integer AnimationId;
    @Column(name = "Name")
    public String Name;
    @Column(name = "OriginVideoUrl")
    public String OriginVideoUrl;
    @Column(name = "HdUrl")
    public String HDVideoUrl;
    @Column(name = "CommonUrl")
    public String CommonVideoUrl;
    @Column(name = "Author")
    public String Author;
    @Column(name = "Year")
    public String Year;
    @Column(name = "Brief")
    public String Brief;
    @Column(name = "HomePic")
    public String HomePic;
    @Column(name = "DetailPic")
    public String DetailPic;
    @Column(name = "Youku")
    public String Youku;
    @Column(name = "UHD")
    public String UHD;
    @Column(name = "HD")
    public String HD;
    @Column(name = "SD")
    public String SD;

    @Column(name = "IsFavorite")
    public boolean IsFav;
    @Column(name = "IsWatched")
    public boolean IsWatched;

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

    /**
     * for ActiveAndroid
     */
    public Animation() {
    }

    public Animation(Parcel in) {
        this(in.readInt(), in.readString(), in.readString(), in.readString(), in.readString(), in.readString(), in.readString(), in.readString(), in.readString(), in.readString(), in.readString(), in.readInt() == 1, in.readInt() == 1);
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
        parcel.writeInt(IsFav ? 1 : 0);
        parcel.writeInt(IsWatched ? 1 : 0);
    }

    private Animation(Integer animationID, String name, String videoUrl,
                      String author, String year, String brief, String homePic,
                      String detailPic, String uhd, String hd, String sd, Boolean isFav, Boolean isWatched) {
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

    public void setWatched(Boolean watched) {
        IsWatched = watched;
    }

    private static class FavoriteHandler extends Handler {
        private UpdateFinishCallback callback;
        private Method method;

        public FavoriteHandler(UpdateFinishCallback callback, Method method) {
            this.callback = callback;
            this.method = method;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            callback.onUpdateFinished(method, msg);
        }
    }

    public void addToFavorite(final UpdateFinishCallback callback) {
        IsFav = true;
        new Thread() {
            @Override
            public void run() {
                super.run();
                boolean exist = new Select().from(Animation.class).where("AnimationId='" + AnimationId + "'").executeSingle() != null;
                if (!exist)
                    save();
                else
                    new com.activeandroid.query.Update(Animation.class).set("IsFavorite='1'").where("AnimationId='" + AnimationId + "'").execute();
                Message msg = Message.obtain();
                Looper.prepare();
                msg.setTarget(new FavoriteHandler(callback, Method.ADD_FAVORITE));
                msg.sendToTarget();
                Looper.loop();
            }
        }.start();
    }

    public void removeFromFavorite(final UpdateFinishCallback callback) {
        IsFav = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                new Update(Animation.class).set("IsFavorite='0'").where("AnimationId='" + AnimationId + "'").execute();
                Message msg = Message.obtain();
                Looper.prepare();
                msg.setTarget(new FavoriteHandler(callback, Method.REMOVE_FAVORITE));
                msg.sendToTarget();
                Looper.loop();
            }
        }.start();
    }

    public static void removeAllFavorite(final UpdateFinishCallback callback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<Animation> list = new Select().from(Animation.class).where("IsFavorite=?", 1).execute();
                Iterator<Animation> iterator = list.iterator();
                ActiveAndroid.beginTransaction();
                while (iterator.hasNext()) {
                    Animation animation = iterator.next();
                    animation.IsFav = false;
                    animation.save();
                }
                ActiveAndroid.setTransactionSuccessful();
                ActiveAndroid.endTransaction();
                Message msg = Message.obtain();
                Looper.prepare();
                msg.setTarget(new FavoriteHandler(callback, Method.REMOVE_ALL_FAVORITE));
                msg.sendToTarget();
                Looper.loop();
            }
        }.start();
    }

    public void checkIsFavorite(final UpdateFinishCallback callback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                boolean isFavorite = new Select().from(Animation.class).where("IsFavorite='1' and AnimationId='" + AnimationId + "'").executeSingle() != null;
                Looper.prepare();
                Message msg = Message.obtain();
                msg.arg1 = isFavorite ? 1 : 0;
                msg.setTarget(new FavoriteHandler(callback, Method.QUERY_FAVORITE));
                msg.sendToTarget();
                Looper.loop();
            }
        }.start();
    }

    public void recordWatch() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Select select = new Select();
                WatchRecord record =
                        select.from(WatchRecord.class)
                                .where("aid=?", AnimationId)
                                .executeSingle();
                if (record == null) {
                    WatchRecord watchRecord = new WatchRecord(AnimationId, true);
                    watchRecord.save();
                    IsWatched = true;
                }
            }
        }.start();
    }

    /**
     * 通过JSONObject开始构建Animation对象,大量数据时，只能在线程中执行
     *
     * @param object Animation JsonObject
     * @return Animation对象
     */
    public static Animation build(JSONObject object) {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Throwable warn = new Throwable("Please do not execute Animation.build(JSONObject object) " +
                    "in Main thread, it's bad performance and may block the ui thread");
            throw new RuntimeException(warn);
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
        String uhd = EMPTY, hd = EMPTY, sd = EMPTY;
        boolean isWatched = false;
        try {
            isWatched = new Select().from(WatchRecord.class).where("aid=?", id).executeSingle() != null;
            isFav = new Select().from(Animation.class).where("AnimationId='" + id + "' AND IsFavorite='1'").executeSingle() != null;
            JSONObject videoSourceObject = object.getJSONObject("VideoSource");
            uhd = videoSourceObject.has("uhd") ? videoSourceObject.getString("uhd") : EMPTY;
            hd = videoSourceObject.has("hd") ? videoSourceObject.getString("hd") : EMPTY;
            sd = videoSourceObject.has("sd") ? videoSourceObject.getString("sd") : EMPTY;
            uhd = uhd.replace(";", "&");
            hd = uhd.replace(";", "&");
            sd = uhd.replace(";", "&");
        } catch (JSONException ignored) {
        } finally {
            return new Animation(id, name, originVideoUrl, author, year, brief, homePic, detailPic, uhd, hd, sd, isFav, isWatched);
        }
    }

    public static Animation build(DownloadRecord record) {
        return new Animation(record.AnimationId, record.Name, record.OriginVideoUrl, record.Author, record.Year, record.Brief, record.HomePic, record.DetailPic, record.UHD, record.HD, record.SD, record.IsFav, record.IsWatched);
    }

    private static String getHDVideoUrl(String url) {
        return url;
    }

    private static String getCommonVideoUrl(String url) {
        return url;
    }

    private static final String YoukuFormat = "http://v.youku.com/v_show/id_%s.html";

    /**
     * 从旧版本导数据（历史遗留问题），新版本启用ActiveAndroid Orm组件后就不需要了
     *
     * @param cursor 旧版本Cursor
     * @return
     */
    public static Animation build(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex("id"));
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String videoUrl = cursor.getString(cursor.getColumnIndex("videourl"));
        String author = cursor.getString(cursor.getColumnIndex("author"));
        String year = cursor.getString(cursor.getColumnIndex("year"));
        String brief = cursor.getString(cursor.getColumnIndex("brief"));
        String homePic = cursor.getString(cursor.getColumnIndex("homepic"));
        String detailPic = cursor.getString(cursor.getColumnIndex("detailpic"));
        Boolean isFav = Boolean.valueOf(cursor.getString(cursor.getColumnIndex("isfav")));
        String UHD = getHDVideoUrl(videoUrl);
        String HD = getCommonVideoUrl(videoUrl);
        String SD = HD;
        int start = videoUrl.indexOf("vid/") + 4;
        int end = videoUrl.indexOf("/type");
        String Youku = String.format(YoukuFormat, videoUrl.substring(start, end));
        return new Animation(id, name, Youku, author, year, brief, homePic, detailPic, UHD, HD, SD, isFav, false);
    }

    /**
     * @param animationArray
     * @return
     */
    public static ArrayList<Animation> build(JSONArray animationArray) {
        ArrayList<Animation> animations = new ArrayList<Animation>();
        for (int i = 0; i < animationArray.length(); i++) {
            try {
                animations.add(Animation.build(animationArray.getJSONObject(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return animations;
    }

    private String getCommonVideoUrl() {
        return SD;
    }

    private String getHDVideoUrl() {
        if (!UHD.equals(EMPTY)) {
            return UHD;
        } else {
            return SD;
        }
    }

    private static String getValue(JSONObject object, String key) {
        try {
            return object.getString(key);
        } catch (JSONException e) {
//			e.printStackTrace();
        }
        return NONE_VALUE;
    }

    public static enum Method {
        ADD_FAVORITE, REMOVE_FAVORITE, REMOVE_ALL_FAVORITE, QUERY_FAVORITE
    }


    public interface UpdateFinishCallback {
        public void onUpdateFinished(Method method, Message msg);
    }

    public String getShareUrl() {
        return "http://i.animetaste.net/view/" + AnimationId;
    }
}
