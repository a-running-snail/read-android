package com.jingdong.app.reader.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;
import org.w3c.dom.Element;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.config.Constant;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.data.DrmTools;
import com.jingdong.app.reader.data.db.DBLocalBookHelper;
import com.jingdong.app.reader.entity.BootEntity;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OlineCard;
import com.jingdong.app.reader.entity.ReturnStatus;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.io.Unzip;
import com.jingdong.app.reader.net.HttpSetting;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.service.download.DownloadService;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.APKUtil;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.UnzipFile;

public class DownloadThreadBook extends DownloadThread {
	public static String KEY_MESSAGE = "error";
	private static int TYPE_SMALL = 0;
	private static int TYPE_BIG = 1;

	private LocalBook localBook;
	public DownloadThreadQueue downloadThreadQueue;

	/**
	 * 图书下载线程
	 * @param downloadThreadQueue 下载队列
	 * @param downloadAbler 下载任务相关信息
	 */
	public DownloadThreadBook(DownloadThreadQueue downloadThreadQueue, DownloadedAble downloadAbler) {
		super(downloadThreadQueue, downloadAbler);
		this.localBook = (LocalBook) downloadAbler;
		this.downloadThreadQueue = downloadThreadQueue;
	}
	
	/**
	 * 下载图书文件
	 */
	@Override
	public void run() {
		handleLoad(localBook);
	}
	
	/**
	 * 下载
	 * @param localBook
	 */
	public void handleLoad(LocalBook localBook) {
		handleLoad(localBook, false, false);
	}
	
	/**
	 * 下载图书
	 * @param localBook
	 * @param isPassBigPic
	 * @param isPassSmallPic
	 */
	public void handleLoad(LocalBook localBook, boolean isPassBigPic, boolean isPassSmallPic) {
		System.out.println("DDDDDDDD===DownloadThreadBook=====handleLoad=====1111===");			
		if (localBook.isMenualStop) {
			isloading = false;
			return;
		}
		downloadThreadQueue.refresh(localBook);
		Log.i("DownloadThread", "handleLoad 99999");
		if (localBook.state == LocalBook.STATE_LOAD_PAUSED //下载暂停
				|| localBook.state == LocalBook.STATE_LOAD_FAILED  //下载失败
				|| localBook.state == LocalBook.STATE_LOADED) /*下载完成*/ {
			localBook.mod_time = System.currentTimeMillis();
			localBook.save();
			isloading = false;
			if (localBook.state == LocalBook.STATE_LOADED) {//若已经下载完成
				sartDownBookImage(localBook, TYPE_BIG);//下载图片
				sartDownBookImage(localBook, TYPE_SMALL);
			}
			return;
		}
		
		//是否为下载试读文件
		if (localBook.source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
			if (!TextUtils.isEmpty(localBook.bookUrl)) {// 试读一定得有url
				getBookContent(localBook);
			} else {
				localBook.state = LocalBook.STATE_LOAD_PAUSED;
				handleLoad(localBook);
			}
		} else {//下载正式文件
			//当前图书的引导文件还没有下载，则下载需要下载引导文件
			if (localBook.boot == null) {
				//调用Rights的verify接口（请求引导文件），请求相关证书和下载图书相关接口地址，成功后继续下载
				loadBoot(localBook);
			} else if (localBook.boot != null) {
				//还没有证书，则下载证书
				if (TextUtils.isEmpty(localBook.cert)) {
					//获取该电子书证书
					getCert(localBook);
				} 
				//若还没有请求到下载地址，则请求下载地址
				else if (TextUtils.isEmpty(localBook.bookUrl)) {
					getBookUrl(localBook);
				} 
				//下载图书内容
				else if (TextUtils.isEmpty(localBook.book_path) || localBook.state != LocalBook.STATE_LOADED) {
					localBook.bookUrl = RequestParamsPool.url2IP(localBook.bookUrl);
					//下载图书内容加密文件
					getBookContent(localBook);
				}
			} else {
				localBook.state = LocalBook.STATE_LOAD_PAUSED;
				handleLoad(localBook);
			}
		}

	}

	/**
	 * 从Rights服务器获取引导文件
	 * @param localBook
	 */
	public void loadBoot(final LocalBook localBook) {
		boolean isSuccess = false;
		String massage = null;
		boolean isborrow = localBook.source.equals(LocalBook.SOURCE_BORROWED_BOOK) ? true : false;
		HttpSetting httpSetting = RequestParamsPool.getBookverifyHttpSetting(LoginUser.getpin(), "" + localBook.book_id, "" + localBook.order_code, isborrow);
		//启动任务
		httpSetting.setType(HttpSetting.TYPE_XML);
		RequestEntry entry = ServiceClient.execute(httpSetting);
		if (entry._statusCode == 0) {
			BootEntity boot = null;
			try {
				boot = BootEntity.parser((Element) entry._userData);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (boot == null) {
				ReturnStatus returnStatus = DataParser.parserReturnStatus((String) entry._tag);
				massage = returnStatus.massage;
				if (returnStatus.code == 107) {
					massage = "你还未登录，请登录后下载";
				}
			} else {
				isSuccess = true;
				localBook.boot = boot;
				localBook.saveBoot();
			}
		}
		if (isSuccess) {
			massage = "";
		} else {
			localBook.state = LocalBook.STATE_LOAD_FAILED;
			if (TextUtils.isEmpty(massage)) {
				massage = "下载失败";
			}
		}
		if (!TextUtils.isEmpty(massage)) {
			MZLog.d("wangguodong", massage);
		}
		//继续下载
		handleLoad(localBook);
	}
	
	/**
	 * 获取电子书加解密证书
	 * @param localBook
	 */
	private void getCert(LocalBook localBook) {
		localBook.downingWhich = LocalBook.DOWNING_CERT;
		boolean isSuccess = false;
		String massage = null;
		int code =-1 ;
		try {
			BootEntity bootEntity = localBook.boot;
			boolean isRandom = true;

			// 添加借阅判断 开始
			boolean isborrow = localBook.source.equals(LocalBook.SOURCE_BORROWED_BOOK) ? true : false;
			boolean isBorrowBuy = localBook.source.equals(LocalBook.SOURCE_USER_BORROWED_BOOK) ? true : false;
			// 添加借阅判断 结束
			
			HttpUriRequest request = RequestParamsPool.getCertRequest(bootEntity, DrmTools.hashDevicesInfor(), isRandom, isborrow, isBorrowBuy);
			RequestEntry entry = new RequestEntry(RequestEntry.REQUEST_TYPE_SEARCH_POST_BY_JSON, request);
			localBook.setRequestEntry(entry);
			entry._type = RequestEntry.TYPE_STRING;

			RequestEntry requestEntry = ServiceClient.execute(entry);
			if (entry.getRequestCode() != localBook.getRequestCode()) {
				return;
			}
			
			if (requestEntry._statusCode == 0) {
				String result = (String) requestEntry._userData;
				JSONObject josnObject = new JSONObject(result);
				localBook.cert = DataParser.getString(josnObject, "key");

				//畅读卡信息
				OlineCard olineCard = OlineCard.parserOLineCardFromCert(josnObject);
				if (!TextUtils.isEmpty(olineCard.cardNum)) {
					olineCard.save();
				}
				LocalBook.saveCardInBook(localBook.getId(), olineCard.cardNum);
				try {
					localBook.random = DataParser.getString(josnObject, "random");
					if (!TextUtils.isEmpty(localBook.random) && !TextUtils.isEmpty(localBook.userName)) {
						localBook.saveRandom();
					}
				} catch (Exception e) {
					Log.e("zhoubo", e.toString());
					e.printStackTrace();
				}
				localBook.deviceId = DrmTools.hashDevicesInfor();
				localBook.save();
				LocalBook book = LocalBook.getLocalBook(localBook.book_id, LoginUser.getpin());

				if (!TextUtils.isEmpty(book.cert)) {
					isSuccess = true;
				} else {
					ReturnStatus returnStatus = DataParser.parserReturnStatus(result);
					massage = returnStatus.massage;
					code = returnStatus.code;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (isSuccess) {
			massage = "";
		} else {
			localBook.state = LocalBook.STATE_LOAD_FAILED;
			if (TextUtils.isEmpty(massage)) {
				massage = "下载失败";
			}
		}
		if (!TextUtils.isEmpty(massage)) {
			if(code == 100){
				ToastUtil.showToastInThread("设备绑定超过10台限制，请到我-设置-解绑设备中解绑后使用。", Toast.LENGTH_LONG, localBook.book_id+"");
			}else{
				ToastUtil.showToastInThread(massage, Toast.LENGTH_LONG, localBook.book_id+"");
			}
		}
		handleLoad(localBook);
	}

	/**
	 * 从Rights服务器获取下载地址
	 * @param localBook
	 */
	private void getBookUrl(LocalBook localBook) {
		System.out.println("DDDDDDDD===DownloadThreadBook=====getBookUrl=====111===");
		localBook.downingWhich = LocalBook.DOWNING_BOOK_URL;
		boolean isSuccess = false;
		String massage = null;
		try {
			BootEntity bootEntity = localBook.boot;
			if (bootEntity == null) {
				return;
			}
			HttpUriRequest request = RequestParamsPool.getBookUrlRequest(bootEntity);
			Log.i("DownloadThread", "bookUrl====" + request.toString());

			RequestEntry entry = new RequestEntry(RequestEntry.REQUEST_TYPE_SEARCH_POST_BY_JSON, request);
			localBook.setRequestEntry(entry);
			entry._type = RequestEntry.TYPE_STRING;
			RequestEntry requestEntry = ServiceClient.execute(entry);
			if (entry.getRequestCode() != localBook.getRequestCode()) {
				return;
			}
			// InputStream inputStream = (InputStream)requestEntry._userData;
			JSONObject josnObject;
			Log.i("DownloadThread", "userData===" + (String) requestEntry._userData);
			String result = (String) requestEntry._userData;
			josnObject = new JSONObject(result);
			String url;
			url = DataParser.getString(josnObject, "ebookAddress");// josnObject.getString("ebookAddress");
			// url = url.replace("ebook-drm.360buy.net",
			// "10.10.224.43:8753");
			url = RequestParamsPool.url2IP(url);

			url = url.replace("\n", "");
			// url = url.replaceAll(".com//", ".com/");
			localBook.bookUrl = url;
			localBook.save();
			LocalBook book = LocalBook.getLocalBook(localBook.book_id, LoginUser.getpin());
			if (!TextUtils.isEmpty(book.bookUrl)) {
				isSuccess = true;
			} else {
				ReturnStatus returnStatus = DataParser.parserReturnStatus(result);
				massage = returnStatus.massage;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isSuccess) {
			// ShowTools.toastInThread("获取图书地址成功");
			massage = "获取图书地址成功";
		} else {
			localBook.state = LocalBook.STATE_LOAD_FAILED;
			if (TextUtils.isEmpty(massage)) {
				massage = "获取图书地址失败";
			}
			// ShowTools.toastInThread("获取图书地址失败");
		}
		if (sIsnotice) {
			MZLog.d("wangguodong", massage);
		}
		handleLoad(localBook);
	}
	

	
	/**
	 * 下载电子书内容文件
	 * @param localBook
	 */
	private void getBookContent(final LocalBook localBook) {
		System.out.println("DDDDDDDD===DownloadThreadBook=====getBookContent=====111===");
		localBook.downingWhich = LocalBook.DOWNING_BOOK_CONTENT;
		RequestEntry entry = null;
		Log.i("DownloadThread", "获取书籍字节流开始...");
		try {

			/**
			 * Rights的引导信息
			 */
			BootEntity bootEntity = localBook.boot;
			if (!localBook.source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
				//检查数据是否合法
				if (bootEntity == null || localBook.bookUrl == null) {
					return;
				}
			}
			//下载请求
			HttpUriRequest request = new HttpGet(localBook.bookUrl);	
			//请求信息
			entry = new RequestEntry(RequestEntry.REQUEST_TYPE_SEARCH_POST_BY_JSON, request);
			localBook.setRequestEntry(entry);
			//文件下载请求
			entry._type = RequestEntry.TYPE_FILE;

			//下载过程的回调
			entry._downloadListener = new OnDownloadListener() {
				/**
				 * 下载进度刷新
				 */
				@Override
				public void onprogress(long progress, long max) {
					localBook.progress = progress;
					if (localBook.size < 1) {
						localBook.size = max;
					}
					//下载进度刷新
					downloadThreadQueue.refresh(localBook);
				}

				@Override
				public void onDownloadCompleted(RequestEntry requestEntry) {
				}
			};
			
			//文件保存路径
			FileGuider savePath = new FileGuider(FileGuider.SPACE_PRIORITY_EXTERNAL);
			savePath.setImmutable(true);
			String suffix = getSuffix(localBook.bookUrl);
			if (localBook.type_id == LocalBook.TYPE_M) {
				savePath.setChildDirName("/apk/" + localBook.book_id);
				savePath.setFileName(localBook.book_id + suffix);
			} else {
				savePath.setChildDirName("/epub/" + localBook.book_id);
				savePath.setFileName(localBook.book_id + suffix);
			}
			//文件保存路径目录
			localBook.dir = savePath.getParentPath();
			entry.fileGuider = savePath;
			if (localBook.size <= 0 || localBook.progress > localBook.size) {
				File fileDir = new File(localBook.dir + File.separator + "content");
				//文件目录存在，则先删除
				if (fileDir.exists()) {
					IOUtil.deleteFile(fileDir);
				}
				DownloadThread.delFile(savePath.getFilePath());
			}
			File file = new File(savePath.getFilePath());
			FileInputStream fis = null;
			if (file != null && file.exists()) {
				fis = new FileInputStream(file);
			}
			if (fis != null && fis.available() > 10 && fis.available() < localBook.size) {
				entry.start = fis.available();
				localBook.progress = fis.available();
				entry.end = localBook.size;
			} else {
				DownloadThread.delFile(file);
				File fileDir = new File(localBook.dir + File.separator + "content");
				if (fileDir.exists()) {
					IOUtil.deleteFile(fileDir);
				}
				localBook.progress = 0;
			}
			entry.start = localBook.progress;
			entry.end = localBook.size;
			if (entry.start > 0) {
				request.addHeader("Range", "bytes=" + entry.start + "-");
			}
			RequestEntry requestEntry = ServiceClient.execute(entry);
			if (entry.getRequestCode() != localBook.getRequestCode()) {
				return;
			}
			if (requestEntry._statusCode == 0) {
			} else {
				if (!TextUtils.isEmpty(requestEntry._stateNotice)) {
					MZLog.d("wangguodong", requestEntry._stateNotice);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int state = LocalBook.STATE_LOAD_FAILED;// 默认下载失败

		if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {
			state = LocalBook.STATE_LOAD_PAUSED;
		} else if (entry.isSuccess && localBook.progress == localBook.size && localBook.book_path != null) {
			localBook.book_path = entry.fileGuider.getFilePath();
			if (localBook.type_id == LocalBook.TYPE_M) {// 多媒体书籍 目前不支持，可以下载

				File f = new File(localBook.book_path);
				String dir = f.getParent() + "/" + "jeb";
				MZLog.d("wangguodong", "文件路径:" + dir);
				try {
					UnzipFile.unZipFile1(dir, localBook.book_path);
					localBook.book_path = dir + "/" + localBook.book_id + "." + "JEB";
					localBook.packageName = APKUtil.ParseJebPackage(localBook);
					if (localBook.packageName != null) {
						state = LocalBook.STATE_LOADED;
						localBook.bookState = LocalBook.BOOK_STATE_NO_INTSTALL;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				Log.i("DownloadThread", "localBook.packageName==" + localBook.packageName);

			} else if (localBook.type_id == LocalBook.TYPE_EBOOK && localBook.format == LocalBook.FORMAT_EPUB) {
				String name = localBook.book_id + "." + "JEB";
				String dir = "/epub/" + localBook.book_id;
				String path = FileGuider.getPath(FileGuider.SPACE_PRIORITY_EXTERNAL, dir, name);
				File f = new File(localBook.book_path);
				String fileDir = f.getParent() + File.separator + "content";

				// 解压epub
				MZLog.d("wangguodong", "这本书是epub文件，需要解压！");
				FileInputStream fin = null;
				try {
					MZLog.d("wangguodong", "开始解压EPUB文件");
					fin = new FileInputStream(new File(path));
					Unzip.unzip(fin, fileDir);
					MZLog.d("wangguodong", "解压EPUB书籍成功");

				} catch (IOException e) {
					MZLog.d("wangguodong", "解压EPUB书籍出问题了");
					IOUtil.closeStream(fin);
				}
				state = LocalBook.STATE_LOADED;
			} else {
				MZLog.d("wangguodong", "这本书是pdf文件，跳过解压！");
				state = LocalBook.STATE_LOADED;
			}
			Log.i("DownloadThread", "localBook.book_path===" + localBook.book_path);

		}

		if (state == LocalBook.STATE_LOAD_PAUSED) {

		} else if (state == LocalBook.STATE_LOADED) {

			Intent intent = new Intent(DownloadService.DOWNLOAD_TASK_FINISH_BROADCAST);
			LocalBroadcastManager.getInstance(MZBookApplication.getContext()).sendBroadcast(intent);
			Log.i("wangguodong", "下载图书成功，请在本地书架查看");

		}

		localBook.state = state;
		handleLoad(localBook);
	}

	public void stopDownload() {
		if (localBook != null) {
			localBook.manualStop();
		}
		try {
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sartDownBookImage(final LocalBook localBook, final int type) {
		boolean isSuccess = false;
		String name = null;
		String url = null;
		String dir = null;
		if (type == TYPE_BIG) {
			localBook.downingWhich = LocalBook.DOWNING_BIG_IMG;
			url = localBook.bigImageUrl;
			if (TextUtils.isEmpty(url))
				return;
			String imageSuffix = getSuffix(url);
			imageSuffix = imageSuffix + Constant.IMAGE_SUFFI;
			name = localBook.book_id + imageSuffix;
		} else {
			localBook.downingWhich = LocalBook.DOWNING_SMALL_IMG;
			url = localBook.smallImageUrl;
			if (TextUtils.isEmpty(url))
				return;
			String imageSuffix = getSuffix(url);
			imageSuffix = imageSuffix + Constant.IMAGE_SUFFI;
			name = "s" + localBook.book_id + imageSuffix;
		}
		if (localBook.type_id == LocalBook.TYPE_M) {
			dir = "/apk/" + localBook.book_id;
		} else {
			dir = "/epub/" + localBook.book_id;
		}
		String path = null;
		// 新添加---当本地已存在时，就不去下载，直接在本地读取。
		if (localBook.type_id == LocalBook.TYPE_EBOOK) {
			path = FileGuider.getPath(FileGuider.SPACE_PRIORITY_EXTERNAL, dir, name);// Environment.getExternalStorageDirectory()+"/"+"jdreader"+dir+name;
			// localBook.dir = ;

			File file = new File(path);
			if (file.exists()) {
				if (type == TYPE_BIG) {
					localBook.bigImagePath = path;
					// handleLoad(localBook, true, false);
				} else {
					localBook.smallImagePath = path;
					// handleLoad(localBook, true, true);
				}
				if (DBLocalBookHelper.isExistIntDB(localBook.book_id)) {
					localBook.save();
					localBook.needFreshImage = true;
					downloadThreadQueue.refresh(localBook);
					localBook.needFreshImage = false;// 刷新完成后，置为null
				}
				return;
			} else {
				/*
				 * String tempPath = RefreshImage.getImagePath(url);//从图片缓存模块里读取
				 * file = new File(tempPath); if (file.exists()) {
				 * FileUtils.copyFile(tempPath, path); if (type == TYPE_BIG) {
				 * localBook.bigImagePath = path; // handleLoad(localBook, true,
				 * false); } else { localBook.smallImagePath = path; //
				 * handleLoad(localBook, true, true); } if
				 * (DBLocalBookHelper.isExistIntDB(localBook.id)) {
				 * localBook.save(); localBook.needFreshImage = true;
				 * downloadThreadQueue.refresh(localBook);
				 * localBook.needFreshImage = false;// 刷新完成后，置为null } return; }
				 */
			}
		}
		// end--
		try {
			BootEntity bootEntity = localBook.boot;
			if (bootEntity == null) {
				return;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// RequestEntry requestEntry = ServiceClient.execute(entry);

		OnDownloadListener onDownloadListener = new OnDownloadListener() {
			@Override
			public void onprogress(long progress, long max) {
				Log.i("wxkly", "onprogress1");
			};

			@Override
			public void onDownloadCompleted(RequestEntry entry) {
				boolean isSuccess = false;
				try {
					// if (entry.getRequestCode() != localBook.getRequestCode())
					// {
					// return;
					// }
					if (entry.isSuccess) {

						String path;
						path = entry.path;
						Bitmap bitmap = ImageUtils.getImage(path);
						if (localBook.type_id == LocalBook.TYPE_EBOOK) {
							bitmap = ImageUtils.CropForExtraWidth(bitmap);
							if (type == TYPE_SMALL) {
							}
						} else {
							bitmap = ImageUtils.getRoundedCornerBitmap(bitmap, ScreenUtils.dip2px(10));
						}
						if (bitmap != null) {
							isSuccess = true;
							ImageUtils.saveFile(bitmap, path);
						}

						if (type == TYPE_BIG) {
							localBook.bigImagePath = path;
						} else {
							localBook.smallImagePath = path;
						}
						if (DBLocalBookHelper.isExistIntDB(localBook.book_id)) {
							localBook.save();
						}
					} else {

					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String notice;
				if (type == TYPE_BIG) {
					notice = "大图";
				} else {
					notice = "小图";
				}
				if (isSuccess) {
					notice = "获取" + notice + "成功";
					localBook.needFreshImage = true;
					downloadThreadQueue.refresh(localBook);
					localBook.needFreshImage = false;// 刷新完成后，置为null
				} else {
					// localBook.stateLoad = LocalBook.STATE_LOAD_FAILED;
					notice = "获取" + notice + "失败";
				}
				Log.i("DownloadThread", "notice==" + notice);
				if (sIsnotice) {
					MZLog.d("wangguodong", notice);
				}

			};
		};

		if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(url)) {
			RequestManager manager = new RequestManager();
			ServiceClient.getImageInstance().issueUserloadImageRequest(MZBookApplication.getContext(), onDownloadListener, url, null, path,
					"" + localBook.book_id, manager);
		}
	}

	/**
	 * 从URL中取出后缀
	 * @param url 下载路径
	 * @return 后缀名
	 */
	public static String getSuffix(String url) {
		int index = url.lastIndexOf(".");
		String imageSuffix = url.substring(index);
		int index_ques = imageSuffix.indexOf("?");
		if (index_ques > 0)
			imageSuffix = imageSuffix.substring(0, index_ques);
		Log.i("zhouob", "imageSuffix===" + imageSuffix);
		return imageSuffix;
	}

	
	
	public DownloadedAble getDownloadAbler() {
		return localBook;
	}

	public void setLocalBook(DownloadedAble localBook) {
		this.localBook = (LocalBook) localBook;
	}
}
