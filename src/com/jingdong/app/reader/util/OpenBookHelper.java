package com.jingdong.app.reader.util;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.bookshelf.BookcaseLocalFragmentNewUI;
import com.jingdong.app.reader.bookshelf.animation.EBookAnimationUtils;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OlineCard;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.onlinereading.OnlineCardManeger;
import com.jingdong.app.reader.pdf.PDFBookViewActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.view.dialog.DialogManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Toast;

/**
 * 打开书工具类
 */
public class OpenBookHelper {

	public static final String EBookIdKey = "EBookIdKey";
	public static final String DocumentIdKey = "DocumentIdKey";
	
	/**
	 * 打开自有书籍（ePub）
	 * @param activity
	 * @param documentId
	 */
	private static void openDocumentEPUB(Activity activity, int documentId) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(OpenBookHelper.DocumentIdKey, documentId);
		intent.setClass(activity, BookPageViewActivity.class);
		activity.startActivity(intent);
	}

	/**
	 * 打开京东电子书（ePub）
	 * @param activity
	 * @param bookId
	 */
	private static void openEBookEPUB(Activity activity, long bookId) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(OpenBookHelper.EBookIdKey, bookId);
		intent.setClass(activity, BookPageViewActivity.class);
		activity.startActivity(intent);
	}

	private static void openDocumentPDF(Activity activity, int documentId) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(OpenBookHelper.DocumentIdKey, documentId);
		intent.setClass(activity, PDFBookViewActivity.class);
		activity.startActivity(intent);
	}

	/**
	 * 打开京东电子书（PDF）
	 * @param activity
	 * @param bookId
	 */
	private static void openEBookPDF(Activity activity, long bookId) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(OpenBookHelper.EBookIdKey, bookId);
		intent.setClass(activity, PDFBookViewActivity.class);
		activity.startActivity(intent);
	}

	/**
	 * 打开电子书
	 * @param activity
	 * @param bookId
	 */
	public static void openEBook(Activity activity, long bookId) {
		LocalBook book = LocalBook.getLocalBook(bookId,LoginUser.getpin());
		if (book != null) {
			
			if (processBorrowed(activity, book, null)) {// 处理借阅权限
				return;
			}

			if (processChangDu(activity, book, null)) {// 处理畅读权限
				return;
			}
		
			if(book.source.equals(LocalBook.SOURCE_BORROWED_BOOK) || book.source.equals(LocalBook.SOURCE_BULIT_IN)
					|| book.source.equals(LocalBook.SOURCE_BUYED_BOOK) || book.source.equals(LocalBook.SOURCE_ONLINE_BOOK)
					|| book.source.equals(LocalBook.SOURCE_TRYREAD_BOOK) || book.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK)){
				
				if (book.format == LocalBook.FORMAT_EPUB) {
					openEBookEPUB(activity, bookId);
				} else {
					openEBookPDF(activity, bookId);
				}
			}
			
		}
	}
	
	/**
	 * 打开书籍阅读
	 * @param activity
	 * @param bookId
	 * @param view
	 */
	public static void openEBook(final Activity activity, final long bookId, View view) {
		if(null == view) {
			openEBook(activity, bookId);
			return;
		}
		
		final LocalBook book = LocalBook.getLocalBook(bookId,LoginUser.getpin());
		if (book != null) {
			
			if (processBorrowed(activity, book, view)) {// 处理借阅权限
				return;
			}

			if (processChangDu(activity, book, view)) {// 处理畅读权限
				return;
			}
		
			if(book.source.equals(LocalBook.SOURCE_BORROWED_BOOK) || book.source.equals(LocalBook.SOURCE_BULIT_IN)
					|| book.source.equals(LocalBook.SOURCE_BUYED_BOOK) || book.source.equals(LocalBook.SOURCE_ONLINE_BOOK)
					|| book.source.equals(LocalBook.SOURCE_TRYREAD_BOOK) || book.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK)){
				
				
				if(null == view) {
					if (book.format == LocalBook.FORMAT_EPUB) {
						openEBookEPUB(activity, bookId);
					} else {
						openEBookPDF(activity, bookId);
					}
				}else {
					EBookAnimationUtils mEBookAnimationUtils = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
					if(null != mEBookAnimationUtils) {
						mEBookAnimationUtils.showOpenEBookAnimation(view, new EBookAnimationUtils.OnAnimationListener() {
							@Override
							public void AnimationEnd() {
								if (book.format == LocalBook.FORMAT_EPUB) {
									openEBookEPUB(activity, bookId);
								} else {
									openEBookPDF(activity, bookId);
								}
							}

							@Override
							public void AnimationStart() {
								// TODO Auto-generated method stub
							}
						});	
					}else {
						commmonOpenEbook(activity, book);
					}
				}
				
			}
			
		}
	}

	public static void openDocument(Activity activity, int documentId) {
		Document document = MZBookDatabase.instance.getDocument(documentId);
		if (document != null) {
			if (document.format == LocalBook.FORMAT_EPUB) {
				openDocumentEPUB(activity, documentId);
			} else {
				openDocumentPDF(activity, documentId);
			}
		}
	}
	
	public static void openDocument(final Activity activity, final int documentId, View view) {
		if(null == view) {
			openDocument(activity, documentId);
			return;
		}
		
		EBookAnimationUtils mEBookAnimationUtils = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
		if(null != mEBookAnimationUtils) {
			mEBookAnimationUtils.showOpenEBookAnimation(view, new EBookAnimationUtils.OnAnimationListener() {
				@Override
				public void AnimationEnd() {
					openDocument(activity, documentId);
				}

				@Override
				public void AnimationStart() {
					// TODO Auto-generated method stub
				}
			});	
		}else {
			openDocument(activity, documentId);
		}
	}
	
	private static void commmonOpenEbook(Activity activity,LocalBook book){
		if (book.format == LocalBook.FORMAT_EPUB) {
			openEBookEPUB(activity, book.book_id);
		} else {
			openEBookPDF(activity, book.book_id);
		}
	}
	
	/**
	 * 在畅读期限内
	 * @param activity
	 * @param book
	 * @param view
	 */
	private static void commmonOpenEbook(final Activity activity, final LocalBook book, View view){
		if(null == view) {
			commmonOpenEbook(activity, book);
		}else {
			EBookAnimationUtils mEBookAnimationUtils = BookcaseLocalFragmentNewUI.getEBookAnimationUtils();
			if(null != mEBookAnimationUtils) {
				mEBookAnimationUtils.showOpenEBookAnimation(view, new EBookAnimationUtils.OnAnimationListener() {
					@Override
					public void AnimationEnd() {
						commmonOpenEbook(activity, book);
					}

					@Override
					public void AnimationStart() {
						// TODO Auto-generated method stub
					}
				});	
			}else {
				commmonOpenEbook(activity, book);
			}
		}
	}

	public static boolean processChangDu(final Activity activity,final LocalBook book, final View view) {
		// 添加畅读权限控制
		if (book.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
			String expireTime=  MZBookDatabase.instance.getChangduExpireTime(book._id);
			if (TextUtils.isEmpty(expireTime)) {
				BookcaseLocalFragmentNewUI.resetClick();
				Toast.makeText(activity, "畅读卡数据异常，请重新下载", Toast.LENGTH_LONG).show();
			}
			
			updateSystemTime(activity);
			final String endtime = OlineDesUtils.decrypt(expireTime);
			if (!TextUtils.isEmpty(endtime)) {
				final long end = Long.parseLong(endtime);
				String time = SettingUtils.getInstance().getString("sys_now");
				if (!TextUtils.isEmpty(time)) {
					String sysCurrentTime = DesUtil.decrypt(time, OnlineCardManeger.KEY);
					long cur = Long.parseLong(sysCurrentTime);
					if (end != -1 && cur != -1) {
						if (end > cur) {
							commmonOpenEbook(activity,book, view);
						} else {
							addToChangduList(activity,book, view);
						}
					} else {
						checkChangDuLocalTime(activity, book, view, end);
					}
				} else {
					checkChangDuLocalTime(activity,book, view, end);	
				}
			} else {
				BookcaseLocalFragmentNewUI.resetClick();
				Toast.makeText(activity, "畅读卡数据异常，请重新下载", Toast.LENGTH_LONG).show();
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 检查本地时间
	 */
	private static void checkChangDuLocalTime(final Activity activity, final LocalBook book, final View view, long end) {
		long cur = System.currentTimeMillis();
		if (end != -1) {
			if (end > cur) {
				// 在畅读期限内
				commmonOpenEbook(activity,book, view);
			} else {
				// 畅读过期了
				addToChangduList(activity,book, view);
			}
		} else {
			BookcaseLocalFragmentNewUI.resetClick();
			Toast.makeText(activity, "畅读卡数据异常，请重新下载!", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 添加畅读权限
	 */
	private static void addToChangduList(final Activity activity, final LocalBook book, final View view) {
		if (NetWorkUtils.isNetworkConnected(activity)) {
			WebRequestHelper.post(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool.addToChangduListParams(book.book_id),
					true, new MyAsyncHttpResponseHandler(activity) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							BookcaseLocalFragmentNewUI.resetClick();
							Toast.makeText(activity, "无法检查畅读权限，请联网重试!", Toast.LENGTH_LONG).show();
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
							try {
								BookcaseLocalFragmentNewUI.resetClick();
								JSONObject jsonObj = new JSONObject(new String(responseBody));
								String code = DataParser.getString(jsonObj, "code");
								String cardEndTime = DataParser.getString(jsonObj, "cardEndTime");
								String cardNO = DataParser.getString(jsonObj, "cardNO");
								JSONArray array =jsonObj.optJSONArray("ebookList");
								
								if (code.equals("0")) { //请求成功
									
									if (array!=null&&array.length() > 0) {
										
										JSONObject temp = array.optJSONObject(0);
										String status =temp.optString("status");
										
										if (!TextUtils.isEmpty(status)&&status.equals("11")) {
											//书籍加入畅读卡ok 更新olinecard
											OlineCard.save("", cardEndTime, cardNO, 0);
											//更新cardnum 
											LocalBook.saveCardInBookByIndex(book._id,cardNO);
											commmonOpenEbook(activity, book, view);
										} else if(!TextUtils.isEmpty(status) && status.equals("14")){
											openFailDialog(activity, book.book_id);
										} else {
											BookcaseLocalFragmentNewUI.resetClick();
											if (!activity.isFinishing()) {
												DialogManager.showCommonDialog(activity, "提示", "畅读过期了，是否去购买新的畅读卡?", "确定", "取消", new OnClickListener() {
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
										}
									} else {
										Toast.makeText(activity, "请求异常，请重试!", Toast.LENGTH_LONG).show();
									}
								} else if(code.equals("14")) { //不支持畅读
									openFailDialog(activity, book.book_id);
								} else if(code.equals("80")) { //书籍下架
									Toast.makeText(activity, "很抱歉，出版社把这本书下架了，原因你懂的！", Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(activity, "请求异常，请重试!", Toast.LENGTH_LONG).show();
								}
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(activity, "请求异常，请重试!", Toast.LENGTH_LONG).show();
							}
						}
			});
			
		} else {
			BookcaseLocalFragmentNewUI.resetClick();
			Toast.makeText(activity, "无法检查畅读权限，请联网重试!", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 书籍不支持畅读提示
	 * @param activity
	 * @param book_id
	 */
	private static void openFailDialog(final Activity activity, final long book_id) {
		if(!activity.isFinishing()) {
			DialogManager.showCommonDialog(activity, "提示", "客官抱歉-_-这本书因版权方要求不支持畅读了，要再看得您再掏点银子了", "去购买", "不看了", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						Intent intent2 = new Intent(activity, BookInfoNewUIActivity.class);
						intent2.putExtra("bookid", book_id);
						activity.startActivity(intent2);
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
	}

	public static boolean processBorrowed(final Activity activity, final LocalBook book, final View view) {
		// 添加借阅权限控制
		if (book.source.equals(LocalBook.SOURCE_BORROWED_BOOK) || book.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK)) {
			String endtime = OlineDesUtils.decrypt(book.borrowEndTime);
			if (!TextUtils.isEmpty(endtime)) {
				final long end = TimeFormat.formatStringTime(endtime);
				updateSystemTime(activity);
				String time = SettingUtils.getInstance().getString("sys_now");
				if (!TextUtils.isEmpty(time)) {
					String sysCurrentTime = DesUtil.decrypt(time, OnlineCardManeger.KEY);
					if (!TextUtils.isEmpty(sysCurrentTime)) {
						long cur = Long.parseLong(sysCurrentTime);
						if (end != -1 && cur != -1) {
							if (end > cur) {
								commmonOpenEbook(activity,book, view);
							} else {
								// 借阅过期了
								borrowedDialog(activity, book);
							}
						} else {
							checkBorrowedLocalTime(activity,book, view, end);	
						}
					} else {
						checkBorrowedLocalTime(activity, book, view, end);
					} 
				} else {
					checkBorrowedLocalTime(activity, book, view, end);
				}
			} else {
				BookcaseLocalFragmentNewUI.resetClick();
				Toast.makeText(activity, "借阅书籍出问题了，请重新下载!", Toast.LENGTH_LONG).show();
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 检查本地借阅时间
	 */
	private static void checkBorrowedLocalTime(final Activity activity, final LocalBook book, final View view, long end) {
		long cur = System.currentTimeMillis();
		if (end != -1) {	
			if (end > cur) {
				// 在借阅期限内
				commmonOpenEbook(activity, book, view);
			} else {
				// 借阅过期了
				borrowedDialog(activity, book);
			}
		}else {
			BookcaseLocalFragmentNewUI.resetClick();
			Toast.makeText(activity, "借阅书籍出问题了，请重新下载!", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 借阅过期提示
	 */
	private static void borrowedDialog(final Activity activity, final LocalBook book) {
		// 借阅过期了
		BookcaseLocalFragmentNewUI.resetClick();
		DialogManager.showCommonDialog(activity, "提示", "借阅过期了，是否去书城购买?", "确定", "取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					Intent launcherIntent = new Intent(activity, BookInfoNewUIActivity.class);
					launcherIntent.putExtra("bookid", book.book_id);
					activity.startActivity(launcherIntent);
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
	
	public static class AnimForBookEntity {
		public Bitmap bitmap;
		public AbsoluteLayout.LayoutParams start_layoutParams;
		public AbsoluteLayout.LayoutParams end_layoutParams;
	}
	
	/**
	 * 更新服务器系统时间
	 * @param activity
	 */
	public static void updateSystemTime(Context context) {
		if (!NetWorkUtils.isNetworkConnected(context)) {
			return;
		}

		//获取服务器当前系统时间
		WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getSystemTimeParams(),
				true, new MyAsyncHttpResponseHandler(context) {
			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					JSONObject jsonObj = new JSONObject(new String(responseBody));
					String code = DataParser.getString(jsonObj, "code");
					if (code.equals("0")) {
						String time = DataParser.getString(jsonObj, "currentTime");
						if (!TextUtils.isEmpty(time)) {
							SettingUtils.getInstance().putString("sys_now", time);
						} 
					} 
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
	}

}
