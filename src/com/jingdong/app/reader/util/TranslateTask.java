package com.jingdong.app.reader.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.R;

public class TranslateTask  {

	public static final int CODE_NORMAL = 0;
	public static final int CODE_QUERY_STRING_TOO_LONG = 20;
	public static final int CODE_UNABLE_TO_TRANSLATE = 30;
	public static final int CODE_UNSUPPORTED_LANGUAGE = 40;
	public static final int CODE_INVALID_KEY = 50;
	public static final int CODE_NO_DICTIONARY_RESULTS = 60;
	
	public static final String TRANSLATION_QUERY_RESULT_OK = "query_result_ok";
	public static final String TRANSLATION_QUERY_RESULT = "query_result";
	public static final String TRANSLATION_QUERY_WORD = "query_word";
	
	/**
	 * 原有翻译接口（已废弃）
	 * @param context
	 * @param keywords
	 */
	public static void requestTranslate(final Context context, String keywords){
	    try {
            keywords=URLEncoder.encode(keywords, WebRequestHelper.CHAR_SET);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        
        WebRequestHelper.get(URLText.getTranslatedWords.replace(":keyword", keywords), new MyAsyncHttpResponseHandler(context) {
            
            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onResponse( int statusCode,
                    Header[] headers, byte[] responseBody) {
                
                String explainStr = "";
                try {
                    JSONObject object = new JSONObject(new String(responseBody));
                    int errorcode = object.optInt("errorCode");
                    switch (errorcode) {
                    case CODE_NORMAL:
                        try {
                            JSONObject basic = object.optJSONObject("basic");
                            JSONArray explains = basic.optJSONArray("explains");
                            for (int i = 0; i < explains.length(); i++) {
                                explainStr += explains.getString(i) + ";";
                            }

                        } catch (Exception e) {
                            
                            //MZLog.d("wangguodong", "未发现基础释义");
                            
                            JSONArray explains = object.optJSONArray("translation");
                            for (int i = 0; i < explains.length(); i++) {
                                explainStr += explains.getString(i) + ";";
                            }
                        }
                        break;
                    case CODE_QUERY_STRING_TOO_LONG:
                        explainStr = context.getResources().getString(
                                R.string.tranlate_text_too_long);
                        break;
                    case CODE_UNABLE_TO_TRANSLATE:
                        explainStr = context.getResources().getString(
                                R.string.tranlate_unable_to_translate);
                        break;
                    case CODE_UNSUPPORTED_LANGUAGE:
                        explainStr = context.getResources().getString(
                                R.string.tranlate_unsupported_language);
                        break;
                    case CODE_INVALID_KEY:
                        explainStr = context.getResources().getString(
                                R.string.tranlate_invalid_key);
                        break;
                    case CODE_NO_DICTIONARY_RESULTS:
                        explainStr = context.getResources().getString(
                                R.string.tranlate_no_dictionary_results);
                        break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    explainStr = context.getResources().getString(
                            R.string.tranlate_some_probleam);
                }
                
                MZLog.d("wangguodong", explainStr);
                Intent it=new Intent(TRANSLATION_QUERY_RESULT_OK);
                it.putExtra(TRANSLATION_QUERY_RESULT, explainStr);
                LocalBroadcastManager.getInstance(context).sendBroadcast(it);
                
            }
        });
	}
	
	
}
