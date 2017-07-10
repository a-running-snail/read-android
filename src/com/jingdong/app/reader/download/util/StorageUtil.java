package com.jingdong.app.reader.download.util;

import java.io.File;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;


public class StorageUtil {

	private static final int ERROR = -1;

    /**
     * 
     * @Title: externalMemoryAvailable
     * @Description: SDCARD是否存在
     * @param @return
     * @return boolean
     * @throws
     */
    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 
     * @Title: getAvailableInternalMemorySize
     * @Description: 获取手机内部剩余存储空间
     * @param @return
     * @return long
     * @throws
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 
     * @Title: getTotalInternalMemorySize
     * @Description:获取手机内部总的存储空间
     * @param @return
     * @return long
     * @throws
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 
     * @Title: getAvailableExternalMemorySize
     * @Description: 获取SDCARD剩余存储空间
     * @param @return
     * @return long
     * @throws
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * 
     * @Title: getTotalExternalMemorySize
     * @Description: 获取SDCARD总的存储空间
     * @param @return
     * @return long
     * @throws
     */
    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
    }
    
    /**
     * 获取当前可用内存，返回数据以字节为单位。
     * 
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

}
