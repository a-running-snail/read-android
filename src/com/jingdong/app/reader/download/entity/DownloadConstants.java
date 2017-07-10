package com.jingdong.app.reader.download.entity;

public class DownloadConstants {

	public static String DEFAULT_STORAGE_FOLDER = "jReader";	// 默认下载存储文件夹
	public static final class NetType {
        public static final int INVALID = 0;
        public static final int WAP = 1;
        public static final int G2 = 2;
        public static final int G3 = 3;
        public static final int WIFI = 4;
        public static final int NO_WIFI = 5;
    }
	
	public enum HttpParams{
		POST("GET"),
	    ACCEPT("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword,application/vnd.android.package-archive,*/*"),
	    ACCEPT_LANGUAGE("Accept-Language", "zh-CN"),
	    ACCEPT_RANGE("Accept-Ranges", "bytes"),
	    CHARSET("Charset", "UTF-8"),
	    CONNECT_TIMEOUT("5000"),
	    KEEP_CONNECT("Connection", "Keep-Alive"),
	    LOCATION("location"),
	    REFERER("referer");

	    public String header;// 标题
	    public String content;// 内容

	    private HttpParams(String header, String content) {
	        this.header = header;
	        this.content = content;
	    }

	    private HttpParams(String content) {
	        this.content = content;
	    }
	}
}
