package com.jingdong.app.reader.activity;

import com.jingdong.app.reader.preloader.CutBitmapDisplayer;
import com.jingdong.app.reader.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

/**
 * 全局控制的常量和变量，还有方法
 * 
 * @author liqiang
 *
 */
public class GlobalVarable {
	
	
	/**
	 * =============== 如果书籍的样式影响了页面显示，请修改level数值 ===============
	 */
	public static final String BOOK_STYLE_LEVEL = "level2h";

	/**
	 * =============== 拇指阅读包名 ===============
	 */
	public static final String MZBOOK_PACKAGENAME = "com.jingdong.app.reader";
	/**
	 * =============== Notification Action ===============
	 */
	public final static String NOTIFICATION_ACTION_EXIT_APP = "com.mzbook.notification.action.exitApp";
	
	/**
	 * =============== Request Code ===============
	 */
	public static final int REQUEST_CODE_GOTO_FIRST_PAGE = 1;
	public static final int REQUEST_CODE_RECOMMEND = 2;
	public static final int REQUEST_CODE_FOLLOWED_USER_BOOK = 12;;
	
	public final static String BOOK_COVER_SIZE_1X = "!w75h113";
	public final static String BOOK_COVER_SIZE_1X5 = "!w100h150";
	public final static String BOOK_COVER_SIZE_2X = "!w150h226";
	public final static String USER_AVATAR_SIZE_1X = "!crop50";
	public final static String USER_AVATAR_SIZE_1X5 = "!crop100";
	public final static String USER_AVATAR_SIZE_2X = "!crop200";
	
	
	
	public final static String BANNER_IMAGE_SIZE_2X = "!w730h250";
	
	public final static String BOOKS_SELECTED_BANNER_IMAGE_SIZE_2X = "!w540";//精选图片
	
	public static String avatarSize = USER_AVATAR_SIZE_1X5;//头像
	
	public static String bookCoverSize = BOOK_COVER_SIZE_2X;//书封面
	
	public static String bannerImageSize = "";//banner广告
	
	public static String BOOK_STORE_PUBLISHER_IMAGE = "!w100h100";//书城出版方图片尺寸w200h200 w100h100 w50h50
	
	//1432635821000l
	public static long LOAD_BOOK_COVER_BY_NEW_WAY_TIME = 1434124800000l;
	
	public static void resetGlobalVarable(Context context) {
		DisplayMetrics metric = context.getResources().getDisplayMetrics();
		switch (metric.densityDpi) {
		case DisplayMetrics.DENSITY_LOW: {
			avatarSize = USER_AVATAR_SIZE_1X;
			bookCoverSize = BOOK_COVER_SIZE_1X;
			bannerImageSize = BANNER_IMAGE_SIZE_2X;
			break;
		}
		case DisplayMetrics.DENSITY_MEDIUM: {
			avatarSize = USER_AVATAR_SIZE_1X;
			bookCoverSize = BOOK_COVER_SIZE_1X;
			bannerImageSize = BANNER_IMAGE_SIZE_2X;
			break;
		}
		case DisplayMetrics.DENSITY_HIGH: {
			avatarSize = USER_AVATAR_SIZE_1X5;
			bookCoverSize = BOOK_COVER_SIZE_1X5;
			bannerImageSize = BANNER_IMAGE_SIZE_2X;
			break;
		}
		case DisplayMetrics.DENSITY_XHIGH: {
			avatarSize = USER_AVATAR_SIZE_1X5;
			bookCoverSize = BOOK_COVER_SIZE_2X;
			bannerImageSize = BANNER_IMAGE_SIZE_2X;
			break;
		}
		case DisplayMetrics.DENSITY_XXHIGH: {
			avatarSize = USER_AVATAR_SIZE_2X;
			bookCoverSize = BOOK_COVER_SIZE_2X;
			bannerImageSize = "";
			break;
		}
		default: {
			avatarSize = USER_AVATAR_SIZE_2X;
			bookCoverSize = BOOK_COVER_SIZE_2X;
			bannerImageSize = "";
			break;
		}
		}
	}


    /**
     * 获得书籍的图片显示选项
     * @return
     */
    public static DisplayImageOptions getDefaultBookDisplayOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.bg_default_cover) // 加载时的图片
        .showImageForEmptyUri(R.drawable.bg_default_cover) // uri空的时候图片
        .showImageOnFail(R.drawable.bg_default_cover) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).displayer(new SimpleBitmapDisplayer()) // 默认配置
        
        .build();
        return options;
    }
    
    
    /**
     * 获得切边的图片显示选项,并回收
     * 
     * @return
     */
    public static DisplayImageOptions getCutBookDisplayOptions() {
    	return getCutBookDisplayOptions(true);
    }
    
    /**
     * 获得切边的图片显示选项
     * @param recycle 是否回收原bitmap
     * @return
     */
    public static DisplayImageOptions getCutBookDisplayOptions(boolean recycle) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showImageOnLoading(R.drawable.bg_default_cover) // 加载时的图片
        .showImageForEmptyUri(R.drawable.bg_default_cover) // uri空的时候图片
        .showImageOnFail(R.drawable.bg_default_cover) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
        .displayer(new CutBitmapDisplayer(recycle))
        .build();
        return options;
    }
    
    /**
     * 购物车切边图片
     * @param recycle
     * @return
     */
    public static DisplayImageOptions getCutShopCartDisplayOptions(boolean recycle) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showImageOnLoading(R.drawable.bg_default_cover) // 加载时的图片
        .showImageForEmptyUri(R.drawable.ebook_default_icon) // uri空的时候图片
        .showImageOnFail(R.drawable.ebook_default_icon) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
        .displayer(new CutBitmapDisplayer(recycle))
        .build();
        return options;
    }
    
    public static DisplayImageOptions getCutBookDisplayOptions2(boolean recycle) {
        DisplayImageOptions options = new DisplayImageOptions.Builder() 
        		.showImageOnLoading(R.drawable.bg_default_cover) // 加载时的图片
        .showImageForEmptyUri(R.drawable.bg_default_cover) // uri空的时候图片
        .showImageOnFail(R.drawable.bg_default_cover) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(1).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
        .displayer(new SimpleBitmapDisplayer())
        .build();
        return options;
    }
    
    
    /**
     * 获得社区图片显示选项
     * @param recycle 是否回收原bitmap
     * @return
     */
    public static DisplayImageOptions getDefaultCommunityDisplayOptions(boolean recycle) {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showImageOnLoading(R.drawable.ebook_default_icon) // 加载时的图片
        .showImageForEmptyUri(R.drawable.ebook_default_icon) // uri空的时候图片
        .showImageOnFail(R.drawable.ebook_default_icon) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
        .displayer(new CutBitmapDisplayer(recycle))
        .build();
        return options;
    }
    
    
    /**
     * 获得头像的图片显示选项
     * @return
     */
    public static DisplayImageOptions getDefaultAvatarDisplayOptions(boolean isFemale) {

        int defaultResId=0;
        if (isFemale)
            defaultResId = R.drawable.defaultavatar_small;
        else
            defaultResId = R.drawable.defaultavatar_small;
        
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(defaultResId) // 加载时的图片
        .showImageForEmptyUri(defaultResId) // uri空的时候图片
        .showImageOnFail(defaultResId) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_INT).displayer(new SimpleBitmapDisplayer()) // 默认配置
        .build();
        return options;
    }
    
    
    
    
    /**
     * 获得出版社的图片显示选项
     * @return
     */
    public static DisplayImageOptions getDefaultPublisherDisplayOptions() {

        
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showImageOnLoading(R.drawable.icon_publishing_house_default) // 加载时的图片
        .showImageForEmptyUri(R.drawable.icon_publishing_house_default) // uri空的时候图片
        .showImageOnFail(R.drawable.icon_publishing_house_default) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_INT).displayer(new SimpleBitmapDisplayer()) // 默认配置
        .build();
        return options;
    }

    
    /**
     * 获得切边的封面大图显示选项
     * @return
     */
    public static DisplayImageOptions getCutBookBigViewDisplayOptions() {

        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.bg_default_cover) // 加载时的图片
        .showImageForEmptyUri(R.drawable.bg_default_cover) // uri空的时候图片
        .showImageOnFail(R.drawable.bg_default_cover) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
        .displayer(new CutBitmapDisplayer(false))
        .build();
        return options;
    }
}
