package com.jingdong.app.reader.bookstore.style.controller;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.bookstore.fragment.BookStoreRootFragment.onBannerAttachListener;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity.ModuleLinkChildList;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.view.customviewpager.JDViewPager;
import com.jingdong.app.reader.view.customviewpager.RecyclingPagerAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 
 * 书城banner广告
 * 
 * @author WANGGUODONG
 * 
 */
public class BannerViewStyleController implements onBannerAttachListener{

	private static final int DEFAULT_LEFT_MARGIN = 0; 	// 整个View左边距
	private static final int DEFAULT_RIGHT_MARGIN = 0; 	// 整个View右边距
	private static final int DEFAULT_BOTTOM_MARGIN = 0; // 整个View底部边距
	private static final int DEFAULT_TOP_MARGIN = 0; 	// 整个View顶部边距
	private static final int DEFAULT_VIEW_BACKGROUND = 0x00ffffff; // 默认VIEW背景颜色,默认透明

	private static ImageView[] imageViews = null;
	private static ImageView imageView = null;
	private static JDViewPager mAdvPager = null;
	private static AtomicInteger what = new AtomicInteger(0);
	
	private static BannerViewStyleController mInstance;
	private static LinearLayout mContainer;
	private static GuidePageChangeListener changeListener=null;
	private static final long time=4000;
	private BannerViewStyleController() {}
	public static  BannerViewStyleController getInstance() {
		if (mInstance == null) {
			mInstance = new BannerViewStyleController();
		}
		if(changeListener==null){
			changeListener=new GuidePageChangeListener();
		}
		return mInstance;
	}

	public LinearLayout getBannerView(final Context context,
			final List<ModuleLinkChildList> urls) {
		MZLog.d("J.Beyond", "getBannerView");
		if (urls == null || urls.size() == 0)
			return null;
		mContainer = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.bookstore_style_banner, null);
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);

		LinearLayout.LayoutParams coLayoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		coLayoutParams.leftMargin = left_margin;
		coLayoutParams.rightMargin = right_margin;
		coLayoutParams.topMargin = top_margin;
		coLayoutParams.bottomMargin = bottom_margin;
		mContainer.setLayoutParams(coLayoutParams);

		int screenWidth = (int) ScreenUtils.getWidthJust(context);// px
		mAdvPager = (JDViewPager) mContainer.findViewById(R.id.view_pager);
		int image_height = (int) (0.6 * screenWidth);

		LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.height = image_height;
		mAdvPager.setLayoutParams(params);


		ViewGroup group = (ViewGroup) mContainer.findViewById(R.id.view_group);
		if(group.getChildCount()>0){
			group.removeAllViews();
		}
		
		
		//测试banner页  tmj
//		boolean test = false;
//		if(test){
//			ModuleLinkChildList url = urls.get(0);
//			url.showName = "测试";
//			url.relateLink = "http://www.baidu.com";
//			url.relateType = 2;
//			url.advResourceList.get(0).addressAll = "http://img30.360buyimg.com/ebookadmin/jfs/t2041/230/2192822680/78262/9bf5b647/56c166f4Nc9ae6bfd.jpg";
//			urls.add(0, url);
//		}
		
		imageViews = new ImageView[urls.size()];

		// 图片指示器
		for (int i = 0; i < urls.size(); i++) {
			imageView = new ImageView(context);
			imageView.setLayoutParams(new LayoutParams(20, 20));
			imageView.setPadding(5, 5, 5, 5);
			imageViews[i] = imageView;
			if (i == 0) {
				imageViews[i].setImageResource(R.drawable.icon_slideshows_dot_red);
			} else {
				imageViews[i].setImageResource(R.drawable.icon_slideshows_dot_grey);
			}
			group.addView(imageViews[i]);
		}
		
		cycleable = urls.size() == 1 ?false:true;
		mAdvPager.setCycle(cycleable);

		mAdvPager.setAdapter(new AdvAdapter(context,urls).setInfiniteLoop(true));
		mAdvPager.setOnPageChangeListener(changeListener);
		
		mAdvPager.setInterval(time);
//		mAdvPager.setStopScrollWhenTouch(true);
//		mAdvPager.setSlideBorderMode(JDViewPager.SLIDE_BORDER_MODE_CYCLE);
		if (cycleable) {
			mAdvPager.startAutoScroll();
			mAdvPager.setCurrentItem(Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % imageViews.length);
		}
		return mContainer;
	}

	private static void whatOption(int size) {
		what.incrementAndGet();
		if (what.get() > imageViews.length - 1) {
			what.getAndAdd(-size);
		}
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {

		}
	}

	private static final Handler viewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			mAdvPager.setCurrentItem(msg.what);
			super.handleMessage(msg);
		}

	};
	private boolean cycleable;

	private static class GuidePageChangeListener implements
			OnPageChangeListener {

		
		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			int pos = position % imageViews.length;
//			what.getAndSet(pos);
			for (int i = 0; i < imageViews.length; i++) {
				imageViews[pos]
						.setImageResource(R.drawable.icon_slideshows_dot_red);
				if (pos != i) {
					imageViews[i]
							.setImageResource(R.drawable.icon_slideshows_dot_grey);
				}
			}

		}

	}

	private static class AdvAdapter extends RecyclingPagerAdapter {
		private List<ModuleLinkChildList> urls = null;
		private int           size;
		private Context       context;
		private boolean       isInfiniteLoop;
		private ImageLoadingListenerImpl loadingListenerImpl;
		public AdvAdapter(Context context, List<ModuleLinkChildList> urls) {
			this.urls = urls;
			this.context = context;
			this.size = urls.size();
			isInfiniteLoop = false;
			loadingListenerImpl = new ImageLoadingListenerImpl();
		}
		
		/**
	     * get really position
	     * 
	     * @param position
	     * @return
	     */
	    private int getPosition(int position) {
	        return isInfiniteLoop ? position % size : position;
	    }
	    
		@Override
		public View getView(final int position, View view, ViewGroup container) {
			ViewHolder holder = null;
	        if (view == null) {
	            holder = new ViewHolder();
	            view = holder.imageView = new ImageView(context);
	            holder.imageView.setScaleType(ScaleType.FIT_XY);
	            view.setTag(holder);
	        } else {
	            holder = (ViewHolder)view.getTag();
	        }
	        ImageLoader.getInstance().displayImage(
					urls.get(getPosition(position)).advResourceList.get(0).addressAll, holder.imageView,
					GlobalVarable.getDefaultBookDisplayOptions(),loadingListenerImpl);
			holder.imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Talking-Data
//					if (urls.get(getPosition(position)).relateType == 2) {
						// webview
//						String url = urls.get(getPosition(position)).relateLink;
//						if (LoginUser.isLogin()) {
//							String url = "http://e.m.jd.com/lottery.html?source=JdReader";
//							Intent intent = new Intent(context,
//									WebViewActivity.class);
//							intent.putExtra(WebViewActivity.BrowserKey, false);
//							intent.putExtra(WebViewActivity.UrlKey, url);
//							intent.putExtra(WebViewActivity.TitleKey, "一元购抽奖");
//							context.startActivity(intent);
//						}else{
//							Intent intent = new Intent(context,LoginActivity.class);
//							intent.putExtra("NeedCallback", true);
//							context.startActivity(intent);
//						}
//					} else {
//						Intent intent = new Intent(context,
//								BookStoreBookListActivity.class);
//						intent.putExtra("fid", urls.get(getPosition(position)).id);
//						intent.putExtra("ftype", 1);
//						intent.putExtra("relationType", 1);
//						intent.putExtra("showName", urls.get(getPosition(position)).showName);
//						context.startActivity(intent);
//					}
						
						if (urls.get(getPosition(position)).relateType == 2) {
							// webview
							String url = urls.get(getPosition(position)).relateLink;
							//抽奖
							if (url.startsWith("http://e.m.jd.com/lottery.html")) {
								if (LoginUser.isLogin()) {
//									String url = "http://e.m.jd.com/lottery.html?source=JdReader";
									Intent intent = new Intent(context,WebViewActivity.class);
									intent.putExtra(WebViewActivity.BrowserKey, false);
									intent.putExtra(WebViewActivity.UrlKey, url);
									intent.putExtra(WebViewActivity.TitleKey, "一元购抽奖");
									context.startActivity(intent);
								}else{
									Intent intent = new Intent(context,LoginActivity.class);
									intent.putExtra("NeedCallback", true);
									context.startActivity(intent);
								}
							}
							//非抽奖
							else{
								Intent intent = new Intent(context,WebViewActivity.class);
								intent.putExtra(WebViewActivity.UrlKey, url);
								intent.putExtra(WebViewActivity.BrowserKey, false);
								context.startActivity(intent);
							}
						}else {
							Intent intent = new Intent(context,BookStoreBookListActivity.class);
							intent.putExtra("fid", urls.get(getPosition(position)).id);
							intent.putExtra("ftype", 1);
							intent.putExtra("relationType", 1);
							intent.putExtra("showName", urls.get(getPosition(position)).showName);
							context.startActivity(intent);
						}
				}
			});
	        return view;
		}
		@Override
		public int getCount() {
			// Infinite loop
	        return isInfiniteLoop ? Integer.MAX_VALUE : size;
		}
		
		private static class ViewHolder {

			ImageView imageView;
		}

		/**
		 * @return the isInfiniteLoop
		 */
		public boolean isInfiniteLoop() {
			return isInfiniteLoop;
		}

		/**
		 * @param isInfiniteLoop
		 *            the isInfiniteLoop to set
		 */
		public AdvAdapter setInfiniteLoop(boolean isInfiniteLoop) {
			this.isInfiniteLoop = isInfiniteLoop;
			return this;
		}

	}

	@Override
	public void onAttach() {
		Log.d("JD_Reader", "onAttach");
		//启动轮播
		if (mAdvPager != null && cycleable) {
			mAdvPager.startAutoScroll();
		}
	}

	@Override
	public void onDisattach() {
		Log.d("JD_Reader", "onDisattach");
		//停止轮播
		if (mAdvPager != null) {
			mAdvPager.stopAutoScroll();
		}
	}
	
	
	public static class ImageLoadingListenerImpl extends SimpleImageLoadingListener {
		public static final List<String> displayedImages = 
		          Collections.synchronizedList(new LinkedList<String>());
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
			if (bitmap != null) {
				ImageView imageView = (ImageView) view;
				boolean isFirstDisplay = !displayedImages.contains(imageUri);
				if (isFirstDisplay) {
					// 图片的淡入效果
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

}
