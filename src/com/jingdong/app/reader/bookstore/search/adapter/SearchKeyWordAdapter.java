package com.jingdong.app.reader.bookstore.search.adapter;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.bookstore.search.BookStoreSearchActivity;
import com.jingdong.app.reader.entity.extra.SearchKeyWord;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;

public class SearchKeyWordAdapter extends BaseAdapter{
	
	private List<SearchKeyWord> list = null ;
	private BookStoreSearchActivity context= null ; 
	private SearchKeyWordItemAdapter searchKeyWordItemAdapter = null ;
	private LayoutInflater listContainer; // 视图容器
	private LinearLayout.LayoutParams lp1 = null,lp2 = null;
	private boolean flag = false;
	
	static class ViewHolder {
		private TextView title= null;
		private ListView listitem = null ;
	}
	
	public SearchKeyWordAdapter(BookStoreSearchActivity context,List<SearchKeyWord> list,LinearLayout.LayoutParams  lp1,boolean flag){
		this.context = context;
		listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.list = list;
		this.lp1 = lp1 ;
		this.flag = flag;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(list==null||list.size()==0){
			return 0;		
		}else{
			return list.size();	
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder viewHolder = null;
		if(convertView==null){ 
			viewHolder = new ViewHolder(); 
			convertView = listContainer.inflate(R.layout.search_key_word, null);
			viewHolder.title= (TextView) convertView.findViewById(R.id.title);
			viewHolder.listitem= (ListView) convertView.findViewById(R.id.listitem);
			convertView.setTag(viewHolder); 
		}else{		
			viewHolder=(ViewHolder) convertView.getTag();
		}
		
		if(list.size() > 1){
			if (position == 0) {
				if (list.get(position).getData().size() > 3) {
					lp2 = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							(int) ScreenUtils.getWidthJust(context)*3*2/19);
				}else{
					lp2 = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							(int) ScreenUtils.getWidthJust(context)*(list.get(position).getData().size())*2/19);
				}
			}else{
				lp2 = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						(int) ScreenUtils.getWidthJust(context)*(list.get(position).getData().size())*2/19);
			}
		
		}else{
			lp2 = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					(int) ScreenUtils.getWidthJust(context)*(list.get(position).getData().size())*2/19);
			
		}
		
		viewHolder.listitem.setLayoutParams(lp2);
		
		viewHolder.title.setText(list.get(position).getTitle());
		MZLog.d("JD_Reader", "position:"+position+",title:"+list.get(position).getTitle());
		searchKeyWordItemAdapter = new SearchKeyWordItemAdapter(context, position,list,lp1,flag);
		viewHolder.listitem.setAdapter(searchKeyWordItemAdapter);
		return convertView;
	}

}
