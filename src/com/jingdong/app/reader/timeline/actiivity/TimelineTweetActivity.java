package com.jingdong.app.reader.timeline.actiivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.community.CommunityUserListActivity;
import com.jingdong.app.reader.community.CommunityUtil;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.adapter.TimelineCommentAdapter;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Comment;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.timeline.model.core.RenderBody;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.ArticleHelper;
import com.jingdong.app.reader.util.JumpToUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.share.CommunityShareUtil;
import com.jingdong.app.reader.util.share.SharePopupWindow;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.TextAreaForDetail;
import com.jingdong.app.reader.view.TopBarView;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.DraftsActivity;
import com.jingdong.app.reader.application.MZBookApplication;

public class TimelineTweetActivity extends BaseActivityWithTopBar implements
		Observer, OnItemClickListener, OnItemLongClickListener,
		android.view.View.OnClickListener, OnScrollListener{
	
	
	private class TweetRunable implements Runnable {
		private int type;
		private Object data;

		public TweetRunable(int action) {
			this.type = action;
		}

		public TweetRunable(int action, Object data) {
			this(action);
			this.data = data;
		}

		@Override
		public void run() {
			switch (type) {
			case TweetModel.INIT_LOAD:
				tweetEntity.initTweet(guid, TimelineTweetActivity.this, type);
				break;
			case TweetModel.LOAD_NOTE_AS_ENTITY:
				tweetEntity.initTweet(noteId, TimelineTweetActivity.this, type);
				break;
			case TweetModel.CLICK_FAVOURITE:
				tweetEntity.clickFavourite(TimelineTweetActivity.this);
				break;
			case TweetModel.CLICK_RECOMMAND:
				tweetEntity.clickRecommand(TimelineTweetActivity.this);
				break;
			case TweetModel.POST_COMMENT:
			case TweetModel.POST_FORWARD:
				// Bundle中包含着当前请求是POST_COMMENT还是POST_FORWARD，因此二者共用一个分支。
//				tweetEntity.postComment(TimelineTweetActivity.this,
//						commentBundle);
				Message msg=new Message();
				msg.arg1 = ObservableModel.SUCCESS_INT;
				msg.what=TweetModel.POST_COMMENT;
				handler.sendMessage(msg);
				break;
			case TweetModel.LOAD_NEXT_COMMENT:
			case TweetModel.LOAD_NEXT_RECOMMEND:
			case TweetModel.LOAD_RECOMMEND:
			case TweetModel.LOAD_COMMENT:
				MZLog.d("wangguodong", "kkkkkkkkk");
				tweetEntity.loadCommentOrForward(TimelineTweetActivity.this,
						guid, type);
				break;
			case TweetModel.DELETE_ENTITY:
				tweetEntity.delteTweet(TimelineTweetActivity.this);
				break;
			case TweetModel.DELETE_COMMENT:
				tweetEntity.deleteComment(TimelineTweetActivity.this,
						(Comment) data);
				break;
			default:
				break;
			}
		}
	}

	private static class TweetHandler extends Handler {
		WeakReference<TimelineTweetActivity> reference;

		public TweetHandler(TimelineTweetActivity activity) {
			reference = new WeakReference<TimelineTweetActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			final TimelineTweetActivity activity = reference.get();
			if (activity != null) {
				
				switch (msg.what) {
				case TweetModel.LOAD_NOTE_AS_ENTITY:
					if (msg.arg1 == ObservableModel.SUCCESS_INT)
						if (msg.arg2 == ObservableModel.SUCCESS_INT)
							activity.guid = (String) msg.obj;
				case TweetModel.INIT_LOAD:
					initEnvironment(activity);
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							onLoadFull(activity);
						} else
							onLoadEmpty(activity);
					} else {
						onLoadFail(activity);
					}
					break;
				case TweetModel.TAB_CHANGED:
					Object tag = activity.noMessageView.getTag();
					if (tag instanceof Integer) {
						int modelType = ((Integer) tag).intValue();
						int model = activity.tweetEntity.getModelType();
						if (modelType == model) {
							activity.noMessageView.setVisibility(View.VISIBLE);
							
						} else {
							activity.noMessageView.setVisibility(View.GONE);
						}
					}
					activity.adapter.notifyDataSetChanged();
					break;
				case TweetModel.CLICK_FAVOURITE:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						activity.invalidateOptionsMenu();
						activity.prepareFavoriteMenuItem();
					}
					break;
				case TweetModel.CLICK_RECOMMAND:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						activity.invalidateOptionsMenu();
						activity.prepareRecommendMenuItem();
						activity.executor.execute(activity.new TweetRunable(
								TweetModel.LOAD_RECOMMEND));
					}
					break;
				case TweetModel.POST_COMMENT:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						activity.executor.execute(activity.new TweetRunable(
								TweetModel.LOAD_COMMENT));
						activity.executor.execute(activity.new TweetRunable(
								TweetModel.LOAD_FORWARD));
						activity.executor.execute(activity.new TweetRunable(
								TweetModel.LOAD_RECOMMEND));
					}
					break;
				case TweetModel.POST_FORWARD:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						activity.executor.execute(activity.new TweetRunable(
								TweetModel.LOAD_FORWARD));
						activity.executor.execute(activity.new TweetRunable(
								TweetModel.LOAD_COMMENT));
					}
					break;
				case TweetModel.LOAD_NEXT_COMMENT:
				case TweetModel.LOAD_NEXT_FORWARD:
				case TweetModel.LOAD_NEXT_RECOMMEND:
					activity.loading.setVisibility(View.GONE);
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						if (msg.arg2 == ObservableModel.SUCCESS_INT) {
							updateTabs(msg, activity);
						}
					} else if (msg.arg1 == ObservableModel.FAIL_INT)
						Toast.makeText(activity, R.string.loading_fail,
								Toast.LENGTH_SHORT).show();
					break;
				case TweetModel.LOAD_COMMENT:
				case TweetModel.LOAD_FORWARD:
				case TweetModel.LOAD_RECOMMEND:
					activity.loading.setVisibility(View.GONE);
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						updateTabs(msg, activity);
					} else if (msg.arg1 == ObservableModel.FAIL_INT)
						Toast.makeText(activity, R.string.loading_fail,
								Toast.LENGTH_SHORT).show();
					break;
				case TweetModel.DELETE_ENTITY:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						Intent intent = new Intent();
						intent.putExtra(DELETE_ENTITY_ID, activity.guid);
						activity.setResult(DELETE_ENTITY, intent);
						activity.finish();
					}
					break;
				case TweetModel.DELETE_COMMENT:
					if (msg.arg1 == ObservableModel.SUCCESS_INT) {
						activity.executor.execute(activity.new TweetRunable(
								TweetModel.LOAD_FORWARD));
						activity.executor.execute(activity.new TweetRunable(
								TweetModel.LOAD_COMMENT));
					}
					break;
				case TweetModel.LOAD_MOCK:
//					activity.buttonGroup.setVisibility(View.GONE);
//					activity.listView.removeFooterView(activity.loading);
//					activity.initArticle(activity.mockEntity);
					activity.viewLoading.setVisibility(View.GONE);
					activity.tweetEntity.setLoading(false);
					activity.initArticle(activity.mockEntity);
					activity.invalidateOptionsMenu();
					activity.prepareFavoriteMenuItem();
					activity.prepareRecommendMenuItem();
					activity.setLoadingVisibility(false);
					activity.listView.setOnItemClickListener(activity);

					activity.listView.setOnItemLongClickListener(activity);

					activity.listView.setOnScrollListener(activity);
					break;
				}
			}
		}

		private void updateTabs(Message msg,
				final TimelineTweetActivity activity) {
			activity.tweetEntity.refreshData(msg.what);
			activity.adapter.notifyDataSetChanged();
			if(msg.what == TweetModel.LOAD_RECOMMEND)
				activity.setRecommendLinearLayout();
			else{
				activity.updateCommentLayout();
			}
			activity.resetResult(false);
		}

		private void onLoadFail(final TimelineTweetActivity activity) {
			activity.viewLoading.setVisibility(View.GONE);
			activity.tweetEntity.setLoading(true);
			activity.setLoadingVisibility(true);
			activity.loading.setVisibility(View.GONE);
		}

		private void onLoadEmpty(final TimelineTweetActivity activity) {
			activity.viewLoading.setVisibility(View.GONE);
			activity.resetResult(true);
			activity.tweetEntity.setLoading(true);
			activity.setLoadingVisibility(true);
			activity.loading.setVisibility(View.GONE);
			activity.loadError.setVisibility(View.VISIBLE);
			Intent intent = new Intent();
			intent.putExtra(DELETE_ENTITY_ID, activity.guid);
			activity.setResult(DELETE_ENTITY, intent);
			Toast.makeText(activity, R.string.delete_timeline, Toast.LENGTH_SHORT)
			.show();
		}

		private void onLoadFull(final TimelineTweetActivity activity) {
			initLoadFullEnvironment(activity);
			activity.executor.execute(activity.new TweetRunable(
					TweetModel.LOAD_COMMENT));
			activity.executor.execute(activity.new TweetRunable(
					TweetModel.LOAD_FORWARD));
			activity.executor.execute(activity.new TweetRunable(
					TweetModel.LOAD_RECOMMEND));
		}

		private void initLoadFullEnvironment(
				final TimelineTweetActivity activity) {
			activity.viewLoading.setVisibility(View.GONE);
			activity.tweetEntity.setLoading(false);
			activity.initArticle(activity.tweetEntity);
			activity.initTopBarView();
			activity.invalidateOptionsMenu();
			activity.prepareFavoriteMenuItem();
			activity.prepareRecommendMenuItem();
			activity.setLoadingVisibility(false);
			activity.listView.setOnItemClickListener(activity);

			activity.listView.setOnItemLongClickListener(activity);

			activity.listView.setOnScrollListener(activity);
		}

		private void initEnvironment(final TimelineTweetActivity activity) {
			activity.viewLoading.setVisibility(View.GONE);
			activity.commentHighLight(true);
			activity.recommendHighLight(false);
			activity.tweetEntity.setModelType(TweetModel.MODEL_COMMENT);
		}
	}

	public final static int MAX_COMMENT_TEXT = 3000;
	private final static int MAX_ORIGIN_TEXT = 200;
	public final static String NOTE_ID = "noteId";
	public final static String TWEET_GUID = "index";
	public final static String ENTITY = "entity";
	public final static int START_COMMENT_FROM_TWEET = 10;
	public final static int DELETE_ENTITY = 11;
	public final static int UPDATE_COMMENTS_NUMBER = 12;
	public final static int START_TWEET_FROM_QUOTE = 20;
	public final static String DELETE_ENTITY_ID = "delete_entity";
	public final static String ACTION_TO_COMMENT = "jumpToComment";
	private int lastItemIndex;
	private Executor executor;
	private String guid;
	private String nickname;
	private long noteId;
	private Entity mockEntity;
	private TweetModel tweetEntity;
	private TweetHandler handler;
	private TimelineCommentAdapter adapter;
	private View tweetRoot;
	private View header;
	private View article;
	private View loading;
	private View viewLoading;
	private LinearLayout noMessageView;
//	private View shareMenuItem;
	private View commentMenuItem;
	private View favoriteMenuItem;
	private View recommendMenuItem;
	private View forwardMenuItem;
	private View deleteMenuItem;
	private TextView loadError;
	private ListView listView;
	
	private LinearLayout recommendUserContainer;
	private TextView recommendcountTv;
	private int recommendContainerWidth;
	private LinearLayout countLinearLayout;
	private LinearLayout recommendLinearLayout;
	
//	private LinearLayout recommendBt;
	private ImageView recommendImageView;
//	private LinearLayout commentLinearLayout;
//	private LinearLayout tipLayout;
	private LinearLayout commentLayout;
	private EditText commentContent;
	private TextView replyTvBt;
	
	
	private Bundle commentBundle;
	// private SocialShareHelper helper;
//	private boolean hasSmartBar = false;
	
	public static int current_tab = 0;//当前tab位置 默认评论 1 是赞
	private RelativeLayout search_result_container;
	private Button loading_button;
	private boolean isPrivate = false;
	private String action;//区别是否从列表中直接进入评论或者是从详情中进入评论
	
	private Context context;
	private int recommendCount;
	private TopBarView topBarView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline_tweet);
		context = this;
//		hasSmartBar = UiStaticMethod.hasSmartBar();
		
		topBarView=getTopBarView();
		initField();
		initTweetView();
		initEvent();
		if (!NetWorkUtils.isNetworkConnected(TimelineTweetActivity.this)) {
			search_result_container.setVisibility(View.VISIBLE);
			tweetRoot.setVisibility(View.GONE);
			viewLoading.setVisibility(View.GONE);
		}else {

			if (guid != null) {
				executor.execute(new TweetRunable(TweetModel.INIT_LOAD));
			} else {
				if (noteId == -1) {
					handler.sendEmptyMessage(TweetModel.LOAD_MOCK);
				} else {
					executor.execute(new TweetRunable(
							TweetModel.LOAD_NOTE_AS_ENTITY));
				}
			}
		}
	}
	
	private void initTopBarView(){
		// 收藏动态
		final TweetModel entity = (TweetModel) tweetEntity;
		if(tweetEntity.getUser().getUserPin().equals(LoginUser.getpin())){
			topBarView.setRightMenuOneVisiable(true, R.drawable.btn_toolbar_delete, true);
		}else{
//			if (entity.isFavourite()) {
//				topBarView.setRightMenuOneVisiable(true, R.drawable.community_collected_icon, true);
//			} else {
//				topBarView.setRightMenuOneVisiable(true, R.drawable.community_not_collect_icon, true);
//			}
		}
		
		
		
		topBarView.setRightMenuTwoVisiable(true, R.drawable.btn_bar_share);

	}
	
	@Override
	public void onRightMenuOneClick() {
		super.onRightMenuOneClick();
		
		if(tweetEntity.getUser().getUserPin().equals(LoginUser.getpin())){
			UiStaticMethod
			.createConfirmDialog(this, R.string.delete_entity,
					R.string.delete_entity_confirm,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
							executor.execute(new TweetRunable(
									TweetModel.DELETE_ENTITY));
						}
					}).create().show();
		}else{
//			boolean nextState = !tweetEntity.isFavourite();
//			if (nextState) {
//				topBarView.setRightMenuOneVisiable(true, R.drawable.community_collected_icon, true);
//			} else {
//				topBarView.setRightMenuOneVisiable(true, R.drawable.community_not_collect_icon, true);
//			}
//			topBarView.setRightMenuTwoVisiable(true, R.drawable.btn_bar_share);
//			new CommunityUtil().clickFavourite(this, tweetEntity);

		}
				
	}
	
	@Override
	public void onRightMenuTwoClick() {
		super.onRightMenuTwoClick();
		showShareView();
	}
	
	@Override
	protected void onDestroy() {
		tweetEntity.deleteObserver(this);
		handler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case START_COMMENT_FROM_TWEET:
			if (resultCode == RESULT_OK) {
				commentBundle = data.getExtras();
				if (commentBundle
						.getBoolean(TimelineCommentsActivity.IS_COMMENT))
					executor.execute(new TweetRunable(TweetModel.POST_COMMENT));
				else
					executor.execute(new TweetRunable(TweetModel.POST_FORWARD));
			}
			else{
				//若为列表进入写评论页的返回，则直接关闭，回到列表页
				if(action!=null && action.equals(TimelineTweetActivity.ACTION_TO_COMMENT) )
					finish();
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
//		case R.id.timeline_tweet_comment:
//			Intent intent = setNewCommentIntent(true, null);
//			startActivityForResult(intent, START_COMMENT_FROM_TWEET);
//			return true;
//		case R.id.timeline_tweet_recommand:
//			executor.execute(new TweetRunable(TweetModel.CLICK_RECOMMAND));
//			return true;
//		case R.id.timeline_tweet_favourite:
//			executor.execute(new TweetRunable(TweetModel.CLICK_FAVOURITE));
//			return true;
//		case R.id.timeline_tweet_delte:
//			UiStaticMethod
//					.createConfirmDialog(this, R.string.delete_entity,
//							R.string.delete_entity_confirm,
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									dialog.dismiss();
//									executor.execute(new TweetRunable(
//											TweetModel.DELETE_ENTITY));
//								}
//							}).create().show();
//			return true;
//		case R.id.timeline_tweet_share:
//			showShareView();
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		Message message = (Message) data;
		handler.sendMessage(message);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, View view,
			final int position, long id) {
		final Comment comment = (Comment) parent.getAdapter().getItem(position);
		if (comment == null) {
			return;
		}
		if (TextUtils.isEmpty(comment.getContent())) {
			Intent intent = new Intent(TimelineTweetActivity.this,
					UserActivity.class);
			intent.putExtra(UserFragment.USER_ID, comment.getUser().getId());
			TimelineTweetActivity.this.startActivity(intent);
			return;
		}
//		int type;
//		if (comment.isCommentAuthor(this))
//			type = R.array.timeline_tweet_list_delte;
//		else
//			type = R.array.timeline_tweet_list_normal;
		// createDialog(parent, position, comment, type);

		boolean isComment = true;
		Intent intent = setNewCommentIntent(isComment, comment);
		intent.putExtra(TimelineCommentsActivity.REPLY_TO, ((Comment) parent
				.getAdapter().getItem(position)).getId());
		
		intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, ((Comment) parent
				.getAdapter().getItem(position)).getUser().getName());
		
		
//		initPopuptWindow(intent);
		startActivityForResult(intent, START_COMMENT_FROM_TWEET);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.message_recommend_layout:
//			tweetEntity.setModelType(TweetModel.MODEL_RECOMMEND);
//			commentHighLight(false);
//			recommendHighLight(true);
//			current_tab=1;
//			resetTabText();
//			break;
//		case R.id.message_comment_layout:
//			tweetEntity.setModelType(TweetModel.MODEL_COMMENT);
//			commentHighLight(true);
//			recommendHighLight(false);
//			current_tab=0;
//			resetTabText();
//			break;
//		case R.id.share_button:
//			showShareView();
//			break;
		case R.id.forward_button:
			Intent intent1 = setForwardIntent();
			startActivityForResult(intent1, START_COMMENT_FROM_TWEET);
			break;
		case R.id.comment_button:
			Intent intent = setNewCommentIntent(true, null);
			startActivityForResult(intent, START_COMMENT_FROM_TWEET);
			break;
		case R.id.favorite_button:
			executor.execute(new TweetRunable(TweetModel.CLICK_FAVOURITE));
			break;
//		case R.id.recommend_button:
//			executor.execute(new TweetRunable(TweetModel.CLICK_RECOMMAND));
//			break;
//		case R.id.delete_button:
//			UiStaticMethod
//					.createConfirmDialog(this, R.string.delete_entity,
//							R.string.delete_entity_confirm,
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									dialog.dismiss();
//									executor.execute(new TweetRunable(
//											TweetModel.DELETE_ENTITY));
//								}
//							}).create().show();
//			break;
		case R.id.loading_button:
			if (guid != null) {
				executor.execute(new TweetRunable(TweetModel.INIT_LOAD));
			} else {
				initEvent();
				if (noteId == -1) {
					handler.sendEmptyMessage(TweetModel.LOAD_MOCK);
				} else {
					executor.execute(new TweetRunable(
							TweetModel.LOAD_NOTE_AS_ENTITY));
				}
			}
			break;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& lastItemIndex == adapter.getCount()) {
			loading.setVisibility(View.VISIBLE);
			if (tweetEntity.isComments())
				executor.execute(new TweetRunable(TweetModel.LOAD_NEXT_COMMENT));
			else
				executor.execute(new TweetRunable(TweetModel.LOAD_NEXT_FORWARD));
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
	}

	private void prepareFavoriteMenuItem() {
		ImageView favorite = (ImageView) favoriteMenuItem
				.findViewById(R.id.favorite_image);
		if (tweetEntity.isFavourite()) {
			favorite.setImageResource(R.drawable.community_detail_collected_icon);
		} else {
			favorite.setImageResource(R.drawable.community_detail_uncollect_icon);
		}
		
//		if (nextState) {
//			topBarView.setRightMenuOneVisiable(true, R.drawable.community_collected_icon, true);
//		} else {
//			topBarView.setRightMenuOneVisiable(true, R.drawable.community_not_collect_icon, true);
//		}
	}

	private void prepareRecommendMenuItem() {
		if (tweetEntity.isTweetAuthor(this)) {
		} else {
			if (tweetEntity.isRecommand()) {
				recommendImageView.setImageResource(R.drawable.community_recommanded_big_icon_);
			} else {
				recommendImageView.setImageResource(R.drawable.community_unrecommand_big_icon);
			}
		}
	}

	private void showShareView() {
		Entity entity = getSupportedEntity();
		new CommunityShareUtil().getCommunityShareView(entity, TimelineTweetActivity.this).showAtLocation(
				TimelineTweetActivity.this.findViewById(R.id.main),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
	}

	private Entity getSupportedEntity() {
		Entity entity;
		if (mockEntity == null)
			entity = tweetEntity;
		else
			entity = mockEntity;
		return entity;
	}

	private void initEvent() {
//		recommendsButtonLayout.setOnClickListener(this);
//		commentsButtonLayout.setOnClickListener(this);
		setLoadingVisibility(true);
//		shareMenuItem.setOnClickListener(this);
		commentMenuItem.setOnClickListener(this);
		favoriteMenuItem.setOnClickListener(this);
//		recommendMenuItem.setOnClickListener(this);
//		deleteMenuItem.setOnClickListener(this);
		loading_button.setOnClickListener(this);
		forwardMenuItem.setOnClickListener(this);
	}

	private void initTweetView() {
		search_result_container = (RelativeLayout) findViewById(R.id.search_result_container);
		loading_button = (Button) findViewById(R.id.loading_button);
		tweetRoot = findViewById(R.id.timeline_tweet_root);
		loadError = (TextView) findViewById(R.id.timeline_tweet_load_error);
		listView = (ListView) findViewById(R.id.timeline_tweet_list);
		viewLoading = findViewById(R.id.screen_loading);
		header = View.inflate(this, R.layout.header_activity_timeline_tweet,
				null);
		loading = View.inflate(this, R.layout.view_loading, null);
		article = header.findViewById(R.id.timeline_tweet_item);
//		buttonGroup = header.findViewById(R.id.message_button_group);
//	
//		recommendsButton = (TextView) header
//				.findViewById(R.id.message_recommend);
//		commentsButton = (TextView) header.findViewById(R.id.message_comment);
//		recommendsButtonLayout = header
//				.findViewById(R.id.message_recommend_layout);
//		commentsButtonLayout = header.findViewById(R.id.message_comment_layout);
//		recommendsHighLight = header.findViewById(R.id.message_recommend_hl);
//		commentsHighLight = header.findViewById(R.id.message_comment_hl);
		
		recommendLinearLayout = (LinearLayout) header.findViewById(R.id.recommendLinearLayout);
		recommendUserContainer = (LinearLayout) header.findViewById(R.id.recommendUserContainer);
		recommendcountTv = (TextView) header.findViewById(R.id.recommendcount);
		countLinearLayout =  (LinearLayout) header.findViewById(R.id.countLinearLayout);
		recommendImageView =  (ImageView)header.findViewById(R.id.recommendImageview);
		
		if(recommendContainerWidth==0){
			recommendUserContainer.getViewTreeObserver().addOnGlobalLayoutListener(
			        new OnGlobalLayoutListener() {
			            @SuppressWarnings("deprecation")
						@Override
			            public void onGlobalLayout() {
			            	if(recommendContainerWidth==0)
			            		recommendContainerWidth = recommendUserContainer.getWidth();
			                recommendUserContainer.getViewTreeObserver()
			                        .removeGlobalOnLayoutListener(this);
			            }
			});
		}
		
		noMessageView = (LinearLayout) header.findViewById(R.id.no_message_view);
//		recommendBt=(LinearLayout) findViewById(R.id.recommend_area);
//		recommendImageView =  (ImageView) findViewById(R.id.recommendImageview);
//		tipLayout = (LinearLayout) findViewById(R.id.tipLayout);
//		commentLayout = (LinearLayout) findViewById(R.id.commentLayout);
//		commentContent =  (EditText) findViewById(R.id.commentContent);
		
		recommendImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				executor.execute(new TweetRunable(TweetModel.CLICK_RECOMMAND));
			}
		});
		
//		commentLinearLayout =(LinearLayout) findViewById(R.id.commentLinearLayout);
//		commentLinearLayout.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = setNewCommentIntent(true, null);
////				initPopuptWindow(intent);
//				startActivityForResult(intent, START_COMMENT_FROM_TWEET);
//			}
//		});
		
		
//		shareMenuItem = tweetRoot.findViewById(R.id.share_button);
		commentMenuItem = tweetRoot.findViewById(R.id.comment_button);
		favoriteMenuItem = tweetRoot.findViewById(R.id.favorite_button);
//		recommendMenuItem = tweetRoot.findViewById(R.id.recommend_button);
		forwardMenuItem = tweetRoot.findViewById(R.id.forward_button);
//		deleteMenuItem = tweetRoot.findViewById(R.id.delete_button);
		listView.setRecyclerListener(adapter);
		listView.addHeaderView(header, null, false);
		listView.addFooterView(UiStaticMethod.getFooterParent(this, loading));
		listView.setAdapter(adapter);
		viewLoading.setVisibility(View.VISIBLE);
		//tweetRoot.findViewById(R.id.bottom_menu_layout).setVisibility(
		//		hasSmartBar ? View.GONE : View.VISIBLE);
		
	}

	private void initField() {
//		View rootView = findViewById(R.id.timeline_tweet_root);
		Intent intent = getIntent();
		
		mockEntity = (Entity) intent.getParcelableExtra(ENTITY);
		isPrivate = intent.getBooleanExtra("isPrivate", false);
		guid = intent.getStringExtra(TWEET_GUID);
		noteId = intent.getLongExtra(NOTE_ID, -1);
		nickname=intent.getStringExtra(TimelineCommentsActivity.USER_NINKNAME);

		tweetEntity = new TweetModel();
		tweetEntity.addObserver(this);
		handler = new TweetHandler(this);
		adapter = new TimelineCommentAdapter(this, tweetEntity);
		executor = NotificationService.getExecutorService();
		
		//判断是否列表页点击评论，若是直接进入写评论页面
		action= intent.getStringExtra("ActionType");
	}
	
	 /** 
     * 创建写书评PopupWindow 
     */  
    protected void initPopuptWindow(final Intent intent) {  
//    	tipLayout.setVisibility(View.GONE);
//    	commentLayout.setVisibility(View.VISIBLE);
//    	
//		commentContent.requestFocus();
//		final InputMethodManager imm=  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//		
//		String ninknameString = intent.getStringExtra(TimelineCommentsActivity.USER_NINKNAME);
//
//		if (!TextUtils.isEmpty(ninknameString)) {
//			commentContent.setHint("回复 " + ninknameString + ":");
//		}
//		
//		replyTvBt.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(context, "click", 0).show();
//				String content = commentContent.getText().toString();
//				if (content!=null && content.length() == 0) {
//					Toast.makeText(context,
//							R.string.post_without_word, Toast.LENGTH_SHORT).show();
//				} else if (content!=null && content.length() > MAX_COMMENT_TEXT) {
//					Toast.makeText(context,
//							R.string.max_comment_text, Toast.LENGTH_SHORT).show();
//				} else {
//					intent.putExtra(TimelineCommentsActivity.USER_COMMENT, content);
//					commentBundle = intent.getExtras();
//					if (commentBundle
//							.getBoolean(TimelineCommentsActivity.IS_COMMENT))
//						executor.execute(new TweetRunable(TweetModel.POST_COMMENT));
//					else
//						executor.execute(new TweetRunable(TweetModel.POST_FORWARD));
//					
//					tipLayout.setVisibility(View.VISIBLE);
//			    	commentLayout.setVisibility(View.GONE);
//			    	commentContent.setText("");
//			    	imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//				}
//				
//				
//
//			}
//		});
		
		
		String ninknameString = intent.getStringExtra(TimelineCommentsActivity.USER_NINKNAME);

    	PopupWindow menuWindow = new SharePopupWindow(
				TimelineTweetActivity.this, new OnPopItemClickedListener() {

					@Override
					public void onPopItemClicked(String content, int position) {
						intent.putExtra(TimelineCommentsActivity.USER_COMMENT, content);
						commentBundle = intent.getExtras();
						if (commentBundle
								.getBoolean(TimelineCommentsActivity.IS_COMMENT))
							executor.execute(new TweetRunable(TweetModel.POST_COMMENT));
						else
							executor.execute(new TweetRunable(TweetModel.POST_FORWARD));
					}
				}, ninknameString, 1);
		menuWindow.showAtLocation(
				TimelineTweetActivity.this.findViewById(R.id.main),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);  
    }  
    
    /**
	 * 当用户引用的字符数超过200的时候，这个方法负责截断字符串
	 * 
	 * @param text
	 *            待截断的字符串
	 * @return 如果用户引用的字符数没有超过200，则返回原字符串，否则返回截断后的字符串。
	 */
	private String chopOriginText(String text) {
		String result = null;
		if (text == null) {
			result = text;
		} else {
			if (text.length() <= MAX_ORIGIN_TEXT)
				result = text;
			else {
				Pattern at = UiStaticMethod.AT_NAME;
				Matcher matcher = at.matcher(text);
				int end = -1;
				int temp;
				while (matcher.find()) {
					temp = matcher.end();
					if (temp <= MAX_ORIGIN_TEXT)
						end = temp;
					else
						break;
				}
				if (end == -1) {
					result = text;
				} else {
					text.substring(0, end);
				}
			}
		}
		return result;
	}

	private void commentHighLight(boolean isHighLight) {
//		if (isHighLight) {
//			commentsButtonLayout.setEnabled(false);
//			commentsButton.setTextColor(getResources().getColor(
//					R.color.text_main));
//			commentsHighLight.setVisibility(View.VISIBLE);
//		} else {
//			commentsButtonLayout.setEnabled(true);
//			commentsButton.setTextColor(getResources().getColor(
//					R.color.text_sub));
//			commentsHighLight.setVisibility(View.GONE);
//		}
	}

	private void recommendHighLight(boolean isHighLight) {
//		if (isHighLight) {
//			recommendsButtonLayout.setEnabled(false);
//			recommendsButton.setTextColor(getResources().getColor(
//					R.color.text_main));
//			recommendsHighLight.setVisibility(View.VISIBLE);
//		} else {
//			recommendsButtonLayout.setEnabled(true);
//			recommendsButton.setTextColor(getResources().getColor(
//					R.color.text_sub));
//			recommendsHighLight.setVisibility(View.GONE);
//		}
	}

//	/**
//	 * 改变底部工具条的显示状态
//	 * 
//	 * @param recommand
//	 *            赞
//	 * @param favourite
//	 *            收藏
//	 * @param delete
//	 *            删除
//	 * @param guest
//	 *            true表示当前用户为游客，不是原作者，false表示当前用户为原作者
//	 */
//	private void changeBottomVisibility(MenuItem recommand, MenuItem favourite,
//			MenuItem delete, boolean guest) {
//
//		recommand.setEnabled(guest).setVisible(guest);
//		favourite.setEnabled(true).setVisible(true);
//		delete.setEnabled(!guest).setVisible(!guest);
//	}

	/**
	 * 设置启动TimelineCommentsActiviy的Intent。
	 * 
	 * @param isComment
	 *            true表示当前用户准备评论该条动态，false表示当前用户准备转发该条动态。
	 * @param comment
	 *            待转发的comment，如果用户要转发当前动态，这个参数为空。否则为待转发的comment
	 * @return 处理后的Intent
	 */
	private Intent setNewCommentIntent(boolean isComment, Comment comment) {
		Intent intent = new Intent(this, TimelineCommentsActivity.class);
		intent.putExtra(TimelineCommentsActivity.ENTITY_GUID, guid);
		intent.putExtra(TimelineCommentsActivity.IS_COMMENT, isComment);
		intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, nickname);
		
		if (comment != null) {
			intent.putExtra(TimelineCommentsActivity.ORIGIN_CONTENT,
					comment.getContent());
		} else if (tweetEntity.getRenderBody().hasEntity())
			intent.putExtra(TimelineCommentsActivity.ORIGIN_CONTENT,
					UiStaticMethod.formatListItem(tweetEntity.getRenderBody()
							.getContent()));
		return intent;
		
	}
	
	/**
	 * 转发的Intent
	 * @return
	 */
	private Intent setForwardIntent(){
		Intent intent = setNewCommentIntent(false,null);
		if(intent!=null){
			String imageUrl="";
			if(tweetEntity.getImages()!=null && tweetEntity.getImages().size()>0){
				imageUrl=tweetEntity.getImages().get(0);
			}else if(tweetEntity.getBook()!=null && tweetEntity.getBook().getCover()!=null){
				imageUrl=tweetEntity.getBook().getCover();
			}else if(tweetEntity.getUser()!=null && tweetEntity.getUser().getAvatar()!=null){
				imageUrl = tweetEntity.getUser().getAvatar();
			}
			intent.putExtra(TimelineCommentsActivity.FORWARD_IMAGE, imageUrl);
			
			String content="";
			if(tweetEntity.getRenderBody().getContent()!=null && !"".equals(tweetEntity.getRenderBody().getContent())){
				content=tweetEntity.getRenderBody().getContent();
			}else if(tweetEntity.getRenderBody().getQuote()!=null && !"".equals(tweetEntity.getRenderBody().getQuote())){
				content= tweetEntity.getRenderBody().getQuote();
			}else
				content="转发动态";
			intent.putExtra(TimelineCommentsActivity.FORWARD_CONTENT, content);
		}
		return intent;
	}

	/**
	 * 填充listview的表头,可能是mock对象，可能是真实Entity
	 * 
	 * @param entity
	 *            数据源
	 * @param header
	 *            表头
	 */
	private void initArticle(Entity entity) {
		initUserRegion(entity);
		View quotationContainer = article.findViewById(R.id.quotationContainer);
		TextView quotationName = (TextView) article
				.findViewById(R.id.timeline_quotation_user_name);
		TextAreaForDetail mainText = (TextAreaForDetail) article.findViewById(R.id.mainTextArea);
		TextAreaForDetail quotationText = (TextAreaForDetail) article
				.findViewById(R.id.quotationTextArea);
		RenderBody renderBody = entity.getRenderBody();
		String content, quote, subContent = "", subQuote = "";
		content = renderBody.getContent();
		quote = renderBody.getQuote();
		renderBody.setContent(UiStaticMethod.formatConcreteTweet(content));
		renderBody.setQuote(UiStaticMethod.formatConcreteTweet(quote));
		if (renderBody.hasEntity()
				&& renderBody.getEntity().getRenderBody() != null) {
			subContent = renderBody.getEntity().getRenderBody().getContent();
			subQuote = renderBody.getEntity().getRenderBody().getQuote();
			renderBody.getEntity().getRenderBody()
					.setContent(UiStaticMethod.formatConcreteTweet(subContent));
			renderBody.getEntity().getRenderBody()
					.setQuote((UiStaticMethod.formatConcreteTweet(subQuote)));
		}
		mainText.parseEntity(entity, true,2);
		ArticleHelper.initQuotationForDetail(quotationContainer, quotationName,
				quotationText, renderBody, true);
		if (tweetEntity.getRenderBody().hasEntity()) {
			quotationText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TimelineTweetActivity.this,
							TimelineTweetActivity.class);
					intent.putExtra(TWEET_GUID, tweetEntity.getRenderBody()
							.getEntity().getGuid());
					startActivityForResult(intent, START_TWEET_FROM_QUOTE);
				}
			});
		}
		renderBody.setContent(content);
		renderBody.setQuote(quote);
		if (renderBody.hasEntity()
				&& renderBody.getEntity().getRenderBody() != null) {
			renderBody.getEntity().getRenderBody().setContent(subContent);
			renderBody.getEntity().getRenderBody().setQuote(subQuote);
		}
		
		if(action!=null && action.equals("jumpToComment"))
		{
			action=null;
			Intent intent1 = setNewCommentIntent(true, null);
			startActivityForResult(intent1, START_COMMENT_FROM_TWEET);
			finish();
//			initPopuptWindow(intent1);
		}
	}

	/**
	 * 初始化个人信息区域
	 * @param entity
	 */
	private void initUserRegion(Entity entity) {
		viewLoading.setVisibility(View.GONE);
		View userRegin = article.findViewById(R.id.timeline_user_region);
		ImageView avatar = (ImageView) article.findViewById(R.id.thumb_nail);
		ImageView avatarLabel = (ImageView) article
				.findViewById(R.id.avatar_label);
		TextView updateTime = (TextView) article
				.findViewById(R.id.timeline_update_time);
		TextView userName = (TextView) article
				.findViewById(R.id.timeline_user_name);
		UserInfo userInfo = entity.getUser();
		long timeStamp = ArticleHelper.getTime(entity, entity.getRenderBody());
		userRegin.setOnClickListener(new JumpToUser(this, userInfo));
		ArticleHelper.initAvatar(this, avatar, userInfo);
		ArticleHelper.initUserName(this, userName, avatarLabel, userInfo);
		updateTime.setText(TimeFormat.formatTime1(getResources(), timeStamp));
	}

	/**
	 * 为当前Activity设置返回值。返回值包括，评论数量，转发数量，当前动态是否被删除
	 * 
	 * @param delete
	 *            true表示当前动态被删除，false表示当前动态没有被删除。
	 */
	private void resetResult(boolean delete) {
		Intent intent = new Intent();
		intent.putExtra(TweetModel.COMMENT_NUMBER,
				tweetEntity.getCommentNumber());
		intent.putExtra(TweetModel.FORWARD_NUMBER,
				tweetEntity.getForwardNumber());
		intent.putExtra(TweetModel.RECOMMENTS_COUNT,
				tweetEntity.getRecommendsCount());
		intent.putExtra(TweetModel.VIEWERRECOMMENDED,
				tweetEntity.isRecommand());
		intent.putExtra(TweetModel.IS_DELETE, delete);
		intent.putExtra(TWEET_GUID, guid);
		intent.putExtra(DELETE_ENTITY_ID, guid);
		setResult(UPDATE_COMMENTS_NUMBER, intent);
	}

	/**
	 * 设置初始加载时，view的可视性
	 * 
	 * @param loading
	 *            true表示初始加载，则只显示loading，隐藏其他view。false表示加载完毕，隐藏loading，显示其他view
	 *            。
	 */
	private void setLoadingVisibility(boolean loading) {
		if (loading) {
			this.loading.setVisibility(View.VISIBLE);
			header.setVisibility(View.GONE);
			article.setVisibility(View.GONE);
//			buttonGroup.setVisibility(View.GONE);
			tweetRoot.setVisibility(View.GONE);
		} else {
			this.loading.setVisibility(View.GONE);
			header.setVisibility(View.VISIBLE);
			article.setVisibility(View.VISIBLE);
			tweetRoot.setVisibility(View.VISIBLE);
			if (isPrivate) {
//				buttonGroup.setVisibility(View.GONE);
				tweetRoot.findViewById(R.id.bottom_menu_layout).setVisibility(View.GONE);
			}else {
//				buttonGroup.setVisibility(View.VISIBLE);
			}
		}
	}
	
	/**
	 * 显示推荐区域
	 */
	@SuppressLint("InflateParams")
	private void setRecommendLinearLayout(){
		recommendCount = tweetEntity.getRecommendsCount();
		if(recommendCount<=0){
			recommendLinearLayout.setVisibility(View.GONE);
			return ;
		}
		recommendLinearLayout.setVisibility(View.VISIBLE);
		recommendUserContainer.removeAllViews();
		
		LinearLayout avatar = (LinearLayout) LayoutInflater.from(TimelineTweetActivity.this)
				.inflate(R.layout.bookstore_bookinfo_readed_item_style,
						null);
		RoundNetworkImageView image=null;
		int count=0 ;
		UserInfo userInfo;
		image=(RoundNetworkImageView) avatar.findViewById(R.id.thumb_nail);
		int imageWidth=image.getLayoutParams().width;
		int spaceWidth=imageWidth/4;
		count = recommendContainerWidth /(imageWidth+spaceWidth);
		for (int i = 0; i < count; i++) {
			if(i<recommendCount){
				avatar = (LinearLayout) LayoutInflater.from(TimelineTweetActivity.this)
						.inflate(R.layout.bookstore_bookinfo_readed_item_style,
								null);
				image=(RoundNetworkImageView) avatar.findViewById(R.id.thumb_nail);
				Comment comment = (Comment) tweetEntity.getRecommendAt(i);
				if(comment!=null){
					userInfo = comment.getUser();
					ArticleHelper.initAvatar(context, image, userInfo);
				}
				recommendUserContainer.addView(avatar);
			}
		}
		if(count<recommendCount){
			countLinearLayout.setVisibility(View.VISIBLE);
			recommendcountTv.setText(recommendCount+"");
			countLinearLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent  = new Intent(context,CommunityUserListActivity.class);
					intent.putExtra("guid", tweetEntity.getGuid());
					startActivity(intent);
				}
			});
		}
		else
			countLinearLayout.setVisibility(View.GONE);
	}

	
	private void updateCommentLayout() {
		int commentNumber = tweetEntity.hasComments() ? tweetEntity.getCommentNumber()
				: tweetEntity.getOriginCommentNumber();
		 if (commentNumber == 0) {
			if (tweetEntity.getModelType() == TweetModel.MODEL_COMMENT) {
				noMessageView.setVisibility(View.VISIBLE);
			} else {
				noMessageView.setVisibility(View.GONE);
			}
		}  else {
			noMessageView.setVisibility(View.GONE);
		}
	}
	
	public interface OnShareItemClickedListener {
		void onShareItemClicked(int type, int position);
	}
	
	public interface OnPopItemClickedListener {
		void onPopItemClicked(String content, int position);
	}

//	public class SharePopupWindow extends PopupWindow {
//
//		private View mMenuView;
//
//		public SharePopupWindow(Activity context,
//				final OnShareItemClickedListener itemsOnClick,
//				boolean showDelete, final int position) {
//			super(context);
//			LayoutInflater inflater = (LayoutInflater) context
//					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			mMenuView = inflater.inflate(
//					R.layout.activity_timeline_popupwindow, null);
//
//			FrameLayout delete = (FrameLayout) mMenuView
//					.findViewById(R.id.delete);
//			FrameLayout copy = (FrameLayout) mMenuView.findViewById(R.id.copy);
//
//			if (!showDelete)
//				delete.setVisibility(View.GONE);
//
//			delete.setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					if (itemsOnClick != null)
//						itemsOnClick.onShareItemClicked(101, position);
//					dismiss();
//
//				}
//			});
//
//			copy.setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					if (itemsOnClick != null)
//						itemsOnClick.onShareItemClicked(102, position);
//					dismiss();
//				}
//			});
//
//			this.setContentView(mMenuView);
//			this.setWidth(LayoutParams.MATCH_PARENT);
//			this.setHeight(LayoutParams.WRAP_CONTENT);
//			this.setFocusable(true);
//			this.setTouchable(true);
//			this.setOutsideTouchable(true);
//			this.setBackgroundDrawable(new ColorDrawable(getResources()
//					.getColor(R.color.bg_menu_shadow)));
//
//		}
//		
//		public SharePopupWindow(Activity context,
//				final OnShareItemClickedListener itemsOnClick,final int position) {
//			super(context);
//			LayoutInflater inflater = (LayoutInflater) context
//					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			mMenuView = inflater.inflate(
//					R.layout.activity_share_popupwindow, null);
//
//			RelativeLayout weibo = (RelativeLayout) mMenuView
//					.findViewById(R.id.popup_sina);
//			RelativeLayout wechat_friend = (RelativeLayout) mMenuView.findViewById(R.id.popup_weixin_friend);
//			
//			RelativeLayout wechat = (RelativeLayout) mMenuView.findViewById(R.id.popup_weixin);
//			LinearLayout cancel = (LinearLayout) mMenuView.findViewById(R.id.cancel);
//			weibo.setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					if (itemsOnClick != null)
//						itemsOnClick.onShareItemClicked(101, position);
//					dismiss();
//				}
//			});
//			
//			cancel.setOnClickListener(new View.OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					dismiss();
//				}
//			});
//			
//			wechat.setOnClickListener(new View.OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					if (itemsOnClick != null)
//						itemsOnClick.onShareItemClicked(103, position);
//					dismiss();
//				}
//			});
//
//			wechat_friend.setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					if (itemsOnClick != null)
//						itemsOnClick.onShareItemClicked(102, position);
//					dismiss();
//				}
//			});
//
//			this.setContentView(mMenuView);
//			this.setWidth(LayoutParams.MATCH_PARENT);
//			this.setHeight(LayoutParams.MATCH_PARENT);
//			this.setFocusable(true);
//			this.setTouchable(true);
//			this.setOutsideTouchable(true);
//			this.setBackgroundDrawable(new ColorDrawable(getResources()
//					.getColor(R.color.bg_menu_shadow)));
//
//		}
//
//	}

	/**
	 * 长按列表项
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		position = position - 1;
		boolean isCurrentUserComment = false;
		
		if(tweetEntity!=null&&tweetEntity.getModelType() == TweetModel.MODEL_RECOMMEND)
		{
			return false;
		}
		
		if(adapter==null||position<0) return false;
		Comment temp = (Comment) adapter.getItem(position);

		if (temp.getUser().getUserPin().equals(LoginUser.getpin())) {//判断当前用户是否评论作者
			isCurrentUserComment = true;
		}

		PopupWindow menuWindow = new SharePopupWindow(
				TimelineTweetActivity.this, new OnShareItemClickedListener() {

					@Override
					public void onShareItemClicked(int type, int position) {

						switch (type) {
						case 101:// 删除

							final Comment comment = (Comment) adapter
									.getItem(position);

							if (comment == null) {
								return;
							}
							Builder builder = UiStaticMethod
									.createConfirmDialog(
											TimelineTweetActivity.this,
											R.string.delete_comment,
											R.string.delete_comment_confirm,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
													executor.execute(new TweetRunable(
															TweetModel.DELETE_COMMENT,
															comment));

												}
											});

							builder.create().show();

							break;

						case 102:// 复制

							final Comment content = (Comment) adapter
									.getItem(position);

							ToastUtil.showToastInThread("回复内容已复制到剪贴板",
									Toast.LENGTH_SHORT);

							ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
							ClipData clip = ClipData.newPlainText(
									"simple text", content.getContent());
							clipboard.setPrimaryClip(clip);

							break;
						}

					}
				}, isCurrentUserComment, position);
		menuWindow.showAtLocation(
				TimelineTweetActivity.this.findViewById(R.id.main),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_community_dongtaixiangqing));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_community_dongtaixiangqing));
	}

}
