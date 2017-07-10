package com.jingdong.app.reader.util.tts;

import java.util.ArrayList;
import java.util.List;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.epub.paging.Element;
import com.jingdong.app.reader.epub.paging.ElementImage;
import com.jingdong.app.reader.epub.paging.ElementText;
import com.jingdong.app.reader.epub.paging.Kit42View;

import android.content.SharedPreferences;
import android.nfc.cardemulation.OffHostApduService;
import android.os.Bundle;
import android.os.Environment;
import android.widget.RadioGroup;
import android.widget.Toast;

public class TTSManager  {

	private static TTSManager instance = null;
	
	// 语音合成对象
	private SpeechSynthesizer mTts;
	
	// 默认发音人
	private String voicer = "xiaoyan";
	
	private String[] mCloudVoicersEntries;
	private String[] mCloudVoicersValue ;
	
	// 缓冲进度
	private int mPercentForBuffering = 0;
	// 播放进度
	private int mPercentForPlaying = 0;
	
	// 云端/本地单选按钮
	private RadioGroup mRadioGroup;
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// 语记安装助手类
    ApkInstaller mInstaller ;
	
	private Toast mToast;
	private SharedPreferences mSharedPreferences;
	
	String[] paraTextArr,nextParaTextArr;
	int currentSentenceIndex = 0;
	private SpeechActionListener speechActionListener;
	//翻页百分比
	private int switchPercent = 0 ;
	List<SpeechSentenceEntity>  speechSentenceList ;
	private int elementCount = 0;
	private Kit42View currentView;
	private SpeechSentenceEntity lastSentence = null;

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	
	public static TTSManager getInstance(){
		if(instance==null){
			instance = new TTSManager();
		}
		return instance;
	}
	
	public void init(){
		// 初始化合成对象
		if(mTts == null ){
			mTts = SpeechSynthesizer.createSynthesizer(MZBookApplication.getContext(), mTtsInitListener);
			// 设置参数
			setParam();
		}
		mInstaller = new  ApkInstaller(MZBookApplication.getInstance().getCurrentMyActivity());
		mToast = Toast.makeText(MZBookApplication.getContext(),"",Toast.LENGTH_SHORT);
	}
	
	/**
	 * 初始化第一页语音朗读
	 */
	public void initFirstPageSpeech(List<Element> elementlist,Double nextPageTextLenth,Kit42View view){
		currentView=view;
		if(elementlist!= null && elementlist.size()>0){
			speechSentenceList = initSpeechSentence(elementlist);
			if(speechSentenceList != null && speechSentenceList.size()!=0){
				currentSentenceIndex = 0;
				startSpeechSentence(lastSentence,speechSentenceList.get(currentSentenceIndex));
//				ttsPlay(speechSentenceList.get(currentSentenceIndex).content);
//				currentView.setSelection(speechSentenceList.get(currentSentenceIndex).elementlist);
				switchPercent=100-(int)((nextPageTextLenth/elementCount)*100);
			}
		}
	}
	
	public class SpeechSentenceEntity{
		List<Element> elementlist;
		String content;
	}
	
	private List<SpeechSentenceEntity> initSpeechSentence(List<Element> elementlist){
		List<SpeechSentenceEntity> list = new ArrayList<SpeechSentenceEntity>();
		Element current;
		SpeechSentenceEntity entity = new SpeechSentenceEntity();
		Boolean newSentence = true;
		StringBuffer contentBuff = new StringBuffer();
		List<Element> elist = new ArrayList<Element>();
		for (int i = 0; i < elementlist.size(); i++) {
			current = elementlist.get(i);
			if(newSentence){
				entity = new SpeechSentenceEntity();
				elist = new ArrayList<Element>();
				contentBuff = new StringBuffer();
				newSentence = false;
				elementCount = 0;
			}
			if(current instanceof ElementText){
				contentBuff.append(current.getContent());
				elist.add(current);
				elementCount++;
				if(TTSUtil.isEndSentence(current.getContent())){
					newSentence = true;
					entity.elementlist=elist;
					entity.content=contentBuff.toString();
					list.add(entity);
					contentBuff = null;
				}
			}
		}
		return list;
	}
	
	private void startSpeechSentence(SpeechSentenceEntity preEntity,SpeechSentenceEntity entity){
		ttsPlay(entity.content);
		
		if(preEntity!=null)
			currentView.setSelection(preEntity.elementlist,entity.elementlist);
		else
			currentView.setSelection(null,entity.elementlist);
	}
	
//	/**
//	 * 初始化第一页语音朗读
//	 */
//	public void initFirstPageSpeech(String speechText,Double nextPageTextLenth){
//		if(speechText !=null && !speechText.equals("")){
//			paraTextArr =TTSUtil.splitTextToSentence(speechText);
////			if(speechActionListener!=null){
////				speechActionListener.loadNextPageText();
////			}
//			if(paraTextArr.length!=0){
//				ttsPlay(paraTextArr[0]);
//				currentSentenceIndex = 0;
//				int count = paraTextArr[paraTextArr.length-1].length();
//				switchPercent=100-(int)((nextPageTextLenth/count)*100);
//			}
//		}
//	}

	public void ttsPlay(String text){
		int code = mTts.startSpeaking(text, mTtsListener);
		if (code != ErrorCode.SUCCESS) {
			if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
				//未安装则跳转到提示安装页面
				mInstaller.install();
			}else {
				showTip("语音合成失败,错误码: " + code);	
			}
		}
	}
	
	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		
		@Override
		public void onSpeakBegin() {
			showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// 合成进度
			mPercentForBuffering = percent;
//			showTip(String.format(getString(R.string.tts_toast_format),
//					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
			mPercentForPlaying = percent;
			if(speechActionListener!=null && currentSentenceIndex == (speechSentenceList.size()-1)){
				showTip(mPercentForPlaying+"");
				if(mPercentForPlaying>=switchPercent){
					switchPercent = 999 ;
					speechActionListener.switchNextPage();
				}
			}
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				if(currentSentenceIndex != (speechSentenceList.size()-1)){
					while(speechSentenceList.get(++currentSentenceIndex).content.trim().equals("")){//去除空字符的影响
					}
					startSpeechSentence(speechSentenceList.get(currentSentenceIndex-1),speechSentenceList.get(currentSentenceIndex));
				}else{
					lastSentence= speechSentenceList.get(currentSentenceIndex);
					if(switchPercent != 999)
						speechActionListener.switchAndSpeechNextPage();
					else{
						speechActionListener.speechNextPage();
					}
				}
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};
	
	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	private void setParam(){
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// 设置在线合成发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
		}else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			// 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
			mTts.setParameter(SpeechConstant.VOICE_NAME, "");
		}
		//设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, "200");
//		mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
		//设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, "50");
//		mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
		//设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, "50");
//		mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
//		mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
	}
	
	
	/**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
//			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
//        		showTip("初始化失败,错误码："+code);
        	} else {
				// 初始化成功，之后可以调用startSpeaking方法
        		// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
        		// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}		
		}
	};
	
	public SpeechActionListener getSpeechActionListener() {
		return speechActionListener;
	}

	public void setSpeechActionListener(SpeechActionListener speechActionListener) {
		this.speechActionListener = speechActionListener;
	}

	/**
	 * 与引擎的接口
	 * @author tanmojie
	 *
	 */
	public interface SpeechActionListener {
		public void switchAndSpeechNextPage();
		public void switchNextPage();
		public void speechNextPage();
	}
	
}
