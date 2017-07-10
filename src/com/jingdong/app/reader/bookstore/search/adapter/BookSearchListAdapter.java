package com.jingdong.app.reader.bookstore.search.adapter;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.entity.extra.JDBookDetail;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookSearchListAdapter extends BaseAdapter{

	private Context mContext;
	private List<JDBookDetail> mBookList;
	

	public BookSearchListAdapter(Context context,List<JDBookDetail> jdBookList) {
		this.mContext = context;
		this.mBookList = jdBookList;
	}

	@Override
	public int getCount() {
		return mBookList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup parent) {

		final ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.bookstore_search_list_item, parent, false);
			holder = new ViewHolder();
			holder.bookTitle = (TextView) convertView
					.findViewById(R.id.bookstore_search_user_book_name);
			holder.bookAuthor = (TextView) convertView
					.findViewById(R.id.bookstore_search_user_book_author);
			// holder.imageView = (ImageView) convertView
			// .findViewById(R.id.action_button);

			holder.bookCover = (ImageView) convertView
					.findViewById(R.id.bookstore_search_user_book_cover);

			holder.bookDesc = (TextView) convertView
					.findViewById(R.id.bookstore_search_book_desc);

			holder.imageViewLabel = (ImageView) convertView
					.findViewById(R.id.imageViewLabel);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final JDBookDetail eBook = mBookList.get(position);
		holder.bookTitle.setText(eBook.getName());
		holder.bookAuthor
				.setText("null".equals(eBook.getAuthor()) ? mContext.getString(R.string.author_unknown)
						: eBook.getAuthor());
		String info = eBook.getInfo();
		if (info != null) {
			info = info.replaceAll("^[　 ]*", "");
			info = info.replaceAll("\\s+", "");
			holder.bookDesc.setText(Html.fromHtml(info));
		} else {
			holder.bookDesc.setText("");
		}
		holder.bookDesc.setVisibility(View.VISIBLE);
		if (!eBook.isEBook()) {
			holder.imageViewLabel.setBackgroundDrawable(mContext.getResources().getDrawable(
					R.drawable.badge_coverlabel_paper));
		} else {
			if (eBook.isFluentRead()) {
				holder.imageViewLabel.setBackgroundDrawable(mContext.getResources().getDrawable(
						R.drawable.badge_coverlabel_vip));
			} else {
				holder.imageViewLabel.setBackgroundDrawable(null);
			}
		}
		
		ImageLoader.getInstance().displayImage(eBook.getImageUrl(),
				holder.bookCover, GlobalVarable.getCutBookDisplayOptions());
		return convertView;
	}
	
	static class ViewHolder {
		TextView bookTitle;
		TextView bookAuthor;
		TextView bookDesc;
		ImageView bookCover;
		ImageView imageViewLabel;
	}
}
