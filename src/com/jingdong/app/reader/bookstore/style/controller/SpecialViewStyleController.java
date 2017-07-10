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
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 
 * 书城书城专区布局，可以自由定义
 * 
 * @author WANGGUODONG
 * 
 */
public class SpecialViewStyleController {

	private static final int DEFAULT_BOOKS_ROW = 1;
	private static final int DEFAULT_BOOKS_COLUMN = 1;
	private static final int DEFAULT_LEFT_MARGIN = 0; // 整个View左边距
	private static final int DEFAULT_RIGHT_MARGIN = 0; // 整个View右边距
	private static final int DEFAULT_BOTTOM_MARGIN = 0; // 整个View底部边距
	private static final int DEFAULT_TOP_MARGIN = 0; // 整个View顶部边距

	private static final int DEFAULT_FIRST_ROW_TOP_MARGIN = 0; // 第一行离顶部view的距离

	private static final int DEFAULT_HORIZONTAL_DIVIDER_WIDTH = 0; // 书籍中间的水平空隙宽度
	private static final int DEFAULT_VERTICAL_DIVIDER_WIDTH = 0; // 书籍中间的垂直空隙宽度
	private static final int DEFAULT_VIEW_BACKGROUND = 0x00ffffff; // 默认VIEW背景颜色,默认透明

	/**
	 * 获取书城专区布局,显示3行1列
	 * 
	 * @param context
	 * @param data
	 *            所有按顺序排列的url数据
	 * @return 布局
	 */
	public static LinearLayout getSpecialStyleView(final Context context, final List<BookStoreModuleBookListEntity.ModuleBookChild> data) {

		if (data == null || data.size() == 0)
			return null;
		ImageLoadingListenerImpl impl = new ImageLoadingListenerImpl();
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);

		int firstRowTopMargin = ScreenUtils.dip2px(context, DEFAULT_FIRST_ROW_TOP_MARGIN);

		int vertical_divider_width = ScreenUtils.dip2px(context, DEFAULT_VERTICAL_DIVIDER_WIDTH);
		int horizontal_divider_width = ScreenUtils.dip2px(context, DEFAULT_HORIZONTAL_DIVIDER_WIDTH);

		int book_item_width = (int) ((screenWidth - left_margin - right_margin - (DEFAULT_BOOKS_COLUMN - 1) * horizontal_divider_width) / DEFAULT_BOOKS_COLUMN);

		LinearLayout verticalLayout = new LinearLayout(context);
		verticalLayout.setBackgroundColor(DEFAULT_VIEW_BACKGROUND);

		verticalLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.topMargin = top_margin;
		params.bottomMargin = bottom_margin;

		verticalLayout.setLayoutParams(params);
		verticalLayout.setBackgroundColor(context.getResources().getColor(R.color.bg_main));

		for (int k = 0; k < DEFAULT_BOOKS_ROW; k++) {

			boolean isNeedAddRow = k < ((int) Math.ceil(data.size() / (DEFAULT_BOOKS_COLUMN * 1.0f))) ? true : false;
			if (isNeedAddRow) {
				LinearLayout horizontaLayout = new LinearLayout(context);
				horizontaLayout.setOrientation(LinearLayout.HORIZONTAL);
				LayoutParams horizontalParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				horizontalParams.gravity = Gravity.CENTER;
				if (k == 0)
					horizontalParams.topMargin = firstRowTopMargin;
				horizontalParams.bottomMargin = vertical_divider_width;
				horizontaLayout.setLayoutParams(horizontalParams);

				for (int i = 0; i < DEFAULT_BOOKS_COLUMN; i++) {

					final int position = k * DEFAULT_BOOKS_COLUMN + i;
					try {
						if (position <= data.size() - 1) {
							if (data.get(position) == null)
								continue;
							LinearLayout bookItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.bookstore_style_ranking_list_item, null);
							LayoutParams bookParams = new LinearLayout.LayoutParams(book_item_width, LayoutParams.WRAP_CONTENT);
							bookParams.rightMargin = horizontal_divider_width;
							bookItem.setLayoutParams(bookParams);

							bookItem.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// if
									// (!NetWorkUtils.isNetworkAvailable(context))
									// {
									// Toast.makeText(context, "网络不可用",
									// Toast.LENGTH_LONG).show();
									// return;
									// }
									// TODO Talking-Data
									TalkingDataUtil.onBookStoreEvent(context, "首页", data.get(position).showName);

									Intent intent = new Intent(context, BookStoreBookListActivity.class);
									intent.putExtra("fid", data.get(position).id);
									intent.putExtra("ftype", 2);
									intent.putExtra("relationType", 1);
									intent.putExtra("showName", data.get(position).showName);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent);

								}
							});

							// bookItem.setBackgroundColor(0xff438745);
							final ImageView bookCover = (ImageView) bookItem.findViewById(R.id.book_cover);
							TextView bookName = (TextView) bookItem.findViewById(R.id.book_title);
							TextView bookInfo = (TextView) bookItem.findViewById(R.id.book_info);
							ImageLoader.getInstance().displayImage(data.get(position).picAddressAll, bookCover, 
									GlobalVarable.getDefaultAvatarDisplayOptions(false),impl);
							bookName.setText(data.get(position).showName);
							bookInfo.setText(data.get(position).note);

							horizontaLayout.addView(bookItem);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				verticalLayout.addView(horizontaLayout);
			}

		}
		return verticalLayout;

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
