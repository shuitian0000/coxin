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
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.util.Log;

import com.codeim.coxin.db.ChatMsgTable;
import com.codeim.coxin.db.TwitterDatabase;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;
import com.codeim.coxin.util.DateTimeHelper;

/**
 * A data class representing Basic info information element
 */
//public class Info extends WeiboResponse implements java.io.Serializable {
public class ChatMsg {
	
	private String id;
	
	private String masterId;
	private String masterName;
	private String slaveId;
	private String slaveName;

	private String content;
	private int sendOrGet;
	private int msgType; //0: text; 1:pic; 2:audio; 3:video
	private Date chatMsgTime;
	private int status;
	private int isUnRead;
	private int isSent;
	
	private static final long serialVersionUID = -6345893237975349030L;

	/* package */public ChatMsg(JSONObject json) throws HttpException {
		super();
		Log.d("ChatMsg", "json before init");
		init(json);
	}

	/* package */ChatMsg(Response res) throws HttpException {
		super();
		init(res.asJSONObject());
	}

	ChatMsg() {

	}

	private void init(JSONObject json) throws HttpException {
		try {
			id = json.getString("id");
			Log.d("chatMsg_id:", id);
			
			masterId = json.getString("masterId");
			
			masterName = json.getString("masterName");
			slaveId = json.getString("slaveId");
			slaveName = json.getString("slaveName");

			content   = json.getString("content");
			msgType   = json.getInt("msgType");
			chatMsgTime = DateTimeHelper.parseDate(json.getString("chatMsgTime"), "yyyy-MM-dd HH:mm:ss");
			status = json.getInt("status");
		} catch (JSONException jsone) {
			throw new HttpException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	public void setId(String id) {
		this.id=id;
	}

	/**
	 * Returns the id of the Info
	 *
	 * @return the id of the Info
	 */
	public String getId() {
		return id;
	}
	public String getMasterId() {
		return masterId;
	}
	
	public String getMasterName() {
		return masterName;
	}
	public String getSlaveId() {
		return slaveId;
	}
	public String getSlaveName() {
		return slaveName;
	}
//	public URL getOtherImageUrl() {
//		try {
//		    return new URL(TwitterApplication.mApi.getBaseURL()+otherImageUrl);
//	    } catch (MalformedURLException ex) {
//		    return null;
//	    }
//	}


	public String getContent() {
		return content;
	}
	public int getMsgType() {
		return msgType;
	}
	public Date getChatMsgTime() {
		return chatMsgTime;
	}
	public int getStatus() {
		return status;
	}

	public static List<ChatMsg> constructInfos(Response res) throws HttpException {
		try {
			JSONArray list = res.asJSONArray();
			int size = list.length();
			List<ChatMsg> infos = new ArrayList<ChatMsg>(size);
			for (int i = 0; i < size; i++) {
				infos.add(new ChatMsg(list.getJSONObject(i)));
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
	static List<ChatMsg> constructResult(Response res) throws HttpException {
		JSONArray list = res.asJSONArray();
		try {
			int size = list.length();
			List<ChatMsg> infos = new ArrayList<ChatMsg>(size);
			for (int i = 0; i < size; i++) {
				infos.add(new ChatMsg(list.getJSONObject(i)));
			}
			return infos;
		} catch (JSONException e) {
		}
		return null;
	}


	@Override
	public String toString() {
		return "Info{" + ", id=" + id + ", masterId='" + masterId
                       + ", masterName=" + masterName + ", slaveId='" + slaveId + ", slaveName='" + slaveName
                       +", content=" + content + ", msgType=" + String.valueOf(msgType)
                       +", chatMsgTime=" + chatMsgTime
                       + ", status=" + status +'}';
	}
	// TODO:增加从游标解析User的方法，用于和data里User转换一条数据
	public static ChatMsg parseChatMsg(Cursor cursor) {
		if (null == cursor || 0 == cursor.getCount() || cursor.getCount() > 1) {
			Log.w("User.parseChatMsg", "Cann't parse Cursor, bacause cursor is null or empty.");
		}
		cursor.moveToFirst();
		ChatMsg u = new ChatMsg();
		u.id = cursor.getString(cursor.getColumnIndex(ChatMsgTable._ID));
		u.masterId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_SEND_USER_ID));
		u.masterName = "";
		u.slaveId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_GET_USER_ID));
		u.slaveName = "";
		u.sendOrGet = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_SEND_OR_GET));
		if(u.sendOrGet==1) {
			u.content = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_LOCAL_CONTENT));
		} else {
			u.content = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_CONTENT));
		}
		u.msgType = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_MSG_TYPE));
		u.status = 0;
		u.isUnRead = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_IS_UNREAD));
		u.isSent = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_IS_SENT));

		try {
			String createAtStr = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_CREATED_AT));
			if (createAtStr != null) {
				u.chatMsgTime = TwitterDatabase.DB_DATE_FORMATTER.parse(createAtStr);
			}

		} catch (ParseException e) {
			Log.w("User", "Invalid created at data.");
		}
		return u;
	}
	// TODO:增加从游标解析User的方法，用于和data里User转换一条数据
	public static List<ChatMsg> parseChatMsgList(Cursor cursor) {
		List<ChatMsg> mChatMsgs = new ArrayList<ChatMsg>();
		
		if (null == cursor || 0 == cursor.getCount() || cursor.getCount() > 1) {
			Log.w("User.parseChatMsg", "Cann't parse Cursor, bacause cursor is null or empty.");
		}
		cursor.moveToFirst();
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
		    ChatMsg u = new ChatMsg();
		    u.id = cursor.getString(cursor.getColumnIndex(ChatMsgTable._ID));
		    u.masterId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_SEND_USER_ID));
		    u.masterName = "";
            u.slaveId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_GET_USER_ID));
		    u.slaveName = "";
		    u.sendOrGet = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_SEND_OR_GET));
		    if(u.sendOrGet==1) {
			    u.content = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_LOCAL_CONTENT));
		    } else {
			    u.content = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_CONTENT));
		    }
		    u.msgType = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_MSG_TYPE));
		    u.status = 0;
		    u.isUnRead = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_IS_UNREAD));
	    	u.isSent = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_IS_SENT));

		    try {
		    	String createAtStr = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_CREATED_AT));
			    if (createAtStr != null) {
			    	u.chatMsgTime = TwitterDatabase.DB_DATE_FORMATTER.parse(createAtStr);
			    }

		    } catch (ParseException e) {
			    Log.w("User", "Invalid created at data.");
		    }
		    mChatMsgs.add(u);
		}
		
		return mChatMsgs;
	}

	public com.codeim.coxin.data.ChatMsg parseInfo() {
		com.codeim.coxin.data.ChatMsg info = new com.codeim.coxin.data.ChatMsg();
	        info.id           = this.id;          
	        info.masterId      = this.masterId;     
	        info.masterName    = this.masterName;  
	        info.slaveId   = this.slaveId;
	        info.slaveName       = this.slaveName;
	        
	        info.content = this.content;
	        info.sendOrGet = 0;
	        info.msgType = this.msgType;
	        info.chatMsgTime     = this.chatMsgTime;    
	        info.status     = this.status;
	        info.isUnRead   = 1;
	        info.isSent     = 0;
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