package com.jingdong.app.reader.bookstore.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.bookstore.view.MyGridView;
import com.jingdong.app.reader.entity.extra.StoreBook;
import com.jingdong.app.reader.util.ScreenUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class BookStoreIndexGridViewAdapter extends BaseAdapter {

	private static final int DEFAULT_BOOKS_ROW = 2;
	private static final int DEFAULT_BOOKS_COLUMN = 3;
	private static final int DEFAULT_LEFT_MARGIN = 0; // 整个View左边距
	private static final int DEFAULT_RIGHT_MARGIN = 0; // 整个View右边距

	private static final int DEFAULT_BOOK_LEFT_MARGIN = 16; // 底部bookView左边距
	private static final int DEFAULT_BOOK_RIGHT_MARGIN = 16; // 底部bookView右边距
	private static final int DEFAULT_BOTTOM_MARGIN = 0; // 整个View底部边距
	private static final int DEFAULT_TOP_MARGIN = 0; // 整个View顶部边距

	private static final int DEFAULT_FIRST_ROW_TOP_MARGIN = 0; // 第一行离顶部view的距离

	private static final int DEFAULT_HORIZONTAL_DIVIDER_WIDTH = 11; //
	// 书籍中间的水平空隙宽度
	private static final int DEFAULT_VERTICAL_DIVIDER_WIDTH = 16; // 书籍中间的垂直空隙宽度
	private static final int DEFAULT_VIEW_BACKGROUND = 0x00ffffff;
	
	private Context context;
	private LayoutInflater inflater;
	private ImageLoadingListenerImpl loadingListenerImpl;
	private List<StoreBook> bookList;
	private int imageWidth;
	private int imageHeight;
	private GridView bookParams;
	private int horizontal_divider_width;
	private int vertical_divider_width;

	public BookStoreIndexGridViewAdapter(Context context, List<StoreBook> bookList) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.bookList = bookList;
		this.loadingListenerImpl = new ImageLoadingListenerImpl();
		iniyLayoutParams();
	}
	

	public void setBookList(List<StoreBook> bookList) {
		this.bookList = bookList;
	}


	private void iniyLayoutParams() {
		int bookLeftmargin = ScreenUtils.dip2px(context, DEFAULT_BOOK_LEFT_MARGIN);
		int bookRightmargin = ScreenUtils.dip2px(context, DEFAULT_BOOK_RIGHT_MARGIN);
		int firstRowTopMargin = ScreenUtils.dip2px(context, DEFAULT_FIRST_ROW_TOP_MARGIN);
		vertical_divider_width = ScreenUtils.dip2px(context, DEFAULT_VERTICAL_DIVIDER_WIDTH);
		horizontal_divider_width = ScreenUtils.dip2px(context, DEFAULT_HORIZONTAL_DIVIDER_WIDTH);
		LayoutParams horizontalParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		horizontalParams.gravity = Gravity.CENTER;
		horizontalParams.bottomMargin = vertical_divider_width;
		horizontalParams.leftMargin = bookLeftmargin;
		horizontalParams.rightMargin = bookRightmargin;
		//图片的宽高（依据屏幕分辨率）
		float screenWidth = ScreenUtils.getWidthJust(context);// px
		
		imageWidth = (int) ((screenWidth - (bookLeftmargin + bookRightmargin + (3 - 1) * horizontal_divider_width)) / 3);
		imageHeight = 4 * imageWidth / 3;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bookList.size();
	}

	@Override
	public Object getItem(int position) {
		return bookList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.bookstore_style_book_item, null);
			convertView.setPadding(0, 0, 0, 0);
			convertView.setLayoutParams(new GridView.LayoutParams(imageWidth,GridView.LayoutParams.MATCH_PARENT));
			holder = new ViewHolder();
			holder.book_cover = (ImageView) convertView.findViewById(R.id.book_cover);
			LayoutParams coverParams = new LinearLayout.LayoutParams(imageWidth, imageHeight);
			holder.book_cover.setScaleType(ScaleType.FIT_XY);
			holder.book_cover.setLayoutParams(coverParams);
			holder.book_cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
			
			holder.book_name = (TextView) convertView.findViewById(R.id.book_name);
			holder.book_author = (TextView) convertView.findViewById(R.id.book_author);
			holder.book_price = (TextView) convertView.findViewById(R.id.book_price);
			holder.book_jdprice = (TextView) convertView.findViewById(R.id.book_jdprice);
			holder.book_price.setVisibility(View.GONE);
			holder.book_jdprice.setVisibility(View.GONE);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		StoreBook storeBook = bookList.get(position);
		if (!TextUtils.isEmpty(storeBook.imageUrl)) {
			ImageLoader.getInstance().displayImage(storeBook.imageUrl, holder.book_cover, 
					GlobalVarable.getCutBookDisplayOptions(false), loadingListenerImpl);
		}
		
		holder.book_name.setText(storeBook.name+ "\n");
		holder.book_author.setText(storeBook.author);
		TextPaint tpaint = holder.book_name.getPaint();
		tpaint.setFakeBoldText(true);
		holder.book_author.setText("null".equals(bookList.get(position).author) ? context.getString(R.string.author_unknown)
				: bookList.get(position).author);
		
		return convertView;
	}
	
	
	
	static class ViewHolder{
		ImageView book_cover;
		TextView book_name;
		TextView book_author;
		TextView book_price;
		TextView book_jdprice;
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


