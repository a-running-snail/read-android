package com.jingdong.app.reader.timeline.actiivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookCommentNewuiActivity;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.DraftsActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.DraftsActivity.Draft;
import com.jingdong.app.reader.activity.DraftsActivity.Drafts;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.util.share.WXShareHelper;
import com.jingdong.app.reader.view.BookCommentDialog;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.SharePopupWindow;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tendcloud.tenddata.TCAgent;

public class TimelineBookListCommentsActivity extends BaseActivityWithTopBar {

	private final static int MAX_COMMENT_TEXT = 2000;
	public static final String ENTITY_GUID = "entityGuid";
	public static final String IS_COMMENT = "isComment";
	public static final String ORIGIN_CONTENT = "originContent";
	public static final String REPLY_TO = "replyTo";
	public static final String USER_COMMENT = "user_comment";
	public static final String CHECKED = "checked";
	private EditText userComment;
	private CheckBox checkBox;
	private String type;
	private RatingBar rating;

	public static final int SEARCH_BOOK = 711;
	public static final int SEARCH_PEOPLE = 811;
	public static final int COMMENTS_BOOK = 911;
	public static final int BOOK_COMMENT = 1111;
	private StringBuffer bookIdBuffer;
	private StringBuffer userIdBuffer;
	
	private ProgressDialog mypDialog;

	private int book_id = -1;

	private String guid = "";

	private TextView rating_mean;

	private TopBarView topBarView = null;
	private Draft draft = null;
	private LinearLayout mentionLayout = null;
	private LinearLayout header=null;

	String bookname="";
	String bookauthor="";
	String bookcover="";
	
	private RelativeLayout mBookLayout = null;
	private ImageView mBookImage = null;
	private TextView mBookName = null;
	private TextView mBookAuthor = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline_booklist_comments);
		
		mBookLayout = (RelativeLayout)findViewById(R.id.booklayout);
		mBookImage = (ImageView)findViewById(R.id.image);
		mBookName = (TextView)findViewById(R.id.mBookName);
		mBookAuthor = (TextView)findViewById(R.id.mBookAuthor);

		topBarView = getTopBarView();
		initTopBar();
		Intent intent = getIntent();
		draft = (Draft) getIntent().getSerializableExtra("draft");
		type = intent.getStringExtra("type");
		guid = intent.getStringExtra("guid");

		mypDialog = new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setIndeterminate(false);
		mypDialog.setMessage(getResources().getString(R.string.loading_data));
		mypDialog.setCancelable(true);

		userComment = (EditText) findViewById(R.id.timeline_comments_content);
		checkBox = (CheckBox) findViewById(R.id.forward_or_comment);
		rating = (RatingBar) findViewById(R.id.rating);
		rating_mean = (TextView) findViewById(R.id.rating_mean);
		header=(LinearLayout) findViewById(R.id.header);

		rating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

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
		rating.setRating(0f);
		bookIdBuffer = new StringBuffer();
		userIdBuffer = new StringBuffer();
		
		mentionLayout = (LinearLayout) findViewById(R.id.mention_book);
		mentionLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						TimelineBookListCommentsActivity.this,
						TimelineSearchBookActivity.class);
				startActivityForResult(intent, SEARCH_BOOK);
			}
		});

		findViewById(R.id.timeline_tweet_at).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(
								TimelineBookListCommentsActivity.this,
								TimelineSearchPeopleActivity.class);
						startActivityForResult(intent, SEARCH_PEOPLE);
					}
				});

		switchToActivity(type);
	}

	public void initTopBar() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, "取消", R.color.red_main);
		topBarView.setRightMenuOneVisiable(true, "发布", R.color.red_main, false);
		topBarView.setListener(this);
	}

	private void switchToActivity(String type) {

		if (type.equals(TimelineBookListActivity.type[0])) {
			// 转发
			checkBox.setVisibility(View.VISIBLE);
			rating.setVisibility(View.GONE);
			initTitleBottom(true);
			getTopBarView().setTitle(getString(R.string.forward_info));

		} else if (type.equals(TimelineBookListActivity.type[1])) {
			// 邀请
			checkBox.setVisibility(View.INVISIBLE);
			rating.setVisibility(View.GONE);
			getTopBarView().setTitle(getString(R.string.invited_to_answer));

			Intent intent = new Intent(TimelineBookListCommentsActivity.this,
					TimelineSearchPeopleActivity.class);
			startActivityForResult(intent, SEARCH_PEOPLE);

		} else if (type.equals(TimelineBookListActivity.type[2])) {
			// 推荐
			checkBox.setVisibility(View.INVISIBLE);
			rating.setVisibility(View.VISIBLE);
			Intent intent = new Intent(TimelineBookListCommentsActivity.this,
					TimelineSearchBookActivity.class);
			startActivityForResult(intent, COMMENTS_BOOK);
		} else if (type.equals(TimelineBookListActivity.type[3])) {
			// 书评
			checkBox.setVisibility(View.INVISIBLE);
			rating.setVisibility(View.VISIBLE);
			Intent intent = new Intent(TimelineBookListCommentsActivity.this,
					TimelineSearchBookActivity.class);
			startActivityForResult(intent, BOOK_COMMENT);

		} else if (type.equals(TimelineBookListActivity.type[4])) {
			// 发布书单
			checkBox.setVisibility(View.VISIBLE);
			initCheckBox(getString(R.string.timeline_booklist_only_myself));
			rating.setVisibility(View.GONE);
			getTopBarView().setTitle(getString(R.string.flow_window_lists));
			userComment.setHint(getString(R.string.timeline_booklist_hint));
		}

		else if (type.equals("share_to_comunity")) {
			// 分享到社区
			checkBox.setVisibility(View.INVISIBLE);
			rating.setVisibility(View.INVISIBLE);
			rating.setRating(0);
			mentionLayout.setVisibility(View.GONE);
			header.setVisibility(View.GONE);
			long id = getIntent().getLongExtra("book_id", -1);
			book_id=(int) id;
			bookname = getIntent().getStringExtra("book_name");
			bookIdBuffer.append(id + ",");
			getTopBarView().setTitle("分享");
			userComment.setHint("写一段分享理由吧...");

			String bookid = getIntent().getStringExtra("book_id");
			if (bookid != null && !bookid.equals(""))
				book_id = Integer.parseInt(bookid);
			bookname = getIntent().getStringExtra("book_name");
			bookauthor = getIntent().getStringExtra("book_author");
			bookcover = getIntent().getStringExtra("book_cover");
			mBookName.setText(bookname);
			mBookAuthor.setText("null".equals(bookauthor) ? getString(R.string.author_unknown) : bookauthor);
			if(isUrl(bookcover)){
				ImageLoader.getInstance().displayImage(bookcover, mBookImage, GlobalVarable.getCutBookDisplayOptions());
			}
		}
		else if (type.equals("draft")) {
			// 是草稿

			if (draft != null) {
				checkBox.setVisibility(View.INVISIBLE);
				userComment.setText(draft.content);
				bookIdBuffer.append(draft.bookidBuffer);
				userIdBuffer.append(draft.useridBuffer);
				rating.setRating(draft.rating);
				book_id = draft.book_id;
				getTopBarView().setTitle(draft.title);
				
				bookname = draft.forwardNickname;
				bookauthor = draft.forwardContent;
				bookcover = draft.forwardImage;
				mBookName.setText(bookname);
				mBookAuthor.setText("null".equals(bookauthor) ? getString(R.string.author_unknown) : bookauthor);
				if(isUrl(bookcover)){
					ImageLoader.getInstance().displayImage(bookcover, mBookImage, GlobalVarable.getCutBookDisplayOptions());
				}
			}
		}
		else if(type.equals("direct_comment")){
			checkBox.setVisibility(View.INVISIBLE);
			rating.setVisibility(View.VISIBLE);
			
			String id = getIntent().getStringExtra("book_id");
			if (id != null && !id.equals("")  )
				book_id = Integer.parseInt(id);
			bookname = getIntent().getStringExtra("book_name");
			bookauthor = getIntent().getStringExtra("book_author");
			bookcover = getIntent().getStringExtra("book_cover");
			bookIdBuffer.append(id + ",");
			getTopBarView().setTitle("书评");
			
			mBookName.setText(bookname);
			mBookAuthor.setText("null".equals(bookauthor) ? getString(R.string.author_unknown) : bookauthor);
			
			if(isUrl(bookcover)){
				ImageLoader.getInstance().displayImage(bookcover, mBookImage, GlobalVarable.getCutBookDisplayOptions());
			}
			
		}

	}

	// 发布动态 入口
	public void postComments(String type) {
		if (userComment.getText().length() < 5) {
			Toast.makeText(this, "书评内容不能少于5个字!", Toast.LENGTH_SHORT)
					.show();
			return;
		} 
		
		else if (userComment.getText().toString().length() > MAX_COMMENT_TEXT) {
			Toast.makeText(this, R.string.max_comment_text, Toast.LENGTH_SHORT)
					.show();
			return;
		} else {
			mypDialog.show();
			MZLog.d("temp","#####$@#$@#$#$W#%REWET^"+type);
			if (type.equals(TimelineBookListActivity.type[0])
					|| type.equals(TimelineBookListActivity.type[1])) {
				if (!guid.equals("")) {
					int state = 3;
					if (type.equals(TimelineBookListActivity.type[0])) {
						if (checkBox.isChecked()) {
							state = 2;
						}
					}

					RequestInvitedOrForward(guid, userComment.getText()
							.toString(), state);

				}

			}

			if (type.equals(TimelineBookListActivity.type[3])|| type.equals("direct_comment")
					|| type.equals("share_to_comunity")) {
				if (book_id != -1) {
					MZLog.d("temp","12#####$@#$@#$#$W#%REWET^");
					RequestPostBookCommets();
				}
				else {
					if(mypDialog.isShowing())
					  mypDialog.dismiss();
				}
			}

			if (type.equals("draft")) {
				if (book_id != -1) {
					MZLog.d("wangguodong", "#####!!!!!!!!11111111");
					if (draft != null) {
						mypDialog.dismiss();
						Bundle bundle = new Bundle();
						bundle.putLong("book_id", book_id);
						bundle.putString("content", userComment.getText()
								.toString());
						bundle.putFloat("rating", rating.getRating());
						bundle.putString("userids", userIdBuffer.toString());

						Intent it = new Intent();
						it.putExtra("data", bundle);
						setResult(DraftsActivity.TYPE_POST_BOOK_COMMENT, it);
						finish();
					}
				}
				
			}

		}

	}

	public void saveDraft() {

		String content = userComment.getText().toString();
		
			Toast.makeText(TimelineBookListCommentsActivity.this, "书评已经保存到草稿箱",
					Toast.LENGTH_LONG).show();
			Draft draft = new Draft();
			draft.type = DraftsActivity.TYPE_POST_BOOK_COMMENT;
			draft.content = content;
			draft.time = System.currentTimeMillis();
			draft.title=bookname;
			draft.bookidBuffer = bookIdBuffer.toString();
			draft.rating = rating.getRating();
			draft.book_id = book_id;
			
			//保存草稿 -- tmj
			draft.forwardImage = bookcover; //图书封面
			draft.forwardNickname = bookname;//图书名称
			draft.forwardContent = bookauthor;//图书作者

			List<Draft> list = LocalUserSetting
					.getDraftsList(TimelineBookListCommentsActivity.this);
			if (list == null)
				list = new ArrayList<Draft>();
			list.add(draft);

			Drafts drafts = new Drafts();
			drafts.drafts = list;
			LocalUserSetting.saveDraftsList(
					TimelineBookListCommentsActivity.this, drafts);

	}

	@Override
	public void onBackPressed() {
		
		String content = userComment.getText().toString();
		if (draft == null && !TextUtils.isEmpty(content)) {
		
		DialogManager.showCommonDialog(TimelineBookListCommentsActivity.this,"提示","是否保存到草稿箱?", "保存草稿", "不保存", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					saveDraft();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
				default:
					break;
				}
				dialog.dismiss();
				finish();
			}
		});
		}
		else {
			finish();
		}
		
	}

	public void RequestPostBookCommets() {

		WebRequestHelper.post(URLText.bookCommentUrl, RequestParamsPool
				.getRequestPostBookComments(book_id, userComment.getText()
						.toString(), rating.getRating(),userIdBuffer.toString()), true,
				new MyAsyncHttpResponseHandler(
						TimelineBookListCommentsActivity.this) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);

						try {
							JSONObject obj = new JSONObject(result);
							int code = obj.optInt("code");
							String msg = obj.optString("message");
							if (code == 0) {
								//Toast.makeText(
								//		TimelineBookListCommentsActivity.this,getString(R.string.post_book_comment_ok), Toast.LENGTH_LONG).show();
								
								commentGetScore();// 领取积分
								
								StringBuilder stringBuilder = new StringBuilder();

								stringBuilder.append('我');
								stringBuilder
										.append(getString(R.string.atJdRead));
								stringBuilder.append(' ');

								stringBuilder
										.append(getString(R.string.postBookComment));
								stringBuilder.append(' ');

								stringBuilder.append("《"+bookname+"》");
								stringBuilder.append(' ');
								final int total = 5;
								int times;
								if ((times = (int) rating.getRating()) > 0) {
									final char star = '\u2605', unstar = '\u2606';
									for (int i = 0; i < times; i++) {
										stringBuilder.append(star);
									}
									for (int i = 0; i < (total - times); i++) {
										stringBuilder.append(unstar);
									}
									stringBuilder.append(' ');
								}

								stringBuilder.append(userComment
										.getText().toString());
								
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
								final String weixinImgBookNameString = "《"+bookname+"》";
								final float weixinImgRatingString = rating
										.getRating();
								final String weixinImgContent1String = userComment
										.getText().toString();
								final String weixinImgcontentString = "";
								final String weixinImgDigestString = "";

								final SharePopupWindow sharePopupWindow = new com.jingdong.app.reader.view.SharePopupWindow(
										TimelineBookListCommentsActivity.this, true);
								Dialog dialog = new BookCommentDialog(
										TimelineBookListCommentsActivity.this,
										new BookCommentDialog.OnCustomDialogItemClickListener() {

											@Override
											public void onCustomDialogItemClick(
													int type) {

												switch (type) {
												case 101:// 微博
													sharePopupWindow
															.shareToWeibo(TimelineBookListCommentsActivity.this,
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
								Toast.makeText(
										TimelineBookListCommentsActivity.this,
										getString(R.string.post_book_comment_error),
										Toast.LENGTH_LONG).show();
							}
							if (mypDialog.isShowing())
								mypDialog.cancel();

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	public void startShareBitmap(String title, String bookName,
			String content1, String content, String digest, float rating,
			SharePopupWindow sharePopupWindow, int type) {

		View view = LayoutInflater.from(TimelineBookListCommentsActivity.this).inflate(
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
//		sharePopupWindow.weixinImg(TimelineBookListCommentsActivity.this, bitmap, type);// 0
																				// 好友
																				// 1
																				// 朋友圈
		WXShareHelper.getInstance().shareImage(this, bitmap, type);

	}

	
	
	public void RequestInvitedOrForward(String guid, String conternt, int state) {

		WebRequestHelper.post(URLText.commmentsPostUrl, RequestParamsPool
				.getRequestInvitedOrForward(guid, conternt, state), true,
				new MyAsyncHttpResponseHandler(
						TimelineBookListCommentsActivity.this) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);

						try {
							JSONObject obj = new JSONObject(result);
							int code = obj.optInt("code");

							if (code == 0) {
								Toast.makeText(
										TimelineBookListCommentsActivity.this,
										getString(R.string.post_book_comment_ok),
										Toast.LENGTH_LONG).show();
								finish();
							} else {
								Toast.makeText(
										TimelineBookListCommentsActivity.this,
										getString(R.string.post_book_comment_error),
										Toast.LENGTH_LONG).show();
							}
							if (mypDialog.isShowing())
								mypDialog.cancel();

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	public void RequestPostCommets(long bookid, String content, float rating,
			int listid) {

		WebRequestHelper.post(URLText.bookCommentUrl, RequestParamsPool
				.getRequestPostCommets(bookid, content, rating, listid), true,
				new MyAsyncHttpResponseHandler(
						TimelineBookListCommentsActivity.this) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);

						try {
							JSONObject obj = new JSONObject(result);
							int code = obj.optInt("code");

							if (code == 0) {
								Toast.makeText(
										TimelineBookListCommentsActivity.this,
										getString(R.string.post_book_comment_ok),
										Toast.LENGTH_LONG).show();
								finish();
								
							
								
								
								
							} else {
								Toast.makeText(
										TimelineBookListCommentsActivity.this,
										getString(R.string.post_book_comment_error),
										Toast.LENGTH_LONG).show();
							}
							if (mypDialog.isShowing())
								mypDialog.cancel();

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	// ################

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case SEARCH_BOOK:
			if (resultCode == TimelineSearchBookActivity.SELECTED_BOOK) {
				String id = data.getStringExtra("book_id");
				String name = data.getStringExtra("book_name");
				bookIdBuffer.append(id + ",");
				String key = "《" + name + "》";
				userComment.append(key);
			}
			break;

		case COMMENTS_BOOK:
			if (resultCode == TimelineSearchBookActivity.SELECTED_BOOK) {
				String id = data.getStringExtra("book_id");
				if (!id.equals("") && id != null)
					book_id = Integer.parseInt(id);
				bookname = data.getStringExtra("book_name");
				bookIdBuffer.append(id + ",");
				getTopBarView().setTitle(bookname);
			}
			if (resultCode == RESULT_CANCELED)// 取消关闭当前activity
				finish();
			break;

		case SEARCH_PEOPLE:
			if (resultCode == TimelineSearchPeopleActivity.CLICK_USER_NAME) {
				String userName = data
						.getStringExtra(TimelineSearchPeopleActivity.USER_NAME);
				
				String userid = data.getStringExtra("userid");
				userIdBuffer.append(userid + ",");
				
				userComment.append("@");
				userComment.append(userName);
				userComment.append(" ");
			}

			break;
		case BOOK_COMMENT:// 书评
			if (resultCode == TimelineSearchBookActivity.SELECTED_BOOK) {
				String id = data.getStringExtra("book_id");
				if (!id.equals("") && id != null)
					book_id = Integer.parseInt(id);
				bookname = data.getStringExtra("book_name");
				bookauthor = data.getStringExtra("book_author");
				bookcover = data.getStringExtra("book_cover");
				bookIdBuffer.append(id + ",");
				getTopBarView().setTitle("书评");
				
				if(TextUtils.isEmpty(bookauthor)) {
					bookauthor = SettingUtils.getInstance().getString("author:" + id);
				}
				
				if(TextUtils.isEmpty(bookcover)) {
					bookcover = SettingUtils.getInstance().getString("cover:" + id);
				}
				
				mBookName.setText(bookname);
				mBookAuthor.setText("null".equals(bookauthor) ? getString(R.string.author_unknown) : bookauthor);
				
				if(isUrl(bookcover)){
					ImageLoader.getInstance().displayImage(bookcover, mBookImage, GlobalVarable.getCutBookDisplayOptions());
				}
				
//				mBookLayout.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View arg0) {
//						Intent intent2 = new Intent(TimelineBookListCommentsActivity.this, BookInfoNewUIActivity.class);
//						intent2.putExtra("bookid", (long)book_id);
//						intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						startActivity(intent2);
//					}
//				});
			}
			if (resultCode == RESULT_CANCELED)// 取消关闭当前activity
				finish();
			break;
		}

	}
	
	public boolean isUrl(String url){
		
		if(TextUtils.isEmpty(url))
			return false;
		
		String regex = "^(https|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]" ;
		Pattern patt = Pattern. compile(regex );
		Matcher matcher = patt.matcher(url);
		boolean isMatch = matcher.matches();
		if (!isMatch) {
		    return false;
		} else {
			 return true;
		}
	}

	/**
	 * 根据用户是要转发还是评论，使用不同的文字填充checkBox
	 * 
	 * @param comment
	 *            true表示用户想要评论，false表示用户想要转发。
	 */
	private void initTitleBottom(boolean comment) {
		checkBox.setText(R.string.timeline_comment_comment_btw);
		if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			checkBox.setPadding(checkBox.getPaddingLeft(),
					checkBox.getPaddingTop(), checkBox.getPaddingRight(),
					checkBox.getPaddingBottom());
		} else {
			checkBox.setPadding(checkBox.getPaddingLeft()
					+ getResources()
							.getDrawable(R.drawable.post_tweet_checkbox)
							.getIntrinsicWidth(), checkBox.getPaddingTop(),
					checkBox.getPaddingRight(), checkBox.getPaddingBottom());
		}
	}

	private void initCheckBox(String comment) {
		checkBox.setText(comment);
		if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			checkBox.setPadding(checkBox.getPaddingLeft(),
					checkBox.getPaddingTop(), checkBox.getPaddingRight(),
					checkBox.getPaddingBottom());
		} else {
			checkBox.setPadding(checkBox.getPaddingLeft()
					+ getResources()
							.getDrawable(R.drawable.post_tweet_checkbox)
							.getIntrinsicWidth(), checkBox.getPaddingTop(),
					checkBox.getPaddingRight(), checkBox.getPaddingBottom());
		}
	}

	@Override
	public void onRightMenuOneClick() {
		super.onRightMenuOneClick();
		postComments(type);
	}

	@Override
	public void onLeftMenuClick() {

		String content = userComment.getText().toString();
		if (draft == null && !TextUtils.isEmpty(content)) {
		
		DialogManager.showCommonDialog(TimelineBookListCommentsActivity.this,"提示","是否保存到草稿箱?", "保存草稿", "不保存", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					saveDraft();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
				default:
					break;
				}
				dialog.dismiss();
				finish();
			}
		});
		}
		else {
			finish();
		}
	}
	
	/**
	 * 评论成功后领取积分
	 */
	protected void commentGetScore() {
		// TODO Auto-generated method stub
		IntegrationAPI.commentGetScore(this, book_id, new GrandScoreListener() {

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
				CustomToast.showToast(TimelineBookListCommentsActivity.this, span);
			}

			@Override
			public void onGrandFail() {
				MZLog.e("J", "评论获取积分失败");
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TCAgent.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_community_shuping));
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_community_shuping));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		TCAgent.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_community_shuping));
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_community_shuping));
	}
	
}
