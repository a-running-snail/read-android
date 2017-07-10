package com.jingdong.app.reader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * 
 * 解压zip文件
 * 
 * @author Michael sun
 */

public class UnzipFile {

	/**
	 * 
	 * 解压zip文件
	 * 
	 * 
	 * 
	 * @param targetPath
	 * 
	 * @param zipFilePath
	 */

	public static void unZipFile0(String targetPath, String zipFilePath) {

		try {

			File zipFile = new File(zipFilePath);

			InputStream is = new FileInputStream(zipFile);

			ZipInputStream zis = new ZipInputStream(is);

			ZipEntry entry = null;

			MZLog.d("unzip","开始解压:" + zipFile.getName() + "...");

			while ((entry = zis.getNextEntry()) != null) {

				String zipPath = entry.getName();

				try {

					if (entry.isDirectory()) {

						File zipFolder = new File(targetPath + File.separator

						+ zipPath);

						if (!zipFolder.exists()) {

							zipFolder.mkdirs();

						}

					} else {

						File file = new File(targetPath + File.separator

						+ zipPath);

						if (!file.exists()) {

							File pathDir = file.getParentFile();

							pathDir.mkdirs();

							file.createNewFile();

						}
//						 InputStream in = zis.(entry);
						
//						StreamToolBox.saveStreamToFile(zis, file);

						FileOutputStream fos = new FileOutputStream(file);
						int bread;

						while ((bread = zis.read()) != -1) {

							fos.write(bread);

						}

						fos.close();

					}

					MZLog.d("unzip","成功解压:" + zipPath);

				} catch (Exception e) {

					System.err.println("解压" + zipPath + "失败");

					continue;

				}

			}

			zis.close();

			is.close();

			MZLog.d("unzip","解压结束");

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	
	/**
	 * 
	 * 解压zip文件
	 * 
	 * 
	 * 
	 * @param targetPath
	 * 
	 * @param zipFilePath
	 */

	public static void unZipFile1(String targetPath, String zipFilePath) {

		try {

			File zipFile = new File(zipFilePath);

	        ZipFile zf = new ZipFile(zipFile);

			ZipEntry entry = null;

			MZLog.d("unzip","开始解压:" + zipFile.getName() + "...");

			 for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {

		        entry = ((ZipEntry)entries.nextElement());

				String zipPath = entry.getName();

				try {

					if (entry.isDirectory()) {

						File zipFolder = new File(targetPath + File.separator

						+ zipPath);

						if (!zipFolder.exists()) {

							zipFolder.mkdirs();

						}

					} else {

						File file = new File(targetPath + File.separator

						+ zipPath);

						if (!file.exists()) {

							File pathDir = file.getParentFile();

							pathDir.mkdirs();

							file.createNewFile();

						}
						 InputStream in = zf.getInputStream(entry);
							StreamToolBox.saveStreamToFile(in, file);
					}
					MZLog.d("unzip","成功解压:" + zipPath);
				} catch (Exception e) {
					System.err.println("解压" + zipPath + "失败");
					continue;
				}
			}
			zf.close();
			MZLog.d("unzip","解压结束");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	  private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte

    /**
056
     * 解压缩一个文件
057
     *
058
     * @param zipFile 压缩文件
059
     * @param folderPath 解压缩的目标目录
060
     * @throws IOException 当解压缩过程出错时抛出
061
     */

    public static void unZipFile(String zipPtah, String folderPath) throws ZipException, IOException {
    	File zipFile = new File(zipPtah);
    	if(!zipFile.exists()){
    		return;
    	}
        File desDir = new File(folderPath);

        if (!desDir.exists()) {

            desDir.mkdirs();

        }

        ZipFile zf = new ZipFile(zipFile);

        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {

            ZipEntry entry = ((ZipEntry)entries.nextElement());

            InputStream in = zf.getInputStream(entry);

            String str = folderPath + File.separator + entry.getName();

//            str = new String(str.getBytes("8859_1"), "GB2312");

            File desFile = new File(str);

            if (!desFile.exists()) {

                File fileParentDir = desFile.getParentFile();

                if (!fileParentDir.exists()) {

                    fileParentDir.mkdirs();

                }

                desFile.createNewFile();

            }

            OutputStream out = new FileOutputStream(desFile);

            byte buffer[] = new byte[BUFF_SIZE];

            int realLength;

            while ((realLength = in.read(buffer)) > 0) {

                out.write(buffer, 0, realLength);
            }

            in.close();

            out.close();
            
        }
        zf.close();

    }

    public static void unInflaterFile(String zipPath, String newPath) throws IOException {
//    	Inflater i = new Inflater();
//    	InflaterInputStream as;
//        /i.
    	StreamToolBox.saveStreamToFile(new  InflaterInputStream(StreamToolBox.loadStreamFromFile(zipPath)), newPath);
    }

	/**
	 * 
	 * @param args
	 */

	public static void main(String[] args) {

		String targetPath = "D:\\test\\unzip";

		String zipFile = "D:\\test\\test.zip";

		//UnzipFile unzip = new UnzipFile();

		try {
			UnzipFile.unZipFile(targetPath, zipFile);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}