package com.jingdong.app.reader.activity;

import java.util.concurrent.PriorityBlockingQueue;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.bookstore.style.controller.BooksViewStyleController.OnHeaderActionClickListener;
import com.jingdong.app.reader.bookstore.style.controller.RankingListViewStyleController;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.view.EmptyLayout;
import com.loopj.android.http.RequestParams;

public class BookStorePaperBookActivity extends BaseActivityWithTopBar implements Runnable {

	LinearLayout containerLayout = null;
	private PriorityBlockingQueue<UrlParams> params = new PriorityBlockingQueue<UrlParams>();
	private boolean isReady = false;
	private boolean isCurrentRequestOver = true;
	private static final int MSG_REQUEST_OVER = 1;
	// private ProgressBar progressBar = null;
	
	private int index = 0;

	class UrlParams implements Comparable<UrlParams> {
		RequestParams params;
		int sort;
		String showname;
		boolean boolNew;

		@Override
		public int compareTo(UrlParams another) {

			return this.sort > another.sort ? 1 : this.sort < another.sort ? -1 : 0;

		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (msg.what == MSG_REQUEST_OVER) {
				UrlParams param = params.poll();
				if (param == null) {
					isReady = true;
					mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
				} else {
					isCurrentRequestOver = false;
					requestData(param);
				}
			}

		};
	};
	private EmptyLayout mEmptyLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookstore_fragment_one);
		containerLayout = (LinearLayout) findViewById(R.id.holder);
		mEmptyLayout = (EmptyLayout) findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				requestData(params.poll());
			}
		});
		params.clear();

		UrlParams params1 = new UrlParams();
		params1.params = RequestParamsPool.getPaperBookStoreListParams(1, 10, true);
		params1.showname = "新书排行";
		params1.sort = 1;
		params1.boolNew = true;

		UrlParams params2 = new UrlParams();
		params2.params = RequestParamsPool.getPaperBookStoreListParams(1, 10, false);
		params2.showname = "纸书热销";
		params2.sort = 2;
		params2.boolNew = false;

		params.offer(params1);
		params.offer(params2);

	}

	@Override
	public void onResume() {
		super.onResume();
		new Thread(this).start();
	}

	public View getLineView() {
		View view = LayoutInflater.from(BookStorePaperBookActivity.this).inflate(R.layout.lineview, null);
		return view;
	}

	public View getDividerView(int height) {
		View view = LayoutInflater.from(BookStorePaperBookActivity.this).inflate(R.layout.bookstore_divider, null);
		if (height != 8) {
			View divider = view.findViewById(R.id.divider);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.height = ScreenUtils.dip2px(BookStorePaperBookActivity.this, height);
			divider.setLayoutParams(params);
		}
		return view;
	}

	private boolean isRequest = false;
	public void requestData(final UrlParams tempParams) {
		if (isRequest) {
			return;
		}
		isRequest  = true;
		if (!NetWorkUtils.isNetworkConnected(this)) {
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			return;
		}
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);

		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, tempParams.params, new MyAsyncHttpResponseHandler(BookStorePaperBookActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				isCurrentRequestOver = true;
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

				String result = new String(responseBody);
				// MZLog.d("wangguodong", result);
				final BookStoreModuleBookListEntity entity = GsonUtils.fromJson(result, BookStoreModuleBookListEntity.class);

				View view = null;

				if (isReady || entity == null) {
					return;
				}

				if (entity.resultList != null) { // int
													// row=(int)
													// Math.floor(entity.resultList.size()/1.0);

					view = RankingListViewStyleController.getRankingListStyleView(BookStorePaperBookActivity.this, tempParams.showname, "更多", 10, 1,
							entity.resultList, new OnHeaderActionClickListener() {

								@Override
								public void onHeaderActionClick() {
									TalkingDataUtil.onBookStoreEvent(BookStorePaperBookActivity.this, "纸质商城", tempParams.showname);
									Intent intent = new Intent(BookStorePaperBookActivity.this, BookStoreBookListActivity.class);
									intent.putExtra(BookStoreBookListActivity.LIST_TYPE, BookStoreBookListActivity.TYPE_PAPER_BOOK);
									intent.putExtra("boolNew", tempParams.boolNew);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
								}
							});
				}

				if (null != view)
					containerLayout.addView(view);
				isCurrentRequestOver = true;
				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}
		});

	}

	public void requestOver() {

		Message msg = new Message();
		msg.what = MSG_REQUEST_OVER;
		if (handler != null)
			handler.sendMessage(msg);
	}

	@Override
	public void onPause() {
		super.onPause();
		isReady = true;
		WebRequestHelper.cancleRequest(BookStorePaperBookActivity.this);
	}

	@Override
	public void run() {

		while (!isReady) {
			try {
				if (isCurrentRequestOver) {
					requestOver();
				}
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
