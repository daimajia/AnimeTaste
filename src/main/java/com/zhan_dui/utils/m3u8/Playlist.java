package com.zhan_dui.utils.m3u8;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.List;

/**
 * @author dkuffner
 */
public final class Playlist implements Iterable<Element> {

    public static Playlist parse(Readable readable) throws ParseException {
        if (readable == null) {
            throw new NullPointerException("playlist");
        }
        return PlaylistParser.create(PlaylistType.M3U8).parse(readable);
    }

    public static Playlist parse(String playlist) throws ParseException {
        if (playlist == null) {
            throw new NullPointerException("playlist");
        }
        return parse(new StringReader(playlist));
    }

    public static Playlist parse(InputStream playlist) throws ParseException {
        if (playlist == null) {
            throw new NullPointerException("playlist");
        }
        return parse(new InputStreamReader(playlist));
    }

    public static Playlist parse(ReadableByteChannel playlist) throws ParseException {
        if (playlist == null) {
            throw new NullPointerException("playlist");
        }
        return parse(makeReadable(playlist));
    }

    private static Readable makeReadable(ReadableByteChannel source) {
        if (source == null) {
            throw new NullPointerException("source");
        }


        return Channels.newReader(source,
                java.nio.charset.Charset.defaultCharset().name());
    }

    private final List<Element> elements;
    private final boolean endSet;
    private final int targetDuration;
    private int mediaSequenceNumber;

    Playlist(List<Element> elements, boolean endSet, int targetDuration, int mediaSequenceNumber) {
        if (elements == null) {
            throw new NullPointerException("elements");
        }
        this.targetDuration = targetDuration;
        this.elements = elements;
        this.endSet = endSet;
        this.mediaSequenceNumber = mediaSequenceNumber;
    }

    public int getTargetDuration() {
        return targetDuration;
    }

    public Iterator<Element> iterator() {
        return elements.iterator();
    }

    public List<Element> getElements() {
        return elements;
    }

    public boolean isEndSet() {
        return endSet;
    }

    public int getMediaSequenceNumber() {
        return mediaSequenceNumber;
    }

    @Override
    public String toString() {
        return "PlayListImpl{" +
                "elements=" + elements +
                ", endSet=" + endSet +
                ", targetDuration=" + targetDuration +
                ", mediaSequenceNumber=" + mediaSequenceNumber +
                '}';
    }
}