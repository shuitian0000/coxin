package com.codeim.coxin.db;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.util.DateTimeHelper;

/**
 * Table - Statuses <br />
 * <br />
 * 为节省流量,故此表不保证本地数据库中所有消息具有前后连贯性, 而只确保最新的MAX_ROW_NUM条<br />
 * 数据的连贯性, 超出部分则视为垃圾数据, 不再允许读取, 也不保证其是前后连续的.<br />
 * <br />
 * 因为用户可能中途长时间停止使用本客户端,而换其他客户端(如网页), <br />
 * 如果保证本地所有数据的连贯性, 那么就必须自动去下载所有本地缺失的中间数据,<br />
 * 而这些数据极有可能是用户通过其他客户端阅读过的无用信息, 浪费了用户流量.<br />
 * <br />
 * 即认为相对于旧信息而言, 新信息对于用户更为价值, 所以只会新信息进行维护, <br />
 * 而旧信息一律视为无用的, 如用户需要查看超过MAX_ROW_NUM的旧数据, 可主动点击, <br />
 * 从而请求服务器. 本地只缓存最有价值的MAX条最新信息.<br />
 * <br />
 * 本地数据库中前MAX_ROW_NUM条的数据模拟一个定长列队, 即在尾部插入N条消息, 就会使得头部<br />
 * 的N条消息被标记为垃圾数据(但并不立即收回),只有在认为数据库数据过多时,<br />
 * 可手动调用 <code>StatusDatabase.gc(int type)</code> 方法进行垃圾清理.<br />
 * 
 * 
 */
public final class StatusTable implements BaseColumns {

	public static final String TAG = "StatusTable";

	// Status Types
	public static final int TYPE_HOME = 1; // 首页(我和我的好友)
	public static final int TYPE_MENTION = 2; // 提到我的
	public static final int TYPE_USER = 3; // 指定USER的
	public static final int TYPE_FAVORITE = 4; // 收藏
	public static final int TYPE_BROWSE = 5; // 随便看看

	public static final String TABLE_NAME = "status";
	public static final int MAX_ROW_NUM = 20; // 单类型数据安全区域

	public static final String OWNER_ID = "owner"; // 用于标识数据的所有者。以便于处理其他用户的信息（如其他用户的收藏）
	public static final String USER_ID = "uid";
	public static final String USER_SCREEN_NAME = "screen_name";
	public static final String PROFILE_IMAGE_URL = "profile_image_url";
	public static final String CREATED_AT = "created_at";
	public static final String TEXT = "text";
	public static final String SOURCE = "source";
	public static final String TRUNCATED = "truncated";
	public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";
	public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";
	public static final String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
	public static final String REPOST_STATUS_ID = "repost_status_id";
	public static final String REPOST_USER_ID = "repost_user_id";
	public static final String REPOST_USER_SCREEN_NAME = "repost_user_screen_name";
	public static final String FAVORITED = "favorited";
	public static final String IS_UNREAD = "is_unread";
	public static final String STATUS_TYPE = "status_type";
	public static final String PIC_THUMB = "pic_thumbnail";
	public static final String PIC_MID = "pic_middle";
	public static final String PIC_ORIG = "pic_original";
	public static final String CONVERSATION_ID = "conversation_id";  // 2013.7.19
	public static final String ATTACHMENT_URL = "attachment_url";  // 2013.7.19
	public static final String FOLLOWERS_COUNT = "followers_count";  // 2013.7.20
	public static final String FRIENDS_COUNT = "friends_count";  // 2013.7.20
	public static final String FAVORITES_COUNT = "favorites_count";  // 2013.7.20
	public static final String STATUSES_COUNT = "statuses_count";  // 2013.7.20
	public static final String USER_TOPIC_COUNT = "user_topic_count";  // 2013.7.20
	public static final String USER_REPLY_COUNT = "user_reply_count";  // 2013.7.20
	public static final String IS_FOLLOWING = "is_following";  // 2013.7.20
	public static final String AUDIO_DURATION = "audio_duration";  // 2013.8.4
	public static final String TYPE = "type";  // 2013.8.4
	public static final String CONVERSATION_COUNT = "conversation_count";  // 2013.9.29
	public static final String REPLY_COUNT = "reply_count";  // 2013.9.29
	
	// private static final String FIELD_PHOTO_URL = "photo_url";
	// private double latitude = -1;
	// private double longitude = -1;
	// private String thumbnail_pic;
	// private String bmiddle_pic;
	// private String original_pic;

	public static final String[] TABLE_COLUMNS = new String[] { _ID,
			USER_SCREEN_NAME, TEXT, PROFILE_IMAGE_URL, IS_UNREAD, CREATED_AT, FAVORITED, 
			IN_REPLY_TO_STATUS_ID, IN_REPLY_TO_USER_ID, REPOST_STATUS_ID, REPOST_USER_ID, 
			REPOST_USER_SCREEN_NAME, IN_REPLY_TO_SCREEN_NAME, TRUNCATED, PIC_THUMB, PIC_MID, PIC_ORIG,
			SOURCE, USER_ID, STATUS_TYPE, OWNER_ID, CONVERSATION_ID, ATTACHMENT_URL, 
			FOLLOWERS_COUNT, FRIENDS_COUNT, FAVORITES_COUNT, STATUSES_COUNT, USER_TOPIC_COUNT, USER_REPLY_COUNT,
			IS_FOLLOWING, AUDIO_DURATION, TYPE, CONVERSATION_COUNT, REPLY_COUNT};

	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ " (" + _ID + " text not null," + STATUS_TYPE + " text not null, "
			+ OWNER_ID + " text not null, " + USER_ID + " text not null, "
			+ USER_SCREEN_NAME + " text not null, " + TEXT + " text not null, "
			+ PROFILE_IMAGE_URL + " text not null, " + IS_UNREAD + " boolean not null, " 
			+ CREATED_AT + " date not null, " + SOURCE + " text not null, "
			+ FAVORITED + " text, " // TODO : text -> boolean
			+ IN_REPLY_TO_STATUS_ID + " text, " + IN_REPLY_TO_USER_ID + " text, "
			+ REPOST_STATUS_ID + " text, " + REPOST_USER_ID + " text, "
			+ REPOST_USER_SCREEN_NAME + " text, "  + IN_REPLY_TO_SCREEN_NAME + " text, " 
			+ PIC_THUMB + " text, " + PIC_MID + " text, " + PIC_ORIG + " text, "
			+ CONVERSATION_ID + " text, " + ATTACHMENT_URL + " text, "  // 2013.7.19
			+ FOLLOWERS_COUNT + " int, " + FRIENDS_COUNT + " int, "  // 2013.7.20
			+ FAVORITES_COUNT + " int, " + STATUSES_COUNT + " int, " // 2013.7.20
			+ USER_TOPIC_COUNT + " int, " + USER_REPLY_COUNT + " int, " // 2013.11.7
			+ IS_FOLLOWING + " boolean, "  // 2013.7.20
			+ AUDIO_DURATION + " int, " + TYPE + " int, "  // 2013.8.4
			+ CONVERSATION_COUNT + " int, " + REPLY_COUNT + " int, "
			+ TRUNCATED + " boolean ," + "PRIMARY KEY (" + _ID + "," + OWNER_ID
			+ "," + STATUS_TYPE + "))";

	/**
	 * 将游标解析为一条Tweet
	 * 
	 * 
	 * @param cursor 该方法不会移动或关闭游标
	 * @return 成功返回 Tweet 类型的单条数据, 失败返回null
	 */
	public static Tweet parseCursor(Cursor cursor) {

		if (null == cursor || 0 == cursor.getCount()) {
			Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
			return null;
		} else if (-1 == cursor.getPosition()) {
			cursor.moveToFirst();
		}

		Tweet tweet = new Tweet();
		tweet.id = cursor.getString(cursor.getColumnIndex(_ID));
		tweet.createdAt = DateTimeHelper.parseDateTimeFromSqlite(cursor.getString(cursor.getColumnIndex(CREATED_AT)));
		tweet.favorited = cursor.getString(cursor.getColumnIndex(FAVORITED));
		tweet.screenName = cursor.getString(cursor.getColumnIndex(USER_SCREEN_NAME));
		tweet.userId = cursor.getString(cursor.getColumnIndex(USER_ID));
		tweet.text = cursor.getString(cursor.getColumnIndex(TEXT));
		tweet.source = cursor.getString(cursor.getColumnIndex(SOURCE));
		tweet.profileImageUrl = cursor.getString(cursor.getColumnIndex(PROFILE_IMAGE_URL));
		tweet.inReplyToScreenName = cursor.getString(cursor.getColumnIndex(IN_REPLY_TO_SCREEN_NAME));
		tweet.inReplyToStatusId = cursor.getString(cursor.getColumnIndex(IN_REPLY_TO_STATUS_ID));
		tweet.inReplyToUserId = cursor.getString(cursor.getColumnIndex(IN_REPLY_TO_USER_ID));
		tweet.repostStatusId = cursor.getString(cursor.getColumnIndex(REPOST_STATUS_ID));
		tweet.repostUserId = cursor.getString(cursor.getColumnIndex(REPOST_USER_ID));
		tweet.truncated = cursor.getString(cursor.getColumnIndex(TRUNCATED));
		tweet.thumbnail_pic = cursor.getString(cursor.getColumnIndex(PIC_THUMB));
		tweet.bmiddle_pic = cursor.getString(cursor.getColumnIndex(PIC_MID));
		tweet.original_pic = cursor.getString(cursor.getColumnIndex(PIC_ORIG));
		tweet.setStatusType(cursor.getInt(cursor.getColumnIndex(STATUS_TYPE)));
		tweet.conversationId = cursor.getString(cursor.getColumnIndex(CONVERSATION_ID));  // 2013.7.19
		tweet.attachmentUrl = cursor.getString(cursor.getColumnIndex(ATTACHMENT_URL));  // 2013.7.19
		tweet.followersCount = cursor.getInt(cursor.getColumnIndex(FOLLOWERS_COUNT));  // 2013.7.20
		tweet.friendsCount = cursor.getInt(cursor.getColumnIndex(FRIENDS_COUNT));  // 2013.7.20
		tweet.favoritesCount = cursor.getInt(cursor.getColumnIndex(FAVORITES_COUNT));  // 2013.7.20
		tweet.statusesCount = cursor.getInt(cursor.getColumnIndex(STATUSES_COUNT));  // 2013.7.20
		tweet.userTopicCount = cursor.getInt(cursor.getColumnIndex(USER_TOPIC_COUNT));  // 2013.11.7
		tweet.userReplyCount = cursor.getInt(cursor.getColumnIndex(USER_REPLY_COUNT));  // 2013.11.7
		tweet.isFollowing = (cursor.getString(cursor.getColumnIndex(IS_FOLLOWING)).equals("1")) ? true : false;  // 2013.7.20
		tweet.audioDuration = cursor.getInt(cursor.getColumnIndex(AUDIO_DURATION));  // 2013.8.4
		tweet.type = cursor.getInt(cursor.getColumnIndex(TYPE));  // 2013.8.4
		Log.d(TAG, "CONVERSATION_COUNT is " + tweet.conversationCount);
		tweet.conversationCount = cursor.getInt(cursor.getColumnIndex(CONVERSATION_COUNT));  // 2013.9.29
		tweet.replyCount = cursor.getInt(cursor.getColumnIndex(REPLY_COUNT));  // 2013.9.29

		return tweet;
	}
}