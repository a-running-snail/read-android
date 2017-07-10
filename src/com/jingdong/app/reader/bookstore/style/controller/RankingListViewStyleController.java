package com.jingdong.app.reader.bookstore.style.controller;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.OnHeaderActionClickListener;
import com.jingdong.app.reader.entity.extra.StoreBook;
import com.jingdong.app.reader.util.ScreenUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 
 * 排行榜view，可以自由定义
 * 
 * @author WANGGUODONG
 * 
 */
public class RankingListViewStyleController {

	private static final int DEFAULT_BOOKS_ROW = 3;
	private static final int DEFAULT_BOOKS_COLUMN = 1;
	private static final int DEFAULT_LEFT_MARGIN = 0; // 整个View左边距
	private static final int DEFAULT_RIGHT_MARGIN = 0; // 整个View右边距
	private static final int DEFAULT_BOTTOM_MARGIN = 8; // 整个View底部边距
	private static final int DEFAULT_TOP_MARGIN = 0; // 整个View顶部边距

	private static final int DEFAULT_FIRST_ROW_TOP_MARGIN = 0; // 第一行离顶部view的距离

	private static final int DEFAULT_HORIZONTAL_DIVIDER_WIDTH = 0; // 书籍中间的水平空隙宽度
	private static final int DEFAULT_VERTICAL_DIVIDER_WIDTH = 0; // 书籍中间的垂直空隙宽度
	private static final int DEFAULT_VIEW_BACKGROUND = 0x00ffffff; // 默认VIEW背景颜色,默认透明

	/**
	 * 获取排行榜布局,显示指定行指定列
	 * 
	 * @param context
	 * @param data
	 *            所有按顺序排列的url数据
	 * @return 布局
	 */
	public static LinearLayout getRankingListStyleView(final Context context, String titleStr, String actionStr, int row, int column,
			final List<StoreBook> data, final OnHeaderActionClickListener listener) {
		if (data == null || data.size() == 0)
			return null;
		ImageLoadingListenerImpl loadingListenerImpl = new ImageLoadingListenerImpl();
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);
		int vertical_divider_width = ScreenUtils.dip2px(context, DEFAULT_VERTICAL_DIVIDER_WIDTH);
		int horizontal_divider_width = ScreenUtils.dip2px(context, DEFAULT_HORIZONTAL_DIVIDER_WIDTH);
		int firstRowTopMargin = ScreenUtils.dip2px(context, DEFAULT_FIRST_ROW_TOP_MARGIN);

		int book_item_width = (int) ((screenWidth - left_margin - right_margin - (column - 1) * horizontal_divider_width) / column);

		// MZLog.d("wangguodong", "bookwidth:"+book_item_width+"-------");

		LinearLayout verticalLayout = new LinearLayout(context);
		// verticalLayout.setBackgroundColor(DEFAULT_VIEW_BACKGROUND);

		verticalLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.topMargin = top_margin;
		params.bottomMargin = bottom_margin;

		verticalLayout.setLayoutParams(params);
		verticalLayout.setBackgroundColor(context.getResources().getColor(R.color.bg_main));
		if (data != null) {
			LinearLayout headerItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.bookstore_style_header_item, null);
			// headerItem.setBackgroundColor(Color.YELLOW);
			TextView title = (TextView) headerItem.findViewById(R.id.title);
			LinearLayout action = (LinearLayout) headerItem.findViewById(R.id.action);
			TextView actionTv = (TextView) action.findViewById(R.id.action_info);
			if (data.size() >= row) {
				action.setVisibility(View.VISIBLE);
				actionTv.setText("更多");
			}else{
				action.setVisibility(View.GONE);
			}
			title.setText(titleStr);
			action.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.onHeaderActionClick();
					}

				}
			});
			verticalLayout.addView(headerItem);

		}

		for (int k = 0; k < row; k++) {

			boolean isNeedAddRow = k < ((int) Math.ceil(data.size() / (column * 1.0f))) ? true : false;
			if (isNeedAddRow) {
				LinearLayout horizontaLayout = new LinearLayout(context);
				horizontaLayout.setOrientation(LinearLayout.HORIZONTAL);
				LayoutParams horizontalParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				horizontalParams.gravity = Gravity.CENTER;
				horizontalParams.bottomMargin = vertical_divider_width;

				if (k == 0)
					horizontalParams.topMargin = firstRowTopMargin;

				horizontaLayout.setLayoutParams(horizontalParams);

				for (int i = 0; i < column; i++) {

					final int position = k * column + i;
					if (position <= data.size() - 1) {
						LinearLayout bookItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.bookstore_style_book_list_item, null);
						LayoutParams bookParams = new LinearLayout.LayoutParams(book_item_width, LayoutParams.WRAP_CONTENT);
						bookParams.rightMargin = horizontal_divider_width;
						bookItem.setLayoutParams(bookParams);

						bookItem.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent2 = new Intent(context, BookInfoNewUIActivity.class);

								if (!data.get(position).isEBook) {
									intent2.putExtra("bookid", data.get(position).paperBookId);
								} else {
									intent2.putExtra("bookid", data.get(position).ebookId);
								}

								intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(intent2);
							}
						});

						// bookItem.setBackgroundColor(0xff438745);
						ImageView bookCover = (ImageView) bookItem.findViewById(R.id.book_cover);
						TextView bookName = (TextView) bookItem.findViewById(R.id.book_title);
						TextView bookInfo = (TextView) bookItem.findViewById(R.id.book_info);
						TextView bookAuthor = (TextView) bookItem.findViewById(R.id.book_author);
						ImageView imageViewLabel = (ImageView) bookItem.findViewById(R.id.imageViewLabel);
						ImageLoader.getInstance().displayImage(data.get(position).imageUrl, bookCover, 
								GlobalVarable.getCutBookDisplayOptions(false),loadingListenerImpl);
						// ImageLoader.getInstance().displayImage(data.get(position).imageUrl,
						// bookCover,
						// GlobalVarable.getCutBookDisplayOptions(false));
						bookName.setText(data.get(position).name);
						TextPaint tpaint = bookName.getPaint();
						tpaint.setFakeBoldText(true);
						bookAuthor.setText("null".equals(data.get(position).author) ? context.getString(R.string.author_unknown) : data.get(position).author);
						if (!data.get(position).isEBook) {
							imageViewLabel.setBackgroundResource(R.drawable.badge_coverlabel_paper);
						} else {
							imageViewLabel.setBackgroundDrawable(null);
						}
						if (null == data.get(position).info) {
							bookInfo.setText("");
						} else {
							String info = data.get(position).info.replaceAll("^[　 ]*", "");
							bookItem.findViewById(R.id.book_info_Layout).setVisibility(View.VISIBLE);
							bookInfo.setText(info);
						}

						horizontaLayout.addView(bookItem);
					}
				}
				verticalLayout.addView(horizontaLayout);
			}

		}
		return verticalLayout;
	}

	/**
	 * 获取排行榜布局,显示指定行指定列
	 * 
	 * @param context
	 * @param data
	 *            所有按顺序排列的url数据
	 * @return 布局
	 */
	public static LinearLayout getBookCoverListStyleView(final Context context, String titleStr, String actionStr, int row, int column,
			final List<StoreBook> data, final OnHeaderActionClickListener listener) {
		if (data == null || data.size() == 0)
			return null;
		
		ImageLoadingListenerImpl loadingListenerImpl = new ImageLoadingListenerImpl();
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);
		int vertical_divider_width = ScreenUtils.dip2px(context, DEFAULT_VERTICAL_DIVIDER_WIDTH);
		int horizontal_divider_width = ScreenUtils.dip2px(context, DEFAULT_HORIZONTAL_DIVIDER_WIDTH);
		int firstRowTopMargin = ScreenUtils.dip2px(context, DEFAULT_FIRST_ROW_TOP_MARGIN);

		int book_item_width = (int) ((screenWidth - left_margin - right_margin - (column - 1) * horizontal_divider_width) / column);

		// MZLog.d("wangguodong", "bookwidth:"+book_item_width+"-------");

		LinearLayout verticalLayout = new LinearLayout(context);
		// verticalLayout.setBackgroundColor(DEFAULT_VIEW_BACKGROUND);

		verticalLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.topMargin = top_margin;
		params.bottomMargin = bottom_margin;

		verticalLayout.setLayoutParams(params);
		verticalLayout.setBackgroundColor(context.getResources().getColor(R.color.bg_main));
		if (data != null) {
			LinearLayout headerItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.bookcover_style_header_item, null);
			// headerItem.setBackgroundColor(Color.YELLOW);
			TextView title = (TextView) headerItem.findViewById(R.id.title);
			LinearLayout action = (LinearLayout) headerItem.findViewById(R.id.action);
			TextView actionTv = (TextView) action.findViewById(R.id.action_info);
			if (data.size() >= row) {
				action.setVisibility(View.VISIBLE);
				actionTv.setText("更多");
			}else{
				action.setVisibility(View.GONE);
			}
			title.setText(titleStr);
			action.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.onHeaderActionClick();
					}

				}
			});
			verticalLayout.addView(headerItem);

		}

		for (int k = 0; k < row; k++) {

			boolean isNeedAddRow = k < ((int) Math.ceil(data.size() / (column * 1.0f))) ? true : false;
			if (isNeedAddRow) {
				LinearLayout horizontaLayout = new LinearLayout(context);
				horizontaLayout.setOrientation(LinearLayout.HORIZONTAL);
				LayoutParams horizontalParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				horizontalParams.gravity = Gravity.CENTER;
				horizontalParams.bottomMargin = vertical_divider_width;

				if (k == 0)
					horizontalParams.topMargin = firstRowTopMargin;

				horizontaLayout.setLayoutParams(horizontalParams);

				for (int i = 0; i < column; i++) {

					final int position = k * column + i;
					if (position <= data.size() - 1) {
						LinearLayout bookItem = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.bookstore_style_book_list_item, null);
						LayoutParams bookParams = new LinearLayout.LayoutParams(book_item_width, LayoutParams.WRAP_CONTENT);
						bookParams.rightMargin = horizontal_divider_width;
						bookItem.setLayoutParams(bookParams);

						bookItem.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent2 = new Intent(context, BookInfoNewUIActivity.class);

								if (!data.get(position).isEBook) {
									intent2.putExtra("bookid", data.get(position).paperBookId);
								} else {
									intent2.putExtra("bookid", data.get(position).ebookId);
								}

								intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(intent2);
							}
						});

						// bookItem.setBackgroundColor(0xff438745);
						ImageView bookCover = (ImageView) bookItem.findViewById(R.id.book_cover);
						TextView bookName = (TextView) bookItem.findViewById(R.id.book_title);
						TextView bookInfo = (TextView) bookItem.findViewById(R.id.book_info);
						TextView bookAuthor = (TextView) bookItem.findViewById(R.id.book_author);
						ImageView imageViewLabel = (ImageView) bookItem.findViewById(R.id.imageViewLabel);
						ImageLoader.getInstance().displayImage(data.get(position).imageUrl, bookCover,
								GlobalVarable.getCutBookDisplayOptions(false),loadingListenerImpl);
						// ImageLoader.getInstance().displayImage(data.get(position).imageUrl,
						// bookCover,
						// GlobalVarable.getCutBookDisplayOptions(false));
						bookName.setText(data.get(position).name);
						TextPaint tpaint = bookName.getPaint();
						tpaint.setFakeBoldText(true);
						bookAuthor.setText("null".equals(data.get(position).author) ? context.getString(R.string.author_unknown) : data.get(position).author);
						if (!data.get(position).isEBook) {
							imageViewLabel.setBackgroundResource(R.drawable.badge_coverlabel_paper);
						} else {
							imageViewLabel.setBackgroundDrawable(null);
						}
						if (null == data.get(position).info) {
							bookInfo.setText("");
						} else {
							String info = data.get(position).info.replaceAll("^[　 ]*", "");
							bookInfo.setText(info);
						}

						horizontaLayout.addView(bookItem);
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
