package com.jingdong.app.reader.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.jingdong.app.reader.config.Configuration;

import android.text.TextUtils;
import android.util.Log;

public class MD5Calculator
{
    private static MessageDigest _digest;
    
    public static String calculateMD5(String s)
    {
        try
        {
        	if (TextUtils.isEmpty(s)) {
        		 return "";
			}
            return calculateMD5(s.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        return "";
    }
    
    public synchronized static String calculateMD5(byte[] input)
    {
        try
        {
            _digest = MessageDigest.getInstance("MD5");
            _digest.reset();
            _digest.update(input);
            byte[] hash = _digest.digest();
            return StringToolBox.getHexString(hash);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        return "";
    }
    
    public static String getKey(long bookId){
    	if (Configuration.getBooleanProperty(Configuration.TEST_MODE) &&
    			!Configuration.getProperty(Configuration.HOST).contains("gw.e.360buy.com")) {
			// 测试环境
//    		Log.i("MD5Calculator", "test key:"+ Configuration.getProperty(Configuration.HOST));
			return MD5Util.md5Hex(bookId + "ebook!@#$%^*()admin");
		} else {
			// 线上环境
			Log.i("MD5Calculator", "release key");
			return MD5Calculator
				.calculateMD5(bookId
						+ "D45A448A7D952F1F88CCE5EBE551FE9AA6FF322A21210B0D;E40D832CAFF2C90C95685C28630EFEAC3DC8EC05B4A83EE26F5B18252B6CEE09A0CAD88C91A774E1D3197F7C5D91BDBF343FAD801CF08E4B5C651264A01520DD");
		}
    }
}
