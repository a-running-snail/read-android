package com.jingdong.app.reader.entity1.bookinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.book.BookEntity;




public class Ebook implements Serializable{
	String id;
	String app_store_product_id;
	String price;
	String web_price;
	String origin_price;
	String description;
	String entity_type;
	String sales_tag;
	boolean is_web_only;
	String third_party_merchant_id;

	boolean is_composite =false;
	String composite_tag_id;

	List<Entities> entities = new ArrayList<Entities>();
	
	
	public void fromJson(JSONObject object){
		if(object!=null)
		{
			
			setId(object.optString("id"));
			setApp_store_product_id(object.optString("app_store_product_id"));
			setPrice(object.optString("price"));
			setWeb_price(object.optString("web_price"));
			setOrigin_price(object.optString("origin_price"));
			setDescription(object.optString("description"));
			setEntity_type(object.optString("entity_type"));
			setSales_tag(object.optString("sales_tag"));
			setIs_web_only(object.optBoolean("is_web_only"));
			setThird_party_merchant_id(object.optString("third_party_merchant_id"));
			setIs_composite(object.optBoolean("is_composite"));
			setComposite_tag_id(object.optString("composite_tag_id"));
			List<Entities> temp = new ArrayList<Entities>();
			try {
				JSONArray array=object.getJSONArray("entities");
				for(int i=0;i<array.length();i++)
				{
					Entities entities=new Entities();
					entities.fromJson(array.getJSONObject(i));
					temp.add(entities);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			setEntities(temp);

		}
	}

	public int getEntityIdWithEdition(int edition) {
		for (Entities entity : entities) {
			if (Integer.parseInt(entity.getEdition()) == edition) {
				return Integer.parseInt(entity.getId());
			}
		}
		return 0;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApp_store_product_id() {
		return app_store_product_id;
	}

	public void setApp_store_product_id(String app_store_product_id) {
		this.app_store_product_id = app_store_product_id;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getWeb_price() {
		return web_price;
	}

	public void setWeb_price(String web_price) {
		this.web_price = web_price;
	}

	public String getOrigin_price() {
		return origin_price;
	}

	public void setOrigin_price(String origin_price) {
		this.origin_price = origin_price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEntity_type() {
		return entity_type;
	}

	public void setEntity_type(String entity_type) {
		this.entity_type = entity_type;
	}

	public String getSales_tag() {
		return sales_tag;
	}

	public void setSales_tag(String sales_tag) {
		this.sales_tag = sales_tag;
	}

	public boolean isIs_web_only() {
		return is_web_only;
	}

	public void setIs_web_only(boolean is_web_only) {
		this.is_web_only = is_web_only;
	}

	public String getThird_party_merchant_id() {
		return third_party_merchant_id;
	}

	public void setThird_party_merchant_id(String third_party_merchant_id) {
		this.third_party_merchant_id = third_party_merchant_id;
	}

	public boolean isIs_composite() {
		return is_composite;
	}

	public void setIs_composite(boolean is_composite) {
		this.is_composite = is_composite;
	}

	public String getComposite_tag_id() {
		return composite_tag_id;
	}

	public void setComposite_tag_id(String composite_tag_id) {
		this.composite_tag_id = composite_tag_id;
	}

	public List<Entities> getEntities() {
		return entities;
	}

	public void setEntities(List<Entities> entities) {
		this.entities = entities;
	}

}
