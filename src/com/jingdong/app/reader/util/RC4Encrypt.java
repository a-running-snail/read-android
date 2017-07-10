package com.jingdong.app.reader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

public class RC4Encrypt {
	/***
	 * @author ThinkinBunny
	 * @since 2012-9-14
	 * @see 文件加解密算法，使用RC4快速加密解密，相关文件 RC4,RC不存在加密解密，异或
	 * **/
    public final static byte[] encodeKey = "jdreadpdfkey".getBytes();
	final static int FILE_SIZE = 5120;

	// static byte[] cryptCode(byte[] byteE, byte[] key) {
	// byte[] byteFina = new byte[byteE.length];
	// RC4 cipher = null;
	// try {
	// cipher = new RC4(key);
	// cipher.crypt(byteE, byteFina);
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// cipher = null;
	// }
	//
	// return byteFina;
	// }

	// public static String key = "jdreadpdfkey";
	// public static int block = 1024;

	/**
	 *
	 * @since 2012-9-14
	 *
	 * @param sourceFile
	 *            targetFile path
	 * @return targetFile path ,void is ok
	 * @throws Exception
	 * @Description RC无加解密说法，异或算法，未加密的crypt ，加密的crypt复原
	 */
	public static String crypt(String sourceFile, String targetFile)
			throws Exception {
		byte[] target = null;
		File srcFile = new File(sourceFile);

		if (!srcFile.exists()) {
			// System.out.println("文件不存在");
			return null;
		}
		// String newName = srcFile.getAbsolutePath()+".m";
		FileInputStream fis = new FileInputStream(srcFile);
		RandomAccessFile savedFile = new RandomAccessFile(new File(targetFile),
				"rwd");

		// Key key = getKey();
		try {

			byte[] buf = new byte[FILE_SIZE];
			// System.out.println(FILE_SIZE);
			int c = 0;
			RC4 rc4 = new RC4(encodeKey);
			while ((c = fis.read(buf)) != -1) {
				if (c != buf.length) {
					target = new byte[c];
					System.arraycopy(buf, 0, target, 0, c);
				} else {
					target = buf;
				}

				target = rc4.crypt(target);// cipher.doFinal(buf,0,c);
				savedFile.write(target);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return targetFile;
	}
}