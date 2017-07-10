//package com.jingdong.app.reader.data.DrmLib;
public class Cipher {
	public int cipher;
	public static native Cipher getInstance(String transformation);
	public native int init(int mode, byte[] key, byte[]iv);

	public native byte[] update(byte[] data_in);

	public native byte[] doFinal(byte[] data_in);

	public native int destroy();

	static {
		System.loadLibrary("java_drmlib");
	}

	public static void main(String args[]) {
		Cipher cipher = Cipher.getInstance("1/1/0");
		System.out.println("cipher: " + cipher.toString());
		byte [] key = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01};
		byte [] iv = {
				(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
		};
		cipher.init(0, key,  iv);
		cipher.update(iv);
	}
}
