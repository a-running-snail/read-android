package com.jingdong.app.reader.bookstore.fragment;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper;
import com.jingdong.app.reader.bookstore.data.BookStoreDataHelper.GetCacheDataListener;
import com.jingdong.app.reader.cache.CacheManager;
import com.jingdong.app.reader.entity.CategoryImageModel;
import com.jingdong.app.reader.entity.extra.CategoryList;
import com.jingdong.app.reader.entity.extra.ChildList;
import com.jingdong.app.reader.entity.extra.JDCategoryBook;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.EmptyLayout;
import com.loopj.android.http.RequestParams;

/**
 * 
 * @ClassName: BookStoreCategoryNewFragment
 * @Description: 书城新分类页面
 * @author J.Beyond
 * @date 2015年5月21日 下午4:52:19
 *
 */
public class BookStoreCategoryNewFragment extends BookStoreBaseFragment {

	private JDCategoryBook jdCategoryBook = null;
	// private View screenLoading;
	private List<CategoryList> mCategoryList = null;
	private List<CategoryImageModel> mImageModels = new ArrayList<CategoryImageModel>();
	/** 标志位，标志已经初始化完成 */
	private boolean isPrepared;
	/** 是否已被加载过一次，第二次就不再去请求数据了 */
	private boolean mHasLoadedOnce;
	private View mFragmentView;
	private Activity mContext;
	private EmptyLayout mEmptyLayout;
	private ListView mTitleListView;
	private ArrayAdapter<String> mTitleAdapter;
	private TitleListAdapter mTitleListAdapter;
	private CategoryGirdViewFragment mGirdViewFragment;

	public BookStoreCategoryNewFragment() {
		super();
	}
	

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.mContext = activity;
		this.mHasLoadedOnce = false;
	}

	public interface LeftTitleClickListener {
		void onTitleClick(CategoryList categoryList);
	}

	private LeftTitleClickListener titleClick;

	public void setTitleClick(LeftTitleClickListener titleClick) {
		this.titleClick = titleClick;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (mCategoryList == null) {
			mCategoryList = new ArrayList<CategoryList>();
		}
		if (mFragmentView == null) {
			mFragmentView = LayoutInflater.from(mContext).inflate(R.layout.fragment_bookstore_category_new, null);
			initView();
			lazyLoad();
		}

		// 因为共用一个Fragment视图，所以当前这个视图已被加载到Activity中，必须先清除后再加入Activity
		ViewGroup parent = (ViewGroup) mFragmentView.getParent();
		if (parent != null) {
			parent.removeView(mFragmentView);
		}
		return mFragmentView;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		MZLog.d("J", "--->BookStoreCategoryNewFragment::isVisibleToUser="+isVisibleToUser);
		boolean isNetworkConnected = NetWorkUtils.isNetworkConnected(mContext);
		if (isVisibleToUser && isPrepared && mHasLoadedOnce && isNetworkConnected) {
			lazyLoad();
		}
		
		if(isVisibleToUser){
			StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_bookstore_type));
		}else{
			StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_bookstore_type));
		}
	}

	/**
	 * 
	 * @Title: initView
	 * @Description: 初始化控件
	 * @param 
	 * @return void
	 * @throws
	 */
	private void initView() {
		mTitleListView = (ListView) mFragmentView.findViewById(R.id.left_title_listview);
		
		mGirdViewFragment = new CategoryGirdViewFragment(this);

		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.right_fragment_container, mGirdViewFragment);
		fragmentTransaction.commitAllowingStateLoss();

		mEmptyLayout = (EmptyLayout) mFragmentView.findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lazyLoad();
			}
		});
		
		isPrepared = true;
	}
	
	@Override
	protected void lazyLoad() {
		if (!isVisible || !isPrepared || mHasLoadedOnce) {
			return;
		}
		reqCategoryListData();
	}


	/**
	 * 
	 * @Title: reqCategoryListData
	 * @Description: 获取分类数据(原始的分类数据没有图书封面信息，需要额外请求其他的接口)
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年5月21日 下午4:49:41
	 */
	private void reqCategoryListData() {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);

		BookStoreDataHelper bookStoreDataHelper = BookStoreDataHelper.getInstance();
		bookStoreDataHelper.getBookStoreCategoryData(mContext, new GetCacheDataListener() {

			@Override
			public void onSuccess(Serializable seri) {
				JDCategoryBook category = (JDCategoryBook) seri;
				mCategoryList = category.getCatList();
				//获取二级分类图书封面
				fetchLeafImage();
				mHasLoadedOnce = true;
			}

			@Override
			public void onFail() {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			}

			@Override
			public void onSuccess(Map<String, Serializable> moduleMap) {
				// TODO Auto-generated method stub

			}
		});

	}

	/**
	 * 
	 * @Title: fetchLeafImage
	 * @Description: 获取二级分类图书封面
	 * @param 
	 * @return void
	 * @throws
	 */
	protected void fetchLeafImage() {
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getCategoryImageParams(), new MyAsyncHttpResponseHandler(mContext) {
			
			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				String response = new String(responseBody);
				try {
					JSONObject json = new JSONObject(response);
					String code = json.optString("code");
					mImageModels.clear();
					if ("0".equals(code)) {
						JSONArray catList = json.optJSONArray("catList");
						if (catList != null) {
							for (int i = 0; i < catList.length(); i++) {
								JSONObject obj = catList.getJSONObject(i);
								int catId = obj.optInt("catId");
								String image = obj.optString("image");
								CategoryImageModel imageModel = new CategoryImageModel(image, catId);
								mImageModels.add(imageModel);
							}
							//整合分类数据
							integrateCategoryData();
						}else{
							setupLeftTitleList();
							MZLog.e("J", "解析catList异常");
						}
						
					}else{
						setupLeftTitleList();
						MZLog.e("J", "请求书城分类图片异常，code="+code);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}
		});;
	}


	/**
	 * 
	 * @Title: integrateCategoryData
	 * @Description: 将原始分类数据跟封面数据整合成新的数据模型
	 * @param 
	 * @return void
	 * @throws
	 */
	protected void integrateCategoryData() {
		for (int i = 0; i < mCategoryList.size(); i++) {
			CategoryList categoryList = mCategoryList.get(i);
			List<ChildList> childList = categoryList.getChildList();
			for (int j = 0; j < childList.size(); j++) {
				ChildList child = childList.get(j);
				for (int k = 0; k < mImageModels.size(); k++) {
					CategoryImageModel imageModel = mImageModels.get(k);
					if (child.getCatId() == imageModel.getCatId()) {
						child.setImage(imageModel.getImage());
					}
				}
			}
		}
		
		//设置左侧的标题菜单列表
		setupLeftTitleList();
	}

	/**
	 * 
	 * @Title: setupLeftTitleList
	 * @Description: 设置左侧标题列表
	 * @param
	 * @return void
	 * @throws
	 */
	protected void setupLeftTitleList() {
		if (mCategoryList != null && mCategoryList.size() > 0) {
			mTitleListAdapter = new TitleListAdapter(mContext);
			mTitleListView.setAdapter(mTitleListAdapter);
			mTitleListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
					if (titleClick != null) {
						CategoryList categoryList = mCategoryList.get(position);
						titleClick.onTitleClick(categoryList);
					}
					mTitleListAdapter.setSelectItem(position);
					mTitleListAdapter.notifyDataSetChanged();
					
					//将点击的Item平滑移动到顶部
					mTitleListView.post(new Runnable() {
						
						@Override
						public void run() {
							mTitleListView.smoothScrollToPositionFromTop(position, 0);
						}
					});
					
				}
			});
			
			//默认选中第一项
			mTitleListView.performItemClick(mTitleListView.getAdapter().getView(0, null, null), 0, mTitleListView.getAdapter().getItemId(0));
		}
	}

	

	//###############################################左侧列表适配器#####################################################

	class TitleListAdapter extends BaseAdapter {

		private Context context;
		LayoutInflater inflater;
		private int selectItem = -1;

		public TitleListAdapter(Context context) {
			this.context = context;
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mCategoryList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mCategoryList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TitleHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.common_title_item, null);
				holder = new TitleHolder();
				holder.titleTv = (TextView) convertView.findViewById(R.id.common_title_tv);
				holder.dirver = convertView.findViewById(R.id.driver);
				holder.frag = convertView.findViewById(R.id.sub_frag);
				convertView.setTag(holder);
			} else {
				holder = (TitleHolder) convertView.getTag();
			}
			
			CategoryList categoryList = mCategoryList.get(position);
			holder.titleTv.setText(categoryList.getShortName());

			// 如果位置相同则设置背景为黄色
			if (position == selectItem) {
				convertView.setBackgroundColor(Color.WHITE);
				holder.titleTv.setTextColor(mContext.getResources().getColor(R.color.red_main));
				holder.dirver.setVisibility(View.GONE);
				holder.frag.setVisibility(View.VISIBLE);
			} else {
				convertView.setBackgroundColor(Color.TRANSPARENT);
				holder.titleTv.setTextColor(mContext.getResources().getColor(R.color.black));
				holder.dirver.setVisibility(View.VISIBLE);
				holder.frag.setVisibility(View.GONE);
			}

			return convertView;
		}

		public void setSelectItem(int selectItem) {
			this.selectItem = selectItem;
		}

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.mHasLoadedOnce = false;
		this.mFragmentView = null;
	}
	
	static class TitleHolder {
		TextView titleTv;
		View dirver;
		View frag;
	}

	
}
