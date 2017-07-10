package com.jingdong.app.reader.onlinereading;

import com.jingdong.app.reader.util.DesUtil;


public class OlineDesUtils {
	
	/**
	 * 密码加密 ，安全性较高，取设备信息加密，移动其他设备不能解密
	 * @param str      
	 */
	public  static String encrypt(String str){
		
	     return DesUtil.encrypt(str,OnlineCardManeger.KEY);
	}
	
	/**
	 * 密码解密
	 * @param str
	 * @return
	 */
	public static String decrypt(String str){
	              return DesUtil.decrypt(str,OnlineCardManeger.KEY);
	}

}
