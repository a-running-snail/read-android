package com.jingdong.app.reader.pdf;

import com.jingdong.app.reader.util.FileGuider;

import java.io.IOException;
import java.util.HashSet;

public class PDFDeviceInfo {
    public static byte[] KeyBook = null;
    public static byte[] KeyUUID = null;
    public static byte[] KeyRand = null;

    public static HashSet<String> NonStandardFontsName = null;
    public static String          NonStandardFontsPath = null;
    public static int             NonStandardFontsNumb = 0;

    static {
        FileGuider fontFileGuider = new FileGuider(FileGuider.SPACE_PRIORITY_EXTERNAL);
        fontFileGuider.setImmutable(true);
        fontFileGuider.setChildDirName("fonts/");

        try {
            PDFDeviceInfo.NonStandardFontsPath = fontFileGuider.getParentPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetInfo ()
    {
        KeyBook = null;
        KeyUUID = null;
        KeyRand = null;
        NonStandardFontsName = null;
        NonStandardFontsNumb = 0;
    }
}