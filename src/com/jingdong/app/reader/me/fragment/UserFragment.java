package com.jingdong.app.reader.me.fragment;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executor;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jingdong.app.reader.me.activity.BookListActivity;
import com.jingdong.app.reader.me.activity.EditInfoActivity;
import com.jingdong.app.reader.me.activity.MoreInfoActivity;
import com.jingdong.app.reader.me.activity.UserListActivity;
import com.jingdong.app.reader.me.model.BookListModel;
import com.jingdong.app.reader.me.model.EditInfoModel;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.me.model.UserFollower;
import com.jingdong.app.reader.me.view.UserInfoView;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.ParserCreator;
import com.jingdong.app.reader.parser.json.EntitiyJSONParser;
import com.jingdong.app.reader.parser.json.TimelineJSONParser;
import com.jingdong.app.reader.parser.url.TweetURLParser;
import com.jingdong.app.reader.parser.url.URLParser;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.actiivity.TweetListActivity;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.R;

public class UserFragment extends Fragment implements Observer, OnClickListener, OnCheckedChangeListener {
	private static class MyHandler extends Handler {
		private WeakReference<UserFragment> reference;

		public MyHandler(UserFragment fragment) {
			reference = new WeakReference<UserFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			UserFragment fragment = reference.get();
			if (fragment != null) {
				switch (msg.what) {
				case INIT_LOAD:
					if (msg.arg1 == TimeLineModel.SUCCESS_INT) {
						if (msg.arg2 == TimeLineModel.SUCCESS_INT) {
							fragment.view.fillView(fragment.userInfo);
							fragment.userId = fragment.userInfo.getId();
							fragment.userName = fragment.userInfo.getName();
							fragment.follower.setFollowing(fragment.userInfo.isFollowedByCurrentUser());
							fragment.follower.setFans(fragment.userInfo.isFollowingCurrentUser());
							fragment.button.setOnCheckedChangeListener(fragment);
							fragment.view.setAvatar(fragment.userInfo.getThumbNail());
							fragment.view.setVisibility(View.VISIBLE);
						} else
							Toast.makeText(fragment.getActivity(), R.string.user_not_found, Toast.LENGTH_SHORT).show();
					} else
						Toast.makeText(fragment.getActivity(), R.string.loading_fail, Toast.LENGTH_SHORT).show();
					break;
				case UserFollower.FOLLOW:
					boolean following = fragment.follower.isFollowing();
					if (msg.arg1 == TimeLineModel.FAIL_INT)
						fragment.follower.setFollowing(!following);
					else if (msg.arg1 == TimeLineModel.SUCCESS_INT) {
						fragment.userInfo.setFollowedByCurrentUser(following);
						if (fragment.userInfo.isFollowedByCurrentUser() && fragment.userInfo.isFollowingCurrentUser()) {
							fragment.button.setText(R.string.follow_each_other);
						}
					}
					break;
				}
			}
		}
	}

	private class Task implements Runnable {
		private int type;

		public Task(int type) {
			this.type = type;
		}

		@Override
		public void run() {
			switch (type) {
			case INIT_LOAD:
				if (userName == null)
					userInfo.parseJson(getActivity(), userId);
				else
					userInfo.parseJson(getActivity(), userName);
				break;
			case UserFollower.FOLLOW:
				follower.followUser(userId);
				break;
			}
		}
	}

	private static final String TAG = "UserFragment";
	public final static int EDIT_INFO = 934;
	public final static int OPEN_FANS = 932;
	public static final int INIT_LOAD = 1000;
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "user_name";
	private UserDetail userInfo;
	private UserFollower follower;
	private ToggleButton button;
	private Executor executor;
	private UserInfoView view;
	private Handler handler;
	private String userName;
	private String userId;
	
	

	public UserFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = new UserInfoView(getActivity());
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		view.setLayoutParams(layoutParams);
		view.setBackgroundColor(getResources().getColor(R.color.bg_white));
		button = (ToggleButton) view.findViewById(R.id.user_follow_button);
		setItemOnClick(view);
		view.setVisibility(View.GONE);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		handler = new MyHandler(this);
		userName = getArguments().getString(USER_NAME);
		userId = getArguments().getString(USER_ID, LoginUser.getpin());
		follower = new UserFollower(getActivity(), false, false);
		follower.addObserver(this);
		userInfo = new UserDetail();
		userInfo.addObserver(this);
		executor = NotificationService.getExecutorService();
		executor.execute(new Task(INIT_LOAD));
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		follower.deleteObserver(this);
		userInfo.deleteObserver(this);
		handler.removeCallbacksAndMessages(null);
		super.onDestroyView();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case EDIT_INFO:
			if (resultCode == Activity.RESULT_OK) {
				updateView(data);
			}
			break;
		case OPEN_FANS:
			view.updateNewFans(userInfo);
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		follower.setFollowing(isChecked);
	}

	@Override
	public void onClick(View v) {
		Resources resources = getResources();
		Bundle bundle = new Bundle();
		ParserCreator parserCreator;
		Intent intent = new Intent();
		intent.setClass(getActivity(), TweetListActivity.class);
		intent.putExtra(USER_ID, userInfo.getId());
		switch (v.getId()) {
		case R.id.user_alltimeline:
			bundle.putString(URLParser.BASE_URL, URLText.userEntitiesUrl + userId + ".json");
			parserCreator = new BaseParserCreator(TweetURLParser.class, EntitiyJSONParser.class, bundle);
			intent.putExtra(TimelineFragment.PARSER_CREATOR, parserCreator);
			startActivity(intent);
			break;
		case R.id.user_book_comment:
			bundle.putString(URLParser.BASE_URL, URLText.userBookCommentUrl + userId + ".json");
			parserCreator = new BaseParserCreator(TweetURLParser.class, EntitiyJSONParser.class, bundle);
			intent.putExtra(TimelineFragment.PARSER_CREATOR, parserCreator);
			startActivity(intent);
			break;
		case R.id.user_note:
			bundle.putString(URLParser.BASE_URL, URLText.userNotesUrl + userId + ".json");
			parserCreator = new BaseParserCreator(TweetURLParser.class, TimelineJSONParser.class, bundle);
			intent.putExtra(TimelineFragment.PARSER_CREATOR, parserCreator);
			startActivity(intent);
			break;
		case R.id.user_favourite:
			bundle.putString(URLParser.BASE_URL, URLText.userFavouriteUrl);
			parserCreator = new BaseParserCreator(TweetURLParser.class, EntitiyJSONParser.class, bundle);
			intent.putExtra(TimelineFragment.PARSER_CREATOR, parserCreator);
			startActivity(intent);
			break;
		case R.id.user_read_done:
			intent.putExtra(BookListFragment.JUMP_READING_DATA, true);
			intent.putExtra(USER_NAME, userName);
			startBookList(intent, BookListModel.READ_BOOKS);
			break;
		case R.id.user_read_wish:
			startBookList(intent, BookListModel.WISH_BOOKS);
			break;
		case R.id.user_book_buy:
			startBookList(intent, BookListModel.BOUGHT_BOOKS);
			break;
		case R.id.user_book_import:
			startBookList(intent, BookListModel.IMPORT_BOOKS);
			break;
		case R.id.user_widget_follow:
			intent.setClass(getActivity(), UserListActivity.class);
			intent.putExtra(UserListActivity.TITLE, resources.getString(R.string.followings_list_title));
			intent.putExtra(UserListActivity.IS_FOLLOWING_LIST, true);
			startActivity(intent);
			break;
		case R.id.user_widget_fans:
			intent.setClass(getActivity(), UserListActivity.class);
			intent.putExtra(UserListActivity.TITLE, resources.getString(R.string.followers_list_title));
			intent.putExtra(UserListActivity.IS_FOLLOWING_LIST, false);
			startActivityForResult(intent, OPEN_FANS);
			break;
		case R.id.user_widget_more:
			intent.setClass(getActivity(), MoreInfoActivity.class);
			intent.putExtra(MoreInfoActivity.USER_DETAIL, userInfo);
			startActivity(intent);
			break;
		case R.id.user_follow_button:
			executor.execute(new Task(UserFollower.FOLLOW));
			break;
		case R.id.user_modify_button:
			intent.setClass(getActivity(), EditInfoActivity.class);
			intent.putExtra(MoreInfoActivity.USER_DETAIL, userInfo);
			intent.putExtra(EditInfoModel.AVATAR, view.getAvatar());
			startActivityForResult(intent, EDIT_INFO);
			break;
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		handler.sendMessage((Message) data);
	}

	/**
	 * 设置当前Activity的点击事件
	 */
	private void setItemOnClick(View view) {
		view.findViewById(R.id.user_alltimeline).setOnClickListener(this);
		view.findViewById(R.id.user_book_comment).setOnClickListener(this);
		view.findViewById(R.id.user_note).setOnClickListener(this);
		view.findViewById(R.id.user_favourite).setOnClickListener(this);
		view.findViewById(R.id.user_read_done).setOnClickListener(this);
		view.findViewById(R.id.user_read_wish).setOnClickListener(this);
		view.findViewById(R.id.user_book_buy).setOnClickListener(this);
		view.findViewById(R.id.user_book_import).setOnClickListener(this);
		view.findViewById(R.id.user_widget_follow).setOnClickListener(this);
		view.findViewById(R.id.user_widget_fans).setOnClickListener(this);
		view.findViewById(R.id.user_widget_more).setOnClickListener(this);
		view.findViewById(R.id.user_follow_button).setOnClickListener(this);
		view.findViewById(R.id.user_modify_button).setOnClickListener(this);
	}

	/**
	 * 启动BookList
	 * 
	 * @param intent
	 *            待发的Intent请求
	 * @param type
	 *            图书列表的类型
	 */
	private void startBookList(Intent intent, int type) {
		intent.setClass(getActivity(), BookListActivity.class);
		intent.putExtra(BookListFragment.BOOKLIST_TYPE, type);
		startActivity(intent);
	}

	private void updateView(Intent data) {
		Bundle bundle = data.getExtras();
		userInfo.setName(bundle.getString(EditInfoModel.NAME, userInfo.getName()));
		userInfo.setSummary(bundle.getString(EditInfoModel.SUMMARY, userInfo.getSummary()));
		userInfo.setContactEmail(bundle.getString(EditInfoModel.CONTECT_EMAIL, userInfo.getContactEmail()));
		userInfo.setSex(UserInfo.convertSex(bundle.getInt(EditInfoModel.SEX, UserInfo.convertSex(userInfo.isFemale()))));
		byte[] avatar = bundle.getByteArray(EditInfoModel.AVATAR);
		view.setTitle(userInfo);
		view.setAvatar(avatar);
		
	}
}
