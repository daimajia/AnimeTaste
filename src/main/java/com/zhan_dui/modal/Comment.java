package com.zhan_dui.modal;

import java.util.Date;

public class Comment {
	public final String Username;
	public final String Avatar;
	public final String Platform;
	public final Date Date;
	public final String Content;

	public Comment(String username, String avatar, String platform, Date date,
			String content) {
		Username = username;
		Avatar = avatar;
		Platform = platform;
		Date = date;
		Content = content;
	}
}
