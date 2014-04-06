package com.zhan_dui.utils.m3u8;

import java.net.URI;

/**
 * A playlist element.
 *
 * @author dkuffner
 */
public interface Element {

    public String getTitle();


    /**
     * Return item duration rounded to nearest integer. This is compatible with old
     * versions of m3u8 draft
     * @return
     */
    public int getDuration();

    
    /**
     * Return item duration as it appears in m3u8. This allows to properly support new playlists
     * with fractional durations
     * @return
     */
    public double getExactDuration();
    
    /**
     * URI to media or playlist.
     *
     * @return the URI.
     */
    public URI getURI();

    /**
     * Media can be encrypted.
     *
     * @return true if media encrypted.
     */
    public boolean isEncrypted();

    /**
     * Element can be another playlist.
     *
     * @return true if element a playlist.
     */
    public boolean isPlayList();

    /**
     * Element is a media file.
     *
     * @return true if element a media file and not a playlist.
     */
    public boolean isMedia();
    
    /**
     * There is discontinuity before this element
     * @return
     */
    public boolean isDiscontinuity();

    /**
     * If media is encryped than will this method return a info object.
     *
     * @return the info object or null if media not encrypted.
     */
    public EncryptionInfo getEncryptionInfo();

    /**
     * If element a playlist than this method will return a PlaylistInfo object.
     *
     * @return a info object or null in case of element is not a playlist.
     */
    public PlaylistInfo getPlayListInfo();

    /**
     * The program date.
     *
     * @return -1 in case of program date is not set.
     */
    public long getProgramDate();

}
