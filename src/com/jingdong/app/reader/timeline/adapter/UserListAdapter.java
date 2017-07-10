package com.jingdong.app.reader.timeline.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.me.model.UserFollower;
import com.jingdong.app.reader.timeline.model.UserModel;
import com.jingdong.app.reader.timeline.model.UserModel.Note;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.UiStaticMethod;

public class UserListAdapter extends BaseAdapter implements RecyclerListener {
	private static class ViewHoloder {
		TextView userName;
		TextView userSummary;
		ImageView userThumbNail;
	}

	private Context context;
	private UserModel model;
	private boolean note;

	public UserListAdapter(Context context, UserModel searchPeople, boolean note) {
		this.context = context;
		this.model = searchPeople;
		this.note = note;
	}
	
	@Override
	public int getCount() {
		return model.getUsers().size();
	}

	@Override
	public Object getItem(int position) {
		return model.getUsers().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item = initViewHolder(convertView);
		UserInfo user = (UserInfo) getItem(position);
		ViewHoloder holoder = (ViewHoloder) item.getTag();
		UiStaticMethod
				.loadThumbnail(context, holoder.userThumbNail, user.getAvatar() , user.isFemale());
		if (model.isShowButton()) {
			initFollowModel(position, (UserDetail) user, holoder);
		}
		holoder.userName.setText(user.getName());
		showSummary(user, holoder.userSummary);
		if (note)
			initNoteArea(position, holoder);
		return item;
	}

	@Override
	public void onMovedToScrapHeap(View view) {
		UiStaticMethod.onMovedToScrapHeap(view, RelativeLayout.class, R.id.thumb_nail);
	}

	/**
	 * 初始化viewholder
	 *
	 * @param item
	 *            待初始化的view
	 * @return 初始化后的view
	 */
	private View initViewHolder(View item) {
		if (item == null) {
			item = View.inflate(context, R.layout.item_users, null);
			ViewHoloder holoder = new ViewHoloder();
			holoder.userName = (TextView) item.findViewById(R.id.timeline_user_name);
			holoder.userSummary = (TextView) item.findViewById(R.id.timeline_user_summary);
			holoder.userThumbNail = (ImageView) item.findViewById(R.id.thumb_nail);
			item.setTag(holoder);
		}
		return item;
	}

	private void showSummary(UserInfo userInfo, TextView textView) {
		textView.setVisibility(View.VISIBLE);
		if (!UiStaticMethod.isNullString(userInfo.getRecommentText())) {
			textView.setText(userInfo.getRecommentText());
		} else if (!UiStaticMethod.isNullString(userInfo.getVname())) {
			textView.setText(userInfo.getVname());
		} else if (!UiStaticMethod.isNullString(userInfo.getSummary())) {
			textView.setText(userInfo.getSummary());
		} else {
			textView.setVisibility(View.GONE);
		}
	}

	/**
	 * 初始化关注按钮
	 *
	 * @param position
	 *            选中用户的行索引
	 * @param userDetail
	 *            选中的用户
	 * @param holoder
	 *            viewHolder
	 */
	private void initFollowModel(int position, UserDetail userDetail, final ViewHoloder holder) {
		final UserFollower followModel = model.getFollowModel(position);
		final String userId = userDetail.getId();
	}
	

	private void initNoteArea(int position, ViewHoloder holoder) {
		Note note = model.getNotes().get(position);
		Resources resources = context.getResources();
	}

}
