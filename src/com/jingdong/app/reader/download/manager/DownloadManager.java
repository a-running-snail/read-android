package com.jingdong.app.reader.download.manager;

import java.util.List;

import android.content.Context;
import android.content.Intent;

import com.jingdong.app.reader.download.cfg.DownloadConfiguration;
import com.jingdong.app.reader.download.db.ThreadDAO;
import com.jingdong.app.reader.download.db.ThreadDAOImpl;
import com.jingdong.app.reader.download.entity.DownloadFileInfo;
import com.jingdong.app.reader.download.entity.DownloadState;
import com.jingdong.app.reader.download.entity.ThreadInfo;
import com.jingdong.app.reader.download.listener.DownloadInitListener;
import com.jingdong.app.reader.download.listener.DownloadListener;
import com.jingdong.app.reader.download.service.DownloadService;

/**
 * 
 *
 *
 *                #####################################################
 *                #                                                   #
 *                #                       _oo0oo_                     #
 *                #                      o8888888o                    #
 *                #                      88" . "88                    #
 *                #                      (| -_- |)                    #
 *                #                      0\  =  /0                    #
 *                #                    ___/`---'\___                  #
 *                #                  .' \\|     |# '.                 #
 *                #                 / \\|||  :  |||# \                #
 *                #                / _||||| -:- |||||- \              #
 *                #               |   | \\\  -  #/ |   |              #
 *                #               | \_|  ''\---/''  |_/ |             #
 *                #               \  .-\__  '-'  ___/-. /             #
 *                #             ___'. .'  /--.--\  `. .'___           #
 *                #          ."" '<  `.___\_<|>_/___.' >' "".         #
 *                #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 *                #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 *                #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 *                #                       `=---='                     #
 *                #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 *                #                                                   #
 *                #               佛祖保佑         永无BUG                 #
 *                #                                                   #
 *                #####################################################
 *
 *
 *
 * @ClassName: DownloadManager
 * @Description: 下载管理类（提供注册下载回调监听，初始化、下载、暂停等方法）
 * @author J.Beyond
 * @date 2015年8月12日 下午5:07:06
 *
 */
public class DownloadManager {

	private static DownloadManager mInstance;
	private ThreadDAO mDao;
	private DownloadManager() {}
	
	public static DownloadManager getInstance() {
		if (mInstance == null) {
			synchronized (DownloadManager.class) {
				if (mInstance == null) {
					mInstance = new DownloadManager();
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 
	 * @Title: registCallbackListener
	 * @Description: 注册监听
	 * @param @param url
	 * @param @param listener
	 * @return void
	 * @throws
	 */
	public void registListener(DownloadFileInfo fileInfo,DownloadListener listener) {
		CallbackManager.getInstance().addListener(fileInfo, listener);
	}
	
	/**
	 * 
	 * @Title: init
	 * @Description: 初始化方法，用于恢复之前下载的进度
	 * @param @param context
	 * @param @param fileInfo
	 * @param @param initListener
	 * @return void
	 * @throws
	 */
	public void init(Context context,DownloadFileInfo fileInfo,DownloadInitListener initListener) {
		try {
			this.mDao = new ThreadDAOImpl(context);
			List<ThreadInfo> threadList = mDao.getThreads(fileInfo.getUrl());
			if (threadList.size() == 0) {
				//首次下载
				fileInfo.setDownloadState(DownloadState.DOWNLOAD_NOTSTART);
				fileInfo.setInitialized(true);
				if (initListener != null) {
					initListener.onInitSuccess(fileInfo);
				}
			}else{
				long finished=0;
				long length = 0;
				for (ThreadInfo threadInfo : threadList) {
					finished += threadInfo.getFinished();
					length = threadInfo.getTotal();
				}
				if (length == 0) {
					if (initListener != null) {
						initListener.onInitFail(fileInfo,"");
					}
					return;
				}
				int progress = (int) (finished * 100 / length);
				fileInfo.setProgress(progress);
				fileInfo.setInitialized(true);
				if (finished == length) {
					fileInfo.setDownloadState(DownloadState.DOWNLOAD_SUCCESS);
				}else{
					fileInfo.setDownloadState(DownloadState.DOWNLOAD_PAUSE);
				}
				if (initListener != null) {
					initListener.onInitSuccess(fileInfo);
				}
			}
			
		} catch (Exception e) {
			if (initListener != null) {
				initListener.onInitFail(fileInfo,"文件初始化错误");
			}
		}
	}
	
	/**
	 * 
	 * @Title: confirmDownloadInMobileNet
	 * @Description: 确定在运营商网络下载
	 * @param @param context
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public void confirmDownloadInMobileNet(Context context,DownloadFileInfo fileInfo) {
		DownloadConfiguration.CONFIRM_DOWNLOAD_IN_MOBILE_NET = true;
		executeDownload(context, fileInfo);
	}
	
	/**
	 * 
	 * @Title: executeDownload
	 * @Description: 下载
	 * @param @param context
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public void executeDownload(Context context,DownloadFileInfo fileInfo) {
		if (context == null) {
			throw new RuntimeException("context can not be null!");
		}
		//通知service停止下载
		Intent intent = new Intent(context,DownloadService.class);
		intent.setAction(DownloadService.ACTION_START);
		intent.putExtra("fileInfo", fileInfo);
		context.startService(intent);
	}
	
	/**
	 * 
	 * @Title: executePause
	 * @Description: 暂停
	 * @param @param context
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public void executePause(Context context,DownloadFileInfo fileInfo){
		if (context == null) {
			throw new RuntimeException("context can not be null!");
		}
//		fileInfo.setDownloadState(DownloadState.DOWNLOAD_PAUSE);
		//通知service停止下载
		Intent intent = new Intent(context,DownloadService.class);
		intent.setAction(DownloadService.ACTION_PAUSE);
		intent.putExtra("fileInfo", fileInfo);
		context.startService(intent);
	}
	
	/**
	 * 
	 * @Title: executeResume
	 * @Description:继续下载
	 * @param @param context
	 * @param @param fileInfo
	 * @return void
	 * @throws
	 */
	public void executeResume(Context context,DownloadFileInfo fileInfo){
		if (context == null) {
			throw new RuntimeException("context can not be null!");
		}
		//通知service停止下载
		Intent intent = new Intent(context,DownloadService.class);
		intent.setAction(DownloadService.ACTION_RESUME);
		intent.putExtra("fileInfo", fileInfo);
		context.startService(intent);
	}
	
	
	
}
