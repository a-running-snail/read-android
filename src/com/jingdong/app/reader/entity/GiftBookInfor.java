package com.jingdong.app.reader.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

public class GiftBookInfor implements Serializable{
	//赠言
	public String wordsoOfAdvice;
	public static GiftBookInfor BuildGiftBookInfor(String wordsoOfAdvice, String presenter, String giveTime) {
		GiftBookInfor giftBookInfor=new GiftBookInfor();
		giftBookInfor.wordsoOfAdvice = wordsoOfAdvice;
		giftBookInfor.presenter = presenter;
		giftBookInfor.giveTime = giveTime;
		return giftBookInfor;
	}

	//赠送者
	public String presenter;
	//赠送时间
	public String giveTime;
	
	public static GiftBookInfor loadSettingFromFile(byte[] bytes)
	    {
		    ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
	        ObjectInputStream in;
			try {
				in = new ObjectInputStream(bin);
			    Object ret = in.readObject();
		        in.close();
		        return (GiftBookInfor)ret;
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
	    }
	    
	    public byte[] getBytes() {
	    	 try  {
	    	        // save the object to a byte array
	    	        ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    	        ObjectOutputStream out = new ObjectOutputStream(bout);
	    	        out.writeObject(this);
	    	        byte[] bytes = bout.toByteArray();
	    	        out.close();
	    			return bytes;
	    	    }
	    	    catch(Exception e)  {
	    	        return null;
	    	    }
	    }
}
