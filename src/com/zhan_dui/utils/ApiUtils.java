package com.zhan_dui.utils;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class ApiUtils {
	public static String getAccessToken(TreeMap<String, String> map,
			String app_secret) {
		String toMd5 = "";
		Set<String> keys = map.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			toMd5 += key + "=" + map.get(key) + "&";

		}
		toMd5 = toMd5.substring(0, toMd5.length() - 1);
		toMd5 += app_secret;
		return MD5.digest(toMd5);
	}
}
