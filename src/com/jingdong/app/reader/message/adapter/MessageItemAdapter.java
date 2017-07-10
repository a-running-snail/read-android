package com.jingdong.app.reader.message.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jingdong.app.reader.message.model.Alert;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.PartClickableTextView;
import com.jingdong.app.reader.R;

public class MessageItemAdapter extends BaseAdapter {
	private static class ViewHolder {
		private PartClickableTextView content;
		private TextView time;
	}
	private Context context;
	private TimeLineModel model;

	public MessageItemAdapter(Context context, TimeLineModel model) {
		this.context = context;
		this.model = model;
	}

	@Override
	public int getCount() {
		return model.getLength();
	}

	@Override
	public Object getItem(int position) {
		return model.getEntityAt(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.item_message, null);
			convertView.setTag(initViewHolder(convertView));
		}
		Alert alert = (Alert) model.getEntityAt(position);
		fillTimelineLayout(convertView, alert);
		return convertView;
	}
	
	private ViewHolder initViewHolder(View item) {
		ViewHolder holder = new ViewHolder();
		holder.content = (PartClickableTextView) item.findViewById(R.id.message_text);
		holder.time = (TextView) item.findViewById(R.id.message_time);
		return holder;
	}
	
	private void fillTimelineLayout(View item, Entity dataSource) {
		Alert alert=(Alert) dataSource;
		ViewHolder viewHolder = (ViewHolder) item.getTag();
		UiStaticMethod.setAtUrlClickable(context, viewHolder.content, alert.getText());
		viewHolder.time.setText(TimeFormat.formatTime(context.getResources(),alert.getTimeStamp()));
	}
}
