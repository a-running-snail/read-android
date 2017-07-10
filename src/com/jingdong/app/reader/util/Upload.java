package com.jingdong.app.reader.util;

import java.io.File;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.UploadFileUtils.UploadRefreshListener;
import com.jingdong.app.reader.R;

public class Upload {

	public static final String TAG = "Upload";
	public static final String BOOK_PATH_ERROR = "book_path_error";
	public static final String UPLOAD_SERVER_ERROR = "upload_server_error";

	public static final String TYPE_MSG = "msg";
	public static final String TYPE_PROGRESS = "progress";

	public interface UploadStateListener {

		public void onUploadFinished(String type, String msg);
	}

	// 获得epub书籍路径
	public static String getSourceFilePath(String coverPath) {

		int position = coverPath.indexOf("content");
		if (position != -1) {
			return coverPath.substring(0, position);
		}
		return "error";
	}

	public static void requestUploadFile(final Context context,
			Map<String, String> map, final UploadStateListener listener) {

		final String documentid = map.get("documentId");
		final String sign = map.get("sign");
		final String path = map.get("sourePath");
		final String name = map.get("name");

		if (map.get("serverId").equals("0") || map.get("serverId") == null) {
			// 如果本地serverid没有的话 重新获取
			MZLog.d("wangguodong", "检查该书是否有serverid");

			WebRequestHelper.post(URLText.synServerId,
					RequestParamsPool.bindYunPanServeridParams(sign, name),
					true, new MyAsyncHttpResponseHandler(context) {

						@Override
						public void onResponse(int statusCode,
								Header[] headers, byte[] responseBody) {
							MZLog.d("wangguodong", new String(responseBody));
							
							DocBind bind = new DocBind();
							bind.userId = LoginUser.getpin();
							bind.documentId = Integer.valueOf(documentid);
							try {
								JSONObject object = new JSONObject(new String(
										responseBody));
								bind.serverId = object.getLong("document_id");
								MZLog.d("wangguodong", "服务器返回的serverid="
										+ bind.serverId);
								MZBookDatabase.instance.UpdateDocBind(bind);

								requestBookState(context, sign, bind.serverId,
										path, name, documentid, listener);

							} catch (Exception e) {
								MZLog.e("wangguodong", e.getMessage());
								if (listener != null)
									listener.onUploadFinished(TYPE_MSG,
											"服务器出错了");
							}

						}

						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3) {
							MZLog.d("wangguodong", "请求上传失败。。网络错误");
							if (listener != null)
								listener.onUploadFinished(TYPE_MSG, "书籍绑定失败");
						}
					});

		} else {
			requestBookState(context, sign,
					Integer.valueOf(map.get("serverId")), path, name,
					documentid, listener);
		}

	}

	private static void requestBookState(final Context context,
			final String sign, final long id, final String booksource,
			final String name, final String documentid,
			final UploadStateListener listener) {
		// 判断服务器上是否已有这本书籍
		JSONArray jsonArr = null;
		try {
			jsonArr = new JSONArray();
			JSONObject obj = new JSONObject();
			obj.put("sign", sign);
			obj.put("id", id);
			jsonArr.put(obj);
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null)
				listener.onUploadFinished(TYPE_MSG, "生成验证json数组出错");
			return;
		}
		WebRequestHelper.post(URLText.synCloudDiskBook,
				RequestParamsPool.isNeedUploadYunPanParams(jsonArr.toString()),
				true, new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						MZLog.d("wangguodong", "请求出错了");
						if (listener != null)
							listener.onUploadFinished(TYPE_MSG, "请求出错了");
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						
						MZLog.d("wangguodong", new String(responseBody));
						
						try {
							JSONArray array = new JSONArray(new String(
									responseBody));
							int unSyncBookCount = array.length();
							if (unSyncBookCount < 1) {
								MZLog.d("wangguodong", "服务器已经存在该书，秒传...");
								if (listener != null)
									listener.onUploadFinished(TYPE_MSG,
											"服务器已经存在该书，秒传...");
								return;
							}
							MZLog.d("wangguodong", "服务器没有找到该书...");
							// 开始上传数据

							MZLog.d("wangguodong", "未发生异常 开始上传");
							String sourceFile = booksource;

							// 上传
							UploadFileUtils uploadFileUtils = new UploadFileUtils(
									new UploadRefreshListener() {

										@Override
										public void onUpLoadSuccessed(
												String path, String msg,
												String yunUrl, String size) {

											String prefix = booksource.substring(booksource
													.lastIndexOf(".") + 1);
											rebackToServer(context, sign, id,
													yunUrl, size, prefix,
													listener);
										}

										@Override
										public void onUpLoadProgressRefresh(
												String path, double progress) {
											if (listener != null)
												listener.onUploadFinished(TYPE_PROGRESS,progress+"");

										}

										@Override
										public void onUpLoadFailed(String path,
												String msg) {
											if (listener != null)
												listener.onUploadFinished(
														TYPE_MSG, "上传失败了");

										}
									});

							uploadFileUtils
									.uploadFileToYun(context, sourceFile);
							// 上传

						} catch (Exception e) {
							e.printStackTrace();
							if (listener != null)
								listener.onUploadFinished(TYPE_MSG,
										e.getMessage());
						}
					}
				});

	}

	private static void rebackToServer(Context context, String sign, long id,
			String url, String size, String type,
			final UploadStateListener listener) {

		WebRequestHelper.post(URLText.uploadedBookReBack, RequestParamsPool
				.uploadYunPanRebackParams(sign, id, url, size, type), true,
				new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						if (listener != null)
							listener.onUploadFinished(TYPE_MSG, "上传回调服务器出错");

					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						try {
							JSONObject object = new JSONObject(new String(
									responseBody));
							if (!object.toString().contains("404")) {
								if (listener != null)
									listener.onUploadFinished(TYPE_MSG, "ok");
							} else {

								if (listener != null)
									listener.onUploadFinished(TYPE_MSG,
											"书籍绑定出错了");
							}

						} catch (JSONException e) {
							e.printStackTrace();
							if (listener != null)
								listener.onUploadFinished(TYPE_MSG, "回调绑定出错了");
						}

					}
				});

	}

}
