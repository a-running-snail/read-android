package com.jingdong.app.reader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.jingdong.app.reader.R;

import com.jingdong.app.reader.client.OnDownloadListener;
import com.jingdong.app.reader.client.RequestEntry;
import com.jingdong.app.reader.client.ServiceClient;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.view.JdOptionDialog;
import com.jingdong.app.reader.view.TestViewProgressBar;

public class ApplicationUpgradeHelper {

	public final static int INSTALL_REQUEST_CODE = 1001;

	private final static int MUST_UPDATE = 1;// 必须升级
	// private final static int NEED_UPDATE = 2;// 需要升级
	// private final static int NO_UPDATE = 0;// 不需要升级
	private static int upgradeState;
	private static ApplicationUpgradeHelper instance;
	private TestViewProgressBar mProgressBar;
	private static int count;
	private JdOptionDialog concelDialog;// 取消升级对话框
	private JdOptionDialog myJdOptionDialog;// 升级对话框
	private Activity tempActivity;
	private Handler handler = new Handler();;
	private StatusCtrl statusCtrl = new StatusCtrl();
	
	private Runnable cancelRunable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			statusCtrl.stopDownloadTasks();
		}
	};

	/**
	 * 创建实例
	 * 
	 * @return ApplicationUpgradeHelper
	 */
	public synchronized static ApplicationUpgradeHelper getInstance() {
		if (null == instance) {
			instance = new ApplicationUpgradeHelper();
		}
		return instance;
	}

	/**
	 * 尝试升级
	 */
	public void tryUpgrade(final Context myActivity, String remoteVersion,
			String downloadUrl, final String description,final boolean isGo91Mark,
			final Runnable runnable) {
		MZLog.d("cj", "tryUpgrade::remoteVersion="+remoteVersion+",description="+description);
		if(!Configuration.getBooleanProperty(Configuration.APPLICATION_UPGRADE)){
			if (runnable != null)
				post(runnable,myActivity);
			return;
		}
		if (Log.D) {
			Log.d("Temp", "tryUpgrade() -->> ");
		}

		final Context mMyActivity = myActivity;
		if(tempActivity==null){
		tempActivity=(Activity) myActivity;
		}
		// String mRemoteVersion = remoteVersion;
		// String mLocalVersion = localVersion;
		
		final String mDownloadUrl = downloadUrl;

		// 升级对话框
		final JdOptionDialog.Builder alertDialogBuilder = new JdOptionDialog.Builder(mMyActivity);
		alertDialogBuilder.setTitle(mMyActivity.getString(R.string.appNewVersion) + remoteVersion);
		alertDialogBuilder.setPositiveButton(R.string.upgrade_app,
				new JdOptionDialog.OnClickListener() {
					@Override
					public void onClick(JdOptionDialog dialog, int which) {
						switch (which) {
						case JdOptionDialog.JDOPTONDIALOG_POS_BUTTON:// 左按钮点击事件
							if (Log.D) {
								Log.d("Temp", "onClick() BUTTON_POSITIVE -->> ");
							}
					
							mProgressBar = myJdOptionDialog.mProgress;
							if (myJdOptionDialog != null) {
								myJdOptionDialog.mPosBt.setVisibility(View.GONE);
								myJdOptionDialog.mNegBt.setVisibility(View.GONE);
								mProgressBar.setVisibility(View.VISIBLE);
							}

							ThreadUtil.runInThread(new Runnable() {
								@Override
								public void run() {
									Looper.prepare();
									startAppDownload(mMyActivity,myJdOptionDialog, statusCtrl,mDownloadUrl);
									Looper.loop();
								}
							});
							
							if (runnable != null)
								post(runnable,mMyActivity);
							return;
						case JdOptionDialog.JDOPTONDIALOG_NEG_BUTTON:// 中按钮点击事件
							if (Log.D) {
								Log.d("Temp", "onClick() BUTTON_NEGATIVE -->> ");
							}
							if (upgradeState == MUST_UPDATE) {
								// if (null != httpRequest) {
								// httpRequest.stop();
								// }
								dialog.dismiss();
								exitAll();
							} else {
								// c = true;
								// if (null != httpRequest) {
								// httpRequest.stop();
								// }
							}
							if (runnable != null)
								post(runnable,mMyActivity);
							return;
						}
					}
				});
		alertDialogBuilder.setNegativeButton(R.string.cancel,
				new JdOptionDialog.OnClickListener() {

					@Override
					public void onClick(JdOptionDialog dialog, int which) {
						int cancle_number = SettingUtils.getInstance().getInt("cancle_number", 0);
						cancle_number++;
						//升级提示取消三次后一周内不在提示
						if (cancle_number >= 3) {
							SettingUtils.getInstance().putLong("cancle_time", System.currentTimeMillis());
						}
						SettingUtils.getInstance().putInt("cancle_number", cancle_number);
						dialog.dismiss();
						if (runnable != null)
							post(runnable,mMyActivity);
					}
				});
		if(isGo91Mark){
			alertDialogBuilder.setMidlleButton(R.string.go91Mark, new JdOptionDialog.OnClickListener() {
				@Override
				public void onClick(JdOptionDialog dialog, int which) {
					 dialog.dismiss();
					 ContextCheckUtil.enterDetail(myActivity,myActivity.getPackageName(),true);
				}
			});
		}
		// 后退时弹出对话框
		alertDialogBuilder
				.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						if(keyCode==KeyEvent.KEYCODE_BACK){
							if (statusCtrl.status == statusCtrl.DOWNING) {
								if(tempActivity!=mMyActivity)
								{
									tempActivity=(Activity) mMyActivity;
									concelDialog =null	;
								}
								if (concelDialog == null) {
									concelDialog = getConcelDialog(mMyActivity,
											false);
								}
								concelDialog.show();
							}
							return true;
						}
						return false;
					}
				});

		post(new Runnable() {
			@Override
			public void run() {
//				String text = mMyActivity.getString(
//						R.string.software_need_update_msg);
				alertDialogBuilder.setMessage("\n目前有新版本，是否升级？\n\n升级改动：\n"
						+ description + "\n\n");

				myJdOptionDialog = alertDialogBuilder.show();
				myJdOptionDialog.setPosDismissBySelf(true);

			}
		},mMyActivity);

	}

	public void tryMustUpgrade(final Context myActivity, String remoteVersion,
			String downloadUrl, final String description,boolean isGo91Mark,
			final Runnable runnable) {

		if (Log.D) {
			Log.d("Temp", "tryUpgrade() -->> ");
		}

		final Context mMyActivity = myActivity;
		// String mRemoteVersion = remoteVersion;
		// String mLocalVersion = localVersion;
		final String mDownloadUrl = downloadUrl;

		// 升级对话框
		final JdOptionDialog.Builder alertDialogBuilder = new JdOptionDialog.Builder(
				mMyActivity);
		alertDialogBuilder.setTitle(mMyActivity
				.getString(R.string.appNewVersion) + remoteVersion);
		alertDialogBuilder.setPositiveButton(R.string.upgrade_app,
				new JdOptionDialog.OnClickListener() {
					@Override
					public void onClick(JdOptionDialog dialog, int which) {
					
						// Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						// .parse(mDownloadUrl));

						// mMyActivity.startActivity(intent);
						// intent.setFlags()
						myJdOptionDialog = alertDialogBuilder
								.getJdOptionDialog();
						mProgressBar = myJdOptionDialog.mProgress;
						if (myJdOptionDialog != null) {
							myJdOptionDialog.mPosBt.setVisibility(View.GONE);
							myJdOptionDialog.mNegBt.setVisibility(View.GONE);
							mProgressBar.setVisibility(View.VISIBLE);
						}

						ThreadUtil.runInThread(new Runnable() {

							@Override
							public void run() {

								startAppDownload(mMyActivity, myJdOptionDialog,
										statusCtrl, mDownloadUrl);

							}
						});
						// if (CommonUtil.isIntentAvailable(intent))
						// mMyActivity.startActivity(intent);
						// else
						// ShowTools.toastLong("升级地址无效！");
						if (runnable != null)
							post(runnable,mMyActivity);
						// exitReader();
						// download(mMyActivity.getHttpGroupaAsynPool());
					}
				});
		alertDialogBuilder.setNegativeButton(R.string.exit,
				new JdOptionDialog.OnClickListener() {
					@Override
					public void onClick(JdOptionDialog dialog, int which) {
						dialog.dismiss();
						if (runnable != null)
							post(runnable,mMyActivity);
						exitReader();
					}
				});
		if(isGo91Mark){
			alertDialogBuilder.setMidlleButton(R.string.go91Mark, new JdOptionDialog.OnClickListener() {
				@Override
				public void onClick(JdOptionDialog dialog, int which) {
					 dialog.dismiss();
					 ContextCheckUtil.enterDetail(myActivity,myActivity.getPackageName(),true);
					 exitReader();
				}
			});
		}
		// 禁止后退
		alertDialogBuilder
				.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							concelDialog = null;
							if (statusCtrl.status == statusCtrl.DOWNING) {
								if (concelDialog == null) {
									concelDialog = getConcelDialog(mMyActivity,
											true);
								}
								concelDialog.show();

							}
							return true;
						}
						return false;
					}

				});
		post(new Runnable() {
			@Override
			public void run() {
				String text = mMyActivity.getString(
						R.string.software_must_update_msg);
				alertDialogBuilder.setMessage(text + "\n\n升级改动：\n"
						+ description);
				myJdOptionDialog = alertDialogBuilder.show();
				myJdOptionDialog.setPosDismissBySelf(true);
			}
		},mMyActivity);
	}

	public void startAppDownload(final Context activity,final JdOptionDialog mpDialog, StatusCtrl statusCtrl, String url) {
		
		statusCtrl.status = StatusCtrl.DOWNING;
		OnDownloadListener onDownloadListener = new OnDownloadListener() {
			@Override
			public void onprogress(final long progress, final long max) {
				post(new Runnable() {
					@Override
					public void run() {
						mProgressBar.setMax((int) max);
						mProgressBar.setProgress((int) progress);
					}
				},activity);
				// downloadListener.onprogress(progress, max)
			}

			@Override
			public void onDownloadCompleted(RequestEntry requestEntry) {

			}
		};
		// HashMap<String, String> result = new HashMap<String, String>();
		String installPath = getDownloadTask(url, onDownloadListener,statusCtrl);
		mpDialog.dismiss();
		if (installPath == null) {
			ToastUtil.showToastInThread(activity.getResources().getString(R.string.download_err), Toast.LENGTH_SHORT);
			return;
		} else {
           if(statusCtrl.status==statusCtrl.DOWNED)
        	   APKUtil.ApkInstall(activity, installPath);
		}
	}

	private String getDownloadTask(String url,OnDownloadListener onDownloadListener, StatusCtrl statusCtrl) {
		RequestEntry requestEntry = null;
		String installPath = null;
		try {
			HttpUriRequest request = new HttpGet(url+"?");
			requestEntry = new RequestEntry(RequestEntry.TYPE_FILE, request);
			statusCtrl.setRequestEntry(requestEntry);
			requestEntry._type = RequestEntry.TYPE_FILE;
			requestEntry._downloadListener = onDownloadListener;
			FileGuider savePath = new FileGuider(FileGuider.SPACE_PRIORITY_EXTERNAL);
			savePath.setChildDirName("update");
			String fileName ="lebook.apk";
			savePath.setImmutable(true);
			savePath.setFileName(fileName);
			requestEntry.fileGuider = savePath;
			File file = new File(savePath.getFilePath());
			if (file.exists()) {
				file.delete();
			}
			FileInputStream fis = null;
			if (file != null && file.exists()) {
				fis = new FileInputStream(file);
			}
			if (fis != null && fis.available() > 10) {
				requestEntry.start = fis.available();
			}
			if (requestEntry.start > 0) {
				request.addHeader("Range", "bytes=" + requestEntry.start + "-");
			}
			requestEntry = ServiceClient.execute(requestEntry);
			if (!requestEntry.isSuccess) {
				return installPath;
			}
			if (requestEntry._response == null) {
				return installPath;
			}
			installPath = savePath.getFilePath();
			statusCtrl.status = StatusCtrl.DOWNED;
			onDownloadListener.onDownloadCompleted(requestEntry);
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return installPath;

	};
	/********
	 * 已废弃 与apk相关的函数请到ApkUtils调用
	 * *********/
           @Deprecated 
	private void install(Context activity, String apkFilePath) {
    }

	private JdOptionDialog getConcelDialog(final Context context, final boolean ismust) {
		JdOptionDialog.Builder builder = new JdOptionDialog.Builder(context);
		builder.setMessage("程序正在升级，确认取消？")
				.setPositiveButton(" 确 认 ",
						new JdOptionDialog.OnClickListener() {
							@Override
							public void onClick(JdOptionDialog dialog, int which) {
								Log.i("wxkly", "mJdOptionDialog is"
										+ myJdOptionDialog.toString());
								// TODO Auto-generated method stub
								statusCtrl.status=statusCtrl.DOWNCONCELED;
								Toast.makeText(context, "取消下载", 1).show();
								ThreadUtil.runInThread(cancelRunable);
								dialog.dismiss();
								if (myJdOptionDialog != null) {
									myJdOptionDialog.dismiss();
								}
								if (ismust) {
									exitReader();
								}
								// if(ismust){
								// ApplicationUpgradeHelper.exitReader();
								// }
							}
						})
				.setNegativeButton(" 取 消  ",
						new JdOptionDialog.OnClickListener() {
							@Override
							public void onClick(JdOptionDialog dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								// mpDialog.show();

							}
						});
				builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						
						return true;
					}
				});
		return builder.create();

	}



	public static void exitReader() {

		System.exit(1);
		android.os.Process.killProcess(android.os.Process.myPid());
		exitAll();
	}

	/**
	 * @author wuxinkai
	 * @since 2012-10-20
	 * @see 下载状态控制类 ，调用stopDownloadTasks() 终止下载任务
	 * **/

	public class StatusCtrl {// 状态管理类
		private RequestEntry requestEntry;

		/**
		 * @param requestEntry
		 *            要设置的 requestEntry
		 */
		public void setRequestEntry(RequestEntry requestEntry) {
			this.requestEntry = requestEntry;
		}

		int status = UNDOWNLOAD;
		static final int UNDOWNLOAD = 0;
		static final int DOWNING = 1;
		static final int DOWNED = 2;
		static final int DOWNCONCELED = 3;
		static final int DOWNSTOP = 4;

		public void stopDownloadTasks() {
			status = DOWNSTOP;
			if (null != requestEntry) {
				requestEntry.setStop(true);
				// requestEntry.stop(true);
			}
		}

	}
	
	/**
	 * 强制退出（杀后台进程，不更新widget和message）
	 */
	public static void exitAll() {
		if (Log.D) {
			Log.d("Temp", "MyApplication exitAll() -->> ");
		}
		com.jingdong.app.reader.util.Log.i("MyApplication",
				"MyApplication exitAll() -->>");
//		Manager.toDoOnExit();
		// 让后台进程去清理缓存文件
		clearCache();

		// 强制杀掉后台进程
		killBackground();
		System.exit(1);
		// 强制杀掉前台进程
		killStage();

	}

	/**
	 * 强制杀掉前台进程
	 */
	public static void killStage() {
		if (Log.D) {
			Log.d("Temp", "MyApplication killStage() -->> ");
		}

		// 结束界面
		// mainActivity.finishThis();（改为杀进程，因此注释掉。2011-06-08）
//		instance.setMainActivity(null);// 无论是否杀界面进程都必须清理，因为即使杀界面进程仍存在
//		GlobalInitialization.getInstance().setGlobalInitializationState(0);// 无论是否杀界面进程都必须清理，因为即使杀界面进程仍存在

		// 杀掉界面进程（改为杀进程。2011-06-08）
		Process.killProcess(Process.myPid());
	}

	/**
	 * 让后台进程去清理缓存文件
	 */
	public static void clearCache() {
		if (Log.D) {
			Log.d("Temp", "MyApplication clearCache() -->> ");
		}

//		// 清理coookies（寄居）
//		SharedPreferences jdSharedPreferences = CommonUtil
//				.getJdSharedPreferences();
//		if (!jdSharedPreferences.getBoolean(Contants.REMEMBER_FLAG, false)) {
//			jdSharedPreferences.edit().putString("cookies", null).commit();
//		}

		// 让后台进程去清理缓存文件
		// Intent intent = new Intent();
		// intent.setClass(MyApplication.getInstance(),
		// MessagePullService.class);
		// intent.setAction(MessagePullService.ACTION_APP_EXIT_CLEAR_CACHE);
		// MyApplication.getInstance().startService(intent);
	}

	/**
	 * 强制杀掉后台进程（如果正在清理缓存文件，那么会在清理缓存文件后自杀）
	 */
	public static void killBackground() {
		if (Log.D) {
			Log.d("Temp", "MyApplication killBackground() -->> ");
		}
		// Intent i = new Intent();
		// i.setClass(MyApplication.getInstance(), MessagePullService.class);
		// i.setAction(MessagePullService.ACTION_STEP_SEEVICE);
		// MyApplication.getInstance().startService(i);
	}
	
	
	public Handler getHandler() {
		return handler;
	}


	/**
	 * 统一 post 接口
	 */
	public void post(final Runnable action,final Context context) {
		// Log.i("zhoubo", "handler==="+handler);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (((Activity) context).isFinishing()) {
					return;
				}
				action.run();
			}
		});
	}
}
