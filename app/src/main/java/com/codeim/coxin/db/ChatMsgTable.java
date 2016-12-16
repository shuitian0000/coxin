package com.codeim.coxin.db;

import java.text.ParseException;

import com.codeim.coxin.data.ChatMsg;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

//import com.codeim.coxin.data.Dm;

/**
 * Table - Direct Messages
 * 
 */
public final class ChatMsgTable implements BaseColumns {

	public static final String TAG = "ChatMsgTable";

	public static final int TYPE_GET = 0;
	public static final int TYPE_SENT = 1;

	public static final String TABLE_NAME = "chatMsg";
	public static final int MAX_ROW_NUM = 20;

	public static final String FIELD_SEND_USER_ID = "send_user_id";
	public static final String FIELD_GET_USER_ID = "get_user_id";
	public static final String FIELD_SEND_OR_GET = "send_or_get"; //0: get; 1: send
	public static final String FIELD_MSG_TYPE = "msg_type"; //0: text; 1:pic; 2:audio; 3:video
	public static final String FIELD_CREATED_AT = "created_at";
	public static final String FIELD_CONTENT = "content";
	public static final String FIELD_LOCAL_CONTENT = "local_content";
	public static final String FIELD_GET_SERVER_ID = "get_server_id";
//	public static final String FIELD_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
//	public static final String FIELD_IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
//	public static final String FIELD_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
	public static final String FIELD_IS_UNREAD = "is_unread";
	public static final String FIELD_IS_SENT = "is_sent";

	public static final String[] TABLE_COLUMNS = new String[] { _ID,
		FIELD_SEND_USER_ID, FIELD_GET_USER_ID, FIELD_SEND_OR_GET,
		FIELD_MSG_TYPE,FIELD_CREATED_AT,
		FIELD_CONTENT,FIELD_LOCAL_CONTENT, FIELD_GET_SERVER_ID,
		FIELD_IS_UNREAD, FIELD_IS_SENT };

	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + _ID + " text primary key, "
			+ FIELD_SEND_USER_ID + " text not null, " + FIELD_GET_USER_ID + " text not null, " 
			+ FIELD_SEND_OR_GET + " text not null, "
			+ FIELD_MSG_TYPE + " text not null, "+ FIELD_CREATED_AT + " timestamp not null, "
			+ FIELD_CONTENT + " text, " + FIELD_LOCAL_CONTENT + " text, "
			+ FIELD_GET_SERVER_ID + " text unique on conflict replace, "
			+ FIELD_IS_UNREAD + " boolean not null, " + FIELD_IS_SENT + " boolean not null"
			+")";

	/**
	 * TODO: 将游标解析为一条私信
	 * 
	 * @param cursor
	 *            该方法不会关闭游标
	 * @return 成功返回Dm类型的单条数据, 失败返回null
	 */
	public static ChatMsg parseCursor(Cursor cursor) {

		if (null == cursor || 0 == cursor.getCount()) {
			Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
			return null;
		}

		ChatMsg chatMsg = new ChatMsg();

		chatMsg.id         = cursor.getString(cursor.getColumnIndex(ChatMsgTable._ID));
		int sendOrGet  = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_SEND_OR_GET));
		chatMsg.sendOrGet  = sendOrGet;
		if(sendOrGet==1) { //send
			chatMsg.masterId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_SEND_USER_ID));
			chatMsg.masterName = "";
			chatMsg.slaveId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_GET_USER_ID));
			chatMsg.slaveName = "";
		} else { //get
//			chatMsg.masterId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_GET_USER_ID));
			chatMsg.masterId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_SEND_USER_ID));
			chatMsg.masterName = "";
//			chatMsg.slaveId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_SEND_USER_ID));
			chatMsg.slaveId = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_GET_USER_ID));
			chatMsg.slaveName = "";
		}
		chatMsg.msgType = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_MSG_TYPE));
		if(sendOrGet==1) { //send //0: text; 1:pic; 2:audio; 3:video
			chatMsg.content  = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_LOCAL_CONTENT));
		} else {
			chatMsg.content  = cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_CONTENT));
		}
		try {
			chatMsg.chatMsgTime = TwitterDatabase.DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(ChatMsgTable.FIELD_CREATED_AT)));
		} catch (ParseException e) {
			Log.w(TAG, "Invalid created at data.");
		}
		chatMsg.status         = 0;
		chatMsg.isUnRead = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_IS_UNREAD));
		chatMsg.isSent   = cursor.getInt(cursor.getColumnIndex(ChatMsgTable.FIELD_IS_SENT));

		return chatMsg;
	}
}