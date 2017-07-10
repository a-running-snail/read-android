package com.jingdong.app.reader.me.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jingdong.app.reader.me.activity.UserPhotoActivity;
import com.jingdong.app.reader.me.model.UserDetail;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.R;

public class UserInfoView extends ScrollView {

	private static Bitmap MALE_THUMB_NAIL;
	private static Bitmap FEMALE_THUMB_NAIL;
	private TextView userName;
	private TextView summary;

	private Context ct;

	public UserInfoView(Context context) {
		super(context);
		this.ct=context;
		inflate(getContext(), R.layout.fragment_user_info, this);
		initStaticArea();
	}

	public UserInfoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		this.ct=context;
	}

	public UserInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.ct=context;
		inflate(getContext(), R.layout.fragment_user_info, this);
		initStaticArea();
		
	}

	/**
	 * 根据UserInfo填充View
	 * 
	 * @param userInfo
	 *            数据源
	 */
	public void fillView(UserDetail userInfo) {
		initHeader(userInfo);
		fillTimeline(userInfo);
		fillReadingData(userInfo);
		fillBookCase(userInfo);
	}

	/**
	 * 根据userInfo中的数据，重新设置用户名，性别，签名档
	 * 
	 * @param userInfo
	 *            用户信息，根据userInfo.getName(),userInfo.isFemale,userInfo.
	 *            getSummary()重新设置view的信息
	 */
	public void setTitle(UserInfo userInfo) {
		UiStaticMethod.setText(summary, userInfo.getSummary());
		if (UiStaticMethod.isNullString(userInfo.getSummary())) {
			findViewById(R.id.user_profile_bottom_line).setVisibility(View.GONE);
		} else {
			findViewById(R.id.user_profile_bottom_line).setVisibility(View.VISIBLE);
		}
		userName.setText(userInfo.getName());
		int profileGender;
		if (userInfo.isFemale())
			profileGender = R.drawable.profile_gender_female;
		else
			profileGender = R.drawable.profile_gender_male;
		userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, profileGender, 0);
	}

	/**
	 * 重新设置用户头像
	 * 
	 * @param imageBytes
	 *            字节数组，根据这个字节数组，重新设置用户头像
	 */
	public void setAvatar(byte[] imageBytes) {
		if (imageBytes != null) {
			ImageView imageView = (ImageView) findViewById(R.id.user_thumbnail);
			imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));	
		}
	}
	public void setAvatar(final String url) {

			ImageView imageView = (ImageView) findViewById(R.id.user_thumbnail);

			imageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
				
					Intent intent=new Intent(ct,UserPhotoActivity.class);
					intent.putExtra("url",url);
					ct.startActivity(intent);
				}
			});
		
		
	}
	

	public void updateNewFans(UserDetail userInfo) {
		TextView newFans = (TextView) findViewById(R.id.user_fans_new_number);
		if (userInfo.isCurrentUser(getContext())) {
			int newFansNumber;
			if ((newFansNumber = Notification.getInstance().getFollowersCount()) == 0)
				newFans.setVisibility(View.GONE);
			else {
				newFans.setVisibility(View.VISIBLE);
				if (newFansNumber >= 10)
					newFans.setText(newFans.getTag() + "N");
				else
					newFans.setText(newFans.getTag() + String.valueOf(newFansNumber));
			}
		} else {
			newFans.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 将用户头像从Drawable转化为字节数组
	 * 
	 * @return 用户头像所代表的字节数组。
	 */
	public byte[] getAvatar() {
		Drawable drawable = ((ImageView) findViewById(R.id.user_thumbnail)).getDrawable();
		byte[] avatar = UiStaticMethod.bitmapToByteArray(UiStaticMethod.drawableToBitmap(drawable));
		return avatar;
	}

	/**
	 * 初始化保存有用户头像的两个静态变量，本方法仅应该在构造函数中被调用
	 */
	private void initStaticArea() {
		if (MALE_THUMB_NAIL == null)
			MALE_THUMB_NAIL = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.avata_male);
		if (FEMALE_THUMB_NAIL == null)
			FEMALE_THUMB_NAIL = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.avata_female);
	}

	/**
	 * 填充用户主页的头部内容
	 * 
	 * @param userInfo
	 *            数据源
	 */
	private void initHeader(UserDetail userInfo) {
		ImageView avatar = (ImageView) findViewById(R.id.user_thumbnail);
		UiStaticMethod.loadThumbnail(getContext(), avatar, userInfo.getThumbNail() + "!w200h200", userInfo.isFemale());
		userName = (TextView) findViewById(R.id.user_name);
		summary = (TextView) findViewById(R.id.user_profile);
		setTitle(userInfo);
		TextView followingNumber = (TextView) findViewById(R.id.user_follow_number);
		followingNumber.setText(Integer.toString(userInfo.getFollowingCount()));
		TextView fansNumber = (TextView) findViewById(R.id.user_fans_number);
		fansNumber.setText(Integer.toString(userInfo.getFollowerCount()));
		TextView vname = (TextView) findViewById(R.id.user_vname);
		UiStaticMethod.setVIP(vname, userInfo, false);
		UiStaticMethod.setText(vname, userInfo.getVname());
		ToggleButton toggleButton = (ToggleButton) findViewById(R.id.user_follow_button);
		Button button = (Button) findViewById(R.id.user_modify_button);
		updateNewFans(userInfo);
		initTitleOnCurrent(userInfo, toggleButton, button);
	}

	private void initTitleOnCurrent(UserDetail userInfo, ToggleButton toggleButton, Button button) {
		if (userInfo.isCurrentUser(getContext())) {
			button.setVisibility(View.VISIBLE);
			toggleButton.setVisibility(View.GONE);
		} else {
			button.setVisibility(View.GONE);
			toggleButton.setVisibility(View.VISIBLE);
			if (userInfo.isFollowedByCurrentUser())
				toggleButton.setChecked(true);
			else
				toggleButton.setChecked(false);
			if (userInfo.isFollowedByCurrentUser() && userInfo.isFollowingCurrentUser()) {
				toggleButton.setText(R.string.follow_each_other);
			}
		}
	}

	/**
	 * 填充用户主页中的动态Layout
	 * 
	 * @param userInfo
	 *            数据源
	 */
	private void fillTimeline(UserDetail userInfo) {
		TextView timelineNumber = (TextView) findViewById(R.id.user_alltimeline_number);
		timelineNumber.setText(Integer.toString(userInfo.getEntitiesCount()));
		TextView bookCommentsNumber = (TextView) findViewById(R.id.user_book_comment_number);
		bookCommentsNumber.setText(Integer.toString(userInfo.getBookCommentsCount()));
		TextView notesNumber = (TextView) findViewById(R.id.user_note_number);
		notesNumber.setText(Integer.toString(userInfo.getNotesCount()));
		View favouriteUpperContainer = findViewById(R.id.user_note);
		View favouriteContainer = findViewById(R.id.user_favourite);
		if (userInfo.isCurrentUser(getContext())) {
			TextView favourite = (TextView) findViewById(R.id.user_favourite_number);
			favourite.setText(Integer.toString(userInfo.getFavouriteCount()));
		} else {
			favouriteContainer.setVisibility(View.GONE);
			favouriteUpperContainer.setBackgroundResource(R.drawable.grey_rect_background);
		}
	}

	/**
	 * 填充用户主页中的阅历Layout
	 * 
	 * @param userInfo
	 *            数据源
	 */
	private void fillReadingData(UserDetail userInfo) {
		TextView readBook = (TextView) findViewById(R.id.user_read_done_number);
		readBook.setText(Integer.toString(userInfo.getReadBookCount()));
		TextView wishReadBook = (TextView) findViewById(R.id.user_read_wish_number);
		wishReadBook.setText(Integer.toString(userInfo.getWishBookCount()));
	}

	/**
	 * 填充用户主页中的书架Layout
	 * 
	 * @param userInfo
	 *            数据源
	 */
	private void fillBookCase(UserDetail userInfo) {
		TextView boughtBookNumber = (TextView) findViewById(R.id.user_book_buy_number);
		boughtBookNumber.setText(Integer.toString(userInfo.getBoughtBookCount()));
		TextView importBookNumber = (TextView) findViewById(R.id.user_book_import_number);
		importBookNumber.setText(Integer.toString(userInfo.getImportBookCount()));
	}
}
