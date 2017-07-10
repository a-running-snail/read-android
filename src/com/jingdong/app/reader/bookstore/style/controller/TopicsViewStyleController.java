package com.jingdong.app.reader.bookstore.style.controller;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.BookStorePaperBookActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity.ModuleLinkChildList;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 
 * 书城顶部专题部分view，可以自由定义
 * 
 * @author WANGGUODONG
 * 
 */
public class TopicsViewStyleController {

	private static final int DEFAULT_TOPICS_ROW = 2;
	private static final int DEFAULT_TOPICS_COLUMN = 4;
	private static final int DEFAULT_LEFT_MARGIN = 16; // 整个View左边距
	private static final int DEFAULT_RIGHT_MARGIN = 16; // 整个View右边距
	private static final int DEFAULT_BOTTOM_MARGIN = 0; // 整个View底部边距 可以与行底边距抵消
	private static final int DEFAULT_TOP_MARGIN = 0; // 整个View顶部边距
	private static final int SUB_DEFAULT_BOTTOM_MARGIN = 0; // 行View底部边距
	private static final int DEFAULT_IMAGE_WIDTH = 48; // 默认图片的宽度
	private static final int DEFAULT_IMAGE_HEIGHT = 48; // 默认图片的高度
	private static final int DEFAULT_VIEW_BACKGROUND = 0xffffffff; // 默认VIEW背景颜色,默认透明

	/**
	 * Item Image 点击回调
	 * 
	 * @author WANGGUODONG
	 * 
	 */
	public interface OnItemImageClickListener {
		public void onItemImageClick(int position);
	}


	/**
	 * 获取默认的TopicsStyleView,显示指定行指定列
	 * 
	 * @param context
	 * @param data
	 *            所有按顺序排列的url数据
	 * @return 布局
	 */

	public static LinearLayout getTopicsStyleView(final Context context,
			int row/*行数*/, int column/*列数*/,
			final List<ModuleLinkChildList> moduleLinkChildList,
			final OnItemImageClickListener listener) {

		if (moduleLinkChildList == null || moduleLinkChildList.size() == 0)
			return null;

		ImageLoadingListenerImpl loadingListenerImpl = new ImageLoadingListenerImpl();
		//屏幕宽
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		//圆圈的大小
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);
		int sub_bottom_margin = ScreenUtils.dip2px(context,SUB_DEFAULT_BOTTOM_MARGIN);
		int image_width = ScreenUtils.dip2px(context, DEFAULT_IMAGE_WIDTH);
		int image_height = ScreenUtils.dip2px(context, DEFAULT_IMAGE_HEIGHT);
		int perItemWidth = (int) ((screenWidth - left_margin - right_margin) / column);

		LinearLayout verticalLayout = new LinearLayout(context);
		verticalLayout.setBackgroundColor(DEFAULT_VIEW_BACKGROUND);
		verticalLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		verticalLayout.setLayoutParams(params);
		verticalLayout.setPadding(left_margin, top_margin, right_margin, bottom_margin);

		//循环行
		for (int k = 0; k < row; k++) {
			//判断是都需要增加行，Math.ceil取整
			boolean isNeedAddRow = k < ((int) Math.ceil(moduleLinkChildList.size() / (column * 1.0f))) ? true : false;
			if (isNeedAddRow) {
				//增加行
				LinearLayout horizontaLayout = new LinearLayout(context);
				horizontaLayout.setOrientation(LinearLayout.HORIZONTAL);
				LayoutParams horizontalParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				//居中
				horizontalParams.gravity = Gravity.CENTER;
				horizontalParams.bottomMargin = sub_bottom_margin;
				horizontaLayout.setLayoutParams(horizontalParams);

				for (int i = 0; i < column; i++) {

					final int position = k * column + i;
					if (position <= moduleLinkChildList.size() - 1) {
						//专题模块
						LinearLayout topicItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.bookstore_style_topic_item,null);
						//专题标签点击事件处理
						topicItem.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								TalkingDataUtil.onBookStoreEvent(context, "首页", moduleLinkChildList.get(position).showName);
								if (moduleLinkChildList.get(position).rtype == 2) {// 借阅
									Intent intent = new Intent(context,BookStoreBookListActivity.class);
									intent.putExtra(
											BookStoreBookListActivity.LIST_TYPE,
											BookStoreBookListActivity.TYPE_BORROWING);
									intent.putExtra("bannerImg",moduleLinkChildList.get(position).moduleBookChild.picAddressAll);
									intent.putExtra(
											"showName",
											moduleLinkChildList.get(position).showName);
									intent.putExtra("relateLink", moduleLinkChildList.get(position).relateLink);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent);
									
								} else if (moduleLinkChildList.get(position).rtype == 3) {// 推荐
									Intent intent = new Intent(context,BookStoreBookListActivity.class);
									intent.putExtra(
											BookStoreBookListActivity.LIST_TYPE,
											BookStoreBookListActivity.TYPE_RECOMMEND);
									intent.putExtra("showName",moduleLinkChildList.get(position).showName);
									intent.putExtra("relateLink", moduleLinkChildList.get(position).relateLink);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent);
								} else if (moduleLinkChildList.get(position).rtype == 4) {//纸书商城
									Intent intent = new Intent(context,BookStorePaperBookActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent);
								} 
								else if (moduleLinkChildList.get(position).rtype == 1) {//畅读专区
									Intent intent = new Intent(context,WebViewActivity.class);
									String webUrl = moduleLinkChildList.get(position).relateLink;
									intent.putExtra(WebViewActivity.UrlKey,webUrl);
									intent.putExtra(WebViewActivity.TopbarKey, true);
									intent.putExtra(WebViewActivity.BrowserKey, false);
									intent.putExtra(WebViewActivity.TitleKey,moduleLinkChildList.get(position).showName);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent);
								}
								else  {
									//专题列表
									Intent intent = new Intent(context,BookStoreBookListActivity.class);
									intent.putExtra("fid",moduleLinkChildList.get(position).moduleBookChild.id);
									intent.putExtra("ftype", 2);
									intent.putExtra("relationType", 1);
									intent.putExtra("showName",moduleLinkChildList.get(position).showName);
									intent.putExtra("relateLink", moduleLinkChildList.get(position).relateLink);
									intent.putExtra("bannerImg",moduleLinkChildList.get(position).moduleBookChild.picAddressAll);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent);
								}
							}
						});

						LayoutParams topicParams = new LinearLayout.LayoutParams(perItemWidth, LayoutParams.WRAP_CONTENT);
						topicItem.setLayoutParams(topicParams);

						//圆圈图片
						RoundNetworkImageView icon = (RoundNetworkImageView) topicItem.findViewById(R.id.topic_image);
						//专题模块-标题
						TextView title = (TextView) topicItem.findViewById(R.id.title);
						LayoutParams iconParams = new LinearLayout.LayoutParams(image_width, image_height);
						iconParams.gravity = Gravity.CENTER;
						icon.setLayoutParams(iconParams);
						if (listener != null)
							icon.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									listener.onItemImageClick(position);
								}
							});
						ImageLoader.getInstance().displayImage(
								moduleLinkChildList.get(position).picAddressAll, icon,
								GlobalVarable.getDefaultPublisherDisplayOptions(),loadingListenerImpl);
						//设置标题
						title.setText(moduleLinkChildList.get(position).showName);
						horizontaLayout.addView(topicItem);
					}
				}
				verticalLayout.addView(horizontaLayout);
			}

		}
		return verticalLayout;

	}

	/**
	 * 完全自定义，显示指定行指定列，可以指定边距 背景等
	 * 
	 * @param 边距单位均为dp
	 * @param data
	 *            所有按顺序排列的url数据
	 * @return 布局
	 */

	public static LinearLayout getTopicsStyleView(Context context, int row,
			int column, int leftMargin, int rightMargin, int bottomMargin,
			int topMargin, int subBottomMargin, int imageWidth,
			int imageHeight, int backgroundColor, List<String> data,
			final OnItemImageClickListener listener) {

		if (data == null || data.size() == 0)
			return null;
		ImageLoadingListenerImpl loadingListenerImpl = new ImageLoadingListenerImpl();
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		int left_margin = ScreenUtils.dip2px(context, leftMargin);
		int right_margin = ScreenUtils.dip2px(context, rightMargin);
		int top_margin = ScreenUtils.dip2px(context, topMargin);
		int bottom_margin = ScreenUtils.dip2px(context, bottomMargin);
		int sub_bottom_margin = ScreenUtils.dip2px(context, subBottomMargin);
		int image_width = ScreenUtils.dip2px(context, imageWidth);
		int image_height = ScreenUtils.dip2px(context, imageHeight);
		int perItemWidth = (int) ((screenWidth - left_margin - right_margin) / column);

		LinearLayout verticalLayout = new LinearLayout(context);
		verticalLayout.setBackgroundColor(backgroundColor);

		verticalLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.topMargin = top_margin;
		params.bottomMargin = bottom_margin;

		verticalLayout.setLayoutParams(params);

		for (int k = 0; k < row; k++) {

			boolean isNeedAddRow = k < ((int) Math.ceil(data.size()
					/ (column * 1.0f))) ? true : false;
			if (isNeedAddRow) {
				LinearLayout horizontaLayout = new LinearLayout(context);
				horizontaLayout.setOrientation(LinearLayout.HORIZONTAL);
				LayoutParams horizontalParams = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				horizontalParams.gravity = Gravity.CENTER;
				horizontalParams.bottomMargin = sub_bottom_margin;
				horizontaLayout.setLayoutParams(horizontalParams);

				for (int i = 0; i < column; i++) {

					final int position = k * column + i;
					if (position <= data.size() - 1) {
						LinearLayout topicItem = (LinearLayout) LayoutInflater
								.from(context).inflate(
										R.layout.bookstore_style_topic_item,
										null);
						LayoutParams topicParams = new LinearLayout.LayoutParams(
								perItemWidth, LayoutParams.WRAP_CONTENT);
						topicItem.setLayoutParams(topicParams);

						// 图片布局
						RoundNetworkImageView icon = (RoundNetworkImageView) topicItem
								.findViewById(R.id.topic_image);
						LayoutParams iconParams = new LinearLayout.LayoutParams(
								image_width, image_height);
						iconParams.gravity = Gravity.CENTER;
						icon.setLayoutParams(iconParams);
						if (listener != null)
							icon.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									listener.onItemImageClick(position);
								}
							});

						ImageLoader.getInstance().displayImage(
								data.get(position),
								icon,
								GlobalVarable
										.getDefaultPublisherDisplayOptions(),
										loadingListenerImpl);
						horizontaLayout.addView(topicItem);
					}
				}
				verticalLayout.addView(horizontaLayout);
			}

		}
		return verticalLayout;

	}
	
	/**
	 * 图片加载器
	 */
	public static class ImageLoadingListenerImpl extends SimpleImageLoadingListener {
		public static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
		/**
		 * 下载图片完毕
		 */
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
