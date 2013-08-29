package com.zhan_dui.utils;

import android.content.res.Configuration;

public class OrientationHelper {

	public static final int LANDSCAPE = Configuration.ORIENTATION_LANDSCAPE;
	public static final int PORTRAIT = Configuration.ORIENTATION_PORTRAIT;
	public static final int NOTHING = -100;

	public static Integer userTending(int orientation, int previous) {

		if (previous == PORTRAIT) {
			if (orientation > 85 && orientation < 115) {
				return LANDSCAPE;
			} else if (orientation > 285 && orientation < 300) {
				return LANDSCAPE;
			} else if(orientation > 160 && orientation < 210){
				return LANDSCAPE;
			}
		} else if (previous == LANDSCAPE) {
			if (orientation > 0 && orientation < 30) {
				return PORTRAIT;
			} else if (orientation > 330 && orientation < 360) {
				return PORTRAIT;
			}
		}
		return NOTHING;
	}
}
