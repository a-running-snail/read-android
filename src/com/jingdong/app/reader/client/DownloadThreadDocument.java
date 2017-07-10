package com.jingdong.app.reader.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.epub.epub.ContentReader;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.io.Unzip;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.service.download.DownloadService;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.FileGuider;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ToastUtil;

public class DownloadThreadDocument extends DownloadThread {
	public static String KEY_MESSAGE = "error";

	private LocalDocument localBook;
	public DownloadThreadQueue downloadThreadQueue;

	@Override
	public void run() {
		
		handleLoad(localBook);
	}

	public DownloadedAble getDownloadAbler() {
		return localBook;
	}

	public void setLocalBook(DownloadedAble localBook) {
		this.localBook = (LocalDocument) localBook;
	}

	public DownloadThreadDocument(DownloadThreadQueue downloadThreadQueue,
			DownloadedAble downloadAbler) {
		super(downloadThreadQueue, downloadAbler);
		this.localBook = (LocalDocument) downloadAbler;
		this.downloadThreadQueue = downloadThreadQueue;
		MZLog.d("wangguodong", "thread type"+localBook.format);
	}

	private void getBookContent(final LocalDocument localBook) {
		localBook.downingWhich = LocalDocument.DOWNING_BOOK_CONTENT;
		boolean isSuccess = false;
		RequestEntry entry = null;
		Log.i("DownloadThread", "getBookContent 0000");
		
		localBook.bookUrl=URLText.MZ_BOOK_YUNPAN_DOWNLOAD_URL+"?jd_user_name="+LoginUser.getpin()+"&document_id="+localBook.server_id;
		
		try {

			if (localBook.bookUrl == null) {
				return;
			}
			HttpUriRequest request = new HttpGet(localBook.bookUrl);
			Log.i("DownloadThread", "localBook.bookUrl====" + localBook.bookUrl);
			entry = new RequestEntry(
					RequestEntry.REQUEST_TYPE_SEARCH_POST_BY_JSON, request);
			localBook.setRequestEntry(entry);
			entry._type = RequestEntry.TYPE_FILE;
			Log.i("DownloadThread", "getBookContent 111111");

			entry._downloadListener = new OnDownloadListener() {
				@Override
				public void onprogress(long progress, long max) {
					localBook.progress = progress;
					if (localBook.size < 1) {
						localBook.size = max;
					}
					MZLog.d("wangguodong", "document progress:"+progress);
					downloadThreadQueue.refresh(localBook);
				}

				@Override
				public void onDownloadCompleted(RequestEntry requestEntry) {
				}
			};
			
			//获取document下载地址
			FileGuider savePath = new FileGuider(
					FileGuider.SPACE_PRIORITY_EXTERNAL);
			savePath.setImmutable(true);
			String suffix = ".epub";//getSuffix(localBook.bookUrl);
			if(localBook.format==1)
				suffix=".pdf";
			
			savePath.setChildDirName("/Document/" + localBook._id);
			savePath.setFileName(localBook._id + suffix);
			localBook.book_path = savePath.getParentPath();
			localBook.bookSource = savePath.getFilePath();


			if (localBook.tryGetContetnFromSD) {
				
				String fileNail=".epub";
				if(localBook.format==1) fileNail=".pdf";
				else if (localBook.format==2){
					fileNail=".epub";
				}
				File file = new File(localBook.book_path + "/" + localBook._id
						+ fileNail);
				if (file.exists()) {
					localBook.state = LocalDocument.STATE_LOADED;
					localBook.book_path = localBook.book_path + "/"
							+ localBook._id + fileNail;
					localBook.bookSource = file.getAbsolutePath();
					localBook.size = file.length();
					isSuccess = true;
					MZLog.d("wangguodong", "图书加载成功，请在书架查看");
					handleLoad(localBook);
					return;
				}
			}
	
			entry.fileGuider = savePath;
			if (localBook.size <= 0 || localBook.progress > localBook.size) {
				DownloadThread.delFile(savePath.getFilePath());
			}
			File file = new File(savePath.getFilePath());
			FileInputStream fis = null;
			if (file != null && file.exists()) {
				fis = new FileInputStream(file);
			}
			if (fis != null && fis.available() > 10
					&& fis.available() < localBook.size) {
				entry.start = fis.available();
				localBook.progress = fis.available();
				entry.end = localBook.size;
			} else {
				DownloadThread.delFile(file);
				localBook.progress = 0;
			}
			entry.start = localBook.progress;
			entry.end = localBook.size;
			if (entry.start > 0) {
				request.addHeader("Range", "bytes=" + entry.start + "-");
			}
			MZLog.d("wangguodong", "document downloading start...");
			RequestEntry requestEntry = ServiceClient.execute(entry);
			if (entry.getRequestCode() != localBook.getRequestCode()) {
				MZLog.d("wangguodong", "document downloading111111...");
				return;
			}
			if (requestEntry._statusCode == 0) {
				isSuccess = true;
			} else {
				isSuccess = false;
				if (!TextUtils.isEmpty(requestEntry._stateNotice)) {
					MZLog.d("wangguodong", requestEntry._stateNotice);
				}
			}
			MZLog.d("wangguodong", "document downloading22222...");
		} catch (Exception e) {
			e.printStackTrace();
		}

		int state = LocalDocument.STATE_LOAD_FAILED;
		if (localBook.state == LocalDocument.STATE_LOAD_PAUSED) {
			state = LocalDocument.STATE_LOAD_PAUSED;
			MZLog.d("wangguodong", "document downloading33333...");
		} else if (entry.isSuccess && localBook.progress == localBook.size
				&& localBook.book_path != null) {
			MZLog.d("wangguodong", "document downloading44444...");
			localBook.book_path = entry.fileGuider.getFilePath();
			 if (localBook.format == LocalDocument.FORMAT_EPUB) {
				
				String name = localBook._id + "." + "epub";
				String dir = "/Document/" + localBook._id;
				String path = FileGuider.getPath(
						FileGuider.SPACE_PRIORITY_EXTERNAL, dir, name);
				
				File f = new File(localBook.book_path);
				String fileDir = f.getParent() + File.separator + "content";

				// 解压epub
				MZLog.d("wangguodong", "这本书是epub文件，需要解压！");
				FileInputStream fin = null;
				try {
					MZLog.d("wangguodong", "开始解压EPUB文件");
					fin = new FileInputStream(new File(path));
					Unzip.unzip(fin, fileDir);
					ContentReader.isNeedJDDecrypt = false;
					ContentReader reader = new ContentReader(fileDir);
					localBook.localImageUrl =  reader.getCoverPath();
					
					MZLog.d("wangguodong", "解压EPUB书籍成功");
					state = LocalDocument.STATE_LOADED;
				} catch (IOException e) {
					MZLog.d("wangguodong", "解压EPUB书籍出问题了");
					IOUtil.closeStream(fin);
					state = LocalDocument.STATE_LOAD_FAILED;
				}
				catch (XmlPullParserException e1) {
					MZLog.d("wangguodong", "解压EPUB书籍出问题了");
					IOUtil.closeStream(fin);
					state = LocalDocument.STATE_LOAD_FAILED;
				}
				
			} else {
				MZLog.d("wangguodong", "这本书是pdf文件，跳过解压！");
				state = LocalDocument.STATE_LOADED;
			}
			Log.i("DownloadThread", "localBook.book_path==="
					+ localBook.book_path);
		}

		if (state == LocalDocument.STATE_LOAD_PAUSED) {
			ToastUtil.showToastInThread("下载图书暂停",Toast.LENGTH_SHORT);
			
		} else if (state == LocalDocument.STATE_LOADED) {

			Intent intent = new Intent(
					DownloadService.DOWNLOAD_TASK_FINISH_BROADCAST);
			LocalBroadcastManager.getInstance(MZBookApplication.getContext())
					.sendBroadcast(intent);

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

	public void handleLoad(LocalDocument localBook) {
		handleLoad(localBook, false, false);
	}

	public void handleLoad(LocalDocument localBook, boolean isPassBigPic,
			boolean isPassSmallPic) {
		if (localBook.isMenualStop) {
			isloading = false;
			return;
		}
		downloadThreadQueue.refresh(localBook);
		if (localBook.state == LocalDocument.STATE_LOAD_PAUSED
				|| localBook.state == LocalDocument.STATE_LOAD_FAILED
				|| localBook.state == LocalDocument.STATE_LOADED) {
			localBook.mod_time = System.currentTimeMillis();
			localBook.add_time = (int) System.currentTimeMillis();
			localBook.saveDocument(localBook);
			MZLog.d("wangguodong", "保存更新时间"+localBook.user_id);
			isloading = false;
			Intent intent = new Intent(
					DownloadService.DOWNLOAD_TASK_FINISH_BROADCAST);
			LocalBroadcastManager.getInstance(MZBookApplication.getContext())
					.sendBroadcast(intent);

			return;
		}

		if (TextUtils.isEmpty(localBook.book_path)
				|| localBook.state != LocalDocument.STATE_LOADED) {
			getBookContent(localBook);
		}

	}


	public static String getSuffix(String url) {
		int index = url.lastIndexOf(".");
		String imageSuffix = url.substring(index);
		Log.i("zhouob", "imageSuffix===" + imageSuffix);
		return imageSuffix;
	}


}
