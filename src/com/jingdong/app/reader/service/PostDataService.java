package com.jingdong.app.reader.service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;


import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.data.db.MZBookDatabase;import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.reading.ReadingData;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;

public class PostDataService extends IntentService {
	private static final String TAG = "PostDataServie";
	public static final String PostDataKey = "PostDataKey";
	public static final String PostUrlKey = "PostUrlKey";
	public static final String TYPE = "type";

	public PostDataService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		
		String type = intent.getStringExtra(TYPE);
		
		
		if (type != null && type.equals("multitude"))// 大量数据提交
		{

			MZLog.d("wangguodong","离线阅历开始上传");
			List<ReadingData> list = MZBookDatabase.instance
					.getAllReadingData();

			if(list==null||list.size()==0) 
			{
				MZLog.d("wangguodong","未发现离线阅历数据");
				return;
			}
				
			for (int i = 0; i < list.size(); i++) {
				try {
					JSONArray arr = new JSONArray();
					JSONObject obj = new JSONObject();
					ReadingData data = list.get(i);
					
					if(data.getDocument_id()==-1&&data.getEbook_id()==-1){
						MZLog.d("wangguodong", "当前数据有问题：");
						break;
					}

					MZLog.d("wangguodong", "当前的ebookid为："+data.getEbook_id());
					if (data.getEbook_id() != -1) {
						obj.put("ebook_id", data.getEbook_id());
					}
					MZLog.d("wangguodong", "当前的serverid为："+data.getDocument_id());
					if (data.getDocument_id() != -1) {
						//如果documentid=0（也就是serverid） 请求服务器获取其serverid
						if(data.getDocument_id()==0)
						{
							
							// FIXME
//							try {
//								Map<String, String> paramMap = new HashMap<String, String>();
//								paramMap.put("auth_token", LocalUserSetting.getToken(getApplicationContext()));
//								paramMap.put("sign", data.getSign());
//								Document document=MZBookDatabase.instance.getDocument(data.getSign());
//								paramMap.put("name", URLEncoder.encode(document.title, "utf-8"));
//								String urlText = URLBuilder.addParameter(
//										URLText.synServerId, paramMap);
//								String results = WebRequest.postWebDataWithContext(getApplicationContext(), urlText, "");
//								JSONObject object = new JSONObject(results);
//								MZLog.d("wangguodong", "当前动态获得serverid为："+object.getInt("document_id"));
//								obj.put("document_id", object.getInt("document_id"));
//							} catch (Exception e) {
//								e.printStackTrace();
//								MZLog.d("wangguodong", "当前书籍没有serverid 无法同步阅历数据");
//								break;
//							}
						}
						else {
							obj.put("document_id", data.getDocument_id());
						}
						
					}

					obj.put("start_time", data.getStart_time());
					obj.put("start_chapter", data.getStart_chapter());
					obj.put("start_para_idx", data.getStart_para_idx());
					obj.put("end_time", data.getEnd_time());
					obj.put("end_chapter", data.getEnd_chapter());
					obj.put("end_para_idx", data.getEnd_para_idx());
					obj.put("length", data.getLength());
					arr.put(obj);
					String postText = "data=" + arr.toString();
					String url = intent.getStringExtra(PostUrlKey);
					if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(postText)) {
						String result=WebRequest.postWebDataWithContext(this, url, postText);
						
						JSONObject object=new JSONObject(result);
						
						if(!object.toString().contains("404")&&object.getString("success").equals("true"))
						{
							MZLog.d("wangguodong","服务器返回数据："+object.toString());
							MZBookDatabase.instance.deleteReadingData(data.get_id());
							MZLog.d("wangguodong","_id="+data.get_id()+"离线阅历上传成功");
						}	
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		} else {
			String url = intent.getStringExtra(PostUrlKey);
			String data = intent.getStringExtra(PostDataKey);
			if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(data)) {
				String resultString=WebRequest.postWebDataWithContext(this, url, data);
				
			MZLog.d("wangguodong", "同步进度结果："+resultString);
			}
		}

	}

}
