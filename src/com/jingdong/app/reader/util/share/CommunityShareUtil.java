package com.jingdong.app.reader.util.share;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity.OnShareItemClickedListener;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 朋友圈分享公共类
 * @author tanmojie
 *
 */
public class CommunityShareUtil  {

	private com.jingdong.app.reader.view.SharePopupWindow sharePopupWindow;
	PopupWindow menuWindow;
	
	public PopupWindow getCommunityShareView(final Entity entity,final Activity context) {
		if (entity != null && entity.getRenderBody() != null && context !=null) {
			String shareString = entity.getShareString(context.getResources());

			String buildShareString = "";
			Pattern pattern = Pattern.compile("<a(.*?)>(.*?)</a>",
					Pattern.CASE_INSENSITIVE);
			Matcher m = pattern.matcher(shareString);
			while (m.find()) {
				buildShareString = m.group(2);
				break;
			}

			if (!UiStaticMethod.isEmpty(buildShareString)) {
				shareString = shareString.replaceAll("<a(.*?)>(.*?)</a>",
						buildShareString);
			}
			
			if(TextUtils.isEmpty(shareString))
				shareString="";
			
			final String descripString =shareString;
			
			final String titleString =descripString;//UiStaticMethod.isEmpty(entity.getWeiXinTitle(getResources()))?shareString:entity.getWeiXinTitle(getResources());
			
			final String url ="http://e.m.jd.com/edm.html   ";
			
			MZLog.d("cj", "descripString"+descripString);
			MZLog.d("cj", "titleString"+titleString);
			
			
			
			//weixin
			
			final String weixinImgTitleString =entity.getShareToWeixinTitle(context.getResources());
			final String weixinImgBookNameString =entity.getBookNameString();
			final float weixinImgRatingString =entity.getBookRating();
			final String weixinImgContent1String =entity.getRenderBody().getContent();
			final String weixinImgcontentString =entity.getRenderBody().getQuote();
			final String weixinImgDigestString =entity.getRenderBody().getQuote();
			
			
			sharePopupWindow = new com.jingdong.app.reader.view.SharePopupWindow(context,true);

			
			menuWindow = new SharePopupWindow(
					context, new OnShareItemClickedListener() {

						@Override
						public void onShareItemClicked(int type, int position) {

							switch (type) {
							
							//position 无用
							
							case 101:// weibo
								sharePopupWindow.shareToWeibo(context, titleString, descripString ,"",url,"");		
								break;

							case 102:// wechat_friend
								 startShareBitmap(weixinImgTitleString,weixinImgBookNameString,weixinImgContent1String,weixinImgcontentString,weixinImgDigestString,weixinImgRatingString,1,context);								 
								break;
								
							case 103:// wechat
								 startShareBitmap(weixinImgTitleString,weixinImgBookNameString,weixinImgContent1String,weixinImgcontentString,weixinImgDigestString,weixinImgRatingString,0,context);								 
								break;
							}

						}
					},  -1);
//			menuWindow.showAtLocation(
//					TimelineTweetActivity.this.findViewById(R.id.main),
//					Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			
			
			return menuWindow;
		} else {
			Toast.makeText(context, R.string.unableToShare,
					Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	public void startShareBitmap(String title,String bookName,String content1,String content,String digest,float rating,int type,Activity context){
		
		View view = LayoutInflater.from(context).inflate(
				R.layout.timeline_share_layout, null);
		
		TextView tiTextView=(TextView) view.findViewById(R.id.title);
		TextView bookNameTextView=(TextView) view.findViewById(R.id.bookName);
		TextView content1TextView=(TextView) view.findViewById(R.id.content1);
		TextView contentTextView=(TextView) view.findViewById(R.id.content);
		TextView digestTextView=(TextView) view.findViewById(R.id.digest);
		RatingBar ratingBar =(RatingBar) view.findViewById(R.id.rating);
		
		if(TextUtils.isEmpty(title))
			tiTextView.setVisibility(View.GONE);
		
		else tiTextView.setText(title);
		
		if(TextUtils.isEmpty(bookName)||"null".equals(bookName))
			bookNameTextView.setVisibility(View.GONE);
		else {
			bookNameTextView.setText(bookName);
		}
		
		if(TextUtils.isEmpty(content1))
			content1TextView.setVisibility(View.GONE);
		else {
			content1TextView.setText(content1);
		}
		
		if(TextUtils.isEmpty(content))
			contentTextView.setVisibility(View.GONE);
		else {
			contentTextView.setText(content);
		}
		
		if(TextUtils.isEmpty(digest))
			digestTextView.setVisibility(View.GONE);
		else {
			digestTextView.setText(digest);
		}
		
		if(Double.isNaN(rating)||rating<=0)
		 ratingBar.setVisibility(View.GONE);
		else {
			ratingBar.setRating(rating);
		}
		
		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Bitmap.Config.RGB_565);
		view.draw(new Canvas(bitmap));

		
		//获得分享的图片
		//测试分享的图片
//		ImageView imageView=new ImageView(TimelineTweetActivity.this);
//		imageView.setImageBitmap(bitmap);
//	    WindowManager mWm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);   
//	    WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();   
//	    mWm.addView(imageView, mParams);   
//		

		//分享的图片
//		sharePopupWindow.weixinImg(TimelineTweetActivity.this, bitmap, 1);
		WXShareHelper.getInstance().shareImage(context, bitmap, type);
//		sharePopupWindow.weixin(TimelineTweetActivity.this, "", "", bitmap, "", 1);//分享朋友圈
		
	}
}
