package com.zhan_dui.utils.m3u8;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dkuffner
 */

final class M3uConstants {
    private M3uConstants() {
        throw new AssertionError("Not allowed");
    }

    final static String COMMENT_PREFIX = "#";

    final static String EX_PREFIX = "#EXT";


    /**
     * An Extended M3U file is distinguished from a basic M3U file by its first line, which MUST be #EXTM3U.
     */
    final static String EXTM3U = "#EXTM3U";

    /**
     * EXTINF is a record marker that describes the media file identified by the URI that follows it.
     * Each media file URI MUST be preceded by an EXTINF tag.  Its format is: #EXTINF:&lt;duration&gt;,&lt;title&gt;
     * <p/>
     * "duration" is an integer that specifies the duration of the media file in seconds.
     * Durations SHOULD be rounded to the nearest integer. The remainder of the line following
     * the comma is the title of the media file.
     */
    final static String EXTINF = "#EXTINF";


    /**
     * The EXT-X-TARGETDURATION tag indicates the approximate duration of the next media file that will be added to
     * the main presentation.  It MUST appear in the Playlist file.  Its format is: #EXT-X-TARGETDURATION:&lt;seconds&gt;
     * <p/>
     * The actual duration of the media file MAY differ slightly from the target duration.
     */
    final static String EXT_X_TARGET_DURATION = "#EXT-X-TARGETDURATION";

    /**
     * Each media file URI in a Playlist has a unique sequence number.  The sequence number
     * of a URI is equal to the sequence number of the URI that preceded it plus one.
     * The EXT-X-MEDIA-SEQUENCE tag indicates the sequence number of the first URI that appears
     * in a Playlist file. Its format is: #EXT-X-MEDIA-SEQUENCE:&lt;number&gt;
     * <p/>
     * If the Playlist file does not contain an EXT-X-MEDIA-SEQUENCE tag then the sequence number
     * of the first URI in the playlist SHALL be considered to be 1.
     */
    final static String EXT_X_MEDIA_SEQUENCE = "#EXT-X-MEDIA-SEQUENCE";


    /**
     * Media files MAY be encrypted.  The EXT-X-KEY tag provides information necessary to decrypt media files
     * that follow it.  Its format is: #EXT-X-KEY:METHOD=&lt;method&gt;[,URI="&lt;URI&gt;"]
     * <p/>
     * The METHOD parameter specifies the encryption method.  The URI parameter, if present,
     * specifies how to obtain the key.
     * <p/>
     * Version 1.0 of the protocol defines two encryption methods: NONE and AES-128.  An encryption
     * method of NONE means that media files are not encrypted.
     * <p/>
     * An encryption method of AES-128 means that media files are encrypted using the Advanced Encryption
     * Standard [AES_128] with a 128-bit key and PKCS7 padding [RFC3852].
     * <p/>
     * A new EXT-X-KEY supersedes any prior EXT-X-KEY.
     * If no EXT-X-KEY tag is present then media files are not encrypted.
     */
    final static String EXT_X_KEY = "#EXT-X-KEY";

    /**
     * The EXT-X-PROGRAM-DATE-TIME tag associates the beginning of the next
     * media file with an absolute date and/or time.  The date/time
     * representation is ISO/IEC 8601:2004 [ISO_8601] and SHOULD indicate a
     * time zone.  For example:   #EXT-X-PROGRAM-DATE-TIME:&lt;YYYY-MM-DDThh:mm:ssZ&gt;
     */
    final static String EXT_X_PROGRAM_DATE_TIME = "#EXT-X-PROGRAM-DATE-TIME";

    /**
     * The EXT-X-ALLOW-CACHE tag indicates whether the client MAY cache
     * downloaded media files for later replay.  Its format is:
     * <p/>
     * #EXT-X-ALLOW-CACHE:&lt;YES|NO&gt;
     */
    final static String EXT_X_ALLOW_CACHE = "#EXT-X-ALLOW-CACHE";


    /**
     * The EXT-X-STREAM-INF tag indicates that the next URI in the Playlist
     * file identifies another Playlist file.  Its format is:
     * <p/>
     * #EXT-X-STREAM-INF:[attribute=value][,attribute=value]*
     * &lt;URI&gt;
     * <p/>
     * The following attributes are defined for the EXT-X-STREAM-INF tag:
     * <p/>
     * BANDWIDTH=&lt;n&gt;
     * <p/>
     * where n is an approximate upper bound of the stream bitrate,
     * expressed as a number of bits per second.
     * <p/>
     * PROGRAM-ID=&lt;i&gt;
     * <p/>
     * where i is a number that uniquely identifies a particular
     * presentation within the scope of the Playlist file.
     * <p/>
     * A Playlist file MAY contain multiple EXT-X-STREAM-INF URIs with the
     * same PROGRAM-ID to describe variant streams of the same presentation.
     */
    final static String EXT_X_STREAM_INF = "#EXT-X-STREAM-INF";

    /**
     * The EXT-X-ENDLIST tag indicates that no more media files will be
     * added to the Playlist file.
     */
    final static String EXT_X_ENDLIST = "#EXT-X-ENDLIST";

    /**
     * The EXT-X-DISCONTINUITY tag indicates that the media file following
     * it has different characteristics than the one that preceded it.  The
     * set of characteristics that MAY change is:
     * <p/>
     * file format
     * <p/>
     * number and type of tracks
     * <p/>
     * encoding parameters
     * <p/>
     * encoding sequence
     * <p/>
     * timestamp sequence
     * <p/>
     * Its format is:
     * <p/>
     * #EXT-X-DISCONTINUITY
     */
    final static String EXT_X_DISCONTINUITY = "#EXT-X-DISCONTINUITY";

    /**
     * A holder class for Patterns.
     */
    static class Patterns {
        private Patterns() {
            throw new AssertionError();
        }

        final static Pattern EXTINF = Pattern.compile(tagPattern(M3uConstants.EXTINF) + "\\s*(-1|[0-9\\.]*)\\s*(?:,((.*)))?");

        private static String tagPattern(String tagName) {
            return "\\s*" + tagName + "\\s*:\\s*";
        }

        // #EXT-X-KEY:METHOD=&lt;method&gt;[,URI="&lt;URI&gt;"
        // #EXT-X-KEY:METHOD=AES-128,URI="https://priv.example.com/key.php?r=52"
        final static Pattern EXT_X_KEY = Pattern.compile(tagPattern(M3uConstants.EXT_X_KEY) + "METHOD=([0-9A-Za-z\\-]*)(,URI=\"(([^\\\\\"]*.*))\")?");

        final static Pattern EXT_X_TARGET_DURATION = Pattern.compile(tagPattern(M3uConstants.EXT_X_TARGET_DURATION) + "([0-9]*)");
        final static Pattern EXT_X_MEDIA_SEQUENCE = Pattern.compile(tagPattern(M3uConstants.EXT_X_MEDIA_SEQUENCE) + "([0-9]*)");

        // YYYY-MM-DDThh:mm:ss
        final static Pattern EXT_X_PROGRAM_DATE_TIME = Pattern.compile(tagPattern(M3uConstants.EXT_X_PROGRAM_DATE_TIME)
                + "(.*)");
        // YYYY       MM                 DD                    hh              mm              ss


        /**
         * Helper method to create a date object for EXT_X_PROGRAM_DATE_TIME pattern.
         *
         * @param line       the line to parse.
         * @param lineNumber line number.
         * @return date as long.
         * @throws ParseException in case of date time cannot be parsed.
         */
        static long toDate(String line, int lineNumber) throws ParseException {
            Matcher matcher = Patterns.EXT_X_PROGRAM_DATE_TIME.matcher(line);

            if (!matcher.find() || !matcher.matches() || matcher.groupCount() < 1) {
                throw new ParseException(line, lineNumber, " must specify date-time");
            }

            SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            System.out.println(ISO8601FORMAT.format(new Date()));
            String dateTime = matcher.group(1);
            try {
                return ISO8601FORMAT.parse(dateTime).getTime();
            } catch (java.text.ParseException e) {
                throw new ParseException(line, lineNumber, e);
            }
            /*int index = 1;
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, toInt(matcher, line, lineNumber, index));
            c.set(Calendar.MONTH, toInt(matcher, line, lineNumber, ++index)-1);
            c.set(Calendar.DAY_OF_MONTH, toInt(matcher, line, lineNumber, ++index));
            c.set(Calendar.HOUR, toInt(matcher, line, lineNumber, ++index));
            c.set(Calendar.MINUTE, toInt(matcher, line, lineNumber, ++index));
            c.set(Calendar.SECOND, toInt(matcher, line, lineNumber, ++index));
            return c.getTime().getTime();*/

        }


        /*
       private static int toInt(Matcher matcher, String line, int lineNumber, int index) throws IllegalTypeParseException {
           try {
               return Integer.valueOf(matcher.group(index));
           } catch(NumberFormatException e) {
               throw new IllegalTypeParseException(line, lineNumber, e);
           }
       } */
    }
}
