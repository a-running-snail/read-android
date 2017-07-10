package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.OrderDetail;
import com.jingdong.app.reader.entity.extra.OrderDetailList;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.OnLinePayTools;
import com.nostra13.universalimageloader.core.ImageLoader;

public class OrderDetailActivity extends BaseActivityWithTopBar {
	
	private TextView order_id;
	private TextView order_money;
	private TextView order_time;
	private Button order_statue;
	private String orderId;
	private OrderDetail orderDetail;
	private List<OrderDetailList> orderDetailLists = new ArrayList<OrderDetailList>();
	private int currentSearchPage = 1;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private ListView mListView = null;
	private boolean inSearch = false;
	private OrderDetailAdapter orderDetailAdapter;
	private int order_mode;
	private int send_status;
	private Context context;
	private ImageView gotoSendbook;
	private View footerView;
	private LinearLayout acceptorLayout;
	private TextView acceptorPin;
	private TextView acceptTime;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.orderdetail);
		context = this;
		
		mListView = (ListView) findViewById(R.id.mlistview);
		
		initHeaderFooterView();
		init();
//		orderDetailLists.clear();
		initField();
		getDetailList();
	}
	
	private void initHeaderFooterView() {
		View headerView = LayoutInflater.from(context).inflate(R.layout.orderdetail_header,null);
		order_id = (TextView) headerView.findViewById(R.id.order_id);
		order_money = (TextView) headerView.findViewById(R.id.order_money);
		order_time = (TextView) headerView.findViewById(R.id.order_time);
		order_statue = (Button) headerView.findViewById(R.id.statueButton);
		acceptorLayout =  (LinearLayout) headerView.findViewById(R.id.acceptor_layout);
		acceptorPin = (TextView) headerView.findViewById(R.id.acceptor_pin);
		acceptTime = (TextView) headerView.findViewById(R.id.accept_time);
		mListView.addHeaderView(headerView);
		
		footerView = LayoutInflater.from(context).inflate(R.layout.orderdetail_footer,null);
		gotoSendbook = (ImageView) footerView.findViewById(R.id.goto_sendbook);
		
	}

	@SuppressWarnings("deprecation")
	private void init(){
		Intent intent = getIntent();
		order_id.setText(intent.getStringExtra("order_id"));
		
		order_time.setText(intent.getStringExtra("order_time"));
		orderId = intent.getStringExtra("order_id");
		int statue = Integer.parseInt(intent.getStringExtra("order_statue"));
		order_mode = Integer.parseInt(intent.getStringExtra("order_mode"));
		if (order_mode == 0 || order_mode == 6) {
			order_money.setText("￥" + intent.getStringExtra("order_money"));
		}else if(order_mode == 3) {
			order_money.setText(intent.getStringExtra("order_money") + "贝币");
		}
		send_status =  Integer.parseInt(intent.getStringExtra("send_status"));
		
		gotoSendbook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO 去赠书
				String sendbookUrl = "http://order.e.jd.com/buySendBook/buySendBook_toPage.action?client=android&send_order_id="+orderId;
				Intent intent = new Intent(context,OnlinePayActivity.class);
				intent.putExtra("url", sendbookUrl);
				startActivity(intent);
			}
		});
		
		if (statue == 1 || statue == 4 || statue == 8) {
			order_statue.setText("");
			order_statue.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(order_mode == 6){
						 OnLinePayTools.gotoSendBookOrderPay(OrderDetailActivity.this,orderId);
					}else
					 OnLinePayTools.gotoOrderPay(OrderDetailActivity.this,orderId);
				}
			});
			order_statue
					.setBackgroundResource(R.drawable.gotopay_btn);
			order_money.setTextColor(getResources().getColor(
					R.color.red_main));
		} else if(statue == 2){
			order_statue.setText("已取消");
			order_statue.setTextColor(getResources().getColor(
					R.color.text_sub));
			order_statue.setClickable(false);
			order_statue.setBackgroundDrawable(null);
			order_money.setTextColor(getResources().getColor(
					R.color.text_sub));
		}
		//订单已完成
		if (statue == 16) {
			//若OrderMode==6则为送书订单，需显示赠书状态
			if(order_mode == 6){
				if(send_status == 0){
					//未赠送
					order_statue.setText("");
					order_statue.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							 //TODO 去赠书页面
							String sendbookUrl = "http://order.e.jd.com/buySendBook/buySendBook_toPage.action?client=android&send_order_id="+orderId;
							Intent intent = new Intent(context,OnlinePayActivity.class);
							intent.putExtra("url", sendbookUrl);
							startActivity(intent);
						}
					});
					order_statue.setBackgroundResource(R.drawable.sendbook_btn);
					order_money.setTextColor(getResources().getColor(R.color.text_sub));
				}
				else if(send_status == 1 || send_status == 3){
					//赠送未领取或者赠送失败
					order_statue.setText("等待领取");
					order_statue.setTextColor(getResources().getColor(
							R.color.text_sub));
					order_statue.setClickable(false);
					order_statue.setBackgroundDrawable(null);
					order_money.setTextColor(getResources().getColor(
							R.color.text_sub));
					
					mListView.addFooterView(footerView);
					
				}else if(send_status == 2){
					//赠送已领取
					order_statue.setText("已领取");
					order_statue.setTextColor(getResources().getColor(
							R.color.text_sub));
					order_statue.setClickable(false);
					order_statue.setBackgroundDrawable(null);
					order_money.setTextColor(getResources().getColor(
							R.color.text_sub));
					
					acceptorLayout.setVisibility(View.VISIBLE);
					
				}
			}else{//正常购买显示订单已完成
				order_statue.setText("已完成");
				order_statue.setTextColor(getResources().getColor(
						R.color.text_sub));
				order_statue.setClickable(false);
				order_statue.setBackgroundDrawable(null);
				order_money.setTextColor(getResources().getColor(
						R.color.text_sub));
			}
		}
		
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
						&& !noMoreBookOnSearch && inSearch) {
					if (!inLoadingMoreOnSearch) {
						inLoadingMoreOnSearch = true;
						getDetailList();
					}
				}
			}

		});
		orderDetailAdapter = new OrderDetailAdapter(OrderDetailActivity.this);
		mListView.setAdapter(orderDetailAdapter);
	}
	
	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}
	
	private void getDetailList(){
		WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool
				.getOrderDetailParams(orderId, currentSearchPage + ""),
				new MyAsyncHttpResponseHandler(OrderDetailActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(OrderDetailActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);
						orderDetail = GsonUtils.fromJson(result,
								OrderDetail.class);

						if (orderDetail != null
								&& orderDetail.getCode().equals("0")) {
							currentSearchPage++;

							if (orderDetail.getResultList() != null) {
								noMoreBookOnSearch = true;
							} else {
								noMoreBookOnSearch = false;
							}
							
							if(orderDetail.getResultList() !=null && orderDetail.getResultList().size() >0 && send_status == 2){
								//赠送已领取
								acceptorLayout.setVisibility(View.VISIBLE);
								acceptorPin.setText(orderDetail.getResultList().get(0).getAcceptNickName());
								String time = orderDetail.getResultList().get(0).getAcceptTime();
								if(!TextUtils.isEmpty(time)){
									time = time.replace("T", " ");
									acceptTime .setText(time);
								}
							}

							List<OrderDetailList> all = new ArrayList<OrderDetailList>();
							for (int i = 0; i < orderDetail.getResultList()
									.size(); i++) {
								OrderDetailList orderList = orderDetail
										.getResultList().get(i);
								all.add(orderList);
							}
							orderDetailLists.addAll(all);
							orderDetailAdapter.notifyDataSetChanged();
						}
						else if(orderDetail == null)
							Toast.makeText(OrderDetailActivity.this,
									getString(R.string.network_connect_error), Toast.LENGTH_LONG).show();
						inLoadingMoreOnSearch = false;
					}
				});
	}
	
	private class OrderDetailAdapter extends BaseAdapter {

		private Context context;

		class ViewHolder {

			ImageView bookCover;
			TextView book_name;
//			TextView good_id;
			TextView author;
			TextView money;
			TextView beibi;
		}

		OrderDetailAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return orderDetailLists.size();
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
						R.layout.orderdetail_list_item, parent, false);
				holder = new ViewHolder();
				
				holder.bookCover = (ImageView) convertView.findViewById(R.id.book_cover);
				holder.book_name = (TextView) convertView
						.findViewById(R.id.book_name);
				holder.author = (TextView) convertView
						.findViewById(R.id.book_author);
//				holder.good_id = (TextView) convertView
//						.findViewById(R.id.good_id);
				holder.money = (TextView) convertView
						.findViewById(R.id.money);
				holder.beibi = (TextView) convertView
						.findViewById(R.id.beibi);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if(!TextUtils.isEmpty(orderDetailLists.get(position).getImgUrl())){
				ImageLoader.getInstance().displayImage(orderDetailLists.get(position).getImgUrl(), holder.bookCover, GlobalVarable.getCutBookDisplayOptions(false));
			}else{
				holder.bookCover.setImageResource(R.drawable.ebook_default_icon);
			}
			
			
			String autString = orderDetailLists.get(position).getAuthor();
					
			if(autString == null || "null".equals(autString) || "".equals(autString))
				holder.author.setText(getResources().getString(R.string.author_unknown) );
			else		
				holder.author.setText(orderDetailLists.get(position).getAuthor());;
			holder.book_name.setText(orderDetailLists.get(position).getName());
//			holder.good_id.setText("商品编号:  " + orderDetailLists.get(position).getBookId());
			if (Float.parseFloat(orderDetailLists.get(position).getPrice()) == 0.0f) {
				holder.money.setText("免费");
				holder.beibi.setVisibility(View.GONE);
			}else {
				if (orderDetailLists.get(position).getOrderMode() == 0 || orderDetailLists.get(position).getOrderMode() == 6) {
					holder.money.setText("￥" + orderDetailLists.get(position).getPrice());
					holder.beibi.setVisibility(View.GONE);
				}else {
					holder.money.setText(orderDetailLists.get(position).getPrice());
					holder.beibi.setVisibility(View.VISIBLE);
				}
			}
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(OrderDetailActivity.this,
							BookInfoNewUIActivity.class);
					intent.putExtra("bookid", orderDetailLists.get(position).getBookId());
					startActivity(intent);
				}
			});
			return convertView;
		}
	}
	@Override
	public void onRightMenuOneClick() {
		super.onRightMenuOneClick();
		OnLinePayTools.gotoOrderPay(this,orderId);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_dingdan_detail));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_dingdan_detail));
	}
	
}
