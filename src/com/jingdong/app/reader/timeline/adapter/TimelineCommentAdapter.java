package com.jingdong.app.reader.timeline.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Comment;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.ArticleHelper;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.R;

public class TimelineCommentAdapter extends BaseAdapter implements RecyclerListener {
	private class ViewHolder {
		private ImageView avatar;
		private ImageView avatarLabel;
		private TextView userName;
		private TextView updateTime;
		private TextView main;
	}

	private Context context;
	private TweetModel tweetEntity;

	public TimelineCommentAdapter(Context context, TweetModel entity) {
		this.context = context;
		this.tweetEntity = entity;
	}

	@Override
	public int getCount() {
		switch (tweetEntity.getModelType()) {
		case TweetModel.MODEL_COMMENT:
			return tweetEntity.getCurrentCommentNumber();
		case TweetModel.MODEL_FORWARD:
			return tweetEntity.getCurrentForwardNumber();
		case TweetModel.MODEL_RECOMMEND:
			return tweetEntity.getCurrentRecommendsCount();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		switch (tweetEntity.getModelType()) {
		case TweetModel.MODEL_COMMENT:
			return tweetEntity.getCommentAt(position);
		case TweetModel.MODEL_FORWARD:
			return tweetEntity.getForwardAt(position);
		case TweetModel.MODEL_RECOMMEND:
			return tweetEntity.getRecommendAt(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item = convertView;
		if (item == null) {
			item = View.inflate(context, R.layout.item_comment, null);
			item.setTag(initViewHolder(item));
		}
		Comment comment = (Comment) getItem(position);
		initViews((ViewHolder) item.getTag(), comment);
		return item;
	}

	@Override
	public void onMovedToScrapHeap(View view) {
		UiStaticMethod.onMovedToScrapHeap(view, LinearLayout.class, R.id.thumb_nail);
	}

	private ViewHolder initViewHolder(View item) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.avatar = (ImageView) item.findViewById(R.id.thumb_nail);
		viewHolder.avatarLabel = (ImageView) item.findViewById(R.id.avatar_label);
		viewHolder.userName = (TextView) item.findViewById(R.id.timeline_user_name);
		viewHolder.updateTime = (TextView) item.findViewById(R.id.timeline_update_time);
		viewHolder.main = (TextView) item.findViewById(R.id.mainTextArea);
		return viewHolder;
	}

	private void initViews(ViewHolder holder, Comment comment) {
		UserInfo userInfo = comment.getUser();
		ArticleHelper.initAvatar(context, holder.avatar, userInfo);
		ArticleHelper.initUserName(context, holder.userName, holder.avatarLabel, userInfo);
		holder.updateTime.setText(TimeFormat.formatTime1(context.getResources(), comment.getTimeStamp()));
		UiStaticMethod.setAtUrlClickable(context, holder.main, comment.getContent());
	}
}
