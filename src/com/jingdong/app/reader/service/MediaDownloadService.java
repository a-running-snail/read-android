package com.jingdong.app.reader.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.URLUtil;

import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.util.MZLog;

public class MediaDownloadService extends IntentService {

    public static final String ACTION_MEDIA_DOWNLOAD = "com.jingdong.app.reader.service.mediadownload";
    public static final String MediaUrlPathKey = "MediaUrlPathKey";
    public static final String MediaSavePathKey = "MediaSavePathKey";
    public static final String MediaDownloadSuccessKey = "MediaDownloadSuccessKey";

    private static final String TAG = "MediaDownloadService";

    public static Vector<String> mediaDownloadingQueue = new Vector<String>();
    
    public static void addDownloadUrl(String url) {
        if (!mediaDownloadingQueue.contains(url)) {
            mediaDownloadingQueue.add(url);
        }
    }
    
    public MediaDownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlPath = intent.getStringExtra(MediaUrlPathKey);
        String savePathText = intent.getStringExtra(MediaSavePathKey);
        String fileName = URLUtil.guessFileName(urlPath, null, null);
        MZLog.d(TAG, "URL: " + urlPath + " Title: " + savePathText);
        
        HttpURLConnection conn = null;
        OutputStream output = null;
        File saveFile = null;
        boolean downloadSuccess = true;
        try {
            if (!mediaDownloadingQueue.contains(urlPath)) {
                mediaDownloadingQueue.add(urlPath);
            }
            URL url = new URL(urlPath);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20 * 60 * 1000);
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            
            InputStream fileIs = conn.getInputStream();
            File savePath = new File(savePathText);
            savePath.mkdirs();
            saveFile = new File(savePath, fileName);
            output = new FileOutputStream(saveFile);
            IOUtil.copy(fileIs, output);
            output.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
            downloadSuccess = false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            IOUtil.closeStream(output);
            if (!downloadSuccess && saveFile != null && saveFile.exists()) {
                saveFile.delete();
            }
            mediaDownloadingQueue.remove(urlPath);
            Intent downloadIntent = new Intent(ACTION_MEDIA_DOWNLOAD);
            downloadIntent.putExtra(MediaUrlPathKey, urlPath);
            downloadIntent.putExtra(MediaDownloadSuccessKey, downloadSuccess);
            LocalBroadcastManager.getInstance(this).sendBroadcast(downloadIntent);
        }
    }

}
