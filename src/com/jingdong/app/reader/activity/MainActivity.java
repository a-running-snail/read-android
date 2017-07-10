package com.jingdong.app.reader.activity;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mobstat.StatService;
import com.jd.cpa.security.CpaConfig;
import com.jd.cpa.security.CpaHelper;
import com.jd.cpa.security.OnDevRepCallback;
import com.jd.cpa.security.ResultType;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.bookstore.buyborrow.BuyBorrowStatus;
import com.jingdong.app.reader.common.MZReadCommonActivityWithActionBar;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.extra.Splash;
import com.jingdong.app.reader.entity.extra.SplashImage;
import com.jingdong.app.reader.extension.giftbag.GiftBagUtil;
import com.jingdong.app.reader.io.EpubImporter;
import com.jingdong.app.reader.io.PDFImporter;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.opentask.InterfaceBroadcastReceiver.Command;
import com.jingdong.app.reader.preloader.BitmapLruCachePreloader;
import com.jingdong.app.reader.service.CpaService;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.DataIntent;
import com.jingdong.app.reader.util.FileUtils;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.LoginHelper;
import com.jingdong.app.reader.util.LoginHelper.LoginListener;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

public class MainActivity extends MZReadCommonActivityWithActionBar {
	public static final String BookNamKey = "BookNameKey";
	public static final String From3rdImportKey = "From3rdImportKey";
	private static final int SCALE_ANIMATION_DURATION = 2000;
	private boolean from3rdImport = false;
	private boolean isUIReady = false;//是否已经走完启动图部分

	private ImageView splashImageView;
	private TextView sloganTextView;
	private View loginPanel;
	private Intent startAppIntent;
	private Bitmap splashBitmap;
	private int width;
	private int height;
	private boolean isNeedToDoOtherTask = false;
	private Button jumpBt;
	private static final int[] SplashImageRes = { R.drawable.splash_local_1, R.drawable.splash_local_2 };
	private ImageView startImageview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//cpa注册
		initCPA();
		
		//是否为启动页
		startAppIntent = getIntent();
		boolean start = startAppIntent == null ? true : startAppIntent.getBooleanExtra("task", true);
		//是否启动首次安装引导图
		boolean isShowSplash = LocalUserSetting.getFirstShowSplash(MainActivity.this);
		mMainTabIndex = startAppIntent.getIntExtra("TAB_INDEX", 1);
		if (start && isShowSplash) {
			startActivity(new Intent(MainActivity.this, UserGuidSplashActivity.class));
			LocalUserSetting.saveFirstShowSplash(MainActivity.this, false);
			finish();
			return;
		}

		GlobalVarable.resetGlobalVarable(this);
		DisplayMetrics metric = this.getResources().getDisplayMetrics();
		width = metric.widthPixels;
		height = metric.heightPixels;

		setContentView(R.layout.activity_main);

		splashImageView = (ImageView) findViewById(R.id.splashImageView);
		sloganTextView = (TextView) findViewById(R.id.sloganTextView);
		sloganTextView.setShadowLayer(10f, 0f, 10f, 0x50000000);
		//京东阅读标识图
		startImageview = (ImageView) findViewById(R.id.start_imageview);
		
		Intent intent = startAppIntent;
		Uri uri = intent.getData();
		String bookName = intent.getStringExtra(BookNamKey);
		from3rdImport = intent.getBooleanExtra(From3rdImportKey, false);
		if (uri != null) {
			MZLog.d("MZBook", uri.getPath());
			ImportBookTask importBookTask = new ImportBookTask(this);
			importBookTask.execute(new String[] { bookName, uri.getPath() });
		}

		//MZBookDatabase.isStorageReady永为真?
		if (MZBookDatabase.isStorageReady) {
			LocalUserSetting.saveApplicationOpenFlag(MainActivity.this, true);// 应用打开标识
			if (start && MZBookApplication.isExiting) {
				//启动京东阅读标识动画，postDelayed延迟操作UI 
				startImageview.postDelayed(new Runnable() {
					@Override
					public void run() {
					    //淡出动画，京东阅读标识图慢慢
						//todo 图片在大屏的界面下有些模糊 
						AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
						animation.setDuration(500);
						animation.setFillAfter(true);
						startImageview.startAnimation(animation);
						LaunchWaitTask launchWaitTask = new LaunchWaitTask();
						launchWaitTask.execute();
					}
				}, 500);
				
				CheckBookStyleTask checkStyle = new CheckBookStyleTask();
				checkStyle.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				// 预加载书架图片
				BitmapLruCachePreloader preloader = new BitmapLruCachePreloader(MainActivity.this);
				MZBookApplication.isExiting = false;
			} else {
				Task tast = new Task();
				tast.execute();
			}

		} else {
			if (isSDCardReady()) {
				Toast.makeText(this, R.string.sqlite_open_error, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, R.string.no_sdcard_storage, Toast.LENGTH_LONG).show();
			}
			this.finish();
		}

		jumpBt=(Button) findViewById(R.id.jump_bt);
		jumpBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				isUIReady = true;
				jumpToFirstPage();
			}
		});
	}
	
	/**
	 * cpa注册方法
	 */
	public void initCPA(){
		
		String unionId = CommonUtil.getPropertiesValue("partnerID");
		String subunionId = CommonUtil.getPropertiesValue("subPartnerID");
		String deviceId= StatisticsReportUtil.readDeviceUUID();
		
		String parnerId = subunionId;
		if(subunionId!=null && !subunionId.equals("")){
			parnerId = unionId+"_"+subunionId;
		}else
			subunionId = "";
		if(parnerId ==null || parnerId.equals(""))
			parnerId = "android";
			
		CpaHelper.registerCpa(this, new OnDevRepCallback() {
			@Override
			public void onSuccess(String codeStr) {
				if ("cpatalk".equals(codeStr)) {

				} else if ("cpa".equals(codeStr)) {

				}
				System.out.println("MZBookApplication.initCPA().new OnDevRepCallback() {...}.onError()");
			}

			@Override
			public void onFail(ResultType fail) {
				System.out.println("MZBookApplication.initCPA().new OnDevRepCallback() {...}.onFail()");
			}

			@Override
			public void onError(ResultType error) {
				System.out.println("MZBookApplication.initCPA().new OnDevRepCallback() {...}.onError()");
			}
		}, deviceId, parnerId, unionId, subunionId);
	}


	@Override
	protected void onStart() {
		super.onStart();
	}

	private int mMainTabIndex;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE) {
			jumpToFirstPage();
		}
	}

	/**
	 * 功能：<br />
	 * 1、自动登录<br />
	 * 2、自动登录完成广播<br />
	 * 3、额外任务以及请求畅读相关信息
	 * 4、更新用户借阅状态
	 * 5、礼包未读消息
	 * 
	 * @Title: autoLogin
	 * @Description: 自动登录
	 * @param @param username
	 * @param @param pwd
	 * @return void
	 * @throws
	 */
	public void autoLogin(final String username, final String pwd) {
		LoginHelper.doLogin(this, username, pwd,null, true, new LoginListener() {
			
			@Override
			public void onLoginSuccess() {
				//绑定设备
				LoginHelper.CheckBind(MainActivity.this, null);
				
				//发送自动登录成功广播
				Intent intent = new Intent();
				intent.setAction("com.jdread.action.login");
				LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);

				//额外任务以及请求畅读相关信息
				ExtraTaskOnLoginSuccess task = new ExtraTaskOnLoginSuccess(MainActivity.this, getIntent(), isNeedToDoOtherTask);
				task.execute();

				if (splashBitmap != null) {
					splashBitmap.recycle();
				}
				
				getBuyBorrowStatus();
				GiftBagUtil.getInstance().getUnReadMessage(getBaseContext());
				finish();
			}
			
			@Override
			public void onLoginFail(String errCode) {
				finish();
			}
		});
	}
	
	/**
	* @Description: 登陆成功后获取借阅状态
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:39:58 
	* @throws 
	*/ 
	private void getBuyBorrowStatus() {
		WebRequestHelper.post(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getBorrowStatusParams(), 
				false, new MyAsyncHttpResponseHandler(this) {

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
						if("0".equals(code)) {
							BuyBorrowStatus b = new BuyBorrowStatus();
							b.status = jsonObj.optBoolean("status");
							b.lendStatus = jsonObj.optInt("lendStatus");
							MZBookApplication.getInstance().setBuyBorrowStatus(b);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	// 更新docbind表
	public void updateDocbind() {
		MZLog.d("wangguodong", "用户重新登录 检查docbind相应书籍是否存在");
		List<Document> doc = MZBookDatabase.instance.listDocument();
		String userId = LoginUser.getpin();
		for (int i = 0; i < doc.size(); i++) {
			DocBind docBind = MZBookDatabase.instance.getDocBind(doc.get(i).documentId, userId);
			if (null == docBind) {
				docBind = new DocBind();
				docBind.documentId = doc.get(i).documentId;
				docBind.userId = userId;
				MZBookDatabase.instance.insertOrUpdateDocBind(docBind);
			}
		}
		MZLog.d("wangguodong", "用户重新登录 docbind数据同步成功");
	}

	/**
	 * 
	 */
	private void jumpToFirstPage() {
		if (!isUIReady) {
			return;
		}
		
		String key = getIntent().getStringExtra("key");
		if (!TextUtils.isEmpty(key)) {
			isNeedToDoOtherTask = true;
			// 如果用户未登录去登录
			Object obj = DataIntent.get(key);
			if (obj != null && obj instanceof Command) {
				//外部任务
				final Command command = (Command) obj;
				Bundle bundle = command.getOutBundle();
				String userName = bundle.getString("userName");
				// 修复数据丢失
				DataIntent.put(key, command);

				if (!TextUtils.isEmpty(userName) && !userName.equals(LoginUser.getpin())) {
					MZLog.d("wangguodong", "外部任务用户名与本地用户名不一致，重新登录!");
					// 去登录
					Intent intent = new Intent(MainActivity.this, LoginActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivityForResult(intent, GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE);
					return;
				} else {
					// 自动登录
					MZLog.d("wangguodong", "外部任务用户名与本地用户名一致，自动登录!" + LoginUser.getUserName() + "," + LoginUser.getpsw());
					autoLogin(LoginUser.getUserName(), LoginUser.getpsw());
				}
			} else {
				Toast.makeText(MainActivity.this, "外部任务执行出错,参数不对！", Toast.LENGTH_LONG).show();
				return;
			}

		}else {
			if (LoginUser.isAutoLogin() && !TextUtils.isEmpty(LoginUser.getUserName()) && !TextUtils.isEmpty(LoginUser.getpsw())) {
				// 自动登录
				autoLogin(LoginUser.getUserName(), LoginUser.getpsw());
			}
		}
		Intent cpaService = new Intent(this, CpaService.class);
		startService(cpaService);

		Intent intent = new Intent();
		intent.setClass(MainActivity.this, LauncherActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("TAB_INDEX", mMainTabIndex);
		startActivity(intent);
		MZBookApplication.removeFormActivityStack(this);
		this.finish();

	}
	
	/**
	 * 默认启动信息（有两份，取其中一份）
	 * @param imageIndex
	 */
	public void setDefaultSplash(int imageIndex) {
		//口号文字展示的位置（高）百分比
		float percent[] = new float[] { 0.75f, 0.2f };
		//口号文字展示的位置（宽）百分比
		float percent_width[] = new float[] { 0.33f, 0.015f };
		//口号文本
		String sloganText[] = new String[] { "You Are What You Read", "借书是恋爱的开始——钱钟书" };
		//默认启动图
		splashBitmap = ImageUtils.getBitmapFromResource(MainActivity.this, SplashImageRes[imageIndex], width, height);
		splashImageView.setImageBitmap(splashBitmap);
		//设备的宽高
		int height = (int) (getWindowManager().getDefaultDisplay().getHeight() * percent[imageIndex]);
		int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * percent_width[imageIndex]);
		RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) sloganTextView.getLayoutParams();
		MZLog.d("perfromance", "height: " + height);
		MZLog.d("perfromance", "width: " + width);
		linearParams.topMargin = height;
		linearParams.leftMargin = width;
		sloganTextView.setText(sloganText[imageIndex]);
		sloganTextView.setLayoutParams(linearParams);
	}

	private void showRandomSplashImage() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//随机
				Random rn = new Random();
				//由于默认启动图有两个，所以是0和1的随机数
				int imageIndex = rn.nextInt(2);
	
				//从配置里边读取轮显序号
				if (LocalUserSetting.getSplashSinceId(MainActivity.this) == -1) {
					//设置默认启动图
					setDefaultSplash(imageIndex);
				}
				else {
					//从本地数据库中读取启动图列表（含历史）
					List<Splash> dataList = MZBookDatabase.instance.findSplashs();
					if (dataList == null || dataList.size() == 0) {
						//设置默认启动图
						setDefaultSplash(imageIndex);
						return;
					}
	
					Random temprn = new Random();
					//从现有的启动图中随机
					int newid = rn.nextInt(dataList.size());
					//从图片目录下取出图片对象
					splashBitmap = ImageUtils.getSplashBitmap(MainActivity.this, "splash_" + dataList.get(newid).id, width, height);
					MZLog.d("J", "dataList.size()=" + dataList.size() + ",newid=" + newid);
					if (splashBitmap != null) {
						if (dataList.get(newid) != null) {
							//设置口号文本
							sloganTextView.setText(dataList.get(newid).adText);
							int theight = (int) (getWindowManager().getDefaultDisplay().getHeight() * dataList.get(newid).adTextTopPercent / 100);
							int twidth = (int) (getWindowManager().getDefaultDisplay().getWidth() * dataList.get(newid).adTextLeftPercent / 100);
	
							MZLog.d("wangguodong", "悬浮图片文字位置：" + height);
							RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) sloganTextView.getLayoutParams();
							linearParams.topMargin = theight;
							linearParams.leftMargin = twidth;
							sloganTextView.setLayoutParams(linearParams);
						}
						splashImageView.setImageBitmap(splashBitmap);
					} else {
						setDefaultSplash(imageIndex);
					}
	
				}
	
				// 执行开机动画
				Animation animation = new ScaleAnimation(1f, 1.1f, 1f, 1.1f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f);
				animation.setDuration(SCALE_ANIMATION_DURATION);
				animation.setFillAfter(true);
				splashImageView.setAnimation(animation);
				animation.startNow();
	
				splashImageView.setVisibility(View.VISIBLE);
				sloganTextView.setVisibility(View.VISIBLE);
				requestSplashImages();
			}
		});
		SplashWaitTask splashWaitTask = new SplashWaitTask();
		splashWaitTask.execute();
	}

	private void showLoginPanel() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				sloganTextView.setVisibility(View.GONE);

				loginPanel.setVisibility(View.VISIBLE);
			}
		});
	}

	private class ImportBookTask extends AsyncTask<String, Integer, Integer> {

		private final ProgressDialog pDialog;
		private final AtomicInteger fileNumber = new AtomicInteger();
		private Context mContext;

		public ImportBookTask(Context mContext) {
			this.mContext = mContext;
			pDialog = new ProgressDialog(mContext);
		}

		@Override
		protected void onPreExecute() {
			Resources resources = getResources();
			pDialog.setTitle(R.string.importBookTitle);
			pDialog.setMessage(resources.getString(R.string.importBookMsg));
			pDialog.setIndeterminate(false);
			pDialog.setMax(100);
			pDialog.setProgress(0);
			pDialog.setCancelable(true);
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
			pDialog.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			String bookName = params[0];
			String bookPath = params[1];
			File file = new File(bookPath);
			if (TextUtils.isEmpty(bookName)) {
				bookName = file.getName();
			}
			File unZipDir = new File(mContext.getExternalCacheDir(), file.getName());
			int flag = EpubImporter.BOOK_IMPORT_FAIL;
			try {
				if (FileUtils.isPDF(file.getName())) {
					flag = PDFImporter.importBook(file.getName(), file, MainActivity.this);
				} else {
					if (EpubImporter.isInBookCase(file, unZipDir)) {
						flag = EpubImporter.BOOK_IS_EXIST;
					} else {
						flag = EpubImporter.importBook(file.getName(), file, mContext);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				delDirRecurse(unZipDir);
				publishProgress(fileNumber.incrementAndGet());
			}
			return flag;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			pDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			pDialog.dismiss();
			if (result == EpubImporter.BOOK_IMPORT_SUCCESS) {
				Toast.makeText(mContext, getString(R.string.importBookNumber, fileNumber.get()), Toast.LENGTH_SHORT).show();
			} else if (result == EpubImporter.BOOK_IS_EXIST) {
				Toast.makeText(mContext, getString(R.string.file_has_exist), Toast.LENGTH_SHORT).show();
			} else if (result == EpubImporter.BOOK_IMPORT_FAIL) {
				Toast.makeText(mContext, getString(R.string.importBookFail), Toast.LENGTH_SHORT).show();
			}
		}

		private void delDirRecurse(File dir) {
			File[] subFiles = dir.listFiles();
			if (subFiles != null) {
				for (File item : subFiles) {
					if (item.isDirectory())
						delDirRecurse(item);
					else
						item.delete();
				}
			}
			dir.delete();
		}
	}

	private class CheckBookStyleTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String bookStyleLevel = LocalUserSetting.getBookStyleLevel(MainActivity.this);
			if (TextUtils.isEmpty(bookStyleLevel)) {
				bookStyleLevel = GlobalVarable.BOOK_STYLE_LEVEL;
				LocalUserSetting.saveBookStyleLevel(MainActivity.this, GlobalVarable.BOOK_STYLE_LEVEL);
			}
			
			if (!GlobalVarable.BOOK_STYLE_LEVEL.equals(bookStyleLevel)) {
				MZBookDatabase.instance.clearBookPageContent();
				LocalUserSetting.saveBookStyleLevel(MainActivity.this, GlobalVarable.BOOK_STYLE_LEVEL);
			}
			return null;
		}

	}

	/**
	 * 通过一个异步任务，显示随机的启动图
	 * @author mowen
	 */
	private class LaunchWaitTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			//随机启动图
			showRandomSplashImage();
			return null;
		}

	}

	private class Task extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					isUIReady = true;
					jumpToFirstPage();
				}
			});
			return null;
		}
	}

	private class SplashWaitTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			try {
				Thread.sleep(SCALE_ANIMATION_DURATION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					isUIReady = true;
					jumpToFirstPage();
				}
			});

			return null;
		}
	}

	/**
	 * 从服务器请求启动图
	 */
	public void requestSplashImages() {
		//WIFI下才请求
		if (NetWorkUtils.isWifiConnected(MainActivity.this)) {
			WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSplashParams(), new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					String result = new String(arg2);
					MZLog.d("wangguodong", "当前处于wifi状态，更新本地splash图片" + result);
					SplashImage image = GsonUtils.fromJson(result, SplashImage.class);
					long sinceid = LocalUserSetting.getSplashSinceId(MainActivity.this);

					if (image == null || image.screenPics == null || image.screenPics.size() == 0) {
						MZLog.d("wangguodong", "未获取到splash图片" + result);
						return;
					}

					for (int i = 0; i < image.screenPics.size(); i++) {
						final int k = i;
						final long tempId = image.screenPics.get(k).id;
						if (tempId > sinceid) {
							// sinceid = tempId;
							final Splash splash = image.screenPics.get(k);
							WebRequestHelper.get(image.screenPics.get(k).url, new FileAsyncHttpResponseHandler(MainActivity.this) {

								@Override
								public void onFailure(int arg0, Header[] arg1, Throwable arg2, File arg3) {
									MZLog.d("wangguodong", "保存splash数据失败");
								}

								@Override
								public void onSuccess(int arg0, Header[] arg1, File response) {
									MZLog.d("wangguodong", "已经获取到splash文件");
									try {
										FileInputStream stream = new FileInputStream(response);
										Bitmap bitmap = BitmapFactory.decodeStream(stream);
										ImageUtils.saveBitmap(MainActivity.this, bitmap, "splash_" + tempId);
										MZLog.d("wangguodong", "保存splash文件到本地");
										MZBookDatabase.instance.insertOrUpdateSplash(splash);
										// 保存显示图片的数据
										LocalUserSetting.saveSplashSinceId(MainActivity.this, tempId);

									} catch (Exception e) {
										MZLog.d("wangguodong", "保存splash文件出异常了");
									}

								}
							});

						} else {
							MZLog.d("wangguodong", "服务器splash没有更新" + result);
						}
					}
				}

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					MZLog.d("wangguodong", "请求服务器失败");
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
    	StatService.onPageStart(this, getString(R.string.mtj_start_app));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
    	StatService.onPageEnd(this, getString(R.string.mtj_start_app));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * 存储卡是否已经就绪
	 * @return
	 */
	private boolean isSDCardReady() {
		try {
			return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
