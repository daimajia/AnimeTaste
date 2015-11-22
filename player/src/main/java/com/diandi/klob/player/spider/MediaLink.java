package com.diandi.klob.player.spider;


import java.util.List;

/**
 * *******************************************************************************
 * *********    Author : klob(kloblic@gmail.com) .
 * *********    Date : 2015-11-19  .
 * *********    Time : 14:11 .
 * *********    Version : 1.0
 * *********    Copyright © 2015, klob, All Rights Reserved
 * *******************************************************************************
 */
public class MediaLink {
    public String type;
    public String m3u8;

    /**
     * “segs”,”type”,”清晰度”
     * "hd3", "flv", "1080P"
     * "hd2", "flv", "超清"
     * "flvhd", "flv", "高清"
     * "mp4", "mp4", "高清"
     * "flv", "flv", "标清"
     * "3gphd", "3gp", "高清"
     */
    public static MediaLink getRecommendUrl(List<MediaLink> links) {
        switch (links.size()) {
            case 1:
                return links.get(0);
            case 2:
                return links.get(1);
            case 3:
                return links.get(2);
            case 4:
                return links.get(2);
            default:
                return links.get(0);
        }

    }

    @Override
    public String toString() {
        return "MediaLink{" +
                "type='" + type + '\'' +
                ", m3u8='" + m3u8 + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof String && type.equals(o) || o instanceof MediaLink && ((MediaLink) o).type.equals(o);
    }

}
