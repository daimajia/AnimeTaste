package com.zhan_dui.utils;

import java.security.MessageDigest;

public class MD5 {
	static final String HEXES = "0123456789abcdef";

	private static String getHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
					HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	public static String digest(String toMd5) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("md5");
			md5.update(toMd5.getBytes());
			return getHex(md5.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
