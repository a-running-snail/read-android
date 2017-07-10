package com.jingdong.app.reader.download.manager;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.jingdong.app.reader.download.cfg.DownloadConfiguration;
import com.jingdong.app.reader.download.db.ThreadDAO;
import com.jingdong.app.reader.download.db.ThreadDAOImpl;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.entity.DownloadState;
import com.jingdong.app.reader.download.entity.ThreadInfo;
import com.jingdong.app.reader.download.listener.DownloadInitListener;
import com.jingdong.app.reader.download.listener.DownloadListener;
import com.jingdong.app.reader.download.service.DownloadService;
import com.jingdong.app.reader.util.KSICibaTranslate;

public class DownloadTools {

	public static DownloadTools mInstance;
	private List<DownloadFileInfo> mFileList = new ArrayList<DownloadFileInfo>();
	private static int currentFinishSize  = 0 ; //已下载完成的词典累计大小
	private String word;//当前查询的词语
	public static boolean isRunning = false;
	private Handler mHandler;
	
	private DownloadTools() {}
	
	public static DownloadTools getInstance() {
		if (mInstance == null) {
			synchronized (DownloadTools.class) {
				if (mInstance == null) {
					mInstance = new DownloadTools();
				}
			}
		}
		return mInstance;
	}
	
	public List<DownloadFileInfo> getmFileList() {
		return mFileList;
	}

	public void setmFileList(List<DownloadFileInfo> mFileList) {
		this.mFileList = mFileList;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
	
	public Handler getmHandler() {
		return mHandler;
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public void startDownload(){
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				currentFinishSize  = 0 ;
				if(mFileList!=null && mFileList.size() > 0)
					fileDown(mFileList.get(0));
			}
		});
		thread.start();
	}
	

	/** 
    * 单线程文件下载 
    * @param url 
    * @param path 
    */ 
   public void fileDown(DownloadFileInfo downloadfile) { 
	   isRunning = true;
       Message message = new Message(); 
       if(!"".equals(downloadfile.getUrl())){ 
           String fileName = downloadfile.getFileName();
           InputStream inputStream = null; 
           FileOutputStream outputStream = null; 
           try { 
               URL myuUrl = new URL(downloadfile.getUrl()); 
               URLConnection connection = myuUrl.openConnection(); 
               connection.connect(); 
               inputStream = connection.getInputStream(); 
               int fileSize = connection.getContentLength(); 
               if(fileSize <= 0){ 
                   message.what = 0; 
                   if(mHandler!=null)
                	   mHandler.sendMessage(message); 
                   return; 
               }else{ 
                   outputStream = new FileOutputStream(downloadfile.getStoragePath() +"/"+ fileName); 
                   //存储文件 
                   
                   int count = 0 ;
                   for (int i = 0; i < 5; i++) {
                	   count = inputStream.available();
                	   if(count!=0)
                		   break;
                   }
                   
                   byte buf[] = new byte[count]; 
                   int len = 0; 
                   int temp = 0; 
                   while ((len = inputStream.read(buf)) != -1 ) { 
                       outputStream.write(buf, 0, len); 
                       temp += len; 
                       Message message2 = new Message(); 
                       message2.what = 1; 
                       message2.obj = currentFinishSize + temp;//下载的百分比 
                       if(mHandler!=null)
                    	   mHandler.sendMessage(message2); 
                   } 
                   Message msg = new Message(); 
                   //通知下载完成 
                   msg.what = -1; 
                   msg.obj = fileName;
                   currentFinishSize += temp;
                   if(mHandler!=null)
                	   mHandler.sendMessage(msg); 
                   
                   mFileList.remove(0);
                   if(mFileList.size()!= 0){
                   	fileDown(mFileList.get(0));
                   }else{
                	   isRunning = false;
						if (!TextUtils.isEmpty(word)) {
							KSICibaTranslate.getInstance();
							KSICibaTranslate.refreshEngine();
							KSICibaTranslate.getInstance().getTranslateResult(word);
						}
                   }
               } 
           } catch (Exception e) { 
               message.what = 2; 
               if(mHandler!=null)
            	   mHandler.sendMessage(message); 
               return; 
           }finally{ 
               try { 
                   if(inputStream != null || outputStream != null){ 
                       outputStream.close(); 
                       inputStream.close(); 
                   } 
               } catch (Exception e2) { 
               } 
           } 
       } 
   } 
}
