/**
 * this is parser for binary data
 */
package com.jingdong.app.reader.client;

import java.io.IOException;
import java.io.InputStream;
/**
 * @author xuxin@staff.ganji.com
 * @version v1.0.0
 *
 */
public class GJParser {
	/**
	 * parser define
	 */
	public static final String TAG = "GJParser";

	public static final byte[] PARSER_FLAG = { 0x47, 0x44 };// "GD"
	public static final byte PARSER_VERSION = 0x1;// version
	/**
	 * chunk's mark define
	 */
	public static final byte CHUNK_ERROR = 0xB;// 11
	public static final byte CHUNK_META = 0x14;// 20
	public static final byte CHUNK_DATA = 0x15;// 21
	public static final byte CHUNK_HISTORY_POSTS = 0x16; // 22
	public static final byte CHUNK_CITY = 0x1F;// 31
	public static final byte CHUNK_CATEGORY = 0x29;// 41
	public static final byte CHUNK_RESUME = 0x2A; // 42
	public static final byte CHUNK_FILTER = 0x33;// 51
	public static final byte CHUNK_POST_FILTER = 0x34; // 52
	public static final byte CHUNK_REGISTER = 0x3D;// 61
	public static final byte CHUNK_UPDATE = 0x47;// 71
	public static final byte CHUNK_SUMMARY = 0x51; // 81
	public static final byte CHUNK_LOCATION = 0x5B; // 91

	/**
	 * meta chunk's mark define
	 */
	public static final byte META_NAME = 0x1;
	public static final byte META_TITLE = 0x2;
	public static final byte META_SPECIAL = 0x3;
	public static final byte META_LIST = 0x4;
	public static final byte META_DETAIL = 0x5;
	public static final byte META_STYLE = 0x6;
	public static final byte META_CHUNK = 0x7;

	/**
	 * data chunk's mark define
	 */
	public static final byte DATA_TOTAL = 0x1;
	public static final byte DATA_CONTENT = 0x2;
	public static final byte DATA_GUID = 0x03;

	/**
	 * parser
	 *
	 * @throws Exception
	 */
	public static Object parse(InputStream is) throws Exception {

		return null;
	}

	
	@SuppressWarnings("unused")
	private static String readString(InputStream is, int length)
			throws IOException {
		if (length <= 0)
			return null;

		byte[] bs = new byte[length];
		is.read(bs);
		return new String(bs, "UTF-8");

	}

	@SuppressWarnings("unused")
	private static boolean readBoolean(InputStream is) throws IOException {
		byte[] data = new byte[1];
		is.read(data);
		return data[0] > 0 ? true : false;
	}

}
