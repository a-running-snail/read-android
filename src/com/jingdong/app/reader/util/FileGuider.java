package com.jingdong.app.reader.util;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.user.LocalUserSetting;

public class FileGuider {
	
	public static int SPACE_ONLY_INTERNAL = 0;// 优先选择内部存储空间然后外部存储空间
	public static int SPACE_ONLY_EXTERNAL = 1;// 优先选择外部存储空间然后内部存储空间
	public static int SPACE_PRIORITY_INTERNAL = 2;// 优先选择内部存储空间或外部存储空间
	public static int SPACE_PRIORITY_EXTERNAL = 3;

	private int space;// 优先选择内部存储空间或外部存储空间
	private boolean immutable;// 当优先选择的存储空间不存在或者空间不足时可否选择另一存储空间
	private long TotalSize;// 总空间
	private long AvailableSize;// 可用空间
	private String childDirName;// 子目录
	private String fileName;// 文件名
	// private int mode;// 权限
	// private int internalType;// 内部存储空间类型
	private final File root;
	private int currentSpacesad;
	
	public FileGuider(int space) {
		this.space = space;
		root = getRoot();
	}

	/**
	 * 检查父级文件夹是否存在
	 * @return
	 * @throws IOException
	 */
	public boolean checkParentPath() throws IOException {
		String path = getParentPath();
		if (path == null) {
			return false;
		}
		File f = new File(getParentPath());
		if (!f.exists()) {
			f.mkdirs();
		}
		return true;
	}

	public File getRoot() {
		File root = null;
		
		if(!TextUtils.isEmpty(LocalUserSetting.getSaveBookDir( MZBookApplication.getContext())))
		{
			root =new File(LocalUserSetting.getSaveBookDir( MZBookApplication.getContext())+File.separator+"JingdongReader");
			
			if (!root.exists()) {
				root = new File( LocalUserSetting.getSaveBookDir(MZBookApplication.getContext()) + 
							File.separator+ "Android"+File.separator +"data"+File.separator + 
							MZBookApplication.getContext().getPackageName()); 
				if (root.exists())
					root.mkdir();
		   }
			
		}
		
		else{
		
		long availableSize = getAvailableSize();
		if (SPACE_ONLY_INTERNAL == space) {
			root = MZBookApplication.getContext().getFilesDir();
		} else if (SPACE_ONLY_EXTERNAL == space) {
			root = MZBookApplication.getContext()
					.getExternalFilesDir(null);// Environment.getExternalStorageDirectory();
		} else if (SPACE_PRIORITY_INTERNAL == space) {
			if (getAvailableInternalMemorySize() > availableSize // 内部存储空间足够
			) {
				root = MZBookApplication.getContext().getFilesDir();
			} else if (externalMemoryAvailable()
					&& getAvailableExternalMemorySize() > availableSize) {
				// root = Environment.getExternalStorageDirectory();
				root = MZBookApplication.getContext()
						.getExternalFilesDir(null);// Environment.getExternalStorageDirectory();
			}
		} else if (SPACE_PRIORITY_EXTERNAL == space) {
			if (externalMemoryAvailable()
					&& getAvailableExternalMemorySize() > availableSize) {
				// root = Environment.getExternalStorageDirectory();
				root = MZBookApplication.getContext()
						.getExternalFilesDir(null);
			} else if (getAvailableInternalMemorySize() > availableSize) {// 内部存储空间足够
				root = MZBookApplication.getContext().getFilesDir();
			}
		}
		}
		return root;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-3-23 下午04:23:09
	 * 
	 *         Name:
	 * 
	 *         Description: 内部可用空间大小
	 * 
	 * @return
	 * 
	 */
	static public long getAvailableInternalMemorySize() {

		File path = Environment.getDataDirectory();

		StatFs stat = new StatFs(path.getPath());

		long blockSize = stat.getBlockSize();

		long availableBlocks = stat.getAvailableBlocks();

		return availableBlocks * blockSize;

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-3-23 下午04:23:48
	 * 
	 *         Name:
	 * 
	 *         Description: 外部可用空间大小
	 * 
	 * @return
	 * 
	 */
	static public long getAvailableExternalMemorySize() {

		if (externalMemoryAvailable()) {

			File path = Environment.getExternalStorageDirectory();

			StatFs stat = new StatFs(path.getPath());

			long blockSize = stat.getBlockSize();

			long availableBlocks = stat.getAvailableBlocks();

			return availableBlocks * blockSize;

		} else {

			return -1;

		}

	}

	public static boolean externalMemoryAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public String getFilePath() {
		try {
			if (!checkParentPath()) {
				return null;
			}
			;
			String path = getParentPath() + "/" + getFileName();
			return path;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public File getFile() {
		File file = new File(getFilePath());
		return file;
	}

	public static String getPath(int pace, String temp, String fileName) {
		FileGuider fileGuider = new FileGuider(pace);
		fileGuider.setChildDirName(temp);
		// if(isMd5FileName){
		// fileGuider.setFileName(MD5Calculator.calculateMD5(fileName));
		// }else{
		fileGuider.setFileName(fileName);
		// }
		if (!TextUtils.isEmpty(fileName)) {
			return fileGuider.getFilePath();
		} else {
			try {
				return fileGuider.getParentPath();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public int getSpace() {
		return space;
	}


	public void setImmutable(boolean immutable) {
		this.immutable = immutable;
	}

	public long getAvailableSize() {
		return AvailableSize;
	}

	public void setAvailableSize(long availableSize) {
		AvailableSize = availableSize;
	}

	public void setChildDirName(String childDirName) {
		this.childDirName = childDirName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getParentPath() throws IOException {
		if (childDirName == null && root != null) {
			return root.getAbsolutePath();
		}

		if (root == null) {
			return null;
		}
		return root.getAbsolutePath() + File.separator + childDirName;
	}

}
