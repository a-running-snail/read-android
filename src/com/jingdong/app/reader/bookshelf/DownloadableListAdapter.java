package com.jingdong.app.reader.bookshelf;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.bookshelf.data.BookShelfDataHelper.EBookItemHolder;
import com.jingdong.app.reader.bookshelf.inf.OnConvertViewClickListener;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.listener.DownloadInitListener;
import com.jingdong.app.reader.download.manager.DownloadManager;
import com.jingdong.app.reader.entity.MyOnlineBookEntity;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DownloadableListAdapter extends BaseAdapter{
	
	List<EBookItemHolder> mData;
	LayoutInflater mInflater;
	private Context mContext;
	private final int TAG = 1000;
	
	public DownloadableListAdapter(Context context, ArrayList<EBookItemHolder> itemHolders) {
		this.mContext = context;
		this.mData = itemHolders;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_free_gifts_booklist, parent, false);
			holder = new ViewHolder();
			holder.bookTitle = (TextView) convertView.findViewById(R.id.user_book_name);
			holder.bookAuthor = (TextView) convertView.findViewById(R.id.user_book_author);
			holder.statue_button = (Button) convertView.findViewById(R.id.statueButton);
			holder.bookCover = (ImageView) convertView.findViewById(R.id.user_book_cover);
			holder.bookSize = (TextView) convertView.findViewById(R.id.book_size);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final EBookItemHolder item = mData.get(position);
		final MyOnlineBookEntity eBook = item.ebook;
		holder.bookTitle.setText(eBook.ebookName);
		holder.bookAuthor.setText("null".equals(eBook.author) ? mContext.getString(R.string.author_unknown) : eBook.author);
		holder.bookSize.setText(eBook.size + "MB");
		holder.statue_button.setTextColor(mContext.getResources().getColor(R.color.highlight_color));
		holder.bookSize.setVisibility(View.VISIBLE);
		ImageLoader.getInstance().displayImage(eBook.imgUrl, holder.bookCover, GlobalVarable.getCutBookDisplayOptions(false));
		//下载按钮的点击事件
//			holder.statue_button.setOnClickListener(new OnConvertViewClickListener(convertView,TAG) {
//				
//				@Override
//				public void onClickCallBack(View registedView, int... positionIds) {
//					
//				}
//			});
		if (item.ebook.pass && item.ebook.supportCardRead) {
			holder.statue_button.setVisibility(View.VISIBLE);
			holder.statue_button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					DownloadManager.getInstance().init(mContext, null,new DownloadInitListener() {
						
						@Override
						public void onInitSuccess(DownloadFileInfo fileInfo) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onInitFail(DownloadFileInfo fileInfo, String errorInfo) {
							// TODO Auto-generated method stub
							
						}
					});
				}
			});
			if (item.existInLocal) {
				holder.statue_button.setText("阅读");
				holder.statue_button.setTextColor(mContext.getResources().getColor(R.color.highlight_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			} else if (item.paused) {
				holder.statue_button.setText("继续");
				holder.statue_button.setTextColor(mContext.getResources().getColor(R.color.text_main));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_black_h24);
			}else if (item.inWaitingQueue) {
				holder.statue_button.setText("等待");
				holder.statue_button.setTextColor(mContext.getResources().getColor(R.color.text_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
			}else if (item.failed) {
				holder.statue_button.setText("失败");
				holder.statue_button.setTextColor(mContext.getResources().getColor(R.color.r_text_disable));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
			} else {
				holder.statue_button.setText("下载");
				holder.statue_button.setTextColor(mContext.getResources().getColor(R.color.highlight_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			}
		}else{
			holder.statue_button.setVisibility(View.GONE);
			holder.bookSize.setText("暂不支持畅读");
		}
		return convertView;
	}
	
	static class ViewHolder {
		TextView bookTitle;
		TextView bookAuthor;
		Button statue_button;
		TextView bookSize;
		ImageView bookCover;
	}

}
