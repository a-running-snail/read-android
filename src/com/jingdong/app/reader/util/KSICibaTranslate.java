package com.jingdong.app.reader.util;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.kingsoft.iciba.sdk2.KSCibaEngine;
import com.kingsoft.iciba.sdk2.interfaces.IKSCibaQueryResult;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;

public class KSICibaTranslate  {

	private static KSICibaTranslate instance = null;
	private static KSCibaEngine mKsCibaEngine = null;
	// DemoID 82334488L 
	//正式厂商ID由词霸提供,谨记
	static long mCompanyID = 65892187L;
	// 用于设置查词类型 
	//	0：英汉、汉英     
	//	1：汉汉      
	//	2：英汉、汉英 + 汉汉
	private static int mSearchType = 2;
	
	private static String seachKeyWords;
	
	static String dictPath=MZBookApplication.getInstance().getCachePath()+File.separator+"dict";
	
	public static KSICibaTranslate getInstance(){
		if(instance==null){
			instance = new KSICibaTranslate();
			mKsCibaEngine = new KSCibaEngine(MZBookApplication.getContext());
			// 该接口不添加离线词典的支持
//			mKsCibaEngine.installEngine(mCompanyID);
			// 该接口支持离线词典，需要用户指定离线词典的路径，同时词典名称务必保持原样，不要修改，路径可以自行指定
			dictPath=MZBookApplication.getInstance().getCachePath()+File.separator+"dict";
//			dictPath="sdcard/iciba/dict";

			mKsCibaEngine.installEngine(dictPath,mCompanyID);
		}
		return instance;
	}
	
	public static void refreshEngine(){
		mKsCibaEngine.installEngine(dictPath,mCompanyID);
	}
	
	public static void getTranslateResult(String keywords){
		if(keywords!= null && ! keywords.equals("")){
			seachKeyWords= keywords;
			mKsCibaEngine.startSearchWord(keywords.trim(), mSearchType, iksCibaQueryResult);
		}
	}
	
	
	/**
	 * 查词回调，显示查词结果
	 */
	private static IKSCibaQueryResult iksCibaQueryResult = new IKSCibaQueryResult() {
		JSONObject result,part,symbol,message,baseInfo,ccMean,spellObject;
		JSONArray symbols,parts,means,suggests,spells;
		String status,resultStr,partstr,translate_type,translate_result,translate_msg,word_symbol,spell;
		StringBuffer strBuffer;
		@Override
		public void searchResult(final String arg0) {
			if(arg0 != null) {
				try {
					strBuffer=new StringBuffer();
					result=new JSONObject(arg0);
					status = result.optString("status");
					if(status.equals("1")){//查词解释
						message = result.optJSONObject("message");
						baseInfo = message.optJSONObject("baseInfo");
						//汉英、英汉翻译
						if(baseInfo!=null && !baseInfo.equals("")){
							translate_type = baseInfo.optString("translate_type");
							if(translate_type.equals("1")){
								symbols = baseInfo.getJSONArray("symbols");
								for(int n=0 ; n<symbols.length(); n++){
									symbol = symbols.getJSONObject(n);
									word_symbol =symbol.optString("word_symbol");
									if(strBuffer.length()!=0)
										strBuffer.append("\n\n");
									strBuffer.append("["+word_symbol+"]");
									parts = symbol.getJSONArray("parts");
									for(int i=0; i <parts.length(); i++){
										part = parts.getJSONObject(i);
										if(strBuffer.length()!=0)
											strBuffer.append("\n");
										partstr = part.optString("part");
										if(!partstr.equals(""))
											strBuffer.append(partstr+"\n");
										means=part.getJSONArray("means");
										for(int k= 0;k<means.length();k++){
											strBuffer.append(means.optString(k)+"; ");
										}
									}
								}
							}else if(translate_type.equals("2")){//翻译
								translate_result=baseInfo.optString("translate_result");
								if(!translate_result.equals("")){
									strBuffer.append(translate_result);
									translate_msg = baseInfo.optString("translate_msg");
									if(!translate_msg.equals(""))
										strBuffer.append(translate_msg);
								}
							}else if(translate_type.equals("3")){//查词建议
								suggests=baseInfo.getJSONArray("suggest");
								for(int i=0;i<suggests.length();i++){
									strBuffer.append(suggests.getJSONObject(i).optString("key"));
								}
							}
						}
						//汉汉翻译
						ccMean = message.optJSONObject("cc_mean");
						if(ccMean!=null && !ccMean.equals("")){
							spells=ccMean.optJSONArray("spells");
							if(spells!=null && spells.length()>0){
								for(int n=0 ; n<spells.length(); n++){
									spellObject = spells.getJSONObject(n);
									spell =spellObject.optString("spell");
									if(strBuffer.length()!=0)
										strBuffer.append("\n\n");
									strBuffer.append("["+spell+"]");
									means = spellObject.optJSONArray("means");
									if (means != null) {
										for(int i=0; i <means.length(); i++){
											if(strBuffer.length()!=0)
												strBuffer.append("\n");
											strBuffer.append(means.optString(i));
										}
									}
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(strBuffer.length()==0){
					strBuffer.append(MZBookApplication.getContext().getResources().getString(
                            R.string.tranlate_no_dictionary_results));
				}
				resultStr=strBuffer.toString();
				strBuffer.setLength(0);
				
				Intent it=new Intent(TranslateTask.TRANSLATION_QUERY_RESULT_OK);
                it.putExtra(TranslateTask.TRANSLATION_QUERY_RESULT, resultStr);
                it.putExtra(TranslateTask.TRANSLATION_QUERY_WORD, seachKeyWords);
                LocalBroadcastManager.getInstance(MZBookApplication.getContext()).sendBroadcast(it);
			}
			
		}
	};
}
