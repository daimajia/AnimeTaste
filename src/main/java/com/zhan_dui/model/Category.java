package com.zhan_dui.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by daimajia on 14-1-16.
 */
@Table(name = "Categories")
public class Category extends Model implements Parcelable {
    @Column(name="cid")
    public int cid;
    @Column(name="name")
    public String Name;
    @Column(name="count")
    public int Count;

    public Category(){}

    public Category(int cid,String name,int count){
        this.cid = cid;
        this.Name = name;
        this.Count = count;
    }

    public static Category build(JSONObject object){
        try {
            return new Category(object.getInt("id"),object.getString("name"),object.getInt("count"));
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(cid);
        parcel.writeString(Name);
        parcel.writeInt(Count);
    }

    public Category(Parcel in){
        cid = in.readInt();
        Name = in.readString();
        Count = in.readInt();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel parcel) {
            return new Category(parcel);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
