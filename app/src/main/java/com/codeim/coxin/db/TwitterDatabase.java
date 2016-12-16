package com.codeim.coxin.db;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.Dm;
import com.codeim.coxin.data.Friend;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DebugTimer;
//import com.codeim.coxin.dao.StatusDAO;
//import com.codeim.coxin.fanfou.Status;

/**
 * A Database which contains all statuses and direct-messages, use
 * getInstane(Context) to get a new instance
 * 
 */
public class TwitterDatabase {

	private static final String TAG = "TwitterDatabase";

	private static final String DATABASE_NAME = "conxin.db";
	private static final int DATABASE_VERSION = 1;

	private static TwitterDatabase instance = null;
	private static DatabaseHelper mOpenHelper = null;
	private Context mContext = null;

	/**
	 * SQLiteOpenHelper
	 * 
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		// Construct
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		public DatabaseHelper(Context context, String name) {
			this(context, name, DATABASE_VERSION);
		}

		public DatabaseHelper(Context context) {
			this(context, DATABASE_NAME, DATABASE_VERSION);
		}

		public DatabaseHelper(Context context, int version) {
			this(context, DATABASE_NAME, null, version);
		}

		public DatabaseHelper(Context context, String name, int version) {
			this(context, name, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Create Database.");
			// 2011.03.01 add beta
			db.execSQL(UserInfoTable.CREATE_TABLE);
			// 2016.06.11 added
			db.execSQL(FriendTable.CREATE_TABLE);
			db.execSQL(ChatMsgTable.CREATE_TABLE);
		}

		@Override
		public synchronized void close() {
			Log.d(TAG, "Close Database.");
			super.close();
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			Log.d(TAG, "Open Database.");
			super.onOpen(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Upgrade Database.");
			dropAllTables(db);
		}

		private void dropAllTables(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + UserInfoTable.TABLE_NAME); // 2011.03.01 add
			// 2016.06.11 added
			db.execSQL("DROP TABLE IF EXISTS " + FriendTable.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + ChatMsgTable.TABLE_NAME);
		}
	}

	private TwitterDatabase(Context context) {
		mContext = context;
		mOpenHelper = new DatabaseHelper(context);
	}
	public static synchronized TwitterDatabase getInstance(Context context) {
		if (null == instance) {
		    Log.d(TAG, "TwitterDatabase Instanse");
			return new TwitterDatabase(context);
		}
		return instance;
	}
	// 测试用
	public SQLiteOpenHelper getSQLiteOpenHelper() {
		return mOpenHelper;
	}
	public static SQLiteDatabase getDb(boolean writeable) {
		if (writeable) {
			return mOpenHelper.getWritableDatabase();
		} else {
			return mOpenHelper.getReadableDatabase();
		}
	}
	public void close() {
		if (null != instance) {
			mOpenHelper.close();
			instance = null;
		}
	}

	/**
	 * 清空所有表中数据, 谨慎使用
	 * 
	 */
	public void clearData() {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM " + UserInfoTable.TABLE_NAME); // 2011.03.01 add
		// 2016.06.11 added
		db.execSQL("DELETE FROM " + FriendTable.TABLE_NAME);
		db.execSQL("DELETE FROM " + ChatMsgTable.TABLE_NAME);
	}

	/**
	 * 直接删除数据库文件, 调试用
	 * 
	 * @return true if this file was deleted, false otherwise.
	 * @deprecated
	 */
	private boolean deleteDatabase() {
		File dbFile = mContext.getDatabasePath(DATABASE_NAME);
		return dbFile.delete();
	}

	public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

	private static final int CONFLICT_REPLACE = 0x00000005;

	
	//------------ UserInfoTable operation
	// 2011.03.01 add
	// UserInfo操作
	public Cursor getAllUserInfo() {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();
		return mDb.query(UserInfoTable.TABLE_NAME, UserInfoTable.TABLE_COLUMNS, null, null, null, null, null);
	}
	/**
	 * 根据id列表获取user数据
	 * 
	 * @param userIds
	 * @return
	 */
	public Cursor getUserInfoByIds(String[] userIds) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();
		String userIdStr = "";
		for (String id : userIds) {
			userIdStr += "'" + id + "',";
		}
		if (userIds.length == 0) {
			userIdStr = "'',";
		}
		userIdStr = userIdStr.substring(0, userIdStr.lastIndexOf(","));// 删除最后的逗号
		return mDb.query(UserInfoTable.TABLE_NAME, UserInfoTable.TABLE_COLUMNS,
				UserInfoTable._ID + " in (" + userIdStr + ")", null, null,
				null, null);

	}

	/**
	 * 新建用户
	 * 
	 * @param user
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long createUserInfo(com.codeim.coxin.data.User user) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(UserInfoTable._ID, user.id);
		initialValues.put(UserInfoTable.FIELD_USER_NAME, user.name);
//		initialValues.put(UserInfoTable.FIELD_USER_SCREEN_NAME, user.screenName);
//		initialValues.put(UserInfoTable.FIELD_LOCALTION, user.location);
//		initialValues.put(UserInfoTable.FIELD_DESCRIPTION, user.description);
		initialValues.put(UserInfoTable.FIELD_PROFILE_IMAGE_URL, user.profileImageUrl);
//		initialValues.put(UserInfoTable.FIELD_URL, user.url);
//		initialValues.put(UserInfoTable.FIELD_PROTECTED, user.isProtected);
//		initialValues.put(UserInfoTable.FIELD_FOLLOWERS_COUNT, user.followersCount);
//		initialValues.put(UserInfoTable.FIELD_LAST_STATUS, user.lastStatus);
//		initialValues.put(UserInfoTable.FIELD_FRIENDS_COUNT, user.friendsCount);
//		initialValues.put(UserInfoTable.FIELD_FAVORITES_COUNT, user.favoritesCount);
//		initialValues.put(UserInfoTable.FIELD_STATUSES_COUNT, user.statusesCount);
//		initialValues.put(UserInfoTable.FIELD_FOLLOWING, user.isFollowing);

		// long rowId = mDb.insertWithOnConflict(UserInfoTable.TABLE_NAME, null,
		// initialValues,SQLiteDatabase.CONFLICT_REPLACE);
		long rowId = insertWithOnConflict(mDb, UserInfoTable.TABLE_NAME, null, initialValues, CONFLICT_REPLACE);
		if (-1 == rowId) {
			Log.e(TAG, "Cann't create user : " + user.id);
		} else {
			Log.v(TAG, "create create user : " + user.id);
		}
		return rowId;
	}

	// SQLiteDatabase.insertWithConflict是LEVEL 8(2.2)才引入的新方法
	// 为了兼容旧版，这里给出一个简化的兼容实现
	// 要注意的是这个实现和标准的函数行为并不完全一致
	private long insertWithOnConflict(SQLiteDatabase db, String tableName,
			String nullColumnHack, ContentValues initialValues,
			int conflictReplace) {

		long rowId = db.insert(tableName, nullColumnHack, initialValues);
		if (-1 == rowId) {
			// 尝试update
			rowId = db.update(tableName, initialValues, UserInfoTable._ID + "="
					+ initialValues.getAsString(UserInfoTable._ID), null);
		}
		return rowId;
	}

	public long createWeiboUserInfo(com.codeim.coxin.fanfou.User user) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
		ContentValues args = new ContentValues();

		args.put(UserInfoTable._ID, user.getId());

		args.put(UserInfoTable.FIELD_USER_NAME, user.getName());

		args.put(UserInfoTable.FIELD_USER_SCREEN_NAME, user.getScreenName());

		String location = user.getLocation();
		args.put(UserInfoTable.FIELD_LOCALTION, location);

		String description = user.getDescription();
		args.put(UserInfoTable.FIELD_DESCRIPTION, description);

		args.put(UserInfoTable.FIELD_PROFILE_IMAGE_URL, user.getProfileImageURL().toString());

		if (user.getURL() != null) {
			args.put(UserInfoTable.FIELD_URL, user.getURL().toString());
		}

		args.put(UserInfoTable.FIELD_PROTECTED, user.isProtected());

		args.put(UserInfoTable.FIELD_FOLLOWERS_COUNT, user.getFollowersCount());

		args.put(UserInfoTable.FIELD_LAST_STATUS, user.getStatusSource());

		args.put(UserInfoTable.FIELD_FRIENDS_COUNT, user.getFriendsCount());

		args.put(UserInfoTable.FIELD_FAVORITES_COUNT, user.getFavouritesCount());

		args.put(UserInfoTable.FIELD_STATUSES_COUNT, user.getStatusesCount());

		args.put(UserInfoTable.FIELD_FOLLOWING, user.isFollowing());

		// long rowId = mDb.insert(UserInfoTable.TABLE_NAME, null, args);

		// 省去判断existUser，如果存在数据则replace
		// long rowId=mDb.insertWithOnConflict(UserInfoTable.TABLE_NAME, null,
		// args, SQLiteDatabase.CONFLICT_REPLACE);
		long rowId = insertWithOnConflict(mDb, UserInfoTable.TABLE_NAME, null, args, CONFLICT_REPLACE);

		if (-1 == rowId) {
			Log.e(TAG, "Cann't createWeiboUserInfo : " + user.getId());
		} else {
			Log.v(TAG, "create createWeiboUserInfo : " + user.getId());
		}
		return rowId;
	}

	/**
	 * 查看数据是否为已保存用户数据
	 * 
	 * @param userId
	 * @return
	 */
	public boolean existsUser(String userId) {
		SQLiteDatabase Db = mOpenHelper.getReadableDatabase();
		boolean result = false;

		Cursor cursor = Db.query(UserInfoTable.TABLE_NAME,
				new String[] { UserInfoTable._ID }, UserInfoTable._ID + "='"
						+ userId + "'", null, null, null, null);
		Log.v("testesetesteste", String.valueOf(cursor.getCount()));
		if (cursor != null && cursor.getCount() > 0) {
			result = true;
		}

		cursor.close();
		return result;
	}

	/**
	 * 根据userid提取信息
	 * 
	 * @param userId
	 * @return
	 */
	public Cursor getUserInfoById(String userId) {
		SQLiteDatabase Db = mOpenHelper.getReadableDatabase();

		Cursor cursor = Db.query(UserInfoTable.TABLE_NAME, UserInfoTable.TABLE_COLUMNS, UserInfoTable._ID + " = '"
						+ userId + "'", null, null, null, null);

		return cursor;
	}

	/**
	 * 更新用户
	 * 
	 * @param uid
	 * @param args
	 * @return
	 */
	public boolean updateUser(String uid, ContentValues args) {
		SQLiteDatabase Db = mOpenHelper.getWritableDatabase();
		return Db.update(UserInfoTable.TABLE_NAME, args, UserInfoTable._ID + "='" + uid + "'", null) > 0;
	}

	/**
	 * 更新用户信息
	 */
	public boolean updateUser(com.codeim.coxin.data.User user) {

		SQLiteDatabase Db = mOpenHelper.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(UserInfoTable._ID, user.id);
		args.put(UserInfoTable.FIELD_USER_NAME, user.name);
//		args.put(UserInfoTable.FIELD_USER_SCREEN_NAME, user.screenName);
//		args.put(UserInfoTable.FIELD_LOCALTION, user.location);
//		args.put(UserInfoTable.FIELD_DESCRIPTION, user.description);
		args.put(UserInfoTable.FIELD_PROFILE_IMAGE_URL, user.profileImageUrl);
//		args.put(UserInfoTable.FIELD_URL, user.url);
//		args.put(UserInfoTable.FIELD_PROTECTED, user.isProtected);
//		args.put(UserInfoTable.FIELD_FOLLOWERS_COUNT, user.followersCount);
//		args.put(UserInfoTable.FIELD_LAST_STATUS, user.lastStatus);
//		args.put(UserInfoTable.FIELD_FRIENDS_COUNT, user.friendsCount);
//		args.put(UserInfoTable.FIELD_FAVORITES_COUNT, user.favoritesCount);
//		args.put(UserInfoTable.FIELD_STATUSES_COUNT, user.statusesCount);
//		args.put(UserInfoTable.FIELD_FOLLOWING, user.isFollowing);

		return Db.update(UserInfoTable.TABLE_NAME, args, UserInfoTable._ID + "='" + user.id + "'", null) > 0;
	}

	/**
	 * 减少转换的开销
	 * 
	 * @param user
	 * @return
	 */
	public boolean updateWeiboUser(com.codeim.coxin.fanfou.User user) {

		SQLiteDatabase Db = mOpenHelper.getWritableDatabase();
		ContentValues args = new ContentValues();

		args.put(UserInfoTable._ID, user.getName());

		args.put(UserInfoTable.FIELD_USER_NAME, user.getName());

		args.put(UserInfoTable.FIELD_USER_SCREEN_NAME, user.getScreenName());

		String location = user.getLocation();
		args.put(UserInfoTable.FIELD_LOCALTION, location);

		String description = user.getDescription();
		args.put(UserInfoTable.FIELD_DESCRIPTION, description);

		args.put(UserInfoTable.FIELD_PROFILE_IMAGE_URL, user.getProfileImageURL().toString());

		if (user.getURL() != null) {
			args.put(UserInfoTable.FIELD_URL, user.getURL().toString());
		}

		args.put(UserInfoTable.FIELD_PROTECTED, user.isProtected());

		args.put(UserInfoTable.FIELD_FOLLOWERS_COUNT, user.getFollowersCount());

		args.put(UserInfoTable.FIELD_LAST_STATUS, user.getStatusSource());

		args.put(UserInfoTable.FIELD_FRIENDS_COUNT, user.getFriendsCount());

		args.put(UserInfoTable.FIELD_FAVORITES_COUNT, user.getFavouritesCount());

		args.put(UserInfoTable.FIELD_STATUSES_COUNT, user.getStatusesCount());

		args.put(UserInfoTable.FIELD_FOLLOWING, user.isFollowing());

		return Db.update(UserInfoTable.TABLE_NAME, args, UserInfoTable._ID + "='" + user.getId() + "'", null) > 0;

	}

	/**
	 * 同步用户,更新已存在的用户,插入未存在的用户
	 */
	public void syncUsers(List<com.codeim.coxin.data.User> users) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
		try {
			mDb.beginTransaction();
			for (com.codeim.coxin.data.User u : users) {
				// if(existsUser(u.id)){
				// updateUser(u);
				// }else{
				// createUserInfo(u);
				// }
				createUserInfo(u);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}

	}

	public void syncWeiboUsers(List<com.codeim.coxin.fanfou.User> users) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		try {
			mDb.beginTransaction();
			for (com.codeim.coxin.fanfou.User u : users) {
				// if (existsUser(u.getId())) {
				// updateWeiboUser(u);
				// } else {
				// createWeiboUserInfo(u);
				// }
				createWeiboUserInfo(u);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}
	
	//********************************** Friends operation
	//* 2011.03.01 add
	//* UserInfoTable & FriendTable 操作
	//*******************************************************
	public int getLocalLastFriendId(String string) {
		Log.v(TAG, "getLocalLastFriendId : " + string);
		
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		Cursor cursor = mDb.rawQuery("SELECT max("+FriendTable.FIELD_SERVER_ID+") FROM "+FriendTable.TABLE_NAME+" WHERE "
        +FriendTable.FIELD_OWNER_ID+" = "+string,
//        new String[]{});
        null);
		
		if(cursor.moveToNext())
		{
		  int id = cursor.getInt(cursor.getColumnIndex("max("+FriendTable.FIELD_SERVER_ID+")"));
		  return id;
		  // 这个id就是最大值
		} else {
			return -1;
		}

	}
	
	/**
	 * 获取当前用户的好友信息
	 * 这个函数参考的数据是本地sqlite数据库，另外要实现与后端服务器数据库的同步时机
	 */
	public Cursor getContactById(int page_size, int page_index,int last_id,String user_id) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

//		return mDb.rawQuery("SELECT * FROM "+UserInfoTable.TABLE_NAME+" INNER JONE "+FriendTable.TABLE_NAME
//				            +" ON "+UserInfoTable.TABLE_NAME+"._ID="+FriendTable.TABLE_NAME+".ownerId", 
//				            new String[]{});
		return mDb.rawQuery("SELECT * FROM "+FriendTable.TABLE_NAME+" WHERE "
	            +FriendTable.FIELD_SERVER_ID+">"+String.valueOf(last_id)+" limit "+String.valueOf(page_size),
//	            new String[]{});
	            null);
//		return mDb.query(FriendTable.TABLE_NAME, FriendTable.TABLE_COLUMNS, null, null, null, null, null);

	}
	
	/**
	 * 新建用户
	 * 
	 * @param friend
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long createFriendInfo(com.codeim.coxin.data.Friend friend) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(FriendTable._ID, friend.id);
		initialValues.put(FriendTable.FIELD_SERVER_ID, friend.id);
		initialValues.put(FriendTable.FIELD_OWNER_ID, friend.ownerId);
		initialValues.put(FriendTable.FIELD_OTHER_ID, friend.otherId);
		initialValues.put(FriendTable.FIELD_OTHER_NAME, friend.otherName);
		initialValues.put(FriendTable.FIELD_OTHER_IMAGE_URL, friend.otherImageUrl);
		initialValues.put(FriendTable.FIELD_CREATE_TIME, 
				DateTimeHelper.dateToString(friend.create_time, "yyyy-MM-dd HH:mm:ss"));
		initialValues.put(FriendTable.FIELD_IS_DELETE, friend.is_delete);

		// long rowId = mDb.insertWithOnConflict(UserInfoTable.TABLE_NAME, null,
		// initialValues,SQLiteDatabase.CONFLICT_REPLACE);
		long rowId = insertWithOnConflict(mDb, FriendTable.TABLE_NAME, null, initialValues, CONFLICT_REPLACE);
		if (-1 == rowId) {
			Log.e(TAG, "Cann't create user : " + friend.id);
		} else {
			Log.v(TAG, "create create user : " + friend.id);
		}
		return rowId;
	}
	
	/**
	 * 同步当前用户的联系人,更新已存在的联系人信息,插入未存在的联系人信息
	 * 入口是获取到的Friend信息
	 */
	public void syncContacts(List<com.codeim.coxin.data.Friend> friends) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
		try {
			mDb.beginTransaction();
			for (com.codeim.coxin.data.Friend u : friends) {
				// if(existsUser(u.id)){
				// updateUser(u);
				// }else{
				// createUserInfo(u);
				// }
				createFriendInfo(u);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}

	}
	public void deleteFriend(String deleteId) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		mDb.delete(FriendTable.TABLE_NAME, FriendTable.FIELD_SERVER_ID + " = " + deleteId, null);
	}
	public boolean isFriend(String ownerId, String otherId) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		Cursor cursor = mDb.rawQuery("SELECT * FROM "+FriendTable.TABLE_NAME+ " WHERE "
		        +FriendTable.FIELD_OWNER_ID+" = "+ownerId+" AND "+FriendTable.FIELD_OTHER_ID+" = "+otherId
		        +" AND "+FriendTable.FIELD_IS_DELETE+" =0", null);
		
		if(cursor!=null && cursor.moveToNext()) {
			return true;
		} else {
			return false;
		}
	}
	public void createFriend(Friend friend) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		Cursor cursor = mDb.rawQuery("SELECT * FROM "+FriendTable.TABLE_NAME+ " WHERE "
		        +FriendTable.FIELD_OWNER_ID+" = "+friend.ownerId+" AND "+FriendTable.FIELD_OTHER_ID+" = "+friend.otherId
		        , null);
		        //+"AND "+FriendTable.FIELD_IS_DELETE+" =0", null);
		if(cursor!=null && cursor.moveToNext()) {
			String id = cursor.getString(cursor.getColumnIndex(FriendTable._ID));
			Cursor cursor_my = mDb.rawQuery("UPDATE "+FriendTable.TABLE_NAME+ " SET "
					+FriendTable.FIELD_IS_DELETE+" =0 WHERE "+FriendTable._ID+" = "+id, null);
		} else {
			createFriendInfo(friend);
		}
	}
	//******************************************************
	//* 2016.07.05 add
	//* ChatMsgTable 操作
	//*******************************************************
	/**
	 * 新建会话，可以是从服务器获得的，也可以是本地建立的
	 * 
	 * @param chatMsg
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long createChatMsg(com.codeim.coxin.data.ChatMsg chatMsg, boolean fromLocal) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(ChatMsgTable._ID, chatMsg.id);
		initialValues.put(ChatMsgTable.FIELD_SEND_USER_ID, chatMsg.masterId);
		initialValues.put(ChatMsgTable.FIELD_GET_USER_ID, chatMsg.slaveId);
		initialValues.put(ChatMsgTable.FIELD_SEND_OR_GET, fromLocal);//0: get; 1: send
		initialValues.put(ChatMsgTable.FIELD_SEND_OR_GET, chatMsg.msgType);//0: text; 1:pic; 2:audio; 3:video
		initialValues.put(ChatMsgTable.FIELD_CREATED_AT, 
				DateTimeHelper.dateToString(chatMsg.chatMsgTime, "yyyy-MM-dd HH:mm:ss"));
		initialValues.put(ChatMsgTable.FIELD_CONTENT, chatMsg.content);
		initialValues.put(ChatMsgTable.FIELD_LOCAL_CONTENT, chatMsg.content);
		initialValues.put(ChatMsgTable.FIELD_GET_SERVER_ID, chatMsg.id);
		initialValues.put(ChatMsgTable.FIELD_IS_UNREAD, fromLocal);
		initialValues.put(ChatMsgTable.FIELD_IS_SENT, "0");

		long rowId = insertWithOnConflict(mDb, ChatMsgTable.TABLE_NAME, null, initialValues, CONFLICT_REPLACE);
		if (-1 == rowId) {
			Log.e(TAG, "Cann't create chatMsg : " + chatMsg.id);
		} else {
			Log.v(TAG, "create create chatMsg : " + chatMsg.id);
		}
		return rowId;
	}
	/**
	 * 同步当前用户会话,更新已存在的会话信息,插入未存在的会话
	 * 入口是从服务器取得的会话信息
	 */
	public void syncChatMsgs(List<com.codeim.coxin.data.ChatMsg> chatMsgs) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
		try {
			mDb.beginTransaction();
			for (com.codeim.coxin.data.ChatMsg u : chatMsgs) {
				// if(existsUser(u.id)){
				// updateUser(u);
				// }else{
				// createUserInfo(u);
				// }
				createChatMsg(u, false);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}
	public void setIsReadById(String id) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
		
		Cursor cursor = mDb.rawQuery("UPDATE "+ChatMsgTable.TABLE_NAME
				+" SET "+ChatMsgTable.FIELD_IS_UNREAD+"=0 "
		        +" WHERE "+ChatMsgTable.FIELD_GET_SERVER_ID+" = "+id, null);
	}
	public void setIsReadByTime(Date last_time) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
		
		Cursor cursor = mDb.rawQuery("UPDATE "+ChatMsgTable.TABLE_NAME
				+" SET "+ChatMsgTable.FIELD_IS_UNREAD+"=0 "
		        +" WHERE "+ChatMsgTable.FIELD_CREATED_AT+" < "+last_time.getTime(), null);
	}
	public int getCountOfUnread(String userId) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
		
		Cursor cursor = mDb.rawQuery("SELECT * FROM "+ChatMsgTable.TABLE_NAME
		        +" WHERE "+ChatMsgTable.FIELD_SEND_OR_GET+" =0 "+" AND "
				+ChatMsgTable.FIELD_GET_USER_ID+" = "+userId+" AND "
				+ChatMsgTable.FIELD_IS_UNREAD+" =1 ", null);
		if(cursor!=null && cursor.moveToNext()) {
			return cursor.getCount();
		} else {
			return 0;
		}
	}
	/**
	 * replace id using serveId
	 */
	public void setIsSent(String id, String serverId) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
		
		Cursor cursor = mDb.rawQuery("UPDATE "+ChatMsgTable.TABLE_NAME
				+" SET "+ChatMsgTable.FIELD_GET_SERVER_ID+" = "+serverId
		        +" WHERE "+ChatMsgTable.FIELD_GET_SERVER_ID+" = "+id, null);
	}
	public void deleteChatMsgById(String deleteId) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		mDb.delete(ChatMsgTable.TABLE_NAME, ChatMsgTable.FIELD_GET_SERVER_ID + " = " + deleteId, null);
	}
	public Cursor getChatMsgsByTime(int page_size, int page_index,Date last_time,String user_id) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		return mDb.rawQuery("SELECT * FROM "+FriendTable.TABLE_NAME+" WHERE "
	            +FriendTable.FIELD_CREATE_TIME+">"+last_time.getTime()+" limit "+String.valueOf(page_size),
//	            new String[]{});
	            null);
//		return mDb.query(FriendTable.TABLE_NAME, FriendTable.TABLE_COLUMNS, null, null, null, null, null);

	}

	//no last "-1" information, ???????
	public Cursor getChatMsgByIdFromLocal(int page_size, int page_index,int last_id,
										  String slaveId,String masterId) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		return mDb.rawQuery("SELECT * FROM "+ChatMsgTable.TABLE_NAME+" WHERE "
						+ChatMsgTable._ID+">"+String.valueOf(last_id)+" AND "
				        +ChatMsgTable.FIELD_GET_USER_ID+" = "+masterId+" OR "
				        +ChatMsgTable.FIELD_SEND_USER_ID+" = "+masterId+
				"limit "+String.valueOf(page_size),
//	            new String[]{});
				null);
//		return mDb.query(FriendTable.TABLE_NAME, FriendTable.TABLE_COLUMNS, null, null, null, null, null);

	}
}
