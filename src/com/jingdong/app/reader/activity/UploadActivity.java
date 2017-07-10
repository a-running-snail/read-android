package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.Upload;
import com.jingdong.app.reader.util.Upload.UploadStateListener;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;

public class UploadActivity extends BaseActivityWithTopBar implements TopBarViewListener {

	public static final String TYPE = "OtherWay";



	private ImageView bookImage;
	private TextView bookName;
	private TextView tips;
	private Document document = null;
	private String authToken;
	private String type;
	
	private List<Map<String, String>> localSignList=new ArrayList<Map<String,String>>();;

	private void init() {


		bookImage = (ImageView) findViewById(R.id.book_image);
		bookName = (TextView) findViewById(R.id.book_name);
		tips = (TextView) findViewById(R.id.tips);

		authToken = LocalUserSetting.getToken(UploadActivity.this);

		bookName.setText(document.title);

		bookImage.setImageBitmap(BitmapFactory.decodeFile(document.coverPath));


	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_upload);
		getTopBarView().setRightMenuOneVisiable(true, "上传", R.color.red_main, false);
		Intent intent = getIntent();
		document = intent.getParcelableExtra("document");
		type = intent.getStringExtra("type");
		init();

	}
	public void requestSynBook() {
		
		DocBind bind = MZBookDatabase.instance.getDocBind(
				document.documentId,LoginUser.getpin());
		if(bind==null)
		{
			DocBind tempBind = new DocBind();
			tempBind.documentId = document.documentId;
			tempBind.userId = LoginUser.getpin();
			bind=tempBind;
			MZBookDatabase.instance.insertOrUpdateDocBind(tempBind);
		}
		
		Map<String, String> temp = new HashMap<String, String>();
		temp.put("serverId", bind.serverId + "");
		temp.put("sign", document.opfMD5);
		MZLog.d("wangguodong", "md5"+document.opfMD5);
		temp.put("name", document.title);
		temp.put("documentId", document.documentId + "");
		temp.put("sourePath", document.bookSource);
		temp.put("isNeedUploaded", "false");
		MZLog.d("wangguodong", "source"+document.bookSource);
		localSignList.add(temp);
		
		for (int i = 0; i < localSignList.size(); i++) {
			// 本地书籍如果没有server_id 需要重新绑定
			if (Integer.valueOf(localSignList.get(i).get("serverId")) == 0) {

				final String documentid = localSignList.get(i)
						.get("documentId");
				WebRequestHelper.post(URLText.synServerId, RequestParamsPool
						.bindYunPanServeridParams(
								localSignList.get(i).get("sign"), localSignList
										.get(i).get("name")), true,
						new MyAsyncHttpResponseHandler(
								UploadActivity.this) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {
								DocBind bind = new DocBind();
								bind.userId = LoginUser.getpin();
								bind.documentId = Integer.valueOf(documentid);
								try {
									JSONObject object = new JSONObject(
											new String(responseBody));
									bind.serverId = object
											.getLong("document_id");
									MZLog.d("wangguodong", "服务器返回的serverid="
											+ bind.serverId);
									MZBookDatabase.instance.UpdateDocBind(bind);
								} catch (Exception e) {
									MZLog.e(activityTag, e.getMessage());
								}

							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								Toast.makeText(UploadActivity.this,
										"书籍绑定失败", Toast.LENGTH_SHORT).show();
							}
						});

			}
		}

		JSONArray jsonArr = new JSONArray();
		try {

			for (Map<String, String> map : localSignList) {
				JSONObject obj = new JSONObject();
				obj.put("sign", map.get("sign"));
				obj.put("id", map.get("serverId"));
				jsonArr.put(obj);
			}

		} catch (Exception e) {
			MZLog.e(activityTag, e.getMessage());
		}

		WebRequestHelper.post(URLText.synCloudDiskBook, RequestParamsPool
				.isNeedUploadYunPanParams(jsonArr.toString()), true,
				new MyAsyncHttpResponseHandler(UploadActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						MZLog.d("wangguodong", "请求失败了");
						tips.setText("请求失败了");
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// 刷新header信息
						try {
							JSONArray array = new JSONArray(new String(
									responseBody));
							int unSyncBookCount = array.length();
							if (unSyncBookCount < 1) {

								tips.setText(R.string.upload_not_find_synbook_msg);
						

							} else {
								tips.setText(UploadActivity.this
										.getResources()
										.getString(
												R.string.upload_find_synbook_msg,
												array.length()));

								// 刷新listview信息
								boolean isNeedUpload = false;
								for (int i = 0; i < array.length(); i++)
									for (int j = 0; j < localSignList.size(); j++) {
										if (array
												.get(i)
												.toString()
												.equals(localSignList.get(j)
														.get("serverId")))

										{
											localSignList.get(j).put(
													"isNeedUploaded", "true");
											isNeedUpload = true;

										}
									}


								for (int position = 0; isNeedUpload
										&& position < localSignList.size(); position++) {

									Map<String, String> map = localSignList
											.get(position);
									if (map.get("isNeedUploaded")
											.equals("true")) {
										map.put("position", position + "");
										MZLog.d(activityTag, "当前位置：" + position
												+ "开始上传...path"+map.get("bookSource"));
										final int index = position;
										Upload.requestUploadFile(
												UploadActivity.this,
												map, new UploadStateListener() {

													@Override
													public void onUploadFinished(
															String type,
															String msg) {
														if(type.equals(Upload.TYPE_MSG)){
														if (msg.equals("ok")) {
															localSignList
																	.get(index)
																	.put("isNeedUploaded",
																			"false");
															
															tips.setText(getResources()
																	.getString(
																			R.string.uploaded));
														

														} else {
															MZLog.d(activityTag,
																	"上传成功 服务器绑定失败");
															
															tips.setText("上传成功 服务器绑定失败");
														}
														}
														else {
															tips.setText(msg+"%");
														}
													}
												});

									}

								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

	}


	@Override
	public void onRightMenuOneClick() {
		super.onRightMenuOneClick();
		tips.setText(R.string.file_uploading);
		if (document != null) {
			if (NetWorkUtils.isNetworkConnected(UploadActivity.this)) {

				requestSynBook();

			} else {
				tips.setText(R.string.network_not_find);
			}

		}
	}
}
