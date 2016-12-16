/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.codeim.coxin.fanfou;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.database.Cursor;
import android.util.Log;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.db.MessageTable;
import com.codeim.coxin.db.TwitterDatabase;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;
import com.codeim.coxin.util.DateTimeHelper;
//import com.codeim.coxin.db.UserInfoTable;

/**
 * A data class representing Basic info information element
 */
//public class Info extends WeiboResponse implements java.io.Serializable {
public class Chat {
	
	private String id;
	private String ownerId;
	
	private String otherId;
	private String otherName;
	private String otherImageUrl;
	
	private String lastChatMsgId;
	private Date lastChatMsgTime;
	private String lastChatMsgContent;
	
	private int unreadCount;
	
	private static final long serialVersionUID = -6345893237975349030L;

	/* package */public Chat(JSONObject json) throws HttpException {
		super();
		Log.d("Info", "json before init");
		init(json);
	}

	/* package */Chat(Response res) throws HttpException {
		super();
		init(res.asJSONObject());
	}

	Chat() {

	}

	private void init(JSONObject json) throws HttpException {
		try {
			id = json.getString("id");
			Log.d("chat_id:", id);
			
			ownerId = json.getString("ownerId");
			
			otherId = json.getString("otherId");
			otherName = json.getString("otherName");
			otherImageUrl = json.getString("otherImageUrl");
			
			lastChatMsgId = json.getString("lastChatMsgId");
			lastChatMsgTime = DateTimeHelper.parseDate(json.getString("lastChatMsgTime"), "yyyy-MM-dd HH:mm:ss");
			lastChatMsgContent = json.getString("lastChatMsgContent");
			
			unreadCount = 1;//json.getInt("unreadCount");
		} catch (JSONException jsone) {
			throw new HttpException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/**
	 * Returns the id of the Info
	 *
	 * @return the id of the Info
	 */
	public String getId() {
		return id;
	}
	public String getOwnerId() {
		return ownerId;
	}
	
	public String getOtherId() {
		return otherId;
	}
	public String getOtherName() {
		return otherName;
	}
	public String getOtherImageUrl() {
		return TwitterApplication.mApi.getBaseURL()+otherImageUrl;
	}
//	public URL getOtherImageUrl() {
//		try {
//		    return new URL(TwitterApplication.mApi.getBaseURL()+otherImageUrl);
//	    } catch (MalformedURLException ex) {
//		    return null;
//	    }
//	}
	
	public String getLatestId() {
		return lastChatMsgId;
	}
	public Date getLatestTime() {
		return lastChatMsgTime;
	}
	public String getLatestContent() {
		return lastChatMsgContent;
	}
	
	public int getUnreadCount() {
		return unreadCount;
	}

	public static List<Chat> constructChats(Response res) throws HttpException {
		try {
			JSONArray list = res.asJSONArray();
			int size = list.length();
			List<Chat> infos = new ArrayList<Chat>(size);
			for (int i = 0; i < size; i++) {
				infos.add(new Chat(list.getJSONObject(i)));
			}
			return infos;
		} catch (JSONException jsone) {
			throw new HttpException(jsone);
		} catch (HttpException te) {
			throw te;
		}
	}

	/**
	 * 
	 * @param res
	 * @return
	 * @throws HttpException
	 */
//	public static UserWapper constructWapperUsers(Response res) throws HttpException {
//		JSONObject jsonUsers = res.asJSONObject(); // asJSONArray();
//		try {
//			JSONArray user = jsonUsers.getJSONArray("users");
//			int size = user.length();
//			List<User> users = new ArrayList<User>(size);
//			for (int i = 0; i < size; i++) {
//				users.add(new User(user.getJSONObject(i)));
//			}
//			long previousCursor = jsonUsers.getLong("previous_curosr");
//			long nextCursor = jsonUsers.getLong("next_cursor");
//			if (nextCursor == -1) { // 鍏煎涓嶅悓鏍囩鍚嶇О
//				nextCursor = jsonUsers.getLong("nextCursor");
//			}
//			return new UserWapper(users, previousCursor, nextCursor);
//		} catch (JSONException jsone) {
//			throw new HttpException(jsone);
//		}
//	}

	/**
	 * @param res
	 * @return
	 * @throws HttpException
	 */
	static List<Chat> constructResult(Response res) throws HttpException {
		JSONArray list = res.asJSONArray();
		try {
			int size = list.length();
			List<Chat> infos = new ArrayList<Chat>(size);
			for (int i = 0; i < size; i++) {
				infos.add(new Chat(list.getJSONObject(i)));
			}
			return infos;
		} catch (JSONException e) {
		}
		return null;
	}


	@Override
	public String toString() {
		return "Info{" + ", id=" + id + ", ownerId='" + ownerId
                       + ", otherId=" + otherId + ", otherName='" + otherName + ", otherImageUrl='" + otherImageUrl
                       + ", lastChatMsgId=" + lastChatMsgId +", lastChatMsgTime=" + lastChatMsgTime + ", lastChatMsgContent=" + lastChatMsgContent
                       + ", unreadCount=" + unreadCount +'}';
	}
	// TODO:增加从游标解析User的方法，用于和data里User转换一条数据
//	public static User parseUser(Cursor cursor) {
//		if (null == cursor || 0 == cursor.getCount() || cursor.getCount() > 1) {
//			Log.w("User.ParseUser", "Cann't parse Cursor, bacause cursor is null or empty.");
//		}
//		cursor.moveToFirst();
//		User u = new User();
//		u.id = cursor.getString(cursor.getColumnIndex(UserInfoTable._ID));
//		u.name = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_USER_NAME));
//		u.screenName = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_USER_SCREEN_NAME));
//		u.location = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_LOCALTION));
//		u.description = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_DESCRIPTION));
//		u.profileImageUrl = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_PROFILE_IMAGE_URL));
//		u.url = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_URL));
//		u.isProtected = (0 == cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_PROTECTED))) ? false : true;
//		u.followersCount = cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_FOLLOWERS_COUNT));
//		u.friendsCount = cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_FRIENDS_COUNT));
//		u.favouritesCount = cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_FAVORITES_COUNT));
//		u.statusesCount = cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_STATUSES_COUNT));
//		u.following = (0 == cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_FOLLOWING))) ? false : true;
//
//		try {
//			String createAtStr = cursor.getString(cursor.getColumnIndex(MessageTable.FIELD_CREATED_AT));
//			if (createAtStr != null) {
//				u.createdAt = TwitterDatabase.DB_DATE_FORMATTER.parse(createAtStr);
//			}
//
//		} catch (ParseException e) {
//			Log.w("User", "Invalid created at data.");
//		}
//		return u;
//	}

	public com.codeim.coxin.data.Chat parseInfo() {
		com.codeim.coxin.data.Chat info = new com.codeim.coxin.data.Chat();
	        info.id           = this.id;          
	        info.ownerId      = this.ownerId;     
	        info.otherId    = this.otherId;  
	        info.otherName   = this.otherName;
	        info.otherImageUrl       = this.otherImageUrl;
	        info.lastChatMsgId  = this.lastChatMsgId;
	                    
	        info.lastChatMsgTime     = this.lastChatMsgTime;    
	        info.lastChatMsgContent    = this.lastChatMsgContent;   
	        info.unreadCount     = this.unreadCount;
		return info;
	}
	
	
//	//the follow by ywwang. from WeiboResponse, move to DateTimeHelper
//	protected static Date parseDate(String str, String format)
//			throws HttpException {
//		//private static Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
//		Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
//		
//		if (str == null || "".equals(str)) {
//			return null;
//		}
//		SimpleDateFormat sdf = formatMap.get(format);
//		if (null == sdf) {
//			sdf = new SimpleDateFormat(format, Locale.US);
//			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//			formatMap.put(format, sdf);
//		}
//		try {
//			synchronized (sdf) {
//				// SimpleDateFormat is not thread safe
//				return sdf.parse(str);
//			}
//		} catch (ParseException pe) {
//			throw new HttpException("Unexpected format(" + str
//					+ ") returned from sina.com.cn");
//		}
//	}
	protected static String getString(String name, JSONObject json,
			boolean decode) {
		String returnValue = null;
		try {
			returnValue = json.getString(name);
			if (decode) {
				try {
					returnValue = URLDecoder.decode(returnValue, "UTF-8");
				} catch (UnsupportedEncodingException ignore) {
				}
			}
		} catch (JSONException ignore) {
			// refresh_url could be missing
		}
		return returnValue;
	}
	protected static String getString(String key, JSONObject json)
			throws JSONException {
		if (!json.has(key)) {
			return "";
		}
		String str = json.getString(key);
		if (null == str || "".equals(str) || "null".equals(str)) {
			return "";
		}
		return String.valueOf(str);
	}
	protected static int getInt(String key, JSONObject json)
			throws JSONException {
		String str = json.getString(key);
		if (null == str || "".equals(str) || "null".equals(str)) {
			return -1;
		}
		return Integer.parseInt(str);
	}
	
	protected static long getLong(String key, JSONObject json)
			throws JSONException {
		String str = json.getString(key);
		if (null == str || "".equals(str) || "null".equals(str)) {
			return -1;
		}
		return Long.parseLong(str);
	}
	protected static boolean getBoolean(String key, JSONObject json)
			throws JSONException {
		String str = json.getString(key);
		if (null == str || "".equals(str) || "null".equals(str)) {
			return false;
		}
		return Boolean.valueOf(str);
	}
}