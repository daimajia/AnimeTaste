package com.zhan_dui.modal;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by daimajia on 14-1-18.
 */
@Table(name="WatchRecord")
public class WatchRecord extends Model{
    public WatchRecord(){}

    public WatchRecord(int animationId, boolean isWatched) {
        this.animationId = animationId;
        this.isWatched = isWatched;
    }

    @Column(name="aid",unique = true,onUniqueConflict = Column.ConflictAction.ABORT)
    public int animationId;

    @Column(name="watched")
    public boolean isWatched;


}
