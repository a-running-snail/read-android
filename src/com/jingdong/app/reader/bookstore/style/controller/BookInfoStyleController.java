package com.jingdong.app.reader.bookstore.style.controller;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.jd.voice.jdvoicesdk.util.Log;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.view.RoundNetworkImageView;

public class BookInfoStyleController {

	public static final int TYPE_BASIC_INFOMATION = 101;// 基本信息
	public static final int TYPE_BOOK_COMMENTS = 102;// 书评
	public static final int TYPE_BOOK_NOTES = 103;// 笔记
	
	public static final int TYPE_BOOK_HEADER = 104;// 书籍header
	public static final int TYPE_BOOK_SALES_PROMOTION = 105;// 书籍促销或者内容简介
	private static final int DEFAULT_HORIZONTAL_DIVIDER_WIDTH = 11; // 书籍中间的水平空隙宽度
	public static final int TYPE_BOOK_READED = 106 ; //读过本书的用户
	/*
	 * 展示的读过本书用户的数量
	 */
	public static int readedUserCount = 3;

	public static LinearLayout getBookinfoStyleView(Context context, int type,
			String titleStr, String actionStr) {

		LinearLayout layout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.bookstore_bookinfo_style_container, null);

		TextView header = (TextView) layout.findViewById(R.id.header_name);
		TextView footer = (TextView) layout.findViewById(R.id.footer_name);
		LinearLayout container = (LinearLayout) layout
				.findViewById(R.id.container);

		header.setText(titleStr);
		footer.setText(actionStr);

		if (type == TYPE_BOOK_COMMENTS) {
			for (int i = 0; i < BookInfoNewUIActivity.DEFAULT_BOOK_COMMENTS_COUNT; i++) {
				LinearLayout item = (LinearLayout) LayoutInflater.from(context)
						.inflate(R.layout.bookstore_bookinfo_comments_style,
								null);
				// // title = (TextView) item.findViewById(R.id.title);
				// TextView content = (TextView)
				// item.findViewById(R.id.content);
				// RatingBar mbar = (RatingBar) item.findViewById(R.id.rating);
				container.addView(item);
			}
		}
		else if (type == TYPE_BOOK_NOTES) {
			for (int i = 0; i < 3; i++) {
				LinearLayout item = (LinearLayout) LayoutInflater.from(context)
						.inflate(R.layout.bookstore_bookinfo_notes_style, null);
				TextView title = (TextView) item.findViewById(R.id.title);
				TextView content = (TextView) item.findViewById(R.id.content);
				RoundNetworkImageView avatar = (RoundNetworkImageView) item
						.findViewById(R.id.avatar);
				View divider = item.findViewById(R.id.divider);
				View default_divider = item.findViewById(R.id.default_divider);

				// title.setText("国东");
				// content.setText("国东国东国东国东国东国东国东国东国东国东国东国东国东国东国东国东国东国东国东国东国东");
				// ImageLoader
				// .getInstance()
				// .displayImage(
				// "http://b.hiphotos.baidu.com/image/pic/item/8cb1cb1349540923ec2138799058d109b3de4968.jpg",
				// avatar,
				// GlobalVarable
				// .getDefaultAvatarDisplayOptions(true));

				if (i == 3 - 1) {
					divider.setVisibility(View.VISIBLE);
					default_divider.setVisibility(View.GONE);
				} else {
					default_divider.setVisibility(View.VISIBLE);
					divider.setVisibility(View.GONE);
				}
				container.addView(item);
			}
		}
		else if (type == TYPE_BASIC_INFOMATION) {

			for (int i = 0; i < 3; i++) {
				LinearLayout item = (LinearLayout) LayoutInflater.from(context)
						.inflate(R.layout.bookstore_bookinfo_basicinfo_style,
								null);
				// TextView title = (TextView) item.findViewById(R.id.title);
				// TextView content = (TextView)
				// item.findViewById(R.id.content);
				// ImageView arrow = (ImageView) item.findViewById(R.id.arrow);
				//
				// if (i == 0 || i == 1)
				// arrow.setVisibility(View.VISIBLE);
				// else {
				// arrow.setVisibility(View.GONE);
				// }

				container.addView(item);
			}
		}else if(type == TYPE_BOOK_READED){
			//暂时屏蔽更多的入口，由于没有设计
			layout.findViewById(R.id.footer_layout).setVisibility(View.GONE);
			layout.findViewById(R.id.header_layout).setVisibility(View.GONE);
			
			DisplayMetrics dm = new DisplayMetrics();
			// 取得窗口属性
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
			// 窗口的宽度
			int screenWidth = dm.widthPixels;
			readedUserCount=3;
			
			LinearLayout item = (LinearLayout) LayoutInflater.from(context)
					.inflate(R.layout.bookstore_bookinfo_readed_style,
							null);
			LinearLayout avatarContainer = (LinearLayout) item.findViewById(R.id.avatarContainer);
			for (int i = 0; i < readedUserCount; i++) {
				LinearLayout avatar = (LinearLayout) LayoutInflater.from(context)
						.inflate(R.layout.bookstore_bookinfo_readed_item_style,
								null);
				RoundNetworkImageView image=null;
				if(i == 0){
					image=(RoundNetworkImageView) avatar.findViewById(R.id.thumb_nail);
					int imageWidth=image.getLayoutParams().width;
					int spaceWidth=imageWidth/4;
					readedUserCount = screenWidth /(imageWidth+spaceWidth);
				}
//				else if(i==readedUserCount-1){
//					image=(RoundNetworkImageView) avatar.findViewById(R.id.thumb_nail);
//					image.setImageDrawable(context.getResources().getDrawable(R.drawable.reader_btn_more));
//				}
				avatarContainer.addView(avatar);
			}
			LinearLayout line= new LinearLayout(context);
			LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT, 1);
			line.setLayoutParams(params);
			line.setBackgroundColor(context.getResources().getColor(R.color.hariline));
			container.addView(item);
			container.addView(line);
		}
		return layout;
	}

	public static LinearLayout getBackCoverStyleView(final Context context,
			String titleStr, String actionStr, String right) {

		LinearLayout layout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.bookstore_bookinfo_style_container, null);

		TextView header = (TextView) layout.findViewById(R.id.header_name);
		TextView header_right = (TextView) layout
				.findViewById(R.id.header_right_name);
		TextView footer = (TextView) layout.findViewById(R.id.footer_name);
		LinearLayout container = (LinearLayout) layout
				.findViewById(R.id.container);
		header_right.setVisibility(View.VISIBLE);
		header.setText(titleStr);
		header_right.setText(right);
		footer.setText(actionStr);

		for (int i = 0; i < BookInfoNewUIActivity.DEFAULT_BOOK_COMMENTS_COUNT; i++) {
			LinearLayout item = (LinearLayout) LayoutInflater.from(context)
					.inflate(R.layout.bookstore_bookinfo_comments_style, null);
			container.addView(item);
		}

		return layout;
	}

	public static LinearLayout getBookSalesOrInfoStyleView(Context context,
			String actionStr) {
		LinearLayout layout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.bookstore_bookinfo_style_container, null);

		LinearLayout topLinearLayout = (LinearLayout) layout
				.findViewById(R.id.top_linearlayout);
		
		TextView header = (TextView) layout.findViewById(R.id.header_name);
		TextView footer = (TextView) layout.findViewById(R.id.footer_name);
		LinearLayout container = (LinearLayout) layout
				.findViewById(R.id.container);

		topLinearLayout.setVisibility(View.GONE);
		header.setVisibility(View.GONE);
		footer.setText(actionStr);

		for (int i = 0; i < 3; i++) {
			LinearLayout item = (LinearLayout) LayoutInflater.from(context)
					.inflate(R.layout.bookstore_bookinfo_salesorinfo_style,
							null);
			TextView title = (TextView) item.findViewById(R.id.title);
			TextView content = (TextView) item.findViewById(R.id.content);
			ImageView arrow = (ImageView) item.findViewById(R.id.arrow);

			if (i % 2 == 0)
				arrow.setVisibility(View.GONE);
			else {
				arrow.setVisibility(View.VISIBLE);
			}

			container.addView(item);
		}

		return layout;
	}

	public static LinearLayout getBackCoverHeaderStyleView(Context context) {
		LinearLayout layout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.book_recoommend_header, null);

		return layout;
	}

	public static LinearLayout getBookInfoHeaderStyleView(Context context) {
		LinearLayout layout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.bookstore_bookinfo_style_container, null);

		TextView header = (TextView) layout.findViewById(R.id.header_name);

		View view = layout.findViewById(R.id.top_line);
		View bottomLineView = layout.findViewById(R.id.bottom_line);
		View ceterview = layout.findViewById(R.id.ceter_line);

		FrameLayout footerLayout = (FrameLayout) layout
				.findViewById(R.id.footer_layout);
		LinearLayout container = (LinearLayout) layout
				.findViewById(R.id.container);
		header.setVisibility(View.GONE);
		//设置纸书购买
		ceterview.setVisibility(View.VISIBLE);
		footerLayout.setVisibility(View.VISIBLE);
		footerLayout.setBackgroundColor(context.getResources().getColor(R.color.category_driver));
		TextView footerName = (TextView) layout.findViewById(R.id.footer_name);
		footerName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		footerName.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.bookinfo_detail_paperbook_buy_icon), null, null, null);
		footerName.setText(" "+context.getResources().getString(R.string.bookinfo_buypaperbook_text));
		

		view.setVisibility(View.GONE);
		bottomLineView.setVisibility(View.GONE);

		LinearLayout item = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.bookstore_bookinfo_header_style, null);

		final ImageView starImage = (ImageView) item
				.findViewById(R.id.star_img);
		final TextView starTxt = (TextView) item.findViewById(R.id.star_txt);

		starImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (starImage.isFocused()) {
					starImage.setFocusable(false);
					starTxt.setFocusable(false);
				} else {
					starImage.setFocusable(true);
					starTxt.setFocusable(true);
				}
			}
		});

		container.addView(item);
		return layout;
	}

	public static LinearLayout getBookInfoOtherLikeStyleView(Context context) {
		LinearLayout layout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.bookstore_bookinfo_style_withaction_container,
						null);
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		TextView title = (TextView) layout.findViewById(R.id.title);
		TextView action = (TextView) layout.findViewById(R.id.action);

		LinearLayout container = (LinearLayout) layout
				.findViewById(R.id.bookHolder);
		
			int horizontal_divider_width = ScreenUtils.dip2px(context, DEFAULT_HORIZONTAL_DIVIDER_WIDTH);

			int imageWidth = (int) ((screenWidth - (2 * ScreenUtils.dip2px(16) + 2 * horizontal_divider_width)) / 3);
			int imageHeight = 4 * imageWidth / 3;

			for (int i = 0; i < 3; i++) {

				LinearLayout item = (LinearLayout) LayoutInflater.from(context)
						.inflate(R.layout.bookstore_bookinfo_otherlike_style, null);

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT);
				params.width = imageWidth;
				if (i > 0) {
					params.leftMargin = horizontal_divider_width;
				}
				item.setLayoutParams(params);

				ImageView bookcover = (ImageView) item.findViewById(R.id.bookcover);
				TextView bookname = (TextView) item.findViewById(R.id.bookname);
				TextView bookAuthor = (TextView) item.findViewById(R.id.bookauthor);
				LayoutParams coverParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);
				coverParams.gravity = Gravity.CENTER;
				bookcover.setLayoutParams(coverParams);
				container.addView(item);
			}

		return layout;
	}

}
