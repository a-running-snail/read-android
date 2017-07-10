/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.jingdong.app.reader.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import org.json.JSONException;
import org.json.JSONObject;


public  class ReadProgress implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 573395757024448446L;
	    public String title;
	    public  float offSet;
	
	   public static ReadProgress loadSettingFromFile(byte[] bytes)
	    {
//		    ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
//	        ObjectInputStream in;
//			try {
//				in = new ObjectInputStream(bin);
//			    Object ret = in.readObject();
//		        in.close();
//		        return (ReadProgress)ret;
//			} catch (StreamCorruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			return null;
		   return readProgressObjJsonReduce(bytes);
	    }
	    
	    public byte[] getBytes()
	    {
//	    	 try  {
//	    	        // save the object to a byte array
//	    	        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//	    	        ObjectOutputStream out = new ObjectOutputStream(bout);
//	    	        out.writeObject(this);
//	    	        byte[] bytes = bout.toByteArray();
//	    	        out.close();
//	    			return bytes;
//	    	    }
//	    	    catch(Exception e)  {
//	    	        return null;
//	    	    }
	    	 return readProgressObjJsonBuilder().toString().getBytes();
	    }
	    
	    public JSONObject readProgressObjJsonBuilder() {
			JSONObject bootjObject = new JSONObject();
			try {
				bootjObject.put("1", title);
				bootjObject.put("2", offSet);
			} catch (JSONException e) {
			}
			return bootjObject;

		}
	    
		public static ReadProgress readProgressObjJsonReduce(byte[] bytes) {
			ReadProgress readProgress = new ReadProgress();
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(new String(bytes));
				readProgress.title = jsonObject.getString("1");
				readProgress.offSet = (float) jsonObject.getDouble("2");
			} catch (JSONException e) {
			}
			return readProgress;

		}
}
