package com.jingdong.app.reader.preloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.mzbook.sortview.optimized.ImageSizeUtils;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

/*
 * 
 * 提前加载书架书籍的图片到内存
 * 
 */
public class BitmapLruCachePreloader {

    private UsingFreqLimitedMemoryCache mCache;
    private List<Map<String, String>>   urls = new ArrayList<Map<String, String>>();
    private int                         maxWidth;
    private int                         maxHeight;
    private ImageSizeUtils              utils;

    public BitmapLruCachePreloader(Context context) {
        initialize(context);

        for (int i = 0; i < urls.size(); i++){
            if (!urls.get(i).get("type").equals("infolder")) {
                maxWidth = utils.getPerItemImageWidth();//按列数平分后每列宽度
                maxHeight = utils.getPerItemImageHeight();//按列数平分后每列高度
            } else {
                maxWidth = utils.getPerSubGridItemImageWidth();//计算后每个文件夹中书的封面宽度
                maxHeight = utils.getPerSubGridItemImageHeight();//计算后每个文件夹中书的封面高度
            }

            MZLog.d("wangguodong", "maxWidth=" + maxWidth + " maxHeight=" + maxHeight);

            String key = MemoryCacheUtils.generateKey(urls.get(i).get("url"), new ImageSize(maxWidth, maxHeight));
            // 判断缓存中是否存在图片了
            if (null != mCache.get(key)) {
                MZLog.d("wangguodong", "本地网络图片已经缓存");
                continue;
            } else {
                // 缓存中未发现图片 通过volley获取图片
                updateCache(context, key, urls.get(i).get("url"));
                MZLog.d("wangguodong", "未发现网络图片缓存 key=" + key + " url=" + urls.get(i));
            }
        }
    }

    /**
     * 初始化
     * @param mContext
     */
    private void initialize(Context mContext) {
        mCache = MZBookApplication.getMemoryCache();
        utils = new ImageSizeUtils(mContext);
        // 获取书架书籍 需要提前缓存的网络图片url
        urls = MZBookDatabase.instance.getCacheUrls(mContext, LoginUser.getpin());
    }

    private void updateCache(Context context, final String key, String url) {

    	//请求图片
        WebRequestHelper.get(url, new FileAsyncHttpResponseHandler(context) {

            @Override
            public void onSuccess(int arg0, Header[] arg1, File arg2) {

                FileInputStream iStream;
                try {
                    iStream = new FileInputStream(arg2);
                    Bitmap mapBitmap = BitmapFactory.decodeStream(iStream);
                    if(mCache!=null&&mapBitmap!=null)
                    		mCache.put(key, mapBitmap);
                    MZLog.d("wangguodong", "cache add success!");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int arg0, Header[] arg1, Throwable arg2,
                    File arg3) {
                MZLog.d("wangguodong", "cache add error!");
            }
        });

    }

}
