package com.jingdong.app.reader.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

import org.apache.http.Header;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.community.CommunityUtil;
import com.jingdong.app.reader.community.FriendCircleFragment;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Comment;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.timeline.model.core.RenderBody;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.share.CommunityShareUtil;
import com.jingdong.app.reader.view.LinkTouchMovementMethod;
import com.jingdong.app.reader.view.TextArea;
import com.jingdong.app.reader.view.TextAreaForDetail;

public class ArticleHelper {

	public static long getTime(Entity entity, RenderBody renderBody) {
		long timeStamp;
		if (entity.getTimeStamp() == 0)
			timeStamp = renderBody.getWrittenTime();
		else
			timeStamp = entity.getTimeStamp();
		return timeStamp;
	}

	public static void initAvatar(Context context, ImageView avatar, final UserInfo userInfo) {
		UiStaticMethod.loadThumbnail(context, avatar, userInfo.getAvatar(), userInfo.isFemale());
		avatar.setOnClickListener(new JumpToUser(context, userInfo));
		avatar.setFocusable(false);
		avatar.setFocusableInTouchMode(false);
	}

	public static void initUserName(Context context, TextView userName, UserInfo userInfo) {
		UiStaticMethod.setVIP(userName, userInfo, true);
		initUserNameClickEvent(context, userName, userInfo);
	}
	
	public static void initUserName(Context context, TextView userName, ImageView userLabel, UserInfo userInfo) {
		UiStaticMethod.setVIP(userLabel, userInfo);
		initUserNameClickEvent(context, userName, userInfo);
	}
	
	public static void initUserNameClickEvent(final Context context, TextView userName, final UserInfo userInfo) {
		if (userInfo == null || UiStaticMethod.isNullString(userInfo.getName())) {
			userName.setVisibility(View.GONE);
			return;
		}
		final String userId = userInfo.getId();
		SpannableStringBuilder spannableString = new SpannableStringBuilder(userInfo.getName());
		MZBookClickableSpan.ClickCallback callback = new MZBookClickableSpan.ClickCallback() {

			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(context, UserActivity.class);
				intent.putExtra(UserFragment.USER_ID, userId);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(UserActivity.JD_USER_NAME, userInfo.getJd_user_name());
				context.startActivity(intent);
			}

		};
		int end = userInfo.getName().length();
		int color = context.getResources().getColor(R.color.text_main);
		spannableString.setSpan(new MZBookClickableSpan(callback, color,
				0, end), 0, end, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
		userName.setText(spannableString, BufferType.SPANNABLE);
		userName.setMovementMethod(LinkTouchMovementMethod.getInstance());
		userName.setFocusable(false);
		userName.setFocusableInTouchMode(false);
		userName.setVisibility(View.VISIBLE);
	}

	public static void initQuotation(final Context context,View quotationContainer, TextView quotationName, TextArea quotation,
			final RenderBody renderBody,boolean clickable) {
		if (renderBody.hasEntity()) {
			quotationContainer.setVisibility(View.VISIBLE);
			if (renderBody.getEntity().getRenderBody().isDeleted()) {
				quotationName.setVisibility(View.GONE);
			} else {
				quotationName.setVisibility(View.VISIBLE);
				initUserName(quotationName.getContext(), quotationName, renderBody.getEntity().getUser());
			}
			quotation.setVisibility(View.VISIBLE);		
			quotation.parseEntitys(renderBody.getEntity(),clickable);
			
			quotationContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,
							TimelineTweetActivity.class);
					intent.putExtra(TimelineTweetActivity.TWEET_GUID, renderBody
							.getEntity().getGuid());
					((Activity)context).startActivity(intent);
				}
			});
		} else {
			quotation.setVisibility(View.GONE);
			quotationName.setVisibility(View.GONE);
			quotationContainer.setVisibility(View.GONE);
		}
	}
	
	public static void initQuotationForDetail(View quotationContainer, TextView quotationName, TextAreaForDetail quotation,
			RenderBody renderBody,boolean clickable) {
		if (renderBody.hasEntity()) {
			quotationContainer.setVisibility(View.VISIBLE);
			if (renderBody.getEntity().getRenderBody().isDeleted()) {
				quotationName.setVisibility(View.GONE);
			} else {
				quotationName.setVisibility(View.VISIBLE);
				initUserName(quotationName.getContext(), quotationName, renderBody.getEntity().getUser());
			}
			quotation.setVisibility(View.VISIBLE);		
			quotation.parseEntitys(renderBody.getEntity(),clickable);
		} else {
			quotation.setVisibility(View.GONE);
			quotationName.setVisibility(View.GONE);
			quotationContainer.setVisibility(View.GONE);
		}
	}

	static long lastClick;
	public static void initBottom(final Context context, boolean hideBottom, View bottomContainer, TextView commentNumber,
			final TextView recommendNumber,LinearLayout shareLinearLayout,LinearLayout commentLinearLayout,LinearLayout recommendLinearLayout,
			final ImageView recommendImageview,final LinearLayout forwardLinearLayout,final Entity entity,final Fragment fragment) {
		if (hideBottom) {
			commentNumber.setVisibility(View.GONE);
			recommendNumber.setVisibility(View.GONE);
			bottomContainer.setVisibility(View.GONE);
		} else {
			commentNumber.setVisibility(View.VISIBLE);
			recommendNumber.setVisibility(View.VISIBLE);
			bottomContainer.setVisibility(View.VISIBLE);
			commentNumber.setText(CommunityUtil.getString(entity.getCommentNumber(),"comment"));
			recommendNumber.setText(CommunityUtil.getString(entity.getRecommendsCount(),"recommand") );
			
			if(entity.isViewerRecommended()){
				recommendNumber.setTextColor(context.getResources().getColor(R.color.red_main));
				recommendImageview.setImageResource(R.drawable.community_list_recommanded_icon);
			}
			else{
				recommendNumber.setTextColor(context.getResources().getColor(R.color.text_sub));
				recommendImageview.setImageResource(R.drawable.community_list_unrecommand_icon);
			}
				
			
			final Activity activity = (Activity) context;
			if(shareLinearLayout!=null){
				shareLinearLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						new CommunityShareUtil().getCommunityShareView(entity, activity).showAtLocation(
								activity.findViewById(R.id.main),
								Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
					}
				});
			}
			if(commentLinearLayout!=null){
				commentLinearLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						String guid = entity.getGuid();
						if (UiStaticMethod.isNullString(guid))
							guid = entity.getRenderBody().getEntity().getGuid();
						Intent intent = new Intent(context, TimelineCommentsActivity.class);
						intent.putExtra(TimelineCommentsActivity.ENTITY_GUID, guid);
						intent.putExtra(TimelineCommentsActivity.IS_COMMENT, true);
						intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, entity.getUser().getName());
						if (entity.getRenderBody().hasEntity())
							intent.putExtra(TimelineCommentsActivity.ORIGIN_CONTENT,
									UiStaticMethod.formatListItem(entity.getRenderBody()
											.getContent()));
						
						if(fragment!=null)
							fragment.startActivityForResult(intent, TimelineTweetActivity.START_COMMENT_FROM_TWEET);
						else
							activity.startActivityForResult(intent, TimelineTweetActivity.START_COMMENT_FROM_TWEET);
						
					}
				});
			}
			if(recommendLinearLayout!=null){
				lastClick = 0;
				recommendLinearLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (System.currentTimeMillis() - lastClick <= 1000) {
							return;
						}
						
						lastClick = System.currentTimeMillis();
						if(entity.getUser().getUserPin().equals(LoginUser.getpin())){
							Toast.makeText(context, "不可以自己赞自己", 0).show();
						}
						else{
							if(entity.isViewerRecommended()){
								entity.setRecommendsCount(entity.getRecommendsCount()-1);
								recommendNumber.setText(CommunityUtil.getString(entity.getRecommendsCount(),"recommand") );
								recommendNumber.setTextColor(context.getResources().getColor(R.color.text_sub));
								recommendImageview.setImageResource(R.drawable.community_list_unrecommand_icon);
							}
							else{
								entity.setRecommendsCount(entity.getRecommendsCount()+1);
								recommendNumber.setText(CommunityUtil.getString(entity.getRecommendsCount(),"recommand") );
								recommendNumber.setTextColor(context.getResources().getColor(R.color.red_main));
								recommendImageview.setImageResource(R.drawable.community_list_recommanded_icon);
							}
							new CommunityUtil().clickRecommand(context,entity);
						}
						
					}
				});
			}
			//转发
			if(forwardLinearLayout!=null){
				forwardLinearLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						String guid = entity.getGuid();
						if (UiStaticMethod.isNullString(guid))
							guid = entity.getRenderBody().getEntity().getGuid();
						
						Intent intent = new Intent(context, TimelineCommentsActivity.class);
						intent.putExtra(TimelineCommentsActivity.ENTITY_GUID, guid);
						intent.putExtra(TimelineCommentsActivity.IS_COMMENT, false);
						intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, entity.getUser().getName());
						if (entity.getRenderBody().hasEntity())
							intent.putExtra(TimelineCommentsActivity.ORIGIN_CONTENT,
									UiStaticMethod.formatListItem(entity.getRenderBody()
											.getContent()));
						
						String imageUrl="";
						if(entity.getImages()!=null && entity.getImages().size()>0){
							imageUrl=entity.getImages().get(0);
						}else if(entity.getBook()!=null && entity.getBook().getCover()!=null){
							imageUrl=entity.getBook().getCover();
						}else if(entity.getUser()!=null && entity.getUser().getAvatar()!=null){
							imageUrl = entity.getUser().getAvatar();
						}
						intent.putExtra(TimelineCommentsActivity.FORWARD_IMAGE, imageUrl);
						
						String content="";
						if(entity.getRenderBody().getContent()!=null && !"".equals(entity.getRenderBody().getContent())){
							content=entity.getRenderBody().getContent();
						}else if(entity.getRenderBody().getQuote()!=null && !"".equals(entity.getRenderBody().getQuote())){
							content= entity.getRenderBody().getQuote();
						}else
							content="转发动态";
						intent.putExtra(TimelineCommentsActivity.FORWARD_CONTENT, content);
						
						if(fragment!=null)
							fragment.startActivityForResult(intent, TimelineTweetActivity.START_COMMENT_FROM_TWEET);
						else{
							activity.startActivityForResult(intent, TimelineTweetActivity.START_COMMENT_FROM_TWEET);
						}
						
					}
				});
			}
		}
	}
	
}
