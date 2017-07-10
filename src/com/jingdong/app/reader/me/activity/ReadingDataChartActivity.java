package com.jingdong.app.reader.me.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.mobstat.StatService;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.Legend.LegendPosition;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.UserInfo;
import com.jingdong.app.reader.me.fragment.BookListFragment;
import com.jingdong.app.reader.me.model.BookListModel;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.oauth.Constants;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.SharePopupWindowReadingData;
import com.jingdong.app.reader.view.SharePopupWindowReadingData.onPopupWindowItemClickListener;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ReadingDataChartActivity extends BaseActivityWithTopBar implements
		OnChartValueSelectedListener, onPopupWindowItemClickListener{

	public static final String UrlKey = "UrlKey";
	public static final String NameKey = "NameKey" ;
	public static final String UserId = "UserId" ;

	private RoundNetworkImageView avatar;
	private PieChart bookCategoryChart;
	private PieChart readChart;
	private PieChart noteChart;
	private TextView bookTotalView;
	private TextView readBookCountView;
	private TextView noteTotalView;
	private TextView shareNoteCountView;
	private TextView bookCategoryNoDataView;
	private TextView bookCommentCountView;
	private TextView bookShareCountView;
	private TextView readTotalTimeView;
	private TextView regDataView;
	private TextView UserNameView;
	private LinearLayout bookCategoryLayout;
	private int bookTotal = 0;
	private int readBookCount = 0;
	private int noteTotal = 0;
	private int shareNoteCount = 0;
	private int shareBookCount = 0;
	private boolean isMZDataReady = false;
	private boolean isJDDataReady = false;
	private SharePopupWindowReadingData sharePopupWindow = null;
	public IWeiboShareAPI mWeiboShareAPI= null;
	private ScrollView scrollView;
	private Bitmap bitmap;
	private String userId;

	private ArrayList<Entry> readChartEntry = new ArrayList<Entry>();
	private ArrayList<Entry> noteChartEntry = new ArrayList<Entry>();
	private ArrayList<Entry> bookCategoryChartEntry = new ArrayList<Entry>();
	private List<BookCategory> bookCategoryList = new ArrayList<BookCategory>();
	static File fileDir = null;
	static FileGuider savePath;
	private LinearLayout readedLinearLayout;
	private LinearLayout noteLinearLayout;
	
	private int[] imageResIds = { R.drawable.icon_dataviz_literature,
			R.drawable.icon_dataviz_internet, R.drawable.icon_dataviz_fiction,
			R.drawable.icon_dataviz_economicmanagement,
			R.drawable.icon_dataviz_history, R.drawable.icon_dataviz_art,
			R.drawable.icon_dataviz_foodandlife,
			R.drawable.icon_dataviz_medicine, R.drawable.icon_dataviz_others };

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_reading_data_chart);
		this.getTopBarView().setTitle(
				this.getString(R.string.activity_reading_data_title));
		this.getTopBarView().setRightMenuOneVisiable(true,
				R.drawable.btn_bar_share, false);
		avatar = (RoundNetworkImageView) findViewById(R.id.thumb_nail);
		UserNameView=(TextView) findViewById(R.id.user_nickname);
		bookTotalView = (TextView) findViewById(R.id.book_total);
		noteTotalView = (TextView) findViewById(R.id.note_total);
		readBookCountView = (TextView) findViewById(R.id.read_over_count);
		shareNoteCountView = (TextView) findViewById(R.id.note_share_count);
		bookCategoryNoDataView = (TextView) findViewById(R.id.book_category_no_data);
		bookCommentCountView = (TextView) findViewById(R.id.book_comment_count);
		bookShareCountView = (TextView) findViewById(R.id.book_share_count);
		readTotalTimeView = (TextView) findViewById(R.id.read_total_time);
		regDataView = (TextView) findViewById(R.id.reg_day);
		bookCategoryLayout = (LinearLayout) findViewById(R.id.book_category_layout);
		scrollView = (ScrollView) findViewById(R.id.scrollView);

		readChart = (PieChart) findViewById(R.id.read_chart);
		noteChart = (PieChart) findViewById(R.id.note_chart);
		readChartEntry.add(new Entry(0, 0));
		readChartEntry.add(new Entry(100, 1));
		noteChartEntry.add(new Entry(0, 0));
		noteChartEntry.add(new Entry(100, 1));
		setupSmallChart(readChart);
		setupSmallChart(noteChart);
		setReadData(readChart, readChartEntry);
		setReadData(noteChart, noteChartEntry);

		bookCategoryChart = (PieChart) findViewById(R.id.book_category_chart);
		setupBookCategoryChart(bookCategoryChart);
		bookCategoryChartEntry.add(new Entry(0, 0));
		bookCategoryChartEntry.add(new Entry(100, 1));
		setBookCategoryData(bookCategoryChart, bookCategoryChartEntry, false);

		String imgUrl = getIntent().getStringExtra(UrlKey);
		String nickname= getIntent().getStringExtra(NameKey);
		userId = getIntent().getStringExtra(UserId);
		if (TextUtils.isEmpty(imgUrl) || TextUtils.isEmpty(nickname)) {
			getUserInfo();
		} else {
			setupAvatar(imgUrl);
			UserNameView.setText(nickname);
		}

		//我读过的图书
		readedLinearLayout = (LinearLayout) findViewById(R.id.readedLinearLayout);
		readedLinearLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ReadingDataChartActivity.this, UserReadingBookActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		//我写下的笔记
		noteLinearLayout = (LinearLayout) findViewById(R.id.myNoteLinearLayout);
		noteLinearLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ReadingDataChartActivity.this,
						BookListActivity.class);
				intent.putExtra(BookListFragment.BOOKLIST_TYPE,
						BookListModel.NOTES_BOOKS);
				intent.putExtra("user_id", userId);
				startActivity(intent);
			}
		});
		
		requestMZStatistic();
		requestJDReadStatistic();
	}

	private void setupSmallChart(PieChart chart) {
		chart.setHoleColorTransparent(true);
		chart.setHoleRadius(90f);
		chart.setDescription("");
		chart.setDrawCenterText(false);
		chart.setDrawHoleEnabled(true);
		// chart.setRotationAngle(0);//确定开始角度
		chart.setDrawXValues(false);
		chart.setDrawYValues(false);
		// enable rotation of the chart by touch
		chart.setRotationEnabled(false);
		// display percentage values
		chart.setUsePercentValues(true);
		// add a selection listener
		chart.setOnChartValueSelectedListener(this);
		// mChart.setTouchEnabled(false);
		chart.animateXY(1500, 1500);
	}

	private void setupBookCategoryChart(PieChart chart) {
		chart.setHoleColorTransparent(true);
		chart.setHoleRadius(90f);
		chart.setDescription("");
		chart.setDrawCenterText(false);
		chart.setDrawHoleEnabled(true);
		// chart.setRotationAngle(0);//确定开始角度
		chart.setDrawXValues(false);
		chart.setDrawYValues(false);
		// enable rotation of the chart by touch
		chart.setRotationEnabled(false);
		// display percentage values
		chart.setUsePercentValues(true);
		// add a selection listener
		chart.setOnChartValueSelectedListener(this);
		// mChart.setTouchEnabled(false);
		chart.animateXY(1500, 1500);
	}

	private void setReadData(PieChart chart, ArrayList<Entry> yValueList) {
		ArrayList<String> xValues = new ArrayList<String>();

		for (int i = 0; i < yValueList.size(); i++)
			xValues.add("" + i);

		PieDataSet set1 = new PieDataSet(yValueList, "Election Results");
		set1.setSliceSpace(1f);

		// add a lot of colors

		ArrayList<Integer> colors = new ArrayList<Integer>();

		Resources res = this.getResources();
		colors.add(res.getColor(R.color.pie_chart_red));
		colors.add(res.getColor(R.color.pie_chart_light_gray));

		set1.setColors(colors);

		PieData data = new PieData(xValues, set1);
		chart.setData(data);
		chart.setDrawLegend(false);
		Legend l = chart.getLegend();
		l.setPosition(LegendPosition.NONE);

		// undo all highlights
		chart.highlightValues(null);

		chart.invalidate();
	}

	private void setBookCategoryData(PieChart chart,
			ArrayList<Entry> yValueList, boolean isDataReady) {
		ArrayList<String> xVals = new ArrayList<String>();

		for (int i = 0; i < yValueList.size(); i++)
			xVals.add("" + i);

		PieDataSet set1 = new PieDataSet(yValueList, "Election Results");
		set1.setSliceSpace(1f);

		// add a lot of colors

		ArrayList<Integer> colors = new ArrayList<Integer>();

		Resources res = this.getResources();
		switch (yValueList.size()) {
		case 9:
			colors.add(res.getColor(R.color.pie_chart_dark_gray));
		case 8:
			colors.add(res.getColor(R.color.pie_chart_magenta));
		case 7:
			colors.add(res.getColor(R.color.pie_chart_violet));
		case 6:
			colors.add(res.getColor(R.color.pie_chart_blue));
		case 5:
			colors.add(res.getColor(R.color.pie_chart_cyan));
		case 4:
			colors.add(res.getColor(R.color.pie_chart_green));
		case 3:
			colors.add(res.getColor(R.color.pie_chart_yellow));
		case 2:
			colors.add(res.getColor(R.color.pie_chart_orange));
		case 1:
			colors.add(res.getColor(R.color.pie_chart_red));
		}

		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i = colors.size() - 1; i >= 0; i--) {
			temp.add(colors.get(i));
		}
		colors = temp;

		if (!isDataReady) {
			colors.clear();
			colors.add(res.getColor(R.color.pie_chart_light_gray));
		}

		set1.setColors(colors);

		PieData data = new PieData(xVals, set1);
		chart.setData(data);
		chart.setDrawLegend(false);
		Legend l = chart.getLegend();
		l.setPosition(LegendPosition.NONE);

		// undo all highlights
		chart.highlightValues(null);

		chart.invalidate();
	}

	@Override
	public void onValueSelected(Entry e, int dataSetIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub

	}

	private void setupAvatar(String imgUrl) {
		if (TextUtils.isEmpty(imgUrl)) {
			return;
		}
		ImageLoader.getInstance().displayImage(imgUrl, avatar,
				GlobalVarable.getDefaultAvatarDisplayOptions(false));
	}

	private synchronized void setupReadChart() {
		if (isMZDataReady && isJDDataReady) {
			readChartEntry.clear();
			readChartEntry.add(new Entry(readBookCount, 0));
			if (bookTotal > 0 && bookTotal >= readBookCount) {
				readChartEntry.add(new Entry(bookTotal - readBookCount, 1));
			} else {
				readChartEntry.add(new Entry(1, 1));
			}
			setReadData(readChart, readChartEntry);
			isMZDataReady = false;
			isJDDataReady = false;
		}
	}

	private synchronized void setupNoteChart() {
		noteChartEntry.clear();
		noteChartEntry.add(new Entry(shareNoteCount, 0));
		if (noteTotal > 0 && noteTotal >= shareNoteCount) {
			noteChartEntry.add(new Entry(noteTotal - shareNoteCount, 1));
		} else {
			noteChartEntry.add(new Entry(1, 1));
		}
		setReadData(noteChart, noteChartEntry);
	}

	private synchronized void setupBookCategoryChart() {
		if (bookCategoryList.size() == 0) {
			return;
		}
		bookCategoryChartEntry.clear();
		int index = 0;
		for (BookCategory cate : bookCategoryList) {
			bookCategoryChartEntry.add(new Entry(cate.bookCount, index));
			index++;
		}
		setBookCategoryData(bookCategoryChart, bookCategoryChartEntry, true);
		bookCategoryNoDataView.setVisibility(View.GONE);
		int row = bookCategoryList.size() / 3;
		int counts = bookCategoryList.size() % 3;
		if (counts > 0) {
			row++;
		}
		int n = counts == 0 ? row : row - 1;
		List<View> legendList = new ArrayList<View>();
		for (int i = 0; i < n; i++) {
			View child = View.inflate(this,
					R.layout.item_book_category_chart_legend, null);
			bookCategoryLayout.addView(child);
			legendList.add(child.findViewById(R.id.legend_1));
			legendList.add(child.findViewById(R.id.legend_2));
			legendList.add(child.findViewById(R.id.legend_3));
		}
		if (counts == 1) {
			View child = View.inflate(this,
					R.layout.item_book_category_chart_legend, null);
			child.findViewById(R.id.legend_1).setVisibility(View.INVISIBLE);
			child.findViewById(R.id.legend_3).setVisibility(View.INVISIBLE);
			bookCategoryLayout.addView(child);
			legendList.add(child.findViewById(R.id.legend_2));
		} else if (counts == 2) {
			View child = View.inflate(this,
					R.layout.item_book_category_chart_legend, null);
			child.findViewById(R.id.legend_2).setVisibility(View.INVISIBLE);
			bookCategoryLayout.addView(child);
			legendList.add(child.findViewById(R.id.legend_1));
			legendList.add(child.findViewById(R.id.legend_3));
		}

		if (bookCategoryList.size() == legendList.size()) {
			index = 0;
			for (View view : legendList) {
				ViewGroup group = (ViewGroup) view;
				BookCategory cate = bookCategoryList.get(index);
				ImageView img = (ImageView) group.getChildAt(0);
				img.setImageResource(cate.imageResId);
				TextView countText = (TextView) group.getChildAt(1);
				countText.setText(String.valueOf(cate.bookCount));
				TextView nameText = (TextView) group.getChildAt(2);
				nameText.setText(cate.name);
				index++;
			}
		}

	}

	private void requestMZStatistic() {
		if (!LoginUser.isLogin()) {
			return;
		}
		if (!NetWorkUtils.isNetworkConnected(this)) {
			return;
		}
		RequestParams request = RequestParamsPool.getMZReadStatistics();
		WebRequestHelper.get(URLText.readStatistic, request, true,
				new MyAsyncHttpResponseHandler(this) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {
							String response = new String(responseBody, "utf-8");
							JSONObject obj = new JSONObject(response);
							noteTotal = obj.optInt("notes_count");
							readBookCount = obj.optInt("read_books_count");
							shareNoteCount = obj.optInt("share_notes_count");
							shareBookCount = obj.optInt("share_books_count");
							int readingTime = obj.optInt("reading_length");
							int bookCommentCount = obj
									.optInt("book_comments_count");
							String str = getString(R.string.reading_data_read_time);
							float time = readingTime / 3600f;
							String timeText = String.format(
									Locale.getDefault(), "%.1f", time);
							readTotalTimeView.setText(String.format(str,
									timeText));
							str = getString(R.string.reading_data_book_share_count);
							bookShareCountView.setText(String.format(str,
									shareBookCount));
							str = getString(R.string.reading_data_book_comment_count);
							bookCommentCountView.setText(String.format(str,
									bookCommentCount));
							shareNoteCountView.setText(String
									.valueOf(shareNoteCount));
							readBookCountView.setText(String
									.valueOf(readBookCount));
							noteTotalView.setText(String.valueOf(noteTotal));
							isMZDataReady = true;
							setupNoteChart();
							setupReadChart();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {

					}
				});
	}

	private void requestJDReadStatistic() {
		if (!LoginUser.isLogin()) {
			return;
		}
		if (!NetWorkUtils.isNetworkConnected(this)) {
			return;
		}
		RequestParams request = RequestParamsPool.getJDReadStatistics();
		WebRequestHelper.post(URLText.JD_BASE_URL, request, true,
				new MyAsyncHttpResponseHandler(this) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {
							String response = new String(responseBody, "utf-8");
							JSONObject obj = new JSONObject(response);
							String code = obj.optString("code");
							if ("0".equals(code)) {
								bookTotal = obj.optInt("totalCount");
								String regStr = obj
										.optString("firstAccessTime");
								String currentTimeStr = obj.optString("currentTime");
								JSONArray array = obj
										.optJSONArray("resultList");
								ArrayList<BookCategory> cateList = new ArrayList<BookCategory>();
								for (int i = 0; i < array.length(); i++) {
									BookCategory cate = BookCategory
											.fromJSON(array.getJSONObject(i));
									cateList.add(cate);
								}
								Collections.sort(cateList,
										new Comparator<BookCategory>() {

											@Override
											public int compare(
													BookCategory lhs,
													BookCategory rhs) {
												if (lhs.bookCount < rhs.bookCount)
													return 1;
												else if (lhs.bookCount > rhs.bookCount) {
													return -1;
												} else
													return 0;
											}
										});
								for (int i = 0; i < cateList.size(); i++) {
									BookCategory cate = cateList.get(i);
									if (i < 8) {
										cate.imageResId = imageResIds[i];
										bookCategoryList.add(cate);
									} else if (i == 8) {
										cate.name = "其他";
										cate.imageResId = imageResIds[8];
										bookCategoryList.add(cate);
									} else {
										BookCategory cate1 = bookCategoryList
												.get(8);
										cate1.bookCount += cate.bookCount;
										bookCategoryList.set(8, cate1);
									}
								}
								bookTotalView.setText(String.valueOf(bookTotal));
								long regTime = TimeFormat
										.formatStringTime(regStr);
								long currentTime = TimeFormat.formatStringTime(currentTimeStr);
								if (regTime > 0) {
									regTime = (currentTime - regTime)
											/ (24 * 60 * 60 * 1000);
									String str = getString(R.string.reading_data_reg_day);
									regDataView.setText(String.format(str,
											regTime));
								}
							}
							isJDDataReady = true;
							setupReadChart();
							setupBookCategoryChart();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {

					}
				});
	}

	private void getUserInfo() {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getUserInfoParams(LoginUser.getpin()), true,
				new MyAsyncHttpResponseHandler(this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						UserInfo userinfo = GsonUtils.fromJson(result,
								UserInfo.class);
						if (userinfo != null) {
							String imgUrl = userinfo.getList().get(0)
									.getYunSmaImageUrl();
							setupAvatar(imgUrl);
							UserNameView.setText(userinfo.getList().get(0).getNickName());
						} else
							Toast.makeText(ReadingDataChartActivity.this,
									getString(R.string.network_connect_error),
									Toast.LENGTH_LONG).show();
					}

				});

	}

	static class BookCategory {
		public int bookCount = 0;
		public int imageResId = 0;
		public String name = null;

		static BookCategory fromJSON(JSONObject json) {
			BookCategory cate = new BookCategory();
			cate.name = json.optString("name");
			cate.bookCount = json.optInt("bookCount");
			return cate;
		}
	}

	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub
		super.onRightMenuOneClick();
		TalkingDataUtil.onBookDetailEvent(this, "分享浮窗-打开");
		sharePopupWindow = new SharePopupWindowReadingData(ReadingDataChartActivity.this);
		//sharePopupWindow.setWeiboShareAPI(mWeiboShareAPI);
		sharePopupWindow.setListener(this);
		sharePopupWindow.show(getTopBarView().getSubmenurightOneImage());
	}

	/**
	 * 截取scrollview的屏幕
	 * **/
	public static Bitmap getBitmapByView(ScrollView scrollView) {
		int h = 0;
		Bitmap bitmap = null;
		// 获取listView实际高度
		for (int i = 0; i < scrollView.getChildCount(); i++) {
			h += scrollView.getChildAt(i).getHeight();
			scrollView.getChildAt(i).setBackgroundResource(R.drawable.bg_sub);
		}
		Log.d("cj", "实际高度:" + h);
		Log.d("cj", " 高度:" + scrollView.getHeight());
		// 创建对应大小的bitmap
		bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
				Bitmap.Config.RGB_565);

		Log.d("cj", "ddddd=========>>" + bitmap.getByteCount());
		final Canvas canvas = new Canvas(bitmap);
		scrollView.draw(canvas);
		// 测试输出
		FileOutputStream out = null;
		try {
			savePath = new FileGuider(
					FileGuider.SPACE_PRIORITY_EXTERNAL);
			savePath.setImmutable(true);
			savePath.setChildDirName("/Image/" + "share" + "." + "JPG");

			try {
				fileDir = new File(savePath.getParentPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			out = new FileOutputStream(fileDir);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			if (null != out) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			// TODO: handle exception
		}
//		Bitmap bitmap2 = decodeBitmap();
		return bitmap;
	}
	
	// 缩放图片
	public static Bitmap decodeBitmap()  
    {  
        BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        // 通过这个bitmap获取图片的宽和高       
        String dir = "Image";
        String name = "share" + "." + "PNG";
        String filepath = FileGuider.getPath(FileGuider.SPACE_PRIORITY_EXTERNAL, dir, name);
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);  

        float realWidth = options.outWidth;  
        float realHeight = options.outHeight;  
        // 计算缩放比         
        int scale = (int) ((realHeight > realWidth ? realHeight : realWidth) / 100);  
        if (scale <= 0)  
        {  
            scale = 1;  
        }  
        options.inSampleSize = 2;  
        options.inJustDecodeBounds = false;  
        // 注意这次要把options.inJustDecodeBounds 设为 false,这次图片是要读取出来的。        
        bitmap = BitmapFactory.decodeFile(filepath, options);  
        return bitmap;  
    }  
	
	@Override
	public void onPopupWindowMore() {
		// TODO Auto-generated method stub
		TalkingDataUtil.onBookDetailEvent(this, "分享-更多");
		bitmap = getBitmapByView(scrollView);
		sharePopupWindow.More(ReadingDataChartActivity.this, "我的阅历", "",
				bitmap, "http://e.m.jd.com");
	}


	@Override
	public void onPopupWindowWeixinClick() {
		// TODO Auto-generated method stub
		TalkingDataUtil.onBookDetailEvent(this, "分享-微信好友");
		bitmap = getBitmapByView(scrollView);
		if (bitmap != null) {
			sharePopupWindow.weixinImg(ReadingDataChartActivity.this, bitmap, 0);
			// post(LoginUser.getpin(),String.valueOf(bookInfo.detail.bookId));
		}

	}

	@Override
	public void onPopupWindowSinaClick() {
		TalkingDataUtil.onBookDetailEvent(this, "分享-新浪微博");
		// TODO Auto-generated method stub
		bitmap = getBitmapByView(scrollView);
		if (bitmap != null) {
			if (mWeiboShareAPI == null) {
				mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this,Constants.APP_KEY);
			}
			mWeiboShareAPI.registerApp();
			sharePopupWindow.setWeiboShareAPI(mWeiboShareAPI);
			sharePopupWindow.sina(ReadingDataChartActivity.this, "我的阅历", "",
					bitmap, "http://e.m.jd.com", "");
			// post(LoginUser.getpin(),String.valueOf(bookInfo.detail.bookId));
		}
	}

	@Override
	public void onPopupWindowWeixinFriend() {
		// TODO Auto-generated method stub
		TalkingDataUtil.onBookDetailEvent(this, "分享-微信朋友圈");
		bitmap = getBitmapByView(scrollView);
		if (bitmap != null) {
			sharePopupWindow.weixinImg(ReadingDataChartActivity.this, bitmap, 1);
		}
	}

	@Override
	public void onPopupWindowCancel() {
		// TODO Auto-generated method stub
		TalkingDataUtil.onBookDetailEvent(this, "分享浮窗-关闭");
		sharePopupWindow.dismiss();
	}

	/**
	 * @param urlpath
	 * @return Bitmap 根据图片url获取图片对象
	 */
	public static Bitmap getBitMBitmap(String urlpath) {
		Bitmap map = null;
		try {
			URL url = new URL(urlpath);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream in;
			in = conn.getInputStream();
			map = BitmapFactory.decodeStream(in);
			// TODO Auto-generated catch block
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_yueli));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_yueli));
	}
	
}
