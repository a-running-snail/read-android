package com.jingdong.app.reader.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import cn.jpush.android.data.r;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.view.CommunityBookInfoView;
import com.jingdong.app.reader.view.LinkTouchMovementMethod;
import com.nostra13.universalimageloader.core.ImageLoader;

public class UiStaticMethod {
	public final static float SCROLL_DISTANCE = 0.25f;
	public final static long ILLEGAL_INDEX = -1;
	public final static Pattern AT_NAME = Pattern.compile("@[-a-zA-Z0-9_\u4e00-\u9fa5]+");
	public final static Pattern AT_BOOK = Pattern.compile("<a href='/books/more/[0-9.json]+'>《[^》]+》</a>");
	public final static String LEFT_QUOTE = "《";
	private final static String RIGHT_QUOTE = "》";
	private final static String PNG = ".png";
	private final static String BOOKS = "/books/";
	private final static String USERS = "/users/";
	private final static String READING_DATA = "/reading_data.json";
	private final static String NETWORK_DISABLE = "No address associated with hostname";
	private final static String NULL_STRING_EMPTY = "";
	private final static String NULL_STRING_NULL = "null";
	// 有两个分支的正则表达式，第一个是匹配中文加冒号，第二个是匹配没有http的情况。
	private final static Pattern ILLEGAL_SCHEME = Pattern
			.compile("((?<!(http|https|Http|Https|rtsp|Rtsp)) *[:：])|(?<=([\u4e00-\u9fa5]))");
	private final static Pattern extraBlankLine = Pattern.compile("(\r\n|\n)[\\s\t ]*(\\1)+");
	private final static Pattern blankAsFirstLine = Pattern.compile("^[\\s\n]*(\\S)");
	private final static Pattern blankAsFinalLine = Pattern.compile("(\\S)(\r\n|\n)[\\s\t ]*$");
	private final static int USER_ROLE_VERIFY_PERSON = 1;
	private final static int USER_ROLE_ORGANIZATION = 2;
	private final static int QUALITY = 100;
	private final static int PREFIX_INCRE = 100;
	
	/**
	 * 根据数据源设置RatingBar是否显示以及显示的分数
	 * 
	 * @param ratingBar
	 *            待设置的RatingBar
	 * @param rating
	 *            所打的分数
	 */
	public static void setRatingBar(RatingBar ratingBar,LinearLayout ratingLinearLayout, double rating) {
		double zero = 0;
		if (!Double.isNaN(zero = rating) && zero != 0) {
			if(ratingLinearLayout!=null)
				ratingLinearLayout.setVisibility(View.VISIBLE);
			ratingBar.setVisibility(View.VISIBLE);
			ratingBar.setRating((float) zero);
		} else{
			if(ratingLinearLayout!=null)
				ratingLinearLayout.setVisibility(View.GONE);
			ratingBar.setVisibility(View.GONE);
		}
			
	}

	/**
	 * 将TextView中的@人名和网页地址设置为蓝色，并分别为其添加点击事件。
	 * 
	 * @param context
	 *            数据上下文
	 * @param textView
	 *            待设置的TextView
	 * @param src
	 *            包含有有人名的字符串
	 */
	public static void setAtUrlClickable(Context context, TextView textView, String src) {
		if (!UiStaticMethod.isNullString(src)) {
			
			src = src.replace("\r\n","<br/>");//动态不换行问题修复
			
			SpannableStringBuilder builder = new SpannableStringBuilder(Html.fromHtml(src));
			setAtClickable(context, builder);
			setUrlClickable(context, builder);
			setATagClickable(context, builder);
			
			int start = 0;
			int end = src.indexOf(" | ");
			if (end != -1)
				builder.setSpan(new ForegroundColorSpan(Color.GRAY), start, end + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			setTextClick(textView, builder);

		} else {
			textView.setVisibility(View.GONE);
		}
	}

	public static void setUrlClickable(Context context, TextView textView, String src) {
		if (!UiStaticMethod.isNullString(src)) {
			SpannableStringBuilder builder = new SpannableStringBuilder(Html.fromHtml(src));
			setUrlClickable(context, builder);
			
			int start = 0;
			int end = src.indexOf(" | ");
			if (end != -1)
				builder.setSpan(new ForegroundColorSpan(Color.GRAY), start, end + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			setTextClick(textView, builder);
		} else {
			textView.setVisibility(View.GONE);
		}
	}

	/**
	 * 如果content的文字内容存在，则显示在textView中，否则隐藏textView
	 * 
	 * @param textView
	 *            文字将显示在这个textView中
	 * @param content
	 *            待判断的文字
	 */
	public static boolean setTextString (TextView textView, String content) {
		boolean show = !UiStaticMethod.isNullString(content);
		if (show) {
            int start = 0;
            int end = content.indexOf("|") + 1;
            if(end > 0 && end < content.length()) {
            	String firstStr = content.substring(start, end);
    			String contentStr = content.substring(end);
                String str = "<font color='#999999'>" + firstStr + "</font>" + contentStr;
                textView.setText(Html.fromHtml(str));
            }else {
            	textView.setText(Html.fromHtml(content).toString());
            }
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
		return show;
	}
	
	public static boolean setText(TextView textView, CharSequence content) {
		boolean show = !UiStaticMethod.isNullString(content);
		if (show) {
			textView.setText(content);
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
		return show;
	}
	
	public static boolean setBookInfo(TextView textView, CharSequence content,ImageView book_cover,TextView author,TextView publisher,String imgurl,String auString,String publisherStr,LinearLayout book_info,int flag) {
		boolean show = !UiStaticMethod.isNullString(content);
		if (show) {
			textView.setText(content);
			textView.setVisibility(View.VISIBLE);
			ImageLoader.getInstance().displayImage(imgurl, book_cover,GlobalVarable.getDefaultCommunityDisplayOptions(false));
			book_cover.setVisibility(View.VISIBLE);
			if(!TextUtils.isEmpty(auString))
				author.setText(auString+" 著");
			author.setVisibility(View.VISIBLE);
			publisher.setText(publisherStr);
			publisher.setVisibility(View.VISIBLE);
			book_info.setVisibility(View.VISIBLE);
			if (flag == 2) {
				book_info.setBackgroundResource(R.color.r_bg_sub);
			}
		} else {
			textView.setVisibility(View.GONE);
			book_cover.setVisibility(View.GONE);
			author.setVisibility(View.GONE);
			publisher.setVisibility(View.GONE);
			book_info.setVisibility(View.GONE);
		}
		return show;
	}
	
	public static boolean setBookInfos(TextView textView, CharSequence content,ImageView book_cover,TextView author,TextView publisher,String imgurl,String auString,String publisherStr,LinearLayout book_info) {
		boolean show = !UiStaticMethod.isNullString(content);
		if (show) {
			textView.setText(content);
			textView.setVisibility(View.VISIBLE);
			ImageLoader.getInstance().displayImage(imgurl, book_cover,GlobalVarable.getDefaultCommunityDisplayOptions(false));
			book_cover.setVisibility(View.VISIBLE);
			author.setText(auString);
			author.setVisibility(View.VISIBLE);
			publisher.setText(publisherStr);
			publisher.setVisibility(View.VISIBLE);
			book_info.setVisibility(View.VISIBLE);
			book_info.setBackgroundResource(R.color.bg_main);
		} else {
			textView.setVisibility(View.GONE);
			book_cover.setVisibility(View.GONE);
			author.setVisibility(View.GONE);
			publisher.setVisibility(View.GONE);
			book_info.setVisibility(View.GONE);
		}
		return show;
	}
	
	public static boolean setTextNoGone(TextView textView, CharSequence content) {
		boolean show = !UiStaticMethod.isNullString(content);
		if (show) {
			textView.setText(content);
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.INVISIBLE);
		}
		return show;
	}

	/**
	 * 判断指定字符串是否为空，为null，为""。如果以上情况中有一种成立，则返回true，否则返回false
	 * 
	 * @param src
	 *            待判断字符串
	 * @return true表示给定字符串为空字符串，false表示给定字符串不为空。
	 */
	public static boolean isNullString(CharSequence src) {
		return (src == null || src.equals(NULL_STRING_EMPTY) || src.equals(NULL_STRING_NULL));
	}

	/**
	 * 判断当前机器是否拥有SmartBar
	 * 
	 * @return true表示当前机器拥有SmartBar,即为魅族机器。false表示当前机器没有SmartBar，为普通机器。
	 */
	public static boolean hasSmartBar() {
		try {
			// 新型号可用反射调用Build.hasSmartBar()
			Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
			return ((Boolean) method.invoke(null)).booleanValue();
		} catch (Exception e) {
		}

		// 反射不到Build.hasSmartBar()，则用Build.DEVICE判断
		if (Build.DEVICE.equals("mx2")) {
			return true;
		} else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
			return false;
		}
		return false;
	}

	/**
	 * 跟据手机是普通手机还是魅族手机，返回合适的Icon资源。
	 * 
	 * @param noramlIcon
	 *            普通手机的Icon资源
	 * @param meizuIcon
	 *            魅族手机的Icon资源
	 * @return 当前手机应显示的Icon资源
	 */
	public static void setIcon(int noramlIcon, int meizuIcon, MenuItem item) {
		int icon = hasSmartBar() ? meizuIcon : noramlIcon;
		item.setIcon(icon);
	}

	/**
	 * 如果用户是VIP，则在textView左边或右边显示VIP认证图像
	 * 
	 * @param textView
	 *            待填充的用户名区域
	 * @param user
	 *            用户信息
	 * @param right
	 *            true表示在Textview右边显示VIP认证图像，false表示在TextView左边显示VIP认证图像
	 */
	public static void setVIP(TextView textView, UserInfo user, boolean right) {
		int imageId;
		switch (user.getRole()) {
		case UiStaticMethod.USER_ROLE_VERIFY_PERSON:
			imageId = R.drawable.profile_verify_person;
			break;
		case UiStaticMethod.USER_ROLE_ORGANIZATION:
			imageId = R.drawable.profile_verify_organization;
			break;
		default:
			imageId = 0;
		}
		if (right)
			textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, imageId, 0);
		else
			textView.setCompoundDrawablesWithIntrinsicBounds(imageId, 0, 0, 0);
	}
	
	/**
	 * 如果用户是VIP，则显示VIP认证图像
	 * 
	 * @param imageView
	 *            用户标签图
	 * @param user
	 *            用户信息
	 */
	public static void setVIP(ImageView imageView, UserInfo user) {
		int imageId;
		switch (user.getRole()) {
		case UiStaticMethod.USER_ROLE_VERIFY_PERSON:
			imageId = R.drawable.profile_verify_person;
			break;
		case UiStaticMethod.USER_ROLE_ORGANIZATION:
			imageId = R.drawable.profile_verify_organization;
			break;
		default:
			imageId = 0;
		}
		if (imageId == 0) {
			imageView.setVisibility(View.GONE);
		} else {
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageResource(imageId);
		}
	}

	/**
	 * 设置底部loading的布局
	 * 
	 * @param context
	 *            数据上下文
	 * @param loading
	 *            包含有loading的view
	 * @return loadingLayout的布局
	 */
	public static LinearLayout getFooterParent(Context context, View loading) {
		LinearLayout footerParent = new LinearLayout(context);
		LinearLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;
		footerParent.addView(loading, layoutParams);
		return footerParent;
	}

	/**
	 * 自定义searchview的hintIcon
	 * 
	 * @param context
	 *            数据上下文
	 * @param searchView
	 *            待设置的searchview
	 * @param iconResId
	 *            icon的id
	 */
	public static void setSearchViewSearchIcon(Context context, SearchView searchView, int iconResId) {
		searchView.setIconifiedByDefault(false);
		int searchImg = context.getResources().getIdentifier("android:id/search_mag_icon", null, null);
		ImageView imageView = (ImageView) searchView.findViewById(searchImg);
		if (imageView != null)
			imageView.setImageResource(iconResId);
	}

	/**
	 * 自定义searchview的closeIcon。 目前没有hack进去。
	 * 
	 * @param context
	 *            数据上下文
	 * @param searchView
	 *            待设置的searchview
	 * @param iconResId
	 *            icon的id
	 */
	public static void setSearchViewCloserIcon(Context context, SearchView searchView, int iconResId) {
		int searchImg = context.getResources().getIdentifier("android:id/search_close_btn", null, null);
		ImageView imageView = (ImageView) searchView.findViewById(searchImg);
		if (imageView != null)
			imageView.setImageResource(iconResId);
	}

	public static void setSearchViewColor(SearchView searchView, int color) {
		int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) searchView.findViewById(id);
		if (textView != null) {
			textView.setTextColor(color);
		}

	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public static byte[] bitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream);
		return outputStream.toByteArray();
	}

	public static File bitmapToFile(Bitmap bitmap, File dir, int prefix) throws IOException {
		File file = File.createTempFile(String.valueOf(prefix + PREFIX_INCRE), PNG, dir);
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
		bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY, outputStream);
		outputStream.flush();
		outputStream.close();
		return file;
	}

	/**
	 * 当前网络是否无法连接
	 * 
	 * @param jsonString
	 *            可能包含有json字符串，也可能是网络无法连接时返回的错误字符串
	 * @return true表示网络正常，false表示网络无法连接。
	 */
	public static boolean isNetWorkConnected(String jsonString) {
		boolean result;
		if (jsonString.contains(UiStaticMethod.NETWORK_DISABLE))
			result = false;
		else
			result = true;
		return result;
	}

	public static Builder createConfirmDialog(Context context, int title, int content, OnClickListener clickListener) {
		AlertDialog.Builder builder = new Builder(context).setTitle(title).setMessage(content);
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton(R.string.delete, clickListener);
		return builder;
	}

	public static String getReadingDataUrl(String userId, long bookId) {
		StringBuilder builder = new StringBuilder();
		builder.append(URLText.baseUrl);
		builder.append(UiStaticMethod.BOOKS);
		builder.append(Long.toString(bookId));
		builder.append(UiStaticMethod.USERS);
		builder.append(userId);
		builder.append(UiStaticMethod.READING_DATA);
		return builder.toString();
	}

	/**
	 * 格式化字符串，去除其中的<strong></strong>和<a/>标签以及多余的空行
	 * 
	 * @param src
	 *            待处理的字符串
	 * @return 处理后的结果
	 */
	public static String formatListItem(String src) {
		String result = null;
		if (src != null) {
			result = src;
			result = UiStaticMethod.extraBlankLine.matcher(result).replaceAll("$1");
			result = Html.fromHtml(src).toString();
			result = formatConcreteTweet(src);
		}
		return result;
	}

	public static String formatConcreteTweet(String src) {
		String result = null;
		if (src != null) {
			result = src;
			result = UiStaticMethod.blankAsFirstLine.matcher(result).replaceAll("$1");
			result = UiStaticMethod.blankAsFinalLine.matcher(result).replaceAll("$1");
		}
		return result;
	}

	public static void loadThumbnail(Context context, ImageView networkImageView, String url, boolean female) {

		ImageLoader.getInstance().displayImage(url, networkImageView,GlobalVarable.getDefaultAvatarDisplayOptions(female));
		
	}

	public static void onMovedToScrapHeap(View container, Class<?> type, int id) {
		if (container.getClass() == type) {
			View item = container.findViewById(id);
			if (item != null && item instanceof ImageView) {
				ImageView imageView = (ImageView) item;
				ImageLoader.getInstance().cancelDisplayTask(imageView);
			}
		}
	}

	public static String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		NumberFormat f = DecimalFormat.getInstance();
		if (f instanceof DecimalFormat) {
			DecimalFormat decimalFormat = (DecimalFormat) f;
			decimalFormat.setDecimalSeparatorAlwaysShown(true);
			decimalFormat.applyLocalizedPattern("#,##0.##");
			String result = decimalFormat.format(size / Math.pow(1024, digitGroups));
			return result + " " + units[digitGroups];
		}
		return null;
	}

	public static String getChannel(Context context) {
		String channel = null;
		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);
			String umengChannel = appInfo.metaData.getString("UMENG_CHANNEL");
			channel = umengChannel.toLowerCase(Locale.US);
		} catch (NameNotFoundException e) {
			channel = null;
			MZLog.e("Channel", Log.getStackTraceString(e));
		}
		return channel;
	}

	public static String getQuotedTitle(String soruce) {
		String result = soruce;
		if (soruce.startsWith(LEFT_QUOTE) && soruce.endsWith(RIGHT_QUOTE))
			;
		else {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(LEFT_QUOTE);
			stringBuilder.append(soruce);
			stringBuilder.append(RIGHT_QUOTE);
			result = stringBuilder.toString();
		}
		return result;
	}

	public static int getScreenWidth(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		return width;
	}

	public static int getScreenHeight(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int height = size.y;
		return height;
	}

	public static File viewToFile(File tmpDir, ListView listview, int width) {
		File file, dest = null;
		ListAdapter adapter = listview.getAdapter();
		int itemscount = adapter.getCount();
		int allitemsheight = 0;
		List<File> files = new LinkedList<File>();
		for (int i = 0; i < itemscount; i++) {
			View childView = adapter.getView(i, null, listview);
			childView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
			childView.setDrawingCacheEnabled(true);
			childView.buildDrawingCache();
			try {
				file = bitmapToFile(childView.getDrawingCache(), tmpDir, i);
				files.add(file);
			} catch (IOException e) {
				MZLog.e("viewToBitmap", Log.getStackTraceString(e));
			} finally {
				childView.destroyDrawingCache();
			}
			allitemsheight += childView.getMeasuredHeight();
		}
		Bitmap bigbitmap = Bitmap.createBitmap(width, allitemsheight, Bitmap.Config.RGB_565);
		Canvas bigcanvas = new Canvas(bigbitmap);
		Paint paint = new Paint();
		int iHeight = 0;
		for (int i = 0; i < files.size(); i++) {
			Bitmap bmp = BitmapFactory.decodeFile(files.get(i).getAbsolutePath());
			bigcanvas.drawBitmap(bmp, 0, iHeight, paint);
			iHeight += bmp.getHeight();
			bmp.recycle();
			bmp = null;
			files.get(i).delete();
		}
		try {
			dest = bitmapToFile(bigbitmap, tmpDir, files.size());
		} catch (IOException e) {
			MZLog.e("viewToBitmap", Log.getStackTraceString(e));
		} finally {
			bigbitmap.recycle();
			bigbitmap = null;
		}
		return dest;
	}

	public static void showGuide(Context context, View rootView, int stringId) {
		View popUpView = View.inflate(context, R.layout.view_guide, null);
		TextView textView = (TextView) popUpView.findViewById(R.id.guide_txt);
		textView.setText(stringId);
		PopupWindow popupWindow = new PopupWindow(popUpView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.color.transparent));
		popupWindow.showAsDropDown(rootView);
	}


	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void goToImmersiveMode(Window window) {
		int uiOptions = window.getDecorView().getSystemUiVisibility();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE;
		window.getDecorView().setSystemUiVisibility(uiOptions);
	}
	
	private static void setTextClick(TextView textView, SpannableStringBuilder builder) {
		textView.setText(builder, BufferType.SPANNABLE);
		textView.setMovementMethod(LinkTouchMovementMethod.getInstance());
		textView.setFocusable(false);
		textView.setFocusableInTouchMode(false);
		textView.setVisibility(View.VISIBLE);
	}

	/**
	 * 将src中的@人名设置为蓝色，并为其添加点击事件
	 * 
	 * @param context
	 *            数据上下文
	 * @param builder
	 *            富文本构建器
	 */
	private static void setAtClickable(final Context context, SpannableStringBuilder builder) {
		Matcher matcher = UiStaticMethod.AT_NAME.matcher(builder.toString());
		while (matcher.find()) {
			final String name = matcher.group();
			MZBookClickableSpan.ClickCallback callback = new MZBookClickableSpan.ClickCallback() {

				@Override
				public void onClick(View widget) {
					Intent intent = new Intent(context, UserActivity.class);
					intent.putExtra(UserFragment.USER_NAME, name.substring(1));
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(intent);
				}

			};
			builder.setSpan(new MZBookClickableSpan(callback, context
					.getResources().getColor(R.color.timeline_book_title),
					matcher.start(), matcher.end()), matcher.start(), matcher
					.end(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	/**
	 * 将src中的网页地址设置为蓝色，并为其添加点击事件。这个正则扫描比较复杂，首先使用Patterns.WEB_URL进行第一次匹配，
	 * 但这此匹配容错性高，所以可能有“好:www.com”这样的域名，再使用ILLEGAL_SCHEME进行第二次匹配，消除掉这些意外。
	 * 
	 * @param context
	 *            数据上下文
	 * @param builder
	 *            富文本构建器
	 */
	private static void setUrlClickable(final Context context, SpannableStringBuilder builder) {
		final Matcher matcher = Patterns.WEB_URL.matcher(builder.toString());
		while (matcher.find()) {
			// 下面是消除多余的匹配
			final String name = matcher.group(), url;
			int start = -1, generalStart;
			Matcher illegal = ILLEGAL_SCHEME.matcher(name);
			while (illegal.find()) {
				start = illegal.end();
			}
			if (start == -1) {
				generalStart = matcher.start();
				if (!name.startsWith("http") && !name.startsWith("https"))
					url = "http://" + name;
				else
					url = name;
			} else {
				generalStart = start + matcher.start();
				if (!name.startsWith("http") && !name.startsWith("https"))
					url = "http://" + name.substring(start);
				else
					url = name.substring(start);
			}
			// 下面设置点击事件和字体颜色
			MZBookClickableSpan.ClickCallback callback = new MZBookClickableSpan.ClickCallback() {

				@Override
				public void onClick(View widget) {
					Intent intent = new Intent(context, WebViewActivity.class);
					intent.putExtra(WebViewActivity.UrlKey, url);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(intent);
				}

			};
			builder.setSpan(new MZBookClickableSpan(callback, context
					.getResources().getColor(R.color.timeline_book_title),
					generalStart, matcher.end()), generalStart, matcher.end(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	private static void setATagClickable(Context context, SpannableStringBuilder builder) {
		URLSpan[] spans = builder.getSpans(0, builder.length(), URLSpan.class);
		URLSpan currentSpan;
		int start, end;
		for (int i = 0; i < spans.length; i++) {
			currentSpan = spans[i];
			start = builder.getSpanStart(currentSpan);
			end = builder.getSpanEnd(currentSpan);
			builder.removeSpan(currentSpan);
			builder.setSpan(new MZBookURLSpan(currentSpan.getURL(), context,
					start, end), start, end,
					SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
	
	public static boolean isEmpty(String str){
		if(null==str)
			return true;
		if(str.equals("")||str.length()==0)
			return true;
		if(str.equals("null"))
			return true;
		return false;
		
	}
	
	
	
	public static int getColorFromString(String color) {
		int alpha = 0;
		int red = 0;
		int green = 0;
		int blue = 0;
		if (color.contains(",")) {
			String[] colorArray = color.split(",");
			if (colorArray.length == 3) {
				alpha = 255;
				red = Integer.parseInt(colorArray[0].trim());
				green = Integer.parseInt(colorArray[1].trim());
				blue = Integer.parseInt(colorArray[2].trim());
			} else if (colorArray.length == 4) {
				red = Integer.parseInt(colorArray[0].trim());
				green = Integer.parseInt(colorArray[1].trim());
				blue = Integer.parseInt(colorArray[2].trim());
				float a = Float.parseFloat(colorArray[3].trim());
				alpha = (int) (a * 255);
			} else {
				return 0;
			}
		} else if (color.length() == 8) {
			String a = color.substring(0, 2);
			String r = color.substring(2, 4);
			String g = color.substring(4, 6);
			String b = color.substring(6);
			alpha = Integer.parseInt(a, 16);
			red = Integer.parseInt(r, 16);
			green = Integer.parseInt(g, 16);
			blue = Integer.parseInt(b, 16);
		} else if (color.length() == 4) {
			String a = color.substring(0, 1);
			String r = color.substring(1, 2);
			String g = color.substring(2, 3);
			String b = color.substring(3);
			a = a + a;
			r = r + r;
			g = g + g;
			b = b + b;
			alpha = Integer.parseInt(a, 16);
			red = Integer.parseInt(r, 16);
			green = Integer.parseInt(g, 16);
			blue = Integer.parseInt(b, 16);
		} else {
			return 0;
		}
		return Color.argb(alpha, red, green, blue);
	}
}
