package com.jingdong.app.reader.extension.giftbag;

import android.graphics.Color;
import android.util.SparseArray;

public  class GiftBagTextAdapter {
	private SparseArray<String> textData;
	private SparseArray<Integer> color;
	private SparseArray<Integer> size;
	/**
	 * 默认字体颜色
	 */
	private static final int DEFALUT_COLOR=Color.YELLOW;
	/**
	 * 默认文字为空串
	 */
	private static final String DEFAUL_STRING="";
	/**
	 * 默认字体大小
	 */
	private static final int DEFALUT_SIZE=40;
	public GiftBagTextAdapter(SparseArray<String> textData,SparseArray<Integer> color,SparseArray<Integer> size) {
		// TODO Auto-generated constructor stub
		this.textData=textData;
		this.color=color;
		this.size=size;
	}
	public int getCount(){
		if(textData==null){
			return 0;
		}
		return textData.size();
	}
	public String getTextData(int index){
		if(index>getCount()-1||index<0){
			return DEFAUL_STRING;
		}
		return textData.get(index, DEFAUL_STRING);
	}
	public int getTextColor(int index){
		if(index>getCount()-1||index<0){
			return DEFALUT_COLOR;
		}
		return color.get(index, DEFALUT_COLOR);
	}
	public int getTextSize(int index){
		if(index>getCount()-1||index<0){
			return DEFALUT_SIZE;
		}
		return size.get(index, DEFALUT_SIZE);
	}
}
