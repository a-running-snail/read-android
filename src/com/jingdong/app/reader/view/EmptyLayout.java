package com.jingdong.app.reader.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.util.ScreenUtils;

public class EmptyLayout extends LinearLayout implements android.view.View.OnClickListener {

	public static final int HIDE_LAYOUT = 4;
	public static final int NETWORK_ERROR = 1;
	public static final int NETWORK_LOADING = 2;
	public static final int NODATA = 3;
	public static final int EMPTY_BOOKCART = 5;
	public static final int EMPTY_BOOKSHELF_SEARCH_RESULT = 6;
	public static final int NOT_LOGIN = 7;

	private boolean clickEnable = true;
	private final Context mContext;
	private android.view.View.OnClickListener listener;
	public  int mErrorState;
	private ImageView mImageView;
	private LinearLayout mEmptyView;
	private View mLoadingView;
	private TextView mErrorTextInfo;
	private Button mErrorBtn;
	private ProgressBar mProgressBar;

	public EmptyLayout(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public EmptyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	private void init() {
		View view = View.inflate(mContext, R.layout.view_error_layout, null);
		mLoadingView = view.findViewById(R.id.loading_ll);
		mProgressBar = (ProgressBar) mLoadingView.findViewById(R.id.animProgress);
		mEmptyView = (LinearLayout)view.findViewById(R.id.error_view);
		mImageView = (ImageView) mEmptyView.findViewById(R.id.error_image);
		mErrorTextInfo = (TextView) mEmptyView.findViewById(R.id.error_txt_info);
		mErrorBtn = (Button) mEmptyView.findViewById(R.id.error_btn);
		mErrorBtn.setOnClickListener(this);
		setBackgroundColor(mContext.getResources().getColor(R.color.bg_main));
		addView(view);
	}

	public void dismiss() {
		mErrorState = HIDE_LAYOUT;
		setVisibility(View.GONE);
	}

	public int getErrorState() {
		return mErrorState;
	}

	public boolean isLoadError() {
		return mErrorState == NETWORK_ERROR;
	}

	public boolean isLoading() {
		return mErrorState == NETWORK_LOADING;
	}

	@Override
	public void onClick(View v) {
		if (clickEnable) {
			// setErrorType(NETWORK_LOADING);
			if (listener != null)
				listener.onClick(v);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		// MyApplication.getInstance().getAtSkinObserable().unregistered(this);
	}

	public void setDayNight(boolean flag) {
	}

	/**
	 * 新添设置背景
	 * 
	 * @author 火蚁 2015-1-27 下午2:14:00
	 * 
	 */
	public void setErrorImagAndMsg(int imgResource, String msg) {
		try {
			mImageView.setBackgroundResource(imgResource);
			mErrorTextInfo.setText(msg);
		} catch (Exception e) {
		}
	}
	
	public void setButtonInfo(int imgResource, String msg,int buttonTextColor) {
		mErrorBtn.setBackgroundResource(imgResource);
		mErrorBtn.setText(msg);
		mErrorBtn.setTextColor(buttonTextColor);
		mErrorBtn.setOnClickListener(listener);
		
		TextView empty = (TextView)mEmptyView.findViewById(R.id.error_empty);
		empty.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		btnParams.setMargins(ScreenUtils.dip2px(30), 0, ScreenUtils.dip2px(30), ScreenUtils.dip2px(40));
		mErrorBtn.setLayoutParams(btnParams);
		mEmptyView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
	}

	public void setErrorType(int i) {
		setVisibility(View.VISIBLE);
		switch (i) {
		case NETWORK_ERROR:
			mErrorState = NETWORK_ERROR;
			mErrorTextInfo.setText(R.string.error_view_network_error_click_to_refresh);
			mImageView.setBackgroundResource(R.drawable.icon_network_failure);
			mErrorBtn.setBackgroundResource(R.drawable.border_btn_grey_s12);
			mErrorBtn.setText(R.string.neterror_reload);
			mErrorBtn.setTextColor(mContext.getResources().getColor(R.color.gray));
			mEmptyView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
			clickEnable = true;
			break;
		case NETWORK_LOADING:
			mErrorState = NETWORK_LOADING;
			// animProgress.setBackgroundDrawable(SkinsUtil.getDrawable(context,"loadingpage_bg"));
			mLoadingView.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
			clickEnable = false;
			break;
		case NODATA:
			mErrorState = NODATA;
			mEmptyView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
			clickEnable = true;
			break;
		case HIDE_LAYOUT:
			setVisibility(View.GONE);
			break;
		case EMPTY_BOOKCART:
			mErrorState = EMPTY_BOOKCART;
			mEmptyView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
			setErrorImagAndMsg(R.drawable.shopcar_empty_icon, mContext.getString(R.string.shopping_cart_empty_txt));
//			setButtonInfo(R.drawable.shoppingcart_border_btn_red, mContext.getString(R.string.go_bookstore),mContext.getResources().getColor(R.color.red_main));
			
			mErrorBtn.setBackgroundColor(getResources().getColor(R.color.red_main));
			mErrorBtn.setText(mContext.getString(R.string.go_bookstore));
			mErrorBtn.setTextColor(Color.WHITE);
			mErrorBtn.setOnClickListener(listener);
			
			TextView empty = (TextView)mEmptyView.findViewById(R.id.error_empty);
			empty.setVisibility(View.GONE);
			LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ScreenUtils.dip2px(160), ScreenUtils.dip2px(44));
			mErrorBtn.setLayoutParams(btnParams);
			mEmptyView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
			break;
		case EMPTY_BOOKSHELF_SEARCH_RESULT:
			mErrorState = EMPTY_BOOKSHELF_SEARCH_RESULT;
			mEmptyView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
			setErrorImagAndMsg(R.drawable.bookstore_icon_search_null, mContext.getString(R.string.book_sign_option));
			setButtonInfo(R.drawable.shoppingcart_border_btn_red, mContext.getString(R.string.book_sign),mContext.getResources().getColor(R.color.red_main));
			break;
		case NOT_LOGIN:
			mErrorState = NOT_LOGIN;
			mErrorTextInfo.setText(R.string.not_login_text);
			mImageView.setBackgroundResource(R.drawable.community_none_bg);
			mErrorBtn.setBackgroundResource(R.drawable.shoppingcart_border_btn_red);
			mErrorBtn.setText(R.string.login_btn_text);
			mErrorBtn.setTextColor(mContext.getResources().getColor(R.color.red_main));
			mEmptyView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
			clickEnable = true;
			break;
		default:
			break;
		}
	}


	public void setOnLayoutClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	@Override
	public void setVisibility(int visibility) {
		if (visibility == View.GONE)
			mErrorState = HIDE_LAYOUT;
		super.setVisibility(visibility);
	}
}
