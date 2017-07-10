package com.jingdong.app.reader.album;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.album.ImageAsyncTask.ICallBack;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class AlbumAdapter extends ArrayAdapter<ImageData>{
	private LayoutInflater inflater = null;
	/** 相册数据列表 */
	private List<ImageData> mDataList = null;
	private DisplayMetrics dm;
	private Context mContext = null;
	/** 保存图片实际宽度 */
	private int imgWidth;
	/** 图片缓存cache */
	private LruCache<String, Bitmap> mLruCache = null;
	private GridView mGridView = null;

	public AlbumAdapter(Context context, List<ImageData> _mDataList, GridView mGridView) {
		super(context, 0, _mDataList);
		initCache();
		this.mGridView = mGridView;
		this.mContext = context;
		this.inflater = LayoutInflater.from(context);
		this.mDataList = new ArrayList<ImageData>();
		this.mDataList.addAll(_mDataList);
		dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		imgWidth = (int)((dm.widthPixels - 5 * dipToPx(6))/4);
	}
	
	@Override
	public int getCount() {
		return mDataList.size() + 1;
	}
	
	/**
	* @Description: 初始化图片缓存
	* @author xuhongwei1
	* @date 2015年10月23日 下午2:13:47 
	* @throws 
	*/ 
	private void initCache() {
		int maxSize = (int)(Runtime.getRuntime().maxMemory()/8);
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(null == convertView) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.album_item, parent, false);
			holder.image = (ImageView)convertView.findViewById(R.id.image);
			holder.choiceIcon = (ImageView)convertView.findViewById(R.id.choiceIcon);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(imgWidth, imgWidth);
			holder.image.setLayoutParams(params);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ImageData data = null;
		if(0 == position) {
			holder.choiceIcon.setVisibility(View.GONE);
			holder.image.setScaleType(ScaleType.CENTER_INSIDE);
			holder.image.setImageResource(R.drawable.album_camera_icon);
		}else {
			data = getItem(position - 1);
			holder.image.setTag(data.imagePath);
			holder.image.setScaleType(ScaleType.CENTER_CROP);
			holder.choiceIcon.setVisibility(View.VISIBLE);
			if(data.isSelected) {
				holder.choiceIcon.setImageResource(R.drawable.album_select_icon);
			}else {
				holder.choiceIcon.setImageResource(R.drawable.album_normal_icon);
			}
			loadImage(holder.image, data);
		}
		
		holder.choiceIcon.setOnClickListener(new onClickListener(false, position, data));
		convertView.setOnClickListener(new onClickListener(true, position, data));
		return convertView;
	}
	
	/**
	* @Description: 加载本地图片
	* @param @param image 图片显示控件
	* @param @param data 图片数据实体类
	* @author xuhongwei1
	* @date 2015年10月21日 下午10:36:55 
	* @throws 
	*/ 
	private void loadImage(ImageView image, ImageData data) {
		String path = data.imagePath;
		if(TextUtils.isEmpty(path)) {
			return;
		}
		
		image.setImageDrawable(new ColorDrawable(Color.rgb(0xf0, 0xf0, 0xf0)));
		Bitmap mBitmap = getBitmap(path);
		if (null != mBitmap) {
			image.setImageBitmap(mBitmap);
		}else {
			ImageAsyncTask.getBitmapForCache(path, imgWidth, new ICallBack() {
				@Override
				public void SuccessCallback(String url, Bitmap mBitmap) {
					if (null == mBitmap) {
						return;
					}
					
					Bitmap b = getBitmap(url);
					if (null == b) {
						b = mBitmap;
						putBitmap(url, mBitmap);
					}else{
						if (null != mBitmap) {
							if (!mBitmap.isRecycled()) {
								mBitmap.recycle();
								mBitmap = null;
							}
						}
					}
					
					ImageView image = (ImageView)mGridView.findViewWithTag(url);
					if (null != image) {
						image.setImageBitmap(b);
					}
					
				}
			});
		}
			 
	}
	
	static class ViewHolder {
		ImageView image;
		ImageView choiceIcon;
	}
	
	public int dipToPx(int dip) {
		return (int) (dip * dm.density + 0.5f);
	}
	
	class onClickListener implements OnClickListener {
		private int position;
		private ImageData data;
		private boolean preview;
		
		public onClickListener(boolean preview, int position, ImageData data) {
			this.position = position;
			this.data = data;
			this.preview = preview;
		}

		@Override
		public void onClick(View arg0) {
			LinkedHashMap<String, ImageData> selectList = AlbumManager.getInstance().getDataList();
			if(0 == position) {
				if(selectList.size() >= AlbumManager.MAX) {
					Toast.makeText(mContext, "超出可选图片张数", Toast.LENGTH_SHORT).show();
					return;
				}else {
					AlbumActivity album = (AlbumActivity)mContext;
					album.openCamera();
				}
			}else {
				if(preview) {
					Intent preview = new Intent(mContext, PreviewPhotoActivity.class);
					preview.putExtra("item", data);
					mContext.startActivity(preview);
				}else {
					ImageView choiceIcon = (ImageView)arg0;
					AlbumActivity a = (AlbumActivity)mContext;
					if(data.isSelected) {
						data.isSelected = false;
						choiceIcon.setImageResource(R.drawable.album_normal_icon);
					}else {
						if(selectList.size() >= AlbumManager.MAX) {
							Toast.makeText(mContext, "超出可选图片张数", Toast.LENGTH_SHORT).show();
							return;
						}
						data.isSelected = true;
						choiceIcon.setImageResource(R.drawable.album_select_icon);
					}
					a.updateSelectList(data);
				}
			}
		}
		
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
