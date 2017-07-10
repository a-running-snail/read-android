package com.jingdong.app.reader.timeline.adapter;

import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.entity.extra.Relation_with_current_user;
import com.jingdong.app.reader.entity.extra.UsersList;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FocusAdapter extends BaseAdapter {

	private Context context;
	private List<UsersList> usersLists;
	private String jd_user_name;

	// flag 0:关注 1：粉丝
	public FocusAdapter(Context context, List<UsersList> usersLists) {
		this.context = context;
		this.usersLists = usersLists;
	}

	@Override
	public int getCount() {
		if (usersLists == null) {
			return 0;
		} else {
			return usersLists.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return usersLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView( int positions, View convertView, ViewGroup parent) {


		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.focusitem, parent, false);}
	
		RelativeLayout relativeLayout = ViewHolder.get(convertView, R.id.relativeLayout);
		RoundNetworkImageView avatar_label = ViewHolder.get(convertView,  R.id.thumb_nail);
		TextView timeline_user_name = ViewHolder.get(convertView, R.id.timeline_user_name);
		TextView timeline_user_summary = ViewHolder.get(convertView, R.id.timeline_user_summary);
		final ImageView imagebutton = ViewHolder.get(convertView, R.id.imagebutton);
		
		final int position = positions;

		ImageLoader.getInstance().displayImage(
				usersLists.get(position).getAvatar(), avatar_label,
				GlobalVarable.getDefaultAvatarDisplayOptions(false));
		timeline_user_name.setText(usersLists.get(position).getName());
		timeline_user_summary.setText(usersLists.get(position).getSummary());
		initFollowModel(position,imagebutton);

		imagebutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean following = usersLists.get(position)
						.getRelation_with_current_user().isFollowing();
				usersLists.get(position).getRelation_with_current_user()
						.setFollowing(!following);
				resetButton(usersLists.get(position)
						.getRelation_with_current_user(),imagebutton);
				if (!following) {
					Follow(usersLists.get(position).getId());
				} else {
					UNFollow(usersLists.get(position).getId());
				}
			}
		});

		relativeLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if ( position >= 0 && position < usersLists.size() ) {
					Intent intent = new Intent();
					intent.setClass(context, UserActivity.class);
					intent.putExtra("user_id", usersLists.get(position).getId());
					intent.putExtra(UserActivity.JD_USER_NAME, usersLists.get(position).getJd_user_name());
					context.startActivity(intent);
				}
			}
		});

		return convertView;
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
	private void initFollowModel(final int position,ImageView imagebutton) {
		jd_user_name = usersLists.get(position).getJd_user_name();
		resetButton(usersLists.get(position).getRelation_with_current_user(),imagebutton);
	}

	public void resetButton(Relation_with_current_user focusModel,ImageView imagebutton) {
		if (focusModel.isFollowing()) {
			if (focusModel.isFollowed()) {
				imagebutton
						.setBackgroundResource(R.drawable.btn_bothfollowing);
			} else {
				imagebutton
						.setBackgroundResource(R.drawable.btn_following);
			}
		} else {
			imagebutton.setBackgroundResource(R.drawable.btn_follow);
		}
		if (LoginUser.getpin().equals(jd_user_name)) {
			imagebutton
			.setBackgroundResource(R.drawable.icon_arrow_right);
			imagebutton.setEnabled(false);
		}
	}

	private void Follow(String id) {
		WebRequestHelper.post(URLText.Follow_SomeOne_URL,
				RequestParamsPool.getFollowSomeParams(id),
				new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(context, R.string.network_connect_error,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub

						String result = new String(responseBody);
						try {
							JSONObject jsonObject = new JSONObject(result);
							String code = jsonObject.optString("code");
							String message = jsonObject.optString("message");
							if (jsonObject != null) {
								if (code.equals("0")) {
									Toast.makeText(context, message, 1).show();
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
	}

	private void UNFollow(String id) {
		WebRequestHelper.post(URLText.Follow_Cancle,
				RequestParamsPool.getUNFollowParams(id),
				new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(context, R.string.network_connect_error,
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub
						String result = new String(responseBody);
						try {
							JSONObject jsonObject = new JSONObject(result);
							String code = jsonObject.optString("code");
							String message = jsonObject.optString("message");
							if (jsonObject != null) {
								if (code.equals("0")) {
									Toast.makeText(context, message, 1).show();
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
	}

}
