package com.jingdong.app.reader.bookstore.search.adapter;

import java.util.List;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.bookstore.search.BookStoreSearchActivity;
import com.jingdong.app.reader.entity.extra.SearchKeyWord;
import com.jingdong.app.reader.util.MZLog;

public class SearchKeyWordItemAdapter extends BaseAdapter{

	private List<SearchKeyWord> list = null ;
	private BookStoreSearchActivity context= null ; 
	private int num = 0 ;
	private LayoutInflater listContainer; // 视图容器
	private List<String> listitem= null;
	private LinearLayout.LayoutParams lp = null;
	private boolean flag = false;
	
	static class ViewHolder {
		private RelativeLayout relativeLayout;
		private LinearLayout linearLayout;
		private TextView search_info= null;
		private ImageView delete = null ;
	}
	
	public SearchKeyWordItemAdapter(BookStoreSearchActivity context,int num, List<SearchKeyWord> list,LinearLayout.LayoutParams lp,boolean flag){
		this.context = context;
		this.list = list;
		this.num = num;
		this.lp = lp;
		listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		listitem = list.get(num).getData();
		this.flag = flag;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(listitem==null||listitem.size()==0){
			return 0;		
		}else{
			if (list.size()>1) {
				if (num == 0) {
					if (listitem.size()>3) {
						return 3;
					}
					return listitem.size();
				}
				return listitem.size();
			}else{
				return listitem.size();	
			}
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if(convertView==null){ 
			viewHolder = new ViewHolder(); 
			convertView = listContainer.inflate(R.layout.search_key_word_item, null); 
			viewHolder.search_info= (TextView) convertView.findViewById(R.id.search_info);
			viewHolder.delete = (ImageView) convertView.findViewById(R.id.delete_img);
			viewHolder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.relativeLayout);
			viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.linearLayout);
			convertView.setTag(viewHolder); 
		}else{		
			viewHolder=(ViewHolder) convertView.getTag();
		}

		
		viewHolder.relativeLayout.setLayoutParams(lp);
		viewHolder.relativeLayout.setGravity(Gravity.CENTER_VERTICAL);
		viewHolder.linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		viewHolder.search_info.setText(listitem.get(position));
		if (num == 0 && flag) {
			viewHolder.delete.setVisibility(View.VISIBLE);
		}
		viewHolder.linearLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String query = list.get(0).getData().get(position);
				list.get(0).getData().remove(position);
				notifyDataSetChanged();
				if(list.get(0).getData().isEmpty()){
					context.initLists();
				}
				MZLog.d("cj", "click========dddddd>>");
				context.storeHistory(query, true);
			}
		});
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				context.foucesFlag = 1;
				context.setEditText(listitem.get(position));
				context.searchSubmit(listitem.get(position),0,false,true);
			}
		});
		return convertView;
	}

}
