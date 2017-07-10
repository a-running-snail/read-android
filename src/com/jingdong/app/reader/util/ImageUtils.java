package com.jingdong.app.reader.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.io.StoragePath;

import android.R.color;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

public class ImageUtils {

	public static final float RADIO_CROP_WIDTH = 0.72f;

	public static final int HEIGHT_REFLECTION = 125;

	public static final int MAX_SINGLE_IMAGE_PIX = (600 * 1024) / 2;
	private static final float startAlpha = 105;
	private static final float offset = startAlpha / HEIGHT_REFLECTION;

	/** 水平方向模糊度 */
	private static float hRadius = 10;
	/** 竖直方向模糊度 */
	private static float vRadius = 10;
	/** 模糊迭代度 */
	private static int iterations = 7;
	
    private static final int IO_BUFFER_SIZE = 2 * 1024;

	/** 
     * 图片效果叠加 
     * @param bmp 限制了尺寸大小的Bitmap 
     * @return 
     */  
    public static Drawable overlay(Bitmap bmp,Bitmap overlay)  
    {  
    	if(null == bmp || null == overlay) {
    		return null;
    	}
    	
        long start = System.currentTimeMillis();  
        int width = bmp.getWidth();  
        int height = bmp.getHeight();  
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);  
          
        // 对边框图片进行缩放  
        int w = overlay.getWidth();  
        int h = overlay.getHeight();  
        
        float scaleX = width * 1F / w;  
        float scaleY = height * 1F / h;  
        Matrix matrix = new Matrix();  
        matrix.postScale(scaleX, scaleY);  
          
        Bitmap overlayCopy = Bitmap.createBitmap(overlay, 0, 0, w, h, matrix, true);  
          
        int pixColor = 0;  
        int layColor = 0;  
          
        int pixR = 0;  
        int pixG = 0;  
        int pixB = 0;  
        int pixA = 0;  
          
        int newR = 0;  
        int newG = 0;  
        int newB = 0;  
        int newA = 0;  
          
        int layR = 0;  
        int layG = 0;  
        int layB = 0;  
        int layA = 0;  
          
        final float alpha = 0.5F;  
          
        int[] srcPixels = new int[width * height];  
        int[] layPixels = new int[width * height];  
        bmp.getPixels(srcPixels, 0, width, 0, 0, width, height);  
        overlayCopy.getPixels(layPixels, 0, width, 0, 0, width, height);  
        int pos = 0;  
        for (int i = 0; i < height; i++)  
        {  
            for (int k = 0; k < width; k++)  
            {  
                pos = i * width + k;  
                pixColor = srcPixels[pos];  
                layColor = layPixels[pos];  
                  
                pixR = Color.red(pixColor);  
                pixG = Color.green(pixColor);  
                pixB = Color.blue(pixColor);  
                pixA = Color.alpha(pixColor);  
                  
                layR = Color.red(layColor);  
                layG = Color.green(layColor);  
                layB = Color.blue(layColor);  
                layA = Color.alpha(layColor);  
                  
                newR = (int) (pixR * alpha + layR * (1 - alpha));  
                newG = (int) (pixG * alpha + layG * (1 - alpha));  
                newB = (int) (pixB * alpha + layB * (1 - alpha));  
                layA = (int) (pixA * alpha + layA * (1 - alpha));  
                  
                newR = Math.min(255, Math.max(0, newR));  
                newG = Math.min(255, Math.max(0, newG));  
                newB = Math.min(255, Math.max(0, newB));  
                newA = Math.min(255, Math.max(0, layA));  
                  
                srcPixels[pos] = Color.argb(newA, newR, newG, newB);  
            }  
        }  
        bitmap.setPixels(srcPixels, 0, width, 0, 0, width, height);  
        
        Drawable drawable = new BitmapDrawable(bitmap);
        srcPixels=null;
        layPixels=null;
        matrix=null;
        
        return drawable;  
    }  
    
    
  //获取图片缩小的图片
    public static Bitmap scaleBitmap(String src,int max)
    {
        //获取图片的高和宽
        BitmapFactory.Options options = new BitmapFactory.Options();
        //这一个设置使 BitmapFactory.decodeFile获得的图片是空的,但是会将图片信息写到options中
        options.inJustDecodeBounds = true;        
        BitmapFactory.decodeFile(src, options); 
       // 计算比例 为了提高精度,本来是要640 这里缩为64
        max=max/10;
        int be = options.outWidth / max;
         if(be%10 !=0)
          be+=10;
         be=be/10;
         if (be <= 0)
          be = 1;
        options.inSampleSize = be;
        //设置可以获取数据
        options.inJustDecodeBounds = false;
        //获取图片
        return BitmapFactory.decodeFile(src, options);        
    }
    /**
     *  加水印
     * @param src
     * @param watermark
     * @return
     */
    public static Bitmap watermarkBitmap(Bitmap src, Bitmap watermark) {
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight(); 
        Bitmap newb= Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src    
        //加入图片
        if (watermark != null) {
            int ww = watermark.getWidth();
            int wh = watermark.getHeight();
            cv.drawBitmap(watermark, w - ww + 5, h - wh +5, null);// 在src的右下角画入水印            
        }
        cv.save(Canvas.ALL_SAVE_FLAG);// 保存
        cv.restore();// 存储
        return newb;
    }
	
	// 获取高斯模糊后的图片

	public static Bitmap BoxBlurFilter(Bitmap bmp) {
		if (null == bmp) {
			return null;
		}
		
		int width = bmp.getWidth()/5;
		int height = bmp.getHeight()/5;
		Bitmap bitmap = ImageTool.getScaleImage(bmp, width,
				height, true, false, false);
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		Bitmap bitmap2 = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		bitmap.getPixels(inPixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < iterations; i++) {
			blur(inPixels, outPixels, width, height, hRadius);
			blur(outPixels, inPixels, height, width, vRadius);
		}
		blurFractional(inPixels, outPixels, width, height, hRadius);
		blurFractional(outPixels, inPixels, height, width, vRadius);
		bitmap2.setPixels(inPixels, 0, width, 0, 0, width, height);
		//Drawable drawable = new BitmapDrawable(bitmap);
		return bitmap2;
	}

	//高斯模糊核心算法
	public static void blur(int[] in, int[] out, int width, int height,
			float radius) {
		int widthMinus1 = width - 1;
		int r = (int) radius;
		int tableSize = 2 * r + 1;
		int divide[] = new int[256 * tableSize];

		for (int i = 0; i < 256 * tableSize; i++)
			divide[i] = i / tableSize;

		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -r; i <= r; i++) {
				int rgb = in[inIndex + clamp(i, 0, width - 1)];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}

			for (int x = 0; x < width; x++) {
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
						| (divide[tg] << 8) | divide[tb];

				int i1 = x + r + 1;
				if (i1 > widthMinus1)
					i1 = widthMinus1;
				int i2 = x - r;
				if (i2 < 0)
					i2 = 0;
				int rgb1 = in[inIndex + i1];
				int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}
	//高斯模糊核心算法
	public static void blurFractional(int[] in, int[] out, int width,
			int height, float radius) {
		radius -= (int) radius;
		float f = 1.0f / (1 + 2 * radius);
		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;

			out[outIndex] = in[0];
			outIndex += height;
			for (int x = 1; x < width - 1; x++) {
				int i = inIndex + x;
				int rgb1 = in[i - 1];
				int rgb2 = in[i];
				int rgb3 = in[i + 1];

				int a1 = (rgb1 >> 24) & 0xff;
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;
				int a2 = (rgb2 >> 24) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;
				int a3 = (rgb3 >> 24) & 0xff;
				int r3 = (rgb3 >> 16) & 0xff;
				int g3 = (rgb3 >> 8) & 0xff;
				int b3 = rgb3 & 0xff;
				a1 = a2 + (int) ((a1 + a3) * radius);
				r1 = r2 + (int) ((r1 + r3) * radius);
				g1 = g2 + (int) ((g1 + g3) * radius);
				b1 = b2 + (int) ((b1 + b3) * radius);
				a1 *= f;
				r1 *= f;
				g1 *= f;
				b1 *= f;
				out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
				outIndex += height;
			}
			out[outIndex] = in[width - 1];
			inIndex += width;
		}
	}
	//高斯模糊核心算法
	public static int clamp(int x, int a, int b) {
		return (x < a) ? a : (x > b) ? b : x;
	}

	// 保存图片
	public static void saveBitmap(Context context, Bitmap bitmap, String name)
			throws IOException {
		File f = new File(StoragePath.getImageDir(context).getAbsolutePath()
				+ File.separator + name + ".png");
		f.createNewFile();
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 获得image目录下的splash图片
	public static Bitmap getSplashBitmap(Context context, String name,int width, int height) {
		String namePath = StoragePath.getImageDir(context) + File.separator+ name + ".png";
		return getBitmapFromNamePath(namePath, width, height,Bitmap.Config.RGB_565);
	}

	// 获得image目录下的头像图片
	public static Bitmap getUserPhotoBitmap(Context context, int width,
			int height) {
		String namePath = StoragePath.getImageDir(context) + File.separator
				+ "photo_preview" + ".png";
		return getBitmapFromNamePath(namePath, width, height,
				Bitmap.Config.RGB_565);
	}

	public static Bitmap getBitmapFromResource(Context context, int id,
			int width, int height) {
		return getBitmapFromResource(context, id, width, height,
				Bitmap.Config.RGB_565);
	}

	public static Bitmap getBitmapFromNamePath(String namePath, int width,
			int height) {
		return getBitmapFromNamePath(namePath, width, height,
				Bitmap.Config.RGB_565);
	}

	public static Bitmap getBitmapFromFile(File file, int width, int height) {
		return getBitmapFromFile(file, width, height, Bitmap.Config.RGB_565);
	}

	public static Bitmap getBitmapFromStream(Uri uri,
			ContentResolver contentResolver, int width, int height) {
		return getBitmapFromStream(uri, contentResolver, width, height,
				Bitmap.Config.RGB_565);
	}

	public static Bitmap getMiniBitmap(byte[] data, int w, int h) {
		if (data == null || data.length == 0) {
			return null;
		}

		Bitmap result = null;
		int width = w;
		// int heigt = h;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		// BitmapFactory.decodeResource(res, mImageIds[position],
		// options);
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		options.inJustDecodeBounds = false;
		// pre-calculate the sample using filmstrip height
		int bitmapSample = (options.outHeight / width);
		if (bitmapSample == 0)
			bitmapSample = 1;
		boolean isExtraWidth = (((options.outWidth * options.outHeight) / (bitmapSample * bitmapSample)) > ImageUtils.MAX_SINGLE_IMAGE_PIX/*
																																		 * ITEM_WIDTH
																																		 * *
																																		 * ITEM_HEIGHT
																																		 */);

		options.inSampleSize = isExtraWidth ?
		/**
		 * determine weather the bitmap is still too large if we only consider
		 * the ratio between original height and film strip height.
		 */
		((int) Math.ceil(Math.sqrt((options.outWidth * options.outHeight)
				/ (double) ImageUtils.MAX_SINGLE_IMAGE_PIX/*
														 * ITEM_WIDTH *
														 * ITEM_HEIGHT
														 */))) : bitmapSample;
		options.inDither = false;
		result = BitmapFactory.decodeByteArray(data, 0, data.length, options);

		if (result == null || result.isRecycled()) {
			return null;
		}

		return result;
	}

	/**
	 * 由于Android对图片使用内存有限制，若是加载几兆的大图片便内存溢出。 Bitmap会将图片的所有像素（即长x宽）加载到内存中，如果图片分辨率过
	 * 大，会直接导致内存溢出（java.lang.OutOfMemoryError），只有在
	 * BitmapFactory加载图片时使用BitmapFactory.Options对相关参数进 行配置来减少加载的像素。
	 * 
	 * Android的Bitmap.Config给出了bitmap的一个像素所对应的存储方式，
	 * 有RGB_565，ARGB_8888，ARGB_4444，ALPHA_8四种。RGB_565表示的
	 * 是红绿蓝三色分别用5,6,5个比特来存储，一个像素占用了5+6+5=16个比
	 * 特。RGB_8888表示红绿蓝和半透明分别用8,8,8,8个比特来存储，一个像
	 * 素占用了8+8+8+8=32个比特。这样的话如果图片是以RGB_8888读入的， 那么占用内存的大小将是RGB_565读入方式的2倍。
	 * 
	 * @param context
	 * @param id
	 *            资源id
	 * @param width
	 *            期望image宽度像素
	 * @param height
	 *            期望image高度像素
	 * @param bitmapConfig
	 *            配置图片的像素存储方式
	 * @return
	 */
	public static Bitmap getBitmapFromResource(Context context, int id,
			int width, int height, Bitmap.Config bitmapConfig) {
		BitmapFactory.Options opts = null;
		if (width > 0 && height > 0) {
			opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(context.getResources(), id, opts);
			// 计算图片缩放比例
			final int minSideLength = Math.min(width, height);
			opts.inSampleSize = computeSampleSize(opts, minSideLength, width
					* height);
			opts.inPreferredConfig = bitmapConfig;
			opts.inJustDecodeBounds = false;
			opts.inInputShareable = true;
			opts.inPurgeable = true;
			return BitmapFactory.decodeResource(context.getResources(), id,
					opts);
		}
		return null;
	}

	/**
	 * 由于Android对图片使用内存有限制，若是加载几兆的大图片便内存溢出。 Bitmap会将图片的所有像素（即长x宽）加载到内存中，如果图片分辨率过
	 * 大，会直接导致内存溢出（java.lang.OutOfMemoryError），只有在
	 * BitmapFactory加载图片时使用BitmapFactory.Options对相关参数进 行配置来减少加载的像素。
	 * 
	 * Android的Bitmap.Config给出了bitmap的一个像素所对应的存储方式，
	 * 有RGB_565，ARGB_8888，ARGB_4444，ALPHA_8四种。RGB_565表示的
	 * 是红绿蓝三色分别用5,6,5个比特来存储，一个像素占用了5+6+5=16个比
	 * 特。RGB_8888表示红绿蓝和半透明分别用8,8,8,8个比特来存储，一个像
	 * 素占用了8+8+8+8=32个比特。这样的话如果图片是以RGB_8888读入的， 那么占用内存的大小将是RGB_565读入方式的2倍。
	 * 
	 * @param file
	 * @param width
	 *            期望image宽度像素
	 * @param height
	 *            期望image高度像素
	 * @param bitmapConfig
	 *            配置图片的像素存储方式
	 * @return
	 */
	public static Bitmap getBitmapFromFile(File file, int width, int height,
			Bitmap.Config bitmapConfig) {
		if (file != null && file.exists()) {
			BitmapFactory.Options opts = null;
			if (width > 0 && height > 0) {
				opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(file.getPath(), opts);
				// 计算图片缩放比例
				final int minSideLength = Math.min(width, height);
				opts.inSampleSize = computeSampleSize(opts, minSideLength,
						width * height);
				opts.inPreferredConfig = bitmapConfig;
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
				return BitmapFactory.decodeFile(file.getPath(), opts);
			}
		}
		return null;
	}

	public static Bitmap CropForExtraWidth(Bitmap source) {
		Bitmap newBmp = null;
		try {
			if (source == null) {
				return source;
			}
			float ratio = (float) source.getWidth()
					/ (float) source.getHeight();
			if (ratio <= RADIO_CROP_WIDTH) {
				return source;
			}
			float leftCropRatio = (ratio - RADIO_CROP_WIDTH) / 2.0f;
			// float rightCropRatio = leftCropRatio;
			int targetWidth = source.getWidth();
			int x = 0;
			targetWidth = (int) (RADIO_CROP_WIDTH * source.getHeight());
			x = (int) (leftCropRatio * source.getHeight());
			newBmp = Bitmap.createBitmap(source, x, 0, targetWidth,
					source.getHeight());
			if (!source.equals(newBmp)) {
				source.recycle();
			}
		} catch (OutOfMemoryError e) {
		}
		return newBmp;
	}

	public static Bitmap getBitmapFromNamePath(String namePath, int width,int height, Bitmap.Config bitmapConfig) {
		if (!UiStaticMethod.isNullString(namePath)) {
			BitmapFactory.Options opts = null;
			if (width > 0 && height > 0) {
				opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(namePath, opts);
				// 计算图片缩放比例
				final int minSideLength = Math.min(width, height);
				opts.inSampleSize = computeSampleSize(opts, minSideLength,width * height);
				opts.inPreferredConfig = bitmapConfig;
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
				return BitmapFactory.decodeFile(namePath, opts);
			}
		}
		return null;
	}

	public static Bitmap getBitmapFromStream(Uri uri,
			ContentResolver contentResolver, int width, int height,
			Bitmap.Config bitmapConfig) {
		try {
			BitmapFactory.Options opts = null;
			if (width > 0 && height > 0) {
				opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(
						contentResolver.openInputStream(uri), null, opts);
				// 计算图片缩放比例
				final int minSideLength = Math.min(width, height);
				opts.inSampleSize = computeSampleSize(opts, minSideLength,
						width * height);
				opts.inPreferredConfig = bitmapConfig;
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
				return BitmapFactory.decodeStream(
						contentResolver.openInputStream(uri), null, opts);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 计算图片采样大小，这段代码是从网上扒下来的。
	 * 参见网址http://orgcent.com/android-outofmemoryerror-load-big-image/
	 * 
	 * @param options
	 *            BitmapFactory的构建参数，该工参数中已经包含图片原始大小
	 * @param minSideLength
	 *            所需要的图片宽高度较小的那个
	 * @param maxNumOfPixels
	 *            所需要的图片最大像素
	 * @return 采样率
	 */
	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	/**
	 * 计算初始采样率 参见网址http://orgcent.com/android-outofmemoryerror-load-big-image/
	 * 
	 * @param options
	 *            BitmapFactory的构建参数，该工参数中已经包含图片原始大小
	 * @param minSideLength
	 *            所需要的图片宽高度较小的那个
	 * @param maxNumOfPixels
	 *            所需要的图片最大像素
	 * @return 采样率
	 */
	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * 
	 * @param drawable
	 *            drawable图片
	 * @param roundPx
	 *            角度
	 * @return
	 * @Description:// 获得圆角图片的方法
	 */

	public static Bitmap getRoundedCornerBitmap(Drawable drawable, float roundPx) {
		Bitmap bitmap = drawableToBitmap(drawable);
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
	 * 
	 * @param drawable
	 * @return
	 * @Description:将Drawable转化为Bitmap
	 */

	public static Bitmap drawableToBitmap(Drawable drawable) {

		int width = drawable.getIntrinsicWidth();

		int height = drawable.getIntrinsicHeight();

		Bitmap bitmap = Bitmap.createBitmap(width, height,

		drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

		: Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(bitmap);

		drawable.setBounds(0, 0, width, height);

		drawable.draw(canvas);

		return bitmap;

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

			options.inSampleSize =1;//computeSampleSize(options, height, width);
			
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
			// e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getImage(InputStream is) {
		try {
			int height = DisplayUtil.getHeight();
			int width = DisplayUtil.getWidth();
			return getBitpMap(MZBookApplication.getInstance(), is, height,
					width);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getImage(String imagePath) {
		try {
			InputStream inputStream = StreamToolBox
					.loadStreamFromFile(imagePath);
			inputStream = StreamToolBox.flushInputStream(inputStream);
			return getImage(inputStream);
		} catch (Exception e) {
		}
		return null;
	}

	public static Bitmap getScaleImage(Bitmap bitmap, int resizedWidth,
			int resizedHeight, boolean isenlarge, boolean isScale,
			boolean isFill) {

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
		Log.i("ImageTool", "width==" + width);
		Log.i("ImageTool", "height==" + height);
		Log.i("ImageTool", "resizedWidth==" + resizedWidth);
		Log.i("ImageTool", "resizedHeight==" + resizedHeight);
		Log.i("ImageTool", "scaleWidth0==" + scaleWidth);
		Log.i("ImageTool", "scaleHeight0==" + scaleHeight);
		if (!isenlarge) {
			if (isScale) {
				scaleWidth = scaleWidth < scaleHeight ? scaleWidth
						: scaleHeight;
				scaleHeight = scaleWidth;
			}
		} else {
			if (isScale) {
				scaleWidth = scaleWidth > scaleHeight ? scaleWidth
						: scaleHeight;
				scaleHeight = scaleWidth;
			}
		}
		Log.i("ImageTool", "scaleWidth1==" + scaleWidth);
		Log.i("ImageTool", "scaleHeight1==" + scaleHeight);

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
		if (isFill) {
			int ShearWidth = 0;
			int ShearHeight = 0;
			if (resizedBitmap.getWidth() > resizedWidth) {
				ShearWidth = resizedBitmap.getWidth() - resizedWidth;
			}
			if (resizedBitmap.getHeight() > resizedHeight) {
				ShearHeight = resizedBitmap.getHeight() - resizedHeight;
			}
			Bitmap tempBmp = Bitmap.createBitmap(resizedBitmap, ShearWidth / 2,
					ShearHeight / 2, resizedBitmap.getWidth() - ShearWidth,
					resizedBitmap.getHeight() - ShearHeight);
			resizedBitmap.recycle();
			resizedBitmap = tempBmp;
			Log.i("ImageTool", "scaleWidth2==" + resizedBitmap.getWidth());
			Log.i("ImageTool", "scaleHeight3==" + resizedBitmap.getHeight());
		}
		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return resizedBitmap;
	}

	/**
	 * 获得倒影
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectedShad(Bitmap originalImage,
			int shadHeight) {
		// The gap we want between the reflection and the original image
		// final int reflectionGap = 4;

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		// This will not scale but will flip on the Y axis
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		// Create a Bitmap with the flip matrix applied to it.
		// We only want the bottom half of the image
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
				height / 2, width, height / 2, matrix, false);

		// Create a new bitmap with same width but taller to fit reflection
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, shadHeight,
				Config.ARGB_8888);

		// Create a new Canvas with the bitmap that's big enough for
		// the image plus gap plus reflection
		Canvas canvas = new Canvas(bitmapWithReflection);
		// Draw in the original image
		// canvas.drawBitmap(originalImage, 0, 0, null);
		// Draw in the gap
		Paint defaultPaint = new Paint();
		defaultPaint.setColor(color.transparent);
		canvas.drawRect(0, 0, width, shadHeight, defaultPaint);
		// Draw in the reflection
		canvas.drawBitmap(reflectionImage, 0, 0, null);

		// Create a shader that is a linear gradient that covers the reflection
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, 0, 0, shadHeight,
				0x70ffffff, 0x00ffffff, TileMode.CLAMP);
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
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);
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

	public static boolean saveFile(Bitmap bm, String filePath,
			Bitmap.CompressFormat format) {
		try {
			File myCaptureFile = new File(filePath);
			if (!myCaptureFile.exists()) {
				myCaptureFile.createNewFile();
				Log.i("zhoubo", "myCaptureFile.createNewFile();");
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(myCaptureFile));
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
	 * 
	 * @param bm
	 * @param fileName
	 * @throws IOException
	 */
	public static boolean saveFile(Bitmap bm, String filePath) {
		try {
			File myCaptureFile = new File(filePath);
			if (!myCaptureFile.exists()) {
				myCaptureFile.createNewFile();
				Log.i("zhoubo", "myCaptureFile.createNewFile();");
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(myCaptureFile));
			bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();
			bos.close();
			return true;
		} catch (Exception e) {
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
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
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
	 * 
	 * @param width
	 *            ，需要平铺的宽度
	 * @param height
	 *            ，需要平铺的高度
	 * @param src
	 *            ，小图片。
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
	 * @param urlpath
	 * @return Bitmap 根据图片url获取图片对象
	 */
	public static Bitmap getBitmapByUrl(String urlpath) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedInputStream(new URL(urlpath).openStream(), IO_BUFFER_SIZE);
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
			byte[] b = new byte[IO_BUFFER_SIZE];
			int read;
			while ((read = in.read(b)) != -1) {
				out.write(b, 0, read);
			}
			out.flush();
			byte[] data = dataStream.toByteArray();
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			data = null;
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 100, output);
		int options = 100;
		while (output.toByteArray().length / 1024 > 32) { // 循环判断如果压缩后图片是否大于32kb,大于继续压缩  	//微信分享缩略图不能超过32K
			output.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			bmp.compress(Bitmap.CompressFormat.JPEG, options, output);// 这里压缩options%，把压缩后的数据存放到baos中
		}

		if (needRecycle) {
			bmp.recycle();
		}
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
