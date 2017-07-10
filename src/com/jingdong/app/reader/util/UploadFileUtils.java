package com.jingdong.app.reader.util;

import java.io.File;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Context;

import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;

public class UploadFileUtils {

	public interface UploadRefreshListener {
		/**
		 * 
		 * @param path
		 *            文件路径
		 * @param progress
		 *            进度百分比，96%
		 */
		public void onUpLoadProgressRefresh(String path, double progress);

		public void onUpLoadSuccessed(String path, String msg, String yunUrl,
				String size);

		public void onUpLoadFailed(String path, String msg);
	}

	private UploadRefreshListener listener = null;

	public UploadFileUtils(UploadRefreshListener listener) {
		this.listener = listener;
	}

	// 上传书籍到云盘

	public void uploadFileToYun(Context context, final String sourceFile) {
		if (sourceFile == null || sourceFile.equals("")) {
			MZLog.d("wangguodong", "文件目录不能为空");
			if (listener != null)
				listener.onUpLoadFailed(sourceFile,"文件目录不能为空");
			return;
		}

		WebRequestHelper.post(URLText.JD_BOOK_UPLOAD_URL,
				RequestParamsPool.uploadYunPanParams(new File(sourceFile)),
				true, new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						if (listener != null && arg2!=null)
							listener.onUpLoadFailed(sourceFile,
									new String(arg2));

					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						try {
							String result = new String(responseBody);

							JSONObject object = new JSONObject(result);
							int code = object.optInt("code");
							String msg = object.optString("msg");
							String size = object.optString("size");
							String url = object.optString("url");

							if (code == 0 && listener != null) {
								listener.onUpLoadSuccessed(sourceFile, msg,
										url, size);
							} else if (listener != null) {
								listener.onUpLoadFailed(sourceFile, msg);
							}

						} catch (Exception e) {
							if (listener != null)
								listener.onUpLoadFailed(sourceFile,
										"exception has occured");
						}

					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						if (listener != null)
							listener.onUpLoadProgressRefresh(sourceFile,
									bytesWritten * 100 / totalSize);
						super.onProgress(bytesWritten, totalSize);
					}

				});

	}
	
	/**
	* @Description: 上传图片到云盘
	* @author xuhongwei1
	* @date 2015年10月26日 下午4:39:50 
	* @throws 
	*/ 
	public void uploadImageToYun(Context context, final String sourceFile) {
		if (sourceFile == null || sourceFile.equals("")) {
			MZLog.d("wangguodong", "文件目录不能为空");
			if (listener != null)
				listener.onUpLoadFailed(sourceFile,"文件目录不能为空");
			return;
		}

		WebRequestHelper.post(URLText.uploadImage,
				RequestParamsPool.uploadYunPanParams(new File(sourceFile)),
				true, new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						if (listener != null && arg2!=null) {
							listener.onUpLoadFailed(sourceFile, new String(arg2));
						}else {
							listener.onUpLoadFailed(sourceFile, null);
						}

					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {
							String result = new String(responseBody);

							JSONObject object = new JSONObject(result);
							int code = object.optInt("code");
							String msg = object.optString("msg");
							String size = object.optString("size");
							String url = object.optString("url");

							if (code == 0 && listener != null) {
								listener.onUpLoadSuccessed(sourceFile, msg,
										url, size);
							} else if (listener != null) {
								listener.onUpLoadFailed(sourceFile, msg);
							}

						} catch (Exception e) {
							if (listener != null)
								listener.onUpLoadFailed(sourceFile,
										"exception has occured");
						}

					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						if (listener != null)
							listener.onUpLoadProgressRefresh(sourceFile,
									bytesWritten * 100 / totalSize);
						super.onProgress(bytesWritten, totalSize);
					}

				});

	}

}
