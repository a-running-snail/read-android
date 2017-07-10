package com.jingdong.app.reader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.res.Resources;

import com.jingdong.app.reader.R;

public class TimeFormat {
	private final static String YYYY_MM_DD_FORMAT_STRING = "yyyy-MM-dd";
	private final static String MM_DD_FORMAT_STRING = "HH:mm";
	private final static SimpleDateFormat YY_MM_DD_FORMAT = new SimpleDateFormat(YYYY_MM_DD_FORMAT_STRING, Locale.US);
	private final static SimpleDateFormat HH_MM_FORMAT = new SimpleDateFormat(MM_DD_FORMAT_STRING, Locale.US);
	private static final long MSECOND_TO_MINUTE = 1000 * 60;
	private static final long THOUSAND = 1000;
	private static Calendar oneMinuteAgo;
	private static Calendar oneHourAgo;
	private static Calendar today;
	private static Calendar yesterday;
	private static Calendar currentTime;
	/**
	 * 格式化该条发言的时间。 若本条发言在1分钟前发表，则显示刚刚； 若本条发言在1小时前发表，则显示几分钟前
	 * 若本条发言在今天发表，则显示今天+具体时间； 若本条发言在昨天发表，则显示昨天+具体时间； 若该条发言在其他时间发表，则只显示日期，不显示时间。
	 *
	 * @param timestamp
	 *            一个Unix时间戳，表示发言发表的时间
	 * @return 格式化后的发言时间
	 */
	public static String formatTime(Resources resources,long timestamp) {
		StringBuilder stringBuilder = new StringBuilder();
		Calendar postTime = Calendar.getInstance();
		postTime.setTime(new Date(timestamp * THOUSAND));
		refreshTime();
		if (postTime.after(oneMinuteAgo)) {
			stringBuilder.append(resources.getString(R.string.justnow));
		} else if (postTime.after(oneHourAgo)) {
			long delta = (currentTime.getTimeInMillis() - postTime.getTimeInMillis()) / MSECOND_TO_MINUTE;
			stringBuilder.append(Long.toString(delta));
			stringBuilder.append(' ');
			stringBuilder.append(resources.getString(R.string.minutes_ago));
		} else if (postTime.after(today)) {
			stringBuilder.append(resources.getString(R.string.today));
			stringBuilder.append(' ');
			stringBuilder.append(HH_MM_FORMAT.format(postTime.getTime()));
		} else if (postTime.after(yesterday)) {
			stringBuilder.append(resources.getString(R.string.yesterday));
			stringBuilder.append(' ');
			stringBuilder.append(HH_MM_FORMAT.format(postTime.getTime()));
		} else {
			stringBuilder.append(YY_MM_DD_FORMAT.format(postTime.getTime()));
		}
		return stringBuilder.toString();
	}
	
	public static String formatTime1(Resources resources,long timestamp) {
		StringBuilder stringBuilder = new StringBuilder();
		Calendar postTime = Calendar.getInstance();
		postTime.setTime(new Date(timestamp * THOUSAND));
		refreshTime();
		if (postTime.after(oneMinuteAgo)) {
			long delta = (currentTime.getTimeInMillis() - postTime.getTimeInMillis()) / THOUSAND;
			if (delta < 0) {
				delta = 0;
			}
			stringBuilder.append(Long.toString(delta));
			stringBuilder.append(' ');
			stringBuilder.append(resources.getString(R.string.seconds_ago));
		} else if (postTime.after(oneHourAgo)) {
			long delta = (currentTime.getTimeInMillis() - postTime.getTimeInMillis()) / MSECOND_TO_MINUTE;
			stringBuilder.append(Long.toString(delta));
			stringBuilder.append(' ');
			stringBuilder.append(resources.getString(R.string.minutes_ago));
		} else if (postTime.after(today)) {
			stringBuilder.append(resources.getString(R.string.today));
			stringBuilder.append(' ');
			stringBuilder.append(HH_MM_FORMAT.format(postTime.getTime()));
		} else if (postTime.after(yesterday)) {
			stringBuilder.append(resources.getString(R.string.yesterday));
			stringBuilder.append(' ');
			stringBuilder.append(HH_MM_FORMAT.format(postTime.getTime()));
		} else {
			stringBuilder.append(YY_MM_DD_FORMAT.format(postTime.getTime()));
		}
		return stringBuilder.toString();
	}
	
	public static String formatTimeByMiliSecond(Resources resources,long timestamp) {
		StringBuilder stringBuilder = new StringBuilder();
		Calendar postTime = Calendar.getInstance();
		postTime.setTime(new Date(timestamp));
		refreshTime();
		if (postTime.after(oneMinuteAgo)) {
			stringBuilder.append(resources.getString(R.string.justnow));
		} else if (postTime.after(oneHourAgo)) {
			long delta = (currentTime.getTimeInMillis() - postTime.getTimeInMillis()) / MSECOND_TO_MINUTE;
			stringBuilder.append(Long.toString(delta));
			stringBuilder.append(' ');
			stringBuilder.append(resources.getString(R.string.minutes_ago));
		} else if (postTime.after(today)) {
			stringBuilder.append(resources.getString(R.string.today));
			stringBuilder.append(' ');
			stringBuilder.append(HH_MM_FORMAT.format(postTime.getTime()));
		} else if (postTime.after(yesterday)) {
			stringBuilder.append(resources.getString(R.string.yesterday));
			stringBuilder.append(' ');
			stringBuilder.append(HH_MM_FORMAT.format(postTime.getTime()));
		} else {
			stringBuilder.append(YY_MM_DD_FORMAT.format(postTime.getTime()));
		}
		return stringBuilder.toString();
	}
	
	
	/**
	 * 根据当前时间，重新设置目前，1分钟，1小时，今天0点，昨天0点的时间戳
	 */
	private static void refreshTime() {
		oneMinuteAgo = Calendar.getInstance(Locale.US);
		oneHourAgo = Calendar.getInstance(Locale.US);
		today = Calendar.getInstance(Locale.US);
		yesterday = Calendar.getInstance(Locale.US);
		oneMinuteAgo.add(Calendar.MINUTE, -1);
		oneHourAgo.add(Calendar.HOUR, -1);
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
		setZeroClockInADay(today);
		setZeroClockInADay(yesterday);
		currentTime = Calendar.getInstance(Locale.US);
	}

	public static String formatTime(long time){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		Date date=new Date(time*1000);
		return format.format(date);
	}
	
	public static long formatStringTime(String time){
		
		 try { 
			 SimpleDateFormat sdf =  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			 Date date = sdf.parse(time);
			 return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			 return -1;
		}
	}
	
	public static long getHour(String time){
		
		try { 
			SimpleDateFormat sdf =  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			Date date = sdf.parse(time);
			return date.getHours();
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	
	
	/**
	 * 设置日期为当天的0点0分0秒0毫秒
	 *
	 * @param calendar
	 *            待设置时间
	 */
	private static void setZeroClockInADay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	   /**
     * 获取时间戳 ,格式yyyy-mm-dd hh:mm:ss<br>
     * 2011-3-9
     *
     * @param
     * @return String timeStamp
     */
    public static String getTimeStamp() {
        //TimeZone t = TimeZone.getTimeZone("GMT+8");
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR); // date.getYear();
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return (year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second);
    }

    /**
     * 
     * @param time1
     * @param time2
     * @return  大于1天返回 true 小于1天返回false
     * 
     */
    public static boolean DateCompare(long time1,long  time2) {		
      //比较	是否大于1天
    	if(Math.abs(((time1 - time2)/(24*3600*1000))) >=1) 
    	{		
    		return true;	}
    	else{	
    		return false;
    	}
    	}
	
	
}
