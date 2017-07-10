package com.jingdong.app.reader.timeline.model;

import java.io.File;
import java.util.ArrayList;
import org.apache.http.Header;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import com.jingdong.app.reader.album.FileUtils;
import com.jingdong.app.reader.album.LoadingDialog;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.UploadFileUtils;
import com.jingdong.app.reader.util.UploadFileUtils.UploadRefreshListener;

public class TimelineActivityModel extends ObservableModel {
	public final static int POST_TWEET = 15;
	public final static int POST_TWEET_NEW = 20;
	public final static int MB = 1024*1024;
	
	private static ArrayList<String> yunpanUrl = null;
	private static int imgSize = 0;
	/** 发布加载中对话框 */
	private LoadingDialog mLoadingDialog = null;

	/**
	 * 发送一条动态
	 * @param context 数据上下文
	 * @param bundle 包含消息内容的bundle
	 */
	public void postTweet(final Context context, final Bundle bundle) {
		if(null == mLoadingDialog) {
			mLoadingDialog = new LoadingDialog(context);
			mLoadingDialog.setCancelable(false);
		}
		showLoadingDialog();
		if(null == yunpanUrl) {
			yunpanUrl = new ArrayList<String>();
		}
		yunpanUrl.clear();
		ArrayList<String> path = bundle.getStringArrayList(TimelinePostTweetActivity.IMAGE_LISTS);
		if(null != path && path.size() > 0) {
			imgSize = path.size();
			uploadImage(context, bundle);
		}else {
			releaseTalk(context, bundle);
		}
	}
	
	/**
	* @Description: 上传图片到云盘
	* @author xuhongwei1
	* @date 2015年10月26日 下午6:13:45 
	* @throws 
	*/ 
	private void uploadImage(final Context context, final Bundle bundle) {
		ArrayList<String> path = bundle.getStringArrayList(TimelinePostTweetActivity.IMAGE_LISTS);
		for(int i=0; i<path.size(); i++) {
			String filename = path.get(i);
			if(FileUtils.getFileSize(filename) >= 4*MB) {
				if(!filename.contains("DraftsBox")) {
					String tempPath = Environment .getExternalStorageDirectory()+"/JDReader/DraftsBox/"+File.separator+ LoginUser.getpin() +File.separator;
					File dir = new File(tempPath);
					if(!dir.exists()) {
						dir.mkdirs();
					}
					String fname = filename.substring(filename.lastIndexOf("/") + 1);
					String newFileName = tempPath + File.separator + fname;
					FileUtils.copyFile(filename, newFileName);
					filename = newFileName;
				}
				FileUtils.compressImage(filename);
			}
			
			UploadFileUtils uploadFileUtils = new UploadFileUtils(
					new UploadRefreshListener() {
						@Override
						public void onUpLoadSuccessed(String path, String msg, String yunUrl, String size) {
							if(path.contains("DraftsBox")) {
								File file = new File(path);
								if(file.exists()) {
									file.delete();
								}
							}
							checkUploadImageIsFinish(context, bundle, yunUrl);
						}

						@Override
						public void onUpLoadProgressRefresh(String path, double progress) {
							
						}

						@Override
						public void onUpLoadFailed(String path,String msg) {
							checkUploadImageIsFinish(context, bundle, "null");
						}
					});
			uploadFileUtils.uploadImageToYun(((Activity)context), filename);
		}
	}
	
	/**
	* @Description: 检查图片是否上传完成
	* @author xuhongwei1
	* @date 2015年10月27日 上午10:55:56 
	* @throws 
	*/ 
	private synchronized void checkUploadImageIsFinish(final Context context, Bundle bundle, String yunUrl) {
		yunpanUrl.add(yunUrl);
		if(yunpanUrl.size() == imgSize) {
			Bundle mBundle = bundle;
			mBundle.putStringArrayList("image_urls", yunpanUrl);
			releaseTalk(context, mBundle);
		}
	}
	
	/**
	* @Description: 发布动态
	* @author xuhongwei1
	* @date 2015年10月27日 上午10:56:15 
	* @throws 
	*/ 
	private void releaseTalk(final Context context, final Bundle bundle) {
		((Activity)context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				WebRequestHelper.post(URLText.postTweetUrl, RequestParamsPool.postTweetParams(bundle), true, new MyAsyncHttpResponseHandler(context) {
					
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						hideLoadingDialog();
						String result =new String (responseBody);
						try{
						JSONObject object=new JSONObject(result);
						MZLog.d("wangguodong", result);
						int code =object.optInt("code");
						String message =object.optString("message","");
						if(code==0)
						{
							Toast.makeText(context, "说说已经发表成功了！", Toast.LENGTH_LONG).show();
							notifyDataChanged(POST_TWEET_NEW, parsePostResult(result));
							
							if(context instanceof TimelinePostTweetActivity) {
								TimelinePostTweetActivity timeline = (TimelinePostTweetActivity)context;
								timeline.exit();
							}
						}
						else {
							Toast.makeText(context, message, Toast.LENGTH_LONG).show();
							hideLoadingDialog();
						}
						}catch(Exception e){
							e.printStackTrace();
							hideLoadingDialog();
						}
						
					}
					
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						hideLoadingDialog();
						if (!NetWorkUtils.isNetworkConnected(context)) {
							TimelinePostTweetActivity timeline = (TimelinePostTweetActivity)context;
							timeline.saveDraft(false);
							Toast.makeText(context, "当前无网络，已保存到草稿箱，请到“我-草稿箱中查看”", Toast.LENGTH_LONG).show();
							timeline.exit();
						}else {
							Toast.makeText(context, "说说发表失败了，请再试试！", Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
	}
	
	private void showLoadingDialog() {
		if(!mLoadingDialog.isShowing()) {
			mLoadingDialog.show();
		}
	}
	
	private void hideLoadingDialog() {
		if(mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
	}
}
