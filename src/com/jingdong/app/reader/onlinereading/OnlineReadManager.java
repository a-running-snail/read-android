package com.jingdong.app.reader.onlinereading;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadTool.DownloadConfirmListener;
import com.jingdong.app.reader.client.RequestEntry;
import com.jingdong.app.reader.client.ServiceClient;
import com.jingdong.app.reader.common.CommonActivity;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.data.DrmTools;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.BootEntity;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OlineCard;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.ReturnStatus;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.NetWorkUtils.NetworkConnectType;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.OpenBookHelper.AnimForBookEntity;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.view.dialog.DialogManager;

/**
 * 在线畅读判断是否能畅读和请求证书走过的整个流程。
 * 
 * @author zhangmurui
 * 
 */
public class OnlineReadManager {
	public final static int FREASH_TYPE_BOOK_PROGRESS = 0;
	public final static int FREASH_TYPE_BOOK_ONLINE_STATE = 1;
	public final static int FREASH_TYPE_BOOK_MOD_TIME = 2;
	public final static int FREASH_BUILDIN_BOOK_STATE = 3;
//	private static DownloadConfirmListener mDownloadConfirmListener;
//	public interface DownloadConfirmListener{
//		public void onPositive();
//		public void onNegative();
//	}

	public static void requestServer2ReadOnline(final BookInforEDetail bookE,
			final CommonActivity activity, final AnimForBookEntity animForBookEntity,
			final boolean fromLocalShelf,final DownloadConfirmListener downloadConfirmListener) {
		if (bookE == null) {
			return;
		}

		WebRequestHelper.post(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool
				.getCheckHasReadCardHttpSetting(String.valueOf(bookE.bookid)),
				true, new MyAsyncHttpResponseHandler(activity) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {

							String resultString = new String(responseBody);
							MZLog.d("wangguodong", "#######" + resultString);
							JSONObject jsonObj = new JSONObject(resultString);
							String code = DataParser.getString(jsonObj, "code");
							if (code.equals("0")) {// 已经阅读过此书。

								prepareGoRead(activity, bookE,
										animForBookEntity,downloadConfirmListener);

							} else if (code.equals("10")) {// 没有畅读卡
								ViewHolder holder = new ViewHolder();
								holder.message = "您还没有畅读卡，是否购买？";
								holder.title = "购买畅读卡";
								holder.posText = "去看看";
								holder.negText = "取消";
								holder.posRunnable = new Runnable() {
									@Override
									public void run() {
										Intent intent = new Intent(activity,
												WebViewActivity.class);
										intent.putExtra(WebViewActivity.UrlKey,
												"http://e.m.jd.com/read_card.html");
										intent.putExtra(WebViewActivity.TopbarKey, true);
										intent.putExtra(WebViewActivity.BrowserKey, false);
										intent.putExtra(WebViewActivity.TitleKey,"购买畅读卡");
										activity.startActivity(intent);
										
									}
								};
								showDialog(activity, holder);
							} else if (code.equals("11")) {// 需要将书与卡绑定。
								ViewHolder holder = new ViewHolder();
								if (fromLocalShelf) {
									holder.message = "您是否将《" + bookE.bookName
											+ "》重新激活？";
									holder.title = "激活确认";
									holder.posText = "激活";
									holder.posRunnable = new Runnable() {
										@Override
										public void run() {
											requestServer2BindOnlineCard(
													activity, bookE,
													animForBookEntity);
										}
									};
									holder.negText = "批量激活";
									holder.negRunnable = new Runnable() {
										@Override
										public void run() {
											// Intent intent = new
											// Intent(activity,
											// MainActivity.class);
											// intent.putExtra(KEY_TAB_ID,
											// MainActivity.TAB_BOOKSHELF);
											// intent.putExtra(
											// KEY,
											// OnlineCardManeger.KEY_ADD_NEW_CARD_BATCH);
											// activity.setResult(Activity.RESULT_OK,
											// intent);
											// activity.finish();
										}
									};
									showDialog(activity, holder);
								} else {
								
											requestServer2BindOnlineCard(
													activity, bookE,
													animForBookEntity);
									
								}
								

							} else if (code.equals("-1")) {
								MZLog.d("wangguodong", "网络异常,请检查网络");
								
							} else if (code.equals("1")) {
								MZLog.d("wangguodong", "服务器忙,请稍后重试");
								
							} else if (code.equals("12")) {// 超过卡可购买的上线。
								ViewHolder holder = new ViewHolder();
								holder.message = "已超过畅读上限1000本,是否购买畅读卡？";
								holder.title = "购买畅读卡";
								holder.posText = "去看看";
								holder.negText = "取消";
								holder.posRunnable = new Runnable() {

									@Override
									public void run() {
										// Intent intent = new Intent(activity,
										// OnlineReadCardActivity.class);
										// activity.startActivity(intent);
										
									}
								};
								showDialog(activity, holder);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	/**
	* @Description: 在线畅读，检查账户是否有可用的畅读卡，有直接畅读
	* @param @param bookE
	* @param @param activity
	* @param @param animForBookEntity
	* @param @param fromLocalShelf
	* @param @param downloadConfirmListener
	* @return void
	* @author xuhongwei1
	* @date 2015年10月12日 下午3:46:56 
	* @throws 
	*/ 
	public static void requestServer2ReadOnline(final BookInforEDetail bookE,
			final Activity activity, final AnimForBookEntity animForBookEntity,
			final boolean fromLocalShelf,final DownloadConfirmListener downloadConfirmListener) {
		if (bookE == null) {
			return;
		}

		WebRequestHelper.post(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool
				.getCheckHasReadCardHttpSetting(String.valueOf(bookE.bookid)),
				true, new MyAsyncHttpResponseHandler(activity) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {
							String resultString = new String(responseBody);
							MZLog.d("wangguodong", "#######" + resultString);
							JSONObject jsonObj = new JSONObject(resultString);
							String code = DataParser.getString(jsonObj, "code");
							if (code.equals("0")) {// 已经阅读过此书。
								prepareGoRead(activity, bookE,
										animForBookEntity,downloadConfirmListener);

							} else if (code.equals("10")) {// 没有畅读卡
								ViewHolder viewHolder = new ViewHolder();
								viewHolder.message = "您还没有畅读卡，是否购买？";
								viewHolder.title = "购买畅读卡";
								viewHolder.posText = "去看看";
								viewHolder.negText = "取消";
								DialogManager.showCommonDialog(activity, viewHolder.title, viewHolder.message, viewHolder.posText, viewHolder.negText,
										new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case DialogInterface.BUTTON_POSITIVE:
											Intent intent = new Intent(activity,
													WebViewActivity.class);
											intent.putExtra(WebViewActivity.UrlKey,
													"http://e.m.jd.com/read_card.html");
											intent.putExtra(WebViewActivity.TopbarKey, true);
											intent.putExtra(WebViewActivity.BrowserKey, false);
											intent.putExtra(WebViewActivity.TitleKey,"购买畅读卡");
											activity.startActivity(intent);
											dialog.dismiss();
											break;
										case DialogInterface.BUTTON_NEGATIVE:
											dialog.dismiss();
											break;
										default:
											break;
										}
										dialog.dismiss();
									}
								});
								
							} else if (code.equals("11")) {// 需要将书与卡绑定。
								if (fromLocalShelf) {
									ViewHolder viewHolder = new ViewHolder();
									viewHolder.message = "您是否将《" + bookE.bookName
											+ "》重新激活？";
									viewHolder.title = "激活确认";
									viewHolder.posText = "激活";
									viewHolder.negText = "批量激活";
									DialogManager.showCommonDialog(activity, viewHolder.title, viewHolder.message, viewHolder.posText, viewHolder.negText,
											new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											switch (which) {
											case DialogInterface.BUTTON_POSITIVE:
												requestServer2BindOnlineCard(
														activity, bookE,
														animForBookEntity);
												break;
											case DialogInterface.BUTTON_NEGATIVE:
												
												break;
											default:
												break;
											}
											dialog.dismiss();
										}
									});
								} else {
									requestServer2BindOnlineCard(
											activity, bookE,
											animForBookEntity);
									
								}
								

							} else if (code.equals("-1")) {
								MZLog.d("wangguodong", "网络异常,请检查网络");
								
							} else if (code.equals("1")) {
								MZLog.d("wangguodong", "服务器忙,请稍后重试");
								
							} else if (code.equals("12")) {// 超过卡可购买的上线。
								ViewHolder viewHolder = new ViewHolder();
								viewHolder.message = "已超过畅读上限1000本,是否购买畅读卡？";
								viewHolder.title = "购买畅读卡";
								viewHolder.posText = "去看看";
								viewHolder.negText = "取消";
								DialogManager.showCommonDialog(activity, viewHolder.title, viewHolder.message, viewHolder.posText, viewHolder.negText,
										new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch (which) {
										case DialogInterface.BUTTON_POSITIVE:

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
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	/**
	 * 社区功能，加入书架时检查是否有畅读卡并加入畅读书单中
	 * @param bookE
	 * @param activity
	 */
	public static void checkChangduAuthAndAdd(final BookInforEDetail bookE,final Activity activity) {
		if (bookE == null) {
			return;
		}

		WebRequestHelper.post(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool
				.getCheckHasReadCardHttpSetting(String.valueOf(bookE.bookid)),
				true, new MyAsyncHttpResponseHandler(activity) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						
					}
					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {

							String resultString = new String(responseBody);
							JSONObject jsonObj = new JSONObject(resultString);
							String code = DataParser.getString(jsonObj, "code");
							if (code.equals("10")) {// 没有畅读卡
								return ;
							} else if (code.equals("0")) {// 有畅读卡，而且已经畅读过此书。
								prepareGoRead(activity, bookE,null,null);
							} else if (code.equals("11")) {// 有畅读卡（尚未畅读过此书）：
								requestServer2BindOnlineCard(activity, bookE, null);

							} else if (code.equals("-1")) {
								MZLog.d("wangguodong", "网络异常,请检查网络");
								
							} else if (code.equals("1")) {
								MZLog.d("wangguodong", "服务器忙,请稍后重试");
								
							} else if (code.equals("12")) {// 超过卡可购买的上线。
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

	}

	public static OrderEntity creatOrderByBook(BookInforEDetail book) {
		OrderEntity order = new OrderEntity();// 下载实体
		order.bookId = book.bookid;// bookid
		order.picUrl = book.picUrl;// 小图
		order.bigPicUrl = book.largeSizeImgUrl;// 大图
		order.bookType = book.bookType;// 书的类型:电子书or多媒体书
		order.formatName = book.formatName;// 图书格式。
		order.author = book.author;// 作者
		order.name = book.bookName;// 书名
		order.book_size = book.size+"M";
		order.orderId = book.bookid;// 如果为畅读则用bookid作为orderId.
		return order;
	}

	/**
	 * @author keshuangjie
	 * @description 领取7天免费畅读卡 0 成功 3 未登陆 23 后台没设置 24 没开始 25 已经结束 22 领取过
	 */
	public static void requestServer2GetFreeReadCard(final CommonActivity activity) {

		WebRequestHelper.post(URLText.JD_BOOK_CHANGDU_URL,
				RequestParamsPool.getFreeReadCard(), true,
				new MyAsyncHttpResponseHandler(activity) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {
							JSONObject jsonObject = new JSONObject(new String(
									responseBody));
							String code = DataParser.getString(jsonObject,
									"code");
							String message = DataParser.getString(jsonObject,
									"message");
							if (code.equals("0")) {
								Toast.makeText(activity, message,
										Toast.LENGTH_SHORT).show();
							} else if (code.equals("100")) {
								Toast.makeText(activity, message,
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(activity, "网络错误，请稍候再试",
										Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

	}

	// 向服务器请求绑定畅读卡。
	private static void requestServer2BindOnlineCard(final Activity activity,
			final BookInforEDetail bookE,
			final AnimForBookEntity animForBookEntity) {

		WebRequestHelper.post(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool
				.getBindCardAndBookHttpSetting(String.valueOf(bookE.bookid)),
				true, new MyAsyncHttpResponseHandler(activity) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						

					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {
							JSONObject jsonObj = new JSONObject(new String(
									responseBody));
							String code = DataParser.getString(jsonObj, "code");

							if (code.equals("11")) {// 卡与书绑定成功。
								prepareGoRead(activity, bookE,
										animForBookEntity,null);

							} else if (code.equals("10")) {// 没有购买畅读卡，异常。
								Toast.makeText(activity, "服务器异常",
										Toast.LENGTH_SHORT).show();
								
							} else if (code.equals("0")) {// 已阅读此书在这里应该是异常。
								Toast.makeText(activity, "您已经将该书加入到畅读中",
										Toast.LENGTH_SHORT).show();
								
							} else if (code.equals("-1")) {
								Toast.makeText(activity, "网络异常，请检查网络",
										Toast.LENGTH_SHORT).show();
								
							} else if (code.equals("1")) {
								Toast.makeText(activity, "服务器忙，请稍后重试",
										Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

	}

	private static void getCert(final LocalBook localBook,
			final Activity activity, final ProgressDialog mpDialog,
			final AnimForBookEntity animForBookEntity) {
		/*
		 * final LayoutInflater factory = LayoutInflater.from(activity); final
		 * LinearLayout pageView = (LinearLayout) factory.inflate(
		 * R.layout.activity_prepare_bookread, null, false); activity.post(new
		 * Runnable() {
		 * 
		 * @Override public void run() { // mpDialog.show();// 显示请求阅读权限。
		 * mpDialog.setContentView(pageView, new LayoutParams(
		 * LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)); } });
		 */
		localBook.downingWhich = LocalBook.DOWNING_CERT;
		boolean isSuccess = false;
		String massage = null;
		try {
			BootEntity bootEntity = localBook.boot;
			if (bootEntity == null) {
				// ShowTools.toastInThread(activity.getString(R.string.bootnull));
				MZLog.d("wangguodong", "boot can not be null");
			}
			boolean isRandom = true;
			
			//添加借阅判断 开始
			boolean isborrow =localBook.source.equals(LocalBook.SOURCE_BORROWED_BOOK)?true:false;
			boolean isBorrowBuy = localBook.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK) ? true : false;
			//添加借阅判断 结束
			
			HttpUriRequest request = RequestParamsPool.getCertRequest(bootEntity, DrmTools.hashDevicesInfor(), isRandom,isborrow,isBorrowBuy);

			RequestEntry entry = new RequestEntry(
					RequestEntry.REQUEST_TYPE_SEARCH_POST_BY_JSON, request);
			localBook.setRequestEntry(entry);
			entry._type = RequestEntry.TYPE_STRING;
			RequestEntry requestEntry = ServiceClient.execute(entry);
			if (entry.getRequestCode() != localBook.getRequestCode()) {
				return;
			}
			// Log.i("DownloadThread", "_statusCode------------>"
			// + requestEntry._statusCode);
			if (requestEntry._statusCode == 0) {
				String result = (String) requestEntry._userData;
				MZLog.d("wangguodong", "cert result:" + result);
				JSONObject josnObject = new JSONObject(result);
				localBook.cert = DataParser.getString(josnObject, "key");
				try {
					localBook.random = DataParser.getString(josnObject,
							"random");
					if (!TextUtils.isEmpty(localBook.random)
							&& !TextUtils.isEmpty(localBook.userName)) {
						localBook.deviceId = DrmTools.hashDevicesInfor();
						localBook.saveRandom();
						localBook.save();
					}
					OlineCard olineCard = OlineCard
							.parserOLineCardFromCert(josnObject);
					if (!TextUtils.isEmpty(olineCard.cardNum)) {
						// olineCard.availab = 1;
						olineCard.save();
					}
					LocalBook.saveCardInBook(localBook.getId(),
							olineCard.cardNum);
					// OnlineReadManager.freshLocalBookOLineState(olineCard);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!TextUtils.isEmpty(localBook.cert)) {
					isSuccess = true;
				} else {
					ReturnStatus returnStatus = DataParser
							.parserReturnStatus(result);
					massage = returnStatus.massage;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isSuccess) {// 证书请求成功,将跳进到阅读中。
			LocalBook templocalBook = LocalBook.getLocalBook(localBook.book_id,
					LoginUser.getpin());
			if (templocalBook == null || TextUtils.isEmpty(templocalBook.cert)) {
				localBook.saveEncryptCert();
			} else if (!TextUtils.isEmpty(templocalBook.cert)
					&& !templocalBook.cert.equals(localBook.cert)) {

				mpDialog.dismiss();
//				JdOptionDialog jdDialog = JdOptionDialogFactory.showDialog(
//						activity, "此书已更新", "是否重新下载？", "是", "否",
//						new JdOptionDialog.OnClickListener() {
//							@Override
//							public void onClick(JdOptionDialog dialog, int which) {
//								localBook.del(true);
//								DownloadTool.downBook(activity, localBook,
//										null, true, localBook.source, 0);
//							}
//						}, new JdOptionDialog.OnClickListener() {
//							@Override
//							public void onClick(JdOptionDialog dialog, int which) {
//
//								GotoReadBook(localBook, activity,
//										animForBookEntity);
//
//							}
//						});
//				jdDialog.setOnCancelListener(new JdOptionDialog.OnCancelListener() {
//
//					@Override
//					public void onCancel(JdOptionDialog dialog) {
//						
//					}
//				});
				DialogManager.showCommonDialog(activity, "此书已更新", "是否重新下载？", "是", "否",new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						switch (arg1) {
						case DialogInterface.BUTTON_POSITIVE:
							localBook.del(true);
							DownloadTool.downBook(activity, localBook,
									null, true, localBook.source, 0,null);
							break;
						case DialogInterface.BUTTON_NEGATIVE:

							GotoReadBook(localBook, activity,animForBookEntity);
							break;
						default:
							break;
						}
					}
				});

				return;
			}
			MZLog.d("wangguodong", "开始阅读:XXXX");
			GotoReadBook(localBook, activity, animForBookEntity);

		} else {
			localBook.state = LocalBook.STATE_LOAD_FAILED;
			if (TextUtils.isEmpty(massage)) {
				massage = "获取阅读权限失败！";
			}
			if (!TextUtils.isEmpty(massage)) {
				{MZLog.d("wangguodong", massage);
				ToastUtil.showToastInThread(massage, Toast.LENGTH_LONG);
			}

			}
		}
	}

	public static void GotoReadBook(final LocalBook book,
			final Activity activity, final AnimForBookEntity animForBookEntity) {

		
		book.mod_time = System.currentTimeMillis();
		book.saveModTime();
		if (book.state == LocalBook.STATE_LOADED) {
			book.state = LocalBook.STATE_LOAD_READING;
			book.saveState();
		}
		
		LocalBook book1=LocalBook.getLocalBook(book.book_id,LoginUser.getpin());
		
		if(book1!=null)
		{
			int index =book1._id;
			EBook ebook = MZBookDatabase.instance.getEBook(index);
			OpenBookHelper.openEBook(activity, ebook.bookId);
		}
		
		


	}


	private static void showDialog(final CommonActivity activity,
			final ViewHolder viewHolder) {
		DialogManager.showCommonDialog(activity, viewHolder.title, viewHolder.message, viewHolder.posText, viewHolder.negText,
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (viewHolder.posRunnable != null) {
						 activity.post(viewHolder.posRunnable);
					} else {
						dialog.dismiss();
						
					}
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					if (viewHolder.negRunnable != null) {
						dialog.dismiss();
						 activity.post(viewHolder.negRunnable);
					} else {
						dialog.dismiss();
						
					}
					break;
				default:
					break;
				}
				dialog.dismiss();
			}
		});
//		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//		builder.setTitle(viewHolder.title);
//		builder.setMessage(viewHolder.message);
//		builder.setPositiveButton(viewHolder.posText,
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						if (viewHolder.posRunnable != null) {
//							 activity.post(viewHolder.posRunnable);
//						} else {
//							dialog.dismiss();
//							
//						}
//
//					}
//				});
//		builder.setNegativeButton(viewHolder.negText,
//				new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						if (viewHolder.negRunnable != null) {
//							dialog.dismiss();
//							 activity.post(viewHolder.negRunnable);
//						} else {
//							dialog.dismiss();
//							
//						}
//
//					}
//
//				});
//		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
//
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				
//
//			}
//		});
//		builder.setCancelable(true);
//
//		builder.show();

	}

	private static class ViewHolder {
		String title;
		String message;
		Runnable posRunnable;
		Runnable negRunnable;
		String posText;
		String negText;

	}

	public static void prepareGoRead(final Activity activity,
			final BookInforEDetail bookE,
			final AnimForBookEntity animForBookEntity,final DownloadConfirmListener downloadConfirmListener) {
		final LocalBook book = LocalBook.getLocalBook(bookE.bookid,LoginUser.getpin());
		if (book != null) {
			if (book.state == LocalBook.STATE_LOADING) {
				// ShowTools.toastInThread("该书正在下载中,请稍后再阅读！");
				
			} else if (book.state == LocalBook.STATE_LOAD_FAILED
					|| book.state == LocalBook.STATE_LOAD_PAUSED
					|| !book.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
				
						final LocalBook book1 = LocalBook
								.getLocalBook(bookE.bookid,LoginUser.getpin());
						if (book1 != null) {
							book1.del(true);
						}
						final OrderEntity orderEntity = creatOrderByBook(bookE);
						if (NetWorkUtils.getNetworkConnectType(activity) == NetworkConnectType.MOBILE) {
							String title="图书大小为"+orderEntity.book_size;
							DialogManager.showCommonDialog(activity, title, "确定使用移动网络下载吗", "确定", "取消", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case DialogInterface.BUTTON_POSITIVE:
										DownloadTool.downBook(activity, orderEntity, null,
												false, LocalBook.SOURCE_ONLINE_BOOK, 0,downloadConfirmListener,false);
										break;
									case DialogInterface.BUTTON_NEGATIVE:
										
										break;
									default:
										break;
									}
									dialog.dismiss();
								}
							}); 
						}else{
							DownloadTool.downBook(activity, orderEntity, null,
									false, LocalBook.SOURCE_ONLINE_BOOK, 0,downloadConfirmListener,false);
						}
				
			} else if (book.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
				final ProgressDialog mpDialog = new ProgressDialog(activity);
				new Thread(new Runnable() {
					@Override
					public void run() {
						getCert(book, activity, mpDialog, animForBookEntity);
					}
				}).start();
			}
		} else {
			
			OrderEntity orderEntity = creatOrderByBook(bookE);
			DownloadTool.downBook(activity, orderEntity, null, false,
					LocalBook.SOURCE_ONLINE_BOOK, 0,downloadConfirmListener,false);
			
		}
	}

	/**
	 * @author ThinkinBunny
	 * @see pdf 在线畅读按键检测组件
	 * @since 2012-10-19
	 * @return boolean
	 * **/
	/*
	 * public static boolean onLineReadChecker(LePDFActivity base, boolean
	 * isTouch, OnlineCheckRunnable OnlineRegisterRunnable) {
	 * 
	 * if (isTouch) { if (!base.getOnlineUserState().isOlineRead()) {
	 * BookReadActivity.showConnectDialog(base.getOnlineUserState(), base,
	 * OnlineRegisterRunnable); return true; } }
	 * 
	 * return false; }
	 * 
	 * /** 更新本地书架。按时间
	 * 
	 * @param book
	 * 
	 * @param activity
	 */
	/*
	 * public static void freshLocalBookshelf(LocalBook book, int freshWhich) {
	 * Intent intent = new Intent(MainActivity.ACTION_READ_TIEM_REFESH);
	 * intent.putExtra(KEY, freshWhich); String key1 = DataIntent.creatKey();
	 * DataIntent.put(key1, book); intent.putExtra(KEY1, key1);
	 * MyApplication.getInstance().getApplicationContext()
	 * .sendBroadcast(intent); }
	 * 
	 * /** 更新本地书架。按时间
	 * 
	 * @param book
	 * 
	 * @param activity
	 */
	/*
	 * public static void freshLocalBookOLineState(OlineCard olineCard) { Intent
	 * intent = new Intent(MainActivity.ACTION_READ_TIEM_REFESH);
	 * intent.putExtra(KEY, OnlineReadManager.FREASH_TYPE_BOOK_ONLINE_STATE);
	 * String key1 = DataIntent.creatKey(); DataIntent.put(key1, olineCard);
	 * intent.putExtra(KEY1, key1);
	 * if(MyApplication.getInstance().getCurrentMyActivity()!=null)
	 * MyApplication.getInstance().getCurrentMyActivity()
	 * .sendBroadcast(intent); }
	 */
}
