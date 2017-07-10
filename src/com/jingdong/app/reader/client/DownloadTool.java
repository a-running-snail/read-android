package com.jingdong.app.reader.client;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.sendbook.SendBookReceiveInfo;
import com.jingdong.app.reader.bookstore.sendbook.SendBookReceiveInfo.SendBookReceiveInfos;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.config.ITransKey;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.BootEntity;
import com.jingdong.app.reader.entity.GiftBookInfor;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.ReturnStatus;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.extension.jpush.JDMessageReceiver.JDMessage;
import com.jingdong.app.reader.extension.jpush.JDMessageReceiver.JDMessages;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.NetWorkUtils.NetworkConnectType;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.jingdong.app.reader.util.StreamToolBox;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

public class DownloadTool implements ITransKey {

	// private static DownloadConfirmListener downloadConfirmListener;
	private static Hashtable<String,String> sources = new Hashtable<String,String>();
//	private static DownloadConfirmListener downloadConfirmListener;
	public interface DownloadConfirmListener{
		public void onConfirm();

		public void onCancel();
	}

	/**
	 * 
	 * @param isExtraTask
	 * @Title: download
	 * @param @param activity
	 * @param @param order
	 * @param @param giftBookInfor
	 * @param @param jumpToLocalBook
	 * @param @param source
	 * @param @param channelID
	 * @param @param tryGetContentFromSd
	 * @param @param confirmListener :
	 *        移动流量下载超3M弹窗确认的监听，有些界面的UI需要点击确认后修改（比如下载按钮）；如果不需要，传null即可
	 * @return void
	 * @throws
	 * @date 2015年4月1日 下午2:24:09
	 */
	@SuppressWarnings("unused")
	public static void download(final Activity activity,
			final OrderEntity order, 
			final GiftBookInfor giftBookInfor,
			final boolean jumpToLocalBook, 
			final String source,
			final int channelID, 
			final boolean tryGetContentFromSd,
			final DownloadConfirmListener confirmListener, 
			boolean isExtraTask) {

		//判断网络连接是否可用
		if (!NetWorkUtils.isNetworkAvailable(activity)) {
			Toast.makeText(activity, "网络不可用", Toast.LENGTH_LONG).show();
			return;
		}
		
		//若为手机移动网络，若超过3M则提示是否继续下载
		if (NetWorkUtils.getNetworkConnectType(activity) == NetworkConnectType.MOBILE) {
			// String title= "图书大小为" +order.size +"M" ;
			if (order == null) {
				return;
			}
			
			//文件大小
			if (order.book_size == null || TextUtils.isEmpty(order.book_size)) {
				executeDownload(activity, order, giftBookInfor,jumpToLocalBook, source, channelID, tryGetContentFromSd);
				return;
			}
			
			if (isExtraTask) {   
				executeDownload(activity, order, giftBookInfor,jumpToLocalBook, source, channelID, tryGetContentFromSd);
				return;
			}
			
			String s = order.book_size.substring(0,order.book_size.length() - 1);
			double size = Double.valueOf(s);
			
			String validDeviceUUIDByInstant = StatisticsReportUtil.getValidDeviceUUIDByInstant();
			if (validDeviceUUIDByInstant == null || TextUtils.isEmpty(validDeviceUUIDByInstant)) {
				ToastUtil.showToastInThread("设备受限", Toast.LENGTH_LONG);
			}
			
			if (size < 3) {//没有超过3M
				executeDownload(activity, order, giftBookInfor,jumpToLocalBook, source, channelID, tryGetContentFromSd);
			} else {//超过3M
				DialogManager.showCommonDialog(activity, "提示","您的WIFI未连接，是否继续下载？", "是", "否",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								switch (which) {
									case DialogInterface.BUTTON_POSITIVE:
										if (confirmListener != null) {
											confirmListener.onConfirm();
										}
										executeDownload(activity, order,
												giftBookInfor, jumpToLocalBook,
												source, channelID,
												tryGetContentFromSd);
										break;
									case DialogInterface.BUTTON_NEGATIVE:
										if (confirmListener != null) {
											confirmListener.onCancel();
										}
	
										addEBook2ShelfWithoutDownload(activity, order,
												giftBookInfor, source);
	
										break;
									default:
										break;
								}
								
							}

							
						});
			}
		} else {
			executeDownload(activity, order, giftBookInfor, jumpToLocalBook,source, channelID, tryGetContentFromSd);
		}

	}

	/**
	 * 执行下载
	 * @param activity
	 * @param order
	 * @param giftBookInfor
	 * @param jumpToLocalBook
	 * @param source
	 * @param channelID
	 * @param tryGetContentFromSd
	 */
	private static void executeDownload(final Activity activity,
			final OrderEntity order, final GiftBookInfor giftBookInfor,
			final boolean jumpToLocalBook, final String source,
			final int channelID, final boolean tryGetContentFromSd) {
		//获取赠书赠言
		getSendbookReceiveInfo(activity,order,source);
		
		if (order.orderId == -1) {//下载免费书
			Runnable success = new Runnable() {
				@Override
				public void run() {
					downloadBoot(activity, order, giftBookInfor,jumpToLocalBook, source, channelID,tryGetContentFromSd);
				}
			};

			Runnable failed = new Runnable() {
				@Override
				public void run() {
				}
			};
			uploadDownRecord(activity, order.bookId, success, failed);
		} else {
			MZLog.d("wangguodong", "下载引导文件...");
			downloadBoot(activity, order, giftBookInfor, jumpToLocalBook,source, channelID, tryGetContentFromSd);
		}
	}

	/**
	 * 下载document
	 * 
	 * @param activity
	 * @param document
	 * @param tryGetContentFromSd
	 */
	public static void downloadDocument(final Activity activity,
			final LocalDocument document, final boolean tryGetContentFromSd,
			final DownloadConfirmListener listener) {

		if (!NetWorkUtils.isNetworkAvailable(activity)) {
			Toast.makeText(activity, "网络不可用", Toast.LENGTH_LONG).show();
			return;
		}
		if (StatisticsReportUtil.getValidDeviceUUIDByInstant() == null
				|| TextUtils.isEmpty(StatisticsReportUtil
						.getValidDeviceUUIDByInstant())) {
			ToastUtil.showToastInThread("设备受限", Toast.LENGTH_LONG);
		}
		if (NetWorkUtils.getNetworkConnectType(activity) == NetworkConnectType.MOBILE) {
			MZLog.d("JD_Reader", "book size:" + document.size + "");
			// 小于3M不提示
			if (document.size < 3 * 1024 * 1024) {
				executeDownloadDoc(activity, document, tryGetContentFromSd);
			} else {
				// 大于等于3M弹窗提示
				DialogManager.showCommonDialog(activity, "提示",
						"您的WIFI未连接，是否继续下载？", "是", "否",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									if (listener != null) {
										listener.onConfirm();
									}
									executeDownloadDoc(activity, document,
											tryGetContentFromSd);
									break;
								case DialogInterface.BUTTON_NEGATIVE:
									if (listener != null) {
										listener.onCancel();
									}
									
									document.state = LocalDocument.STATE_LOAD_PAUSED;
									if (document.save()) {
										MZBookDatabase.instance.saveToBookShelf(document._id, System.currentTimeMillis(), 1, LoginUser.getpin());
										sendFinishBroadCast(activity);
										String message =  "已放入书架，您可在网络环境好时，点击继续下载";
										Toast.makeText(activity, message,
												Toast.LENGTH_LONG).show();
//										//跳转到书架
//										Intent intent = new Intent(activity, LauncherActivity.class);
//										intent.putExtra("TAB_INDEX", 1);
//										activity.startActivity(intent);
									}
									break;
								default:
									break;
								}
								dialog.dismiss();
							}
						});
			}
		} else {
			executeDownloadDoc(activity, document, tryGetContentFromSd);
		}
		// DialogManager. showCommonDialog(activity, "提示", "您的WIFI未连接，是否继续下载？" ,
		// "是" , "否" , new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// switch (which) {
		// case DialogInterface.BUTTON_POSITIVE :
		// if (listener!= null) {
		// listener.onConfirm();
		// }
		// executeDownloadDoc(activity, document, tryGetContentFromSd);
		// break ;
		// case DialogInterface.BUTTON_NEGATIVE :
		// if (listener!= null) {
		// listener.onCancel();
		// }
		//
		// break ;
		// default :
		// break ;
		// }
		// dialog.dismiss();
		// }
		// });
		// } else {
		// executeDownloadDoc(activity, document, tryGetContentFromSd);
		//
		// }

	}
	
	/**
	 * 
	 * @Title: addBook2ShelfWithoutDownload
	 * @param @param activity
	 * @param @param order
	 * @param @param giftBookInfor
	 * @param @param source
	 * @return void
	 * @throws
	 * @date 2015年4月21日 上午11:27:30
	 */
	private static void addEBook2ShelfWithoutDownload(
			final Activity activity,
			final OrderEntity order,
			final GiftBookInfor giftBookInfor,
			final String source) {
		LocalBook localBook = new LocalBook();
		localBook.book_id = order.bookId;
		localBook.bigImageUrl = order.bigPicUrl;
		localBook.smallImageUrl = order.picUrl;
		Log.i("zhoubo", "localBook.bigImageUrl=="
				+ localBook.bigImageUrl);
		localBook.type_id = order.bookType;
		Log.i("DownloadTool", "localBook.type_id"
				+ localBook.type_id);

		localBook.order_code = String
				.valueOf(order.orderId);
		localBook.title = order.name;
		localBook.author = order.author;
		localBook.state = LocalBook.STATE_LOAD_PAUSED;
		localBook.userName = LoginUser.getpin();
		localBook.add_time = System
				.currentTimeMillis();
		localBook.mod_time = System
				.currentTimeMillis();
		localBook.borrowEndTime = order.borrowEndTime;
		MZLog.d("wangguodong", "借阅书籍保存时间====："
				+ localBook.borrowEndTime);
		if (giftBookInfor != null)
			localBook.giftBookInfor = giftBookInfor;
		localBook.source = source;
		if (localBook.source == LocalBook.SOURCE_TRYREAD_BOOK) {
			localBook.bookUrl = order.tryDownLoadUrl;
		}
		if (!TextUtils.isEmpty(order.formatName)) {
			if (order.formatName
					.equals(LocalBook.FORMATNAME_PDF)) {
				localBook.format = LocalBook.FORMAT_PDF;
				localBook.formatName = LocalBook.FORMATNAME_PDF;
			}
			if (order.formatName
					.equals(LocalBook.FORMATNAME_EPUB)) {
				localBook.format = LocalBook.FORMAT_EPUB;
				localBook.formatName = LocalBook.FORMATNAME_EPUB;
			}
		} else if (order.format != -1) {
			if (order.format == LocalBook.FORMAT_PDF) {
				localBook.format = LocalBook.FORMAT_PDF;
				localBook.formatName = LocalBook.FORMATNAME_PDF;
			} else if (order.format == LocalBook.FORMAT_EPUB) {
				localBook.format = LocalBook.FORMAT_EPUB;
				localBook.formatName = LocalBook.FORMATNAME_EPUB;
			}
		} else {
			Toast.makeText(activity, "下载参数有误！",
					Toast.LENGTH_SHORT).show();
			MZLog.d("wangguodong",
					"boot55555555555");
			return;
		}

		boolean isSuccuss = false;
		boolean isave = localBook.save();

		if (isave) {
			isSuccuss = true;
			// 更新书架表
			MZLog.d("wangguodong", "保存下载ebook到书架");
			MZBookDatabase.instance
					.savaJDEbookToBookShelf(
							localBook.book_id,
							String.valueOf(localBook.add_time),
							localBook.userName);

		}
		MZLog.d("wangguodong", "已经添加到书架");
		String message = null;
		if (isSuccuss) {
			message = "已放入书架，您可在网络环境好时，点击继续下载";
		} else {
			message = "加入书架失败";
		}
		Toast.makeText(activity, message,
				Toast.LENGTH_LONG).show();
		sendFinishBroadCast(activity);
	}

	private static void executeDownloadDoc(Activity activity,
			LocalDocument document, boolean tryGetContentFromSd) {
		document.add_time = System.currentTimeMillis();
		int index = document.saveDocument(document);
		if (index > -1) {
			document._id = index;
			// 更新书架表
			MZLog.d("wangguodong", "保存下载document到书架+document_id" + index);
			MZBookDatabase.instance.saveToBookShelf(document._id,
					document.add_time, 1, LoginUser.getpin());
			MZLog.d("wangguodong", "开始下载Documnet数据了。。。。");
			Intent intent = new Intent(activity, DownloadService.class);
			intent.putExtra("type", document.TYPE_DOCUMENT);
			intent.putExtra(KEY, Long.parseLong(document._id + ""));
			intent.putExtra(KEY2, tryGetContentFromSd);// 尝试本地sd中是否存在正在下载的文件。
			activity.startService(intent);
		} else {
			MZLog.d("wangguodong", "下载数据出错");
		}
		sendFinishBroadCast(activity);
	}

	/**
	 * isonline 标示为是否为在线畅读的书籍。
	 * 
	 * @param activity
	 * @param order
	 * @param giftBookInfor
	 * @param jumpToLocalBook
	 * @param LocalBook
	 *            localBook 1.2.0后改为都跳到我的书架为true。1.2.3后又改为false。都不跳。
	 * @param source
	 *            图书类型
	 */
	public static void downBook(final Activity activity,
			final LocalBook localBook, final GiftBookInfor giftBookInfor,
			final boolean jumpToLocalBook, final String source,
			final int channelID, DownloadConfirmListener confirmListener) {

		OrderEntity order = new OrderEntity();
		order.bookId = localBook.book_id;
		order.bigPicUrl = localBook.bigImageUrl;
		order.picUrl = localBook.smallImageUrl;
		order.orderId = Long.valueOf(localBook.order_code);
		order.name = localBook.title;
		order.author = localBook.author;
		order.bookType = localBook.type_id;
		order.format = localBook.format;
		DownloadTool.downBook(activity, order, giftBookInfor, jumpToLocalBook,
				localBook.source, 0, confirmListener, false);// InterfaceBroadcastReceiver.CHANNEL_ID_DEFAULT
	}

	/**
	 * isonline 标示为是否为在线畅读的书籍。
	 * 
	 * @param activity
	 * @param order
	 * @param giftBookInfor
	 * @param jumpToLocalBook
	 *            1.2.0后改为都跳到我的书架为true。1.2.3后又改为false。都不跳。
	 * @param source
	 *            图书类型
	 * @param isExtraTask
	 */
	@SuppressWarnings("unused")
	public static void downBook(final Activity activity,
			final OrderEntity order, final GiftBookInfor giftBookInfor,
			final boolean jumpToLocalBook, final String source,
			final int channelID, final DownloadConfirmListener confirmListener,
			final boolean isExtraTask) {
		if (order.bookType != LocalBook.TYPE_M
				&& order.bookType != LocalBook.TYPE_EBOOK) {
			Toast.makeText(activity, "该图书类型设备不支持！", Toast.LENGTH_SHORT).show();
			return;
		}
		MZLog.d("wangguodong", "$$$$$$$book_id:" + order.bookId
				+ "$$$$$$$orderId:" + order.orderId);
		final LocalBook localBook = LocalBook.getLocalBook(order.bookId, LoginUser.getpin());
		if (localBook != null) {
			if (!TextUtils.isEmpty(localBook.source)
					&& localBook.source.equals(LocalBook.SOURCE_BULIT_IN)) {
				localBook.del(true);
			} else if(localBook.source.equals(LocalBook.SOURCE_ONLINE_BOOK) && source.equals(LocalBook.SOURCE_BUYED_BOOK)){
				localBook.source = source;
				localBook.save();
				return ;
			}
			else {
				Log.i("wangguodong", "书已存在本地");
				// 修复畅读在下载状态未修改问题
				localBook.source = source;
			} 
		}

		FileGuider fileGuider = new FileGuider(FileGuider.SPACE_PRIORITY_EXTERNAL);
		fileGuider.setChildDirName("/epub/" + order.bookId);
		try {
			//File file = new File(fileGuider.getParentPath() + "/"
			//		+ order.bookId + ".JEB");
			if (false) {//don't user local file, cannot make sure it's right version.
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						activity);
				builder.setTitle("下载确认");
				builder.setMessage("本地已存在《" + order.name + "》，是否直接加载到书架！");
				builder.setPositiveButton("直接加载", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						download(activity, order, giftBookInfor,jumpToLocalBook, source, channelID, true,confirmListener, isExtraTask);
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("重新下载", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						download(activity, order, giftBookInfor,jumpToLocalBook, source, channelID, false,confirmListener, isExtraTask);
						dialog.dismiss();
					}
				});
				builder.setCancelable(true);
				builder.show();
			} else {
				MZLog.d("wangguodong", "开始直接下载书籍...");
				download(activity, order, giftBookInfor, jumpToLocalBook,source, channelID, false, confirmListener, isExtraTask);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static OrderEntity creatOrderByBook(BookInforEDetail book,long orderId) {
		OrderEntity order = new OrderEntity();// 下载实体
		order.bookId = book.bookid;// bookid
		order.picUrl = book.picUrl;// 小图
		order.bigPicUrl = book.largeSizeImgUrl;// 大图
		order.bookType = book.bookType;//
		order.author = book.author;// 作者
		order.name = book.bookName;// 书名
		order.orderId = orderId;// 订单id
		order.formatName = book.formatName;// 图书格式。
		order.book_size = book.size + "M";// 图书大小

		if (orderId == 0) {
			// 判断为免费书 ， 这里有错，免费书订单号应该为-1
			if (/*
				 * order.picUrl.startsWith("http://") &&
				 * order.bigPicUrl.startsWith("http://") &&
				 */order.bookId > 0) {
				return order;
			}
		}
		if (/*
			 * order.picUrl.startsWith("http://") &&
			 * order.bigPicUrl.startsWith("http://") &&
			 */order.bookId > 0 && !TextUtils.isEmpty(order.name)
				&& order.orderId > 0) {
			if (MZBookApplication.DEBUG)
				Log.d("yfxiawei MSG Error", order.bookId + "|" + order.picUrl
						+ "|" + order.bigPicUrl + "|" + order.bookType + "|"
						+ order.author + "|" + order.name + "|" + order.orderId);
			return order;
		} else {
			return null;
		}
	}

	// 免费书
	public static OrderEntity creatOrderByBook(BookInforEDetail book) {
		OrderEntity order = new OrderEntity();// 下载实体
		order.bookId = book.bookid;// bookid
		order.picUrl = book.picUrl;// 小图
		order.bigPicUrl = book.largeSizeImgUrl;// 大图
		order.bookType = book.bookType;// 书的类型:电子书or多媒体书
		order.formatName = book.formatName;// 图书格式。
		order.author = book.author;// 作者
		order.name = book.bookName;// 书名
		if (book.isFree) {
			order.orderId = -1;// 订单id
		} else if (book.canReadOnline) {// 如果为畅读则用bookid作为orderId.
			order.orderId = book.bookid;
		}
		return order;
	}

	/**
	 * 保存图书信息到数据库&书架&启动服务器去下载文件
	 * @param activity 当前的Activity
	 * @param order 订单信息实体
	 * @param source 来源（试读、已购、畅读等）
	 * @param boot 引导文件
	 * @param giftBookInfor
	 * @param tryGetContentFromSd
	 */
	private static void saveLocalbook(final Activity activity,
			OrderEntity order, String source, BootEntity boot,
			GiftBookInfor giftBookInfor, boolean tryGetContentFromSd) {
		System.out.println("DDDDDDDD===saveLocalbook=====11111========");
		final LocalBook localBook = new LocalBook();
		//图书Id
		localBook.book_id = order.bookId;
		//图书封面
		localBook.bigImageUrl = order.bigPicUrl;
		localBook.smallImageUrl = order.picUrl;
		if (boot != null)
			localBook.boot = boot;//Rights引导文件内容
		localBook.type_id = order.bookType;
		//订单号
		localBook.order_code = String.valueOf(order.orderId);
		localBook.title = order.name;
		localBook.author = order.author;
		localBook.state = LocalBook.STATE_LOAD_PAUSED;
		localBook.userName = LoginUser.getpin();
		localBook.add_time = System.currentTimeMillis();
		localBook.mod_time = System.currentTimeMillis();
		localBook.borrowEndTime = order.borrowEndTime;
		if (source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK)) {
			localBook.borrowEndTime = order.userBuyBorrowEndTime;
		}
		
		if (giftBookInfor != null)
			localBook.giftBookInfor = giftBookInfor;

		//来源
		if (source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
			localBook.source = LocalBook.SOURCE_ONLINE_BOOK;
		} else if (source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
			localBook.source = LocalBook.SOURCE_BUYED_BOOK;
		} else if (source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
			localBook.source = LocalBook.SOURCE_TRYREAD_BOOK;
			localBook.bookUrl = order.tryDownLoadUrl;
		} else if (source.equals(LocalBook.SOURCE_BORROWED_BOOK)) {
			localBook.source = LocalBook.SOURCE_BORROWED_BOOK;
		}else if (source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK)) {
			localBook.source = LocalBook.SOURCE_USER_BORROWED_BOOK;
		}
		else {
			localBook.source = LocalBook.SOURCE_BUYED_BOOK;
		}

		//图书文件格式
		if (!TextUtils.isEmpty(order.formatName)) {
			if (order.formatName.equals(LocalBook.FORMATNAME_PDF)) {
				localBook.format = LocalBook.FORMAT_PDF;
				localBook.formatName = LocalBook.FORMATNAME_PDF;
			}
			if (order.formatName.equals(LocalBook.FORMATNAME_EPUB)) {
				localBook.format = LocalBook.FORMAT_EPUB;
				localBook.formatName = LocalBook.FORMATNAME_EPUB;
			}
		} else if (order.format != -1) {
			if (order.format == LocalBook.FORMAT_PDF) {
				localBook.format = LocalBook.FORMAT_PDF;
				localBook.formatName = LocalBook.FORMATNAME_PDF;
			} else if (order.format == LocalBook.FORMAT_EPUB) {
				localBook.format = LocalBook.FORMAT_EPUB;
				localBook.formatName = LocalBook.FORMATNAME_EPUB;
			}
		} else {
			Toast.makeText(activity, "下载参数有误！", Toast.LENGTH_SHORT).show();
			return;
		}

		boolean isSuccuss = false;
		boolean isave = localBook.save();
		boolean file_error = SettingUtils.getInstance().getBoolean("file_error:" + localBook.book_id, false);
		boolean isUpdate = SettingUtils.getInstance().getBoolean("" +localBook.book_id);
		boolean isBuyed = SettingUtils.getInstance().getBoolean("Buyed:" + localBook.book_id, false);
		
		if (isave) {
			isSuccuss = true;
			// 更新书架表
			MZLog.d("wangguodong", "保存下载ebook到书架");
			String change_time = String.valueOf(System.currentTimeMillis());
			long time = SettingUtils.getInstance().getLong("change_time" + localBook.book_id, 0);
			if(time > 0) {
				change_time = String.valueOf(time);
				if(!file_error && !isUpdate && !isBuyed) {
					change_time = String.valueOf(localBook.add_time);
				}
			}
			//保存图书信息到数据库书架表
			MZBookDatabase.instance.savaJDEbookToBookShelf(localBook.book_id,change_time, localBook.userName);
		}
		MZLog.d("wangguodong", "已经添加到书架");
		String message = null;
		if (isSuccuss) {
			message = "本书已添加到书架！";
		} else {
			message = "下载失败";
		}
		
		if((!isUpdate && !isBuyed) || !isSuccuss) {
			Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
		}
		
		//通知书架页面更新内容
		sendFinishBroadCast(activity);

		MZLog.d("wangguodong", "%%%%%%%%%发送书架更新广播");

		if (isSuccuss) {
			// 下载，将同步该本书。
			MZLog.d("wangguodong", "开始下载数据了。。。。");
			Intent intent = new Intent(activity, DownloadService.class);
			intent.putExtra(KEY, localBook.book_id);
			intent.putExtra(KEY2, tryGetContentFromSd);// 尝试本地sd中是否存在正在下载的文件。
			activity.startService(intent);
		}
	}

	public static void sendFinishBroadCast(Context context) {
		Intent intent = new Intent("com.mzread.action.downloaded");
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/**
	 * 下载Rigthts引导文件
	 * @param activity
	 * @param order
	 * @param giftBookInfor
	 * @param jumpToLocalBook
	 * @param source
	 * @param channelID
	 * @param tryGetContentFromSd
	 */
	static void downloadBoot(final Activity activity,
			final OrderEntity order, final GiftBookInfor giftBookInfor,
			final boolean jumpToLocalBook, final String source,
			final int channelID, final boolean tryGetContentFromSd) {
		
		if (source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
			MZLog.d("wangguodong", "试读书籍直接下载...");
			saveLocalbook(activity, order, source, null, null,tryGetContentFromSd);
			MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), order.bookId, source);
		} else {
			MZLog.d("wangguodong", "pin--->" + LoginUser.getpin());
			String url = "http://" + Configuration.getProperty(Configuration.DBOOK_HOST).trim() + "/client.action";
			MZLog.d("wangguodong", "引导文件url--->" + url);
			
			boolean isborrow = source.equals(LocalBook.SOURCE_BORROWED_BOOK) ? true : false;
			boolean isBorrowBuy = source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK) ? true : false;
			
			//发请求获取引导证书
			WebRequestHelper.post(url, RequestParamsPool.getBookverifyParams(
					LoginUser.getpin(), "" + order.bookId, "" + order.orderId,
					isborrow, isBorrowBuy), true, new MyAsyncHttpResponseHandler(activity) {

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2,Throwable arg3) {
					Toast.makeText(activity, "下载失败", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onResponse(int statusCode, Header[] headers,
						byte[] responseBody) {
					String string = new String(responseBody);
					MZLog.d("wangguodong", "下载引导文件结果：" + string);
					BootEntity boot = null;
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						byte[] bytes = responseBody;
						String xml = StreamToolBox.loadStringFromStream(StreamToolBox.getByteArrayInputStream(bytes));
						Document dom = builder.parse(StreamToolBox.getByteArrayInputStream(bytes),"UTF-8");
						Element root = dom.getDocumentElement();
						try {
							boot = BootEntity.parser(root);
							MZLog.d("wangguodong", "引导文件下载成功");
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						if (boot == null) {
							ReturnStatus returnStatus = DataParser.parserReturnStatus(xml);
							MZLog.d("wangguodong", "download code------>"+ returnStatus.code);
							String message = returnStatus.massage;
							if (TextUtils.isEmpty(message)) {
								message = "下载失败";
							}
							if (returnStatus.code == 107) {
								Toast.makeText(activity, "未登录",Toast.LENGTH_SHORT).show();
								return;
							}
							MZLog.d("wangguodong", "没有登录");
							Toast.makeText(activity, message,Toast.LENGTH_SHORT).show();
						} else {
							MZLog.d("wangguodong", "boot文件木有问题");
							saveLocalbook(activity, order, source, boot,giftBookInfor, tryGetContentFromSd);
							MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), order.bookId, source);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public static void uploadDownRecord(final Activity activity,
			final long bookId, final Runnable success, final Runnable failed) {

		String url = "http://"
				+ Configuration.getProperty(Configuration.HOST).trim()
				+ "/downrecord/downrecord_insert.action";

		WebRequestHelper.get(url,
				RequestParamsPool.getUploadDownRecordParams(bookId),
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						try {
							JSONObject jsonObject = new JSONObject(new String(
									arg2));
							MZLog.d("wangguodong", new String(arg2));
							String code = DataParser.getString(jsonObject,
									"code");
							// 如果成功了code为1，失败code为0 如此其他的接口都不一样，设计的接口太烂
							if (code.equals("0")) {
								String message = DataParser.getString(
										jsonObject, "message");

								if (!LoginUser.isLogin()) {

									Toast.makeText(activity, "您还木有登录哦",
											Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(
											MZBookApplication.getInstance(),
											"下载失败!", Toast.LENGTH_SHORT).show();
								}
								// }
								failed.run();
							} else if (code.equals("1")) {
								success.run();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						failed.run();
					}
				});

	}

	public interface RefreshAfterDownload {
		public void refreshDownloadStatus(OrderEntity bookEntity,
				boolean alreadyDownload);
	}

	public static void startDownload(long localBookId,
			boolean tryGetContentFromSd) {
		Intent intent = new Intent(MZBookApplication.getInstance(),
				DownloadService.class);
		intent.putExtra(KEY, localBookId);
		intent.putExtra(KEY2, tryGetContentFromSd);// 尝试本地sd中是否存在正在下载的文件。
		MZBookApplication.getInstance().startService(intent);
	}
	
	public static boolean sourceEquals(String source_down, String source_now) {
		sources.put(LocalBook.SOURCE_TRYREAD_BOOK, "0");
		sources.put(LocalBook.SOURCE_BORROWED_BOOK, "1");
		sources.put(LocalBook.SOURCE_ONLINE_BOOK, "1");
		sources.put(LocalBook.SOURCE_BUYED_BOOK, "1");
		if(sources.get(source_down)!=null && sources.get(source_now)!=null 
				&& sources.get(source_down).equals(sources.get(source_now)) )
			return true;
		return false;
	}
	
	/**
	 * 获取赠书赠言信息
	 * @param activity
	 * @param order
	 * @param source
	 */
	private static void getSendbookReceiveInfo(final Activity activity,final OrderEntity order, final String source){
		
		//检查本地有没有赠书赠言
		if(!source.equals(LocalBook.SOURCE_BUYED_BOOK) || activity.isFinishing())
			return ;
		List<SendBookReceiveInfo> list = LocalUserSetting.getSendBookReceiveInfos(activity);
		SendBookReceiveInfo info;
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				info = list.get(i);
				if (info.ebookId.equals(String.valueOf(order.bookId))) {
					list.remove(i);
					SendBookReceiveInfos infos = new SendBookReceiveInfos();
					infos.infos = list;
					LocalUserSetting.saveSendBookReceiveInfos(activity, infos);
					break;
				}
			}
		}
		
		if(!order.isReceived)
			return ;
		
		WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getReceiveInfoParams(String.valueOf(order.bookId)),
				false, new MyAsyncHttpResponseHandler(activity) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						JSONObject jsonObj;
						try {
							jsonObj = new JSONObject(new String(responseBody));
							if (jsonObj != null) {
								String code = jsonObj.optString("code");
								if ("0".equals(code)) {
									SendBookReceiveInfo info = GsonUtils.fromJson(jsonObj.toString(),
											SendBookReceiveInfo.class);
									if (info != null) {
										info.ebookId = String.valueOf(order.bookId);
										info.userPin = LoginUser.getpin();
									}
									if (activity.isFinishing() || info==null)
										return;
									List<SendBookReceiveInfo> list = LocalUserSetting.getSendBookReceiveInfos(activity);
									list.add(info);
									SendBookReceiveInfos infos = new SendBookReceiveInfos();
									infos.infos = list;
									LocalUserSetting.saveSendBookReceiveInfos(activity, infos);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	
}
