package com.codeim.coxin.db;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.util.DebugTimer;
//import com.codeim.coxin.dao.StatusDAO;
//import com.codeim.coxin.fanfou.Status;

/**
 * A Database which contains all statuses and direct-messages, use
 * getInstane(Context) to get a new instance
 * 
 */
public class CoxinDatabase {

	private static final String TAG = "CoxinDatabase";

	private static final String DATABASE_NAME = "conxin_db";
	private static final int DATABASE_VERSION = 1;

	private static CoxinDatabase instance = null;
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
			
//			db.execSQL(StatusTable.CREATE_TABLE);
//			db.execSQL(MessageTable.CREATE_TABLE);
//			db.execSQL(FollowTable.CREATE_TABLE);

			// 2011.03.01 add beta
			db.execSQL(UserInfoTable.CREATE_TABLE);
			
			// 2016.06.11 added
			db.execSQL(FriendTable.CREATE_TABLE);
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
//			db.execSQL("DROP TABLE IF EXISTS " + StatusTable.TABLE_NAME);
//			db.execSQL("DROP TABLE IF EXISTS " + MessageTable.TABLE_NAME);
//			db.execSQL("DROP TABLE IF EXISTS " + FollowTable.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + UserInfoTable.TABLE_NAME); // 2011.03.01 add
			// 2016.06.11 added
			db.execSQL("DROP TABLE IF EXISTS " + FriendTable.TABLE_NAME);
		}
	}

	private CoxinDatabase(Context context) {
		mContext = context;
		mOpenHelper = new DatabaseHelper(context);
	}

	public static synchronized CoxinDatabase getInstance(Context context) {
		if (null == instance) {
		    Log.d(TAG, "CoxinDatabase Instanse");
			return new CoxinDatabase(context);
		}
		return instance;
	}

	// used for test
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
	 * clear all data in table, be careful
	 * 
	 */
	public void clearData() {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

//		db.execSQL("DELETE FROM " + StatusTable.TABLE_NAME);
//		db.execSQL("DELETE FROM " + MessageTable.TABLE_NAME);
//		db.execSQL("DELETE FROM " + FollowTable.TABLE_NAME);
		
		db.execSQL("DELETE FROM " + UserInfoTable.TABLE_NAME); // 2011.03.01 add
		// 2016.06.11 added
		db.execSQL("DELETE FROM " + FriendTable.TABLE_NAME);
	}

	/**
	 * ֱdelete database file, just for debug
	 * 
	 * @return true if this file was deleted, false otherwise.
	 * @deprecated
	 */
	private boolean deleteDatabase() {
		File dbFile = mContext.getDatabasePath(DATABASE_NAME);
		return dbFile.delete();
	}

	/**
	 * get one message of one type
	 * 
	 * @param tweetId
	 * @param type
	 *            of status <li>StatusTable.TYPE_HOME</li> <li>
	 *            StatusTable.TYPE_MENTION</li> <li>StatusTable.TYPE_USER</li>
	 *            <li>StatusTable.TYPE_FAVORITE</li> <li>-1 means all types</li>
	 * @return  Tweet object from Cursor
	 * @deprecated use StatusDAO#findStatus()
	 */
	public Tweet queryTweet(String tweetId, int type) {
		SQLiteDatabase Db = mOpenHelper.getWritableDatabase();

		String selection = StatusTable._ID + "=? ";
		if (-1 != type) {
			selection += " AND " + StatusTable.STATUS_TYPE + "=" + type;
		}

		Cursor cursor = Db.query(StatusTable.TABLE_NAME, StatusTable.TABLE_COLUMNS, selection, new String[] { tweetId }, null, null, null);

		Tweet tweet = null;

		if (cursor != null) {
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				tweet = StatusTable.parseCursor(cursor);
			}
		}

		cursor.close();
		return tweet;
	}

	/**
	 *  check whether one message(specified type) exist, quickly.
	 * 
	 * @param tweetId
	 * @param type
	 *            <li>StatusTable.TYPE_HOME</li> <li>StatusTable.TYPE_MENTION</li>
	 *            <li>StatusTable.TYPE_USER</li> <li>StatusTable.TYPE_FAVORITE</li>
	 * @return is exists
	 * @deprecated use StatusDAO#isExists()
	 */
	public boolean isExists(String tweetId, String owner, int type) {
		SQLiteDatabase Db = mOpenHelper.getWritableDatabase();
		boolean result = false;

		Cursor cursor = Db.query(StatusTable.TABLE_NAME,
				new String[] { StatusTable._ID }, StatusTable._ID + " =? AND "
						+ StatusTable.OWNER_ID + "=? AND "
						+ StatusTable.STATUS_TYPE + " = " + type, new String[] {
						tweetId, owner }, null, null, null);

		if (cursor != null && cursor.getCount() > 0) {
			result = true;
		}

		cursor.close();
		return result;
	}

	/**
	 * delete one message
	 * 
	 * @param tweetId
	 * @param type
	 *            -1 means all types
	 * @return the number of rows affected if a whereClause is passed in, 0
	 *         otherwise. To remove all rows and get a count pass "1" as the
	 *         whereClause.
	 * @deprecated use {@link StatusDAO#deleteStatus(String, String, int)}
	 */
	public int deleteTweet(String tweetId, String owner, int type) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String where = StatusTable._ID + " =? ";
		if (!TextUtils.isEmpty(owner)) {
			where += " AND " + StatusTable.OWNER_ID + " = '" + owner + "' ";
		}
		if (-1 != type) {
			where += " AND " + StatusTable.STATUS_TYPE + " = " + type;
		}

		return db.delete(StatusTable.TABLE_NAME, where, new String[] { tweetId });
	}

	/**
	 * delete the garbage which external MAX_ROW_NUM
	 * 
	 * @param type
	 *            <li>StatusTable.TYPE_HOME</li> <li>StatusTable.TYPE_MENTION</li>
	 *            <li>StatusTable.TYPE_USER</li> <li>StatusTable.TYPE_FAVORITE</li>
	 *            <li>-1 means all types</li>
	 */
	public void gc(String owner, int type) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		String sql = "DELETE FROM " + StatusTable.TABLE_NAME + " WHERE "
				+ StatusTable._ID + " NOT IN " + " (SELECT " + StatusTable._ID // �Ӿ�
				+ " FROM " + StatusTable.TABLE_NAME;
		boolean first = true;
		if (!TextUtils.isEmpty(owner)) {
			sql += " WHERE " + StatusTable.OWNER_ID + " = '" + owner + "' ";
			first = false;
		}
		if (type != -1) {
			if (first) {
				sql += " WHERE ";
			} else {
				sql += " AND ";
			}
			sql += StatusTable.STATUS_TYPE + " = " + type + " ";
		}
		sql += " ORDER BY " + StatusTable.CREATED_AT + " DESC LIMIT " + StatusTable.MAX_ROW_NUM + ")";

		if (!TextUtils.isEmpty(owner)) {
			sql += " AND " + StatusTable.OWNER_ID + " = '" + owner + "' ";
		}
		if (type != -1) {
			sql += " AND " + StatusTable.STATUS_TYPE + " = " + type + " ";
		}

		Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

	private static final int CONFLICT_REPLACE = 0x00000005;

	/**
	 * ��Status����д��һ������, �˷���Ϊ˽�з���, �ⲿ����������ʹ�� putTweets()
	 * 
	 * @param tweet
	 *            ��Ҫд��ĵ�����Ϣ
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 * @deprecated use {@link StatusDAO#insertStatus(Status, boolean)}
	 */
	public long insertTweet(Tweet tweet, String owner, int type, boolean isUnread) {
		SQLiteDatabase Db = mOpenHelper.getWritableDatabase();

		if (isExists(tweet.id, owner, type)) {
			Log.w(TAG, tweet.id + "is exists.");
			return -1;
		}

		ContentValues initialValues = makeTweetValues(tweet, owner, type, isUnread);
		long id = Db.insert(StatusTable.TABLE_NAME, null, initialValues);

		if (-1 == id) {
			Log.e(TAG, "cann't insert the tweet : " + tweet.toString());
		} else {
			// Log.v(TAG, "Insert a status into database : " +
			// tweet.toString());
		}

		return id;
	}

	/**
	 * ����һ����Ϣ
	 * 
	 * @param tweetId
	 * @param values
	 *            ContentValues ��Ҫ�����ֶεļ�ֵ��
	 * @return the number of rows affected
	 * @deprecated use {@link StatusDAO#updateStatus(String, ContentValues)}
	 */
	public int updateTweet(String tweetId, ContentValues values) {
		Log.v(TAG, "Update Tweet  : " + tweetId + " " + values.toString());

		SQLiteDatabase Db = mOpenHelper.getWritableDatabase();

		return Db.update(StatusTable.TABLE_NAME, values, StatusTable._ID + "=?", new String[] { tweetId });
	}

	/** @deprecated */
	private ContentValues makeTweetValues(Tweet tweet, String owner, int type, boolean isUnread) {
		// ����һ������Ϣ
		ContentValues initialValues = new ContentValues();
		initialValues.put(StatusTable.OWNER_ID, owner);
		initialValues.put(StatusTable.STATUS_TYPE, type);
		initialValues.put(StatusTable._ID, tweet.id);
		initialValues.put(StatusTable.TEXT, tweet.text);
		initialValues.put(StatusTable.USER_ID, tweet.userId);
		initialValues.put(StatusTable.USER_SCREEN_NAME, tweet.screenName);
		initialValues.put(StatusTable.PROFILE_IMAGE_URL, tweet.profileImageUrl);
		initialValues.put(StatusTable.PIC_THUMB, tweet.thumbnail_pic);
		initialValues.put(StatusTable.PIC_MID, tweet.bmiddle_pic);
		initialValues.put(StatusTable.PIC_ORIG, tweet.original_pic);
		initialValues.put(StatusTable.FAVORITED, tweet.favorited);
		initialValues.put(StatusTable.IN_REPLY_TO_STATUS_ID, tweet.inReplyToStatusId);
		initialValues.put(StatusTable.IN_REPLY_TO_USER_ID, tweet.inReplyToUserId);
		initialValues.put(StatusTable.IN_REPLY_TO_SCREEN_NAME, tweet.inReplyToScreenName);
		initialValues.put(StatusTable.REPOST_STATUS_ID, tweet.repostStatusId);
		initialValues.put(StatusTable.REPOST_USER_ID, tweet.repostUserId);
		// initialValues.put(FIELD_IS_REPLY, tweet.isReply());
		initialValues.put(StatusTable.CREATED_AT, DB_DATE_FORMATTER.format(tweet.createdAt));
		initialValues.put(StatusTable.SOURCE, tweet.source);
		initialValues.put(StatusTable.IS_UNREAD, isUnread);
		initialValues.put(StatusTable.TRUNCATED, tweet.truncated);
		initialValues.put(StatusTable.CONVERSATION_ID, tweet.conversationId);  // 2013.7.19
		initialValues.put(StatusTable.ATTACHMENT_URL, tweet.attachmentUrl);  // 2013.7.19
		initialValues.put(StatusTable.FOLLOWERS_COUNT, tweet.followersCount);  // 2013.7.20
		initialValues.put(StatusTable.FRIENDS_COUNT, tweet.friendsCount);  // 2013.7.20
		initialValues.put(StatusTable.FAVORITES_COUNT, tweet.favoritesCount);  // 2013.7.20
		initialValues.put(StatusTable.STATUSES_COUNT, tweet.statusesCount);  // 2013.7.20
		initialValues.put(StatusTable.USER_TOPIC_COUNT, tweet.userTopicCount);  // 2013.11.7
		initialValues.put(StatusTable.USER_REPLY_COUNT, tweet.userReplyCount);  // 2013.11.7
		initialValues.put(StatusTable.IS_FOLLOWING, tweet.isFollowing);  // 2013.7.20
		initialValues.put(StatusTable.AUDIO_DURATION, tweet.audioDuration);  // 2013.10.18
		initialValues.put(StatusTable.TYPE, tweet.type);  // 2013.10.18
		initialValues.put(StatusTable.CONVERSATION_COUNT, tweet.conversationCount);  // 2013.10.18
		initialValues.put(StatusTable.REPLY_COUNT, tweet.replyCount);  // 2013.10.18
		
		// TODO: truncated

		return initialValues;
	}

	/**
	 * д��N����Ϣ
	 * 
	 * @param tweets
	 *            ��Ҫд�����ϢList
	 * @return д��ļ�¼����
	 */
	public int putTweets(List<Tweet> tweets, String owner, int type, boolean isUnread) {
		if (TwitterApplication.DEBUG) {
			DebugTimer.betweenStart("Status DB");
		}
		if (null == tweets || 0 == tweets.size()) {
			return 0;
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int result = 0;
		try {
			db.beginTransaction();

			for (int i = tweets.size() - 1; i >= 0; i--) {
				Tweet tweet = tweets.get(i);

				Log.d(TAG, "insertTweet, tweet id=" + tweet.id);
				Log.d(TAG, "insertTweet, tweet conversation_count =" + tweet.conversationCount);
				if (TextUtils.isEmpty(tweet.id) || tweet.id.equals("false")){
					Log.e(TAG, "tweet id is null, ghost message encounted");
					continue;
				}
				
				ContentValues initialValues = makeTweetValues(tweet, owner, type, isUnread);
				long id = db.insert(StatusTable.TABLE_NAME, null, initialValues);

				if (-1 == id) {
					Log.e(TAG, "cann't insert the tweet : " + tweet.toString());
				} else {
					++result;
					// Log.v(TAG,
					// String.format("Insert a status into database[%s] : %s",
					// owner, tweet.toString()));
					Log.v("TAG", "Insert Status");
				}
			}

			// gc(type); // ��������
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		if (TwitterApplication.DEBUG) {
			DebugTimer.betweenEnd("Status DB");
		}
		return result;
	}

	/**
	 * ȡ��ָ���û���ĳһ���͵�������Ϣ
	 * 
	 * @param userId
	 * @param tableName
	 * @return a cursor
	 * @deprecated use {@link StatusDAO#findStatuses(String, int)}
	 */
	public Cursor fetchAllTweets(String owner, int type) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		return mDb.query(StatusTable.TABLE_NAME, StatusTable.TABLE_COLUMNS,
				StatusTable.OWNER_ID + " = ? AND " + StatusTable.STATUS_TYPE + " = " + type, new String[] { owner }, null, null,
				StatusTable.CREATED_AT + " DESC ");
		// LIMIT " + StatusTable.MAX_ROW_NUM);
	}

	/**
	 * ȡ���Լ���ĳһ���͵�������Ϣ
	 * 
	 * @param tableName
	 * @return a cursor
	 */
	public Cursor fetchAllTweets(int type) {
		// ��ȡ��¼�û�id
		SharedPreferences preferences = TwitterApplication.mPref;
		String myself = preferences.getString(Preferences.CURRENT_USER_ID, TwitterApplication.mApi.getUserId());

		return fetchAllTweets(myself, type);

	}
	
	public Cursor fetchAllTopicTweets(String owner, int type) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		return mDb.query(StatusTable.TABLE_NAME, StatusTable.TABLE_COLUMNS,
				StatusTable.OWNER_ID + " = ? AND " + StatusTable.STATUS_TYPE + " = " + type 
				+ " AND " + StatusTable.IN_REPLY_TO_STATUS_ID + " < 1", 
				new String[] { owner }, null, null, StatusTable.CREATED_AT + " DESC ");
		// LIMIT " + StatusTable.MAX_ROW_NUM);
	}
	
	public Cursor fetchAllReplyTweets(String owner, int type) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		return mDb.query(StatusTable.TABLE_NAME, StatusTable.TABLE_COLUMNS,
				StatusTable.OWNER_ID + " = ? AND " + StatusTable.STATUS_TYPE + " = " + type 
				+ " AND " + StatusTable.IN_REPLY_TO_STATUS_ID + " >= 1", 
				new String[] { owner }, null, null, StatusTable.CREATED_AT + " DESC ");
		// LIMIT " + StatusTable.MAX_ROW_NUM);
	}

	/**
	 * ���ĳ���͵�������Ϣ
	 * 
	 * @param tableName
	 * @return the number of rows affected if a whereClause is passed in, 0
	 *         otherwise. To remove all rows and get a count pass "1" as the
	 *         whereClause.
	 */
	public int dropAllTweets(int type) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		return mDb.delete(StatusTable.TABLE_NAME, StatusTable.STATUS_TYPE + " = " + type, null);
	}

	/**
	 * ȡ������ĳ����������ϢID
	 * 
	 * @param type
	 * @return The newest Status Id
	 */
	public String fetchMaxTweetId(String owner, int type) {
		return fetchMaxOrMinTweetId(owner, type, true);
	}

	/**
	 * ȡ������ĳ���������ϢID
	 * 
	 * @param tableName
	 * @return The oldest Status Id
	 */
	public String fetchMinTweetId(String owner, int type) {
		return fetchMaxOrMinTweetId(owner, type, false);
	}

	private String fetchMaxOrMinTweetId(String owner, int type, boolean isMax) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		String sql = "SELECT " + StatusTable._ID + " FROM "
				+ StatusTable.TABLE_NAME + " WHERE " + StatusTable.STATUS_TYPE
				+ " = " + type + " AND " + StatusTable.OWNER_ID + " = '"
				+ owner + "' " + " ORDER BY " + StatusTable.CREATED_AT;
		if (isMax)
			sql += " DESC ";

		Cursor mCursor = mDb.rawQuery(sql + " LIMIT 1", null);

		String result = null;

		if (mCursor == null) {
			return result;
		}

		mCursor.moveToFirst();
		if (mCursor.getCount() == 0) {
			result = null;
		} else {
			result = mCursor.getString(0);
		}
		mCursor.close();

		return result;
	}

	/**
	 * Count unread tweet
	 * 
	 * @param tableName
	 * @return
	 */
	public int fetchUnreadCount(String owner, int type) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + StatusTable._ID + ")"
				+ " FROM " + StatusTable.TABLE_NAME + " WHERE "
				+ StatusTable.STATUS_TYPE + " = " + type + " AND "
				+ StatusTable.OWNER_ID + " = '" + owner + "' AND "
				+ StatusTable.IS_UNREAD + " = 1 ",
		// "LIMIT " + StatusTable.MAX_ROW_NUM,
				null);

		int result = 0;

		if (mCursor == null) {
			return result;
		}

		mCursor.moveToFirst();
		result = mCursor.getInt(0);
		mCursor.close();

		return result;
	}

	public int addNewTweetsAndCountUnread(List<Tweet> tweets, String owner, int type) {
		putTweets(tweets, owner, type, true);

		return fetchUnreadCount(owner, type);
	}

	/**
	 * Set isFavorited
	 * 
	 * @param tweetId
	 * @param isFavorited
	 * @return Is Succeed
	 * @deprecated use {@link Status#setFavorited(boolean)} and
	 *             {@link StatusDAO#updateStatus(Status)}
	 */
	public boolean setFavorited(String tweetId, String isFavorited) {
		ContentValues values = new ContentValues();
		values.put(StatusTable.FAVORITED, isFavorited);
		int i = updateTweet(tweetId, values);

		return (i > 0) ? true : false;
	}

	// DM & Follower

	/**
	 * д��һ��˽��
	 * 
	 * @param dm
	 * @param isUnread
	 * @return the row ID of the newly inserted row, or -1 if an error occurred,
	 *         ��Ϊ������ԭ��,�˴����صĲ��� _ID ��ֵ, ����һ���������� row_id
	 */
	public long createDm(Dm dm, boolean isUnread) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(MessageTable._ID, dm.id);
		initialValues.put(MessageTable.FIELD_USER_SCREEN_NAME, dm.screenName);
		initialValues.put(MessageTable.FIELD_TEXT, dm.text);
		initialValues.put(MessageTable.FIELD_PROFILE_IMAGE_URL, dm.profileImageUrl);
		initialValues.put(MessageTable.FIELD_IS_UNREAD, isUnread);
		initialValues.put(MessageTable.FIELD_IS_SENT, dm.isSent);
		initialValues.put(MessageTable.FIELD_CREATED_AT, DB_DATE_FORMATTER.format(dm.createdAt));
		initialValues.put(MessageTable.FIELD_USER_ID, dm.userId);

		return mDb.insert(MessageTable.TABLE_NAME, null, initialValues);
	}

	//

	/**
	 * Create a follower
	 * 
	 * @param userId
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long createFollower(String userId) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(FollowTable._ID, userId);
		long rowId = mDb.insert(FollowTable.TABLE_NAME, null, initialValues);
		if (-1 == rowId) {
			Log.e(TAG, "Cann't create Follower : " + userId);
		} else {
			Log.v(TAG, "Success create follower : " + userId);
		}
		return rowId;
	}

	/**
	 * ���Followers�����������
	 * 
	 * @param followers
	 */
	public void syncFollowers(List<String> followers) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		try {
			mDb.beginTransaction();

			boolean result = deleteAllFollowers();
			Log.v(TAG, "Result of DeleteAllFollowers: " + result);

			for (String userId : followers) {
				createFollower(userId);
			}

			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}

	/**
	 * @param type
	 *            <li>MessageTable.TYPE_SENT</li> <li>MessageTable.TYPE_GET</li>
	 *            <li>�����κ�ֵ����Ϊȡ����������</li>
	 * @return
	 */
	public Cursor fetchAllDms(int type) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		String selection = null;

		if (MessageTable.TYPE_SENT == type) {
			selection = MessageTable.FIELD_IS_SENT + " = " + MessageTable.TYPE_SENT;
		} else if (MessageTable.TYPE_GET == type) {
			selection = MessageTable.FIELD_IS_SENT + " = " + MessageTable.TYPE_GET;
		}

		return mDb.query(MessageTable.TABLE_NAME, MessageTable.TABLE_COLUMNS,
				selection, null, null, null, MessageTable.FIELD_CREATED_AT + " DESC");
	}

	public Cursor fetchInboxDms() {
		return fetchAllDms(MessageTable.TYPE_GET);
	}

	public Cursor fetchSendboxDms() {
		return fetchAllDms(MessageTable.TYPE_SENT);
	}

	public Cursor fetchAllFollowers() {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		return mDb.query(FollowTable.TABLE_NAME, FollowTable.TABLE_COLUMNS, null, null, null, null, null);
	}

	/**
	 * FIXME:
	 * 
	 * @param filter
	 * @return
	 */
	public Cursor getFollowerUsernames(String filter) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		String likeFilter = '%' + filter + '%';

		// FIXME: �˷���������Ӧ����������д˽��ʱ�Զ������ϵ�˵Ĺ���,
		// �������ݿ��,��Ϊ����tweets���е���������, ���Լ���ʹ�øù���û��ʵ�ʼ�ֵ(��Ϊ�ܴ����ݿ��ж�������ϵ�˺���)
		// ����ɹ�ע��/����ע�����������, ���ܲ���ʹ�ñ�����һ��
		// [�����ע] �� id/name ����(��getFriendsIds��getFollowersIds�Ľ���, ��Ϊ�ͻ���ֻ�ܸ����Ƿ�˽��,
		// ���������
		// ֻ��ʾfollowers���б����������ɷ���������"ֻ�ܸ������ע���˷�˽��"�Ĵ�����Ϣ, �������û��޷����,
		// ��Ϊ����ϵ���������ṩ������ѡ���,
		// ���ҽ�Ŀǰ���Զ���ɹ��ܵĻ����ϼ�һ��[ѡ����ϵ��]��ť, ��������һ���µ���ϵ���б�ҳ������ʾ���пɷ���˽�ŵ���ϵ�˶���,
		// �����ֻ�д����ʱ��ѡ����ϵ�˹���
		return null;

		// FIXME: clean this up. �����ݿ���ʧЧ, ����, ����
		// return mDb.rawQuery(
		// "SELECT user_id AS _id, user"
		// + " FROM (SELECT user_id, user FROM tweets"
		// + " INNER JOIN followers on tweets.user_id = followers._id UNION"
		// + " SELECT user_id, user FROM dms INNER JOIN followers"
		// + " on dms.user_id = followers._id)"
		// + " WHERE user LIKE ?"
		// + " ORDER BY user COLLATE NOCASE",
		// new String[] { likeFilter });
	}

	/**
	 * @param userId
	 *            ���û��Ƿ�follow Me
	 * @deprecated δʹ��
	 * @return
	 */
	public boolean isFollower(String userId) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		Cursor cursor = mDb.query(FollowTable.TABLE_NAME,
				FollowTable.TABLE_COLUMNS, FollowTable._ID + "= ?",
				new String[] { userId }, null, null, null);

		boolean result = false;

		if (cursor != null && cursor.moveToFirst()) {
			result = true;
		}

		cursor.close();

		return result;
	}

	public boolean deleteAllFollowers() {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		return mDb.delete(FollowTable.TABLE_NAME, null, null) > 0;
	}

	public boolean deleteDm(String id) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		return mDb.delete(MessageTable.TABLE_NAME, String.format("%s = '%s'", MessageTable._ID, id), null) > 0;

	}

	/**
	 * @param tableName
	 * @return the number of rows affected
	 */
	public int markAllTweetsRead(String owner, int type) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(StatusTable.IS_UNREAD, 0);

		return mDb.update(StatusTable.TABLE_NAME, values, StatusTable.STATUS_TYPE + "=" + type, null);
	}

	public boolean deleteAllDms() {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		return mDb.delete(MessageTable.TABLE_NAME, null, null) > 0;
	}

	public int markAllDmsRead() {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(MessageTable.FIELD_IS_UNREAD, 0);

		return mDb.update(MessageTable.TABLE_NAME, values, null, null);
	}

	public String fetchMaxDmId(boolean isSent) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		Cursor mCursor = mDb.rawQuery("SELECT " + MessageTable._ID + " FROM "
				+ MessageTable.TABLE_NAME + " WHERE "
				+ MessageTable.FIELD_IS_SENT + " = ? " + " ORDER BY "
				+ MessageTable.FIELD_CREATED_AT + " DESC   LIMIT 1",
				new String[] { isSent ? "1" : "0" });

		String result = null;

		if (mCursor == null) {
			return result;
		}

		mCursor.moveToFirst();
		if (mCursor.getCount() == 0) {
			result = null;
		} else {
			result = mCursor.getString(0);
		}
		mCursor.close();

		return result;
	}

	public int addNewDmsAndCountUnread(List<Dm> dms) {
		addDms(dms, true);

		return fetchUnreadDmCount();
	}

	public int fetchDmCount() {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + MessageTable._ID + ") FROM " + MessageTable.TABLE_NAME, null);

		int result = 0;

		if (mCursor == null) {
			return result;
		}

		mCursor.moveToFirst();
		result = mCursor.getInt(0);
		mCursor.close();

		return result;
	}

	private int fetchUnreadDmCount() {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();

		Cursor mCursor = mDb.rawQuery("SELECT COUNT(" + MessageTable._ID
				+ ") FROM " + MessageTable.TABLE_NAME + " WHERE "
				+ MessageTable.FIELD_IS_UNREAD + " = 1", null);

		int result = 0;

		if (mCursor == null) {
			return result;
		}

		mCursor.moveToFirst();
		result = mCursor.getInt(0);
		mCursor.close();

		return result;
	}

	public void addDms(List<Dm> dms, boolean isUnread) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		try {
			mDb.beginTransaction();

			for (Dm dm : dms) {
				createDm(dm, isUnread);
			}

			// limitRows(TABLE_DIRECTMESSAGE, TwitterApi.RETRIEVE_LIMIT);
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}

	// 2011.03.01 add
	// UserInfo����

	public Cursor getAllUserInfo() {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();
		return mDb.query(UserInfoTable.TABLE_NAME, UserInfoTable.TABLE_COLUMNS, null, null, null, null, null);
	}

	/**
	 * ����id�б��ȡuser����
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
		userIdStr = userIdStr.substring(0, userIdStr.lastIndexOf(","));// ɾ�����Ķ���
		return mDb.query(UserInfoTable.TABLE_NAME, UserInfoTable.TABLE_COLUMNS,
				UserInfoTable._ID + " in (" + userIdStr + ")", null, null,
				null, null);

	}

	/**
	 * �½��û�
	 * 
	 * @param user
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long createUserInfo(com.codeim.coxin.data.User user) {
		SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();

		ContentValues initialValues = new ContentValues();
		initialValues.put(UserInfoTable._ID, user.id);
		initialValues.put(UserInfoTable.FIELD_USER_NAME, user.name);
		initialValues.put(UserInfoTable.FIELD_USER_SCREEN_NAME, user.screenName);
		initialValues.put(UserInfoTable.FIELD_LOCALTION, user.location);
		initialValues.put(UserInfoTable.FIELD_DESCRIPTION, user.description);
		initialValues.put(UserInfoTable.FIELD_PROFILE_IMAGE_URL, user.profileImageUrl);
		initialValues.put(UserInfoTable.FIELD_URL, user.url);
		initialValues.put(UserInfoTable.FIELD_PROTECTED, user.isProtected);
		initialValues.put(UserInfoTable.FIELD_FOLLOWERS_COUNT, user.followersCount);
		initialValues.put(UserInfoTable.FIELD_LAST_STATUS, user.lastStatus);
		initialValues.put(UserInfoTable.FIELD_FRIENDS_COUNT, user.friendsCount);
		initialValues.put(UserInfoTable.FIELD_FAVORITES_COUNT, user.favoritesCount);
		initialValues.put(UserInfoTable.FIELD_STATUSES_COUNT, user.statusesCount);
		initialValues.put(UserInfoTable.FIELD_FOLLOWING, user.isFollowing);

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

	// SQLiteDatabase.insertWithConflict��LEVEL 8(2.2)��������·���
	// Ϊ�˼��ݾɰ棬�������һ���򻯵ļ���ʵ��
	// Ҫע��������ʵ�ֺͱ�׼�ĺ�����Ϊ������ȫһ��
	private long insertWithOnConflict(SQLiteDatabase db, String tableName,
			String nullColumnHack, ContentValues initialValues,
			int conflictReplace) {

		long rowId = db.insert(tableName, nullColumnHack, initialValues);
		if (-1 == rowId) {
			// ����update
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

		// ʡȥ�ж�existUser���������������replace
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
	 * �鿴�����Ƿ��ѱ����û�����
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
	 * ����userid��ȡ��Ϣ
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
	 * �����û�
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
	 * �����û���Ϣ
	 */
	public boolean updateUser(com.codeim.coxin.data.User user) {

		SQLiteDatabase Db = mOpenHelper.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(UserInfoTable._ID, user.id);
		args.put(UserInfoTable.FIELD_USER_NAME, user.name);
		args.put(UserInfoTable.FIELD_USER_SCREEN_NAME, user.screenName);
		args.put(UserInfoTable.FIELD_LOCALTION, user.location);
		args.put(UserInfoTable.FIELD_DESCRIPTION, user.description);
		args.put(UserInfoTable.FIELD_PROFILE_IMAGE_URL, user.profileImageUrl);
		args.put(UserInfoTable.FIELD_URL, user.url);
		args.put(UserInfoTable.FIELD_PROTECTED, user.isProtected);
		args.put(UserInfoTable.FIELD_FOLLOWERS_COUNT, user.followersCount);
		args.put(UserInfoTable.FIELD_LAST_STATUS, user.lastStatus);
		args.put(UserInfoTable.FIELD_FRIENDS_COUNT, user.friendsCount);
		args.put(UserInfoTable.FIELD_FAVORITES_COUNT, user.favoritesCount);
		args.put(UserInfoTable.FIELD_STATUSES_COUNT, user.statusesCount);
		args.put(UserInfoTable.FIELD_FOLLOWING, user.isFollowing);

		return Db.update(UserInfoTable.TABLE_NAME, args, UserInfoTable._ID + "='" + user.id + "'", null) > 0;
	}

	/**
	 * ����ת���Ŀ���
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
	 * ͬ���û�,�����Ѵ��ڵ��û�,����δ���ڵ��û�
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
}
