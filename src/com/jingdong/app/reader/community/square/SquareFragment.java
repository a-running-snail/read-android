package com.jingdong.app.reader.community.square;

import java.util.ArrayList;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.community.FriendCircleFragment;
import com.jingdong.app.reader.community.FriendCircleFragment.TimelineFragmentRunnable;
import com.jingdong.app.reader.community.square.entity.SquareEntity;
import com.jingdong.app.reader.entity.BaseEvent;
import com.jingdong.app.reader.entity.ReLoginEvent;
import com.jingdong.app.reader.eventbus.de.greenrobot.event.EventBus;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.jingdong.app.reader.view.xlistview.XListView.IXListViewListener;
import com.tendcloud.tenddata.TCAgent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SquareFragment extends CommonFragment implements OnClickListener, SwipeRefreshLayout.OnRefreshListener, IXListViewListener{
	/** 每页数据个数 */
	private final int pageSize = 20;
	/** 当前页 */
	private int currentPage = 1;
	/** 广场列表数据 */
	private ArrayList<SquareEntity> mDataList = new ArrayList<SquareEntity>();
	private XListView mListView = null;
	private SquareAdapter mSquareAdapter = null;
	/** 数据排序类型显示布局 */
	private RelativeLayout mTypeLayout = null;
	/** 数据排序类型名称 */
	private TextView mTypeName = null;
	/** 数据排序类型 0：按时间 1：按热度 */
	private int sortType = 0;
	private SwipeRefreshLayout mSwipeRefreshLayout = null;
	/** 错误信息显示布局 */
	private EmptyLayout mErrorLayout;
	/** 数据加载中标识 */
	private boolean loading = false;
	/** 登陆请求结果标识 */
	private final int LOGIN = 111;
	
	public static final int START_ACTIVITY_FROM_LIST = 222;
	
	public static final int GO_TO_COMMENT = 213;

	public SquareFragment() {
		super();
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser) {
			if(mDataList.size() == 0) {
				if (!NetWorkUtils.isNetworkConnected(getActivity())) {
					mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				}else if(LoginUser.isLogin()){
					currentPage = 1;
					getSquareData(true);
				}else {
					mErrorLayout.setErrorType(EmptyLayout.NOT_LOGIN);
				}
			}
			
			TCAgent.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_square));
			StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_square));
		}else {
			TCAgent.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_square));
			StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_square));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_square, null);
		mListView = (XListView)rootView.findViewById(R.id.mListView);
		mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.mSwipeRefreshLayout);
		mSwipeRefreshLayout.setColorScheme(R.color.red_main, R.color.bg_main, R.color.red_sub, R.color.bg_main);
		mTypeLayout = (RelativeLayout)rootView.findViewById(R.id.mTypeLayout);
		mTypeName = (TextView)rootView.findViewById(R.id.mTypeName);
		mErrorLayout = (EmptyLayout)rootView.findViewById(R.id.error_layout);
		mTypeLayout.setOnClickListener(this);
		mSwipeRefreshLayout.setOnRefreshListener(this);
		mListView.setPullLoadEnable(false);
		mListView.setPullRefreshEnable(false);
		mListView.setXListViewListener(this);
		mErrorLayout.setOnLayoutClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(EmptyLayout.NETWORK_ERROR == mErrorLayout.mErrorState) {
					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
						mErrorLayout.postDelayed(new Runnable() {
							@Override
							public void run() {
								mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
							}
						}, 500);
					}else if(LoginUser.isLogin()){
						currentPage = 1;
						getSquareData(true);
					}else {
						mErrorLayout.setErrorType(EmptyLayout.NOT_LOGIN);
					}
				}else if(EmptyLayout.NOT_LOGIN == mErrorLayout.mErrorState) {
					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
					}else{
						Intent it = new Intent(getActivity(), LoginActivity.class);
						startActivityForResult(it, LOGIN);
					}
				}
			}
		});
		
		if (!NetWorkUtils.isNetworkConnected(getActivity())) {
			mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
		}else if(LoginUser.isLogin()){
			//检查缓存数据
			
//			if(mDataList.size() == 0) {
//			currentPage = 1;
//			getSquareData(true);
//			}
		}else {
			mErrorLayout.setErrorType(EmptyLayout.NOT_LOGIN);
		}
		EventBus.getDefault().register(this);
		return rootView;
	}
	
	public void onEventMainThread(BaseEvent baseEvent) {
		if (baseEvent instanceof ReLoginEvent) {
			try {
				if(mDataList!=null && mDataList.size() == 0 && mErrorLayout !=null) {
					if (!NetWorkUtils.isNetworkConnected(getActivity())) {
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
					}else if(LoginUser.isLogin()){
						currentPage = 1;
						getSquareData(true);
					}else {
						mErrorLayout.setErrorType(EmptyLayout.NOT_LOGIN);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.mTypeLayout:
			if(loading) {
				return;
			}
			
			if(0 == sortType) {
				sortType = 1;
			}else {
				sortType = 0;
			}
			currentPage = 1;
			getSquareData(false);
			mSwipeRefreshLayout.setRefreshing(true);
			break;
		
		default:
			break;
		}
	}
	
	/**
	* @Description: 请求广场列表数据
	* @param boolean showloading 是否在屏幕中间显示加载中
	* @author xuhongwei1
	* @date 2015年11月5日 下午3:50:06 
	* @throws 
	*/ 
	private void getSquareData(boolean showloading) {
		if(loading) {
			return;
		}
		
		if(showloading) {
			mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		}
		
		if (!NetWorkUtils.isNetworkConnected(getActivity())) {
			mErrorLayout.postDelayed(new Runnable() {
				@Override
				public void run() {
					loading = false;
					onLoadComplete();
					mSwipeRefreshLayout.setRefreshing(false);
					mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				}
			}, 500);
			return;
		}
		
		loading = true;
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSquareParams(currentPage, pageSize, sortType), 
				true, new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						loading = false;
						onLoadComplete();
						mSwipeRefreshLayout.setRefreshing(false); 
						String result = new String(responseBody);
						
						try {
							JSONObject json = new JSONObject(result);
							String codeStr = json.optString("code");
							if (!TextUtils.isEmpty(codeStr) && codeStr.equals("0")) {
								ArrayList<SquareEntity> list = JsonParseUtils.parseSquareEntity(json);
								if(1 == currentPage) {
									mDataList.clear();
									mDataList.addAll(list);
									initLayout();
								}else {
									mDataList.addAll(list);
									if(list.size() > 0) {
										flushData();
									}
								}
								int totalCount = json.optInt("totalCount");
								if (list.size() >= totalCount){
									mListView.setPullLoadEnable(false);
								}else
									mListView.setPullLoadEnable(true);
							} else {
								Toast.makeText(getActivity(), getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							Toast.makeText(getActivity(), getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
						}
						
						mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
						
					}
					
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						loading = false;
						onLoadComplete();
						mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
						mSwipeRefreshLayout.setRefreshing(false);
						Toast.makeText(getActivity(), getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}
			
		});
		
	}
	
	/**
	* @Description: 初始化布局
	* @author xuhongwei1
	* @date 2015年11月5日 下午3:51:24 
	* @throws 
	*/ 
	private void initLayout() {
		if(0 == sortType) {
			mTypeName.setText("按时间");
		}else {
			mTypeName.setText("按热度");
		}
		
		if(null == mSquareAdapter) {
			mSquareAdapter = new SquareAdapter(getActivity(), mListView, getRootFragment());
		}
		mSquareAdapter.setData(mDataList);
		mListView.setAdapter(mSquareAdapter);
	}
	
	/**
	* @Description: 刷新显示分页数据
	* @author xuhongwei1
	* @date 2015年11月5日 下午3:51:44 
	* @throws 
	*/ 
	private void flushData() {
		mSquareAdapter.setData(mDataList);
	}

	@Override
	public void onRefresh() {
		if(mDataList.size() == 0) {
			if (!NetWorkUtils.isNetworkConnected(getActivity())) {
				mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			}else if(LoginUser.isLogin()){
				currentPage = 1;
				getSquareData(false);
			}else {
				mErrorLayout.setErrorType(EmptyLayout.NOT_LOGIN);
			}
		}else {
			currentPage = 1;
			getSquareData(false);
		}
	}

	@Override
	public void onLoadMore() {
		currentPage++;
		getSquareData(false);
	}
	
	private void onLoadComplete() {
		mListView.setPullLoadEnable(true);
		mListView.stopRefresh();
		mListView.stopLoadMore();
	}
	
	private Fragment getRootFragment() {
		Fragment fragment = getParentFragment();
		while (fragment.getParentFragment() != null) {
			fragment = fragment.getParentFragment();
		}
		return fragment;
	}
	 
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SquareFragment.START_ACTIVITY_FROM_LIST) {
			if(resultCode == TimelineTweetActivity.UPDATE_COMMENTS_NUMBER) {
				String guid = data.getStringExtra(TimelineTweetActivity.TWEET_GUID);
				int index = getIndex(guid);
				SquareEntity itemData = mDataList.get(index);
				int commentCount = data.getIntExtra(TweetModel.COMMENT_NUMBER, itemData.commentCount);
				int recommendsCount = data.getIntExtra(TweetModel.RECOMMENTS_COUNT, itemData.recommendsCount);
				boolean recommend = data.getBooleanExtra(TweetModel.VIEWERRECOMMENDED, itemData.recommend);
				boolean delete = data.getBooleanExtra(TweetModel.IS_DELETE, false);
				 
				if(delete) {
					mDataList.remove(index);
				}else {
					mDataList.get(index).commentCount = commentCount;
					mDataList.get(index).recommendsCount = recommendsCount;
					mDataList.get(index).recommend = recommend;
				}
				mSquareAdapter.notifyDataSetChanged();
			}
		}else if(requestCode == LOGIN) {
			if(LoginUser.isLogin()) {
				//检查缓存数据
				if(mDataList.size() == 0) {
					currentPage = 1;
					getSquareData(true);
				}	
			}else {
				
			}
		}
		else if(requestCode == SquareFragment.GO_TO_COMMENT) {
			if (resultCode == getActivity().RESULT_OK) {
				String guid = data.getStringExtra(TimelineCommentsActivity.ENTITY_GUID);
				int index = getIndex(guid);
				SquareEntity itemData = mDataList.get(index);
				int commentCount = itemData.commentCount;
				mDataList.get(index).commentCount = commentCount+1;
				mSquareAdapter.notifyDataSetChanged();
			}
		}
	 }
	 
	 /**
	* @Description: 根据guid查询数据在列表中的位置
	* @param String guid 数据唯一id
	* @return int 返回位置
	* @author xuhongwei1
	* @date 2015年11月5日 下午3:52:50 
	* @throws 
	*/ 
	private int getIndex(String guid) {
		 int index = 0;
		 for(int i=0; i<mDataList.size(); i++) {
			 if(guid.equals(mDataList.get(i).guid)) {
				 return i;
			 }
		 }
		 
		 return index;
	 }
	
}
