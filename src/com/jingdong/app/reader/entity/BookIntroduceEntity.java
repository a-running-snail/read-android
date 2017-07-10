package com.jingdong.app.reader.entity;
import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;
public class BookIntroduceEntity{
	public final static String KEY_ZWYZ= "zwyz";
	public final static String KEY_CBTIME= "cbtime";
	public final static String KEY_CBS = "cbs";
	public final static String KEY_YC = "yc";
	public final static String KEY_PAGES = "pages";
	public final static String KEY_JDPRICE = "jdPrice";
	public final static String KEY_YSTIME = "ystime";
	public final static String KEY_ZHIZ = "zhiz";
	public final static String KEY_AUTHOR = "author";
	public final static String KEY_CATEGORY = "category";
	public final static String KEY_PRICE = "price";
	public final static String KEY_ISBN = "isbn";
	public final static String KEY_KB = "kb";
	public final static String KEY_BC = "bc";
	public final static String KEY_ZZHEN = "zzhen";

	public String zwyz;
	public String cbtime;
	public String cbs;
	public int yc;
	public int pages;
	public double jdPrice;
	public String ystime;
	public String zhiz;
	public String author;
	public String category;
	public double price;
	public String isbn;
	public int kb;
	public int bc;
	public String zzhen;

	public final static String KEY_ZWYZ_Z= "正文语种";
	public final static String KEY_CBTIME_Z= "出版时间";
	public final static String KEY_CBS_Z = "出 版 社";
	public final static String KEY_YC_Z = "印     次";
	public final static String KEY_PAGES_Z = "页     数";
	public final static String KEY_JDPRICE_Z = "京 东 价";
	public final static String KEY_YSTIME_Z = "印刷时间";
	public final static String KEY_ZHIZ_Z = "纸     张";
	public final static String KEY_AUTHOR_Z = "作    者";
	public final static String KEY_CATEGORY_Z = "所属分类";
	public final static String KEY_PRICE_Z = "市 场 价";
	public final static String KEY_ISBN_Z = "I S B N";
	public final static String KEY_KB_Z = "开   本";
	public final static String KEY_BC_Z = "版   次";
	public final static String KEY_ZZHEN_Z = "装   帧";

	public static ArrayList<key2Value>  key2valueList = new ArrayList<key2Value>();
	public static Hashtable<String,String> hs = new Hashtable<String,String>();
	static {
		hs.clear();
		hs.put(KEY_ZWYZ, KEY_ZWYZ_Z);
		hs.put(KEY_CBTIME, KEY_CBTIME_Z);
		hs.put(KEY_CBS, KEY_CBS_Z);
		hs.put(KEY_YC, KEY_YC_Z);
		hs.put(KEY_PAGES, KEY_PAGES_Z);
		hs.put(KEY_JDPRICE, KEY_JDPRICE_Z);
		hs.put(KEY_YSTIME, KEY_YSTIME_Z);
		hs.put(KEY_ZHIZ, KEY_ZHIZ_Z);
		hs.put(KEY_AUTHOR, KEY_AUTHOR_Z);
		hs.put(KEY_CATEGORY, KEY_CATEGORY_Z);
		hs.put(KEY_PRICE, KEY_PRICE_Z);
		hs.put(KEY_ISBN, KEY_ISBN_Z);
		hs.put(KEY_KB, KEY_KB_Z);
		hs.put(KEY_BC, KEY_BC_Z);
		hs.put(KEY_ZZHEN, KEY_ZZHEN_Z);

	}
	public static class key2Value{
    	public String key;
    	public String value;
    }


	public static BookIntroduceEntity parse(JSONObject jsonObject) {
		BookIntroduceEntity bE = null;
		if (jsonObject != null) {
			       try {
			    	   key2valueList.clear();
			    	   bE = new BookIntroduceEntity();
			    	   getString(jsonObject, BookIntroduceEntity.KEY_ZWYZ);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_CBTIME);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_CBS);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_YC);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_PAGES);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_JDPRICE);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_YSTIME);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_ZHIZ);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_AUTHOR);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_CATEGORY);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_PRICE);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_ISBN);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_KB);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_BC);
			    	   getString(jsonObject, BookIntroduceEntity.KEY_ZZHEN);

					} catch (Exception e) {
						e.printStackTrace();
					}
        }
		return bE;
    }
	public static  void getString(JSONObject jsonObject,String key){
		String value = DataParser.getString(jsonObject,key);
           if(value!=""){
        	   key2Value k = new key2Value();
        	   k.key = hs.get(key);
        	   k.value = value.equals("null")?"":value;
        	   key2valueList.add(k);
           }
	}
	public static void getDouble(JSONObject jsonObject,String key){
		double value = DataParser.getDouble(jsonObject,key);
           if(value!=-1){
        	   key2Value k = new key2Value();
        	   k.key = hs.get(key);
        	   k.value = String.valueOf(value);
        	   key2valueList.add(k);
           }
	}
	public static void getInt(JSONObject jsonObject,String key){
		int value = DataParser.getInt(jsonObject,key);
           if(value!=-1){
        	   key2Value k = new key2Value();
        	   k.key = hs.get(key);
        	   k.value = String.valueOf(value);
        	   key2valueList.add(k);
           }
	}

	}