package com.jingdong.app.reader.bookstore.adapter;

import java.util.List;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity.ModuleBookChild;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SpecialThemeListAdapter extends BaseAdapter{
	
	private Context context;
	private LayoutInflater mInflater;
	private List<ModuleBookChild> mDataList;

	public void setDataList(List<ModuleBookChild> mDataList) {
		this.mDataList = mDataList;
	}

	public SpecialThemeListAdapter(Context context,List<BookStoreModuleBookListEntity.ModuleBookChild> childs) {
		 this.context = context;
		 this.mDataList = childs;
		 this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.bookstore_style_ranking_list_item, null);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.book_cover);
			viewHolder.title = (TextView) convertView.findViewById(R.id.book_title);
			viewHolder.subtitle = (TextView) convertView.findViewById(R.id.book_info);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		ModuleBookChild moduleBookChild = mDataList.get(position);
		
		ImageLoader.getInstance().displayImage(moduleBookChild.picAddressAll, viewHolder.imageView, 
				GlobalVarable.getDefaultAvatarDisplayOptions(false));
		viewHolder.title.setText(moduleBookChild.showName);
		viewHolder.subtitle.setText(moduleBookChild.note);
		return convertView;
	}
	
	static class ViewHolder{
		ImageView imageView;
		TextView title;
		TextView subtitle;
	}
	
	

}
