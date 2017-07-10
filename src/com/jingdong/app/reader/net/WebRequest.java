package com.jingdong.app.reader.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.jingdong.app.reader.activity.MainActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.me.model.EditInfoModel;
import com.jingdong.app.reader.request.Redirect;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;



public class WebRequest {


	// private static final int MZRequestNetworkErr = 101;
	private static final int MZRequestAuthLogOut=401;
	private static final int MZRequestAuthErr = 403;
	// private static final int MZRequestNotExist = 404;
	// private static final int MZRequestServerErr = 500;
	// private static final int MZRequestUnknownErr = 600;

	private static final String[] httpMethodText = new String[] { "(GET)", "(POST)", "(DELETE)" };
	public static final int httpGet = 0;
	public static final int httpPost = 1;
	public static final int httpDelete = 2;
	// public static final int httpPut = 3;

	private static final String TAG = "WebData";
	public static final String CHAR_SET = "UTF-8";
	public static final String AUTH_TOKEN = "auth_token";

	public static List<Book> requestFreeBookList(Context context, String url, int page, int count) {
		List<Book> bookList = new ArrayList<Book>();

		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("page", String.valueOf(page));
		paramMap.put("count", String.valueOf(count));
		String urlText = URLBuilder.addParameter(url, paramMap);

		String jsonText = getWebDataWithContext(context, urlText);

		try {
			JSONObject json = new JSONObject(jsonText);
			JSONArray bookArray = json.getJSONArray("books");
			for (int i = 0; i < bookArray.length(); ++i) {
				JSONObject bookJSON = bookArray.getJSONObject(i);
				Book book = Book.fromJSON(bookJSON);
				bookList.add(book);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return bookList;
	}

	public static String deleteWebDataWithContext(Context context, String urlText) {
		String result = requestWebData(urlText, null, httpDelete);
		result = processAuthInvalidError(context, result, urlText, "(DELETE)");
		return result;
	}

	public static String getWebDataWithContext(Context context, String urlText) {
		String result = requestWebData(urlText, null, httpGet);
		result = processAuthInvalidError(context, result, urlText, "(GET)");
		return result;
	}



	public static String getWebDataWithContextWithoutRedirect(Context context, String urlText) {
		String result = requestWebData(urlText, null, httpGet, false);
		result = processAuthInvalidError(context, result, urlText, "(GET)");
		return result;
	}

	public static String postWebDataWithContext(Context context, String urlText, String postText) {
		String result = requestWebData(urlText, postText, httpPost);
		result = processAuthInvalidError(context, result, urlText, "(POST)" + postText);
		return result;
	}

	public static String postBytesWithContext(Context context, String urlText, byte[] postData) {
		String result = requestWebDataWithByte(urlText, postData);
		result = processAuthInvalidError(context, result, urlText, "(POST)" + Arrays.toString(postData));
		return result;
	}

	private static void jumpToMainActivity(Context context) {
		if (context instanceof Activity) {
			Activity activity = (Activity) context;
			Intent intent = new Intent(activity, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(intent);
		} else if (context instanceof Service) {
			Intent intent = new Intent();
			intent.setClass(context, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			Intent intent = new Intent();
			intent.setClass(context, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
		}
		MZBookApplication.exitApplication();
	}

	public static String processAuthInvalidError(Context context, String result, String requesturl, String data) {
		if (result.startsWith("[")) {
			return result;
		}

		if (result.equals("Received authentication challenge is null")) {
			HashMap<String, String> m = new HashMap<String, String>();
			m.put("url", requesturl);
			m.put("method", data);
			m.put("message", "Received authentication challenge is null");
			LocalUserSetting.clearUserInfo(context);
			jumpToMainActivity(context);
			result = "";
			return result;
		}

		try {
			JSONObject obj = new JSONObject(result);
			if (!obj.isNull("error")) {
				HashMap<String, String> m = new HashMap<String, String>();
				m.put("url", requesturl);
				m.put("method", data);
				JSONObject errorObject = obj.optJSONObject("error");
				if (errorObject != null) {
					int errorCode = errorObject.optInt("code");
					if (errorCode == MZRequestAuthErr||errorCode==MZRequestAuthLogOut) {
						LocalUserSetting.clearUserInfo(context);
						jumpToMainActivity(context);
					}
					final String message = errorObject.optString("message");
					if (!UiStaticMethod.isNullString(message) && context instanceof Activity) {
						final Activity activity = (Activity)context;
						activity.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
							}
						});
					}
				} else {
					String errorText = obj.optString("error");
					if (errorText.equals("认证码无效.")) {
						LocalUserSetting.clearUserInfo(context);
						jumpToMainActivity(context);
					}
					else if(errorText.equals("继续操作前请注册或者登录.")) {
						LocalUserSetting.clearUserInfo(context);
						jumpToMainActivity(context);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String requestWebData(String urlText, String postText, int httpMethod) {
		return requestWebData(urlText, postText, httpMethod, true);
	}

	private static String requestWebData(String urlText, String postText, int httpMethod, boolean redirect) {
		String text = "";
		HttpURLConnection conn = null;
		System.setProperty("http.keepAlive", "false");
		try {
			MZLog.d(TAG, urlText);
			URL url = new URL(urlText);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(20 * 1000);
			conn.setConnectTimeout(10 * 1000);
			conn.setInstanceFollowRedirects(redirect);
			if (httpMethod == httpGet) {
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				conn.connect();

			} else if (httpMethod == httpDelete) {
				conn.setRequestMethod("DELETE");
				conn.setDoInput(true);
				conn.connect();

			} else if (httpMethod == httpPost) {
				byte[] postData = postText.getBytes();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");		
				conn.setFixedLengthStreamingMode(postData.length);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

				OutputStream out = conn.getOutputStream();
				out.write(postData);
				out.close();
			}

			int response = conn.getResponseCode();
			int fileLength = conn.getContentLength();
			MZLog.d(TAG, httpMethodText[httpMethod] + "The response is: " + response + " The file length is: "
					+ fileLength);
			if (response >= 200 && response < 300) {
				InputStream is = conn.getInputStream();
				text = IOUtil.readIt(is);
			} else {
				if (redirect) {
					InputStream err = conn.getErrorStream();
					text = IOUtil.readIt(err);
				} else {
					if (response == 301 || response == 302) {
						text = Redirect.toJson(conn.getHeaderField(Redirect.LOCATION));
					} else {
						InputStream err = conn.getErrorStream();
						text = IOUtil.readIt(err);
					}
				}
			}
		} catch (IOException e) {
			text = e.getMessage();
			e.printStackTrace();
			try {
				int response = conn.getResponseCode();
				int fileLength = conn.getContentLength();
				MZLog.d(TAG, "(GET)The response is: " + response + " The file length is: " + fileLength);
				if (response >= 200 && response < 300) {
					InputStream is = conn.getInputStream();
					text = IOUtil.readIt(is);
				} else {
					if (redirect) {
						InputStream err = conn.getErrorStream();
						text = IOUtil.readIt(err);
					} else {
						if (response == 301 || response == 302) {
							text = Redirect.toJson(conn.getHeaderField(Redirect.LOCATION));
						} else {
							InputStream err = conn.getErrorStream();
							text = IOUtil.readIt(err);
						}
					}
				}
			} catch (IOException e1) {
				if (e1 != null) {
					e1.printStackTrace();
				}
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		if (text == null) {
			text = "";
		}
		MZLog.d(TAG, text);
		return text;

	}

	private static String requestWebDataWithByte(String urlText, byte[] postData) {
		String text = "";
		HttpURLConnection conn = null;
		System.setProperty("http.keepAlive", "false");
		try {
			URL url = new URL(urlText);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(20 * 1000);
			conn.setConnectTimeout(10 * 1000);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setFixedLengthStreamingMode(postData.length);
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + EditInfoModel.BOUNDARY);
			OutputStream out = conn.getOutputStream();
			out.write(postData);
			out.close();
			int response = conn.getResponseCode();
			int fileLength = conn.getContentLength();
			MZLog.d(TAG, httpMethodText[httpPost] + "The response is: " + response + " The file length is: "
					+ fileLength);
			if (response >= 200 && response < 300) {
				InputStream is = conn.getInputStream();
				text = IOUtil.readIt(is);
			} else {
				InputStream err = conn.getErrorStream();
				text = IOUtil.readIt(err);
			}
		} catch (IOException e) {
			text = e.getMessage();
			e.printStackTrace();
			try {
				int response = conn.getResponseCode();
				int fileLength = conn.getContentLength();
				MZLog.d(TAG, "(GET)The response is: " + response + " The file length is: " + fileLength);
				if (response >= 200 && response < 300) {
					InputStream is = conn.getInputStream();
					text = IOUtil.readIt(is);
				} else {
					InputStream err = conn.getErrorStream();
					text = IOUtil.readIt(err);
				}
			} catch (IOException e1) {
				if (e1 != null) {
					e1.printStackTrace();
				}
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		if (text == null) {
			text = "";
		}
		MZLog.d(TAG, text);
		return text;

	}


}
