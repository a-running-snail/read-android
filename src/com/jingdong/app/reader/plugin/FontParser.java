package com.jingdong.app.reader.plugin;

/**
 * 字体解析器，解析字体名称
 * @author yfxiawei yfxiawei@360buy.com
 * 上午9:33:02
 *
 */
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import android.widget.Toast;

import com.jingdong.app.reader.data.db.DBHelper;
import com.jingdong.app.reader.util.ToastUtil;

public class FontParser {

	public static int COPYRIGHT = 0;

	public static int FAMILY_NAME = 1;

	public static int FONT_SUBFAMILY_NAME = 2;

	public static int UNIQUE_FONT_IDENTIFIER = 3;

	public static int FULL_FONT_NAME = 4;

	public static int VERSION = 5;

	public static int POSTSCRIPT_NAME = 6;

	public static int TRADEMARK = 7;

	public static int MANUFACTURER = 8;

	public static int DESIGNER = 9;

	public static int DESCRIPTION = 10;

	public static int URL_VENDOR = 11;

	public static int URL_DESIGNER = 12;

	public static int LICENSE_DESCRIPTION = 13;

	public static int LICENSE_INFO_URL = 14;

	private final Map<Integer, String> fontProperties = new HashMap<Integer, String>();
	private static FontParser fontParser = null;

	public static FontParser getInstance() {
		if (null == fontParser) {
			fontParser = new FontParser();
		}
		return fontParser;

	}

	/**
	 * 
	 */
	private FontParser() {
		// TODO 自动生成的构造函数存根
	}

	/**********
	 * 保存字体信息到数据库 导入字体会检测 字体的完成性，如果获取不到字体名提示导入失败
	 * 
	 * @author yfxiawei 2013年2月27日17:34:01
	 * @param filePath
	 *            文件绝对路径
	 * @param fileSize
	 *            文件大小
	 * @exception 如果字体文件无效直接返回
	 *                ，不继续插入数据库
	 * @since version 1.2.4
	 * *****/
	public void saveFontItemToTable(String filePath, long fileSize) {
		parse(filePath);

		FontItem fontItem = new FontItem(getFontName(), filePath, null,
				fileSize, fileSize, FontItem.STATE_LOADED,
				FontItem.KEY_PLUGIN_FONT, FontItem.KEY_FONT_SRC_IMPORT);
		if (fontItem.getName() == null) {
			ToastUtil.showToastInThread("无效的字体文件:" + filePath,Toast.LENGTH_SHORT);
			return;
		}
		DBHelper.savePlugin(fontItem);

	}

	/**********
	 * 检测字体是否已经导入
	 * 
	 * @author yfxiawei 2013年2月27日17:34:01
	 * 
	 * *****/
	public static boolean isImported(String filePath) {
		return DBHelper.fontIsImported(filePath);

	}

	/**
	 * 
	 * 获取ttf font name
	 * 
	 * @return
	 */

	public String getFontName() {

		if (fontProperties.containsKey(FULL_FONT_NAME)) {

			return fontProperties.get(FULL_FONT_NAME);

		} else if (fontProperties.containsKey(FAMILY_NAME)) {

			return fontProperties.get(FAMILY_NAME);

		} else {

			return null;

		}

	}

	/**
	 * 
	 * 获取ttf属性
	 * 
	 * @param nameID
	 *            属性标记，见静态变量
	 * 
	 * @return 属性值
	 */

	public String getFontPropertie(int nameID) {

		if (fontProperties.containsKey(nameID)) {

			return fontProperties.get(nameID);

		} else {
			return null;
		}

	}

	/**
	 * 
	 * 获取ttf属性集合
	 * 
	 * @return 属性集合(MAP)
	 */

	public Map<Integer, String> getFontProperties() {
		return fontProperties;
	}

	/**
	 * 
	 * 执行解析 解析成功返回true
	 * 
	 * @param fileName
	 *            ttf文件名
	 * 
	 * @throws IOException
	 * 
	 */

	public boolean parse(String fileName) {

		fontProperties.clear();

		RandomAccessFile f = null;

		try {

			f = new RandomAccessFile(fileName, "r");

			parseInner(f);
			if (null != getFontName())
				return true;

		} catch (IOException exception) {
		} finally {

			try {

				f.close();

			} catch (Exception e) {

				// ignore;

			}

		}
		return false;

	}

	private void parseInner(RandomAccessFile randomAccessFile)
			throws IOException {

		int majorVersion = randomAccessFile.readShort();

		int minorVersion = randomAccessFile.readShort();

		int numOfTables = randomAccessFile.readShort();

		if (majorVersion != 1 || minorVersion != 0) {
			return;
		}

		// jump to TableDirectory struct

		randomAccessFile.seek(12);

		boolean found = false;

		byte[] buff = new byte[4];

		TableDirectory tableDirectory = new TableDirectory();

		for (int i = 0; i < numOfTables; i++) {

			randomAccessFile.read(buff);

			tableDirectory.name = new String(buff);

			tableDirectory.checkSum = randomAccessFile.readInt();

			tableDirectory.offset = randomAccessFile.readInt();

			tableDirectory.length = randomAccessFile.readInt();

			if ("name".equalsIgnoreCase(tableDirectory.name)) {

				found = true;

				break;

			} else if (tableDirectory.name == null
					|| tableDirectory.name.length() == 0) {

				break;

			}

		}

		// not found table of name

		if (!found) {
			return;
		}

		randomAccessFile.seek(tableDirectory.offset);

		NameTableHeader nameTableHeader = new NameTableHeader();

		nameTableHeader.fSelector = randomAccessFile.readShort();

		nameTableHeader.nRCount = randomAccessFile.readShort();

		nameTableHeader.storageOffset = randomAccessFile.readShort();

		NameRecord nameRecord = new NameRecord();

		for (int i = 0; i < nameTableHeader.nRCount; i++) {

			nameRecord.platformID = randomAccessFile.readShort();

			nameRecord.encodingID = randomAccessFile.readShort();

			nameRecord.languageID = randomAccessFile.readShort();

			nameRecord.nameID = randomAccessFile.readShort();

			nameRecord.stringLength = randomAccessFile.readShort();

			nameRecord.stringOffset = randomAccessFile.readShort();

			long pos = randomAccessFile.getFilePointer();

			byte[] bf = new byte[nameRecord.stringLength];

			long vpos = tableDirectory.offset + nameRecord.stringOffset
					+ nameTableHeader.storageOffset;

			randomAccessFile.seek(vpos);

			randomAccessFile.read(bf);

			String temp = new String(bf, Charset.forName("utf-16").name());// new
			// String(bf,
			// Charset.forName("utf-16"));

			fontProperties.put(nameRecord.nameID, temp);

			randomAccessFile.seek(pos);

		}

	}

	@Override
	public String toString() {

		return fontProperties.toString();

	}

	private static class TableDirectory {

		String name; // table name

		int checkSum; // Check sum

		int offset; // Offset from beginning of file

		int length; // length of the table in bytes

	}

	private static class NameTableHeader {

		int fSelector; // format selector. Always 0

		int nRCount; // Name Records count

		int storageOffset; // Offset for strings storage,

	}

	private static class NameRecord {

		int platformID;

		int encodingID;

		int languageID;

		int nameID;

		int stringLength;

		int stringOffset; // from start of storage area

	}

}