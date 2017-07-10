package com.jingdong.app.reader.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bob.util.ShowTools;
import com.jingdong.app.reader.entity.WeiXinEntity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.HttpGroup.HttpError;
import com.jingdong.app.reader.util.HttpGroup.HttpGroupSetting;
import com.jingdong.app.reader.util.HttpGroup.HttpResponse;
import com.jingdong.app.reader.util.HttpGroup.HttpSetting;
import com.jingdong.app.reader.util.HttpGroup.HttpSettingParams;

public final class CommonUtil {

	private static final String TAG = "CommonUtil";
	public static String weixinPayId = "";
	public static int businessType = -1;

	/**
	 * 取得DeviceId
	 */
	public static String getDeviceId() {
		try {
			if(isHaveDeviceIdGranted()){
				TelephonyManager tm = (TelephonyManager) MZBookApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
				return tm==null?null:tm.getDeviceId();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	/**是否有获取设备号权限
	 * @return
	 */
	public static boolean isHaveDeviceIdGranted(){
        return isHaveGranted("android.permission.READ_PHONE_STATE");
	}
	/**是否有wifi联网权限
	 * @return
	 */
	public static boolean isHaveWifiConnetGranted(){
		String pesmission="android.permission.ACCESS_WIFI_STATE";
		return isHaveGranted(pesmission);
	}
	/**是否有权限
	 * @return
	 */
	private static boolean isHaveGranted(String permissionStr){
		
		PackageManager pm = MZBookApplication.getInstance().getPackageManager();  
		boolean permission = (PackageManager.PERMISSION_GRANTED ==   
				pm.checkPermission(permissionStr, "com.jingdong.app.reader")); 
		return permission;
	}
	/**
	 * 获取主配置文件
	 */
	public static SharedPreferences getJdSharedPreferences() {
		SharedPreferences sharedPreferences = MZBookApplication.getInstance()//
				.getSharedPreferences("jdAndroidClient", Context.MODE_PRIVATE);
		return sharedPreferences;
	}

	public static void saveTOJdSharedPreferences(String key, String value) {
		if (!TextUtils.isEmpty(key)) {
			SharedPreferences sharedPreferences = CommonUtil
					.getJdSharedPreferences();
			sharedPreferences.edit().putString(key, value).commit();
		}
	}

	public static void saveBoolean(String key, boolean value) {
		if (!TextUtils.isEmpty(key)) {
			SharedPreferences sharedPreferences = CommonUtil
					.getJdSharedPreferences();
			sharedPreferences.edit().putBoolean(key, value).commit();
		}
	}



	public static String getString(String key) {
		String value = null;
		if (key != null && getJdSharedPreferences().contains(key)) {
			value = getJdSharedPreferences().getString(key, null);
		}
		return value;
	}

	/**
	 * 取得Mac后，执行的函数
	 */
	public interface MacAddressListener {

		void setMacAddress(String macAddress);

	}

	/**
	 * 判断intent是否有效
	 */
	public static boolean isIntentAvailable(Intent intent) {
		PackageManager packageManager = MZBookApplication.getInstance()
				.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		boolean isSupter = false;
		if (list != null && list.size() > 0) {
			isSupter = true;
		}

		return isSupter;
	}

	/**
	 * 微信支付
	 * @param payId
	 * @param type
	 */
	public static void toWeiXinClient(final String payId, final int type) {

		weixinPayId = payId;
		
		// 准备httpGroup
		HttpGroupSetting httpGroupSetting = new HttpGroupSetting();
		httpGroupSetting.setType(HttpGroupSetting.TYPE_JSON);
		httpGroupSetting.setMyActivity(MZBookApplication.getInstance()
				.getCurrentMyActivity());
		final HttpGroup httpGroup = new HttpGroup.HttpGroupaAsynPool(
				httpGroupSetting);

		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setPost(true);
		httpSetting.setUrl(URLText.JD_PAY_URL);

		// httpSetting.setUrl("http://paybeta.m.360buy.com/index.action");

		httpSetting.setFunctionId("weixinPay");
		httpSetting.putJsonParam("appId", "jdpay_ebook");
		httpSetting.putJsonParam("payId", payId);
		httpSetting.setListener(new HttpGroup.OnCommonListener() {
			// httpSetting.setListener(new HttpGroup.OnCommonListener() {
			@Override
			public void onReady(HttpSettingParams httpSettingParams) {
				// httpSettingParams.putJsonParam("tokenKey", tokenKey);
			}

			@Override
			public void onError(HttpError error) {
				ShowTools.toastInThread("网络异常");
			}

			@Override
			public void onEnd(HttpResponse httpResponse) {
				JSONObjectProxy obj = httpResponse.getJSONObject();
				try {
					String message = obj.getStringOrNull("message");
					JSONObjectProxy json = obj.getJSONObjectOrNull("body");
					MZLog.d("JD_Reader", "CommonUtil#toWeiXinClient::onEnd-->body:"+json.toString());

					WeiXinEntity weiXinEntity = new WeiXinEntity(json);
					WeiXinUtil.setWeiXinInfo(weiXinEntity);
					WeiXinUtil.doWeiXinPay(weiXinEntity);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		httpGroup.add(httpSetting);
	}

	public interface BrowserUrlListener {
		void onGenToken(String token);

		void onComplete();
	}

    /**
     * 取得WIFI MAC地址（新）
     */
    public synchronized static void getLocalMacAddress(
            final MacAddressListener listener) {


        final WifiManager wifi = (WifiManager) MZBookApplication.getInstance()
                .getSystemService(Context.WIFI_SERVICE);
        String macAddress=null;
       if(isHaveWifiConnetGranted()){
    	   
    	   final WifiInfo info = wifi.getConnectionInfo();
    	   macAddress = info.getMacAddress();
       }
        

        if (null != macAddress) {
            listener.setMacAddress(macAddress);
        } else {
            final Object waiter = new Object();

            // 线程
            Thread thread = new Thread() {

                @Override
                public void run() {
                	try {

            			if(isHaveWifiConnetGranted()){
            				wifi.setWifiEnabled(true);// 打开WIFI
                			
                			String macAddress = null;
                			int times = 0;
                			while (null == (macAddress = wifi.getConnectionInfo()
                					.getMacAddress()) && times < 60) {
                				times++;
                				synchronized (waiter) {
                					try {
                						
                						waiter.wait(500);
                						
                					} catch (InterruptedException e) {
                						e.printStackTrace();
                					}
                				}
                			}
                			
                			wifi.setWifiEnabled(false);// 关闭WIFI
                			
                			listener.setMacAddress(macAddress);
                		
            			}else{
            				listener.setMacAddress(null);
            			}
            			
						
					} catch (Exception e) {
						e.printStackTrace();
					}
                }	

            };
            thread.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
            
            ExecutorService exec = Executors.newFixedThreadPool(1);
            exec.execute(thread);

        }

    }
    
    static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{

    	@Override
		public void uncaughtException(Thread t, Throwable e) {
			System.err.println(t.getName() + " : " + e.getMessage());
		}
    	
    }
    
    

    public static boolean getBoolean(String key) {
        boolean value = true;
        if (!TextUtils.isEmpty(key)) {
            SharedPreferences sharedPreferences = CommonUtil
                    .getJdSharedPreferences();
            if (sharedPreferences.contains(key)) {
                value = sharedPreferences.getBoolean(key, true);
            }
        }
        return value;
    }
    

    public static void queryBrowserUrl(Context context,final BrowserUrlListener listener) {
    	
    	WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, 
    			RequestParamsPool.getTokenParams(), true,new MyAsyncHttpResponseHandler(context) {
					
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						
					}
					
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String result = new String(responseBody);
						try {
							JSONObject jsonObj = new JSONObject(result);
							JSONObjectProxy obj = new JSONObjectProxy(jsonObj);
							String token = obj.getStringOrNull("tokenKey");
							String code = obj.getStringOrNull("code");
							if (null == token) {
								return;
							}
							String url = obj.getStringOrNull("url");
							if (null == url) {
								return;
							}
							listener.onGenToken(token);
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
    }
    public static void queryBrowserUrl(Context context,final BrowserUrlListener listener,String id) {
    	
    	WebRequestHelper.get(URLText.JD_BOOK_ORDER_URL, 
    			RequestParamsPool.getTokenParams(id), true,new MyAsyncHttpResponseHandler(context) {
    		
    		@Override
    		public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
    			
    		}
    		
    		@Override
    		public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
    			String result = new String(responseBody);
    			try {
    				JSONObject jsonObj = new JSONObject(result);
    				JSONObjectProxy obj = new JSONObjectProxy(jsonObj);
    				String token = obj.getStringOrNull("tokenKey");
    				String code = obj.getStringOrNull("code");
    				String url = obj.getStringOrNull("payUrl");
    				if (null == url) {
    					return;
    				}
    				listener.onGenToken(url);
    				
    			} catch (JSONException e) {
    				e.printStackTrace();
    			}
    		}
    	});
    }

    public static String getPropertiesValue(String key) {
		InputStream asset_is = null;
		try {
			asset_is = MZBookApplication.getInstance().getAssets()
					.open("cpa.properties");
			Properties properties = new Properties();
			properties.load(asset_is);

			return properties.getProperty(key);
			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (asset_is != null) {
					asset_is.close();
					asset_is = null;
				}
			} catch (Exception e2) {
			}
		}
		return null;
	}
 
    /**
     * 
     * @Title: toggleSoftInput
     * @Description: 如果输入法在窗口上已经显示，则隐藏，反之则显示
     * @param @param context
     * @return void
     * @throws
     */
    public static void toggleSoftInput(Context context) {
    	//1.得到InputMethodManager对象
    	InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    	//2.调用toggleSoftInput方法，实现切换显示软键盘的功能。
    	imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
    
    
    public static void writeFile(String filename, String msg) {
    	try {
    	FileOutputStream fos = new FileOutputStream(filename, true);
    	fos.write(msg.getBytes());
    	fos.close();
    	} catch (Exception e) {
    	e.printStackTrace();
    	}
    	}
    
}
