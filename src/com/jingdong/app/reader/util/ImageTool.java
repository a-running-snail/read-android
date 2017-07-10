package com.jingdong.app.reader.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.R.color;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;

public class ImageTool {
	
	/**
	 * 图像缓存器，用来装载图片
	 * 
	 * @key 为图片的URL
	 * @value bitmap SoftReference
	 */
	private static HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();


	// 这个函数会对图片的大小进行判断，并得到合适的缩放比例，比如2即1/2,3即1/3
	static int computeSampleSize(BitmapFactory.Options options, int height,
			int width) {
		int w = options.outWidth;
		int h = options.outHeight;
		int candidate = (w * h) / (height * width);
		if (((w * h) % (height * width)) > 0) {
			candidate++;
		}
		if (candidate == 0)
			return 1;

		if (true);
//			Log.i("ImageTool", "for w/h " + w + "/" + h + " returning "
//					+ candidate + "(" + (w / candidate) + " / "
//					+ (h / candidate));
		return candidate;
	}

	public static Bitmap getBitpMap(Context context, InputStream is,
			int height, int width) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			// 先指定原始大小
			options.inSampleSize = 1;
			// 只进行大小判断
			options.inJustDecodeBounds = true;
			// 调用此方法得到options得到图片的大小
			is.mark(0);
			BitmapFactory.decodeStream(is, null, options);
			// 我们的目标是在800pixel的画面上显示。
			// 所以需要调用computeSampleSize得到图片缩放的比例

			options.inSampleSize = computeSampleSize(options, height, width);
			// OK,我们得到了缩放的比例，现在开始正式读入BitMap数据
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			is.reset();
			// 根据options参数，减少所需要的内存
			Bitmap sourceBitmap = BitmapFactory.decodeStream(is, null, options);
			return sourceBitmap;
		} catch (OutOfMemoryError error) {
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getImageFromStream(Context context, String imagePath,
			int height, int width) {
		InputStream is;
		try {
			is = StreamToolBox.loadStreamFromFile(imagePath);
			is = StreamToolBox.flushInputStream(is);
			return getBitpMap(context, is, height, width);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getImage(InputStream is) {
		try {
			int height = DisplayUtil.getHeight();
			int width = DisplayUtil.getWidth();
			return getBitpMap(MZBookApplication.getInstance(), is, height, width);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}


	public static Bitmap getImage(String imagePath) {
		try {
			InputStream inputStream = StreamToolBox.loadStreamFromFile(imagePath);
			inputStream = StreamToolBox.flushInputStream(inputStream);
 		       return getImage(inputStream);
		} catch (Exception e) {
		}
		return null;
	}

	public static Bitmap getScaleImage(Bitmap bitmap, int resizedWidth,
			int resizedHeight, boolean isenlarge, boolean isScale,boolean isFill) {

		Bitmap BitmapOrg = bitmap;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();

		int newWidth = resizedWidth;
		int newHeight = resizedHeight;

		if (!isenlarge) {
			if (newWidth > width && newHeight > height) {
				return bitmap;
			}
		}

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		if (!isenlarge) {
			if (isScale) {
				scaleWidth = scaleWidth < scaleHeight ? scaleWidth
						: scaleHeight;
				scaleHeight = scaleWidth;
			}
		} else {
			if (isScale) {
				scaleWidth = scaleWidth > scaleHeight ?scaleWidth
						: scaleHeight;
				scaleHeight = scaleWidth;
			}
		}

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
		if(isFill){
			int ShearWidth = 0;
			int ShearHeight = 0;
			if(resizedBitmap.getWidth()>resizedWidth){
				ShearWidth = resizedBitmap.getWidth() - resizedWidth;
			}
			if(resizedBitmap.getHeight()>resizedHeight){
				 ShearHeight = resizedBitmap.getHeight() - resizedHeight;
			}
			Bitmap tempBmp = Bitmap.createBitmap(resizedBitmap, ShearWidth/2, ShearHeight/2, resizedBitmap.getWidth()-ShearWidth, resizedBitmap.getHeight()-ShearHeight);
			resizedBitmap.recycle();
			resizedBitmap = tempBmp;
		}
		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return resizedBitmap;
	}

    /**
     * 获得带倒影的图片方法 封装
     * 
     * @param bitmap
     * @return
     */ 
	public static Bitmap getReflectedImage(Bitmap originalImage, int reflectionGap) {
		Bitmap getImage = null;
		try {
			getImage = createReflectedImage(originalImage, reflectionGap);

		} catch (Exception e) {
			e.printStackTrace();
			getImage = getFailedBitmap();
		}
		return getImage;
	}
    /**
     * 获得带倒影的图片方法1
     * 
     * @param bitmap
     * @return
     */ 
	public static Bitmap createReflectedImage(Bitmap originalImage, int reflectionGap) {
        // The gap we want between the reflection and the original image
        //final int reflectionGap = 4;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // This will not scale but will flip on the Y axis
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        // Create a Bitmap with the flip matrix applied to it.
        // We only want the bottom half of the image
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
        		 height/2, width,  height/2, matrix, false);

        // Create a new bitmap with same width but taller to fit reflection
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                (height +(int)DisplayUtil.dip2px(30)), Config.ARGB_8888);

        // Create a new Canvas with the bitmap that's big enough for
        // the image plus gap plus reflection
        Canvas canvas = new Canvas(bitmapWithReflection);
        // Draw in the original image
        canvas.drawBitmap(originalImage, 0, 0, null);
        // Draw in the gap
        Paint defaultPaint = new Paint();
        defaultPaint.setColor(color.transparent);
        canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
        // Draw in the reflection
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        // Create a shader that is a linear gradient that covers the reflection
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0,
                originalImage.getHeight(), 0, bitmapWithReflection.getHeight()
                        + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		// Set the paint to use this shader (linear gradient)
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
    }
	
    /**
     *获得倒影
     * 
     * @param bitmap
     * @return
     */ 
	public static Bitmap createReflectedShad(Bitmap originalImage,int shadHeight) {
        // The gap we want between the reflection and the original image
        //final int reflectionGap = 4;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // This will not scale but will flip on the Y axis
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        // Create a Bitmap with the flip matrix applied to it.
        // We only want the bottom half of the image
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
        		 height/2, width,  height/2, matrix, false);

        // Create a new bitmap with same width but taller to fit reflection
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,shadHeight, Config.ARGB_8888);

        // Create a new Canvas with the bitmap that's big enough for
        // the image plus gap plus reflection
        Canvas canvas = new Canvas(bitmapWithReflection);
        // Draw in the original image
        //canvas.drawBitmap(originalImage, 0, 0, null);
        // Draw in the gap
        Paint defaultPaint = new Paint();
        defaultPaint.setColor(color.transparent);
        canvas.drawRect(0, 0, width, shadHeight, defaultPaint);
        // Draw in the reflection
        canvas.drawBitmap(reflectionImage, 0,0, null);

        // Create a shader that is a linear gradient that covers the reflection
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0,0, 0,shadHeight, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		// Set the paint to use this shader (linear gradient)
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, 0, width, shadHeight, paint);

		return bitmapWithReflection;
    }
    /**
     * 3.获得带倒影的图片方法 原始数据
     * 
     * @param bitmap
     * @return
     */ 
    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) { 
        final int reflectionGap = 4; 
        int width = bitmap.getWidth(); 
        int height = bitmap.getHeight(); 
        Matrix matrix = new Matrix(); 
        matrix.preScale(1, -1); 
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, 
                width, height / 2, matrix, false); 
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, 
                (height + height / 2), Config.ARGB_8888); 
        Canvas canvas = new Canvas(bitmapWithReflection); 
        canvas.drawBitmap(bitmap, 0, 0, null); 
        Paint deafalutPaint = new Paint(); 
        canvas 
                .drawRect(0, height, width, height + reflectionGap, 
                        deafalutPaint); 
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null); 
        Paint paint = new Paint(); 
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, 
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 
                0x00ffffff, TileMode.CLAMP); 
        paint.setShader(shader); 
        // Set the Transfer mode to be porter duff and destination in 
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
        // Draw a rectangle using the paint with our linear gradient 
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() 
                + reflectionGap, paint); 
        return bitmapWithReflection; 
    } 
    
    public static boolean saveFile(Bitmap bm, String filePath, Bitmap.CompressFormat format) {
        try {
        File myCaptureFile = new File(filePath);   
        if(!myCaptureFile.exists()){
            myCaptureFile.createNewFile();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));   
        bm.compress(format, 80, bos);
            bos.flush();
            bos.close();   
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;        
    }
    /**  
     * 保存文件  
     * @param bm  
     * @param fileName  
     * @throws IOException  
     */  
    public static boolean saveFile(Bitmap bm, String filePath)  {   
        try {
        File myCaptureFile = new File(filePath);   
        if(!myCaptureFile.exists()){
        	myCaptureFile.createNewFile();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));   
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);   
			bos.flush();
	        bos.close();   
		    return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return false;
    }  
    
    
    /**
     * 1.放大缩小图片
     * 
     * @param bitmap
     * @param w
     * @param h
     * @return
     */ 
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) { 
        int width = bitmap.getWidth(); 
        int height = bitmap.getHeight(); 
        Matrix matrix = new Matrix(); 
        float scaleWidht = ((float) w / width); 
        float scaleHeight = ((float) h / height); 
        matrix.postScale(scaleWidht, scaleHeight); 
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, 
                matrix, true); 
        return newbmp; 
    } 
     
    /**
     * 2.获得圆角图片的方法
     * 
     * @param bitmap
     * @param roundPx
     * @return
     */ 
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) { 
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap 
                .getHeight(), Config.ARGB_8888); 
        Canvas canvas = new Canvas(output); 
        final int color = 0xff424242; 
        final Paint paint = new Paint(); 
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
        final RectF rectF = new RectF(rect); 
        paint.setAntiAlias(true); 
        canvas.drawARGB(0, 0, 0, 0); 
        paint.setColor(color); 
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
        canvas.drawBitmap(bitmap, rect, rect, paint); 
        return output; 
    } 
    /**
     * 合成有小图平铺出来的大图。
     * @param width，需要平铺的宽度
     * @param height，需要平铺的高度
     * @param src，小图片。
     * @return 合成后的图片。
     */
	public static Bitmap createRepeater(int width, int height, Bitmap src) {
		int w_count = (width + src.getWidth() - 1) / src.getWidth();
		int h_count = (height + src.getHeight() - 1) / src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		for (int idx = 0; idx < w_count; ++idx) {
			for (int idy = 0; idy < h_count; ++idy) {
				canvas.drawBitmap(src, idx * src.getWidth(),
						idy * src.getHeight(), null);
			}
		}

		return bitmap;
	}
     
    /**
     * 4.将Drawable转化为Bitmap
     * 
     * @param drawable
     * @return
     */ 
    public static Bitmap drawableToBitmap(Drawable drawable) { 
        int width = drawable.getIntrinsicWidth(); 
        int height = drawable.getIntrinsicHeight(); 
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable 
                .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 
                : Bitmap.Config.RGB_565); 
        Canvas canvas = new Canvas(bitmap); 
        drawable.setBounds(0, 0, width, height); 
        drawable.draw(canvas); 
        return bitmap; 
    }
    
	public static Bitmap getFailedBitmap() {
		Bitmap loadBitmap = null;
		String keyBitmap = "key_failed_bitmap";
		if (imageCache.containsKey(keyBitmap)) {
			SoftReference<Bitmap> softReference = imageCache.get(keyBitmap);
			loadBitmap = softReference.get();
			if (loadBitmap != null) {
				return loadBitmap;
			}
		}
		loadBitmap = ((BitmapDrawable) (MZBookApplication.getInstance()
				.getResources().getDrawable(R.drawable.book_e_default)))
				.getBitmap();
		SoftReference<Bitmap> bitmapRef = new SoftReference<Bitmap>(loadBitmap);
		imageCache.put(keyBitmap, bitmapRef);
		return loadBitmap;
	}
}
