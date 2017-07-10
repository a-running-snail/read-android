package com.jingdong.app.reader.community;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookCartActivity;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager.AddToCartListener;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadTool.DownloadConfirmListener;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.SimplifiedDetail;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OnlineReadManager;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity.OnShareItemClickedListener;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.dialog.DialogManager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 朋友圈公共类
 * @author tanmojie
 *
 */
public class CommunityUtil  {
	
	
	/**
	 * 用户点击favourite按钮后，本方法负责向服务器提交结果
	 * 
	 * @param context
	 *            当前数据上下文
	 */
	public void clickFavourite(Context context,TweetModel entity) {
		boolean nextState = !entity.isFavourite();
		String urlString;
		if (nextState)
			urlString = URLText.favouriteUrl;
		else
			urlString = URLText.unFavouriteUrl;
		postFavourite(context, nextState, urlString, entity, 1);

	}
	
	/**
	 * 用户点击favourite按钮后，本方法负责向服务器提交结果
	 * 
	 * @param context
	 *            当前数据上下文
	 */
	public void clickRecommand(Context context,Entity entity) {
		boolean nextState = !entity.isViewerRecommended();
		String urlString;
		if (nextState)
			urlString = URLText.likeEntityUrl;
		else
			urlString = URLText.unlikeEntityUrl;
		postRecommand(context, nextState, urlString, entity, 2);

	}
	
	/**
	 * 向服务器提交点赞，喜欢操作
	 * @param context
	 * @param nextState
	 * @param baseUrl
	 * @param entityId
	 * @param type
	 */
	public void postFavourite(final Context context,
			Boolean nextState, final String baseUrl, final TweetModel entity, int type) {

		final boolean tempNextState = nextState;
		final int temptype = type;//1为喜欢 2为推荐

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				WebRequestHelper.post(baseUrl,
						RequestParamsPool.getTimelineFavoriteParams(entity.getGuid()),
						true, new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								boolean success;
								success = parsePostResult(new String(
										responseBody));

								if (success) {
									if (temptype == 1){
										entity.setFavourite(tempNextState);
									}
//									else
//										entity.setViewerRecommended(tempNextState);
								} else {

									try {

										JSONObject object = new JSONObject(
												new String(responseBody));

										String msg = object
												.optString("message");

										if (!TextUtils.isEmpty(msg))
											ToastUtil.showToastInThread(msg,
													Toast.LENGTH_SHORT);

									} catch (Exception e) {
										e.printStackTrace();
									}

								}

//								if (temptype == 1)
//									notifyDataChanged(CLICK_FAVOURITE, success);
//								else
//									notifyDataChanged(CLICK_RECOMMAND, success);

							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {

								ToastUtil.showToastInThread("请求出错了，请检查网络！",
										Toast.LENGTH_SHORT);
							}
						});
			}
		});
	}
	
	/**
	 * 向服务器提交点赞，喜欢操作
	 * @param context
	 * @param nextState
	 * @param baseUrl
	 * @param entityId
	 * @param type
	 */
	public void postRecommand(final Context context,
			Boolean nextState, final String baseUrl, final Entity entity, int type) {

		final boolean tempNextState = nextState;
		final int temptype = type;//1为喜欢 2为推荐

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				WebRequestHelper.post(baseUrl,
						RequestParamsPool.getTimelineFavoriteParams(entity.getGuid()),
						true, new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								boolean success;
								success = parsePostResult(new String(
										responseBody));

								if (success) {
									entity.setViewerRecommended(tempNextState);
								} else {

									try {

										JSONObject object = new JSONObject(
												new String(responseBody));

										String msg = object
												.optString("message");

										if (!TextUtils.isEmpty(msg))
											ToastUtil.showToastInThread(msg,
													Toast.LENGTH_SHORT);

									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {

								ToastUtil.showToastInThread("请求出错了，请检查网络！",
										Toast.LENGTH_SHORT);
							}
						});
			}
		});
	}
	
	
	/**
	 * 判断所发出的post请求是否正确的被服务器响应。
	 * 
	 * @param result
	 *            发出post请求后的返回值
	 * @return true表示该操作成功，false表示该操作失败
	 */
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

	
	
	/**
	 * 加入书架
	 * @param book
	 */
	public static void addToBookcase(final Book book,final Context context){
		LocalBook localBook = LocalBook.getLocalBook(book.bookId, LoginUser.getpin());
		if(localBook!=null){
			Toast.makeText(context, "此书已在书架中！", 0).show();
			return ;
		}
		if(book.isEbook()){
			if (NetWorkUtils.isNetworkConnected(context)) {
				addBookToBookcase(context,book.bookId);
			}
		}
	}
	
	private static void addBookToBookcase(final Context context,long bookid) {
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getBookInfoParams(bookid,null), true,
				new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

					}
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);
					
						try {
							JSONObject json = new JSONObject(result);
							String codeStr = json.optString("code");
							if (codeStr != null && codeStr.equals("0")) {
								final JDBookInfo bookInfo = GsonUtils.fromJson(result, JDBookInfo.class);
								if (bookInfo != null && bookInfo.detail != null) {
									if (bookInfo.detail.isEBook) {
										if(bookInfo.detail.isFree){//是否是免费书
											OrderEntity orderEntity = OrderEntity.FromJDBooKInfo2OrderEntityWithoutOrderid(bookInfo.detail);
											DownloadTool.download((Activity) context, orderEntity, null, false, LocalBook.SOURCE_BUYED_BOOK, 0, true, null, false);
										}else if(bookInfo.detail.isAlreadyBuy){//是否已购买
											if (NetWorkUtils.isWifiConnected(context)) {
												downloadBook(context,bookInfo.detail);
											}else{
												String message = "当前无WiFi连接，继续下载会使用你的流量，是否继续？";
												DialogManager.showCommonDialog(context, "温馨提示", message,
														"下载", "取消", new DialogInterface.OnClickListener() {

													@Override
													public void onClick(DialogInterface dialog, int which) {
														switch (which) {
														case DialogInterface.BUTTON_POSITIVE:
															downloadBook(context, bookInfo.detail);
															break;
														case DialogInterface.BUTTON_NEGATIVE:
															break;
														default:
															break;
														}
														dialog.dismiss();
													}
												});
											}
										}else if(bookInfo.detail.isFluentRead && bookInfo.detail.isUserCanFluentRead){//该书是否支持畅读并且用户有权限畅读该书
											onlineRead(context,bookInfo,null);
										}else{//没有购买并且不能畅读该书
											String message;
											if(bookInfo.detail.isFluentRead){//该书可以畅读
												message = "你可以选择开通畅读权限随时加入书架，或者购买此书";
												showTipDialog(context,message,bookInfo,true);
											}
											else{//该书不能畅读
												message = "你可以选择购买此书随时加入书架";
												showTipDialog(context,message,bookInfo,false);
											}
										}
										
										
									} 
								}
							} else {
								Toast.makeText(context, context.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	/**
	 * 显示不能加入书架的提示
	 * @param context
	 * @param message
	 * @param bookInfo
	 * @param showPostiveButton
	 */
	private static void showTipDialog(final Context context,String message,final JDBookInfo bookInfo,final boolean showPostiveButton){
		DialogManager.showCommonDialog(context, "温馨提示", message,
				"购买此书", "开通畅读", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (LoginUser.isLogin()) {
						SimplifiedDetail detail = new SimplifiedDetail();
						detail.bookId = bookInfo.detail.bookId;
						BookCartManager.getInstance().addToShoppingCart(context, detail, new AddToCartListener() {

							@Override
							public void onAddSuccess() {
								Intent itIntent = new Intent(context, BookCartActivity.class);
								context.startActivity(itIntent);
							}
							@Override
							public void onAddFail() {
								Toast.makeText(context, "购买失败，请检查网络状况是否正常！", 0).show();
							}
						});
					} else {
						Intent login = new Intent(context, LoginActivity.class);
						context.startActivity(login);
					}
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					if(showPostiveButton){
						Intent intent = new Intent(context,WebViewActivity.class);
						intent.putExtra(WebViewActivity.UrlKey,
								"http://e.m.jd.com/readCard.html");
						intent.putExtra(WebViewActivity.TopbarKey, true);
						intent.putExtra(WebViewActivity.BrowserKey, false);
						intent.putExtra(WebViewActivity.TitleKey, "购买畅读卡");
						context.startActivity(intent);
					}else{
					}
					break;
				default:
					break;
				}
				dialog.dismiss();
			}
		},showPostiveButton);
	}
	
	/**
	 * 畅读书籍
	 * @param context
	 * @param jdbook
	 * @param listener
	 */
	private static void onlineRead(Context context, JDBookInfo jdbook, DownloadConfirmListener listener) {
		BookInforEDetail bookE = new BookInforEDetail();// 下载实体
		bookE.bookid = jdbook.detail.bookId;// bookid
		bookE.picUrl = jdbook.detail.logo;// 小图
		bookE.size = jdbook.detail.size + "";
		bookE.largeSizeImgUrl = jdbook.detail.largeLogo;// 大图
		bookE.bookType = LocalBook.TYPE_EBOOK;
		// 书的类型:电子书or多媒体书
		bookE.formatName = jdbook.detail.format;// 图书格式。
		bookE.author = jdbook.detail.author;// 作者
		bookE.bookName = jdbook.detail.bookName;// 书名
		OnlineReadManager.checkChangduAuthAndAdd(bookE, (Activity) context);
	}
	
	/**
	 * 下载已购书籍
	 * @param context
	 * @param bookInfo
	 */
	public static  void downloadBook(Context context,JDBookInfo.Detail bookInfo){
		OrderEntity orderEntity = OrderEntity
				.FromJDBooKInfo2OrderEntity(bookInfo);
		DownloadTool.download(
				(Activity) context,
				orderEntity, null, false,
				LocalBook.SOURCE_BUYED_BOOK, 0, false,new DownloadConfirmListener() {
					
					@Override
					public void onConfirm() {
					}
					@Override
					public void onCancel() {
					}
				},false);
	}
	
	public static void unWishBook(Context context,long bookid) {
		WebRequestHelper.post(URLText.unwishBook, RequestParamsPool.unWishBookParams(bookid), true, new MyAsyncHttpResponseHandler(context) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

			}
		});
	}

	public static void wishBook(Context context,long bookid) {
		WebRequestHelper.post(URLText.wishBook, RequestParamsPool.wishBookParams(bookid), true, new MyAsyncHttpResponseHandler(context) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

			}
		});
	}
	
	public static String getString(int count,String type){
		String str="";
		if(count==0){
			if(type.equals("comment"))
				str="评论";
			else
				str="赞";
		}
		else
			str = count+"";
		return str;
	}
}
