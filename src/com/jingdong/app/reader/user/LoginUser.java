package com.jingdong.app.reader.user;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.config.Constant;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.RsaEncoder;

/**
 * Copyright 2010 Jingdong Android Mobile Application
 * 
 * @author: hechel Time: 下午01:26:47 Name: LoginUser Description:登录的用户
 */
public class LoginUser {
	public String userName;
	public String psw;
	public String pin;

	public String token;
	public boolean isRemember;
	public boolean isAutoLogin;

	public final static int USERSTATE_IN = 1;
	public final static int USERSTATE_OUT = 0;
	private static int sLoginState = USERSTATE_OUT;// 0->未登录，1->登录，默认状态是未登录
	private static LoginUser instance;

	public static void setUserState(int state) {
		LoginUser.sLoginState = state;
		SharedPreferences sp = MZBookApplication.getInstance()
				.getSharedPreferences(Constant.JD_SHARE_PREFERENCE,
						Context.MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.putInt(Constant.LOGIN_STATE, state);
		String cookie = WebRequestHelper.getCookies();
		if (cookie!=null)
			cookie = OlineDesUtils.encrypt(cookie);
		ed.putString(Constant.LOGIN_COOKIE,cookie);
		ed.commit();
		
	}

	public static void setLoginState(String pin, String cookie, int loginState) {
		sLoginState = loginState;
		SharedPreferences sp = MZBookApplication.getInstance()
				.getSharedPreferences(Constant.JD_SHARE_PREFERENCE,
						Context.MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.putInt(Constant.LOGIN_STATE, loginState);
		if (cookie!=null)
			cookie = OlineDesUtils.encrypt(cookie);
		ed.putString(Constant.LOGIN_COOKIE, cookie);
		ed.commit();
		// HttpGroup.setCookies(cookie);
		getInstance().pin = pin;
	}

	public static void setLoginState(String loginName, String psw, String pin,
			boolean issave) {
		LoginUser loginUser = getInstance();
		loginUser.userName = loginName;
		loginUser.psw = psw;
		loginUser.pin = pin;
		if (issave) {
			saveLoginState(loginName, psw, pin);
		}
	}

	public static void setLoginState(String loginName, String psw, String pin,
			boolean rememberFlag, boolean autologin, boolean issave) {
		LoginUser loginUser = getInstance();
		setLoginState(loginName, psw, pin, issave);
		loginUser.isRemember = rememberFlag;
		loginUser.isAutoLogin = autologin;
		if (issave) {
			saveLoginState(loginName, psw, pin, rememberFlag, autologin);
		}
	}

	private static void saveLoginState(String loginName, String psw, String pin) {
		SharedPreferences sp = MZBookApplication.getInstance()
				.getSharedPreferences(Constant.JD_SHARE_PREFERENCE,
						Context.MODE_PRIVATE);
		Editor ed = sp.edit();
		if (loginName == null) {
			loginName = "";
		}
		if (psw == null) {
			psw = "";
		}
		if (pin == null) {
			pin = "";
		}
		ed.putString(Constant.REMEMBER_NAME, loginName);
		ed.putString(Constant.REMEMBER_PASSWORD, psw);
		ed.putString(Constant.REMEMBER_PIN, pin);
		ed.commit();
	}

	private static void saveLoginState(String loginName, String psw,
			String pin, boolean rememberFlag, boolean autologin) {

		saveLoginState(loginName, psw, pin);

		SharedPreferences sp = MZBookApplication.getInstance()
				.getSharedPreferences(Constant.JD_SHARE_PREFERENCE,
						Context.MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.putBoolean(Constant.REMEMBER_FLAG, rememberFlag);
		ed.putBoolean(Constant.AUTO_LOGIN, autologin);
		ed.commit();
	}

	public static LoginUser getInstance() {
		if (instance == null) {
			SharedPreferences sp = MZBookApplication.getInstance()
					.getSharedPreferences(Constant.JD_SHARE_PREFERENCE,
							Context.MODE_PRIVATE);
			LoginUser user = new LoginUser();
			
			//由于配置文件key更换，需要 
			Editor ed = sp.edit();
			if( !sp.getString("userName", "").equals("")){
				ed.putString(Constant.REMEMBER_NAME, sp.getString("userName", ""));
				ed.remove("userName");
			}
			if( !sp.getString("pin", "").equals("")){
				ed.putString(Constant.REMEMBER_PIN, sp.getString("pin", ""));
				ed.remove("pin");
			}
			if( !sp.getString("password", "").equals("")){
				ed.putString(Constant.REMEMBER_PASSWORD, sp.getString("password", ""));
				ed.remove("password");
			}
			ed.commit();
			
			sp = MZBookApplication.getInstance()
					.getSharedPreferences(Constant.JD_SHARE_PREFERENCE,
							Context.MODE_PRIVATE);
			user.userName = sp.getString(Constant.REMEMBER_NAME, "");
			user.psw = sp.getString(Constant.REMEMBER_PASSWORD, "");
			user.pin = sp.getString(Constant.REMEMBER_PIN, "");
			user.isRemember = sp.getBoolean(Constant.REMEMBER_FLAG, false);
			user.isAutoLogin = sp.getBoolean(Constant.AUTO_LOGIN, false);
			String cookie = sp.getString(Constant.LOGIN_COOKIE, null);
			if (cookie!=null)
				cookie = OlineDesUtils.decrypt(cookie);
			user.sLoginState = sp.getInt(Constant.LOGIN_STATE, USERSTATE_OUT);
			WebRequestHelper.setCookies(cookie);
			instance = user;
		}
		return instance;
	}

	public static String getpin() {
		LoginUser user = getInstance();
		return user.pin;
	}

	public static String getpsw() {
		LoginUser user = getInstance();
		return user.psw;
	}

	public static String getUserName() {
		LoginUser user = getInstance();
		return user.userName;
	}

	public static boolean isAutoLogin() {
		LoginUser user = getInstance();
		return user.isAutoLogin;
	}

	/*
	 * 判断是否登录 false 没有登录
	 */
	public static boolean isLogin() {
		boolean isLogin = false;
		if (sLoginState == USERSTATE_IN) {
			isLogin = true;
		}
		// }
		return isLogin;
	}

	/**
	 * @author hesong
	 * 
	 *         Time: 2010-12-27 下午14:46:34
	 * 
	 *         Name: getRememberedUser
	 * 
	 * @return: null
	 * 
	 *          Description: 清空系统记住的用户名和密码
	 */
	private static void clearRememberFile(boolean username, boolean pin,
			boolean password, boolean remember, boolean autologin) {
		Editor edit = CommonUtil.getJdSharedPreferences().edit();
		if (username)
			edit.remove(Constant.REMEMBER_NAME);
		if (pin)
			edit.remove(Constant.REMEMBER_PIN);
		if (password)
			edit.remove(Constant.REMEMBER_PASSWORD);
		if (remember)
			edit.remove(Constant.REMEMBER_FLAG);
		if (autologin)
			edit.remove(Constant.AUTO_LOGIN);
		edit.remove(Constant.LOGIN_STATE);
		edit.remove(Constant.LOGIN_COOKIE);
		edit.commit();
	}

	public static void clearAllRememberFile() {
		clearRememberFile(true, true, true, true, true);
	}

	public static void logOut(boolean isCleanFile) {
		if (isCleanFile) {
			clearRememberFile(false, true, true, true, true);// 注销登录保留userName
		} else {
			clearRememberFile(false, false, false, false, false);
		}
		WebRequestHelper.setCookies(null);
		instance = null;
		sLoginState = USERSTATE_OUT;
		RsaEncoder.setEncodeEntity(null);

	}

	// 完成用户登录完成 书架数据扫描操作
	public static void scanDocumentToBookShelf(Context context, String userid) {
		//从数据库中读取数据
		List<Integer> ids = MZBookDatabase.instance.scanLocalDocument(userid);
		if (ids == null || ids.size() == 0)
			return;

		MZLog.d("wangguodong", "扫描到书架上还木有添加的书籍" + ids.size() + "本");
		for (int i = 0; i < ids.size(); i++) {
			// 1 表示document 0 ebook -1 文件夹
			MZBookDatabase.instance.saveToBookShelf(ids.get(i),System.currentTimeMillis(), 1, userid);
		}
		LocalUserSetting.saveLoginScan(context, true);
	}

	// 用户解绑 去除数据
	public static void unBindUserAndClearEbookData() {
		new UnBindTask().execute();
	}

	static class UnBindTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {

			List<LocalBook> list = LocalBook.getLocalBookList("user_name=?",
					new String[] { LoginUser.getpin() });
			List<Integer> ids =new ArrayList<Integer>();
			if (list == null) {
				MZLog.d("wangguodong", "该用户木有下载书籍。。。");
				return true;
			}
			for (int i = 0; i < list.size(); i++) {
				String pathString = list.get(i).dir;
				if (!TextUtils.isEmpty(pathString)) {
					try {
						IOUtil.deleteFile(new File(pathString));
					} catch (Exception e) {
					}
				}
				ids.add(list.get(i)._id);
			}
			if(ids==null||ids.size()==0)
				return true;
			MZBookDatabase.instance.clearEbookDataByUserid(ids);
			Intent intent = new Intent("com.mzread.action.refresh");
			LocalBroadcastManager.getInstance(MZBookApplication.getContext()).sendBroadcast(intent);
			return true;
		}

	}

}
