package com.jingdong.app.reader.view.refreshview;

import com.android.refreshview.R;
import com.jingdong.app.reader.view.refreshview.XRefreshView.XRefreshViewListener;
import com.jingdong.app.reader.view.refreshview.callback.IFooterCallBack;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XRefreshViewFooter extends LinearLayout implements IFooterCallBack {
    private Context mContext;

    private View mContentView;
    private View mProgressBar;
    private TextView mHintView;
    private TextView mClickView;
    private TextView mTextLoading;

    public XRefreshViewFooter(Context context) {
        super(context);
        initView(context);
    }

    public XRefreshViewFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @Override
    public void callWhenNotAutoLoadMore(final XRefreshViewListener listener) {
        mClickView.setText(R.string.xrefreshview_footer_hint_click);
        mClickView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onLoadMore(false);
                    onStateRefreshing();
                }
            }
        });
    }

    @Override
    public void onStateReady() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mTextLoading.setVisibility(View.GONE);
        mClickView.setVisibility(View.VISIBLE);
        setVisibility(VISIBLE);
    }

    @Override
    public void onStateRefreshing() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mTextLoading.setVisibility(View.VISIBLE);
        mClickView.setVisibility(View.GONE);
        setVisibility(VISIBLE);
    }

    @Override
    public void onStateFinish() {
        mHintView.setText(R.string.xrefreshview_footer_hint_normal);
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mTextLoading.setVisibility(View.GONE);
        mClickView.setVisibility(View.GONE);
        setVisibility(VISIBLE);

    }

    @Override
    public void onStateComplete() {
        mHintView.setText(R.string.xrefreshview_footer_hint_complete);
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mTextLoading.setVisibility(View.GONE);
        setVisibility(VISIBLE);
    }

    public void hide() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
                .getLayoutParams();
        lp.height = 0;
        mContentView.setLayoutParams(lp);
    }

    public void show() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
                .getLayoutParams();
        lp.weight = mContext.getResources().getDimensionPixelSize(R.dimen.footer_width);
        lp.height = mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);
        mContentView.setLayoutParams(lp);
    }

    private void initView(Context context) {
        setOrientation(HORIZONTAL);
        mContext = context;
        LinearLayout moreView = (LinearLayout) LayoutInflater
                .from(mContext).inflate(R.layout.xrefreshview_footer, null);
        
        addView(moreView);

        mContentView = moreView.findViewById(R.id.xrefreshview_footer_content);
        mProgressBar = moreView.findViewById(R.id.xrefreshview_footer_progressbar);
        mHintView = (TextView) moreView.findViewById(R.id.xrefreshview_footer_hint_textview);
        mClickView = (TextView) moreView.findViewById(R.id.xrefreshview_footer_click_textview);
        mTextLoading = (TextView) moreView.findViewById(R.id.xrefreshview_footer_loading);
        
        setVisibility(GONE);
    }

    @Override
    public int getFooterHeight() {
        return getMeasuredHeight();
    }
}
