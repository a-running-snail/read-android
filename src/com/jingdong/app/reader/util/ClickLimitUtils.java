package com.jingdong.app.reader.util;

public class ClickLimitUtils {
	private static long lastClickTime;
	/**
	* @Description: 防止控件重复点击
	* @param @return
	* @return boolean
	* @author xuhongwei1
	* @date 2015年10月12日 下午5:09:42 
	* @throws 
	*/ 
	public synchronized static boolean isFastClick() {
		long time = System.currentTimeMillis();   
		if(0 == lastClickTime) {
			lastClickTime = time;
			return true;
		}
        
        if ( time - lastClickTime < 500) {   
            return true;   
        }   
        lastClickTime = time;   
        return false;   
	}
}
