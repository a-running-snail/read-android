package com.jingdong.app.reader.util.tts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jingdong.app.reader.R;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;


public class TTSUtil {

	static String sentenceSplit = "[。？：！.?:!/n/r…]";
	/**
	 * 判断一段文字是否为完整句子
	 * @param text
	 * @return
	 */
	public static Boolean isEndSentence(String text){
		Pattern pattern = Pattern.compile(sentenceSplit);
		Matcher matcher=pattern.matcher(text); 
		return matcher.find();
	}
	/**
	 * 将一段文字切分成句子数组返回
	 * @param text
	 * @return
	 */
	public static String[] splitTextToSentence(String text){
		Pattern pattern = Pattern.compile(sentenceSplit);
		return pattern.split(text);
	}
	
	/**
	 * 将开头不完整的句子截掉
	 * @param text
	 * @return
	 */
	public static String cutUncompletSentence(String text){
		Pattern pattern = Pattern.compile(sentenceSplit);
		Matcher matcher=pattern.matcher(text); 
		int end=matcher.end();
		return text.substring(end);
	}
}
