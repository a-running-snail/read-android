package com.jingdong.app.reader.epub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.jingdong.app.reader.epub.paging.DecryptHelper;
import com.jingdong.app.reader.io.IOUtil;

public class JDDecryptUtil {

	public static String key = "";
	public static String deviceUUID = "";
	public static String random = "";
	public static boolean isTryRead = false;

	private static final int InputLength = 256;
	
	/**
	 * 解密文件
	 * @param filename 文件路径
	 * @return
	 */
	public synchronized static InputStream decryptFile(String filename) {
		if (isTryRead) {
			//解密试读文件
			return decryptTryFile(filename);
		}
		DecryptHelper.init(key, deviceUUID, random);
		InputStream is = null;
		ByteArrayOutputStream os = null;
		try {
			
			File file = new File(filename);
			if (!file.exists()) {
				return null;
			}
			is = new FileInputStream(file);
			os = new ByteArrayOutputStream();
			
			
			byte[] output = new byte[InputLength * 32];//8k
			byte[] buff = new byte[InputLength];//256b
			long fileLength = file.length();
	        int rc = 0;
	        int endLabel = 0;
	        int outputLength = 0;
	        int clength = 0;
	        while ((rc = is.read(buff, 0, InputLength)) > 0) {
	        	if (fileLength - InputLength > 0) {
	        		fileLength -= InputLength;
	        		endLabel = 0;
	        	} else {
	        		endLabel = 1;
	        	}
	        	
	        	outputLength = DecryptHelper.decrypt(buff, rc, output, output.length, endLabel);
	        	if (outputLength < 0) {
	        		break;
	        	}
	        	outputLength -= clength;
	        	os.write(output, 0, outputLength);
	        	clength += outputLength;
	        }
	        return new ByteArrayInputStream(os.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtil.closeStream(is);
			IOUtil.closeStream(os);
			DecryptHelper.close();
		}
		return null;
	}
	
	private synchronized static InputStream decryptTryFile(String filename) {
		DecryptHelper.create();
		InputStream is = null;
		ByteArrayOutputStream os = null;
		try {
			
			File file = new File(filename);
			if (!file.exists()) {
				return null;
			}
			is = new FileInputStream(file);
			os = new ByteArrayOutputStream();
			
			byte[] output = new byte[InputLength * 32];//8k
			byte[] buff = new byte[InputLength];//256b
			long fileLength = file.length();
	        int rc = 0;
	        int endLabel = 0;
	        int outputLength = 0;
	        int clength = 0;
	        while ((rc = is.read(buff, 0, InputLength)) > 0) {
	        	if (fileLength - InputLength > 0) {
	        		fileLength -= InputLength;
	        		endLabel = 0;
	        	} else {
	        		endLabel = 1;
	        	}
	        	
	        	outputLength = DecryptHelper.decrypt(buff, rc, output, output.length, endLabel);
	        	if (outputLength < 0) {
	        		break;
	        	}
	        	outputLength -= clength;
	        	os.write(output, 0, outputLength);
	        	clength += outputLength;
	        }
	        return new ByteArrayInputStream(os.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtil.closeStream(is);
			IOUtil.closeStream(os);
			DecryptHelper.close();
		}
		
		return null;
	}
}
