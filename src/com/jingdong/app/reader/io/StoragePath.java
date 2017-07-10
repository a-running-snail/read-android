package com.jingdong.app.reader.io;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import static android.os.Environment.MEDIA_MOUNTED;

public class StoragePath {
    private static final String BookDir                     = "Book";
    private static final String ImageDir                    = "Image";
    private static final String DatabaseDir                 = "db";
    private static final String DocumentDir                 = "Document";
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static void init(Context context) {
        try {
            File bookDir = getBookDir(context);
            if (!bookDir.exists()) {
                bookDir.mkdirs();
            }

            File imageDir = getImageDir(context);
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            File docDir = getDocumentDir(context);
            if (!docDir.exists()) {
                docDir.mkdirs();
            }

            File cacheDir = getCachesDir(context, true);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getBookDir(Context context) {
        return context.getExternalFilesDir(BookDir);
    }

    public static File getImageDir(Context context) {
        return context.getExternalFilesDir(ImageDir);
    }

    public static File getDatabaseDir(Context context) {
        return context.getExternalFilesDir(DatabaseDir);
    }

    public static File getDocumentDir(Context context) {
        return context.getExternalFilesDir(DocumentDir);
    }

    public static File getCachesDir(Context context, boolean preferExternal) {

        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) {
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = context.getExternalCacheDir();
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }
}
