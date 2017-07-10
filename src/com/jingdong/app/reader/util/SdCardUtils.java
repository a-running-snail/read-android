package com.jingdong.app.reader.util;
import java.io.BufferedReader;  
import java.io.File;  
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.util.ArrayList;  
import java.util.HashMap;
import java.util.List;  
import java.util.Map;
import java.util.Scanner;

import com.jingdong.app.reader.application.MZBookApplication;

import android.os.Build;
import android.os.Environment;  
import android.text.TextUtils;
  
public class SdCardUtils {  
	
	public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";
  
    // 返回值不带File seperater "/",如果没有外置第二个sd卡,返回null  
    public static String getSecondExterPath() {  
        List<String> paths = getAllExterSdcardPath();  
        MZLog.d("performance",paths.size()+"");
        if (paths.size() > 1 ) {  
        	
            for (String path : paths) {  
            	MZLog.d("performance",path);
                if (path != null && !path.equalsIgnoreCase(getFirstExterPath())) {   
                    return path;  
                }  
            }  
  
            return null;  
  
        } else {  
            return null;  
        }  
    }  
  
    public static boolean isFirstSdcardMounted(){  
        if (!Environment.getExternalStorageState().equals(  
                Environment.MEDIA_MOUNTED)) {  
            return false;  
        }  
        return true;  
    }  
      
    public static boolean isSecondSDcardMounted() {  
        String sd2 = getSecondExterPath();  
        if (sd2 == null) {  
            return false;  
        }  
       // if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        //	return true;
        return checkFsWritable(sd2 + File.separator);  
  
    }  
  
  private static boolean checkFsWritable(String dir) {  
  
        if (dir == null)  
            return false;  
        
  
        File directory = new File(dir);  
  
        if (!directory.isDirectory()) {  
            if (!directory.mkdirs()) {  
                return false;  
            }  
        }  
        String path = MZBookApplication.getContext().getPackageName();
        File f = new File( dir + File.separator+ "Android"+File.separator +"data"+File.separator + 
				MZBookApplication.getContext().getPackageName());  
        try {  
            if (f.exists()) {  
            	return true;  
            }  
            if (!f.mkdir()) {  
                return false;  
            }  
            return true;  
  
        } catch (Exception e) {  
        	e.printStackTrace();
        }  
        return false;  
  
    }  
  
    public static String getFirstExterPath() {  
        return Environment.getExternalStorageDirectory().getPath();  
    }  
    
    
    
  
    public static List<String> getAllExterSdcardPath() {  
        List<String> SdList = new ArrayList<String>();  
  
        String firstPath = getFirstExterPath();  
        String secondPath = System.getenv("SECONDARY_STORAGE");
        if  (!TextUtils.isEmpty(secondPath))
        	secondPath = secondPath.split(":")[0];
        MZLog.d("performance","firstPath: "+firstPath);
        MZLog.d("performance","secondary: "+secondPath);
        // 得到路径  
        try {  
            Runtime runtime = Runtime.getRuntime();  
            Process proc = runtime.exec("mount");  
            InputStream is = proc.getInputStream();  
            InputStreamReader isr = new InputStreamReader(is);  
            String line;  
            BufferedReader br = new BufferedReader(isr);  
            while ((line = br.readLine()) != null) { 
                // 将常见的linux分区过滤掉  
            	if (line.contains("secure"))  
                    continue;  
                if (line.contains("asec"))  
                    continue;  
                if (line.contains("media"))  
                    continue;  
                if (line.contains("system") || line.contains("cache")  
                        || line.contains("sys") || line.contains("data")  
                        || line.contains("tmpfs") || line.contains("shell")  
                        || line.contains("root") || line.contains("acct")  
                        || line.contains("proc") || line.contains("misc")  
                        || line.contains("obb")) {  
                    continue;  
                }  
  
                if (line.contains("fat") || line.contains("fuse") || line  
                        .contains("ntfs")) {  
                      
                    String columns[] = line.split(" ");  
                    if (columns != null && columns.length > 1) {  
                        String path = columns[1];  
                        if (path!=null&&!SdList.contains(path)&&path.toLowerCase().contains("sd"))  
                            SdList.add(columns[1]);  
                    }  
                }  
            }  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        
        if (!TextUtils.isEmpty(secondPath) && !SdList.contains(secondPath)) {
        	SdList.add(secondPath);
        }
  
        if (!SdList.contains(firstPath)) {  
            SdList.add(firstPath);  
        }  
        
        return SdList;  
    }  
}  
