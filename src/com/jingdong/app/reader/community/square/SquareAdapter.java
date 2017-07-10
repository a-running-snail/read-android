package com.jingdong.app.reader.community.square;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.community.CommunityUtil;
import com.jingdong.app.reader.community.FriendCircleFragment;
import com.jingdong.app.reader.community.square.entity.SquareEntity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.preloader.CutBitmapDisplayer;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.timeline.model.core.RenderBody;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.util.share.CommunityShareUtil;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SquareAdapter extends BaseAdapter{
	private Context mContext = null;
	private int count = 0;
	private LayoutInflater inflater = null;
	private ArrayList<SquareEntity> mDataList = new ArrayList<SquareEntity>();
	private XListView mListView;
	private Fragment fragment;
	
	public SquareAdapter(Context context, XListView mListView, Fragment fragment) {
		this.mContext = context;
		if(null != context) {
			this.inflater = LayoutInflater.from(context);	
		}
		this.mListView = mListView;
		this.fragment = fragment;
	}
	
	public void setData(ArrayList<SquareEntity> list) {
		mDataList.clear();
		mDataList.addAll(list);
		count = mDataList.size();
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return count;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(null == convertView) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.fragment_square_item, parent, false);
			holder.mBookImage = (ImageView)convertView.findViewById(R.id.mBookImage);
			holder.mBookName = (TextView)convertView.findViewById(R.id.mBookName);
			holder.mUserIcon = (RoundNetworkImageView)convertView.findViewById(R.id.mUserIcon);
			holder.mUserName = (TextView)convertView.findViewById(R.id.mUserName);
			holder.mHotIcon = (ImageView)convertView.findViewById(R.id.mHotIcon);
			holder.rating = (RatingBar)convertView.findViewById(R.id.rating);
			holder.mUpdateTime = (TextView)convertView.findViewById(R.id.mUpdateTime);
			holder.content = (TextView)convertView.findViewById(R.id.content);
			holder.timeline_comment = (TextView)convertView.findViewById(R.id.timeline_comment);
			holder.timeline_recommend = (TextView)convertView.findViewById(R.id.timeline_recommend);
			holder.shareLinearLayout = (LinearLayout)convertView.findViewById(R.id.shareLinearLayout);
			holder.commentLinearLayout = (LinearLayout)convertView.findViewById(R.id.commentLinearLayout);
			holder.recommendLinearLayout = (LinearLayout)convertView.findViewById(R.id.recommendLinearLayout);
			holder.forwardLinearLayout= (LinearLayout) convertView.findViewById(R.id.forwardLinearLayout);
			holder.ratingLayout = (RelativeLayout)convertView.findViewById(R.id.ratingLayout);
			holder.recommend_imagevie = (ImageView)convertView.findViewById(R.id.recommend_imagevie);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		SquareEntity data = mDataList.get(position);
		if(!TextUtils.isEmpty(data.mBookInfoEntity.imageUrl)) {
			ImageLoader.getInstance().displayImage(data.mBookInfoEntity.imageUrl, holder.mBookImage,
					getCutBookDisplayOptions(false));
		}else{
			holder.mBookImage.setImageResource(R.drawable.ebook_default_icon);
		}
		
		holder.mUserIcon.setImageResource(R.drawable.defaultavatar_small);
		if(!TextUtils.isEmpty(data.mUserInfoEntity.yunSmaImageUrl)) {
			ImageLoader.getInstance().displayImage(data.mUserInfoEntity.yunSmaImageUrl, holder.mUserIcon);
		}
		
		if(data.recommend) {
			holder.recommend_imagevie.setImageResource(R.drawable.community_list_recommanded_icon);
			holder.timeline_recommend.setTextColor(mContext.getResources().getColor(R.color.red_main));
		}else {
			holder.recommend_imagevie.setImageResource(R.drawable.community_list_unrecommand_icon);
			holder.timeline_recommend.setTextColor(mContext.getResources().getColor(R.color.text_sub));
		}
		
		holder.mBookName.setText(data.mBookInfoEntity.name);
		holder.mUserName.setText(data.mUserInfoEntity.nickName);
		if(data.rating==0)
			data.rating=5;
		holder.rating.setRating(data.rating);
		
		holder.mUpdateTime.setText(getTime(data.commentdate.replace("T", " ")));
		holder.content.setText(data.content);
		holder.timeline_comment.setText(CommunityUtil.getString(data.commentCount,"comment"));
		holder.timeline_recommend.setText(CommunityUtil.getString(data.recommendsCount,"recommand"));
		
		holder.recommend_imagevie.setTag("recommendIcon:" + data.id);
		holder.timeline_recommend.setTag("recommendsCount:" + data.id);
		
		holder.mBookImage.setOnClickListener(new onClickListener(onClickListener.BOOKINFO, position));
		holder.mBookName.setOnClickListener(new onClickListener(onClickListener.BOOKINFO, position));
		holder.mUserIcon.setOnClickListener(new onClickListener(onClickListener.USERINFO, position));
		holder.mUserName.setOnClickListener(new onClickListener(onClickListener.USERINFO, position));
		holder.ratingLayout.setOnClickListener(new onClickListener(onClickListener.DETAIL, position));
		holder.content.setOnClickListener(new onClickListener(onClickListener.DETAIL, position));
		holder.shareLinearLayout.setOnClickListener(new onClickListener(onClickListener.SEND, position));
		holder.commentLinearLayout.setOnClickListener(new onClickListener(onClickListener.COMMENT, position));
		holder.recommendLinearLayout.setOnClickListener(new onClickListener(onClickListener.RECOMMEND, position));
		holder.forwardLinearLayout.setOnClickListener(new onClickListener(onClickListener.FORWARD, position));
		return convertView;
	}
	
	public static DisplayImageOptions getCutBookDisplayOptions(boolean recycle) {
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.bg_default_cover) // 加载时的图片
        .showImageForEmptyUri(R.drawable.ebook_default_icon) // uri空的时候图片
        .showImageOnFail(R.drawable.ebook_default_icon) // 加载失败的图片
        .resetViewBeforeLoading(false) // 默认配置
        .delayBeforeLoading(10).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
        .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
        .cacheOnDisk(true)// 设置下载的图片是否缓存在sdcard中
        .considerExifParams(false) // 默认配置
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
        .displayer(new CutBitmapDisplayer(recycle))
        .build();
        return options;
    }
	
	static class ViewHolder {
		ImageView mBookImage;
		TextView mBookName;
		RoundNetworkImageView mUserIcon;
		TextView mUserName;
		ImageView mHotIcon;
		RatingBar rating;
		TextView mUpdateTime;
		TextView content;
		TextView timeline_comment;
		TextView timeline_recommend;
		LinearLayout shareLinearLayout;
		LinearLayout commentLinearLayout;
		LinearLayout recommendLinearLayout;
		LinearLayout forwardLinearLayout;
		RelativeLayout ratingLayout;
		ImageView recommend_imagevie;
	}

	@Override
	public SquareEntity getItem(int arg0) {
		return mDataList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	private String getTime(String date) {
		final long MINTUE = 60*1000;
		final long HOUR = 60*MINTUE;
		final long DAY = 24*HOUR;
		final long WEEK = 7*DAY;
		
		String time = null;
		try {
			long curTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date strtodate = formatter.parse(date);
			long historytime = strtodate.getTime();

			Date curDate = new Date(curTime);
			int curYear = curDate.getYear();
			int history = strtodate.getYear();
			
			long diff = Math.abs(historytime - curTime);// 时间差
			if (curYear == history) {
				 if (diff <= WEEK && diff > DAY) {
					 return time = diff / DAY + "天前更新";// 天前更新
				 }else if (diff <= DAY && diff > HOUR) {
					 return time = diff / HOUR + "小时前更新";// 小时前更新
				 }else if (diff <= HOUR) {
					 int min = (int)(diff / MINTUE);
					 if(min < 1) {
						 min = 1;
					 }
					 return time = min + "分钟前更新";// 分钟前更新
				 }else {
					 SimpleDateFormat jn = new SimpleDateFormat("yyyy-MM-dd");
					 return jn.format(strtodate);// 今年内：月日更新
				 }
			}else {
				SimpleDateFormat jn = new SimpleDateFormat("yyyy-MM-dd");
				return jn.format(strtodate);// 非今年：年月日更新
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return time;
	}

	class onClickListener implements OnClickListener {
		public static final int BOOKINFO = 1;
		public static final int USERINFO = 2;
		public static final int DETAIL = 3;
		public static final int SEND = 4;
		public static final int COMMENT = 5;
		public static final int RECOMMEND = 6;
		public static final int FORWARD  = 7;
		
		private int type; //1:图书详情 2:用户信息 3:详情 4:分享 5：评论 6：推荐 7：转发
		private SquareEntity data;
		private int index;
		
		public onClickListener(int type, int index) {
			this.type = type;
			this.data = mDataList.get(index);
			this.index = index;
		}

		@Override
		public void onClick(View arg0) {
			if(BOOKINFO == type) {
//				Intent intent2 = new Intent(mContext, BookInfoNewUIActivity.class);
//				intent2.putExtra("bookid", data.mBookInfoEntity.ebookId);
//				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				mContext.startActivity(intent2);
			}else if(USERINFO == type) {
				Intent intent = new Intent(mContext, UserActivity.class);
				intent.putExtra(UserFragment.USER_ID, data.mUsersEntity.id+"");
				intent.putExtra(UserActivity.JD_USER_NAME, data.mUsersEntity.jdUserName);
				mContext.startActivity(intent);
			}else if(DETAIL == type) {
				Intent intent = new Intent();
				intent.putExtra(TimelineTweetActivity.TWEET_GUID, data.guid);
				intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, data.mUserInfoEntity.nickName);
				intent.setClass(mContext, TimelineTweetActivity.class);
				if(fragment!=null) {
					fragment.startActivityForResult(intent, SquareFragment.START_ACTIVITY_FROM_LIST);
				}else
					((Activity)mContext).startActivityForResult(intent, SquareFragment.START_ACTIVITY_FROM_LIST);
			}else if(SEND == type) {
				
				Entity entity = new Entity();
				entity.setRenderType(data.renderType);
				UserInfo user=new UserInfo();
				user.setName(data.mUserInfoEntity.nickName);
				entity.setUser(user);
				Book book =new Book();
				book.setTitle(data.mBookInfoEntity.name);
				entity.setBook(book);
				RenderBody renderBody = new RenderBody();
				renderBody.setRating(data.rating);
				renderBody.setContent(data.content);
				entity.setRenderBody(renderBody);
				
				new CommunityShareUtil().getCommunityShareView(entity, (Activity) mContext).showAtLocation(((Activity) mContext).findViewById(R.id.drawer_layout),
						Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			}else if(COMMENT == type) {
//				Intent intent = new Intent();
//				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				intent.putExtra(TimelineTweetActivity.TWEET_GUID, data.guid);
//				intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, data.mUserInfoEntity.nickName);
//				intent.putExtra("ActionType", TimelineTweetActivity.ACTION_TO_COMMENT);
//				intent.setClass(mContext, TimelineTweetActivity.class);
//				if(fragment!=null) {
//					fragment.startActivityForResult(intent, FriendCircleFragment.START_ACTIVITY_FROM_LIST);
//				}else 
//					((Activity) mContext).startActivityForResult(intent, FriendCircleFragment.START_ACTIVITY_FROM_LIST);
				
				Intent intent = new Intent(mContext, TimelineCommentsActivity.class);
				intent.putExtra(TimelineCommentsActivity.ENTITY_GUID, data.guid);
				intent.putExtra(TimelineCommentsActivity.IS_COMMENT, true);
				intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, data.mUserInfoEntity.nickName);
				
				if(fragment!=null)
					fragment.startActivityForResult(intent, SquareFragment.GO_TO_COMMENT);
				
			}else if(RECOMMEND == type) {
				if(!TextUtils.isEmpty(data.mUserInfoEntity.pin) && data.mUserInfoEntity.pin.equals(LoginUser.getpin())){
					Toast.makeText(mContext, "不可以自己赞自己", Toast.LENGTH_SHORT).show();
				}else{
					if (!NetWorkUtils.isNetworkConnected(mContext)) {
						Toast.makeText(mContext, mContext.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
						return;
					}
					
					boolean recommemd = mDataList.get(index).recommend;
					if(recommemd) {
						mDataList.get(index).recommend = false;	
						mDataList.get(index).recommendsCount -= 1;
					}else {
						mDataList.get(index).recommend = true;
						mDataList.get(index).recommendsCount += 1;
					}
					
					TextView timeline_recommend = (TextView)arg0.findViewById(R.id.timeline_recommend);
					ImageView recommend_imagevie = (ImageView)arg0.findViewById(R.id.recommend_imagevie);
					if(null != recommend_imagevie) {
						if(mDataList.get(index).recommend) {
							recommend_imagevie.setImageResource(R.drawable.community_list_recommanded_icon);
							timeline_recommend.setTextColor(mContext.getResources().getColor(R.color.red_main));
						}else {
							recommend_imagevie.setImageResource(R.drawable.community_list_unrecommand_icon);
							timeline_recommend.setTextColor(mContext.getResources().getColor(R.color.text_sub));
						}
					}
					
					if(null != timeline_recommend) {
						timeline_recommend.setText(CommunityUtil.getString(mDataList.get(index).recommendsCount,"recommand"));
					}
					
					clickRecommand(data);
				}
			}else if(FORWARD == type) {
				Intent intent = new Intent(mContext, TimelineCommentsActivity.class);
				intent.putExtra(TimelineCommentsActivity.ENTITY_GUID, data.guid);
				intent.putExtra(TimelineCommentsActivity.IS_COMMENT, false);
				intent.putExtra(TimelineCommentsActivity.USER_NINKNAME, data.mUserInfoEntity.nickName);
				
				String imageUrl=data.mBookInfoEntity.imageUrl;
				if( imageUrl==null || imageUrl.equals("")){
					imageUrl = data.mUserInfoEntity.yunMidImageUrl;
				}
				intent.putExtra(TimelineCommentsActivity.FORWARD_IMAGE, imageUrl);
				
				String content=data.content;
				intent.putExtra(TimelineCommentsActivity.FORWARD_CONTENT, content);
				
				if(fragment!=null)
					fragment.startActivityForResult(intent, SquareFragment.GO_TO_COMMENT);
			}
			
		}
		
		private void clickRecommand(final SquareEntity entity) {
			final String baseUrl;
			if(!mDataList.get(index).recommend) {
				baseUrl = URLText.unlikeEntityUrl;
			}else {
				baseUrl = URLText.likeEntityUrl;
			}
			
			((Activity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {

					WebRequestHelper.post(baseUrl, RequestParamsPool.getTimelineFavoriteParams(entity.guid),
							true, new MyAsyncHttpResponseHandler(mContext) {

								@Override
								public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
									boolean success = parsePostResult(new String(responseBody));
									if (success) {
//										boolean recommemd = mDataList.get(index).recommend;
//										if(recommemd) {
//											mDataList.get(index).recommend = false;	
//											mDataList.get(index).recommendsCount -= 1;
//										}else {
//											mDataList.get(index).recommend = true;
//											mDataList.get(index).recommendsCount += 1;
//										}
//										
//										String id = mDataList.get(index).id + "";
//										ImageView recommend_imagevie = (ImageView)mListView.findViewWithTag("recommendIcon:" + id);
//										if(null != recommend_imagevie) {
//											if(mDataList.get(index).recommend) {
//												recommend_imagevie.setImageResource(R.drawable.community_recommended_icon);
//											}else {
//												recommend_imagevie.setImageResource(R.drawable.icon_points_good);
//											}
//										}
//										TextView timeline_recommend = (TextView)mListView.findViewWithTag("recommendsCount:" + id);
//										if(null != timeline_recommend) {
//											timeline_recommend.setText(mDataList.get(index).recommendsCount + "");
//										}
									} else {
										try {
											JSONObject object = new JSONObject(new String(responseBody));
											String msg = object.optString("message");
											if (!TextUtils.isEmpty(msg))
												ToastUtil.showToastInThread(msg, Toast.LENGTH_SHORT);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}


								}

								@Override
								public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
									ToastUtil.showToastInThread("请求出错了，请检查网络！", Toast.LENGTH_SHORT);
								}
					});
				}
			}); 
		}
		
	}
	
	public static boolean parsePostResult(String result) {
		boolean value = false;
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("code")==0) {
				value = true;
			}
		} catch (JSONException e) {
			MZLog.e("parsePost", Log.getStackTraceString(e));
		}
		return value;
	}
	
}
