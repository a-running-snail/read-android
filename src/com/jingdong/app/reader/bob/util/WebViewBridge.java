package com.jingdong.app.reader.bob.util;

import java.util.ArrayList;

import org.apache.http.Header;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookcaseCloudActivity;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.activity.MainActivity;
import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.config.ITransKey;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.me.activity.OrderActivity;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.myInterface.WebviewLoadInterface;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.opentask.InterfaceBroadcastReceiver;
import com.jingdong.app.reader.opentask.InterfaceBroadcastReceiver.Command;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ActivityUtils;
import com.jingdong.app.reader.util.DataIntent;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.NetWorkUtils.NetworkConnectType;
import com.jingdong.app.reader.util.OnLinePayTools;
import com.jingdong.app.reader.util.share.ShareResultListener;
import com.jingdong.app.reader.util.share.WBShareHelper;
import com.jingdong.app.reader.util.share.WXShareHelper;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.dialog.DialogManager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebViewBridge implements ITransKey{
	
	public static final String interfaceNameString = "bridge";
	private static MyActivity mActivity;
	private WebviewLoadInterface webViewInterface;
	public String params="";
	public int operationSource=0;
	public int requestCode = 0;

	public WebViewBridge(MyActivity mActivity,WebviewLoadInterface webviewInterface) {
		WebViewBridge.mActivity = mActivity;
		this.webViewInterface = webviewInterface;
	}
	
	

	/**
	* @Description: 打开已购列表
	* @author xuhongwei1
	* @date 2015年11月18日 上午9:40:49 
	* @throws 
	*/ 
	@JavascriptInterface
	public void goToBookcaseCloud() {
		ActivityUtils.startActivity(mActivity, new Intent(
				mActivity, BookcaseCloudActivity.class));
	}
	
	/**
	* @Description: 跳转书城
	* @author xuhongwei1
	* @date 2015年11月18日 上午9:41:00 
	* @throws 
	*/ 
	@JavascriptInterface
	public void goToBookStore() {
		goToShopping();
	}
	
	/**
	* @Description: 登陆
	* @author xuhongwei1
	* @date 2015年11月18日 上午9:41:13 
	* @throws 
	*/ 
	@JavascriptInterface
	public void goToLogin(String params) {
		if(params!=null)
			this.params = params;
		
		Intent intent = new Intent(mActivity,LoginActivity.class);
		mActivity.startActivityForResult(intent, requestCode);
	}
	
	/**
	* @Description: 打开应用市场
	* @author xuhongwei1
	* @date 2015年11月18日 上午9:29:51 
	* @throws 
	*/ 
	@JavascriptInterface
	public void goToAppMarket() {
		try{
			Uri uri = Uri.parse("market://details?id=" + mActivity.getPackageName());  
			Intent intent = new Intent(Intent.ACTION_VIEW,uri);  
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);          
			mActivity.startActivity(intent);
		 }catch(ActivityNotFoundException e){
			 Toast.makeText(mActivity, "无法打开应用市场", Toast.LENGTH_SHORT).show();
		 }
	}
	
	/**
	* @Description: 打开应用市场
	* @author xuhongwei1
	* @date 2015年11月18日 上午9:29:51 
	* @throws 
	*/ 
	@JavascriptInterface
	public void goToAppMarket(String packagename) {
		try{
			Uri uri = Uri.parse("market://details?id=" + packagename);  
			Intent intent = new Intent(Intent.ACTION_VIEW,uri);  
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);          
			mActivity.startActivity(intent);
		 }catch(ActivityNotFoundException e){
			 Toast.makeText(mActivity, "无法打开应用市场", Toast.LENGTH_SHORT).show();
		 }
	}

	/**
	 * 
	 * @Title: clearShoppingCart
	 * @Description: 清空指定id的商品
	 * @param @param buiedBookIds
	 * @return void
	 * @throws
	 * @date 2015年4月16日 下午3:58:42
	 */

	@JavascriptInterface
	public void clearShoppingCart(String buiedBookIds) {
		MZLog.d("JD_Reader", "WebViewBridge#clearShoppingCart" + buiedBookIds);
		// String[] bookIds = null;
		// if (buiedBookIds.contains(",")) {
		// bookIds = buiedBookIds.split(",");
		// } else {
		// bookIds = new String[] { buiedBookIds };
		// }
		// JDBookCart.getInstance(mActivity).clearBookCart(mActivity, bookIds);

	}

	/**
	 * 
	 * @Title: goToShopping
	 * @Description: 去书城
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年4月16日 下午3:58:26
	 */
	@JavascriptInterface
	public void goToShopping() {
		MZLog.d("JD_Reader", "WebViewBridge#goToShopping---mActivity:" + mActivity);
		Intent intent = new Intent(mActivity, LauncherActivity.class);
		intent.putExtra("TAB_INDEX", 0);
		intent.putExtra("BOOK_STORE_INDEX", 100);
		mActivity.startActivity(intent);
	}

	/**
	 * 
	 * @Title: goToMyJD
	 * @Description: 去我的京东
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年4月16日 下午3:57:55
	 */
	@JavascriptInterface
	public void goToMyJD() {
		MZLog.d("JD_Reader", "WebViewBridge#goToMyJD--mActivity:" + mActivity);
		Intent intent = new Intent(mActivity, LauncherActivity.class);
		intent.putExtra("TAB_INDEX", 3);
		mActivity.startActivity(intent);
	}

	/**
	 * 跳转到书架界面
	 */
	@JavascriptInterface
	public void goToBookShelf() {
		Intent intent = new Intent(mActivity, LauncherActivity.class);
		intent.putExtra("TAB_INDEX", 1);
		mActivity.startActivity(intent);
	}

	/**
	 * 
	 * @Title: quickDownload
	 * @Description: 立即下载
	 * @param @param jsonString
	 * @return void
	 * @throws
	 * @date 2015年4月16日 下午3:56:42
	 */
	@JavascriptInterface
	public void quickDownload(String jsonString) {
		MZLog.d("JD_Reader", "WebViewBridg#quickDownload:" + jsonString);
		String username = LoginUser.getInstance().getpin();
		String url = "openApp.jdebook://communication?params=";
		String temp = ",\"dlType\":\"1\",\"location\":\"11\",\"type\":\"32\",\"userName\":\"" + username + "\"";
		StringBuffer sb = new StringBuffer(url);
		sb.append(jsonString);
		sb.insert(sb.length() - 1, temp);

		url = sb.toString();
		Uri uri = Uri.parse(url);
		Intent i = new Intent(Intent.ACTION_VIEW, uri);

		String scheme = uri.getScheme();
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		i.setPackage("com.android.browser");
		// if (!scheme.equalsIgnoreCase("http")
		// && !scheme.equalsIgnoreCase("https")) {
		// if (!CommonUtil.isIntentAvailable(i)) {// 如果google浏览器无法处理，交给系统处理

		// i.setPackage(null);Bundle[{author=长投网, bookId=30201358, format=epub, orderId=11311079321, picUrl=http://img10.360buyimg.com/n2/jfs/t2095/275/99799501/130896/7c7c1cb4/55eeb281N620c2a1b.jpg, userName=t2902371, bigPicUrl=http://img10.360buyimg.com/n1/jfs/t2095/275/99799501/130896/7c7c1cb4/55eeb281N620c2a1b.jpg, isFreeBook=false, bookName=理财，一辈子的慢思考, bookSize=1.09, bookType=1}]
		// startActivity(i);1448871588035
		Intent intent = new Intent(InterfaceBroadcastReceiver.ACTION);
		final Command command = Command.createCommand(i);

		Bundle bundle = command.getBundle();
		intent.putExtras(bundle);
		final String key = DataIntent.creatKey();//
		DataIntent.put(key, command);
		if (NetWorkUtils.getNetworkConnectType(mActivity) == NetworkConnectType.MOBILE) {
			final Bundle outBundle = command.getOutBundle();
			String bookSize = outBundle.getString("bookSize");
			MZLog.d("J", bundle.toString());
			double size = Double.valueOf(bookSize);
			if (size >= 3) {
				DialogManager.showCommonDialog(mActivity, "提示", "您的WIFI未连接，是否继续下载？", "是", "否", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							download(key);
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							addBook2ShelfWithoutDownload(outBundle);
						default:
							break;
						}
						dialog.dismiss();
					}
				});
			} else {
				download(key);
			}
		} else {
			download(key);
		}

		// ExtraTaskOnLoginSuccess onLongSucce = new ExtraTaskOnLoginSuccess(
		// mActivity, intent, true);
		// onLongSucce.execute();

	}

	/**
	 * 
	 * @Title: download
	 * @Description: 立即下载
	 * @param @param key
	 * @return void
	 * @throws
	 * @date 2015年4月20日 下午8:20:15
	 */
	@JavascriptInterface
	private void download(final String key) {
		Intent mianIntent = new Intent(mActivity, MainActivity.class);
		mianIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mianIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		mianIntent.putExtra("task", false);
		mianIntent.putExtra("TAB_INDEX", 1);
		mianIntent.putExtra(KEY, key);// (KEY, command);
		mActivity.startActivity(mianIntent);
	}

	/**
	 * 
	 * @Title: goToOrder
	 * @Description: 去结算
	 * @param @param bookid
	 * @return void
	 * @throws
	 * @date 2015年4月16日 下午3:57:10
	 */
	@JavascriptInterface
	public void goToOrder(String bookid) {
		MZLog.d("JD_Reader", "WebViewBridge#goToOrder:" + bookid);
		// 以下添加购买逻辑
		if (OnlinePayActivity.payidList == null) {
			OnlinePayActivity.payidList = new ArrayList<String>();
		} else {
			OnlinePayActivity.payidList.clear();
		}

		OnlinePayActivity.payidList.add("" + bookid);

		OnLinePayTools.gotoEbookPay(mActivity, null);
	}

	/**
	 * 去我的积分
	 */
	@JavascriptInterface
	public void goToIngegrationIndex() {
		MZLog.d("JD_Reader", "WebViewBridge#goToIngegrationIndex");
		Intent intent = new Intent(mActivity, IntegrationActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mActivity.startActivity(intent);
	}

	/**
	 * 
	 * @Title: goToBookDetail
	 * @Description: 去图书详情
	 * @param @param bookid
	 * @return void
	 * @throws
	 * @date 2015年4月16日 下午3:57:29
	 */
	@JavascriptInterface
	public void goToBookDetail(String bookid) {
		MZLog.d("JD_Reader", "WebViewBridge#goToBookDetail" + bookid);

		Intent intent = new Intent(mActivity, BookInfoNewUIActivity.class);
		intent.putExtra("bookid", Long.parseLong(bookid));
		mActivity.startActivity(intent);
	}

	/**
	 * 
	 * @Title: weiboShare
	 * @Description: 微博分享本地方法
	 * @param title
	 *            ：文字内容
	 * @param imageUrl
	 *            ：图片URL
	 * @param linkUrl
	 *            ：链接地址
	 * @param defaluttext
	 * @return void
	 * @throws
	 */
	@JavascriptInterface
	public void weiboShare(String title, String imageUrl, String linkUrl) {
		MZLog.d("J", "WebViewBridge#weiboShare——>title::" + title + ",imageUrl::" + imageUrl + ",linkUrl::" + linkUrl);
		WBShareHelper.getInstance().doShare(mActivity, title, "", imageUrl, linkUrl, "", new ShareResultListener() {

			@Override
			public void onShareRusult(int resultType) {
				switch (resultType) {
				case ShareResultListener.SHARE_SUCCESS:
					Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
					shareToGetScore();
					break;
				case ShareResultListener.SHARE_CANCEL:
					Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

					break;
				case ShareResultListener.SHARE_FAILURE:
					Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
					break;
				}
			}
		});
	}

	/**
	 * 
	 * @Title: weixinShare
	 * @Description: 微信分享本地方法
	 * @param title
	 *            ：文字内容
	 * @param imageUrl
	 *            ：图片URL
	 * @param linkUrl
	 *            ：链接地址
	 * @param type
	 *            ：0 微信好友；1 朋友圈
	 * @return void
	 * @throws
	 */
	@JavascriptInterface
	public void weixinShare(String title, String imageUrl, String linkUrl, int type) {
		MZLog.d("J", "WebViewBridge#weixinShare——>title::" + title + ",imageUrl::" + imageUrl + ",linkUrl::" + linkUrl + ",type::" + type);
		// 分享链接不为空
		if (!TextUtils.isEmpty(linkUrl)) {
			WXShareHelper.getInstance().doShare(mActivity, title, "", imageUrl, linkUrl, type, new ShareResultListener() {

				@Override
				public void onShareRusult(int resultType) {
					switch (resultType) {
					case ShareResultListener.SHARE_SUCCESS:
						Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
						shareToGetScore();
						break;
					case ShareResultListener.SHARE_CANCEL:
						Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();

						break;
					case ShareResultListener.SHARE_FAILURE:
						Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
						break;
					}
				}
			});
		} else {
			if (!TextUtils.isEmpty(imageUrl)) {
				WXShareHelper.getInstance().shareImage(mActivity, imageUrl, type);
			}
		}
	}

	// 分享成功赠送积分
	private void shareToGetScore() {
		IntegrationAPI.shareGetScore(mActivity, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
//				String content = "分享成功，恭喜您获得" + score + "积分";
//				CustomToast.showToast(mActivity, content);
				String scoreInfo = "分享成功，恭喜您获得"+score.getGetScore()+"积分";
				SpannableString span = new SpannableString(scoreInfo);
				int start1 = 10;
				int end1 = start1 + String.valueOf(score.getGetScore()).length();
				span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				CustomToast.showToast(mActivity, span);
			}

			@Override
			public void onGrandFail() {
			}
		});
	}

	/**
	 * 
	 * @Title: addBook2ShelfWithoutDownload
	 * @param @param bundle
	 * @return void
	 * @throws
	 * @date 2015年4月20日 下午8:09:14
	 */
	private void addBook2ShelfWithoutDownload(Bundle bundle) {
		LocalBook localBook = new LocalBook();
		localBook.book_id = bundle.getLong("bookId");
		localBook.bigImageUrl = bundle.getString("bigPicUrl");
		localBook.smallImageUrl = bundle.getString("picUrl");
		Log.i("zhoubo", "localBook.bigImageUrl==" + localBook.bigImageUrl);
		localBook.type_id = bundle.getInt("bookType");
		Log.i("DownloadTool", "localBook.type_id" + localBook.type_id);
		localBook.order_code = String.valueOf(bundle.getLong("orderId"));
		localBook.title = bundle.getString("bookName");
		localBook.author = bundle.getString("author");
		localBook.state = LocalBook.STATE_LOAD_PAUSED;
		localBook.userName = LoginUser.getpin();
		localBook.add_time = System.currentTimeMillis();
		localBook.mod_time = System.currentTimeMillis();
		// localBook.borrowEndTime = order.borrowEndTime;
		MZLog.d("wangguodong", "借阅书籍保存时间====：" + localBook.borrowEndTime);
		localBook.source = LocalBook.SOURCE_BUYED_BOOK;
		String formatName = bundle.getString("format");
		if (!TextUtils.isEmpty(formatName)) {
			if (formatName.equals(LocalBook.FORMATNAME_PDF)) {
				localBook.format = LocalBook.FORMAT_PDF;
				localBook.formatName = LocalBook.FORMATNAME_PDF;
			}
			if (formatName.equals(LocalBook.FORMATNAME_EPUB)) {
				localBook.format = LocalBook.FORMAT_EPUB;
				localBook.formatName = LocalBook.FORMATNAME_EPUB;
			}
		} else {
			Toast.makeText(mActivity, "下载参数有误！", Toast.LENGTH_SHORT).show();
			MZLog.d("wangguodong", "boot55555555555");
			return;
		}

		boolean isSuccuss = false;
		boolean isave = localBook.save();

		if (isave) {
			isSuccuss = true;
			// 更新书架表
			MZLog.d("wangguodong", "保存下载ebook到书架");
			MZBookDatabase.instance.savaJDEbookToBookShelf(localBook.book_id, String.valueOf(localBook.add_time), localBook.userName);

		}
		MZLog.d("wangguodong", "已经添加到书架");
		String message = null;
		if (isSuccuss) {
			message = "已放入书架，您可在网络环境好时，点击继续下载";
		} else {
			message = "加入书架失败";
		}
		Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
		sendFinishBroadCast(mActivity);

		// 跳转到书架
		Intent intent = new Intent(mActivity, LauncherActivity.class);
		intent.putExtra("TAB_INDEX", 1);
		mActivity.startActivity(intent);
	}

	@JavascriptInterface
	public void sendFinishBroadCast(Context context) {
		Intent intent = new Intent("com.mzread.action.downloaded");
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	
	@JavascriptInterface
	public void openBrowser(String url) {
		try {
			Uri uri = Uri.parse(url);
			if (uri != null && !uri.isRelative()) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
				mActivity.startActivity(browserIntent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 社会化分享方法
	 * @param title
	 * @param imageUrl
	 * @param linkUrl
	 * @param shareAccess 分享渠道：0为微信好友，1为微信朋友圈，2为微博，3为短信
	 * @param shareType 分享类型：sendbook为购书送书
	 */
	@JavascriptInterface
	public void socialShare(String title, String imageUrl, String linkUrl, int shareAccess,String shareType,final String remark) {
		final String type = shareType;
		Resources res=mActivity.getResources();
		Bitmap bitmap=BitmapFactory.decodeResource(res, R.drawable.sendbook_share_icon);
		switch (shareAccess) {
		case 0:// 微信好友
		case 1:// 微信朋友圈
			
			if (!TextUtils.isEmpty(linkUrl)) {
				String shareTitleText="",shareDescriptionText="";
				if(shareAccess == 0){
					shareTitleText = "我买了本好书送给你，快来戳我领取吧！";
					shareDescriptionText = "红包已经out啦，我买了本好书送给你，你猜是哪本？快来戳我查看并领取吧！";
				}else{
					shareTitleText = "拼手速，抢好书！我在京东阅读买了本好书，谁先抢到就归谁！";
					shareDescriptionText = "";
				}
				
				WXShareHelper.getInstance().doShare(mActivity, shareTitleText, shareDescriptionText, bitmap, linkUrl, shareAccess,
						new ShareResultListener() {
							@Override
							public void onShareRusult(int resultType) {
								switch (resultType) {
								case ShareResultListener.SHARE_SUCCESS:
									//TODO 购书送书的分享(暂时不做)
									if(type!=null && type.equalsIgnoreCase("sendbook")){
										
									}else{
										shareToGetScore();
									}
									Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
									break;
								case ShareResultListener.SHARE_CANCEL:
									if(type!=null && type.equalsIgnoreCase("sendbook")){
										rollBackSendBookStatus(remark);
									}
									Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();
									break;
								case ShareResultListener.SHARE_FAILURE:
									if(type!=null && type.equalsIgnoreCase("sendbook")){
										rollBackSendBookStatus(remark);
									}
									Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
									break;
								}
							}
						});
			} else {
				if (!TextUtils.isEmpty(imageUrl)) {
					WXShareHelper.getInstance().shareImage(mActivity, imageUrl, shareAccess);
				}
			}
			break;
		case 2:// 微博
			String shareTitleText = "红包已经out啦！我买了本好书送给你，你猜是哪本？快来戳我查看并领取吧！";
			String shareDescriptionText = "";
			
			WBShareHelper.getInstance().doShare(mActivity, shareTitleText, "", imageUrl, linkUrl, "", new ShareResultListener() {

				@Override
				public void onShareRusult(int resultType) {
					switch (resultType) {
					case ShareResultListener.SHARE_SUCCESS:
						if(type!=null && type.equalsIgnoreCase("sendbook")){
							
						}else{
							shareToGetScore();
						}
						Toast.makeText(mActivity, "分享成功", Toast.LENGTH_LONG).show();
						break;
					case ShareResultListener.SHARE_CANCEL:
						if(type!=null && type.equalsIgnoreCase("sendbook")){
							rollBackSendBookStatus(remark);
						}
						Toast.makeText(mActivity, "分享取消", Toast.LENGTH_LONG).show();
						break;
					case ShareResultListener.SHARE_FAILURE:
						if(type!=null && type.equalsIgnoreCase("sendbook")){
							rollBackSendBookStatus(remark);
						}
						Toast.makeText(mActivity, "分享失败", Toast.LENGTH_LONG).show();
						break;
					}
				}
			});
			break;
		case 3:// 短信
			if(title == null)
				title ="";
			String shareText ="红包已经out啦，我在京东阅读买了本电子书《"+title+"》送给你，戳我领取就可以免费阅读啦："+linkUrl+"。我买你读就是这么任性！";
		    Uri smsToUri = Uri.parse("smsto:");  
		    Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);  
		    intent.putExtra("sms_body", shareText);  
		    mActivity.startActivity(intent);  
		default:
			break;
		}
	}
	
	/**
	 * 送书状态回滚（若赠书分享取消或者失败则调用此方法）
	 */
	private void rollBackSendBookStatus(String remark){
		if (!NetWorkUtils.isNetworkConnected(mActivity)) {
			return ;
		}
		if(TextUtils.isEmpty(remark))
			return ;
		String[] arr = remark.split("_");
		if(arr == null || arr.length < 3)
			return ;
		String orderId = arr[0];
		String ebookId = arr[1];
		String t = arr[2];
		
		
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getRollBackSentStatusParams(orderId,ebookId,t), true,
				new MyAsyncHttpResponseHandler(mActivity) {
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					}
				});
	}
	
	/**
	 * 去我的订单列表页
	 */
	@JavascriptInterface
	public void gotoMyOrderList() {
		if(mActivity.isFinishing())
			return ;
		Intent intent2 = new Intent(mActivity, OrderActivity.class);
		intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mActivity.startActivity(intent2);
		mActivity.finish();
	}
	
}
