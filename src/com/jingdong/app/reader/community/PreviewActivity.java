package com.jingdong.app.reader.community;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.http.Header;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.album.PhotoView;
import com.jingdong.app.reader.album.PhotoViewAttacher.OnViewTapListener;
import com.jingdong.app.reader.application.MZBookApplication;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PreviewActivity extends Activity implements OnClickListener{
	/** 索引显示 */
	private Button mTitle = null;
	/** 保存原始图片 */
	private Button save = null;
	/** 显示原始图片 */
	private Button mOriginalImage = null;
	private ViewPager mViewPager = null;
	/** 图片url列表 */
	private ArrayList<String> urlList = new ArrayList<String>();
	/** 索引位置 */
	private int index = 0;
	/** 下载队列 */
	private Hashtable<String, String> downloadQueue = new Hashtable<String, String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		index = getIntent().getIntExtra("index", 0);
		urlList = getIntent().getStringArrayListExtra("urls");
		
		initView();
	}
	
	private void initView() {
		mTitle = (Button)findViewById(R.id.title);
		save = (Button)findViewById(R.id.save);
		mOriginalImage = (Button)findViewById(R.id.mOriginalImage);
		mViewPager = (ViewPager)findViewById(R.id.mViewPager);
		mViewPager.setAdapter(new ViewPagerAdapter());
		mViewPager.setCurrentItem(index, false);
		
		index++;
		localCheck();
		mTitle.setText(index + "/" + urlList.size());
		initListener();
	}
	
	/**
	* @Description: 检查本地图片文件
	* @author xuhongwei1
	* @date 2015年10月28日 上午11:05:10 
	* @throws 
	*/ 
	private void localCheck() {
		int position = index - 1;
		if(position >= 0 && position < urlList.size()) {
			String url = urlList.get(position);
			if(!TextUtils.isEmpty(url)) {
				String filename = url.substring(url.lastIndexOf("/") + 1);
				String saveimg = Environment.getExternalStorageDirectory() + "/DCIM/Camera/" + File.separator + filename;
				String orgimg = MZBookApplication.getInstance().getCachePath() + "/Community/" + File.separator + filename;
				File file = new File(saveimg);
				if(file.exists()) {
					save.setTextColor(Color.rgb(0x99, 0x99, 0x99));
					save.setText("已保存");
					save.setOnClickListener(null);
				}else {
					save.setTextColor(Color.WHITE);
					save.setText("保存");
					save.setOnClickListener(this);
				}
				
				File org = new File(orgimg);
				if(org.exists()) {
					mOriginalImage.setVisibility(View.GONE);
				}else {
					mOriginalImage.setVisibility(View.VISIBLE);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void initListener() {
		save.setOnClickListener(this);
		mOriginalImage.setOnClickListener(this);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				index = arg0 + 1;
				localCheck();
				mTitle.setText(index + "/" + urlList.size());
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.mOriginalImage:
			saveImage(true);
			break;
		case R.id.save:
			saveImage(false);
			break;
		case R.id.mViewPager:
			finish();
			break;

		default:
			break;
		}
	}
	
	/**
	* @Description: 保存图片或下载原图
	* @param boolean isOrg	true:保存图片到相册	false:下载原图
	* @author xuhongwei1
	* @date 2015年10月28日 上午10:47:01 
	* @throws 
	*/ 
	private void saveImage(boolean isOrg){
		String url = urlList.get(index - 1);
		String filename = url.substring(url.lastIndexOf("/") + 1);
		String tmpPath;
		if(isOrg) {
			tmpPath = "/JDReader/Community/";
		}else {
			tmpPath = "/DCIM/Camera/";
		}
		String path = Environment.getExternalStorageDirectory() + tmpPath;
		File dir = new File(path);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		downloadFile(url, path + File.separator + filename);
	}
	
	class ViewPagerAdapter extends PagerAdapter {
		
		@Override  
	    public Object instantiateItem(ViewGroup container, int position) {
			RelativeLayout layout = new RelativeLayout(PreviewActivity.this);
			PhotoView photoView = new PhotoView(PreviewActivity.this);
			photoView.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
	        container.addView(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); 
	        loadImage(photoView, urlList.get(position));
	        photoView.setTag("image:" + urlList.get(position));
	        photoView.setOnViewTapListener(new OnViewTapListener() {
				@Override
				public void onViewTap(View view, float x, float y) {
					PreviewActivity.this.finish();
				}
			});
	        
	        RelativeLayout.LayoutParams imgparams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	        imgparams.addRule(RelativeLayout.CENTER_IN_PARENT);
	        layout.addView(photoView, imgparams);
	        
	        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	        params.addRule(RelativeLayout.CENTER_IN_PARENT);
	        TextView pro = new TextView(PreviewActivity.this);
	        pro.setTextColor(Color.WHITE);
	        pro.setTextSize(20);
	        pro.setVisibility(View.GONE);
	        pro.setTag(urlList.get(position));
	        layout.addView(pro, params);
	        
	        return layout;  
	    }
		
		@Override  
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}
		
		@Override
		public int getCount() {
			return urlList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;  
		} 
		
	} 
	
	/**
	* @Description: 加载显示图片
	* @param PhotoView image 图片显示控件
	* @param String url 图片访问地址
	* @author xuhongwei1
	* @date 2015年10月28日 上午11:06:45 
	* @throws 
	*/ 
	public void loadImage(PhotoView image, String url) {
		String filename = url.substring(url.lastIndexOf("/") + 1);
		String path = MZBookApplication.getInstance().getCachePath() + "/Community/" + filename;
		File file = new File(path);
		if(file.exists()) {
			ImageLoader.getInstance().displayImage("file://" + path, image, getCutBookDisplayOptions(true));
		}else {
			String url_60 = url + "!q60.jpg";
			ImageLoader.getInstance().displayImage(url_60, image, getCutBookDisplayOptions(false));
		}
	}

	public DisplayImageOptions getCutBookDisplayOptions(boolean org) {
		Config bitmapConfig;
		if(org) {
			bitmapConfig = Bitmap.Config.ARGB_8888;
		}else {
			bitmapConfig = Bitmap.Config.RGB_565;
		}
		
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showImageOnLoading(new ColorDrawable(Color.rgb(0x00, 0x00, 0x00))) 
        .showImageForEmptyUri(new ColorDrawable(Color.rgb(0x00, 0x00, 0x00))) 
        .showImageOnFail(new ColorDrawable(Color.rgb(0x00, 0x00, 0x00))) 
        .resetViewBeforeLoading(false)
        .delayBeforeLoading(10).bitmapConfig(bitmapConfig)
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .considerExifParams(false)
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
        .build();
        return options;
    }
	
	/**
	* @Description: 下载图片
	* @param String url 远程图片地址
	* @param String filename 保存图片本地地址
	* @author xuhongwei1
	* @date 2015年10月28日 上午11:00:14 
	* @throws 
	*/ 
	private void downloadFile(final String url, final String filename) {  
		if(downloadQueue.contains(url)) {
			return;
		}

		downloadQueue.put(filename, url);
		AsyncHttpClient client = new AsyncHttpClient(); 
		String[] allowedContentTypes = new String[] { "image/png", "image/jpeg" };  
		client.get(url, new BinaryHttpResponseHandler(allowedContentTypes) {  
		    @Override  
		    public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {  
		    	downloadQueue.remove(filename);
		        Bitmap bmp = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);  
		        File file = new File(filename);  
		        try {  
		        	if(!filename.contains("Community")) {
		            	Toast.makeText(PreviewActivity.this, "保存成功", Toast.LENGTH_LONG).show();
		            }else {
		            	TextView pro = (TextView)mViewPager.findViewWithTag(url);
				        if(null != pro) {
				        	pro.setVisibility(View.GONE);
				        }	
				        
				        PhotoView photoView = (PhotoView)mViewPager.findViewWithTag("image:" + url);
				        if(null != photoView) {
				        	loadImage(photoView, url);
//				        	photoView.setImageBitmap(bmp);
				        }
		            }
		            // 若存在则删除  
		            if (file.exists())  
		                file.delete();  
		            file.createNewFile();  
		            OutputStream stream = new FileOutputStream(file);  
		            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);  
		            stream.close();  
		            bmp.recycle();
		            localCheck();
		        } catch (IOException e) {  
		            e.printStackTrace();  
		        }  
		  
		    }  
		  
		    @Override  
		    public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {  
		        Toast.makeText(PreviewActivity.this, "保存失败", Toast.LENGTH_LONG).show();  
		    }  
		  
		    @Override  
		    public void onProgress(int bytesWritten, int totalSize) {  
		        super.onProgress(bytesWritten, totalSize); 
		        // 下载进度显示  
		        int count = (int) ((bytesWritten * 1.0 / totalSize) * 100);  
		        if(filename.contains("Community")) {
		        	TextView pro = (TextView)mViewPager.findViewWithTag(url);
			        if(null != pro) {
			        	pro.setVisibility(View.VISIBLE);
			        	pro.setText(count + "%");
			        }	
		        }
		    }  
		  
		    @Override  
		    public void onRetry(int retryNo) {  
		        super.onRetry(retryNo);  
		        // 返回重试次数  
		    }
		  
		}); 
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_community_dongtaiyulan));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_community_dongtaiyulan));
	}
	
}
