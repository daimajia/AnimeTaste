package com.zhan_dui.utils.m3u8;

/**
 * @author dkuffner
 */
enum PlaylistType {
    M3U8("UTF-8", "application/vnd.apple.mpegurl", "m3u8"),
    M3U("US-ASCII", "audio/mpegurl", "m3u");

    final String encoding;
    final String contentType;
    final String extension;

    PlaylistType(String encoding, String contentType, String extension) {
        this.encoding = encoding;
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }
}
