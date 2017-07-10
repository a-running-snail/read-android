package com.jingdong.app.reader.entity;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jingdong.app.reader.d.m;
import com.jingdong.app.reader.util.MZLog;

public class BootEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8165192914776534063L;

	private String bookid;

	private String orderId;

	public String certjosh;

	public String contentjosh;

	public String coverURl;

	/************
	 * 因为混淆序列话可能导致classnotfound的严重后果 ，所以现在现在修改boot的存储方式
	 * 原来直接是序列化的，。如果keep为BootEntity，可能存在一些安全问题，现在是用json存取
	 * 
	 * @author yfxiawei
	 * @since 2013年1月30日9:47:58
	 * 
	 * 
	 * ****/
	public JSONObject bootEntityObjJsonBuilder() {
		JSONObject bootjObject = new JSONObject();
		try {
			bootjObject.put("1", bookid);
			bootjObject.put("2", orderId);
			bootjObject.put("3", certjosh);
			bootjObject.put("4", contentjosh);
			bootjObject.put("5", coverURl);
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			MZLog.d("wangguodong",e.getLocalizedMessage());
		}
		return bootjObject;

	}

	/************
	 * 因为混淆序列话可能导致classnotfound的严重后果 ，所以现在现在修改boot的存储方式
	 * 原来直接是序列化的，。如果keep为BootEntity，可能存在一些安全问题，现在是用json存取
	 * 如果数据非json格式则说明是原始版本数据，需要做兼容处理
	 * 
	 * @author yfxiawei
	 * @since 2013年1月30日9:47:58
	 * 
	 * 
	 * ****/
	public static BootEntity bootEntityObjJsonReduce(byte[] bytes) {
		BootEntity bootEntity = new BootEntity();
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(new String(bytes));

			bootEntity.bookid = jsonObject.getString("1");
			bootEntity.orderId = jsonObject.getString("2");
			bootEntity.certjosh = jsonObject.getString("3");
			bootEntity.contentjosh = jsonObject.getString("4");
			bootEntity.coverURl = jsonObject.getString("5");
		} catch (JSONException e) {

			ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(bin);
				Object ret = in.readObject();
				in.close();
				if (ret instanceof m) {
					m bootTemp = (m) ret;

					bootEntity.bookid = bootTemp.d;// deabc
					bootEntity.orderId = bootTemp.e;
					bootEntity.certjosh = bootTemp.a;
					bootEntity.contentjosh = bootTemp.b;
					bootEntity.coverURl = bootEntity.coverURl;
					return bootEntity;
				}
				return null;

			} catch (StreamCorruptedException e1) {

			} catch (IOException e1) {

			} catch (ClassNotFoundException e1) {
				MZLog.d("wangguodong",e1.getLocalizedMessage());
			}

		}
		return bootEntity;

	}

	public JSONObject getCertJosh() {
		try {
			return new JSONObject(certjosh);
		} catch (JSONException e) {
			MZLog.d("wangguodong",e.getLocalizedMessage());
		}
		return null;
	}

	public JSONObject getContentJosh() {
		try {
			return new JSONObject(contentjosh);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static BootEntity loadSettingFromFile(byte[] bytes) {

		return bootEntityObjJsonReduce(bytes);

	}

	public byte[] getBytes() {

		// ByteArrayOutputStream bout = new ByteArrayOutputStream();
		// ObjectOutputStream out = new ObjectOutputStream(bout);
		// out.writeObject(this);
		// bootEntityObjJsonBuilder().toString().getBytes();
		// byte[] bytes = bout.toByteArray();
		// out.close();
		// return bytes;
		return bootEntityObjJsonBuilder().toString().getBytes();

	}

	public static BootEntity parser(Element root) {
		// ebookids
		// File file = httpResponse.getSaveFile();
		BootEntity boot = new BootEntity();
		try {
			NodeList Assettems = root.getElementsByTagName("Asset");
			if (Assettems.getLength() > 0) {
				Element node = (Element) Assettems.item(0);
				NodeList ebookids = node.getElementsByTagName("EbookId");
				if (ebookids.getLength() > 0) {
					Element node0 = (Element) ebookids.item(0);
					boot.bookid = node0.getFirstChild().getNodeValue();
				} else {
					return null;
				}
				NodeList orderIds = node.getElementsByTagName("OrderId");
				if (orderIds.getLength() > 0) {
					Element node0 = (Element) orderIds.item(0);
					boot.orderId = node0.getFirstChild().getNodeValue();
				} else {
					return null;
				}
				NodeList certURIs = node.getElementsByTagName("CertURI");
				if (certURIs.getLength() > 0) {
					Element node0 = (Element) certURIs.item(0);
					boot.certjosh = node0.getFirstChild().getNodeValue();
				} else {
					return null;
				}
				NodeList contentURIs = node.getElementsByTagName("ContentURI");
				if (contentURIs.getLength() > 0) {
					Element node0 = (Element) contentURIs.item(0);
					boot.contentjosh = node0.getFirstChild().getNodeValue();
				} else {
					return null;
				}
				try {
					NodeList coverURs = node.getElementsByTagName("CoverURl");
					if (coverURs.getLength() > 0) {
						Element node0 = (Element) coverURs.item(0);
						boot.coverURl = node0.getFirstChild().getNodeValue();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return boot;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getBookid() {
		return bookid;
	}

	public void setBookid(String bookid) {
		this.bookid = bookid;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

}
