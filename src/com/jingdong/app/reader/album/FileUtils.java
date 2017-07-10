package com.jingdong.app.reader.album;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jingdong.app.reader.user.LoginUser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class FileUtils {
	
	/**
	* @Description: 获取文件大小
	* @author xuhongwei1
	* @date 2015年10月23日 下午2:59:46 
	* @throws 
	*/
	public static long getFileSize(String filename) {
		long size = 0;
		try {
			File file = new File(filename);
			if (file.exists()) {
				FileInputStream fis = null;
				fis = new FileInputStream(file);
				size = fis.available();
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return size;
	}
	
	/**
	* @Description: 拷贝文件
	* @param oldFileName 源文件
	* @param newFileName 新生成文件
	* @author xuhongwei1
	* @date 2015年10月26日 上午9:15:34 
	* @throws 
	*/ 
	public static void copyFile(String oldFileName, String newFileName) {
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int byteread = 0;
			File oldfile = new File(oldFileName);
			if (oldfile.exists()) { // 文件存在时
				inStream = new FileInputStream(oldFileName); // 读入原文件
				fs = new FileOutputStream(newFileName);
				byte[] buffer = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
			}else {
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != inStream) {
					inStream.close();
				}
				if (null != fs) {
					fs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	* @Description: 压缩图片文件
	* @param String filename 文件名
	* @return boolean true:成功 false:失败
	* @author xuhongwei1
	* @date 2015年10月28日 下午10:02:57 
	* @throws 
	*/ 
	public static boolean compressImage(String filename) {
		File file = new File(filename);
		if(!file.exists()) {
			return false;
		}
		
		String tmpfile = Environment .getExternalStorageDirectory()+"/JDReader/DraftsBox/"+File.separator+ LoginUser.getpin() +File.separator + "temp.jpg";
		try{
			Bitmap bmp = BitmapFactory.decodeFile(filename);
			if(null == bmp) {
				return false;
			}
			
			FileOutputStream out = new FileOutputStream(tmpfile);
			if(null == out) {
				return false;
			}
			
			bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
			bmp.recycle();
			
			File f = new File(tmpfile);
			if(f.exists()) {
				if(file.delete()) {
					f.renameTo(file);
				}
			}
			
			return true;
		}catch(OutOfMemoryError e) {
		}catch (FileNotFoundException e) {
		}catch (IOException e) {
		}
		
		return false;
	}

}
