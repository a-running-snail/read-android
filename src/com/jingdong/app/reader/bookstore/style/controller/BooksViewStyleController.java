package com.jingdong.app.reader.bookstore.style.controller;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.bookstore.adapter.BookStoreIndexGridViewAdapter;
import com.jingdong.app.reader.bookstore.view.MyGridView;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.entity.extra.StoreBook;
import com.jingdong.app.reader.util.ScreenUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 
 * 书城书籍推荐部分布局，可以自由定义
 * 
 * @author WANGGUODONG
 * 
 */
public class BooksViewStyleController {

	// public static final int DEFAULT_BOOK_COVER_WIDTH = 102;
	// public static final int DEFAULT_BOOK_COVER_HEIGHT = 136;

	public static final int DEFAULT_BOOKS_ROW = 2;
	public static final int DEFAULT_BOOKS_COLUMN = 3;
	public static final int DEFAULT_LEFT_MARGIN = 0; // 整个View左边距
	public static final int DEFAULT_RIGHT_MARGIN = 0; // 整个View右边距
    
	public static final int DEFAULT_BOOK_LEFT_MARGIN = 16; // 底部bookView左边距
	public static final int DEFAULT_BOOK_RIGHT_MARGIN = 16; // 底部bookView右边距
	public static final int DEFAULT_BOTTOM_MARGIN = 0; // 整个View底部边距
	public static final int DEFAULT_TOP_MARGIN = 0; // 整个View顶部边距

	public static final int DEFAULT_FIRST_ROW_TOP_MARGIN = 0; // 第一行离顶部view的距离

	public static final int DEFAULT_HORIZONTAL_DIVIDER_WIDTH = 11; //
	// 书籍中间的水平空隙宽度
	public static final int DEFAULT_VERTICAL_DIVIDER_WIDTH = 16; // 书籍中间的垂直空隙宽度
	public static final int DEFAULT_VIEW_BACKGROUND = 0x00ffffff;
	
	private static Handler handler;

	// //
	// 默认VIEW背景颜色,默认透明

	/**
	 * Item Image 点击回调
	 * 
	 * @author WANGGUODONG
	 * 
	 */
	public interface OnHeaderActionClickListener {
		public void onHeaderActionClick();
	}

	public static MyGridView getBookGridView(final Context context, final List<StoreBook> bookList, String titleStr, String actionStr) {
		if (bookList == null || bookList.size() == 0 || context == null)
			return null;
		//设置布局Margin参数
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);

		//初始化View容器
		LinearLayout container = new LinearLayout(context);
		container.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.topMargin = 20;
		params.bottomMargin = bottom_margin;
		container.setLayoutParams(params);
		container.setGravity(Gravity.CENTER);
		container.setBackgroundColor(context.getResources().getColor(R.color.bg_main));

		MyGridView gridView = new MyGridView(context);
		gridView.setHorizontalSpacing(1);
		gridView.setVerticalSpacing(1);
		AbsListView.LayoutParams gridParams = new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.MATCH_PARENT);
		gridView.setLayoutParams(gridParams);
		gridView.setNumColumns(3);
		gridView.setColumnWidth((int) (screenWidth/3));
		gridView.setGravity(Gravity.CENTER);
		gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		
		BookStoreIndexGridViewAdapter adapter = new BookStoreIndexGridViewAdapter(context, bookList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent2 = new Intent(context, BookInfoNewUIActivity.class);
				intent2.putExtra("bookid", bookList.get(position).ebookId);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent2);
			}
		});
//		container.addView(gridView);
		
		return gridView;
	}
	
	
	

	/**
	 * 获取书籍推荐部分布局,显示2行3列
	 * 
	 * @param context
	 * @param data
	 *            所有按顺序排列的url数据
	 * @return 布局
	 */
	public static LinearLayout getBooksStyleView(final Context context, String titleStr,String showInfo, String actionStr, int row, int column, final List<StoreBook> bookList,
			final OnHeaderActionClickListener listener) {
		ImageLoadingListenerImpl listenerImpl = new ImageLoadingListenerImpl();
		if (bookList == null || bookList.size() == 0 || context == null)
			return null;

		float screenWidth = ScreenUtils.getWidthJust(context);// px
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);
		
		int bookLeftmargin = ScreenUtils.dip2px(context, DEFAULT_BOOK_LEFT_MARGIN);
		int bookRightmargin = ScreenUtils.dip2px(context, DEFAULT_BOOK_RIGHT_MARGIN);

		int firstRowTopMargin = ScreenUtils.dip2px(context, DEFAULT_FIRST_ROW_TOP_MARGIN);
		int vertical_divider_width = ScreenUtils.dip2px(context, DEFAULT_VERTICAL_DIVIDER_WIDTH);
		int horizontal_divider_width = ScreenUtils.dip2px(context, DEFAULT_HORIZONTAL_DIVIDER_WIDTH);

		int imageWidth = (int) ((screenWidth - (bookLeftmargin + bookRightmargin + (column - 1) * horizontal_divider_width)) / column);
		int imageHeight = 4 * imageWidth / 3;
		LinearLayout verticalLayout = new LinearLayout(context);

		verticalLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.topMargin = top_margin;
		params.bottomMargin = bottom_margin;

		LayoutInflater li = LayoutInflater.from(context);

		verticalLayout.setLayoutParams(params);
		verticalLayout.setBackgroundColor(context.getResources().getColor(R.color.bg_main));

		if (bookList != null) {
			LinearLayout headerItem = (LinearLayout) li.inflate(R.layout.bookstore_style_header_item, null);
			// headerItem.setBackgroundColor(Color.YELLOW);
			TextView title = (TextView) headerItem.findViewById(R.id.title);
			LinearLayout action = (LinearLayout) headerItem.findViewById(R.id.action);
			TextView actionTv = (TextView) action.findViewById(R.id.action_info);
			if (bookList.size() >= row) {
				action.setVisibility(View.VISIBLE);
				actionTv.setText("更多");
			} else {
				action.setVisibility(View.GONE);
			}
			action.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.onHeaderActionClick();
					}

				}
			});

			View bottomLine = headerItem.findViewById(R.id.bottomLine);
			bottomLine.setVisibility(View.GONE);
			title.setText(titleStr);
			verticalLayout.addView(headerItem);

		}
		LayoutParams horizontalParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		horizontalParams.gravity = Gravity.CENTER;
		horizontalParams.bottomMargin = vertical_divider_width;
		horizontalParams.leftMargin = bookLeftmargin;
		horizontalParams.rightMargin = bookRightmargin;

		LayoutParams bookParams = new LinearLayout.LayoutParams(imageWidth, LayoutParams.WRAP_CONTENT);

		int size = bookList.size();
		int tempdata = (int) Math.ceil(size / (column * 1.0f));
		for (int k = 0; k < row; k++) {

			boolean isNeedAddRow = k < tempdata ? true : false;
			if (isNeedAddRow) {
				LinearLayout horizontaLayout = new LinearLayout(context);
				horizontaLayout.setOrientation(LinearLayout.HORIZONTAL);

				if (k == 0)
					horizontalParams.topMargin = firstRowTopMargin;
				horizontaLayout.setLayoutParams(horizontalParams);

				for (int i = 0; i < column; i++) {

					final int position = k * column + i;
					if (position <= bookList.size() - 1) {
						LinearLayout bookItem = (LinearLayout) li.inflate(R.layout.bookstore_style_book_item, null);

						bookParams.rightMargin = horizontal_divider_width;
						bookItem.setLayoutParams(bookParams);

						bookItem.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent2 = new Intent(context, BookInfoNewUIActivity.class);
								intent2.putExtra("bookid", bookList.get(position).ebookId);
								intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(intent2);
							}
						});

						// bookItem.setBackgroundColor(0xff438745);
						ImageView bookCover = (ImageView) bookItem.findViewById(R.id.book_cover);
						TextView bookName = (TextView) bookItem.findViewById(R.id.book_name);
						TextView bookAuthor = (TextView) bookItem.findViewById(R.id.book_author);
						TextView priceTv = (TextView) bookItem.findViewById(R.id.book_price);
						TextView jdPriceTv = (TextView) bookItem.findViewById(R.id.book_jdprice);

						LayoutParams coverParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);
						coverParams.gravity = Gravity.CENTER;
						bookCover.setLayoutParams(coverParams);
						ImageLoader.getInstance().displayImage(bookList.get(position).imageUrl, bookCover, GlobalVarable.getCutBookDisplayOptions(false),
								listenerImpl);

						if(!TextUtils.isEmpty(showInfo)){
							if(showInfo.contains("author")){
								bookAuthor.setText("null".equals(bookList.get(position).author) ? context.getString(R.string.author_unknown)
										: bookList.get(position).author);
							}else{
								bookAuthor.setVisibility(View.GONE);
							}
							
							if(showInfo.contains("price")){
								priceTv.setText("￥"+bookList.get(position).price);
								priceTv.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);  
							}else{
								priceTv.setVisibility(View.GONE);
							}
							
							if(showInfo.contains("jdPrice")){
								jdPriceTv.setText("￥"+bookList.get(position).jdPrice);
							}else{
								jdPriceTv.setVisibility(View.GONE);
							}
						}
						
						bookName.setText(bookList.get(position).name + "\n");
						TextPaint tpaint = bookName.getPaint();
						tpaint.setFakeBoldText(true);
						

						horizontaLayout.addView(bookItem);
					}
				}
				verticalLayout.addView(horizontaLayout);
			}

		}
		return verticalLayout;

	}
	
	/**
	 * 限时特价view
	 * 
	 */
	static long currentHour,currentMinute,currentSecond;
	public static MyReceiver myBroadCastReceiver;
	static AlarmManager am ;
	public static LinearLayout getSpecialPriceView(final Context context, String titleStr, String actionStr, int row, int column,long leftTime, 
			final BookStoreModuleBookListEntity entity ,final OnHeaderActionClickListener listener) {
		final List<StoreBook> bookList =entity.resultList;
		
		ImageLoadingListenerImpl listenerImpl = new ImageLoadingListenerImpl();
		if (bookList == null || bookList.size() == 0 || context == null)
			return null;

		final int showSize= bookList.size();
		
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		int left_margin = ScreenUtils.dip2px(context, DEFAULT_LEFT_MARGIN);
		int right_margin = ScreenUtils.dip2px(context, DEFAULT_RIGHT_MARGIN);
		int top_margin = ScreenUtils.dip2px(context, DEFAULT_TOP_MARGIN);
		int bottom_margin = ScreenUtils.dip2px(context, DEFAULT_BOTTOM_MARGIN);
		
		int bookLeftmargin = ScreenUtils.dip2px(context, DEFAULT_BOOK_LEFT_MARGIN);
		int bookRightmargin = ScreenUtils.dip2px(context, DEFAULT_BOOK_RIGHT_MARGIN);

		int firstRowTopMargin = ScreenUtils.dip2px(context, DEFAULT_FIRST_ROW_TOP_MARGIN);
		int vertical_divider_width = ScreenUtils.dip2px(context, DEFAULT_VERTICAL_DIVIDER_WIDTH);
		int horizontal_divider_width = ScreenUtils.dip2px(context, DEFAULT_HORIZONTAL_DIVIDER_WIDTH);

		int imageWidth = (int) ((screenWidth - (bookLeftmargin + bookRightmargin + (column - 1) * horizontal_divider_width)) / column);
		int imageHeight = 4 * imageWidth / 3;
		
		LinearLayout verticalLayout = new LinearLayout(context);

		verticalLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = left_margin;
		params.rightMargin = right_margin;
		params.topMargin = top_margin;
		params.bottomMargin = bottom_margin;

		LayoutInflater li = LayoutInflater.from(context);

		verticalLayout.setLayoutParams(params);
		verticalLayout.setBackgroundColor(context.getResources().getColor(R.color.bg_main));

		if (bookList != null) {
			LinearLayout headerItem = (LinearLayout) li.inflate(R.layout.bookstore_style_special_price_header, null);
			TextView title = (TextView) headerItem.findViewById(R.id.title);
			LinearLayout action = (LinearLayout) headerItem.findViewById(R.id.action);
			TextView actionTv = (TextView) action.findViewById(R.id.action_info);
			final TextView lefeTimeHourTv = (TextView) headerItem.findViewById(R.id.left_time_hour);
			final TextView lefeTimeMinuteTv = (TextView) headerItem.findViewById(R.id.left_time_minute);
			final TextView lefeTimeSecondTv = (TextView) headerItem.findViewById(R.id.left_time_second);
			
			final LinearLayout leftTimeLayout = (LinearLayout) headerItem.findViewById(R.id.left_time_layout);
			final LinearLayout endLayout = (LinearLayout) headerItem.findViewById(R.id.end_layout);
			leftTimeLayout.setVisibility(View.VISIBLE);
			endLayout.setVisibility(View.GONE);
			
			if (bookList.size() >= row) {
				action.setVisibility(View.VISIBLE);
				actionTv.setText("更多");
			} else {
				action.setVisibility(View.GONE);
			}
			action.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.onHeaderActionClick();
					}

				}
			});
			
			currentHour = leftTime / (60 * 60 * 1000);
			currentMinute = ((leftTime / (60 * 1000)) -  currentHour * 60);
			currentSecond = (leftTime / 1000 - currentHour * 60 * 60 - currentMinute * 60);
				
			if(handler!=null){
				handler.removeMessages(0);
			}
			handler = new Handler() {
				public void handleMessage(android.os.Message msg) {
//					Log.e("----倒计时handler-----", "221321");
					switch (msg.what) {
					case 0:
						if (currentSecond != 0)
							currentSecond--;
						else if (currentSecond == 0 && currentMinute != 0) {
							currentSecond = 59;
							currentMinute--;
						} else if (currentSecond == 0 && currentMinute == 0 && currentHour != 0) {
							currentSecond = 59;
							currentMinute = 59;
							currentHour--;
						} else {
							myBroadCastReceiver.abortBroadcast();
							context.unregisterReceiver(myBroadCastReceiver);
							myBroadCastReceiver=null;
							handler.removeMessages(0);
							handler.sendEmptyMessage(1);
						}
						
						lefeTimeHourTv.setText(String.format("%02d", currentHour));
						lefeTimeMinuteTv.setText(String.format("%02d", currentMinute));
						lefeTimeSecondTv.setText(String.format("%02d", currentSecond));
						break;
					case 1:
						leftTimeLayout.setVisibility(View.GONE);
						endLayout.setVisibility(View.VISIBLE);
						break;
					default:
						break;
					}
				};
			};
//			
			
			
			if(myBroadCastReceiver!=null){
				myBroadCastReceiver.abortBroadcast();
				context.unregisterReceiver(myBroadCastReceiver);
			}
			//生成广播处理   
			myBroadCastReceiver = new MyReceiver();   
			//实例化过滤器并设置要过滤的广播   
			IntentFilter intentFilter = new IntentFilter("COUNT_DOWN"); 
			//注册广播   
			context.registerReceiver(myBroadCastReceiver, intentFilter);  
			
			Intent intent = new Intent("COUNT_DOWN");
			intent.putExtra("msg","0");  
			PendingIntent pi = PendingIntent.getBroadcast(context,0,intent,0);  
			if(am!=null){
				am.cancel(pi);
			}
			am = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);  
			am.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),1000,pi);
			
			View bottomLine = headerItem.findViewById(R.id.bottomLine);
			bottomLine.setVisibility(View.GONE);
			title.setText(titleStr);
			verticalLayout.addView(headerItem);

		}
		
		LayoutParams horizontalParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		horizontalParams.gravity = Gravity.CENTER;
		horizontalParams.bottomMargin = vertical_divider_width;
		horizontalParams.leftMargin = bookLeftmargin;
		horizontalParams.rightMargin = bookRightmargin;

		LayoutParams bookParams = new LinearLayout.LayoutParams(imageWidth, LayoutParams.WRAP_CONTENT);

		int size = bookList.size();
		int tempdata = (int) Math.ceil(size / (column * 1.0f));
		for (int k = 0; k < row; k++) {

			boolean isNeedAddRow = k < tempdata ? true : false;
			if (isNeedAddRow) {
				LinearLayout horizontaLayout = new LinearLayout(context);
				horizontaLayout.setOrientation(LinearLayout.HORIZONTAL);

				if (k == 0)
					horizontalParams.topMargin = firstRowTopMargin;
				horizontaLayout.setLayoutParams(horizontalParams);

				for (int i = 0; i < showSize; i++) {

					final int position = k * column + i;
					if (position <= bookList.size() - 1) {
						LinearLayout bookItem = (LinearLayout) li.inflate(R.layout.bookstore_style_book_special_price_item, null);

						bookParams.rightMargin = horizontal_divider_width;
						bookItem.setLayoutParams(bookParams);
						
						//大于三本时点击书籍进入列表页
						if(showSize>3){
							bookItem.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(context, BookStoreBookListActivity.class);
									intent.putExtra("fid", entity.moduleBookChild.id);
									intent.putExtra("ftype", 2);
									intent.putExtra("relationType", 1);
									intent.putExtra("showName", entity.moduleBookChild.showName);
									intent.putExtra("from", "special_price");
									intent.putExtra("position", position);
									intent.putExtra("type", "specialPrice");
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent);
								}
							});
						}
						//小于三本时点击书籍进入书籍详情页
						else{
							bookItem.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent2 = new Intent(context, BookInfoNewUIActivity.class);
									intent2.putExtra("bookid", bookList.get(position).ebookId);
									intent2.putExtra("type", "specialPrice");
									intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.startActivity(intent2);
								}
							});
						}

						

						// bookItem.setBackgroundColor(0xff438745);
						ImageView bookCover = (ImageView) bookItem.findViewById(R.id.book_cover);
						TextView bookName = (TextView) bookItem.findViewById(R.id.book_name);
						TextView SpecialPrice = (TextView) bookItem.findViewById(R.id.special_price);
						TextView OriginalPrice = (TextView) bookItem.findViewById(R.id.original_price);

						LayoutParams coverParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);
						coverParams.gravity = Gravity.CENTER;
						bookCover.setLayoutParams(coverParams);
						ImageLoader.getInstance().displayImage(bookList.get(position).imageUrl, bookCover, GlobalVarable.getCutBookDisplayOptions(false),
								listenerImpl);

						bookName.setText(bookList.get(position).name + "\n");

						TextPaint tpaint = bookName.getPaint();
						tpaint.setFakeBoldText(true);
						SpecialPrice.setText("￥"+bookList.get(position).jdPrice);
						OriginalPrice.setText("￥"+bookList.get(position).price);
						OriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //删除线

						horizontaLayout.addView(bookItem);
					}
				}
				//展示书本大于3本时可以滚动浏览
				if(showSize>3){
					HorizontalScrollView scrollView = new HorizontalScrollView(context);
					LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					scrollView.setLayoutParams(params1);
					scrollView.setHorizontalScrollBarEnabled(false);
					scrollView.addView(horizontaLayout);
					verticalLayout.addView(scrollView);
				}
				else
					verticalLayout.addView(horizontaLayout);
			}
		}
		return verticalLayout;

	}			

	public static class MyReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String msg = intent.getStringExtra("msg");
			if(msg.equals("0"))
				handler.sendEmptyMessage(0);
		}

	}
	
	public static class ImageLoadingListenerImpl extends SimpleImageLoadingListener {
		public static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

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
