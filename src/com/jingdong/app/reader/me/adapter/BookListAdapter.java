package com.jingdong.app.reader.me.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.me.model.BookListModel;
import com.jingdong.app.reader.util.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookListAdapter extends BaseAdapter {

	private BookListModel bookList;
	private final Context context;
	private final boolean ratingBar;
	private final boolean showBorrowButton;
	private final boolean showBorrowText;
	private final boolean showHiddenText;
	private final String userId;
	private final boolean showNoteCount;
	private final int bookListType;

	/**
	 * 创建一个图书列表的Adapter
	 * 
	 * @param context
	 *            数据上下
	 * @param bookList
	 *            图书列表
	 * @param showRatingBar
	 *            显示星级
	 * @param showBorrowButton
	 *            显示借阅按钮
	 * @param showBorrowText
	 *            显示可借阅文字提示
	 * @param showHiddenText
	 *            显示已隐藏提示
	 * @param noteCount
	 *            笔记数量
	 */
	public BookListAdapter(Context context, int bookListType,
			BookListModel bookList, String userId, boolean showRatingBar,
			boolean showBorrowButton, boolean showBorrowText,
			boolean showHiddenText, boolean notes) {
		this.bookList = bookList;
		this.context = context;
		this.bookListType = bookListType;
		this.userId = userId;
		this.ratingBar = showRatingBar;
		this.showBorrowButton = showBorrowButton;
		this.showBorrowText = showBorrowText;
		this.showHiddenText = showHiddenText;
		this.showNoteCount = notes;
	}

	@Override
	public int getCount() {
		return bookList.getLength();
	}

	@Override
	public Object getItem(int position) {
		return bookList.getBookAt(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_homepage_booklist, null);
		}
		final Book book = bookList.getBookAt(position);
		ImageView bookCover = ViewHolder.get(convertView, R.id.user_book_cover);
		TextView bookName = ViewHolder.get(convertView, R.id.user_book_name);
		TextView bookAuthor = ViewHolder
				.get(convertView, R.id.user_book_author);

		// TextView borrow=ViewHolder.get(convertView, R.id.borrow);
		TextView inBookStore = ViewHolder.get(convertView, R.id.in_bookstore);
		RatingBar mRatingBar = ViewHolder.get(convertView,
				R.id.user_book_rating);
		TextView ratingValue = ViewHolder.get(convertView,
				R.id.book_rating_value);
		TextView notesCount = ViewHolder.get(convertView, R.id.notes_count);
		TextView notesCounts = ViewHolder.get(convertView, R.id.notes_counts);
		LinearLayout linearLayout = ViewHolder.get(convertView, R.id.linearLayout);

		bookAuthor.setText("null".equals(book.getAuthorName()) ? "佚名" : book
				.getAuthorName());
		if (book.isDocument()) {
			bookName.setText(book.getName());
			ImageLoader.getInstance().displayImage("", bookCover,
					GlobalVarable.getCutBookDisplayOptions(false));
		} else {
			bookName.setText(book.getTitle());
			ImageLoader.getInstance().displayImage(book.getCover(), bookCover,
					GlobalVarable.getCutBookDisplayOptions(false));
		}

		if (showNoteCount) {
			notesCount.setVisibility(View.VISIBLE);
			linearLayout.setVisibility(View.GONE);
			if (bookListType == BookListModel.NOTES_BOOKS) {
				notesCount.setText("笔记 " + book.getNoteCount());
			} else if (bookListType == BookListModel.IMPORT_BOOKS) {
				bookAuthor.setVisibility(View.GONE);
				notesCount.setText(book.getAuthorName());
			} else if (bookListType == BookListModel.READ_BOOKS) {
				notesCount.setText("笔记 " + book.getNoteCount());
			}
		} else {
			notesCount.setVisibility(View.GONE);
		}

		if (book.isEbook()) {
			inBookStore.setVisibility(View.VISIBLE);
			double price = book.getWebPrice();
			int text;
			if (price != 0)
				text = R.string.can_buy;
			else
				text = R.string.free;
			inBookStore.setText(text);
		} else if (book.isBorrowable() && showBorrowText) {
			inBookStore.setVisibility(View.VISIBLE);
			inBookStore.setText(R.string.can_borrow);
		} else {
			inBookStore.setVisibility(View.GONE);
		}
		if (showHiddenText && book.isHidden()) {
			inBookStore.setVisibility(View.VISIBLE);
			inBookStore.setText(R.string.ishide);
		}

		if (ratingBar) {
			notesCount.setVisibility(View.GONE);
			linearLayout.setVisibility(View.VISIBLE);
			if (book.getUserRating() == Book.NO_EXIST) {
				mRatingBar.setVisibility(View.GONE);

			} else if (Double.isNaN(book.getUserRating())
					&& !Double.isNaN(book.getRating())) {
				mRatingBar.setRating((float) book.getRating());
				notesCounts.setText("笔记 " + book.getNoteCount());
			} else if (!Double.isNaN(book.getUserRating())
					&& Double.isNaN(book.getRating())) {
				mRatingBar.setRating((float) book.getUserRating());
				notesCounts.setText("笔记 " + book.getNoteCount());
			} else {
				linearLayout.setVisibility(View.GONE);
			}

		} else {
			linearLayout.setVisibility(View.GONE);
		}

		return convertView;
	}

}
