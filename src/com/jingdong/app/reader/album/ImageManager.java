package com.jingdong.app.reader.album;

import java.io.File;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageManager {
	
	public static Bitmap getBitmapFromCache(String fileName){ 
		try {
			return BitmapFactory.decodeFile(fileName); 
		} catch (OutOfMemoryError p) {
			gc(p);
		}
		
		return null;
	}
	
	public static Bitmap getBitmapFromCache(String filename, int width){
		try {
			File file = new File(filename);
			if (!file.exists()) {
				return null;
			}
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
	        opts.inJustDecodeBounds = true;
	        BitmapFactory .decodeFile(filename, opts); 
	        int inSampleSize=1;
	        if(opts.outWidth > width){   
	        	inSampleSize = opts.outWidth/width;  
	        
	        	opts.inSampleSize=inSampleSize;
		    	opts.inScaled = false;
		        opts.inJustDecodeBounds = false;
		        return BitmapFactory.decodeFile(filename, opts); 
	        }else{
	        	return BitmapFactory.decodeFile(filename); 
	        }
		} catch (OutOfMemoryError p) {
			gc(p);
		}
		
		return null;
	} 
	
//	public static Bitmap getBitmapFromResource(int id){ 
//		try {
//			return BitmapFactory.decodeResource(GolukApplication.getInstance().getResources(), id);
//		} catch (OutOfMemoryError p) {
//			gc(p);
//		}
//		 
//		return null;
//	}
//	
//	public static Bitmap getBitmapFromResource(int id, int width, int height){
//		try {
//			BitmapFactory.Options opts = new BitmapFactory.Options();
//	        opts.inJustDecodeBounds = true;
//	        BitmapFactory.decodeResource(GolukApplication.getInstance().getResources(), id, opts); 
//	        int inSampleSize=1;
//	        if(opts.outWidth > width){   
//	        	inSampleSize = opts.outWidth/width;  
//	        
//	        	opts.inSampleSize=inSampleSize;
//		    	opts.inScaled = false;
//		        opts.inJustDecodeBounds = false;
//		        return BitmapFactory .decodeResource(GolukApplication.getInstance().getResources(), id, opts); 
//	        }else{
//	        	return BitmapFactory .decodeResource(GolukApplication.getInstance().getResources(), id); 
//	        } 
//		} catch (OutOfMemoryError p) {
//			gc(p);
//		}
//		 
//		return null;
//	}
	
	private static void gc(OutOfMemoryError mOutOfMemoryError) {
		System.gc();
		System.runFinalization();
		mOutOfMemoryError.printStackTrace();
	}
	  	
}
