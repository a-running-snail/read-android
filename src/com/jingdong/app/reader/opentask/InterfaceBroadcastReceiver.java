package com.jingdong.app.reader.opentask;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.activity.MainActivity;
import com.jingdong.app.reader.config.ITransKey;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.DataIntent;
import com.jingdong.app.reader.util.JSONArrayPoxy;
import com.jingdong.app.reader.util.JSONObjectProxy;
import com.jingdong.app.reader.util.MZLog;

public class InterfaceBroadcastReceiver extends BroadcastReceiver implements
		ITransKey {

	public static final String ACTION = "com.jdreader.interfaceBroadcastReceiver";
	public static final int MODULE_ID_HOME = 1;// 启动app
	public static final int MODULE_ID_DETAIL_PAGE = 100;// 打开书籍详情页
	public static final int MODULE_ID_BOOKSHELT = 28;// 前往书架
	public static final int MODULE_ID_DOWNLOAD_CHANGDU = 3202;// 下载畅读 type
																// 32/dlType 2
	public static final int MODULE_ID_DOWNLOAD_BUYED = 3201;// 下载已购 type
															// 32/dlType 1
	public static final int MODULE_ID_ME_HOME = 26;// 个人主页
	public static final int MODULE_ID_BOOKSTORE_HOME = 21;// 前往书架
	public static final int MODULE_ID_WEIXINE_EBOOKPAY =  10;
	public static int type = -1;

	@Override
	public void onReceive(Context context, Intent intent) {

		// 功能模块ID
		Command command = Command.createCommand(intent);
		if (0 == command.getModuleId()) {// 0代表不要执行
			return;
		}

		int moduleId = command.getModuleId();
		Bundle bundle = command.getOutBundle();

		switch (moduleId) {

		case MODULE_ID_HOME: {
			MZLog.d("wangguodong", "外部任务：###打开APP");
			Intent mianIntent = new Intent(context, MainActivity.class);
			mianIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mianIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			context.startActivity(mianIntent);
		}
			break;

		case MODULE_ID_BOOKSHELT: {
			MZLog.d("wangguodong", "外部任务：###打开书架");

			Intent launcherIntent = new Intent(context, LauncherActivity.class);
			launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launcherIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			launcherIntent
					.putExtra("TAB_INDEX", LauncherActivity.TAB_BOOKSHELF);
			context.startActivity(launcherIntent);
			break;
		}

		case MODULE_ID_BOOKSTORE_HOME: {
			MZLog.d("wangguodong", "外部任务：###打开书城");
			Intent launcherIntent = new Intent(context, LauncherActivity.class);
			launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launcherIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			launcherIntent.putExtra("TAB_INDEX", LauncherActivity.TAB_BOOKSHOP);
			context.startActivity(launcherIntent);

			break;
		}

		case MODULE_ID_ME_HOME: {
			MZLog.d("wangguodong", "外部任务：###打开个人主页");
			Intent launcherIntent = new Intent(context, LauncherActivity.class);
			launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launcherIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			launcherIntent
					.putExtra("TAB_INDEX", LauncherActivity.TAB_MY_CENTER);
			context.startActivity(launcherIntent);

			break;
		}

		case MODULE_ID_DETAIL_PAGE: {
			MZLog.d("wangguodong", "外部任务：###打开图书详情页");

			Intent launcherIntent = new Intent(context,
					BookInfoNewUIActivity.class);
			launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launcherIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			long bookid = bundle.getLong("bookid");
			launcherIntent.putExtra("bookid", bookid);
			context.startActivity(launcherIntent);

			break;
		}

		case MODULE_ID_DOWNLOAD_BUYED: {
			MZLog.d("wangguodong", "外部任务：###下载已购");

			Intent mianIntent = new Intent(context, MainActivity.class);
			mianIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mianIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			String key = DataIntent.creatKey();//
			DataIntent.put(key, command);
			mianIntent.putExtra(KEY, key);// (KEY, command);
			context.startActivity(mianIntent);
			break;
			
		}

		case MODULE_ID_DOWNLOAD_CHANGDU: {
			MZLog.d("wangguodong", "外部任务：###下载畅读");

			Intent mianIntent = new Intent(context, MainActivity.class);
			mianIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mianIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			String key = DataIntent.creatKey();//
			DataIntent.put(key, command);
			mianIntent.putExtra(KEY, key);// (KEY, command);
			context.startActivity(mianIntent);

			break;
		}
		
		case MODULE_ID_WEIXINE_EBOOKPAY:{
			Log.i("JD_Reader", "InterfaceBroadcastReceiver#onReceive::WXPay");
			// 电子书支付订单
			CommonUtil.toWeiXinClient(bundle.getString("payId"),bundle.getInt("moduleId"));
			break;
		}

		}

	}

	public static Intent createIntent(int moduleId, Bundle param) {
		Intent intent = new Intent(InterfaceBroadcastReceiver.ACTION);
		Bundle bundle = new Bundle();
		bundle.putInt("moduleId", moduleId);
		if (null != param) {
			Command.outBundleToBundle(param, bundle);
		}
		intent.putExtras(bundle);
		return intent;
	}

	public static class Command {

		private int moduleId = 0;
		private Bundle outBundle = new Bundle();

		private Command(int moduleId, Bundle outBundle) {
			this.moduleId = moduleId;
			this.outBundle = outBundle;
		}

		private Command(Uri data) {

			int inputType = -1;
			int dlType = -1;// 下载类型
			String inputParams = null;

			List<String> paramsList = data.getQueryParameters("params");
			String payId;
			int count = paramsList.size();
			if (count > 0) {
				inputParams = paramsList.get(0);
				MZLog.d("wangguodong", "=======######"+inputParams);
			}
			if (!TextUtils.isEmpty(inputParams)) {
				try {
					JSONObjectProxy jsonObjectProxy = new JSONObjectProxy(new JSONObject(inputParams));
					if(!TextUtils.isEmpty(jsonObjectProxy.getStringOrNull("type")))
						inputType = Integer.parseInt(jsonObjectProxy.getStringOrNull("type"));

					String dltypeString = jsonObjectProxy.getStringOrNull("dlType");
					if (null != dltypeString)
						dlType = Integer.parseInt(dltypeString);
					
					String userName = jsonObjectProxy.getStringOrNull("userName");
					if (!TextUtils.isEmpty(userName))
						outBundle.putString("userName",userName);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				if(!TextUtils.isEmpty(data.getQueryParameter("type")))
					inputType = Integer.parseInt(data.getQueryParameter("type"));
				String dltypeString = data.getQueryParameter("dlType");
				if (null != dltypeString)
					dlType = Integer.parseInt(dltypeString);
				
				String userName = data.getQueryParameter("userName");
				if (!TextUtils.isEmpty(userName))
					outBundle.putString("userName",userName);
			}

			MZLog.d("wangguodong", "type -->> " + inputType);
			MZLog.d("wangguodong", "dlType -->> " + dlType);

			if (dlType != -1) {
				inputType = inputType * 100 + dlType;
				MZLog.d("wangguodong", inputType+"$$$$$$$$$$");
			}

			if (MODULE_ID_HOME == inputType) {// 首页
				moduleId = MODULE_ID_HOME;
			} else if (MODULE_ID_BOOKSHELT == inputType) {// 书架
				moduleId = MODULE_ID_BOOKSHELT;
			} else if (MODULE_ID_ME_HOME == inputType) {// 个人主页
				moduleId = MODULE_ID_ME_HOME;
			} else if (MODULE_ID_BOOKSTORE_HOME == inputType) {// 书城主页
				moduleId = MODULE_ID_BOOKSTORE_HOME;
			} else if (MODULE_ID_DETAIL_PAGE == inputType) {// 商品详情页
				JSONObjectProxy jsonObjectProxy;
				try {
					jsonObjectProxy = new JSONObjectProxy(new JSONObject(inputParams));
					moduleId = MODULE_ID_DETAIL_PAGE;
					JSONArrayPoxy arrayPoxy = jsonObjectProxy.getJSONArrayOrNull("bookList");

					if (arrayPoxy != null && arrayPoxy.length() > 0) {
						outBundle.putLong("bookid",arrayPoxy.getJSONObjectOrNull(0).getLongOrNull("bookId"));
					} else {
						outBundle.putLong("bookid", 0);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (MODULE_ID_DOWNLOAD_CHANGDU == inputType) {// 畅读下载
				try {

					JSONObjectProxy temp = new JSONObjectProxy(new JSONObject(inputParams));
					JSONArrayPoxy arrayPoxy = temp.getJSONArrayOrNull("bookList");

					JSONObjectProxy jsonObjectProxy = null;
					if (arrayPoxy != null && arrayPoxy.length() > 0) {
						jsonObjectProxy = arrayPoxy.getJSONObject(0);
					}

					moduleId = MODULE_ID_DOWNLOAD_CHANGDU;
					if (jsonObjectProxy.getLongOrNull("bookId") != null) {
						outBundle.putLong("bookId",jsonObjectProxy.getLongOrNull("bookId"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy.getStringOrNull("picUrl"))) {
						outBundle.putString("picUrl",jsonObjectProxy.getStringOrNull("picUrl"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("bigPicUrl"))) {
						outBundle.putString("bigPicUrl",
								jsonObjectProxy.getStringOrNull("bigPicUrl"));
					}
					if (jsonObjectProxy.getLongOrNull("orderId") != null) {
						outBundle.putLong("orderId",
								jsonObjectProxy.getLongOrNull("orderId"));
					}
					;
					if (jsonObjectProxy.getIntOrNull("bookType") != null) {
						outBundle.putInt("bookType",
								jsonObjectProxy.getIntOrNull("bookType"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("format"))) {
						outBundle.putString("format",
								jsonObjectProxy.getStringOrNull("format"));
					}
					if (jsonObjectProxy.getIntOrNull("channelIDKey") != null) {
						outBundle.putInt("channelIDKey",
								jsonObjectProxy.getIntOrNull("channelIDKey"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("userName"))) {
						outBundle.putString("userName",
								jsonObjectProxy.getStringOrNull("userName"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("userPsw"))) {
						outBundle.putString("userPsw",
								jsonObjectProxy.getStringOrNull("userPsw"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("cookies"))) {
						outBundle.putString("cookies",
								jsonObjectProxy.getStringOrNull("cookies"));
					}
					if (jsonObjectProxy.getBooleanOrNull("isFreeBook") != null) {
						outBundle.putBoolean("isFreeBook",
								jsonObjectProxy.getBooleanOrNull("isFreeBook"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("bookName"))) {
						outBundle.putString("bookName",
								jsonObjectProxy.getStringOrNull("bookName"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("author"))) {
						outBundle.putString("author",
								jsonObjectProxy.getStringOrNull("author"));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else if (MODULE_ID_DOWNLOAD_BUYED == inputType) {// 已购下载

				try {
					JSONObjectProxy temp = new JSONObjectProxy(new JSONObject(
							inputParams));

					JSONArrayPoxy arrayPoxy = temp
							.getJSONArrayOrNull("bookList");

					JSONObjectProxy jsonObjectProxy = null;
					if (arrayPoxy != null && arrayPoxy.length() > 0) {
						jsonObjectProxy = arrayPoxy.getJSONObject(0);
						MZLog.d("wangguodong", "$$$$#######"+jsonObjectProxy.toString());
					}
					moduleId = MODULE_ID_DOWNLOAD_BUYED;
					if (jsonObjectProxy.getLongOrNull("bookId") != null) {
						outBundle.putLong("bookId",
								jsonObjectProxy.getLongOrNull("bookId"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("picUrl"))) {
						outBundle.putString("picUrl",
								jsonObjectProxy.getStringOrNull("picUrl"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("bigPicUrl"))) {
						outBundle.putString("bigPicUrl",
								jsonObjectProxy.getStringOrNull("bigPicUrl"));
					}
					if (jsonObjectProxy.getLongOrNull("orderId") != null) {
						outBundle.putLong("orderId",
								jsonObjectProxy.getLongOrNull("orderId"));
					}
					;
					if (jsonObjectProxy.getIntOrNull("bookType") != null) {
						outBundle.putInt("bookType",
								jsonObjectProxy.getIntOrNull("bookType"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("format"))) {
						outBundle.putString("format",
								jsonObjectProxy.getStringOrNull("format"));
					}
					if (jsonObjectProxy.getIntOrNull("channelIDKey") != null) {
						outBundle.putInt("channelIDKey",
								jsonObjectProxy.getIntOrNull("channelIDKey"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("userName"))) {
						outBundle.putString("userName",
								jsonObjectProxy.getStringOrNull("userName"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("userPsw"))) {
						outBundle.putString("userPsw",
								jsonObjectProxy.getStringOrNull("userPsw"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("cookies"))) {
						outBundle.putString("cookies",
								jsonObjectProxy.getStringOrNull("cookies"));
					}
					if (jsonObjectProxy.getBooleanOrNull("isFreeBook") != null) {
						outBundle.putBoolean("isFreeBook",
								jsonObjectProxy.getBooleanOrNull("isFreeBook"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("bookName"))) {
						outBundle.putString("bookName",
								jsonObjectProxy.getStringOrNull("bookName"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("author"))) {
						outBundle.putString("author",
								jsonObjectProxy.getStringOrNull("author"));
					}
					if (!TextUtils.isEmpty(jsonObjectProxy
							.getStringOrNull("bookSize"))) {
						outBundle.putString("bookSize",
								jsonObjectProxy.getStringOrNull("bookSize"));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}else if (MODULE_ID_WEIXINE_EBOOKPAY ==  inputType) {
				JSONObjectProxy jsonObjectProxy;
				try {
					jsonObjectProxy = new JSONObjectProxy(new JSONObject(
							inputParams));
					payId = jsonObjectProxy.getStringOrNull("payId");
					if (!TextUtils.isEmpty(payId)) {
						moduleId = MODULE_ID_WEIXINE_EBOOKPAY;
						outBundle.putString("payId", payId);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}else {
				moduleId = MODULE_ID_HOME;
			}

		}

		public int getModuleId() {
			return moduleId;
		}

		public Bundle getOutBundle() {
			return outBundle;
		}

		public Bundle getBundle() {
			Bundle bundle = new Bundle();
			bundle.putInt("moduleId", moduleId);
			outBundleToBundle(outBundle, bundle);
			return bundle;
		}

		public static Command createCommand(Intent intent) {

			if(intent!=null){
				Uri data = intent.getData();
				if (null != data) {
					return new Command(data);
				}
				Bundle bundle = intent.getExtras();
				if (null != bundle) {
					int moduleId = bundle.getInt("moduleId", 0);
					Bundle outBundle = new Bundle();
					for (String key : bundle.keySet()) {
						if (key.startsWith("param_")) {
							Object object = bundle.get(key);
							if (object instanceof String) {
								String[] split = key.split("_");
								outBundle.putString(split[1], (String) object);
							} else if (object instanceof Integer) {
								outBundle.putInt(key.split("_")[1],
										(Integer) object);
							} else if (object instanceof Long) {
								outBundle.putLong(key.split("_")[1], (Long) object);
							} else if (object instanceof Boolean) {
								outBundle.putBoolean(key.split("_")[1],
										(Boolean) object);
							}
						}
					}
					if (0 != moduleId) {
						return new Command(moduleId, outBundle);
					}
				}
				return null;
			}
			return null;
			
		}

		public static void outBundleToBundle(Bundle outBundle, Bundle bundle) {
			for (String key : outBundle.keySet()) {
				Object object = outBundle.get(key);
				if (object instanceof String) {
					bundle.putString("param_" + key, (String) object);
				} else if (object instanceof Integer) {
					bundle.putInt("param_" + key, (Integer) object);
				} else if (object instanceof Long) {
					bundle.putLong("param_" + key, (Long) object);
				} else if (object instanceof Boolean) {
					bundle.putBoolean("param_" + key, (Boolean) object);
				}
			}
		}

	}

}
