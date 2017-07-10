package com.jingdong.app.reader.activity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.DelFromCartListener;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.GetShoppingCartInfoListener;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.BookCardItemEntity;
import com.jingdong.app.reader.entity.ProductEntity;
import com.jingdong.app.reader.entity.SuitEntity;
import com.jingdong.app.reader.entity.SuitEntity.PromotionalEntity;
import com.jingdong.app.reader.entity.extra.SimplifiedDetail;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.OnLinePayTools;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 *	购物车
 * @author xuhongwei
 *
 */
public class BookCartActivity extends BaseActivityWithTopBar {
	private ExpandableListView mCommonLv = null;
	private List<ItemHolder> mAllItemHolders = new ArrayList<ItemHolder>();
	private TextView payinfo = null;
	private List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	private List<String> mGroupData;// 定义组数据
	private List<List<ItemHolder>> mChildrenData;// 定义组中的子数据
	private CheckBox mAllselect = null;
	private List<GroupMoudle> mGroupMoudles = new ArrayList<GroupMoudle>();
	private HashMap<Long, ItemHolder> itemHolders_cache = null;
	private boolean hasPromotion = false;
	private boolean isDeleteOnRightMenu = false;
	private TextView mMoneyInfoTv;
	private List<SuitEntity> mSuitEntityList;
	private List<ProductEntity> mSignalProductList;
	private ExpandableAdapter mExpandableAdapter;
	private EmptyLayout mEmptyLayout;
	/** 去结算 */
	private TextView mGotoPay;
	/** 合计几本书 */
	private TextView mBookNum;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_bookcart);
		initView();
		initShoppingCart();
		initEvent();
	}

	/**
	 * 
	 * @Title: initView
	 * @Description: build up UI
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年4月2日 下午10:16:29
	 */
	private void initView() {
		mGotoPay = (TextView) findViewById(R.id.mGotoPay);
		mBookNum = (TextView) findViewById(R.id.mBookNum);
		mCommonLv = (ExpandableListView) findViewById(R.id.common_lv);
		payinfo = (TextView) findViewById(R.id.payinfo);
		mMoneyInfoTv = (TextView) findViewById(R.id.bookcart_money_info_tv);
		mAllselect = (CheckBox) findViewById(R.id.allslelect);
		mEmptyLayout = (EmptyLayout) findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEmptyLayout.getErrorState() == EmptyLayout.NETWORK_ERROR) {
					initShoppingCart();
				} else if (mEmptyLayout.getErrorState() == EmptyLayout.EMPTY_BOOKCART) {
					Intent intent = new Intent(BookCartActivity.this, LauncherActivity.class);
					intent.putExtra("lx", 0);
					startActivity(intent);
				}
			}
		});

		mGroupData = new ArrayList<String>();
		mChildrenData = new ArrayList<List<ItemHolder>>();
		mExpandableAdapter = new ExpandableAdapter();
		mCommonLv.setAdapter(mExpandableAdapter);
		mAllselect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				for (ItemHolder holder : mAllItemHolders) {
					holder.isChecked = mAllselect.isChecked();
				}
				updatePayinfo();
				mExpandableAdapter.notifyDataSetChanged();
			}
		});

	}

	/**
	 * 
	 * @Title: initShoppingCart
	 * @Description: 初始化购物车
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年4月24日 下午5:09:20
	 */
	public void initShoppingCart() {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		BookCartManager.getInstance().getShoppingCartInfos(this, new GetShoppingCartInfoListener() {

			@Override
			public void onSuccess(boolean isFromLocal, BookCardItemEntity bookCardItemEntity) {
				if (bookCardItemEntity != null) {
					mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
					getTopBarView().setRightMenuOneVisiable(true, "删除", R.color.red_main, false);
					adjustListView(bookCardItemEntity);
				} else {
					mEmptyLayout.setErrorType(EmptyLayout.EMPTY_BOOKCART);
					getTopBarView().setRightMenuOneVisiable(false, "删除", R.color.red_main, false);
				}
			}

			@Override
			public void onStart() {

			}

			@Override
			public void onFail() {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			}
		});
	}

	/**
	 * 
	 * @Title: initEvent
	 * @Description: 去结算 点击事件
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年4月2日 下午10:11:44
	 */
	private void initEvent() {
		if (payinfo == null) {
			return;
		}
		mGotoPay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (OnlinePayActivity.payidList == null)
					OnlinePayActivity.payidList = new ArrayList<String>();
				else
					OnlinePayActivity.payidList.clear();
				for (int i = 0; i < mAllItemHolders.size(); i++) {
					if (mAllItemHolders.get(i).isChecked) {
						SimplifiedDetail de = mAllItemHolders.get(i).detail;
						OnlinePayActivity.payidList.add("" + de.bookId);
					}
				}

				TalkingDataUtil.onBookDetailEvent(BookCartActivity.this, "购物车_结算_PV");

				OnLinePayTools.gotoEbookPay(BookCartActivity.this, null);
			}
		});
	}

	@Override
	public void onRightMenuOneClick() {
		int n = 0;
		for (ItemHolder holder : mAllItemHolders) {
			if (holder.isChecked) {
				n++;
			}
		}
		if (n == 0) {
			Toast.makeText(getBaseContext(), "请您先选中想要删除的书本！", Toast.LENGTH_SHORT).show();
			return;
		}
		showDialog("您确定要删除" + n + "本书？");
	}
	

	/**
	 * 
	 * @Title: adjustListView
	 * @Description: 根据服务端返回的商品价格信息调整布局
	 * @param @param itemEntity
	 * @return void
	 * @throws
	 * @date 2015年3月19日 下午6:35:27
	 */
	protected void adjustListView(BookCardItemEntity itemEntity) {
		mSuitEntityList = itemEntity.getSuitEntityList();
		mSignalProductList = itemEntity.getSignalProductList();

		mGroupData.clear();
		mChildrenData.clear();
		mGroupMoudles.clear();
		list.clear();
		mAllItemHolders.clear();
		mCommonLv.setAdapter(mExpandableAdapter);
		
		if (mSuitEntityList != null) {
			// 遍历促销商品列表
			for (int j = 0; j < mSuitEntityList.size(); j++) {
				SuitEntity suitEntity = mSuitEntityList.get(j);
				mGroupData.add(suitEntity.getPromotionalEntity().getName());
				List<ItemHolder> Child1 = new ArrayList<ItemHolder>();
				for (int k = 0; k < suitEntity.getProductEntityList().size(); k++) {
					ProductEntity entity = suitEntity.getProductEntityList().get(k);
					ItemHolder itemHolder = new ItemHolder();
					SimplifiedDetail sd = new SimplifiedDetail();
					sd.bookId = entity.getShopId();
					sd.logo = itemEntity.getImageDomain() + entity.getImgUrl();
					sd.jdPrice = entity.getShopPrice();
					sd.bookName = entity.getShopName();
					itemHolder.detail = sd;
					itemHolder.isChecked = true;
					itemHolder.promotionalEntity = suitEntity.getPromotionalEntity();
					mAllItemHolders.add(itemHolder);
					Child1.add(itemHolder);

				}
				mChildrenData.add(Child1);
				mGroupMoudles.add(new GroupMoudle(Child1));
			}
		}
		if (mSignalProductList != null) {
			List<ItemHolder> Child2 = new ArrayList<ItemHolder>();

			// 遍历非促销商品
			for (int k = 0; k < mSignalProductList.size(); k++) {
				ProductEntity productEntity = mSignalProductList.get(k);
				ItemHolder itemHolder = new ItemHolder();
				SimplifiedDetail sd = new SimplifiedDetail();
				sd.bookId = productEntity.getShopId();
				sd.logo = itemEntity.getImageDomain() + productEntity.getImgUrl();
				sd.jdPrice = productEntity.getShopPrice();
				sd.bookName = productEntity.getShopName();
				itemHolder.detail = sd;
				itemHolder.isChecked = true;
				mAllItemHolders.add(itemHolder);
				Child2.add(itemHolder);
			}

			mChildrenData.add(Child2);
			mGroupMoudles.add(new GroupMoudle(Child2));
			mGroupData.add("");
		}

		// 展开分组
		for (int i = 0; i < mGroupData.size(); i++) {
			mCommonLv.expandGroup(i);
		}
		// 不能点击收缩
		mCommonLv.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return true;
			}
		});

		StringBuilder sb = new StringBuilder();
		// 说明存在满减商品
		if (mSuitEntityList != null) {
			sb.append("原始金额：￥" + formatDouble(itemEntity.getOriginalPrice()) + "\n");
			sb.append("返现金额：￥" + formatDouble(itemEntity.getOriginalPrice() - itemEntity.getTotalCostcontent()) + "\n");
			sb.append("合计金额：￥" + formatDouble(itemEntity.getTotalCostcontent()));
			mMoneyInfoTv.setVisibility(View.VISIBLE);
			hasPromotion = true;
			mMoneyInfoTv.setText(sb.toString());
		} else {
			mMoneyInfoTv.setVisibility(View.GONE);
		}
		String payInfoText = null;
		if (isDeleteOnRightMenu) {
			payInfoText = "￥" + String.format("%.2f", 0.00) ;
		}else{
			payInfoText = "合计：￥" + itemEntity.getTotalCostcontent();
		}
		payinfo.setText(payInfoText);
		mBookNum.setText("共" + mAllItemHolders.size() + "本");

		setAllCheckBoxState(!isDeleteOnRightMenu);

	}

	/**
	 * 
	 * @Title: formatDouble
	 * @Description: 保存两位
	 * @param @param d
	 * @param @return
	 * @return double
	 * @throws
	 * @date 2015年4月27日 下午5:53:15
	 */
	private double formatDouble(double d) {
		BigDecimal b = new BigDecimal(d);
		return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 
	 * @Title: showDialog
	 * @Description: 删除商品确认弹窗
	 * @param @param message
	 * @return void
	 * @throws
	 * @date 2015年4月2日 下午10:15:32
	 */
	private void showDialog(String message) {

		DialogManager.showCommonDialog(this, "提示", message, "确定", "取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					isDeleteOnRightMenu = true;
					deleteFormBookCart();
					break;
				case DialogInterface.BUTTON_NEGATIVE:

					break;
				default:
					break;
				}
				dialog.dismiss();
			}
		});
	}

	/**
	 * 
	 * @Title: deleteFormBookCart
	 * @Description: 执行删除
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年4月2日 下午10:16:12
	 */
	public void deleteFormBookCart() {
		// switchContentView(SHOW_LOADING);
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		if (itemHolders_cache == null) {
			itemHolders_cache = new HashMap<Long, ItemHolder>();
		}
		List<Long> delBookList = new ArrayList<Long>();
		for (int i = 0; i < mAllItemHolders.size(); i++) {

			if (mAllItemHolders.get(i).isChecked) {
				MZLog.d("wangguodong", "删除书籍");
				delBookList.add(mAllItemHolders.get(i).detail.bookId);
			} else {
				itemHolders_cache.put(mAllItemHolders.get(i).detail.bookId, mAllItemHolders.get(i));
			}
		}
		String[] bookids = new String[delBookList.size()];
		for (int i = 0; i < delBookList.size(); i++) {
			bookids[i] = String.valueOf(delBookList.get(i));
		}
		BookCartManager.getInstance().deleteFromShoppingCart(this, true, bookids, new DelFromCartListener() {

			@Override
			public void onDelSuccess(boolean isDelInCart, BookCardItemEntity cardItemEntity) {
				if (cardItemEntity != null) {
					mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
					adjustListView(cardItemEntity);
				} else {
					// switchContentView(SHOW_EMPTY);
					mEmptyLayout.setErrorType(EmptyLayout.EMPTY_BOOKCART);
					getTopBarView().setRightMenuOneVisiable(false, "删除", R.color.red_main, false);
					// showEmptyPromt();
				}

			}

			@Override
			public void onDelFail() {
				Toast.makeText(BookCartActivity.this, "删除失败，请重试", Toast.LENGTH_LONG).show();
				// switchContentView(SHOW_NORMAL);
				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}
		});

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (itemHolders_cache != null) {
			itemHolders_cache.clear();
			itemHolders_cache = null;
		}
	}



	/**
	 * 
	 * @Title: updatePayinfo
	 * @Description: 更新购物车商品价格
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年3月19日 下午6:33:14
	 */
	public void updatePayinfo() {
		if (payinfo != null) {
			double originalTotalPrice = 0.00;
			double promotionPrice = 0.00;
			double sumPrice = 0.00;

			for (int i = 0; i < mGroupMoudles.size(); i++) {
				GroupMoudle groupMoudle = mGroupMoudles.get(i);
				groupMoudle.calculate();
				originalTotalPrice += groupMoudle.getOriginalPrice();
				promotionPrice += groupMoudle.getRePrice();
				sumPrice += groupMoudle.getSumPrice();
				MZLog.d("J.Beyond", "originalTotalPrice:" + originalTotalPrice);
				MZLog.d("J.Beyond", "promotionPrice:" + promotionPrice);
				MZLog.d("J.Beyond", "sumPrice:" + sumPrice);
			}
			StringBuilder sb = new StringBuilder();
			// 说明存在满减商品
			if (hasPromotion && promotionPrice >= 0) {
				sb.append("原始金额：￥" + String.format("%.2f", originalTotalPrice) + "\n");
				sb.append("返现金额：￥" + String.format("%.2f", promotionPrice) + "\n");
				sb.append("合计金额：￥" + String.format("%.2f", sumPrice));
				mMoneyInfoTv.setVisibility(View.VISIBLE);
				mMoneyInfoTv.setText(sb.toString());
			} else {
				// mMoneyInfoTv.setVisibility(View.GONE);
			}
			payinfo.setText("合计：￥" + String.format("%.2f", sumPrice));
			
			int num=0;
			for (int i = 0; i < mGroupMoudles.size(); i++) {
				GroupMoudle groupMoudle = mGroupMoudles.get(i);
				List<ItemHolder> list = groupMoudle.getItemHolders();
				for(int j=0; j<list.size(); j++) {
					ItemHolder item = list.get(j);
					if(item.isChecked) {
						num++;
					}
				}
			}
			
			mBookNum.setText("共" + num + "本");
		}
	}

	/**
	 * 
	 * @Title: setAllCBChecked
	 * @Description: 将所有checkbook设置为选中状态
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年3月19日 下午8:02:03
	 */
	public void setAllCheckBoxState(boolean state) {
		mAllselect.setChecked(state);
		if (!state && mSuitEntityList != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("原始金额：￥" + String.format("%.2f", 0.00) + "\n");
			sb.append("返现金额：￥" + String.format("%.2f", 0.00) + "\n");
			sb.append("合计金额：￥" + String.format("%.2f", 0.00));
			mMoneyInfoTv.setVisibility(View.VISIBLE);
			hasPromotion = true;
			mMoneyInfoTv.setText(sb.toString());
			payinfo.setText("￥" + 0.00);
			mBookNum.setText("共0本");
		}
		for (int i = 0; i < mGroupMoudles.size(); i++) {
			List<ItemHolder> itemHolders = mGroupMoudles.get(i).getItemHolders();
			for (int j = 0; j < itemHolders.size(); j++) {
				ItemHolder itemHolder = itemHolders.get(j);
				itemHolder.isChecked = state;
			}
		}

		mExpandableAdapter.notifyDataSetChanged();
	}

	/**
	 * list item的点击事件
	 * 
	 * @author Beyond
	 *
	 */
	private class ItemClickListener implements OnClickListener {
		private ViewHolder holder;
		private int childPosition;
		private int groupPosition;
		private ItemHolder itemHolder;
		private boolean isCheckBoxClick;

		public ItemClickListener(ViewHolder holder, ItemHolder itemHolder, int groupPosition, int childPosition, boolean isCheckBoxClick) {
			this.holder = holder;
			this.groupPosition = groupPosition;
			this.childPosition = childPosition;
			this.itemHolder = itemHolder;
			this.isCheckBoxClick = isCheckBoxClick;
		}

		@Override
		public void onClick(View v) {
			MZLog.d("JD_Reader", "ItemClick-->" + v);
			MZLog.d("JD_Reader", "holder.action.isChecked()-->" + holder.action.isChecked());
			if (isCheckBoxClick) {
				holder.action.setChecked(holder.action.isChecked());
			} else {
				holder.action.setChecked(!holder.action.isChecked());
			}
			ItemHolder itemHolder = mGroupMoudles.get(groupPosition).getItemHolders().get(childPosition);
			itemHolder.isChecked = holder.action.isChecked();
			updatePayinfo();
			changeAllSelectState();
		}

	}

	/**
	 * 
	 * @Title: changeAllSelectState
	 * @Description: 更改全选按钮的选中状态
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年3月19日 下午6:37:02
	 */
	private void changeAllSelectState() {
		boolean isAllItemChecked = true;
		for (int i = 0; i < mGroupMoudles.size(); i++) {
			List<ItemHolder> itemHolders = mGroupMoudles.get(i).getItemHolders();
			for (int j = 0; j < itemHolders.size(); j++) {
				if (!itemHolders.get(j).isChecked) {
					isAllItemChecked = false;
					break;
				}
			}
		}
		mAllselect.setChecked(isAllItemChecked);
	}

	/**
	 * list item view holder
	 * 
	 * @author Beyond
	 *
	 */
	static class ViewHolder {
		ImageView cover;
		TextView name;
		TextView price;
		CheckBox action;
		TextView promote;
		LinearLayout promoteLl;
		LinearLayout itemLl;
		View topLine;
		View bottomLine;
	}

	/**
	 * list item entity holder
	 * 
	 * @author Beyond
	 *
	 */
	static class ItemHolder {
		SimplifiedDetail detail;
		boolean isChecked = false;
		boolean hasPromotion = false;
		PromotionalEntity promotionalEntity;

	}

	/**
	 * 
	 * @ClassName: ExpandableAdapter
	 * @Description: 分组列表适配器
	 * @author J.Beyond
	 * @date 2015年3月19日 下午6:37:23
	 *
	 */
	private class ExpandableAdapter extends BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mChildrenData.get(groupPosition).get(childPosition);
		}
		
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(BookCartActivity.this).inflate(R.layout.item_bookcart_booklist, null);
				holder = new ViewHolder();
				holder.price = (TextView) convertView.findViewById(R.id.book_price);
				holder.action = (CheckBox) convertView.findViewById(R.id.action);
				holder.name = (TextView) convertView.findViewById(R.id.user_book_name);
				holder.cover = (ImageView) convertView.findViewById(R.id.user_book_cover);
				holder.promote = (TextView) convertView.findViewById(R.id.bookcart_promote_info_tv);
				holder.promoteLl = (LinearLayout) convertView.findViewById(R.id.bookcart_promote_info_ll);
				holder.itemLl = (LinearLayout) convertView.findViewById(R.id.bookcart_item_ll);
				holder.topLine = convertView.findViewById(R.id.bookcart_item_top_line);
				holder.bottomLine = convertView.findViewById(R.id.bookcart_item_bottom_line);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// 当前的item
			// final ItemHolder itemHolder = mAllItemHolders.get(childPosition);
			final ItemHolder itemHolder = mChildrenData.get(groupPosition).get(childPosition);
			if (childPosition == 0) {
				holder.topLine.setVisibility(View.VISIBLE);
			} else {
				holder.topLine.setVisibility(View.GONE);
			}
			LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
			holder.bottomLine.setVisibility(View.VISIBLE);
			Log.e("J", "holder.action.getMeasuredWidth()=="+holder.action.getMeasuredWidth());
			if (isLastChild) {
//				holder.bottomLine.setVisibility(View.GONE);
//				holder.itemLl.addView(getLineView());
				lineParams.leftMargin = 0;
			} else {
				lineParams.leftMargin = ScreenUtils.dip2px(70);
			}
			holder.bottomLine.setLayoutParams(lineParams);

			ImageLoader.getInstance().displayImage(itemHolder.detail.logo, holder.cover, GlobalVarable.getCutShopCartDisplayOptions(false));
			holder.name.setText(itemHolder.detail.bookName);
			holder.price.setText("￥" + itemHolder.detail.jdPrice + "");
			holder.action.setChecked(itemHolder.isChecked);
			holder.action.setOnClickListener(new ItemClickListener(holder, itemHolder, groupPosition, childPosition, true));
			convertView.setOnClickListener(new ItemClickListener(holder, itemHolder, groupPosition, childPosition, false));

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mChildrenData.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mGroupData.get(groupPosition);

		}

		@Override
		public int getGroupCount() {
			return mGroupData.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			TextView myText = null;
			if (convertView != null) {
				myText = (TextView) convertView;
				myText.setText(mGroupData.get(groupPosition));
			} else {
				myText = createView(mGroupData.get(groupPosition));
			}
			return myText;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		/**
		 * 
		 * @Title: createView
		 * @Description: 创建group布局
		 * @param @param content
		 * @param @return
		 * @return TextView
		 * @throws
		 * @date 2015年3月19日 下午6:38:06
		 */
		private TextView createView(String content) {
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
			TextView myText = new TextView(BookCartActivity.this);
			myText.setLayoutParams(layoutParams);
			myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			myText.setPadding(80, 15, 15, 0);
			myText.setTextColor(getResources().getColor(R.color.red_main));
			myText.setText(content);
			if (TextUtils.isEmpty(content)) {
				myText.setVisibility(View.GONE);
			}else{
				myText.setVisibility(View.VISIBLE);
			}
			return myText;
		}
	}
	public View getLineView() {
		View view = LayoutInflater.from(this).inflate(R.layout.lineview, null);
		return view;
	}
	/**
	 * 
	 * @ClassName: GroupMoudle
	 * @Description: 每一个group的实体，对外提供计算本组所有商品的原始价格、返现金额、合计金额
	 * @author J.Beyond
	 * @date 2015年3月19日 下午6:38:30
	 *
	 */
	class GroupMoudle {
		// 本组所有商品原始金额
		double originalPrice = 0.00;
		// 返现金额
		double rePrice = 0.00;
		// 本组所有商品合计金额
		double sumPrice = 0.00;

		List<ItemHolder> itemHolders;

		public GroupMoudle(List<ItemHolder> itemHolders) {
			this.itemHolders = itemHolders;
		}

		/**
		 * 计算本组商品价格
		 */
		public void calculate() {
			originalPrice = 0.00;
			sumPrice = 0.00;
			rePrice = 0.00;
			double promotePrice = 0.00;
			double needPrice = 0.00;
			for (int i = 0; i < itemHolders.size(); i++) {
				ItemHolder itemHolder = itemHolders.get(i);
				if (itemHolder.isChecked) {
					double jdPrice = itemHolder.detail.jdPrice;
					// 本组商品原始总价
					originalPrice += jdPrice;
					if (itemHolder.promotionalEntity != null) {
						promotePrice += itemHolder.detail.jdPrice;
						// 满减商品
						rePrice = itemHolder.promotionalEntity.getRePrice();
						needPrice = itemHolder.promotionalEntity.getNeedPrice();
					}
				}
			}
			// 达到满减条件(购买满减活动的商品总价达到条件)
			if (promotePrice >= needPrice) {
				sumPrice = originalPrice - rePrice;
			} else {
				rePrice = 0.00;
				sumPrice = originalPrice;
			}

		}

		public double getOriginalPrice() {
			return originalPrice;
		}

		public void setOriginalPrice(double originalPrice) {
			this.originalPrice = originalPrice;
		}

		public double getSumPrice() {
			return sumPrice;
		}

		public void setSumPrice(double sumPrice) {
			this.sumPrice = sumPrice;
		}

		public double getRePrice() {
			return rePrice;
		}

		public void setRePrice(double rePrice) {
			this.rePrice = rePrice;
		}

		public List<ItemHolder> getItemHolders() {
			return itemHolders;
		}

		public void setItemHolders(List<ItemHolder> itemHolders) {
			this.itemHolders = itemHolders;
		}

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_bookcart));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_bookcart));
	}

}
