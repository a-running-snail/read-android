package com.jingdong.app.reader.entity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class TryReadCatalogEntity {
	public final static String KEY_ID= "id";
	public final static String KEY_TITLE= "title";
	public final static String KEY_LOCK = "lock";
	public final static String KEY_SUBCHAPTERLIST = "subChapterList";
	public int id;
	public String title;
	public boolean lock; //是否锁定（0：未锁定；1：锁定）
	public   ArrayList<TryReadCatalogEntity> subChapterList = new ArrayList<TryReadCatalogEntity>();

	public BookInforEDetail bookE ;


	public  final static TryReadCatalogEntity parse(JSONObject jsonObject) {
		TryReadCatalogEntity entity = null;
		try{
			if (jsonObject != null) {
				       try {
				    	   entity = new TryReadCatalogEntity();
				    	   entity.title = DataParser.getString(jsonObject, TryReadCatalogEntity.KEY_TITLE);
				    	   entity.id = DataParser.getInt(jsonObject, TryReadCatalogEntity.KEY_ID);
				    	   entity.lock = DataParser.getInt(jsonObject, TryReadCatalogEntity.KEY_LOCK)==0?false:true;
				    	   if(!jsonObject.isNull(TryReadCatalogEntity.KEY_SUBCHAPTERLIST)){
				    	      JSONArray subChapterListObj = jsonObject.getJSONArray(TryReadCatalogEntity.KEY_SUBCHAPTERLIST);
				    	      if(!subChapterListObj.equals("")){
				    	    	  int length = 0;
				  				if (subChapterListObj != null && (length = subChapterListObj.length()) > 0) {
				  					JSONObject jsonObj = null;
				  					TryReadCatalogEntity object;
				  					entity.subChapterList.clear();
				  					for (int index = 0; index < length; index++) {
				  						jsonObj = subChapterListObj.getJSONObject(index);
				  						if (jsonObj != null) {
				  							object = parseObj(jsonObj);
				  							if (object != null) {
				  								entity.subChapterList.add(object);
				  							}
				  						}
				  					}
				  				}else{
				  					entity.subChapterList.clear();
				  				}
				    	      }
				    	   }else{
				    		   entity.subChapterList.clear();
				    	   }
						} catch (Exception e) {
							e.printStackTrace();
						}
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return entity;
         }

	private  final static TryReadCatalogEntity parseObj(JSONObject jsonObject){
		TryReadCatalogEntity entity = null;
		if (jsonObject != null) {
			       try {
			    	   entity = new TryReadCatalogEntity();
			    	   entity.title = DataParser.getString(jsonObject, TryReadCatalogEntity.KEY_TITLE);
			    	   entity.id = DataParser.getInt(jsonObject, TryReadCatalogEntity.KEY_ID);
			    	   entity.lock = DataParser.getInt(jsonObject, TryReadCatalogEntity.KEY_LOCK)==0?false:true;
					} catch (Exception e) {
						e.printStackTrace();
					}
        }
		return entity;
	}

	}