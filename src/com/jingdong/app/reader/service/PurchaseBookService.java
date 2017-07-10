package com.jingdong.app.reader.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.BookEntity;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.io.StoragePath;
import com.jingdong.app.reader.io.Unzip;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;

@Deprecated

/*
 * 此类不建议使用了 请使用DownloadService
 */
public class PurchaseBookService extends IntentService {

    public static final String ACTION_BOOK_DOWNLOAD = "com.jingdong.app.reader.service.bookdownload";
    public static final String DownLoadPercentKey = "DownLoadPercentKey";
    public static final String PurchaseTimeKey = "PurchaseTimeKey";
    public static final String EbookIdKey = "EbookIdKey";
    public static final String EditionKey = "EditionKey";
    public static final String BookKey = "BookKey";
    private static final String TAG = "BookDownloadService";
    public static final int INVILID_VALUE = -1;
    
    public static Hashtable<Integer, Integer> bookDownloadingQueue;
    
    private LocalBroadcastManager localBroadcastManager;

    static {
        bookDownloadingQueue = new Hashtable<Integer, Integer>();
    }

    public PurchaseBookService() {
        super(TAG);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String bookInfoText = intent.getStringExtra(BookKey);
        int edition = intent.getIntExtra(EditionKey, INVILID_VALUE);
        long purchaseTime = intent.getLongExtra(PurchaseTimeKey, 0);

        if (edition == -1 || TextUtils.isEmpty(bookInfoText)) {
            return;
        }

        
        //以下需要优化  遗留问题 开始
        Book bookInfo = null;
        try {
            JSONObject json = new JSONObject(bookInfoText);
            bookInfo = Book.fromJSON(json);
            if (bookInfo.ebookId == 0) {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
      //以下需要优化  遗留问题 结束
        
        bookDownloadingQueue.put(bookInfo.ebookId, edition);
        MZLog.d("wangguodong", bookDownloadingQueue.size()+"====099090909090");
        
        
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
            bookDownloadingQueue.remove(bookInfo.ebookId);
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            bookDownloadingQueue.remove(bookInfo.ebookId);
            return;
        }

        String authToken = LocalUserSetting.getToken(this);
        if (TextUtils.isEmpty(authToken)) {
            bookDownloadingQueue.remove(bookInfo.ebookId);
            return;
        }
        String userId = LoginUser.getpin();
        String postText = "ebook_id=" + bookInfo.ebookId + "&edition=" + edition + "&public_key=" + publicKeyText
                + "&auth_token=" + authToken;

        int entityId;
        String urlPath;
        try {
            String text = WebRequest.postWebDataWithContext(this, URLText.purchaseUrl, postText);
            JSONObject json = new JSONObject(text);
            urlPath = json.getString("download_link");
            String encryptedKey = json.getString("encrypted_key");
            entityId = json.getInt("ebook_entity_id");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] key = cipher.doFinal(Base64.decode(encryptedKey, Base64.DEFAULT));
            String aesKey = new String(key, "UTF-8");

            if (purchaseTime == 0) {
                purchaseTime = System.currentTimeMillis();
            }

            
            MZBookDatabase.instance.insertOrUpdatePurchase(userId, bookInfo.ebookId, edition, purchaseTime);
            MZBookDatabase.instance.insertOrUpdateBookName(bookInfo.ebookId, aesKey);
            MZBookDatabase.instance.insertOrUpdateEBook(bookInfo);
            
        } catch (Exception e) {
            e.printStackTrace();
            bookDownloadingQueue.remove(bookInfo.ebookId);
            return;
        }

        String bookName = String.valueOf(entityId);

        HttpURLConnection conn;
        InputStream is = null;
        OutputStream output = null;
        URL url = null;
        FileInputStream fin = null;
        File fileDir = new File(StoragePath.getBookDir(this), bookName);
        fileDir.mkdirs();
        try {
            File filePath = new File(fileDir, bookName + ".epub");
            url = new URL(urlPath);
            conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(20 * 60 * 1000);
            conn.setConnectTimeout(30 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();
            long fileLength = conn.getContentLength();
            MZLog.d(TAG, "The response is: " + response + " The file length is: " + fileLength);
            is = conn.getInputStream();

            for (BookEntity entity : bookInfo.entityList) {
                if (entity.entityId == entityId && entity.fileSize > 0) {
                    fileLength = entity.fileSize;
                }
            }

            if (fileLength < 0) {
                fileLength = is.available();
            }

            output = new FileOutputStream(filePath);
            byte[] b = new byte[4096];
            long lengthSoFar = 0;
            int read;
            while ((read = is.read(b)) != -1) {
                output.write(b, 0, read);
                lengthSoFar += read;
                double downloadPercent = 0;
                if (fileLength > 0) {
                    downloadPercent = (double) lengthSoFar / (double) fileLength * 0.99;
                    if (downloadPercent > 0.99) {
                        downloadPercent = 0.99;
                    }
                    Intent downloadIntent = new Intent(ACTION_BOOK_DOWNLOAD);
                    downloadIntent.putExtra(EbookIdKey, bookInfo.ebookId);
                    downloadIntent.putExtra(EditionKey, edition);
                    downloadIntent.putExtra(DownLoadPercentKey, downloadPercent);
                    localBroadcastManager.sendBroadcast(downloadIntent);
                }
            }

            output.flush();
            MZLog.d(TAG, "Download finished: " + url.toExternalForm());

            fin = new FileInputStream(filePath);

            Unzip.unzip(fin, fileDir.getPath() + File.separator + "content");

            int oldEntityId = MZBookDatabase.instance.getLocalEntityId(bookInfo.ebookId, edition);
            if (oldEntityId > 0 && oldEntityId != entityId) {
                File oldFileDir = new File(StoragePath.getBookDir(this), String.valueOf(oldEntityId));
                if (oldFileDir.exists()) {
                    IOUtil.deleteFile(oldFileDir);
                }
                MZBookDatabase.instance.deleteBookPage(bookInfo.ebookId, 0);
                MZBookDatabase.instance.deletePageContent(bookInfo.ebookId, 0);
            }
            
            if (edition == 0) {
                // if purchase complete version, we need delete trial version if exist
                int trialEntityId = MZBookDatabase.instance.getLocalEntityId(bookInfo.ebookId, 1);
                if (trialEntityId > 0) {
                    File trialFileDir = new File(StoragePath.getBookDir(this), String.valueOf(trialEntityId));
                    if (trialFileDir.exists()) {
                        IOUtil.deleteFile(trialFileDir);
                    }
                }
                MZBookDatabase.instance.deleteBookPage(bookInfo.ebookId, 0);
                MZBookDatabase.instance.deletePageContent(bookInfo.ebookId, 0);
            }

            MZBookDatabase.instance.insertOrUdapteLocalBook(bookInfo.ebookId, entityId, edition);
            //书籍被下载或购买 更新最后阅读时间 优先显示
			EBook eBook = new EBook();
			eBook.readAt = System.currentTimeMillis();
			eBook.ebookId = bookInfo.ebookId;
            MZBookDatabase.instance.updateEBookLastReadTime(eBook);
            //更新书架表
            MZBookDatabase.instance.saveToBookShelf(eBook.ebookId, eBook.readAt, 0, userId);

            Intent downloadIntent = new Intent(ACTION_BOOK_DOWNLOAD);
            downloadIntent.putExtra(EbookIdKey, bookInfo.ebookId);
            downloadIntent.putExtra(EditionKey, edition);
            downloadIntent.putExtra(DownLoadPercentKey, 1.0);
            
            localBroadcastManager.sendBroadcast(downloadIntent);
            localBroadcastManager.sendBroadcast(new Intent("com.mzread.action.downloaded"));

            
			WebRequest.postWebDataWithContext(
					this,
					URLText.downloadSuccessUrl,
					"ebook_entity_id="
							+ bookInfo.getEntityIdWithEdition(edition)
							+ "&auth_token=" + authToken);
        } catch (IOException e) {
            IOUtil.deleteFile(fileDir);
            MZLog.d(TAG, "download file failed: " + urlPath);
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(is);
            IOUtil.closeStream(output);
            IOUtil.closeStream(fin);
            bookDownloadingQueue.remove(bookInfo.ebookId);
        }
    }

}
