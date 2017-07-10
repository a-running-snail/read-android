package com.jingdong.app.reader.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.jingdong.app.reader.io.EpubImporter;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.R;
@Deprecated
public class OpdsBookDownloadService extends IntentService {
	public static final String OpdsBookUrlPathKey = "OpdsBookUrlPathKey";
	public static final String OpdsBookNameKey = "OpdsBookNameKey";
	public static final String DOWNLOAD_FINISH = "com.mzread.action.download.finished";
	public static final String ACTION_DOCUMENT_DOWNLOAD = "com.jingdong.app.reader.service.document.download";
	public static final String DownLoadPercentKey = "DownLoadPercentKey";
	public static final String DownLoadDocumentId = "DownLoadDocumentIdKey";

	private static int globalNotifyId = 1;
	private static final String TAG = "OpdsBookDownloadService";
	private LocalBroadcastManager localBroadcastManager;

	public OpdsBookDownloadService() {
		super(TAG);
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String urlPath = intent.getStringExtra(OpdsBookUrlPathKey);
		String bookName = intent.getStringExtra(OpdsBookNameKey);
		int serverid =intent.getIntExtra(DownLoadDocumentId,-1);;
		MZLog.d(TAG, "URL: " + urlPath + " Title: " + bookName);

		if (!bookName.toLowerCase(Locale.getDefault()).endsWith(".epub")) {
			bookName = bookName + ".epub";
		}

		NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.icon)
				.setContentTitle(getString(R.string.book_downloading))
				.setContentText(bookName);

		// Gets an instance of the NotificationManager service
		NotificationManager nofityManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Sets an activity indicator for an operation of indeterminate length
		notifyBuilder.setProgress(0, 0, true);
		// Issues the notification
		int notifyId = globalNotifyId;
		nofityManager.notify(notifyId, notifyBuilder.build());
		++globalNotifyId;

		HttpURLConnection conn = null;
		OutputStream output = null;
		File tmpFile = null;
		try {
			URL url = new URL(urlPath);
			conn = (HttpURLConnection) url.openConnection();
			//conn.setRequestProperty("User-Agent", "MZBook");
			conn.setReadTimeout(20 * 60 * 1000);
			conn.setConnectTimeout(10 * 1000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();

			long fileLength = conn.getContentLength();

			InputStream tmpFileIs = conn.getInputStream();
			File tmpFileDir = getExternalCacheDir();
			tmpFileDir.mkdirs();
			tmpFile = new File(tmpFileDir, bookName);
			output = new FileOutputStream(tmpFile);

			byte[] b = new byte[4096];
			int read;
			long lengthSoFar = 0;
			while ((read = tmpFileIs.read(b)) != -1) {
				lengthSoFar += read;
				output.write(b, 0, read);
				double downloadPercent = 0;
				if (fileLength > 0) {
					downloadPercent = (double) lengthSoFar
							/ (double) fileLength * 0.99;
					if (downloadPercent > 0.99) {
						downloadPercent = 0.99;
					}
					// purchaseProgress.put(bookInfo.ebookId, downloadPercent);
					Intent downloadIntent = new Intent(ACTION_DOCUMENT_DOWNLOAD);
					if(serverid!=-1)
						downloadIntent.putExtra(DownLoadDocumentId,serverid);
					downloadIntent
							.putExtra(DownLoadPercentKey, downloadPercent);
					localBroadcastManager.sendBroadcast(downloadIntent);
				}
			}

			output.flush();

			EpubImporter.importBook(bookName, tmpFile, this);
			final String downloadFinish = "\"" + bookName + "\""
					+ getString(R.string.download_finish);
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), downloadFinish,
							Toast.LENGTH_LONG).show();

				}
			});
			// 书籍下载完成
			LocalBroadcastManager.getInstance(getApplicationContext())
					.sendBroadcast(new Intent("com.mzread.action.downloaded"));

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			IOUtil.closeStream(output);
			if (tmpFile != null && tmpFile.exists()) {
				tmpFile.delete();
			}
			nofityManager.cancel(notifyId);
		}
	}

}
