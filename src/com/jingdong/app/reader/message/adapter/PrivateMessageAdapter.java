package com.jingdong.app.reader.message.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.privateMsg.Conversation;
import com.jingdong.app.reader.util.ListInterface;
import com.jingdong.app.reader.util.UiStaticMethod;

public class PrivateMessageAdapter extends BaseAdapter implements RecyclerListener {
	private static class ViewHolder {
		ImageView networkImageView;
		ImageView imagebutton;
		TextView userName;
		TextView message;
		ImageView dotImageView;
	}

	private Context context;
	private ListInterface<Conversation> conversations;

	public PrivateMessageAdapter(Context context, ListInterface<Conversation> conversations) {
		this.context = context;
		this.conversations = conversations;
	}

	@Override
	public int getCount() {
		return conversations.size();
	}

	@Override
	public Object getItem(int position) {
		return conversations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.privatemessage, null);
			setViewHolder(convertView);
		}
		setViewContent(position, convertView);
		return convertView;
	}

	@Override
	public void onMovedToScrapHeap(View view) {
		UiStaticMethod.onMovedToScrapHeap(view, RelativeLayout.class, R.id.thumb_nail);
	}

	/**
	 * 为指定的View设置viewHolder
	 * 
	 * @param convertView
	 *            待设置的view
	 */
	private void setViewHolder(View convertView) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.networkImageView = (ImageView) convertView.findViewById(R.id.thumb_nail);
		viewHolder.userName = (TextView) convertView.findViewById(R.id.timeline_user_name);
		viewHolder.message = (TextView) convertView.findViewById(R.id.timeline_user_summary);
		viewHolder.imagebutton = (ImageView) convertView.findViewById(R.id.imagebutton);
		viewHolder.dotImageView = (ImageView) convertView.findViewById(R.id.dot);
		convertView.setTag(viewHolder);
	}

	/**
	 * 用adapter中指定位置的数据填充view
	 * 
	 * @param postion
	 *            指定的位置
	 * @param convertView
	 *            待填充的view
	 */
	private void setViewContent(int postion, View convertView) {
		Conversation conversation = (Conversation) getItem(postion);
		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
		UiStaticMethod.loadThumbnail(context, viewHolder.networkImageView, conversation.getUserInfo().getThumbNail(), conversation.getUserInfo().isFemale());
		viewHolder.userName.setText(conversation.getUserInfo().getName());
		viewHolder.message.setText(conversation.getLastMessage().getBody());
		viewHolder.imagebutton
		.setBackgroundResource(R.drawable.icon_arrow_right);
		Log.d("cj", "isHasNew()===========>>>>>>>" + conversation.isHasNew());
		if (conversation.isHasNew()) {
			viewHolder.dotImageView.setVisibility(View.VISIBLE);
		} else {
			viewHolder.dotImageView.setVisibility(View.GONE);
		}
	}
}
