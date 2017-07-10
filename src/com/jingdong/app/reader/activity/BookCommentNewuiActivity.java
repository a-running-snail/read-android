package com.jingdong.app.reader.activity;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineSearchBookActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineSearchPeopleActivity;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.share.WXShareHelper;
import com.jingdong.app.reader.view.BookCommentDialog;
import com.jingdong.app.reader.view.CustomProgreeDialog;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.SharePopupWindow;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;

public class BookCommentNewuiActivity extends BaseActivityWithTopBar implements
		TopBarViewListener {

	static public final String RatingValueKey = "RatingValueKey";
	static public final String TitleKey = "TitleKey";
	static public final String BookIdKey = "BookIdKey";
	public static final int SEARCH_BOOK = 711;
	public static final int SEARCH_PEOPLE = 811;
	private long bookid = -1;
	private String bookname = "";
	private RatingBar bookRatingBar = null;
	private TextView rating_mean;
	private EditText bookcomment = null;
	private ProgressDialog progressBar = null;
	private LinearLayout mentionLayout = null;
	
	private StringBuffer bookIdBuffer;
	private StringBuffer userIdBuffer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookcomment_newui);
		initTopBar();
		bookRatingBar = (RatingBar) findViewById(R.id.bookRating);
		bookcomment = (EditText) findViewById(R.id.bookCommentText);
		bookIdBuffer = new StringBuffer();
		userIdBuffer = new StringBuffer();
		
		mentionLayout = (LinearLayout) findViewById(R.id.mention_book);
		mentionLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						BookCommentNewuiActivity.this,
						TimelineSearchBookActivity.class);
				startActivityForResult(intent, SEARCH_BOOK);
			}
		});

		findViewById(R.id.timeline_tweet_at).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(
								BookCommentNewuiActivity.this,
								TimelineSearchPeopleActivity.class);
						startActivityForResult(intent, SEARCH_PEOPLE);
					}
				});

		// 限制输入字数长度
		final int mMaxLenth = 2000;
		bookcomment.addTextChangedListener(new TextWatcher() {
			private int cou = 0;
			int selectionEnd = 0;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				cou = before + count;
				// 修正光标一直在最后的问题
				// bookcomment.setSelection(bookcomment.length());
				cou = bookcomment.length();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {

				if (cou > mMaxLenth) {
					selectionEnd = bookcomment.length();
					s.delete(mMaxLenth, selectionEnd);
					Toast.makeText(BookCommentNewuiActivity.this, "已达到最大字数限制",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		rating_mean = (TextView) findViewById(R.id.rating_mean);
		float bookRating = getIntent().getFloatExtra(RatingValueKey, 0);
		bookid = getIntent().getLongExtra(BookIdKey, 0);
		bookname = getIntent().getStringExtra("bookname");
		bookRatingBar.setRating(bookRating);
		progressBar = CustomProgreeDialog.instance(
				BookCommentNewuiActivity.this, "正在发布信息，请稍候...");

		if (bookid == 0)
			return;

		bookRatingBar
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

					@Override
					public void onRatingChanged(RatingBar arg0, float rating,
							boolean arg2) {
						// TODO Auto-generated method stub
						if ((int) rating == 1) {
							rating_mean.setTextColor(getResources().getColor(
									R.color.text_main));
							rating_mean.setText("不知所云");
						} else if ((int) rating == 2) {
							rating_mean.setTextColor(getResources().getColor(
									R.color.text_main));
							rating_mean.setText("差强人意");
						} else if ((int) rating == 3) {
							rating_mean.setTextColor(getResources().getColor(
									R.color.text_main));
							rating_mean.setText("马马虎虎");
						} else if ((int) rating == 4) {
							rating_mean.setTextColor(getResources().getColor(
									R.color.text_main));
							rating_mean.setText("推荐阅读");
						} else if ((int) rating == 5) {
							rating_mean.setTextColor(getResources().getColor(
									R.color.text_main));
							rating_mean.setText("非读不可");
						}
					}
				});
	}

	public void initTopBar() {

		getTopBarView().setLeftMenuVisiable(true, "取消", R.color.red_main);
		getTopBarView().setRightMenuOneVisiable(true, "发布", R.color.red_main,
				false);
		getTopBarView().setListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case SEARCH_BOOK:
			if (resultCode == TimelineSearchBookActivity.SELECTED_BOOK) {
				String id = data.getStringExtra("book_id");
				String name = data.getStringExtra("book_name");
				bookIdBuffer.append(id + ",");
				String key = "《" + name + "》";
				bookcomment.append(key);
			}
			break;


		case SEARCH_PEOPLE:
			if (resultCode == TimelineSearchPeopleActivity.CLICK_USER_NAME) {
				String userName = data
						.getStringExtra(TimelineSearchPeopleActivity.USER_NAME);
				
				String userid = data.getStringExtra("userid");
				userIdBuffer.append(userid + ",");
				
				bookcomment.append("@");
				bookcomment.append(userName);
				bookcomment.append(" ");
			}

			break;
		}

	}
	
	
	@Override
	public void onLeftMenuClick() {
		finish();
	}

	@Override
	public void onRightMenuOneClick() {

		String rating = bookRatingBar.getRating() + "";
		String content = bookcomment.getText().toString();

		if (content.length() < 5) {
			Toast.makeText(BookCommentNewuiActivity.this, "书评内容不能少于5个字!",
					Toast.LENGTH_LONG).show();
			return;
		}
		progressBar.show();
		WebRequestHelper.post(URLText.bookComment, RequestParamsPool
				.writeBookCommentsParams(bookid + "", rating, content,userIdBuffer.toString()), true,
				new MyAsyncHttpResponseHandler(BookCommentNewuiActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(BookCommentNewuiActivity.this,
								"发布书评失败，请检查网络!", Toast.LENGTH_LONG).show();
						if (progressBar != null && progressBar.isShowing())
							progressBar.dismiss();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String resultString = new String(responseBody);
						MZLog.d("wangguodong", "aaacode=" + resultString);

						if (progressBar != null && progressBar.isShowing())
							progressBar.dismiss();

						try {
							JSONObject object = new JSONObject(resultString);
							int code = object.optInt("code");
							if (code == 0) {
								// Toast.makeText(BookCommentNewuiActivity.this,
								// "书评发布成功", Toast.LENGTH_LONG).show();
								commentGetScore();// 领取积分
								// 选择分享

								StringBuilder stringBuilder = new StringBuilder();

								stringBuilder.append('我');
								stringBuilder
										.append(getString(R.string.atJdRead));
								stringBuilder.append(' ');

								stringBuilder
										.append(getString(R.string.postBookComment));
								stringBuilder.append(' ');

								stringBuilder.append(bookname);
								stringBuilder.append(' ');
								final int total = 5;
								int times;
								if ((times = (int) bookRatingBar.getRating()) > 0) {
									final char star = '\u2605', unstar = '\u2606';
									for (int i = 0; i < times; i++) {
										stringBuilder.append(star);
									}
									for (int i = 0; i < (total - times); i++) {
										stringBuilder.append(unstar);
									}
									stringBuilder.append(' ');
								}

								stringBuilder.append(bookcomment.getText()
										.toString());

								String shareString = stringBuilder.toString();

								if (TextUtils.isEmpty(shareString))
									shareString = "";
								final String descripString = shareString;

								final String titleString = descripString;
								final String url = "http://e.m.jd.com/edm.html";
								// weixin

								StringBuilder stringBuilder11 = new StringBuilder();
								stringBuilder11.append("我 ");
								stringBuilder11
										.append(getString(R.string.postBookCommentNew));

								final String weixinImgTitleString = stringBuilder11
										.toString();
								final String weixinImgBookNameString = bookname;
								final float weixinImgRatingString = bookRatingBar
										.getRating();
								final String weixinImgContent1String = bookcomment
										.getText().toString();
								final String weixinImgcontentString = "";
								final String weixinImgDigestString = "";

								final SharePopupWindow sharePopupWindow = new com.jingdong.app.reader.view.SharePopupWindow(
										BookCommentNewuiActivity.this, true);
								Dialog dialog = new BookCommentDialog(
										BookCommentNewuiActivity.this,
										new BookCommentDialog.OnCustomDialogItemClickListener() {

											@Override
											public void onCustomDialogItemClick(
													int type) {

												switch (type) {
												case 101:// 微博
													sharePopupWindow
															.shareToWeibo(BookCommentNewuiActivity.this,
																	titleString,
																	descripString,
																	"", url,
																	"");
													finish();
													break;
												case 102:// 好友
													startShareBitmap(
															weixinImgTitleString,
															weixinImgBookNameString,
															weixinImgContent1String,
															weixinImgcontentString,
															weixinImgDigestString,
															weixinImgRatingString,
															sharePopupWindow, 0);
													finish();
													break;
												case 103:// 朋友圈
													startShareBitmap(
															weixinImgTitleString,
															weixinImgBookNameString,
															weixinImgContent1String,
															weixinImgcontentString,
															weixinImgDigestString,
															weixinImgRatingString,
															sharePopupWindow, 1);

													finish();
													break;
												default:
													finish();
													break;

												}

											}
										});

								dialog.show();

							} else {
								String message = object.getString("message");
								if (TextUtils.isEmpty(message)) {

									Toast.makeText(
											BookCommentNewuiActivity.this,
											"书评发布失败，请重试！", Toast.LENGTH_LONG)
											.show();
								} else {
									Toast.makeText(
											BookCommentNewuiActivity.this,
											message, Toast.LENGTH_LONG).show();

								}
								finish();
							}
						} catch (Exception e) {
							e.printStackTrace();
							finish();
						}

					}
				});

	}

	public void startShareBitmap(String title, String bookName,
			String content1, String content, String digest, float rating,
			SharePopupWindow sharePopupWindow, int type) {

		View view = LayoutInflater.from(BookCommentNewuiActivity.this).inflate(
				R.layout.timeline_share_layout, null);

		TextView tiTextView = (TextView) view.findViewById(R.id.title);
		TextView bookNameTextView = (TextView) view.findViewById(R.id.bookName);
		TextView content1TextView = (TextView) view.findViewById(R.id.content1);
		TextView contentTextView = (TextView) view.findViewById(R.id.content);
		TextView digestTextView = (TextView) view.findViewById(R.id.digest);
		RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating);

		tiTextView.setText(title);

		if (TextUtils.isEmpty(bookName))
			bookNameTextView.setVisibility(View.GONE);
		else {
			bookNameTextView.setText(bookName);
		}

		if (TextUtils.isEmpty(content1))
			content1TextView.setVisibility(View.GONE);
		else {
			content1TextView.setText(content1);
		}

		if (TextUtils.isEmpty(content))
			contentTextView.setVisibility(View.GONE);
		else {
			contentTextView.setText(content);
		}

		if (TextUtils.isEmpty(digest))
			digestTextView.setVisibility(View.GONE);
		else {
			digestTextView.setText(digest);
		}

		if (Double.isNaN(rating) || rating <= 0)
			ratingBar.setVisibility(View.GONE);
		else {
			ratingBar.setRating(rating);
		}

		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Bitmap.Config.RGB_565);
		view.draw(new Canvas(bitmap));

		// 获得分享的图片
		// 测试分享的图片
		// ImageView imageView=new ImageView(TimelineTweetActivity.this);
		// imageView.setImageBitmap(bitmap);
		// WindowManager mWm =
		// (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		// WindowManager.LayoutParams mParams = new
		// WindowManager.LayoutParams();
		// mWm.addView(imageView, mParams);
		//

		// 分享的图片
//		sharePopupWindow.weixinImg(BookCommentNewuiActivity.this, bitmap, type);// 0
																				// 好友
																				// 1
																				// 朋友圈
		WXShareHelper.getInstance().shareImage(this, bitmap, type);
	}

	/**
	 * 评论成功后领取积分
	 */
	protected void commentGetScore() {
		// TODO Auto-generated method stub
		IntegrationAPI.commentGetScore(this, bookid, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
//				CustomToast.showToast(BookCommentNewuiActivity.this,
//						"书评发布成功，成功领取" + score + "积分");
				
				String scoreInfo = "书评发布成功，成功领取" + score.getGetScore() + "积分";
				SpannableString span = new SpannableString(scoreInfo);
				int start = 11;
				int end = start + String.valueOf(score.getGetScore()).length();
				span.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				CustomToast.showToast(BookCommentNewuiActivity.this, span);
			}

			@Override
			public void onGrandFail() {
				MZLog.e("J", "评论获取积分失败");
			}
		});
	}

}
