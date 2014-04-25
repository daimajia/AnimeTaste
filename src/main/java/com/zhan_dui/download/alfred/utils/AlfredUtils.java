package com.zhan_dui.download.alfred.utils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by daimajia on 14-1-30.
 */
public class AlfredUtils {
    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static String getReadableSize(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
                + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getReadableSize(long bytes) {
        if (bytes <= 0)
            return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(bytes
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    public static String getReadableSpeed(long downloaded,long timespend,TimeUnit timeUnit) {
        long span = timeUnit.toSeconds(timespend);
        if(timespend * span == 0){
            return "0";
        }
        return getReadableSize(downloaded/span,true) + "/s";
    }

    public static String getFileNameWithExtention(String url){
        String fileName;
        int slashIndex = url.lastIndexOf("/");
        int qIndex = url.lastIndexOf("?");
        if (qIndex > slashIndex) {//if has parameters
            fileName = url.substring(slashIndex + 1, qIndex);
        } else {
            fileName = url.substring(slashIndex + 1);
        }
        return fileName;
    }

    public static String getFileName(String url) {
        String fileNameWithExtension = getFileNameWithExtention(url);
        String fileName = fileNameWithExtension;
        if (fileNameWithExtension.contains(".")) {
            fileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf("."));
        }
        return fileName;
    }

    public static String getFileExtension(String url){
        String extenstion;
        int pointIndex = url.lastIndexOf(".");
        int qIndex = url.lastIndexOf("?");
        if(qIndex > pointIndex){
            extenstion = url.substring(pointIndex+1,qIndex);
        }else{
            extenstion = url.substring(pointIndex+1);
        }
        return extenstion;
    }

    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
