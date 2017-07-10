package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.entity.extra.OrderEntity;
import com.jingdong.app.reader.entity.extra.OrderList;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.OnLinePayTools;
import com.tendcloud.tenddata.TCAgent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OneMonthAgoFragment extends CommonFragment {

	private View loading;
	private ListView mListView;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private OrderAdapter orderAdapter;
	private List<OrderList> orderLists = new ArrayList<OrderList>();
	private boolean inSearch = false;
	private OrderEntity orderEntity;
	private RelativeLayout emptylLayout;
	private Button emptybutton;
	private View topView;

	public OneMonthAgoFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentTag = "BookStoreLightReadingFragment";
//		orderLists.clear();
		initField();
		searchOrder();
	}

	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		// Talking-Data
		TCAgent.onPageStart(getActivity(), "订单_一个月前");

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.onemoth_order, null);
		loading = rootView.findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		mListView = (ListView) rootView.findViewById(R.id.mlistview);
		emptylLayout = (RelativeLayout) rootView
				.findViewById(R.id.emptylLayout);
		emptybutton = (Button) rootView.findViewById(R.id.emptybutton);
		topView = (View) rootView.findViewById(R.id.topView);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0)
					return;

				if (firstVisibleItem + visibleItemCount == totalItemCount
						&& !noMoreBookOnSearch && !inSearch) {
					
					if (!inLoadingMoreOnSearch) {
						inLoadingMoreOnSearch = true;
						searchOrder();
					}
				}
			}

		});
		
		emptybutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(arg0.getContext(),
						LauncherActivity.class);
				intent.putExtra("TAB_INDEX", 0);
				startActivity(intent);
			}
		});

		View header = inflater.inflate(R.layout.listview_empty_header, null);
		mListView.addHeaderView(header);
		orderAdapter = new OrderAdapter(getActivity());
		mListView.setAdapter(orderAdapter);
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Talking-Data
		TCAgent.onPageEnd(getActivity(), "订单_一个月前");
	}

	private void searchOrder() {

		WebRequestHelper.get(
				URLText.JD_BASE_URL,
				RequestParamsPool.getOneMonthAgoOrderParams(currentSearchPage
						+ "", perSearchCount + ""),
				new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(getActivity(),
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						orderEntity = GsonUtils.fromJson(result,
								OrderEntity.class);

						if (orderEntity != null
								&& orderEntity.getCode().equals("0")) {
							if (orderEntity.getResultList().size() == 0 && orderLists.size() == 0) {
								topView.setVisibility(View.GONE);
								mListView.setVisibility(View.GONE);
								emptylLayout.setVisibility(View.VISIBLE);
							} else {
								currentSearchPage++;

								if (orderEntity.getResultList() != null
										&& orderEntity.getResultList().size() < perPageCount) {
									noMoreBookOnSearch = true;
								} else {
									noMoreBookOnSearch = false;
								}

								List<OrderList> all = new ArrayList<OrderList>();
								for (int i = 0; i < orderEntity.getResultList()
										.size(); i++) {
									OrderList orderList = orderEntity
											.getResultList().get(i);
									all.add(orderList);
								}
								orderLists.addAll(all);
								orderAdapter.notifyDataSetChanged();
							}
						} else if (orderEntity == null)
							Toast.makeText(getActivity(),
									getString(R.string.network_connect_error),
									Toast.LENGTH_LONG).show();
						inLoadingMoreOnSearch = false;
						loading.setVisibility(View.GONE);
					}
				});
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OnlinePayActivity.FromOrderListActivity:
			if(orderLists!=null && orderLists.size()>0)
				orderLists.clear();
			initField();
			searchOrder();
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class OrderAdapter extends BaseAdapter {

		private Context context;

		class ViewHolder {

			TextView order_id;
			TextView order_money;
			TextView order_time;
			Button statueButton;
		}

		OrderAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return orderLists.size();
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
				convertView = LayoutInflater.from(context).inflate(
						R.layout.order_item, parent, false);
				holder = new ViewHolder();
				holder.order_id = (TextView) convertView
						.findViewById(R.id.order_id);
				holder.order_money = (TextView) convertView
						.findViewById(R.id.order_money);
				holder.order_time = (TextView) convertView
						.findViewById(R.id.order_time);
				holder.statueButton = (Button) convertView
						.findViewById(R.id.statueButton);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (orderLists.get(position).getOrderStatus() == 1
					|| orderLists.get(position).getOrderStatus() == 4
					|| orderLists.get(position).getOrderStatus() == 8) {
				holder.statueButton.setText("");
				holder.statueButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(orderLists.get(position).getOrderMode() == 6){
							 OnLinePayTools.gotoSendBookOrderPay(getActivity(),orderLists.get(position).getOrderId()+"");
						}else
							OnLinePayTools.gotoOrderPay(getActivity(),orderLists.get(position).getOrderId()+"");
					}
				});
				holder.statueButton
						.setBackgroundResource(R.drawable.gotopay_btn);
				holder.order_money.setTextColor(getResources().getColor(
						R.color.red_main));
			} else if(orderLists.get(position).getOrderStatus() == 2){
				holder.statueButton.setText("已取消");
				holder.statueButton.setTextColor(getResources().getColor(
						R.color.text_sub));
				holder.statueButton.setClickable(false);
				holder.statueButton.setBackgroundDrawable(null);
				holder.order_money.setTextColor(getResources().getColor(
						R.color.text_sub));
			}
			//订单已完成
			if (orderLists.get(position).getOrderStatus() == 16) {
				//若OrderMode==6则为送书订单，需显示赠书状态
				if(orderLists.get(position).getOrderMode() == 6){
					if(orderLists.get(position).getSentStatus() == 0){
						//未赠送
						holder.statueButton.setText("");
						holder.statueButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								 //TODO 去赠书页面
								String sendbookUrl = "http://order.e.jd.com/buySendBook/buySendBook_toPage.action?client=android&send_order_id="+orderLists.get(position).getOrderId();
								Intent intent = new Intent(context,OnlinePayActivity.class);
								intent.putExtra("url", sendbookUrl);
								startActivityForResult(intent,OnlinePayActivity.FromOrderListActivity);
							}
						});
						holder.statueButton.setBackgroundResource(R.drawable.sendbook_btn);
						holder.order_money.setTextColor(getResources().getColor(R.color.text_sub));
					}
					else if(orderLists.get(position).getSentStatus() == 1 || orderLists.get(position).getSentStatus() == 3){
						//赠送未领取或者赠送失败
						holder.statueButton.setText("等待领取");
						holder.statueButton.setTextColor(getResources().getColor(
								R.color.text_sub));
						holder.statueButton.setClickable(false);
						holder.statueButton.setBackgroundDrawable(null);
						holder.order_money.setTextColor(getResources().getColor(
								R.color.text_sub));
						
					}else if(orderLists.get(position).getSentStatus() == 2){
						//赠送已领取
						holder.statueButton.setText("已领取");
						holder.statueButton.setTextColor(getResources().getColor(
								R.color.text_sub));
						holder.statueButton.setClickable(false);
						holder.statueButton.setBackgroundDrawable(null);
						holder.order_money.setTextColor(getResources().getColor(
								R.color.text_sub));
					}
				}else{//正常购买显示订单已完成
					holder.statueButton.setText("已完成");
					holder.statueButton.setTextColor(getResources().getColor(
							R.color.text_sub));
					holder.statueButton.setClickable(false);
					holder.statueButton.setBackgroundDrawable(null);
					holder.order_money.setTextColor(getResources().getColor(
							R.color.text_sub));
				}
			}
			
			holder.order_id.setText(orderLists.get(position).getOrderId() + "");
			if (orderLists.get(position).getPrice() == 0) {
				holder.order_money.setText("免费");
			}else {
				if (orderLists.get(position).getOrderMode() == 0||orderLists.get(position).getOrderMode() == 4) {
					holder.order_money.setText("￥" + orderLists.get(position).getPrice());
				}else if(orderLists.get(position).getOrderMode() == 3) {
					holder.order_money.setText(orderLists.get(position).getPrice()
							+ "贝币");
				}
			}
			holder.order_time.setText(orderLists.get(position).getOrderTime());

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(getActivity(),
							OrderDetailActivity.class);
					intent.putExtra("order_id", orderLists.get(position)
							.getOrderId() + "");
					intent.putExtra("order_money", orderLists.get(position)
							.getPrice() + "");
					intent.putExtra("order_time", orderLists.get(position)
							.getOrderTime());
					intent.putExtra("order_statue", orderLists.get(position)
							.getOrderStatus() + "");
					intent.putExtra("order_mode", orderLists.get(position)
							.getOrderMode() + "");
					intent.putExtra("send_status", orderLists.get(position).getSentStatus()+"");
					startActivity(intent);
				}
			});
			return convertView;
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser){
			StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_my_dingdan_month_ago));
		}else{
			StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_my_dingdan_month_ago));
		}
	}
	
}
