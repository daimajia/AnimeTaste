package com.zhan_dui.modal;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.zhan_dui.download.alfred.missions.M3U8Mission;

import java.util.Date;
import java.util.List;

/**
 * Created by daimajia on 14-4-3.
 */
@Table(name="DownloadRecord")
public class DownloadRecord extends Model implements Parcelable {

    public enum STATUS{
        DOWNLOADING,ERROR,SUCCESS,CANCELED
    };
    /**
     * Animation Information
     */
    @Column(name = "AnimationId",unique = true,onUniqueConflict= Column.ConflictAction.IGNORE,onUniqueConflicts = Column.ConflictAction.IGNORE)
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
    /**
     * Download Information
     */
    @Column(name = "AddedTime")
    public Date AddedTime;
    @Column(name = "Size")
    public long Size;
    @Column(name = "DownloadedSize")
    public long DownloadedSize;
    @Column(name = "Duration")
    public int Duration;
    @Column(name = "DownloadedDuration")
    public int DownloadedDuration;
    @Column(name = "Segments")
    public int Segments;
    @Column(name = "DownloadedSegments")
    public int DownloadedSegments;
    @Column(name = "DownloadedPercentage")
    public int DownloadedPercentage;
    @Column(name = "RangeStart")
    public  int RangeStart;
    @Column(name = "Status")
    public STATUS Status;
    @Column(name = "Extra")
    public String Extra;
    @Column(name = "SaveDir")
    public String SaveDir;
    @Column(name = "SaveFileName")
    public String SaveFileName;
    @Column(name = "UsingDownloadUrl")
    public String UsingDownloadUrl;


    private DownloadRecord(Animation animation){
        AnimationId = animation.AnimationId;
        Name = animation.Name;
        OriginVideoUrl = animation.OriginVideoUrl;
        HDVideoUrl = animation.HDVideoUrl;
        CommonVideoUrl = animation.CommonVideoUrl;
        Author = animation.Author;
        Year = animation.Year;
        Brief = animation.Brief;
        HomePic = animation.HomePic;
        DetailPic = animation.DetailPic;
        Youku = animation.Youku;
        UHD = animation.UHD;
        HD = animation.HD;
        SD = animation.SD;
        IsFav = animation.isFavorite();
        IsWatched = animation.isWatched();
    }

    public DownloadRecord(Animation animation,M3U8Mission mission){
        this(animation);
        AddedTime = new Date();
        Size = mission.getFilesize();
        DownloadedSize = mission.getDownloaded();
        Duration = mission.getVideoDuration();
        DownloadedPercentage = mission.getPercentage();
        Segments = mission.getSegmentsCount();
        Duration = mission.getVideoDuration();
        DownloadedDuration = mission.getDownloadedDuration();
        SaveDir = mission.getSaveDir();
        SaveFileName = mission.getSaveName();
        UsingDownloadUrl = mission.getUri();
    }

    public static Animation getFromDownloadRecord(DownloadRecord record){
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Name);
        dest.writeString(this.OriginVideoUrl);
        dest.writeString(this.HDVideoUrl);
        dest.writeString(this.CommonVideoUrl);
        dest.writeString(this.Author);
        dest.writeString(this.Year);
        dest.writeString(this.Brief);
        dest.writeString(this.HomePic);
        dest.writeString(this.DetailPic);
        dest.writeString(this.Youku);
        dest.writeString(this.UHD);
        dest.writeString(this.HD);
        dest.writeString(this.SD);
        dest.writeLong(AddedTime != null ? AddedTime.getTime() : -1);
        dest.writeLong(this.Size);
        dest.writeLong(this.DownloadedSize);
        dest.writeInt(this.Duration);
        dest.writeInt(this.DownloadedDuration);
        dest.writeInt(this.Segments);
        dest.writeInt(this.DownloadedSegments);
        dest.writeInt(this.RangeStart);
        dest.writeInt(this.Status == null ? -1 : this.Status.ordinal());
        dest.writeString(this.Extra);
    }

    public DownloadRecord() {
    }

    private DownloadRecord(Parcel in) {
        this.Name = in.readString();
        this.OriginVideoUrl = in.readString();
        this.HDVideoUrl = in.readString();
        this.CommonVideoUrl = in.readString();
        this.Author = in.readString();
        this.Year = in.readString();
        this.Brief = in.readString();
        this.HomePic = in.readString();
        this.DetailPic = in.readString();
        this.Youku = in.readString();
        this.UHD = in.readString();
        this.HD = in.readString();
        this.SD = in.readString();
        long tmpDownloadTime = in.readLong();
        this.AddedTime = tmpDownloadTime == -1 ? null : new Date(tmpDownloadTime);
        this.Size = in.readLong();
        this.DownloadedSize = in.readLong();
        this.Duration = in.readInt();
        this.DownloadedDuration = in.readInt();
        this.Segments = in.readInt();
        this.DownloadedSegments = in.readInt();
        this.RangeStart = in.readInt();
        int tmpStatus = in.readInt();
        this.Status = tmpStatus == -1 ? null : STATUS.values()[tmpStatus];
        this.Extra = in.readString();
    }

    public static Animation getAnimation(DownloadRecord record){
        return Animation.build(record);
    }

    public static Parcelable.Creator<DownloadRecord> CREATOR = new Parcelable.Creator<DownloadRecord>() {
        public DownloadRecord createFromParcel(Parcel source) {
            return new DownloadRecord(source);
        }
        public DownloadRecord[] newArray(int size) {
            return new DownloadRecord[size];
        }
    };

    public static void deleteOne(Animation animation){
        new Delete().from(DownloadRecord.class).where("AnimationId = ?",animation.AnimationId).execute();
    }

    public static List<DownloadRecord> getAllDownloaded(){
        return new Select()
                .from(DownloadRecord.class)
                .where("Status = ?",STATUS.SUCCESS.ordinal())
                .orderBy("AddedTime desc")
                .execute();
    }

    public static List<DownloadRecord> getAllFailures(){
        return new Select()
                .from(DownloadRecord.class)
                .where("Status = ?",STATUS.ERROR.ordinal())
                .orderBy("AddedTime desc")
                .execute();
    }

//    public static List<DownloadRecord> getAllCanceled(){
//        return new Select()
//                .from(DownloadRecord.class)
//                .where("Status = ? ",STATUS.CANCELED.ordinal())
//                .orderBy("AddedTime desc")
//                .execute();
//    }

    public static DownloadRecord getFromAnimation(Animation animation,boolean needSuccess){
        if(needSuccess){
            return new Select().from(DownloadRecord.class)
                    .where("AnimationId = ? and Status = ?",animation.AnimationId,STATUS.SUCCESS.ordinal())
                    .executeSingle();
        }else{
            return new Select().from(DownloadRecord.class)
                    .where("AnimationId = ?",animation.AnimationId)
                    .executeSingle();
        }
    }

    public static void save(Animation animation,M3U8Mission mission){
        DownloadRecord record = new Select()
                .from(DownloadRecord.class)
                .where("AnimationId = ?" , animation.AnimationId)
                .executeSingle();
        if(record == null){
            Log.e("Download","START CREATE A RECORD");
            new DownloadRecord(animation,mission).save();
        }else{
            Log.e("Download","UPDATE");
            int status;
            if(mission.isDone()){
                status = mission.isSuccess() ? STATUS.SUCCESS.ordinal() : STATUS.ERROR.ordinal();
                status = mission.isCanceled() ? STATUS.CANCELED.ordinal() : status;
            }else{
                status = STATUS.DOWNLOADING.ordinal();
            }
            new Update(DownloadRecord.class)
                            .set("Size = ?," +
                                    "DownloadedSize = ?," +
                                    "Duration = ?," +
                                    "DownloadedDuration = ?," +
                                    "Segments = ?," +
                                    "DownloadedSegments = ?," +
                                    "DownloadedPercentage = ?," +
                                    "RangeStart = ?,"+
                                    "Status = ?," +
                                    "Extra = ?," +
                                    "SaveDir = ?," +
                                    "SaveFileName = ? ," +
                                    "UsingDownloadUrl = ? ",
                            mission.getFilesize(),
                            mission.getDownloaded(),
                            mission.getVideoDuration(),
                            mission.getDownloadedDuration(),
                            mission.getSegmentsCount(),
                            mission.getDownloadedSegmentCount(),
                            mission.getPercentage(),
                            mission.getCurrentSegmentDownloaded(),
                            status,
                            "",
                            mission.getSaveDir(),
                            mission.getSaveName(),
                            mission.getUri())
                    .where("AnimationId = ?",animation.AnimationId)
                    .execute();
        }
    }

}
