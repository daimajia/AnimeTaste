package com.zhan_dui.utils.m3u8;

import java.net.URI;

/**
 * @author dkuffner
 */
final class ElementImpl implements Element {
    private final PlaylistInfo playlistInfo;
    private final EncryptionInfo encryptionInfo;
    private final double duration;
    private final URI uri;
    private final boolean discontinuity;
    private final String title;
    private final long programDate;

    public ElementImpl(PlaylistInfo playlistInfo, EncryptionInfo encryptionInfo, double duration, URI uri,
    		String title, long programDate, boolean discontinuity) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }

        if (duration < -1) {
            throw new IllegalArgumentException();
        }
        if (playlistInfo != null && encryptionInfo != null) {
            throw new IllegalArgumentException("Element cannot be a encrypted playlist.");
        }
        this.playlistInfo = playlistInfo;
        this.encryptionInfo = encryptionInfo;
        this.duration = duration;
        this.uri = uri;
        this.title = title;
        this.discontinuity=discontinuity;
        this.programDate = programDate;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return (int)Math.round(duration);
    }
    
    public boolean isDiscontinuity() {
    	return discontinuity;
    }
    public double getExactDuration() {
    	return duration;
    }

    public URI getURI() {
        return uri;
    }

    public boolean isEncrypted() {
        return encryptionInfo != null;
    }

    public boolean isPlayList() {
        return playlistInfo != null;
    }

    public boolean isMedia() {
        return playlistInfo == null;
    }

    public EncryptionInfo getEncryptionInfo() {
        return encryptionInfo;
    }

    public PlaylistInfo getPlayListInfo() {
        return playlistInfo;
    }

    public long getProgramDate() {
        return programDate;
    }

    @Override
    public String toString() {
        return "ElementImpl{" +
                "playlistInfo=" + playlistInfo +
                ", encryptionInfo=" + encryptionInfo +
                ", discontinuity=" +discontinuity +
                ", duration=" + duration +
                ", uri=" + uri +
                ", title='" + title + '\'' +
                '}';
    }

    static final class PlaylistInfoImpl implements PlaylistInfo {
        private final int programId;
        private final int bandWidth;
        private final String codec;

        public PlaylistInfoImpl(int programId, int bandWidth, String codec) {
            this.programId = programId;
            this.bandWidth = bandWidth;
            this.codec = codec;
        }

        public int getProgramId() {
            return programId;
        }

        public int getBandWitdh() {
            return bandWidth;
        }

        public String getCodecs() {
            return codec;
        }

        @Override
        public String toString() {
            return "PlaylistInfoImpl{" +
                    "programId=" + programId +
                    ", bandWidth=" + bandWidth +
                    ", codec='" + codec + '\'' +
                    '}';
        }
    }

    static final class EncryptionInfoImpl implements EncryptionInfo {
        private final URI uri;
        private final String method;

        public EncryptionInfoImpl(URI uri, String method) {
            this.uri = uri;
            this.method = method;
        }

        public URI getURI() {
            return uri;
        }

        public String getMethod() {
            return method;
        }

        @Override
        public String toString() {
            return "EncryptionInfoImpl{" +
                    "uri=" + uri +
                    ", method='" + method + '\'' +
                    '}';
        }
    }
}
