package com.jingdong.app.reader.service.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Locale;

import javax.crypto.Cipher;

import org.json.JSONObject;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.book.SerializableBook;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.io.EpubImporter;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.io.Unzip;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.jingdong.app.reader.util.ToastUtil;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

public class DownloadTask extends Thread {

	public final static int TIME_OUT = 30000;
	public final static int BUFFER_SIZE = 1024 * 4;

	private TaskInfo mCurrentTaskInfo;
	private DownloadTaskListener listener;
	private double downloadPercent;
	private Context mContext;

	public DownloadTask(Context mContext, TaskInfo info,
			DownloadTaskListener listener) {
		super();
		this.mContext = mContext;
		this.mCurrentTaskInfo = info;
		this.listener = listener;
	}

	@Override
	public void run() {

		if (getCurrentTaskInfo().getDownloadType().equals(TaskInfo.DOWNLOAD_FILE_TYPE_EBOOK)) {
			startDownloadEbook();
		} else if (getCurrentTaskInfo().getDownloadType().equals(TaskInfo.DOWNLOAD_FILE_TYPE_DOCUMENT)) {
			startDownloadDocument();
		} else {
			startDownloadOther();
		}
		DownloadService.removeFinishedTask(mCurrentTaskInfo.getIdentity());

	}

	public void startDownloadEbook() {

		SerializableBook bookInfo = getCurrentTaskInfo().getDetail().getBook();
		int edition = getCurrentTaskInfo().getDetail().getEdition();
		long purchaseTime = getCurrentTaskInfo().getDetail().getPurchaseTime();

		if (edition == -1 || null == bookInfo || bookInfo.ebookId == 0) {
			if (listener != null)
				listener.errorDownload(this,
						"download error:edition has probleam or bookinfo is null");
			return;
		}

		String publicKeyText = null;
		PrivateKey privateKey = null;
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			KeyPair keyPair = kpg.genKeyPair();

			byte[] pub = keyPair.getPublic().getEncoded();
			privateKey = keyPair.getPrivate();
			publicKeyText = Base64.encodeToString(pub, Base64.NO_WRAP);
			publicKeyText = URLEncoder.encode(publicKeyText, "UTF-8");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			if (listener != null)
				listener.errorDownload(this,
						"download error:NoSuchAlgorithmException");
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			if (listener != null)
				listener.errorDownload(this,
						"download error:UnsupportedEncodingException");
			return;
		}

		String authToken = LocalUserSetting.getToken(mContext);
		if (TextUtils.isEmpty(authToken)) {
			if (listener != null)
				listener.errorDownload(this,
						"download error:authToken is empty");
			return;
		}
		String userId = LoginUser.getpin();
		String postText = "ebook_id=" + bookInfo.ebookId + "&edition="
				+ edition + "&public_key=" + publicKeyText + "&auth_token="
				+ authToken;

		int entityId;
		String urlPath;
		try {
			String text = WebRequest.postWebDataWithContext(mContext,
					getCurrentTaskInfo().getDownloadUrl(), postText);
			JSONObject json = new JSONObject(text);
			urlPath = json.getString("download_link");
			String encryptedKey = json.getString("encrypted_key");
			entityId = json.getInt("ebook_entity_id");
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] key = cipher.doFinal(Base64.decode(encryptedKey,
					Base64.DEFAULT));
			String aesKey = new String(key, "UTF-8");
			if (purchaseTime == 0) {
				purchaseTime = System.currentTimeMillis();
			}

			MZBookDatabase.instance.insertOrUpdatePurchase(userId,
					bookInfo.ebookId, edition, purchaseTime);
			MZBookDatabase.instance.insertOrUpdateBookName(bookInfo.ebookId,
					aesKey);
			MZBookDatabase.instance.insertOrUpdateEBook(bookInfo);

		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null)
				listener.errorDownload(this,
						"download error:access encrypted key error");
			return;
		}

		String bookName = String.valueOf(entityId);
		File fileDir = new File(StoragePath.getBookDir(mContext), bookName);
		fileDir.mkdirs();
		File filePath = new File(fileDir, bookName + ".epub");
		startDownload(bookName, filePath, urlPath);
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(filePath);
			Unzip.unzip(fin, fileDir.getPath() + File.separator + "content");
			dealWithEbook(bookInfo, edition, entityId, authToken, userId);
			if (listener != null)// 下载完成
				listener.finishDownload(this);
		} catch (IOException e) {
			e.printStackTrace();
			IOUtil.closeStream(fin);
		}

	}

	public void startDownloadDocument() {
		String urlPath = getCurrentTaskInfo().getDownloadUrl();
		String bookName = getCurrentTaskInfo().getDetail().getBookName();
		int serverid = getCurrentTaskInfo().getDetail().getServerId();
		if (!bookName.toLowerCase(Locale.getDefault()).endsWith(".epub")) {
			bookName = bookName + ".epub";
		}
		File tmpFileDir = mContext.getExternalCacheDir();
		tmpFileDir.mkdirs();
		File tmpFile = new File(tmpFileDir, bookName);
		startDownload(bookName, tmpFile, urlPath);
		if (listener != null)// 下载完成
			listener.finishDownload(this);

	}

	public void startDownloadOther() {
		// 扩展
	}

	public void dealWithEbook(SerializableBook bookInfo, int edition,
			int entityId, String authToken, String userId) {

		int oldEntityId = MZBookDatabase.instance.getLocalEntityId(
				bookInfo.ebookId, edition);
		if (oldEntityId > 0 && oldEntityId != entityId) {
			File oldFileDir = new File(StoragePath.getBookDir(mContext),
					String.valueOf(oldEntityId));
			if (oldFileDir.exists()) {
				IOUtil.deleteFile(oldFileDir);
			}
			MZBookDatabase.instance.deleteBookPage(bookInfo.ebookId, 0);
			MZBookDatabase.instance.deletePageContent(bookInfo.ebookId, 0);
		}

		if (edition == 0) {
			// if purchase complete version, we need delete trial version if
			// exist
			int trialEntityId = MZBookDatabase.instance.getLocalEntityId(
					bookInfo.ebookId, 1);
			if (trialEntityId > 0) {
				File trialFileDir = new File(StoragePath.getBookDir(mContext),
						String.valueOf(trialEntityId));
				if (trialFileDir.exists()) {
					IOUtil.deleteFile(trialFileDir);
				}
			}
			MZBookDatabase.instance.deleteBookPage(bookInfo.ebookId, 0);
			MZBookDatabase.instance.deletePageContent(bookInfo.ebookId, 0);
		}

		MZBookDatabase.instance.insertOrUdapteLocalBook(bookInfo.ebookId,
				entityId, edition);
		// 书籍被下载或购买 更新最后阅读时间 优先显示
		EBook eBook = new EBook();
		eBook.readAt = System.currentTimeMillis();
		eBook.ebookId = bookInfo.ebookId;
		MZBookDatabase.instance.updateEBookLastReadTime(eBook);
		// 更新书架表
		MZBookDatabase.instance.saveToBookShelf(eBook.ebookId, eBook.readAt, 0,
				userId);
		WebRequest.postWebDataWithContext(mContext, URLText.downloadSuccessUrl,
				"ebook_entity_id=" + bookInfo.getEntityIdWithEdition(edition)
						+ "&auth_token=" + authToken);
	}

	public void startDownload(String bookName, File outputFile, String targetUrl) {

		HttpURLConnection conn = null;
		InputStream is = null;
		OutputStream output = null;
		URL url = null;
		try {
			StatisticsReportUtil.readDeviceUUID();
			if (StatisticsReportUtil.getValidDeviceUUIDByInstant() == null) {
				ToastUtil.showToastInThread("获取不到设备号,请检查是否禁用了相关权限", Toast.LENGTH_LONG);
			}
			url = new URL(targetUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(20 * 60 * 1000);
			conn.setConnectTimeout(TIME_OUT);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			long fileLength = conn.getContentLength();
			is = conn.getInputStream();
			output = new FileOutputStream(outputFile);
			byte[] b = new byte[BUFFER_SIZE];
			long lengthSoFar = 0;
			int read;
			while ((read = is.read(b)) != -1) {
				output.write(b, 0, read);
				lengthSoFar += read;
				double downloadPercent = 0;
				if (fileLength > 0) {
					downloadPercent = (double) lengthSoFar
							/ (double) fileLength * 0.99;
					if (downloadPercent > 0.99) {
						downloadPercent = 0.99;
					}
					setDownloadPercent(downloadPercent);
					if (listener != null)
						listener.updateProcess(this);

				}
			}
			output.flush();

			if (getCurrentTaskInfo().getDownloadType().equals(
					TaskInfo.DOWNLOAD_FILE_TYPE_DOCUMENT)) {
				EpubImporter.importBook( bookName, outputFile, mContext,getCurrentTaskInfo().getDetail().docBind);
				//删除临时文件
				IOUtil.deleteFile(outputFile);
			}
			output.close();
			is.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			IOUtil.deleteFile(outputFile);
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			IOUtil.closeStream(is);
			IOUtil.closeStream(output);
		}
	}

	public TaskInfo getCurrentTaskInfo() {
		return mCurrentTaskInfo;
	}

	public void setCurrentTaskInfo(TaskInfo mCurrentTaskInfo) {
		this.mCurrentTaskInfo = mCurrentTaskInfo;
	}

	public DownloadTaskListener getListener() {
		return listener;
	}

	public void setListener(DownloadTaskListener listener) {
		this.listener = listener;
	}

	public double getDownloadPercent() {
		return downloadPercent;
	}

	public void setDownloadPercent(double downloadPercent) {
		this.downloadPercent = downloadPercent;
	}

}
