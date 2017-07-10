
package com.jingdong.app.reader.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


public class StreamToolBox {


    
    public static byte[] getByteArray(InputStream in) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bout = new BufferedOutputStream(baos);
        byte[] buffer = new byte[4096];
        while (true) {
            int doneLength = bin.read(buffer);
            if (doneLength == -1)
                break;
            bout.write(buffer, 0, doneLength);
        }
        bout.flush();
        return baos.toByteArray();
    }
    
    public static ByteArrayInputStream getByteArrayInputStream(byte[] bytes) throws IOException {
        return  new ByteArrayInputStream(bytes);

    }
    
    public static synchronized InputStream loadStreamFromFile(String filePathName)
            throws FileNotFoundException, IOException {
        return new FileInputStream(filePathName);
    }

    public static synchronized void saveStreamToFile(byte[] byteArray, File file) throws IOException {
        try {
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.close();
        } catch (Exception e) {
            // Log.i("StreamToolBox", e.toString());
        }
    }
    
     public static synchronized void saveStreamToFile(byte[] byteArray, String path) throws IOException {
    	saveStreamToFile(byteArray,new File(path));    
    }
    
    
    public static String loadStringFromStream(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(16384);
        copyStream(in, baos);
        baos.close();
        return baos.toString("UTF-8");
    }

    public static void saveStringToFile(String str, String fileNamePath) throws IOException {
        saveStreamToFile(new ByteArrayInputStream(str.getBytes("UTF-8")), fileNamePath);
    }

    public static synchronized boolean saveStreamToFile(InputStream in, String fileNamePath){
        boolean isSaveOk = false;
    	try {
            File f = new File(fileNamePath);
            if (f.exists()) {
                f.delete();
            } else {
                f.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(f);
            copyStream(in, fos);
            fos.close();
            isSaveOk = true;
        } catch (Exception e) {
            // Log.i("StreamToolBox", e.toString());
        }finally{	
        	try{
            	in.close();
        	}catch (Exception e) {
                // Log.i("StreamToolBox", e.toString());
            }
        }
		return isSaveOk;
    }

    public static synchronized void saveStreamToFile(InputStream in, File file) throws IOException {
        try {
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            copyStream(in, fos);
            fos.close();
        } catch (Exception e) {
            // Log.i("StreamToolBox", e.toString());
        }
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        BufferedOutputStream bout = new BufferedOutputStream(out);

        byte[] buffer = new byte[4096];

        while (true) {
            int doneLength = bin.read(buffer);
            if (doneLength == -1)
                break;
            bout.write(buffer, 0, doneLength);
        }
        bout.flush();
    }

    public static ByteArrayInputStream flushInputStream(InputStream in) throws IOException {
        try{
        	BufferedInputStream bin = new BufferedInputStream(in);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bout = new BufferedOutputStream(baos);
            byte[] buffer = new byte[4096];
            while (true) {
                int doneLength = bin.read(buffer);
                if (doneLength == -1)
                    break;
                bout.write(buffer, 0, doneLength);
            }
            bout.flush();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return bais;
        }catch(OutOfMemoryError error){
        	System.gc();
        }
    	return null;
    }

    
    public static byte[] getBytes(Object obj)
    {
    	 try  {
    	        // save the object to a byte array
    		 if(obj!=null){
    	        ByteArrayOutputStream bout = new ByteArrayOutputStream();
    	        ObjectOutputStream out = new ObjectOutputStream(bout);
    	        out.writeObject(obj);
    	        byte[] bytes = bout.toByteArray();
    	        out.close();
    			return bytes;
    			}
    	    }
    	    catch(Exception e)  {
    	        return null;
    	    }
    	     return null;
    }
    
	   public static Object getObject(byte[] bytes)
	    {
		   if(bytes==null){
			   return null; 
		   }
		    ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
	        ObjectInputStream in;
			try {
				in = new ObjectInputStream(bin);
			    Object obj = in.readObject();
		        in.close();
		        return  obj;
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
    
    
}
