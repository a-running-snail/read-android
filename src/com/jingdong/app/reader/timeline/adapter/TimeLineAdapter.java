package com.jingdong.app.reader.timeline.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.timeline.model.core.RenderBody;
import com.jingdong.app.reader.timeline.model.core.Entity.RenderType;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.ArticleHelper;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.share.CommunityShareUtil;
import com.jingdong.app.reader.view.TextArea;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.community.CommunityUtil;

public class TimeLineAdapter extends BaseAdapter {
	private static class ViewHolder {
		private ImageView avatar;
		private ImageView avatarLabel;
		private TextView userName;
		private TextView updateTime;
		private TextArea main;
		private View quotationContainer;
		private TextView quotationName;
		private TextArea quotation;
		private View bottomContainer;
		private TextView commentNumber;
		private TextView recommendNumber;
		private LinearLayout shareLinearLayout;
		private LinearLayout commentLinearLayout;
		private LinearLayout recommendLinearLayout;
		private LinearLayout forwardLinearLayout;
		private ImageView recommendImagevie;
	}

	private TimeLineModel timeline;
	private Context context;
	private final boolean hideBottom;
	private Fragment fragment;

	public TimeLineAdapter(Activity activity, TimeLineModel timeLine, boolean hideBottom,Fragment fragment) {
		this.context = activity;
		this.timeline = timeLine;
		this.hideBottom = hideBottom;
		this.fragment=fragment;
	}

	@Override
	public int getCount() {
		return timeline.getLength();
	}

	@Override
	public Object getItem(int arg0) {
		return timeline.getEntityAt(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item = convertView;
		if (item == null) {
			item = View.inflate(context, R.layout.item_tweet, null);
			item.setTag(initViewHolder(item));
		}
		if (timeline.getLength() == 0) {
			item.setVisibility(View.GONE);
		} else {
			Entity entity = (Entity) getItem(position);
			if (entity.getRenderBody().isDeleted())
				item.setVisibility(View.GONE);
			else {
				item.setVisibility(View.VISIBLE);
				initView(context, (ViewHolder) item.getTag(), entity, hideBottom);
			}
		}
		return item;
	}

	public  void initView(Context context, View item, Entity entity) {
		item.setTag(initViewHolder(item));
		initView(context, (ViewHolder) item.getTag(), entity, true);
	}

	private  ViewHolder initViewHolder(View item) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.avatar = (ImageView) item.findViewById(R.id.thumb_nail);
		viewHolder.avatarLabel = (ImageView) item.findViewById(R.id.avatar_label);
		viewHolder.userName = (TextView) item.findViewById(R.id.timeline_user_name);
		viewHolder.updateTime = (TextView) item.findViewById(R.id.timeline_update_time);
		viewHolder.main = (TextArea) item.findViewById(R.id.mainTextArea);
		viewHolder.quotationContainer = item.findViewById(R.id.quotationContainer);
		viewHolder.quotationName = (TextView) item.findViewById(R.id.timeline_quotation_user_name);
		viewHolder.quotation = (TextArea) item.findViewById(R.id.quotationTextArea);
		viewHolder.bottomContainer = item.findViewById(R.id.bottomContainer);
		viewHolder.commentNumber = (TextView) item.findViewById(R.id.timeline_comment);
		viewHolder.recommendNumber = (TextView) item.findViewById(R.id.timeline_recommend);
		viewHolder.shareLinearLayout = (LinearLayout) item.findViewById(R.id.shareLinearLayout);
		viewHolder.commentLinearLayout = (LinearLayout) item.findViewById(R.id.commentLinearLayout);
		viewHolder.recommendLinearLayout = (LinearLayout) item.findViewById(R.id.recommendLinearLayout);
		viewHolder.forwardLinearLayout = (LinearLayout) item.findViewById(R.id.forwardLinearLayout);
		viewHolder.recommendImagevie = (ImageView) item.findViewById(R.id.recommend_imagevie);
		return viewHolder;
	}

	private  void initView(Context context, ViewHolder holder, Entity entity, boolean hideBottom) {
		final UserInfo userInfo = entity.getUser();
		RenderBody renderBody = entity.getRenderBody();
		long timeStamp = ArticleHelper.getTime(entity, renderBody);
		String content,quote,subContent="",subQuote="";
		content=renderBody.getContent();
		quote=renderBody.getQuote();
		renderBody.setContent(UiStaticMethod.formatListItem(content));
		renderBody.setQuote(UiStaticMethod.formatListItem(quote));
		if(renderBody.hasEntity()&&renderBody.getEntity().getRenderBody()!=null){
			subContent=renderBody.getEntity().getRenderBody().getContent();
			subQuote=renderBody.getEntity().getRenderBody().getQuote();
			renderBody.getEntity().getRenderBody().setContent(UiStaticMethod.formatListItem(subContent));
			renderBody.getEntity().getRenderBody().setQuote((UiStaticMethod.formatListItem(subQuote)));
		}
		ArticleHelper.initAvatar(context, holder.avatar, userInfo);
		ArticleHelper.initUserName(context, holder.userName, holder.avatarLabel, userInfo);
		holder.updateTime.setText(TimeFormat.formatTime1(context.getResources(), timeStamp));
		holder.main.parseEntity(entity,false,0);
		ArticleHelper.initQuotation(context,holder.quotationContainer, holder.quotationName, holder.quotation, renderBody,false);
		ArticleHelper.initBottom(context, hideBottom, holder.bottomContainer, holder.commentNumber, holder.recommendNumber,
				holder.shareLinearLayout,holder.commentLinearLayout,holder.recommendLinearLayout,holder.recommendImagevie,holder.forwardLinearLayout, entity,fragment);
		renderBody.setContent(content);
		renderBody.setQuote(quote);
		if(renderBody.hasEntity()&&renderBody.getEntity().getRenderBody()!=null){
			renderBody.getEntity().getRenderBody().setContent(subContent);
			renderBody.getEntity().getRenderBody().setQuote(subQuote);
		}
	}
}
