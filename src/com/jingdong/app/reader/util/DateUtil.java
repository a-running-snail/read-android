package com.jingdong.app.reader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtil {

	/*
	 *  Copyright  sunflower
	 *
	 *  Licensed under the Apache License, Version 2.0 (the "License");
	 *  you may not use this file except in compliance with the License.
	 *  You may obtain a copy of the License at
	 *
	 *      http://www.apache.org/licenses/LICENSE-2.0
	 *
	 *  Unless required by applicable law or agreed to in writing, software
	 *  distributed under the License is distributed on an "AS IS" BASIS,
	 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 *  See the License for the specific language governing permissions and
	 *  limitations under the License.
	 *
	 */

		private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd");
		private static final SimpleDateFormat timeFormat = new SimpleDateFormat(
				"HH:mm:ss");
		private static final SimpleDateFormat timeHMFormat = new SimpleDateFormat(
				"HH:mm");

		/**
		 * 获得当前日期时间
		 * <p>
		 * 日期时间格式yyyy-MM-dd HH:mm:ss
		 * 
		 * @return
		 */
		public static String currentDatetime() {
			return datetimeFormat.format(now());
		}

		/**
		 * 格式化日期时间
		 * <p>
		 * 日期时间格式yyyy-MM-dd HH:mm:ss
		 * 
		 * @return
		 */
		public static String formatDatetime(Date date) {
			return datetimeFormat.format(date);
		}

		/**
		 * 格式化日期时间
		 * 
		 * @param date
		 * @param pattern
		 *            格式化模式，详见{@link SimpleDateFormat}构造器
		 *            <code>SimpleDateFormat(String pattern)</code>
		 * @return
		 */
		public static String formatDatetime(Date date, String pattern) {
			SimpleDateFormat customFormat = (SimpleDateFormat) datetimeFormat
					.clone();
			customFormat.applyPattern(pattern);
			return customFormat.format(date);
		}

		/**
		 * 获得当前日期
		 * <p>
		 * 日期格式yyyy-MM-dd
		 * 
		 * @return
		 */
		public static String currentDate() {
			return dateFormat.format(now());
		}

		/**
		 * 格式化日期
		 * <p>
		 * 日期格式yyyy-MM-dd
		 * 
		 * @return
		 */
		public static String formatDate(Date date) {
			return dateFormat.format(date);
		}

		/**
		 * 获得当前时间
		 * <p>
		 * 时间格式HH:mm:ss
		 * 
		 * @return
		 */
		public static String currentTime() {
			return timeFormat.format(now());
		}

		/**
		 * 格式化时间
		 * <p>
		 * 时间格式HH:mm:ss
		 * 
		 * @return
		 */
		public static String formatTime(Date date) {
			return timeFormat.format(date);
		}
		
		/**
		 * 获得当前时间
		 * <p>
		 * 时间格式HH:mm
		 * 
		 * @return
		 */
		public static String currentTimeHM() {
			return timeHMFormat.format(now());
		}

		/**
		 * 获得当前时间的<code>java.util.Date</code>对象
		 * 
		 * @return
		 */
		public static Date now() {
			return new Date();
		}

		public static Calendar calendar() {
			Calendar cal = GregorianCalendar.getInstance(Locale.CHINESE);
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			return cal;
		}

		/**
		 * 获得当前时间的毫秒数
		 * <p>
		 * 详见{@link System#currentTimeMillis()}
		 * 
		 * @return
		 */
		public static long millis() {
			return System.currentTimeMillis();
		}

		/**
		 * 
		 * 获得当前Chinese月份
		 * 
		 * @return
		 */
		public static int month() {
			return calendar().get(Calendar.MONTH) + 1;
		}

		/**
		 * 获得月份中的第几天
		 * 
		 * @return
		 */
		public static int dayOfMonth() {
			return calendar().get(Calendar.DAY_OF_MONTH);
		}

		/**
		 * 今天是星期的第几天
		 * 
		 * @return
		 */
		public static int dayOfWeek() {
			return calendar().get(Calendar.DAY_OF_WEEK);
		}

		/**
		 * 今天是年中的第几天
		 * 
		 * @return
		 */
		public static int dayOfYear() {
			return calendar().get(Calendar.DAY_OF_YEAR);
		}

		/**
		 *判断原日期是否在目标日期之前
		 * 
		 * @param src
		 * @param dst
		 * @return
		 */
		public static boolean isBefore(Date src, Date dst) {
			return src.before(dst);
		}

		/**
		 *判断原日期是否在目标日期之后
		 * 
		 * @param src
		 * @param dst
		 * @return
		 */
		public static boolean isAfter(Date src, Date dst) {
			return src.after(dst);
		}

		/**
		 *判断两日期是否相同
		 * 
		 * @param date1
		 * @param date2
		 * @return
		 */
		public static boolean isEqual(Date date1, Date date2) {
			return date1.compareTo(date2) == 0;
		}

		/**
		 * 判断某个日期是否在某个日期范围
		 * 
		 * @param beginDate
		 *            日期范围开始
		 * @param endDate
		 *            日期范围结束
		 * @param src
		 *            需要判断的日期
		 * @return
		 */
		public static boolean between(Date beginDate, Date endDate, Date src) {
			return beginDate.before(src) && endDate.after(src);
		}

		/**
		 * 获得当前月的最后一天
		 * <p>
		 * HH:mm:ss为0，毫秒为999
		 * 
		 * @return
		 */
		public static Date lastDayOfMonth() {
			Calendar cal = calendar();
			cal.set(Calendar.DAY_OF_MONTH, 0); // M月置零
			cal.set(Calendar.HOUR_OF_DAY, 0);// H置零
			cal.set(Calendar.MINUTE, 0);// m置零
			cal.set(Calendar.SECOND, 0);// s置零
			cal.set(Calendar.MILLISECOND, 0);// S置零
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);// 月份+1
			cal.set(Calendar.MILLISECOND, -1);// 毫秒-1
			return cal.getTime();
		}

		/**
		 * 获得当前月的第一天
		 * <p>
		 * HH:mm:ss SS为零
		 * 
		 * @return
		 */
		public static Date firstDayOfMonth() {
			Calendar cal = calendar();
			cal.set(Calendar.DAY_OF_MONTH, 1); // M月置1
			cal.set(Calendar.HOUR_OF_DAY, 0);// H置零
			cal.set(Calendar.MINUTE, 0);// m置零
			cal.set(Calendar.SECOND, 0);// s置零
			cal.set(Calendar.MILLISECOND, 0);// S置零
			return cal.getTime();
		}

		private static Date weekDay(int week) {
			Calendar cal = calendar();
			cal.set(Calendar.DAY_OF_WEEK, week);
			return cal.getTime();
		}

		/**
		 * 获得周五日期
		 * <p>
		 * 注：日历工厂方法{@link #calendar()}设置类每个星期的第一天为Monday，US等每星期第一天为sunday
		 * 
		 * @return
		 */
		public static Date friday() {
			return weekDay(Calendar.FRIDAY);
		}

		/**
		 * 获得周六日期
		 * <p>
		 * 注：日历工厂方法{@link #calendar()}设置类每个星期的第一天为Monday，US等每星期第一天为sunday
		 * 
		 * @return
		 */
		public static Date saturday() {
			return weekDay(Calendar.SATURDAY);
		}

		/**
		 * 获得周日日期
		 * <p>
		 * 注：日历工厂方法{@link #calendar()}设置类每个星期的第一天为Monday，US等每星期第一天为sunday
		 * 
		 * @return
		 */
		public static Date sunday() {
			return weekDay(Calendar.SUNDAY);
		}

		/**
		 * 将字符串日期时间转换成java.util.Date类型
		 * <p>
		 * 日期时间格式yyyy-MM-dd HH:mm:ss
		 * 
		 * @param datetime
		 * @return
		 */
		public static Date parseDatetime(String datetime) throws ParseException {
			return datetimeFormat.parse(datetime);
		}

		/**
		 * 将字符串日期转换成java.util.Date类型
		 *<p>
		 * 日期时间格式yyyy-MM-dd
		 * 
		 * @param date
		 * @return
		 * @throws ParseException
		 */
		public static Date parseDate(String date) throws ParseException {
			return dateFormat.parse(date);
		}

		/**
		 * 将字符串日期转换成java.util.Date类型
		 *<p>
		 * 时间格式 HH:mm:ss
		 * 
		 * @param time
		 * @return
		 * @throws ParseException
		 */
		public static Date parseTime(String time) throws ParseException {
			return timeFormat.parse(time);
		}

		/**
		 * 根据自定义pattern将字符串日期转换成java.util.Date类型
		 * 
		 * @param datetime
		 * @param pattern
		 * @return
		 * @throws ParseException
		 */
		public static Date parseDatetime(String datetime, String pattern)
				throws ParseException {
			SimpleDateFormat format = (SimpleDateFormat) datetimeFormat.clone();
			format.applyPattern(pattern);
			return format.parse(datetime);
		}
		
		/** 
	     * 获取两个日期相差的天数 
	     * @param date 日期字符串 
	     * @param otherDate 另一个日期字符串 
	     * @return 相差天数 
	     */  
	    public static int getIntervalDays(String date, String otherDate) {  
	        return getIntervalDays(string2date(date, dateFormat), string2date(otherDate, dateFormat));  
	    }  
	      
	    /** 
	     * @param date 日期 
	     * @param otherDate 另一个日期 
	     * @return 相差天数 
	     */  
	    public static int getIntervalDays(Date date, Date otherDate) {  
	        long time = Math.abs(date.getTime() - otherDate.getTime());  
	        return (int)time/(24 * 60 * 60 * 1000);  
	    }  
	    
	    /**
	     * 
	     * @param dateString
	     * @param format
	     * @return
	     */
	    public static Date string2date(String dateString, SimpleDateFormat format) {  
	        Date formateDate = null;  
	        try {  
	            formateDate = format.parse(dateString);  
	        } catch (ParseException e) {  
	            return null;  
	        }  
	        return formateDate;  
	    }
	    
	    public static String date2string(Date date, String formatStr) {  
	        String strDate = "";  
	        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);  
	        strDate = sdf.format(date);  
	        return strDate;  
	    } 
	    
	    /**  
	     * 计算两个日期之间相差的天数  
	     * @param smdate 较小的时间 
	     * @param bdate  较大的时间 
	     * @return 相差天数 
	     * @throws ParseException  
	     */    
	    public static int daysBetween(Date smdate,Date bdate) throws ParseException{    
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
	        smdate=sdf.parse(sdf.format(smdate));  
	        bdate=sdf.parse(sdf.format(bdate));  
	        Calendar cal = Calendar.getInstance();    
	        cal.setTime(smdate);    
	        long time1 = cal.getTimeInMillis();                 
	        cal.setTime(bdate);    
	        long time2 = cal.getTimeInMillis();         
	        long between_days=(time2-time1)/(1000*3600*24);  
	            
	       return Integer.parseInt(String.valueOf(between_days));           
	    }    
	
	    /**
		 * 计算两个时间差，显示文字形式
		 * @param serverTime
		 * @param commentTime
		 * @return
		 */
		public static String daytimeBetweenText(String serverDate,String commentDate) throws ParseException{
			long serverTime = parseDatetime(serverDate).getTime();
			long commentTime = parseDatetime(commentDate).getTime();
			
			long sub=serverTime-commentTime;
			int result=0;
			result=(int) (sub/86400000/365);
			if(result>0)
				return result+"年前";
			result=(int) (sub/86400000);
			if(result>0)
				return result+"天前";
			result=(int) (sub/3600000);
			if(result>0)
				return result+"小时前";
			result=(int) (sub/60000);
			if(result>0)
				return result+"分前";
			result=(int) (sub/(1000));
			if(result>0)
				return result+"秒前";
			return "刚刚";
		}
}




