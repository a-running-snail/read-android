package com.jingdong.app.reader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Environment;

/**
 * 记录debug信息，方便某些不好复现的bug获取相关参数
 * @author tanmojie
 *
 */
public class DebugUtils {

	private static DebugUtils instance;  
    private static String path = Environment.getExternalStorageDirectory() + File.separator+ "JDReader"+File.separator+"DebugLog"+File.separator;
      
    public static DebugUtils getInstance() {  
        if (instance == null)  
        	instance = new DebugUtils();  
        return instance;  
    }  
    
    /**
     * 记录debug信息
     * @param tag
     * @param content
     */
    @SuppressWarnings("rawtypes")
	public void writeDebugInfo(String tag ,HashMap<String,String> valueMap){
    	File fileDir = new File(path);
    	if (!fileDir.exists()) {
			fileDir.mkdir();
		}
    	
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = df1.format(new Date(System.currentTimeMillis()));
		String fileName = path + "debug-" + dateStr + ".txt";
		
		String time = df.format(new Date(System.currentTimeMillis()));
		StringBuffer writeContent = new StringBuffer();
		writeContent.append("tag-->"+tag + "\n");
		writeContent.append("time-->"+time + "\n");
		
		if(valueMap.size()>0)
			writeContent.append("content-->");
		Iterator iter = valueMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			writeContent.append("<"+key.toString()+","+val.toString()+">--");
		}
		if(valueMap.size()>0)
			writeContent.append("\n---------------------\r\n");
		
		FileWriter writer = null;  
        try {     
            writer = new FileWriter(fileName, true);     
            writer.write(writeContent.toString());       
        } catch (IOException e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(writer != null){  
                    writer.close();     
                }  
            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        }   
    }

	
}
