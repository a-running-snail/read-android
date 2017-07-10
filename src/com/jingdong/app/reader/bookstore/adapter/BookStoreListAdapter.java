package com.jingdong.app.reader.bookstore.adapter;

import java.util.ArrayList;
import java.util.List;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.bookstore.view.MyGridView;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BookStoreListAdapter extends BaseAdapter{
	
	public static final int TYPE_BOOK_LIST_GRID = 0;// 网格书籍TYPE5//1:全封面; 2:全列表;
	public static final int TYPE_BOOK_LIST_VERTICAL = 1;// 排行榜TYPE5//1:全封面; 2:全列表;
	public static final int TYPE_BOOK_LIST_SPECIAL_THEME = 2;// 特辑TYPE6
	private LayoutInflater mInflater;
	private List<BookStoreModuleBookListEntity> mModuleList;
	private Context mContext;
	private BookStoreIndexGridViewAdapter gridViewAdapter;
	private SpecialThemeListAdapter themeListAdapter;
	public BookStoreListAdapter(Context context, List<BookStoreModuleBookListEntity> moduleList) {
		this.mContext = context;
		this.mModuleList = moduleList;
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}
	@Override
	public int getCount() {
		return mModuleList.size();
	}

	@Override
	public Object getItem(int position) {
		return mModuleList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BookStoreModuleBookListEntity bookListEntity = mModuleList.get(position);
		ListGridHolder gridHolder = null;
		ListVerticalHolder verticalHolder = null;
		SpecialThemeHolder themeHolder = null;
		int type = getItemViewType(position);
		if (convertView == null) {
			switch (type) {
			case TYPE_BOOK_LIST_GRID:
				gridHolder = new ListGridHolder();
				convertView = mInflater.inflate(R.layout.bookstore_list_cell_grid, null);
				gridHolder.showNameTv = (TextView) convertView.findViewById(R.id.title);
				gridHolder.moreTv = (TextView) convertView.findViewById(R.id.action_info);
				gridHolder.myGridView = (MyGridView) convertView.findViewById(R.id.my_gridview);
				gridViewAdapter = new BookStoreIndexGridViewAdapter(mContext,bookListEntity.resultList);
				gridHolder.myGridView.setAdapter(gridViewAdapter);
				gridHolder.myGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
				gridHolder.showNameTv.setText(bookListEntity.moduleBookChild.showName);
				gridHolder.moreTv.setText("更多");
				convertView.setTag(gridHolder);
				break;
			case TYPE_BOOK_LIST_VERTICAL:
				verticalHolder = new ListVerticalHolder();
				convertView = mInflater.inflate(R.layout.bookstore_list_cell_verical, null);
				verticalHolder.showNameTv = (TextView) convertView.findViewById(R.id.title);
				verticalHolder.moreTv = (TextView) convertView.findViewById(R.id.action_info);
				verticalHolder.myListView = (ListView) convertView.findViewById(R.id.my_listview);
				convertView.setTag(verticalHolder);
				break;
			case TYPE_BOOK_LIST_SPECIAL_THEME:
				themeHolder = new SpecialThemeHolder();
				convertView = mInflater.inflate(R.layout.bookstore_list_cell_special_theme, null);
//				themeHolder.specialThemeLv = (ListView) convertView.findViewById(R.id.special_theme_lv);
//				List<BookStoreModuleBookListEntity.ModuleBookChild> childs = new ArrayList<BookStoreModuleBookListEntity.ModuleBookChild>();
//				childs.add(bookListEntity.moduleBookChild);
//				themeListAdapter = new SpecialThemeListAdapter(mContext, childs);
//				themeHolder.specialThemeLv.setAdapter(themeListAdapter);
				themeHolder.imageView = (ImageView) convertView.findViewById(R.id.book_cover);
				themeHolder.title = (TextView) convertView.findViewById(R.id.book_title);
				themeHolder.subtitle = (TextView) convertView.findViewById(R.id.book_info);
				themeHolder.topDriver = convertView.findViewById(R.id.theme_top_driver);
				themeHolder.bottomDriver = convertView.findViewById(R.id.theme_bottom_driver);
				ImageLoader.getInstance().displayImage(bookListEntity.moduleBookChild.picAddressAll, themeHolder.imageView, 
						GlobalVarable.getDefaultAvatarDisplayOptions(false));
				themeHolder.title.setText(bookListEntity.moduleBookChild.showName);
				themeHolder.subtitle.setText(bookListEntity.moduleBookChild.note);
				convertView.setTag(themeHolder);
				break;
			}
		}else{
			switch (type) {
			case TYPE_BOOK_LIST_GRID:
				gridHolder = (ListGridHolder) convertView.getTag();
				BookStoreIndexGridViewAdapter gridViewAdapter=(BookStoreIndexGridViewAdapter)gridHolder.myGridView.getAdapter();
				gridViewAdapter.setBookList(bookListEntity.resultList);
				gridViewAdapter.notifyDataSetChanged();
				gridHolder.showNameTv.setText(bookListEntity.moduleBookChild.showName);
				gridHolder.moreTv.setText("更多");
				break;
			case TYPE_BOOK_LIST_VERTICAL:
				verticalHolder = (ListVerticalHolder) convertView.getTag();
				break;
			case TYPE_BOOK_LIST_SPECIAL_THEME:
				themeHolder = (SpecialThemeHolder) convertView.getTag();
//				List<BookStoreModuleBookListEntity.ModuleBookChild> childs = new ArrayList<BookStoreModuleBookListEntity.ModuleBookChild>();
//				childs.add(bookListEntity.moduleBookChild);
//				SpecialThemeListAdapter themeListAdapter = (SpecialThemeListAdapter) themeHolder.specialThemeLv.getAdapter();
//				themeListAdapter.setDataList(childs);
//				themeListAdapter.notifyDataSetChanged();
				ImageLoader.getInstance().displayImage(bookListEntity.moduleBookChild.picAddressAll, themeHolder.imageView, 
						GlobalVarable.getDefaultAvatarDisplayOptions(false));
				themeHolder.title.setText(bookListEntity.moduleBookChild.showName);
				themeHolder.subtitle.setText(bookListEntity.moduleBookChild.note);
				break;
			}
		}
		return convertView;
	}
	
	 /** 
     * 根据数据源的position返回需要显示的的layout的type 
     *  
     * type的值必须从0开始 
     *  
     * */  
    @Override  
    public int getItemViewType(int position) {  
    	BookStoreModuleBookListEntity bookListEntity = mModuleList.get(position);
        return bookListEntity==null?0:bookListEntity.viewType;  
    }  
  
    /** 
     * 返回所有的layout的数量 
     *  
     * */  
    @Override  
    public int getViewTypeCount() {  
        return 3;  
    }
	
	
	
	static class ListGridHolder{
		TextView showNameTv;
		TextView moreTv;
		MyGridView myGridView;
		
	}
	
	static class ListVerticalHolder{
		TextView showNameTv;
		TextView moreTv;
		ListView myListView;
	}
	
	static class SpecialThemeHolder{
//		ListView specialThemeLv;
		View topDriver;
		View bottomDriver;
		ImageView imageView;
		TextView title;
		TextView subtitle;
	}

}
