package com.jingdong.app.reader.timeline.actiivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.DraftsActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.DraftsActivity;
import com.jingdong.app.reader.activity.DraftsActivity.Draft;
import com.jingdong.app.reader.activity.DraftsActivity.Drafts;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.community.FriendCircleFragment.TimelineFragmentRunnable;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.KeyBoardUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineCommentsActivity extends BaseActivityWithTopBar implements
		TopBarViewListener {

	private final static int MAX_COMMENT_TEXT = 3000;
	private final static int MAX_ORIGIN_TEXT = 200;
	public static final String ENTITY_GUID = "entityGuid";
	public static final String IS_COMMENT = "isComment";
	public static final String ORIGIN_CONTENT = "originContent";
	public static final String REPLY_TO = "replyTo";
	public static final String USER_COMMENT = "user_comment";
	public static final String CHECKED = "checked";
	public static final String USER_NINKNAME = "nick_name";
	public static final String FORWARD_IMAGE = "forward_image";
	public static final String FORWARD_CONTENT = "forward_content";
	private EditText userComment;
	private CheckBox checkBox;
	private boolean commment;

	public static final int SEARCH_BOOK = 711;
	public static final int SEARCH_PEOPLE = 811;

	private StringBuffer bookIdBuffer;
	private TopBarView topBarView = null;
	private Draft draft = null;
	private StringBuffer userIdBuffer;
	private List<Map<String, String>> bookNames = new ArrayList<Map<String, String>>();
	private String type;
	private String forwardContent;
	private String forwardImage;
	private RelativeLayout forwardLayout;
	private ImageView forwardImageview;
	private TextView forwardNicknameTv,forwardContentTv;
	private String ninknameString;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline_comments);

		Intent intent = getIntent();
		commment = intent.getBooleanExtra(IS_COMMENT, true);
		ninknameString = intent.getStringExtra(USER_NINKNAME);
		
		forwardContent = intent.getStringExtra(FORWARD_CONTENT);
		forwardImage = intent.getStringExtra(FORWARD_IMAGE);

		userComment = (EditText) findViewById(R.id.timeline_comments_content);
		checkBox = (CheckBox) findViewById(R.id.forward_or_comment);
		
		forwardLayout = (RelativeLayout) findViewById(R.id.forwardlayout);
		forwardImageview = (ImageView) findViewById(R.id.forward_image);
		forwardNicknameTv = (TextView) findViewById(R.id.forward_nickname);
		forwardContentTv = (TextView) findViewById(R.id.forward_content);
		
		if (!TextUtils.isEmpty(ninknameString)) {
			userComment.setHint("回复 " + ninknameString + ":");
		}

		draft = (Draft) getIntent().getSerializableExtra("draft");

		topBarView = getTopBarView();
		initTopBar();

		initTitleBottom(commment);

		bookIdBuffer = new StringBuffer();
		userIdBuffer = new StringBuffer();

		findViewById(R.id.mention_book).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								TimelineCommentsActivity.this,
								TimelineSearchBookActivity.class);
						startActivityForResult(intent, SEARCH_BOOK);
					}
				});

		findViewById(R.id.timeline_tweet_at).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(
								TimelineCommentsActivity.this,
								TimelineSearchPeopleActivity.class);
						startActivityForResult(intent, SEARCH_PEOPLE);
					}
				});

		if (draft != null) {
			userComment.setText(draft.content);
			userIdBuffer.append(draft.useridBuffer);
			
			if(!commment){
				forwardLayout.setVisibility(View.VISIBLE);
				checkBox.setVisibility(View.GONE);
				forwardContentTv.setText(draft.forwardContent);
				
				if(draft.forwardImage!=null && !"".equals(draft.forwardImage))
					ImageLoader.getInstance().displayImage(draft.forwardImage, forwardImageview,GlobalVarable.getCutBookDisplayOptions(false));
				if(draft.forwardNickname!=null && !"".equals(draft.forwardNickname))
					forwardNicknameTv.setText(draft.forwardNickname);
			}
			else{
				forwardLayout.setVisibility(View.GONE);
			}
		}else{
			//若为转发 --tmj
			if(!commment){
				forwardLayout.setVisibility(View.VISIBLE);
				checkBox.setVisibility(View.GONE);
				
				ImageLoader.getInstance().displayImage(forwardImage, forwardImageview,GlobalVarable.getDefaultCommunityDisplayOptions(false));
				forwardNicknameTv.setText(ninknameString);
				forwardContentTv.setText(Html.fromHtml(forwardContent).toString());
				
				userComment.setHint("说说分享心得…");
			}else
				forwardLayout.setVisibility(View.GONE);
		}
	}

	public void initTopBar() {
		if (topBarView == null)
			return;
		int titleId;
		if (commment)
			titleId = R.string.reply;
		else
			titleId = R.string.forward;
		topBarView.setTitle(getResources().getString(titleId));
		topBarView.setLeftMenuVisiable(true, "取消", R.color.red_main);
		topBarView.setRightMenuOneVisiable(true, "发布", R.color.red_main, false);
		topBarView.setListener(this);
	}

	@Override
	public void onLeftMenuClick() {
		String content = userComment.getText().toString();

		if (draft == null && !TextUtils.isEmpty(content))

		{
			DialogManager.showCommonDialog(TimelineCommentsActivity.this, "提示",
					"是否保存到草稿箱?", "保存草稿", "不保存",
					new DialogInterface.OnClickListener() {

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
		} else {
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		String content = userComment.getText().toString();

		if (draft == null && !TextUtils.isEmpty(content))

		{
			DialogManager.showCommonDialog(TimelineCommentsActivity.this, "提示",
					"是否保存到草稿箱?", "保存草稿", "不保存",
					new DialogInterface.OnClickListener() {

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
		} else {
			finish();
		}
	}

	public void saveDraft() {
		String content = userComment.getText().toString();

		Toast.makeText(TimelineCommentsActivity.this, "评论已经保存到草稿箱",
				Toast.LENGTH_LONG).show();
		Draft draft = new Draft();
		draft.type = DraftsActivity.TYPE_POST_COMMENT;
		draft.content = content;
		draft.time = System.currentTimeMillis();
		draft.bookidBuffer = bookIdBuffer.toString();
		draft.useridBuffer=userIdBuffer.toString();
		// 评论的时候

		Bundle bundle = getIntent().getExtras();
		draft.entity_guid = bundle.getString(ENTITY_GUID);
		draft.origin_content = chopOriginText(bundle.getString(ORIGIN_CONTENT));
		draft.replyTo = bundle
				.getLong(REPLY_TO, TweetModel.DEFAULT_ERROR_VALUE);

		//转发保存草稿 -- tmj
		if(!commment){
			draft.forwardImage = forwardImage;
			draft.forwardNickname = ninknameString;
			draft.forwardContent = forwardContent;
			draft.type = DraftsActivity.TYPE_POST_FORWARD;
		}
		
		List<Draft> list = LocalUserSetting
				.getDraftsList(TimelineCommentsActivity.this);
		if (list == null)
			list = new ArrayList<Draft>();
		list.add(draft);

		Drafts drafts = new Drafts();
		drafts.drafts = list;
		LocalUserSetting.saveDraftsList(TimelineCommentsActivity.this, drafts);

	}

	@Override
	public void onRightMenuOneClick() {
		Intent intent = new Intent();
		if (userComment.getText().length() == 0 && commment) {
			Toast.makeText(TimelineCommentsActivity.this,
					R.string.post_without_word, Toast.LENGTH_SHORT).show();
		} else if (userComment.getText().toString().length() > MAX_COMMENT_TEXT) {
			Toast.makeText(TimelineCommentsActivity.this,
					R.string.max_comment_text, Toast.LENGTH_SHORT).show();
		} else {
			Bundle bundle = getIntent().getExtras();
			intent.putExtra(ENTITY_GUID, bundle.getString(ENTITY_GUID));
			intent.putExtra(IS_COMMENT, bundle.getBoolean(IS_COMMENT));
			intent.putExtra(ORIGIN_CONTENT,
					chopOriginText(bundle.getString(ORIGIN_CONTENT)));
			
			intent.putExtra(TimelinePostTweetActivity.TWEET_USERS, userIdBuffer.toString());

			String contentString = userComment.getText().toString();

			for (int i = 0; i < bookNames.size(); i++) {

				Map<String, String> temp = bookNames.get(i);
				String bookname = temp.get("bookname");
				String bookid = temp.get("bookid");

				Pattern pattern = Pattern.compile("(《" + bookname + "》)");
				Matcher matcher = pattern.matcher(contentString);

				String str = "";
				while (matcher.find()) {
					str = matcher.replaceAll("<a href='/books/more/" + bookid
							+ "'>" + matcher.group(1) + "</a>");
				}

				contentString = str;
			}

			intent.putExtra(USER_COMMENT, contentString);

			intent.putExtra(CHECKED, checkBox.isChecked());
			
			//若为转发，则要求同时评论并转发 --tmj
			if(!commment){
				intent.putExtra(CHECKED, true);
				if(contentString == null ||contentString.equals(""))
					intent.putExtra(USER_COMMENT, "转发动态");
			}
			
			intent.putExtra(REPLY_TO,
					bundle.getLong(REPLY_TO, TweetModel.DEFAULT_ERROR_VALUE));
//			if (null != draft) {
//				setResult(DraftsActivity.TYPE_POST_COMMENT, intent);
//			} else {
//				setResult(RESULT_OK, intent);
//			}
			
			CommonUtil.toggleSoftInput(this);
			KeyBoardUtils.closeKeybord(userComment,this);
			postComment(this, intent);
		}
	}
	
	
	/**
	 * 向服务器发送一条评论或转发
	 * 
	 * @param context
	 *            数据上下文
	 * @param bundle
	 *            创建一条评论所需要的数据。
	 */
	public void postComment(final Context context, final Intent intent) {

		final Bundle bundle = intent.getExtras();
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				WebRequestHelper.post(URLText.commmentsPostUrl,
						RequestParamsPool
								.getEntitysCommentsOrForwordParams(bundle),
						true, new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);
								try {
									JSONObject json=new JSONObject(result);
									if(json!=null && json.optString("code").equals("0")){
										if (null != draft) {
											setResult(DraftsActivity.TYPE_POST_COMMENT, intent);
										} else {
											setResult(RESULT_OK, intent);
										}
										finish();
									}else{
										ToastUtil.showToastInThread("发布失败了,请重试!",
												Toast.LENGTH_SHORT);
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
								
							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								ToastUtil.showToastInThread("请求失败了,请重试!",
										Toast.LENGTH_SHORT);
							}
						});
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case SEARCH_BOOK:
			if (resultCode == TimelineSearchBookActivity.SELECTED_BOOK) {
				String id = data.getStringExtra("book_id");
				String name = data.getStringExtra("book_name");
				bookIdBuffer.append(id + ",");
				userComment.append("《");
				userComment.append(name);
				userComment.append("》 ");

				Map<String, String> map = new HashMap<String, String>();
				map.put("bookid", id);
				map.put("bookname", name);
				bookNames.add(map);

			}
			break;

		case SEARCH_PEOPLE:
			if (resultCode == TimelineSearchPeopleActivity.CLICK_USER_NAME) {
				String userName = data
						.getStringExtra(TimelineSearchPeopleActivity.USER_NAME);
				userComment.append("@");
				userComment.append(userName);
				userComment.append(" ");
				

				String userid = data.getStringExtra("userid");
				userIdBuffer.append(userid + ",");
			}

			break;
		}

	}

	/**
	 * 根据用户是要转发还是评论，使用不同的文字填充checkBox
	 * 
	 * @param comment
	 *            true表示用户想要评论，false表示用户想要转发。
	 */
	private void initTitleBottom(boolean comment) {
		if (comment) {
			checkBox.setText(R.string.timeline_comment_forward_btw);
		} else {
			checkBox.setText(R.string.timeline_comment_comment_btw);
		}
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
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_community_dongtaipinglun));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_community_dongtaipinglun));
	}
	
}
