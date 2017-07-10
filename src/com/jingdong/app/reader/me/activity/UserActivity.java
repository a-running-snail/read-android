package com.jingdong.app.reader.me.activity;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.fragment.BookListFragment;
import com.jingdong.app.reader.me.model.BookListModel;
import com.jingdong.app.reader.me.model.EditInfoModel;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.message.activity.ChatActivity;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.actiivity.CommentActivity;
import com.jingdong.app.reader.timeline.actiivity.FansActivity;
import com.jingdong.app.reader.timeline.actiivity.FocusActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.dialog.CommonDialog;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class UserActivity extends BaseActivityWithTopBar {

	public static final String BookIdKey = "BookIdKey";
	public static final int ToLoginActivityKeyCode = 200;
	private String usertoken;
	private UserDetail userDetail = null;
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "user_name";
	public static final String NAME = "name";
	public static final String JD_USER_NAME = "jd_user_name";
	public final static int EDIT_INFO = 934;
	public final static int OPEN_FANS = 932;
	public static final int INIT_LOAD = 1000;
	private String userid = "";
	private String userName = null;
	private boolean isFollowingOther = false;

	private String jd_user_name;
	private FrameLayout loding;
	private ScrollView rootScrollView;
	private TextView userIntro;
	private String userNickName;
	private String imgurl;
	// private TextView userNickName;
	private RoundNetworkImageView avatar;
	private ImageView avatarLabel;

	private TextView focusNum;
	private TextView fansNum;
	private LinearLayout fansNewLayout;
	// private TextView newFansCount;

	private LinearLayout focusPeopleLayout;
	private RelativeLayout collection_layout;
	private LinearLayout fansLayout;

	// private LinearLayout sendMsgAreaLayout;
	private LinearLayout buttonArea;
	private LinearLayout followButtonArea;
	private ImageView followButtonImage;
	private TextView followButtonText;
	private TopBarView topBarView = null;
	// private Personal personal = null;
	private TextView user_nickname;
	// private ImageView sexImageView;
	private UserInfo userInfos;

	// 界面容器布局
	// private LinearLayout booksHolder;
	// private TextView booksCount;
	// private LinearLayout booksHeader;

	// private LinearLayout timelineHolder;
	// private LinearLayout readingdataHolder;
	// private LinearLayout snsHolder;

	private LayoutInflater mInflater;

	// 界面分区
	// private FrameLayout headerArea;
	private FrameLayout booksArea;
	// private FrameLayout timelineArea;
	// private FrameLayout readingdataArea;
	private FrameLayout snsArea;
	private TextView compose_num;
	private TextView bookComments_num;
	private TextView bookNote_num;
	private TextView readed_num;
	private TextView like_num;
	private TextView import_num;
	private TextView collection_num;
	private RelativeLayout compose_layout;
	private RelativeLayout bookComments_layout;
	private RelativeLayout bookNotes_layout;
	private RelativeLayout readed_layout;
	private RelativeLayout like_layout;
	private RelativeLayout import_layout;
	private ImageView composeImageView;
	private ImageView bookcommImageView;
	private ImageView bookNoImageView;
	private ImageView readediImageView;
	private ImageView likeImageView;
	private ImageView importImageView;
	private ImageView collectionImageView;

	private boolean current_user_is_followed_by;
	private boolean current_user_is_following;
	private String following_users_count;
	private String books_count_in_book_shelf;
	private String follower_users_count;
	private String notes_entities_count;
	private String read_books_count;
	private String wish_books_count;
	private String entity_count;
	private String user_tweet_count;
	private String book_comment_count;
	private String book_count;
	private String document_count;
	private String favourite_count;
	private Boolean isShowDialog = false;
	private String name;
	private int position;

	private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateNewFans();
			LoginUser.getpin();
		}

	};

	private void initView() {
		mInflater = LayoutInflater.from(this);

		loding = (FrameLayout) findViewById(R.id.loading);
		rootScrollView = (ScrollView) findViewById(R.id.rootView);
		userIntro = (TextView) findViewById(R.id.user_info);
		// userNickName = (TextView) findViewById(R.id.user_nickname);
		avatar = (RoundNetworkImageView) findViewById(R.id.thumb_nail);
		focusNum = (TextView) findViewById(R.id.focus_num);
		fansNum = (TextView) findViewById(R.id.fans_num);
		focusPeopleLayout = (LinearLayout) findViewById(R.id.focus_people_layout);
		fansLayout = (LinearLayout) findViewById(R.id.fans_layout);
		// newFansCount = (TextView) findViewById(R.id.new_add_fans);

		// sendMsgAreaLayout = (LinearLayout) findViewById(R.id.send_msg);
		followButtonArea = (LinearLayout) findViewById(R.id.user_follow_button);
		// followButtonImage = (ImageView) findViewById(R.id.user_follow_icon);
		followButtonText = (TextView) findViewById(R.id.user_follow_text);
		buttonArea = (LinearLayout) findViewById(R.id.button_area);
		compose_num = (TextView) findViewById(R.id.compose_num);
		bookComments_num = (TextView) findViewById(R.id.bookComments_num);
		bookNote_num = (TextView) findViewById(R.id.bookNote_num);
		readed_num = (TextView) findViewById(R.id.readed_num);
		like_num = (TextView) findViewById(R.id.like_num);
		import_num = (TextView) findViewById(R.id.import_num);
		user_nickname = (TextView) findViewById(R.id.user_nickname);
		// sexImageView = (ImageView) findViewById(R.id.sex);
		collection_layout = (RelativeLayout) findViewById(R.id.collection_layout);
		collection_num = (TextView) findViewById(R.id.collection_num);
		compose_layout = (RelativeLayout) findViewById(R.id.compose_layout);
		bookComments_layout = (RelativeLayout) findViewById(R.id.bookComments_layout);
		bookNotes_layout = (RelativeLayout) findViewById(R.id.bookNotes_layout);
		readed_layout = (RelativeLayout) findViewById(R.id.readed_layout);
		like_layout = (RelativeLayout) findViewById(R.id.like_layout);
		import_layout = (RelativeLayout) findViewById(R.id.import_layout);
		composeImageView = (ImageView) findViewById(R.id.compost_img);
		collectionImageView = (ImageView) findViewById(R.id.collection_img);
		bookcommImageView = (ImageView) findViewById(R.id.bookComments_img);
		bookNoImageView = (ImageView) findViewById(R.id.bookNote_img);
		readediImageView = (ImageView) findViewById(R.id.readed_img);
		likeImageView = (ImageView) findViewById(R.id.like_img);
		importImageView = (ImageView) findViewById(R.id.import_img);
		
		//隐藏外部导入
		import_layout.setVisibility(View.GONE);
	}

	public void resetButton() {
		if (current_user_is_following) {
			if (current_user_is_followed_by) {
				followButtonArea.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_bothfollowing_homepage));
				followButtonText.setText("互相关注");
				followButtonText.setTextColor(getResources().getColor(
						R.color.text_main));
			} else {
				followButtonArea.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_following_homepage));
				followButtonText.setText("已关注");
				followButtonText.setTextColor(getResources().getColor(
						R.color.text_main));
			}
		} else {
			followButtonArea.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.btn_follow_homepage));
			followButtonText.setText("加关注");
			followButtonText.setTextColor(getResources().getColor(
					R.color.red_main));
		}
	}

	public void prepareLayout() {

		if (userInfos.getUserPin().equals(LoginUser.getpin())) {
			followButtonArea.setVisibility(View.GONE);
		} else {
			resetButton();
			collection_layout.setVisibility(View.GONE);
			import_layout.setVisibility(View.GONE);
		}

		focusNum.setText(following_users_count + "");
		fansNum.setText(follower_users_count + "");
		ImageLoader.getInstance().displayImage(userInfos.getAvatar(), avatar,
				GlobalVarable.getDefaultAvatarDisplayOptions(false));
		userIntro.setText(userInfos.getSummary());

		if (!user_tweet_count.equals("null")) {

			compose_num.setText(user_tweet_count);
			composeImageView.setVisibility(View.VISIBLE);
			compose_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(UserActivity.this,
							CommentActivity.class);
					intent.putExtra("title", "随便说说");
					intent.putExtra("user_id", userInfos.getId());
					intent.putExtra(UserActivity.JD_USER_NAME,
							userInfos.getJd_user_name());
					startActivity(intent);
				}
			});

		}

		if (!book_comment_count.equals("null")) {

			bookComments_num.setText(book_comment_count);
			bookcommImageView.setVisibility(View.VISIBLE);
			bookComments_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(UserActivity.this,
							CommentActivity.class);
					intent.putExtra("title", "书评");
					intent.putExtra("user_id", userInfos.getId());
					startActivity(intent);
				}
			});

		}

		if (!notes_entities_count.equals("null")) {

			bookNote_num.setText(notes_entities_count);
			bookNoImageView.setVisibility(View.VISIBLE);
			bookNotes_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(UserActivity.this,
							BookListActivity.class);
					intent.putExtra(BookListFragment.BOOKLIST_TYPE,
							BookListModel.NOTES_BOOKS);
					intent.putExtra("user_id", userInfos.getId());
					startActivity(intent);
				}
			});

		}

		if (!read_books_count.equals("null")) {

			readed_num.setText(read_books_count);
			readediImageView.setVisibility(View.VISIBLE);
			readed_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(UserActivity.this,
							BookListActivity.class);
					intent.putExtra(BookListFragment.BOOKLIST_TYPE,
							BookListModel.READ_BOOKS);
					intent.putExtra("user_id", userInfos.getId());
					startActivity(intent);
				}
			});

		}

		if (!wish_books_count.equals("null")) {

			like_num.setText(wish_books_count);
			likeImageView.setVisibility(View.VISIBLE);
			like_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(UserActivity.this,
							WantActivity.class);
					intent.putExtra("user_id", userInfos.getId());
					startActivity(intent);
				}
			});

		}

		if (!document_count.equals("null")) {

			import_num.setText(document_count);
			importImageView.setVisibility(View.VISIBLE);
			import_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.putExtra(USER_ID, userInfos.getId());
					intent.setClass(UserActivity.this, BookListActivity.class);
					intent.putExtra(BookListFragment.BOOKLIST_TYPE,
							BookListModel.IMPORT_BOOKS);
					startActivity(intent);
				}
			});

		}

		if (!favourite_count.endsWith("null")) {
			collection_num.setText(favourite_count);
			collectionImageView.setVisibility(View.VISIBLE);
			collection_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(UserActivity.this,
							CommentActivity.class);
					intent.putExtra("title", "收藏");
					intent.putExtra("user_id", userInfos.getId());
					startActivity(intent);
				}
			});
		}

		user_nickname.setText(userInfos.getName());

		if (userInfos.isFemale()) {
			user_nickname.append(Html.fromHtml("<img src=\""
					+ R.drawable.icon_boy + "\">", imageGetter, null));
		} else {
			user_nickname.append(Html.fromHtml("<img src=\""
					+ R.drawable.icon_girl + "\">", imageGetter, null));
		}

		followButtonArea.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				current_user_is_following = !current_user_is_following;
				resetButton();
				Intent intent=new Intent();
				intent.putExtra("status", current_user_is_following);
				intent.putExtra("position", position);
				setResult(RESULT_OK, intent);
				if (current_user_is_following) {
					Follow(userid);
				} else {
					UNFollow(userid);
				}
			}
		});

		focusPeopleLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (userInfos != null) {
					Intent intent = new Intent();
					intent.setClass(UserActivity.this, FocusActivity.class);
					intent.putExtra("user_id", userInfos.getId());
					intent.putExtra(UserActivity.JD_USER_NAME,
							userInfos.getJd_user_name());
					startActivity(intent);
				}
			}
		});

		fansLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (userInfos != null) {
					Intent intent = new Intent();
					intent.setClass(UserActivity.this, FansActivity.class);
					intent.putExtra("user_id", userInfos.getId());
					intent.putExtra(UserActivity.JD_USER_NAME,
							userInfos.getJd_user_name());
					startActivityForResult(intent, OPEN_FANS);
				}

			}
		});

	}

	ImageGetter imageGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			int id = Integer.parseInt(source);

			// 根据id从资源文件中获取图片对象
			Drawable d = getResources().getDrawable(id);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		}
	};

	public void updateFollowingButton(String type) {
		if (type.equals("each_other")) {
			followButtonArea
					.setBackgroundResource(R.drawable.homepage_gray_button);
			followButtonImage.setImageResource(R.drawable.fanslist_mutual);
			followButtonText
					.setText(R.string.user_homepage_newui_focus_each_other);
		} else if (type.equals("following")) {
			followButtonArea
					.setBackgroundResource(R.drawable.homepage_gray_button);
			followButtonImage.setImageResource(R.drawable.fanslist_tick);
			followButtonText.setText(R.string.user_homepage_newui_focused);
		} else {
			followButtonArea
					.setBackgroundResource(R.drawable.homepage_green_button);
			followButtonImage.setImageResource(R.drawable.fanslist_plus);
			followButtonText.setText(R.string.user_homepage_newui_focus);
		}

	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationService.NOTIFICATION_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				notificationReceiver, filter);

	}

	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				notificationReceiver);
	}

	public void updateNewFans() {

		if (userDetail != null
				&& userDetail.getId() == LocalUserSetting
						.getUserId(UserActivity.this)) {
			int newFansNumber;
			if ((newFansNumber = Notification.getInstance().getFollowersCount()) == 0) {
				// newFansCount.setVisibility(View.GONE);
			} else {
				// newFansCount.setVisibility(View.VISIBLE);
				// newFansCount.setText("+"+String.valueOf(newFansNumber));
			}
		} else {
			// newFansCount.setVisibility(View.GONE);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_homepage);
		initView();
		topBarView = (TopBarView) findViewById(R.id.topbar);
		initTopbarView();

		userid = getIntent().getStringExtra(USER_ID);
		userName = getIntent().getStringExtra(USER_NAME);
		jd_user_name = getIntent().getStringExtra(JD_USER_NAME);
		name=getIntent().getStringExtra(NAME);
		position = getIntent().getIntExtra("position", -1);

		if (topBarView != null) {
			topBarView.setTitle("个人主页");
			if (LoginUser.getpin().equals(
					jd_user_name == null ? "" : jd_user_name)) {
//				topBarView.setRightMenuOneVisiable(true, "编辑",
//						R.color.red_main, false);
			} else {
				topBarView.setRightMenuOneVisiable(true, "私信",
						R.color.red_main, false);
			}
		}
		
	}

	public void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		// topBarView.setRightMenuOneVisiable(true, R.drawable.btn_bar_setting,
		// false);
		topBarView.setListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (userid != null) {
			getUserInfo(userid, "","");
		} else if (name != null) {
			getUserInfo("", "",name);
		} else if (userName != null) {
			getUserInfo("", userName,"");
		}
		registerReceiver();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_gerenzhuye));
	}

	private void getUserInfo(String userId, String username,String name) {
		if (!NetWorkUtils.isNetworkConnected(UserActivity.this)) {
			Toast.makeText(UserActivity.this,
					getString(R.string.network_connect_error),
					Toast.LENGTH_SHORT).show();
			return;
		}
		WebRequestHelper.get(URLText.Personal_homepage,
				RequestParamsPool.getPersonalParams(userId, username,name), true,
				new MyAsyncHttpResponseHandler(UserActivity.this, true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Log.d("cj", "onFailures=======>>");
						Toast.makeText(UserActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@SuppressWarnings("unused")
					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						Log.d("cj", "result=======>>" + result);
						// personal = GsonUtils.fromJson(result,
						// Personal.class);
						try {
							JSONObject o = new JSONObject(result);
							userInfos = getUserInfos().fromJSON(
									o.getJSONObject("user"));
							following_users_count = o
									.optString("following_users_count");
							books_count_in_book_shelf = o
									.optString("books_count_in_book_shelf");
							follower_users_count = o
									.optString("follower_users_count");
							notes_entities_count = o
									.optString("notes_entities_count");
							read_books_count = o.optString("read_books_count");
							wish_books_count = o.optString("wish_books_count");
							entity_count = o.optString("entity_count");
							user_tweet_count = o.optString("user_tweet_count");
							book_comment_count = o
									.optString("book_comment_count");
							book_count = o.optString("book_count");
							document_count = o.optString("document_count");
							favourite_count = o.optString("favourite_count");
							current_user_is_followed_by = o
									.optBoolean("current_user_is_followed_by");
							current_user_is_following = o
									.optBoolean("current_user_is_following");
							if(userid == null || userid.equals(""))
								userid = userInfos.getId();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						if (userInfos != null) {
							userNickName = userInfos.getName();
							prepareLayout();
						} else
							Toast.makeText(UserActivity.this,
									getString(R.string.network_connect_error),
									Toast.LENGTH_SHORT).show();
					}

				});

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_gerenzhuye));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (!TextUtils.isEmpty(userid)
				&& userid.equals(LoginUser.getpin())
				|| (userName != null && userName.equals(LocalUserSetting
						.getUserName(UserActivity.this)))) {
			MenuInflater inflater = new MenuInflater(this);
			inflater.inflate(R.menu.edit_profile, menu);
			MenuItem Item = menu.findItem(R.id.edit);
			View actionView = Item.getActionView();
			TextView view = (TextView) actionView
					.findViewById(R.id.edit_action);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (avatar != null
							&& NetWorkUtils
									.isNetworkConnected(UserActivity.this)) {
						Drawable drawable = avatar.getDrawable();
						if (drawable != null) {
							byte[] avatar = UiStaticMethod
									.bitmapToByteArray(UiStaticMethod
											.drawableToBitmap(drawable));
							Intent intent = new Intent(UserActivity.this,
									EditInfoActivity.class);
							intent.putExtra(MoreInfoActivity.USER_DETAIL,
									userDetail);
							intent.putExtra(EditInfoModel.AVATAR, avatar);
							startActivityForResult(intent, EDIT_INFO);
						}

					}

				}
			});
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case EDIT_INFO:
			if (resultCode == Activity.RESULT_OK) {
				ResquestHomePageDataTask task = new ResquestHomePageDataTask();
				task.execute();
			}
			break;
		case OPEN_FANS:
			// 更新粉丝的新增数目
			updateNewFans();
			break;
		case ToLoginActivityKeyCode :
			//从登录界面返回时如果没登录直接关闭当前页面
			if(resultCode == 3){
				if(!LoginUser.isLogin())
					finish();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	class ResquestHomePageDataTask extends AsyncTask<Void, Void, UserDetail> {
		@Override
		protected UserDetail doInBackground(Void... params) {

			Map<String, String> map = new HashMap<String, String>();
			map.put(LocalUserSetting.AUTHTOKEN, usertoken);
			map.put("author_book_count", "3");
			try {
				String url = "";
				if (userName == null) {
					url = URLBuilder.addParameter(
							URLText.userHomepageUrl.replace(":d", userid + ""),
							map);
				}

				else
					url = URLBuilder.addParameter(
							URLText.userHomepageByNickNameUrl.replace(":s",
									URLEncoder.encode(userName,
											WebRequestHelper.CHAR_SET)), map);

				String result = WebRequest.getWebDataWithContext(
						UserActivity.this, url);

				if (result.contains("error")) {
					return null;
				}
				JSONObject object = new JSONObject(result);
				userDetail = new UserDetail();
				userDetail.parseJson(object);
			} catch (Exception e) {
				userDetail = null;
				e.printStackTrace();
			}
			return userDetail;
		}

		class RequestFollowingTsdk extends AsyncTask<Boolean, Void, Integer> {

			@Override
			protected Integer doInBackground(Boolean... params) {
				String resultString = "";
				if (params[0]) {
					resultString = WebRequest.postWebDataWithContext(
							UserActivity.this,
							URLText.followCertainUser
									+ userDetail.getId()
									+ ".json?auth_token="
									+ LocalUserSetting
											.getToken(UserActivity.this), "");
				} else {
					resultString = WebRequest.postWebDataWithContext(
							UserActivity.this,
							URLText.unFollowCertainUser
									+ userDetail.getId()
									+ ".json?auth_token="
									+ LocalUserSetting
											.getToken(UserActivity.this), "");
				}

				if (resultString.contains("success") && params[0])// params[0]=true
																	// 表示关注人
																	// 然后成功
					return 1;
				else if (resultString.contains("success") && !params[0]) {
					return 2;
				} else {
					return -1;
				}
			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result == -1) {
					Toast.makeText(UserActivity.this, "操作失败哦",
							Toast.LENGTH_SHORT).show();
				} else if (result == 1) {
					isFollowingOther = true;
					if (userDetail != null
							&& userDetail.isFollowingCurrentUser()) {
						updateFollowingButton("each_other");
					} else {
						updateFollowingButton("following");
					}

				} else {
					updateFollowingButton("not followed");
					isFollowingOther = false;
				}

			}
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		if (!LoginUser.isLogin()) {
			if(!isShowDialog){
				isShowDialog=true;
				String message="是否登录查看更多信息？";
				CommonDialog commonDialog=DialogManager.getCommonDialog(this, "提示", message, "马上登录", "暂不登录", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							Intent login = new Intent(UserActivity.this, LoginActivity.class);
							login.putExtra("from", "userActivity");
							startActivityForResult(login, UserActivity.ToLoginActivityKeyCode);
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							finish();
							break;
						default:
							break;
						}
						dialog.dismiss();
						isShowDialog=false;
					}
				});
				//设置dialog不可取消
				commonDialog.setCancelable(false);
				commonDialog.show();
			}
		}
	}
	
	/**
	 * 编辑功能（先取消）tmj
	 */
	@Override
	public void onRightMenuOneClick() {
		if (userInfos != null) {
			if (LoginUser.getpin().equals(userInfos.getJd_user_name())) {
//				Intent intent = new Intent(UserActivity.this,
//						ModifyUserInfoActivity.class);
//				startActivity(intent);
			} else {
				Intent intent = new Intent(UserActivity.this,
						ChatActivity.class);
				intent.putExtra(ChatActivity.USER_INFO, userInfos);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		}
	}

	private void Follow(String id) {
		WebRequestHelper.post(URLText.Follow_SomeOne_URL,
				RequestParamsPool.getFollowSomeParams(id),
				new MyAsyncHttpResponseHandler(UserActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Log.d("cj", "onFailure=======>>");
						Toast.makeText(UserActivity.this,
								R.string.network_connect_error,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);
						Log.d("cj", "result=======>>" + result);
						try {
							JSONObject jsonObject = new JSONObject(result);
							String code = jsonObject.optString("code");
							String message = jsonObject.optString("message");
							if (jsonObject != null) {
								if (code.equals("0")) {
									Toast.makeText(UserActivity.this, message,
											1).show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void UNFollow(String id) {
		WebRequestHelper.post(URLText.Follow_Cancle,
				RequestParamsPool.getUNFollowParams(id),
				new MyAsyncHttpResponseHandler(UserActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Log.d("cj", "onFailure=======>>");
						Toast.makeText(UserActivity.this,
								R.string.network_connect_error,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub
						String result = new String(responseBody);
						Log.d("cj", "result=======>>" + result);

						try {
							JSONObject jsonObject = new JSONObject(result);
							String code = jsonObject.optString("code");
							String message = jsonObject.optString("message");
							if (jsonObject != null) {
								if (code.equals("0")) {
									Toast.makeText(UserActivity.this, message,
											1).show();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}

	public UserInfo getUserInfos() {
		return userInfos;
	}

	public void setUserInfos(UserInfo userInfos) {
		this.userInfos = userInfos;
	}
	
}
