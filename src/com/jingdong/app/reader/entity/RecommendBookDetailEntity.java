package com.jingdong.app.reader.entity;
import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;
public class RecommendBookDetailEntity extends Entity{
	public final static String KEY_ID= "id";
	public final static String KEY_PICURL= "picUrl";
	public final static String KEY_JDPRICE = "jdPrice";
	public final static String KEY_BOOKNAME = "bookName";
	public int id;
	public String picUrl;
	public double jdPrice;
	public String bookName;


	public static final RecommendBookDetailEntity parse(JSONObject jsonObject) {
		RecommendBookDetailEntity recommend = null;
		if (jsonObject != null) {
			       try {
			    	   recommend = new RecommendBookDetailEntity();
			    	   recommend.id = DataParser.getInt(jsonObject, RecommendBookDetailEntity.KEY_ID);
			    	   recommend.picUrl = DataParser.getString(jsonObject, RecommendBookDetailEntity.KEY_PICURL);
			    	   recommend.jdPrice = DataParser.getDouble(jsonObject, RecommendBookDetailEntity.KEY_JDPRICE);
			    	   recommend.bookName = DataParser.getString(jsonObject, RecommendBookDetailEntity.KEY_BOOKNAME);
					} catch (Exception e) {
						e.printStackTrace();
					}
        }
		return recommend;
         }

	@Override
	public String getImageUrl() {
		// TODO Auto-generated method stub
		return picUrl;
	}

	@Override
	public String getHomeImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	}