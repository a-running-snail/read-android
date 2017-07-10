package com.jingdong.app.reader.me.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.me.model.ScoreRecord;
import com.jingdong.app.reader.me.model.ScoreRecordModel;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.jingdong.app.reader.view.xlistview.XListView.IXListViewListener;

@SuppressLint("InflateParams")
public class IntegrationRecordFrament extends BackHandledFragment implements TopBarViewListener, IXListViewListener {

	protected static final int GET_DATA_SUCCESS = 0x001;
	protected static final int GET_DATA_FAIL = 0x002;
	private static final int SHOW_LOADING = 0x03;
	private static final int SHOW_NORMAL = 0x04;
	private static final int SHOW_NET_ERROR = 0x05;
	private TopBarView topBarView;
	private XListView mRecordLv;
	private FragmentManager mFragmentMgr;
	private Activity mActivity;
	private int currentPage = 1;
	private int pageSize = 20;
	private List<ScoreRecordModel> mResultList;

	private Handler mHander = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GET_DATA_SUCCESS:
				ScoreRecord record = (ScoreRecord) msg.obj;
				setupUI(record);
				break;
			case GET_DATA_FAIL:

				break;
			}
		};
	};
	protected boolean noMore;
	private EmptyLayout mEmptyLayout;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
		this.mResultList = new ArrayList<ScoreRecordModel>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.integration_record_root, null);
		initView(view);
		onRefresh();
		return view;
	}

	private void getRecordData() {
		if (!NetWorkUtils.isNetworkAvailable(mActivity)) {
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			return;
		}
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getLastWeekScoreParams(currentPage, pageSize), true, new MyAsyncHttpResponseHandler(
				mActivity) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				String result = new String(responseBody);
				MZLog.d("J", "onResponse=======>>" + result);
				ScoreRecord record = GsonUtils.fromJson(result, ScoreRecord.class);
				Message msg = Message.obtain();
				if (record != null && "0".equals(record.getCode())) {
					msg.what = GET_DATA_SUCCESS;
					msg.obj = record;
				} else {
					msg.what = GET_DATA_FAIL;
				}
				mHander.sendMessage(msg);

				currentPage++;
				if (record.getTotalPage() == 1) {
					noMore = true;
				} else {
					if (currentPage == record.getTotalPage()) {
						noMore = true;
					}
				}
				if (noMore) {
					mRecordLv.setPullLoadEnable(false);
				}

				mRecordLv.stopRefresh();
				mRecordLv.stopLoadMore();
			}
		});
	}

	protected void setupUI(ScoreRecord record) {
		if (record == null) {
			return;
		}
		mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
		mResultList.addAll(record.getResultList());
		RecordListAdapter mAdapter = new RecordListAdapter(mActivity);
		mRecordLv.setAdapter(mAdapter);
	}

	private void initView(View view) {
		mFragmentMgr = getFragmentManager();
		topBarView = (TopBarView) view.findViewById(R.id.topbar);
		initTopbarView();
		mRecordLv = (XListView) view.findViewById(R.id.integration_record_list);
		mRecordLv.setPullLoadEnable(true);
		mRecordLv.setPullRefreshEnable(true);
		mRecordLv.setXListViewListener(this);
		mEmptyLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getRecordData();
			}
		});
	}

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setListener(this);
		topBarView.setTitle("积分记录");
	}

	@Override
	public void onLeftMenuClick() {
		if (mActivity != null) {
			((IntegrationActivity) mActivity).onBackPressed();
		}
	}

	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRightMenuTwoClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onRefresh() {
		MZLog.d("J", "onRefresh");
		currentPage = 1;
		mResultList.clear();
		getRecordData();
	}

	@Override
	public void onLoadMore() {
		MZLog.d("J", "onLoadMore");
		getRecordData();
	}

	class RecordListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public RecordListAdapter(Context ctx) {
			mInflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mResultList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mResultList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.integration_record_list_item, null);
				holder.recordInfoTv = (TextView) convertView.findViewById(R.id.record_info_tv);
				holder.recordTimeTv = (TextView) convertView.findViewById(R.id.record_time_tv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			ScoreRecordModel model = mResultList.get(position);
			String recordInfo = null;
			if (model.getStore() < 0) {
				recordInfo = model.getStoreTypeStr() + model.getGiftName() + " 花费" + model.getStorePlus() + "积分";
			} else {
				recordInfo = model.getStoreTypeStr() + " 获得" + model.getStore() + "积分";
			}
			holder.recordInfoTv.setText(recordInfo);
			try {
				SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd"); // 格式化当前系统日期
				Date dateTime = dateFm.parse(model.getCreated());
				String format = dateFm.format(dateTime);
				holder.recordTimeTv.setText(format);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			return convertView;
		}
	}

	static class ViewHolder {
		TextView recordInfoTv;
		TextView recordTimeTv;
	}
}
