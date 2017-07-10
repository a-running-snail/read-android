package com.jingdong.app.reader.album;

import java.util.List;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class PreviewPhotoAdapter extends PagerAdapter{
	private Context mContext;
	/** 照片数据列表 */
	private List<ImageData> mDataList;
	/** 图片缓存cache */
	private LruCache<String, Bitmap> mLruCache = null;
	/** 保存图片实际宽度 */
	private int imgWidth;
	private DisplayMetrics dm;
	
	public PreviewPhotoAdapter(Context context, List<ImageData> _mDataList) {
		this.mContext = context;
		this.mDataList = _mDataList;
		dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		imgWidth = dm.widthPixels;
		initCache();
	}
	
	/**
	* @Description: 初始化图片缓存
	* @author xuhongwei1
	* @date 2015年10月23日 下午2:13:47 
	* @throws 
	*/ 
	private void initCache() {
		int maxSize = (int)(Runtime.getRuntime().maxMemory()/5);
		mLruCache = new LruCache<String, Bitmap>(maxSize){  
		    @Override  
		    protected int sizeOf(String key, Bitmap bitmap) {  
		    	if (bitmap == null) {
		    		return 0;
		    	}
		    	return bitmap.getRowBytes() * bitmap.getHeight();
		    }  
		};  
	}
	
	public Bitmap getBitmap(String filename) {
		return mLruCache.get(filename);
	}
	
	public void putBitmap(String filename, Bitmap mBitmap) {
		mLruCache.put(filename, mBitmap);
	}
	
	@Override  
    public Object instantiateItem(ViewGroup container, int position) {
		ImageData data = mDataList.get(position);
		PhotoView photoView = new PhotoView(mContext);
		photoView.setBackgroundColor(Color.rgb(0xf0, 0xf0, 0xf0));
        container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); 
        photoView.setTag(data.imagePath);
        loadImage(photoView, data);
        return photoView;  
    } 
	
	/**
	* @Description: 加载本地图片
	* @param @param image 图片显示控件
	* @param @param data 图片数据实体类
	* @author xuhongwei1
	* @date 2015年10月21日 下午10:36:55 
	* @throws 
	*/ 
	private void loadImage(PhotoView image, ImageData data) {
		String path = data.imagePath;
		ImageLoader.getInstance().displayImage("file://"+path, image, getCutBookDisplayOptions(path));
	}
	
	public DisplayImageOptions getCutBookDisplayOptions(String pathName) {
		BitmapFactory.Options decodingOptions = new BitmapFactory.Options();
		decodingOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, decodingOptions);
		if(decodingOptions.outWidth > imgWidth) {
			decodingOptions.inSampleSize = (int)(decodingOptions.outWidth/imgWidth);
		}
		decodingOptions.inJustDecodeBounds = false;
		
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showImageOnLoading(new ColorDrawable(Color.rgb(0xf0, 0xf0, 0xf0))) // 加载时的图片
        .showImageForEmptyUri(new ColorDrawable(Color.rgb(0xf0, 0xf0, 0xf0))) // uri空的时候图片
        .showImageOnFail(new ColorDrawable(Color.rgb(0xf0, 0xf0, 0xf0))) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
        .decodingOptions(decodingOptions)
        .build();
        return options;
    }

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override  
    public boolean isViewFromObject(View arg0, Object arg1) {  
        return arg0 == arg1;  
    } 
	
	 @Override  
     public void destroyItem(ViewGroup container, int position, Object object) {
		 PhotoView view = (PhotoView)object;
		 container.removeView(view);
		 String path = (String)view.getTag();
		 ImageLoader.getInstance().getMemoryCache().remove(path);
	 }
	 
	/**
	* @Description: 释放图片资源
	* @author xuhongwei1
	* @date 2015年10月23日 下午2:24:00 
	* @throws 
	*/ 
	public void destory() {
		 mLruCache.evictAll();
	}

}
