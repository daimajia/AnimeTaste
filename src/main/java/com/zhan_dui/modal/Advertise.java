package com.zhan_dui.modal;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by daimajia on 14-1-16.
 */
@Table(name="Advertise")
public class Advertise extends Model implements Parcelable{
    @Column(name="adid")
    public int Id;
    @Column(name="name")
    public String Name;
    @Column(name="device")
    public String Device;
    @Column(name="brief")
    public String Brief;
    @Column(name="link")
    public String Link;
    @Column(name="detailpic")
    public String DetailPic;

    public Advertise(){}

    private Advertise(int id, String name, String device, String brief, String link, String detailPic) {
        Id = id;
        Name = name;
        Device = device;
        Brief = brief;
        Link = link;
        DetailPic = detailPic;
    }

    public static Advertise build(JSONObject object){
        try{
            return new Advertise(object.getInt("Id"),object.getString("Name"),object.getString("Device"),object.getString("Brief"),object.getString("Link"),object.getString("DetailPic"));
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(Id);
        parcel.writeString(Name);
        parcel.writeString(Device);
        parcel.writeString(Brief);
        parcel.writeString(Link);
        parcel.writeString(DetailPic);
    }

    public static final Creator<Advertise> CREATOR = new Creator<Advertise>() {
        @Override
        public Advertise createFromParcel(Parcel parcel) {
            return new Advertise(parcel);
        }

        @Override
        public Advertise[] newArray(int size) {
            return new Advertise[size];
        }
    };

    private Advertise(Parcel in){
        Id = in.readInt();
        Name = in.readString();
        Device = in.readString();
        Brief = in.readString();
        Link = in.readString();
        DetailPic = in.readString();
    }
}
