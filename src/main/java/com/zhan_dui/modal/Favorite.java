package com.zhan_dui.modal;

import org.json.JSONObject;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name="Favorite")
public class Favorite {
    @Column(name = "aid")
	public final Integer animationId;
    @Column(name = "addtime")
	public final Long addTime;

	public Favorite(JSONObject object, Long addTime) {
		this.addTime = addTime;
		animationId = Animation.build(object).AnimationId;
	}

	public Favorite(Animation video, Long addTime) {
		this.addTime = addTime;
		animationId = video.AnimationId;
	}

	public Favorite(Animation video) {
		animationId = video.AnimationId;
		addTime = System.currentTimeMillis();
	}
}
