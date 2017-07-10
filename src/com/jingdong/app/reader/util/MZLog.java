package com.jingdong.app.reader.util;

import com.jingdong.app.reader.application.MZBookApplication;

import android.util.Log;

public class MZLog {

	
    public static int d(String tag, String msg) {
        if (MZBookApplication.DEBUG) {
            return Log.d(tag, msg+getFunctionName());
        } else {
            return 0;
        }
    }
    public static int i(String tag, String msg) {
        if (MZBookApplication.DEBUG) {
            return Log.d(tag, msg+getFunctionName());
        } else {
            return 0;
        }
    }
    
    public static int e(String tag, String msg) {
        if (MZBookApplication.DEBUG) {
            return Log.e(tag, msg+getFunctionName());
        } else {
            return 0;
        }
    }
    /**
     * 方便日志定位
     * add by wangguodong 
     * @param tag 标签
     * @return
     */
    private static String getFunctionName()  
    {  
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();  
        if(sts == null)  
        {  
            return null;  
        }  
        for(StackTraceElement st : sts)  
        {  
            if(st.isNativeMethod())  
            {  
                continue;  
            }  
            if(st.getClassName().equals(Thread.class.getName()))  
            {  
                continue;  
            }  
            if(st.getClassName().equals(MZLog.class.getName()))  
            {  
                continue;  
            }  
            return "[" + Thread.currentThread().getName() + ": "  
                    + st.getFileName() + ":" + st.getLineNumber() + " "  
                    + st.getMethodName() + " ]";  
        }  
        return null;  
    }  

}
