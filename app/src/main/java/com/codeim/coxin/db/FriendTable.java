package com.codeim.coxin.db;

import java.text.ParseException;

import com.codeim.coxin.data.DbFriend;
import com.codeim.coxin.data.Dm;
import com.codeim.coxin.data.Friend;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

//import com.codeim.coxin.data.Dm;

/**
 * Table - Direct Messages
 * 
 */
public final class FriendTable implements BaseColumns {

	public static final String TAG = "FriendTable";

	public static final int TYPE_GET = 0;
	public static final int TYPE_SENT = 1;

	public static final String TABLE_NAME = "friend";
	public static final int MAX_ROW_NUM = 20;

	public static final String FIELD_SERVER_ID = "server_id";
	public static final String FIELD_OWNER_ID = "owner_id";
	public static final String FIELD_OTHER_ID = "otherId";
	public static final String FIELD_OTHER_NAME = "otherName";
	public static final String FIELD_OTHER_IMAGE_URL = "other_image_url";
	public static final String FIELD_CREATE_TIME = "create_time";
	public static final String FIELD_IS_DELETE = "is_delete";
//	public static final String FIELD_DELETE_ID = "delete_id";
//	public static final String FIELD_DELETE_TIME = "delete_time";
	
//	public static final String FIELD_TEXT = "text";
//	public static final String FIELD_IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
//	public static final String FIELD_IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
//	public static final String FIELD_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
//	public static final String FIELD_IS_UNREAD = "is_unread";
//	public static final String FIELD_IS_SENT = "is_send";

	public static final String[] TABLE_COLUMNS = new String[] { _ID,
		FIELD_SERVER_ID,
		FIELD_OWNER_ID,FIELD_OTHER_ID,
		FIELD_OTHER_NAME,FIELD_OTHER_IMAGE_URL,
		FIELD_CREATE_TIME,FIELD_IS_DELETE
//		,FIELD_DELETE_ID,FIELD_DELETE_TIME 
		};

	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + _ID + " text primary key on conflict replace, "
			+ FIELD_SERVER_ID + " text not null, "
			+ FIELD_OWNER_ID + " text not null, " + FIELD_OTHER_ID + " text not null, " 
			+ FIELD_OTHER_NAME + " text not null, "+ FIELD_OTHER_IMAGE_URL + " text not null, "
			+ FIELD_CREATE_TIME + " date not null, " + FIELD_IS_DELETE + " text"
//			+", " + FIELD_DELETE_ID + " text, " + FIELD_DELETE_TIME + " date" 
			+")";

	/**
	 * TODO: 将游标解析为一条私信
	 * 
	 * @param cursor
	 *            该方法不会关闭游标
	 * @return 成功返回Dm类型的单条数据, 失败返回null
	 */
	public static Friend parseCursor(Cursor cursor) {

		if (null == cursor || 0 == cursor.getCount()) {
			Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
			return null;
		}

		Friend dm = new Friend();

		dm.id = cursor.getString(cursor.getColumnIndex(FriendTable.FIELD_SERVER_ID));
		dm.ownerId = cursor.getString(cursor.getColumnIndex(FriendTable.FIELD_OWNER_ID));
		dm.otherId = cursor.getString(cursor.getColumnIndex(FriendTable.FIELD_OTHER_ID));
		try {
			dm.create_time = TwitterDatabase.DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(FriendTable.FIELD_CREATE_TIME)));
		} catch (ParseException e) {
			Log.w(TAG, "Invalid created at data.");
		}
		dm.is_delete = (1==cursor.getInt(cursor.getColumnIndex(FriendTable.FIELD_IS_DELETE)) ? 1: 0);
//		dm.delete_id = cursor.getString(cursor.getColumnIndex(FriendTable.FIELD_DELETE_ID));
//		try {
//			dm.delete_time = TwitterDatabase.DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(FriendTable.FIELD_DELETE_TIME)));
//		} catch (ParseException e) {
//			Log.w(TAG, "Invalid delete at data.");
//		}

		return dm;
	}
	
}