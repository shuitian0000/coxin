package com.codeim.coxin.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.util.Log;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.db.TwitterDatabase;
import com.codeim.coxin.http.HttpException;

public class DateTimeHelper {
	private static final String TAG = "DateTimeHelper";

	// Wed Dec 15 02:53:36 +0000 2010
	public static final DateFormat TWITTER_DATE_FORMATTER = new SimpleDateFormat("E MMM d HH:mm:ss Z yyyy", Locale.US);

	public static final DateFormat TWITTER_SEARCH_API_DATE_FORMATTER = new SimpleDateFormat(
			"E, d MMM yyyy HH:mm:ss Z", Locale.US); // TODO: Z -> z ?
	
	public static final String LOCAL_DEFAULT_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
	public static final DateFormat LOCAL_DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

	public static final Date parseDateTime(String dateString) {
		try {
			Log.v(TAG, String.format("in parseDateTime, dateString=%s", dateString));
			return TWITTER_DATE_FORMATTER.parse(dateString);
		} catch (ParseException e) {
			Log.w(TAG, "Could not parse Twitter date string: " + dateString);
			return null;
		}
	}

	// Handle "yyyy-MM-dd'T'HH:mm:ss.SSS" from sqlite
	public static final Date parseDateTimeFromSqlite(String dateString) {
		try {
			Log.v(TAG, String.format("in parseDateTime, dateString=%s", dateString));
			return TwitterDatabase.DB_DATE_FORMATTER.parse(dateString);
		} catch (ParseException e) {
			Log.w(TAG, "Could not parse Twitter date string: " + dateString);
			return null;
		}
	}

	public static final Date parseSearchApiDateTime(String dateString) {
		try {
			return TWITTER_SEARCH_API_DATE_FORMATTER.parse(dateString);
		} catch (ParseException e) {
			Log.w(TAG, "Could not parse Twitter search date string: " + dateString);
			return null;
		}
	}

	public static final DateFormat AGO_FULL_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

	public static String getRelativeDate(Date date) {
		Date now = new Date();

		String prefix = TwitterApplication.mContext.getString(R.string.tweet_created_at_beautify_prefix);
		String sec = TwitterApplication.mContext.getString(R.string.tweet_created_at_beautify_sec);
		String min = TwitterApplication.mContext.getString(R.string.tweet_created_at_beautify_min);
		String hour = TwitterApplication.mContext.getString(R.string.tweet_created_at_beautify_hour);
		String day = TwitterApplication.mContext.getString(R.string.tweet_created_at_beautify_day);
		String suffix = TwitterApplication.mContext.getString(R.string.tweet_created_at_beautify_suffix);

		// Seconds.
		long diff = (now.getTime() - date.getTime()) / 1000;
		
		Log.d(TAG, "now time: " + now.getTime());
		Log.d(TAG, "now time: " + now.toString());
		Log.d(TAG, "date time: " + date.getTime());
		Log.d(TAG, "date time: " + date.toString());
		Log.d(TAG, "time: " + diff);

		if (diff < 0) {
			diff = 0;
		}

		if (diff < 60) {
			return diff + sec + suffix;
		}

		// Minutes.
		diff /= 60;

		if (diff < 60) {
			return prefix + diff + min + suffix;
		}

		// Hours.
		diff /= 60;

		if (diff <= 24) {
			return prefix + diff + hour + suffix;
		}
		
		// days.
		diff /= 24;
		
		if (diff >= 1 && diff < 80) {
		    return prefix + diff + day + suffix;
		} else {
		    return prefix + "80" + day + suffix;
		}

		// return AGO_FULL_DATE_FORMATTER.format(date);
	}

	public static long getNowTime() {
		return Calendar.getInstance().getTime().getTime();
	}
	
	//the follow by ywwang. from WeiboResponse
	public static Date parseDate(String str, String format)
			throws HttpException {
		//private static Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
		Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
		
		if (str == null || "".equals(str)) {
			return null;
		}
		SimpleDateFormat sdf = formatMap.get(format);
		if (null == sdf) {
			sdf = new SimpleDateFormat(format, Locale.US);
			String defaultTimeZoneID = TimeZone.getDefault().getID();//Asia/Shanghai
			Log.v("TAG","默认时区(中国/上海)：" + defaultTimeZoneID);
			sdf.setTimeZone(TimeZone.getTimeZone(defaultTimeZoneID));
//			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			formatMap.put(format, sdf);
		}
		try {
			synchronized (sdf) {
				// SimpleDateFormat is not thread safe
				return sdf.parse(str);
			}
		} catch (ParseException pe) {
			throw new HttpException("Unexpected format(" + str
					+ ") returned from sina.com.cn");
		}
	}
	public static Date parseDateFromStr(String str, String format)
			{
		//private static Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
		Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
		
		if (str == null || "".equals(str)) {
			return null;
		}
		SimpleDateFormat sdf = formatMap.get(format);
		if (null == sdf) {
			sdf = new SimpleDateFormat(format, Locale.US);
			String defaultTimeZoneID = TimeZone.getDefault().getID();//Asia/Shanghai
			sdf.setTimeZone(TimeZone.getTimeZone(defaultTimeZoneID));
			//sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			formatMap.put(format, sdf);
		}
		try {
			synchronized (sdf) {
				// SimpleDateFormat is not thread safe
				return sdf.parse(str);
			}
		} catch (ParseException pe) {
			Log.e("parseDateFromStr", "Unexpected format(" + str
					+ ")");
			return null;
		}
	}
	
	public static String dateToString(Date data, String format) {
		Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
		
		if(format=="") format = LOCAL_DEFAULT_FORMAT_STRING;
		
		if (data == null || "".equals(data)) {
			return null;
		}
		
		SimpleDateFormat sdf = formatMap.get(format);
		if (null == sdf) {
			sdf = new SimpleDateFormat(format, Locale.US);
			
			String defaultTimeZoneID = TimeZone.getDefault().getID();//Asia/Shanghai
			Log.v("TAG","默认时区(中国/上海)：" + defaultTimeZoneID);
			sdf.setTimeZone(TimeZone.getTimeZone(defaultTimeZoneID));
//			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			formatMap.put(format, sdf);
		}
		
		synchronized (sdf) {
		// SimpleDateFormat is not thread safe
		return sdf.format(data);
	    }
		
//		try {
//			synchronized (sdf) {
//				// SimpleDateFormat is not thread safe
//				return sdf.format(data);
//			}
//		}
//		catch (ParseException pe) {
//			throw new HttpException("Unexpected format(" + data
//					+ ") returned from sina.com.cn");
//		}
		
	}
	
	public static Date timeAdd(Date oldTime, int addDays, int addHours, int addMins, String format) {
		Date newTime;
		long addValue;
		newTime = oldTime;
		
		addValue = addDays * 24 * 60 * 60 * 1000 + addHours * 60 * 60 * 1000 + addMins * 60 * 1000;
		newTime = new Date(oldTime.getTime() + addValue);

		return newTime;
	}
	
	public static Date timeAddValue(Date oldTime, long addValue, String format) {
		Date newTime;
		newTime = oldTime;
		
		newTime = new Date(oldTime.getTime() + addValue);

		return newTime;
	}
}
