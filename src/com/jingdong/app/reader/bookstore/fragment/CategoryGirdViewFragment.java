package com.jingdong.app.reader.bookstore.fragment;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.bookstore.fragment.BookStoreCategoryNewFragment.LeftTitleClickListener;
import com.jingdong.app.reader.entity.extra.CategoryList;
import com.jingdong.app.reader.entity.extra.ChildList;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class CategoryGirdViewFragment extends Fragment {

	private GridView mSubCategoryGridView;
	private CategoryList mCategoryList;
	private GridViewAdapter mAdapter;
	private Activity mContext;


	public CategoryGirdViewFragment() {
		super();
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.mContext = activity;
	}
	
	public CategoryGirdViewFragment(BookStoreCategoryNewFragment container) {
		if (container != null) {
			container.setTitleClick(new LeftTitleClickListener() {

				@Override
				public void onTitleClick(CategoryList categoryList) {
					if (categoryList != null) {
						mCategoryList = categoryList;
						mAdapter = new GridViewAdapter(mContext);
						mSubCategoryGridView.setAdapter(mAdapter);
					}
				}
			});
		}
	}

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.category_girdview_layout, null);
		initView(view);
		return view;
	}

	private void initView(View view) {
		mSubCategoryGridView = (GridView) view.findViewById(R.id.sub_category_gview);
		mSubCategoryGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ChildList childList = mCategoryList.getChildList().get(position);
				TalkingDataUtil.onBookStoreEvent(mContext, "分类", childList.getCatName() + "_" + childList.getCatName());
				Intent intent = new Intent(mContext, BookStoreBookListActivity.class);
				intent.putExtra("title", childList.getCatName());
				intent.putExtra("catId", childList.getCatId());
				intent.putExtra(BookStoreBookListActivity.LIST_TYPE, BookStoreBookListActivity.TYPE_CATAGORY);
				startActivity(intent);
			}
		});
	}

	class GridViewAdapter extends BaseAdapter {

		private Context context;
		private LayoutInflater inflater;
		private ImageLoadingListenerImpl loadingListenerImpl;

		public GridViewAdapter(Context context) {
			this.context = context;
			this.inflater = LayoutInflater.from(context);
			this.loadingListenerImpl = new ImageLoadingListenerImpl();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mCategoryList.getChildList().size();
		}

		@Override
		public Object getItem(int position) {
			return mCategoryList.getChildList().get(position);
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
				convertView = inflater.inflate(R.layout.category_grid_item, null);
				holder = new ViewHolder();
				holder.cover = (ImageView) convertView.findViewById(R.id.sub_cover);
				holder.title = (TextView) convertView.findViewById(R.id.sub_category_title);
				holder.count = (TextView) convertView.findViewById(R.id.sub_category_count);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			ChildList childList = mCategoryList.getChildList().get(position);
			ImageLoader.getInstance().displayImage(childList.getImage(), holder.cover, 
					GlobalVarable.getDefaultBookDisplayOptions(), loadingListenerImpl);
			holder.title.setText(childList.getCatName());
			holder.count.setText(childList.getAmount() + "");

			return convertView;
		}

	}

	static class ViewHolder {
		ImageView cover;
		TextView title;
		TextView count;
	}

	
	public static class ImageLoadingListenerImpl extends SimpleImageLoadingListener {
		public static final List<String> displayedImages = 
		          Collections.synchronizedList(new LinkedList<String>());
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
