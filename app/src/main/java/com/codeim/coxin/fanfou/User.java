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
import com.codeim.coxin.db.UserInfoTable;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;

/**
 * A data class representing Basic user information element
 */
//public class User extends WeiboResponse implements java.io.Serializable {
public class User {

//	static final String[] POSSIBLE_ROOT_NAMES = new String[] { "user", "sender", "recipient", "retweeting_user" };
//	private Weibo weibo;
	private String id;
	private String name;
	private String screenName;
	private String location;
	private String description;
	private String birthday;
	private String gender;
	private String profileImageUrl;
	private String url;
	private boolean isProtected;
	private int followersCount;
	private double latitude = 0;  //come 2013.5.18
	private double longitude = 0;  //come 2013.5.18
	private int distance;  //come 2013.5.18

	private Date statusCreatedAt;
	private String statusId = null;
	private String statusText = null;
	private String statusSource = null;
	private boolean statusTruncated = false;
	private String statusInReplyToStatusId;
	private String statusInReplyToUserId = null;
	private boolean statusFavorited = false;
	private String statusInReplyToScreenName = null;
	private String statusRepostStatusId = null;  //come 2013.7.3
	private String statusRepostUserId = null;
	private String statusHtml = null;  //come 2013.5.18
	private String statusConversationId = null;  //come 2013.7.9
	private int statusAudioDuration = 0;  // come 2013.8.4
	private int statusType = 0;  // come 2013.8.4
	private int statusReplyCount = 0;  // come 2013.8.29
	private int statusConversationCount = 0;  // come 2013.8.29
	
	private String attachmentUrl = null;

	private String profileBackgroundColor;
	private String profileTextColor;
	private String profileLinkColor;
	private String profileSidebarFillColor;
	private String profileSidebarBorderColor;
	private int friendsCount;
	private Date createdAt;
	private int favouritesCount;
	private int utcOffset;
	private String timeZone;
	private String profileBackgroundImageUrl;
	private String profileBackgroundTile;
	private boolean following;
	private boolean notificationEnabled;
	private int statusesCount;
	private int topicCount;  // come 2013.11.4
	private int replyCount;  // come 2013.11.4
	private boolean geoEnabled;
	private boolean verified;
	private static final long serialVersionUID = -6345893237975349030L;

//	/* package */User(Response res, Weibo weibo) throws HttpException {
//		super(res);
//		Element elem = res.asDocument().getDocumentElement();
//		init(elem, weibo);
//	}
//
//	/* package */public User(Response res, Element elem, Weibo weibo) throws HttpException {
//		super(res);
//		init(elem, weibo);
//	}

	/* package */public User(JSONObject json) throws HttpException {
		super();
		Log.d("User", "json before init");
		init(json);
	}

	/* package */User(Response res) throws HttpException {
		super();
		init(res.asJSONObject());
	}

	User() {

	}

	private void init(JSONObject json) throws HttpException {
		try {
			id = json.getString("id");
			Log.d("user_id:", id);
			
			name = json.getString("name");
//			screenName = json.getString("screen_name");
//			location = json.getString("location");
			gender = json.getString("gender");
			// birthday = json.getString("birthday");
//			description = json.getString("description");
			profileImageUrl = json.getString("profile_image_url");
//			url = json.getString("url");
//			isProtected = json.getBoolean("protected");
//			followersCount = json.getInt("followers_count");
			if (!json.isNull("curlat")) {
			    latitude = json.getDouble("curlat");  //come 2013.5.18
			}
			if (!json.isNull("curlon")) {
			    longitude = json.getDouble("curlon");  //come 2013.5.18
			}
//			distance = json.getInt("distance");  //come 2013.5.18
			// profileBackgroundColor = json.getString("profile_background_color");
			// profileTextColor = json.getString("profile_text_color");
			// profileLinkColor = json.getString("profile_link_color");
			// profileSidebarFillColor = json.getString("profile_sidebar_fill_color");
			// profileSidebarBorderColor = json.getString("profile_sidebar_border_color");
//			friendsCount = json.getInt("friends_count");
//			createdAt = parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
//			favouritesCount = json.getInt("favourites_count");
//			utcOffset = getInt("utc_offset", json);
			// timeZone = json.getString("time_zone");
			// profileBackgroundImageUrl = json.getString("profile_background_image_url");
			// profileBackgroundTile = json.getString("profile_background_tile");
//			following = getBoolean("following", json);
//			notificationEnabled = getBoolean("notifications", json);
//			statusesCount = json.getInt("statuses_count");
//			topicCount = json.getInt("topic_count");
//			replyCount = json.getInt("reply_count");
//			if (!json.isNull("status")) {
//				JSONObject status = json.getJSONObject("status");
//				// statusCreatedAt = parseDate(status.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
//				statusCreatedAt = parseDate(status.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
//				statusId = status.getString("id");
//				statusText = status.getString("text");
//				statusSource = status.getString("source");
//				statusTruncated = status.getBoolean("truncated");
//				statusInReplyToStatusId = getString("in_reply_to_status_id", status);
//				// statusInReplyToStatusId = status.getString("in_reply_to_status_id");
//				// statusInReplyToStatusId = status.getString("in_reply_to_lastmsg_id"); // 饭否不知为什么把这个参数的名称改了
//				statusInReplyToUserId = status.getString("in_reply_to_user_id");
//				statusFavorited = status.getBoolean("favorited");
//				statusInReplyToScreenName = status.getString("in_reply_to_screen_name");
//				// statusRepostStatusId = status.getString("repost_status_id");
//				// statusRepostUserId = status.getString("repost_user_id");
//				statusHtml = status.getString("statusnet_html");
//				statusConversationId = status.getString("statusnet_conversation_id");
//				statusAudioDuration = status.getInt("audio_duration");  // come 2013.8.4
//				statusType = status.getInt("type");  // come 2013.8.4
//				statusReplyCount = status.getInt("reply_count");  // come 2013.8.29
//				statusConversationCount = status.getInt("conversation_count");  // come 2013.8.29
//				if (!status.isNull("attachments")) {  //come 2013.5.18
//				    JSONArray attachments = status.getJSONArray("attachments");
//					attachmentUrl = attachments.getJSONObject(0).getString("url");
//					// Log.d("User", "attachments, attachmentUrl: " + attachmentUrl);
//				} else {
//				    Log.d("User", "no attachments");
//				}
//			}
		} catch (JSONException jsone) {
			throw new HttpException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

//	private void init(Element elem, Weibo weibo) throws HttpException {
//		this.weibo = weibo;
//		ensureRootNodeNameIs(POSSIBLE_ROOT_NAMES, elem);
//		id = getChildString("id", elem);
//		name = getChildText("name", elem);
//		screenName = getChildText("screen_name", elem);
//		location = getChildText("location", elem);
//		description = getChildText("description", elem);
//		profileImageUrl = getChildText("profile_image_url", elem);
//		url = getChildText("url", elem);
//		isProtected = getChildBoolean("protected", elem);
//		followersCount = getChildInt("followers_count", elem);
//
//		profileBackgroundColor = getChildText("profile_background_color", elem);
//		profileTextColor = getChildText("profile_text_color", elem);
//		profileLinkColor = getChildText("profile_link_color", elem);
//		profileSidebarFillColor = getChildText("profile_sidebar_fill_color", elem);
//		profileSidebarBorderColor = getChildText("profile_sidebar_border_color", elem);
//		friendsCount = getChildInt("friends_count", elem);
//		createdAt = getChildDate("created_at", elem);
//		favouritesCount = getChildInt("favourites_count", elem);
//		utcOffset = getChildInt("utc_offset", elem);
//		// timeZone = getChildText("time_zone", elem);
//		profileBackgroundImageUrl = getChildText("profile_background_image_url", elem);
//		profileBackgroundTile = getChildText("profile_background_tile", elem);
//		following = getChildBoolean("following", elem);
//		notificationEnabled = getChildBoolean("notifications", elem);
//		statusesCount = getChildInt("statuses_count", elem);
//		topicCount = getChildInt("topic_count", elem);
//		replyCount = getChildInt("reply_count", elem);
//		geoEnabled = getChildBoolean("geo_enabled", elem);
//		verified = getChildBoolean("verified", elem);
//
//		NodeList statuses = elem.getElementsByTagName("status");
//		if (statuses.getLength() != 0) {
//			Element status = (Element) statuses.item(0);
//			statusCreatedAt = getChildDate("created_at", status);
//			statusId = getChildString("id", status);
//			statusText = getChildText("text", status);
//			statusSource = getChildText("source", status);
//			statusTruncated = getChildBoolean("truncated", status);
//			statusInReplyToStatusId = getChildString("in_reply_to_status_id", status);
//			statusInReplyToUserId = getChildString("in_reply_to_user_id", status);
//			statusFavorited = getChildBoolean("favorited", status);
//			statusInReplyToScreenName = getChildText("in_reply_to_screen_name", status);
//			// statusRepostStatusId = getChildText("repost_status_id", status);
//			// statusRepostUserId = getChildText("repost_user_id", status);
//			statusHtml = getChildString("statusnet_html", status);  //come 2013.5.18
//			statusConversationId = getChildString("statusnet_conversation_id", status);  //2013.7.9
//			statusAudioDuration = getChildInt("audio_duration", status);  // 2013.8.4
//			statusType = getChildInt("type", status);  // 2013.8.4
//			statusReplyCount = getChildInt("reply_count", status);
//			statusConversationCount = getChildInt("conversation_count", status);
//		}
//	}

	/**
	 * Returns the id of the user
	 * 
	 * @return the id of the user
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the name of the user
	 * 
	 * @return the name of the user
	 */
	public String getName() {
		return name;
	}

	public String getGender() {
		return gender;
	}

	public String getBirthday() {
		return birthday;
	}

	/**
	 * Returns the screen name of the user
	 * 
	 * @return the screen name of the user
	 */
	public String getScreenName() {
		return screenName;
	}

	/**
	 * Returns the location of the user
	 * 
	 * @return the location of the user
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Returns the description of the user
	 * 
	 * @return the description of the user
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the profile image url of the user
	 * 
	 * @return the profile image url of the user
	 */
	public URL getProfileImageURL() {
		try {
			return new URL(TwitterApplication.mApi.getBaseURL()+profileImageUrl);
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * Returns the url of the user
	 * 
	 * @return the url of the user
	 */
	public URL getURL() {
		try {
			return new URL(TwitterApplication.mApi.getBaseURL()+url);
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * Test if the user status is protected
	 * 
	 * @return true if the user status is protected
	 */
	public boolean isProtected() {
		return isProtected;
	}

	/**
	 * Returns the number of followers
	 * 
	 * @return the number of followers
	 * @since Weibo4J 1.0.4
	 */
	public int getFollowersCount() {
		return followersCount;
	}
	
	public double getLatitude() {  //come 2013.5.18
	    return latitude;
	}
	
	public double getLongitude() {  //come 2013.5.18
	    return longitude;
	}
	
	public int getDistance() {  //come 2013.5.18
	    return distance;
	}

	// TODO: uncomment
	// public DirectMessage sendDirectMessage(String text) throws WeiboException
	// {
	// return weibo.sendDirectMessage(this.getName(), text);
	// }

//	public static List<User> constructUsers(Response res, Weibo weibo) throws HttpException {
//		Document doc = res.asDocument();
//		if (isRootNodeNilClasses(doc)) {
//			return new ArrayList<User>(0);
//		} else {
//			try {
//				ensureRootNodeNameIs("users", doc);
//				// NodeList list =
//				// doc.getDocumentElement().getElementsByTagName(
//				// "user");
//				// int size = list.getLength();
//				// List<User> users = new ArrayList<User>(size);
//				// for (int i = 0; i < size; i++) {
//				// users.add(new User(res, (Element) list.item(i), weibo));
//				// }
//
//				// 去除掉嵌套的bug
//				NodeList list = doc.getDocumentElement().getChildNodes();
//				List<User> users = new ArrayList<User>(list.getLength());
//				Node node;
//				for (int i = 0; i < list.getLength(); i++) {
//					node = list.item(i);
//					if (node.getNodeName().equals("user")) {
//						users.add(new User(res, (Element) node, weibo));
//					}
//				}
//
//				return users;
//			} catch (HttpException te) {
//				if (isRootNodeNilClasses(doc)) {
//					return new ArrayList<User>(0);
//				} else {
//					throw te;
//				}
//			}
//		}
//	}

//	public static UserWapper constructWapperUsers(Response res, Weibo weibo) throws HttpException {
//		Document doc = res.asDocument();
//		if (isRootNodeNilClasses(doc)) {
//			return new UserWapper(new ArrayList<User>(0), 0, 0);
//		} else {
//			try {
//				ensureRootNodeNameIs("users_list", doc);
//				Element root = doc.getDocumentElement();
//				NodeList user = root.getElementsByTagName("users");
//				int length = user.getLength();
//				if (length == 0) {
//					return new UserWapper(new ArrayList<User>(0), 0, 0);
//				}
//				//
//				Element listsRoot = (Element) user.item(0);
//				NodeList list = listsRoot.getChildNodes();
//				List<User> users = new ArrayList<User>(list.getLength());
//				Node node;
//				for (int i = 0; i < list.getLength(); i++) {
//					node = list.item(i);
//					if (node.getNodeName().equals("user")) {
//						users.add(new User(res, (Element) node, weibo));
//					}
//				}
//				//
//				long previousCursor = getChildLong("previous_curosr", root);
//				long nextCursor = getChildLong("next_curosr", root);
//				if (nextCursor == -1) { // 兼容不同标签名称
//					nextCursor = getChildLong("nextCurosr", root);
//				}
//				return new UserWapper(users, previousCursor, nextCursor);
//			} catch (HttpException te) {
//				if (isRootNodeNilClasses(doc)) {
//					return new UserWapper(new ArrayList<User>(0), 0, 0);
//				} else {
//					throw te;
//				}
//			}
//		}
//	}

	public static List<User> constructUsers(Response res) throws HttpException {
		try {
			JSONArray list = res.asJSONArray();
			int size = list.length();
			List<User> users = new ArrayList<User>(size);
			for (int i = 0; i < size; i++) {
				users.add(new User(list.getJSONObject(i)));
			}
			return users;
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
	public static UserWapper constructWapperUsers(Response res) throws HttpException {
		JSONObject jsonUsers = res.asJSONObject(); // asJSONArray();
		try {
			JSONArray user = jsonUsers.getJSONArray("users");
			int size = user.length();
			List<User> users = new ArrayList<User>(size);
			for (int i = 0; i < size; i++) {
				users.add(new User(user.getJSONObject(i)));
			}
			long previousCursor = jsonUsers.getLong("previous_curosr");
			long nextCursor = jsonUsers.getLong("next_cursor");
			if (nextCursor == -1) { // 兼容不同标签名称
				nextCursor = jsonUsers.getLong("nextCursor");
			}
			return new UserWapper(users, previousCursor, nextCursor);
		} catch (JSONException jsone) {
			throw new HttpException(jsone);
		}
	}

	/**
	 * @param res
	 * @return
	 * @throws HttpException
	 */
	static List<User> constructResult(Response res) throws HttpException {
		JSONArray list = res.asJSONArray();
		try {
			int size = list.length();
			List<User> users = new ArrayList<User>(size);
			for (int i = 0; i < size; i++) {
				users.add(new User(list.getJSONObject(i)));
			}
			return users;
		} catch (JSONException e) {
		}
		return null;
	}

	/**
	 * @return created_at or null if the user is protected
	 * @since Weibo4J 1.1.0
	 */
	public Date getStatusCreatedAt() {
		return statusCreatedAt;
	}

	/**
	 * 
	 * @return status id or -1 if the user is protected
	 */
	public String getStatusId() {
		return statusId;
	}

	/**
	 * 
	 * @return status text or null if the user is protected
	 */
	public String getStatusText() {
		return statusText;
	}

	/**
	 * 
	 * @return source or null if the user is protected
	 * @since 1.1.4
	 */
	public String getStatusSource() {
		return statusSource;
	}

	/**
	 * 
	 * @return truncated or false if the user is protected
	 * @since 1.1.4
	 */
	public boolean isStatusTruncated() {
		return statusTruncated;
	}

	/**
	 * 
	 * @return in_reply_to_status_id or -1 if the user is protected
	 * @since 1.1.4
	 */
	public String getStatusInReplyToStatusId() {
		return statusInReplyToStatusId;
	}

	/**
	 * 
	 * @return in_reply_to_user_id or -1 if the user is protected
	 * @since 1.1.4
	 */
	public String getStatusInReplyToUserId() {
		return statusInReplyToUserId;
	}

	/**
	 * 
	 * @return favorited or false if the user is protected
	 * @since 1.1.4
	 */
	public boolean isStatusFavorited() {
		return statusFavorited;
	}

	/**
	 * 
	 * @return in_reply_to_screen_name or null if the user is protected
	 * @since 1.1.4
	 */

	public String getStatusInReplyToScreenName() {
		return "" != statusInReplyToUserId ? statusInReplyToScreenName : null;
	}
	
	public String getStatusConversationId() {
		return statusConversationId;
	}
	
	public int getStatusAudioDuration() {
		return statusAudioDuration;
	}
	
	public int getStatusType() {
		return statusType;
	}
	
	public int getStatusReplyCount() {
	    return statusReplyCount;
	}
	
	public int getStatusConversationCount() {
	    return statusConversationCount;
	}
	
	public String getStatusRepostStatusId() {
	    return statusRepostStatusId;
	}
	
	public String getStatusRepostUserId() {
	    return statusRepostUserId;
	}
	
	public String getStatusHtml() {  //come 2013.5.18
	    return statusHtml;
	}
	
	public String getAttachmentUrl() {  //come 2013.5.18
	    return attachmentUrl;
	}

	public String getProfileBackgroundColor() {
		return profileBackgroundColor;
	}

	public String getProfileTextColor() {
		return profileTextColor;
	}

	public String getProfileLinkColor() {
		return profileLinkColor;
	}

	public String getProfileSidebarFillColor() {
		return profileSidebarFillColor;
	}

	public String getProfileSidebarBorderColor() {
		return profileSidebarBorderColor;
	}

	public int getFriendsCount() {
		return friendsCount;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public int getFavouritesCount() {
		return favouritesCount;
	}

	public int getUtcOffset() {
		return utcOffset;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public String getProfileBackgroundImageUrl() {
		return profileBackgroundImageUrl;
	}

	public String getProfileBackgroundTile() {
		return profileBackgroundTile;
	}

	/**
     *
     */
	public boolean isFollowing() {
		return following;
	}

	/**
	 * @deprecated use isNotificationsEnabled() instead
	 */

	public boolean isNotifications() {
		return notificationEnabled;
	}

	/**
	 * 
	 * @since Weibo4J 2.0.1
	 */
	public boolean isNotificationEnabled() {
		return notificationEnabled;
	}

	public int getStatusesCount() {
		return statusesCount;
	}
	
	public int getTopicCount() {
	    return topicCount;
	}
	
	public int getReplyCount() {
	    return replyCount;
	}

	/**
	 * @return the user is enabling geo location
	 * @since Weibo4J 2.0.10
	 */
	public boolean isGeoEnabled() {
		return geoEnabled;
	}

	/**
	 * @return returns true if the user is a verified celebrity
	 * @since Weibo4J 2.0.10
	 */
	public boolean isVerified() {
		return verified;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		return obj instanceof User && ((User) obj).id.equals(this.id);
	}

	@Override
	public String toString() {
//		return "User{" + "weibo=" + weibo + ", id=" + id + ", name='" + name
		return "User{" + ", id=" + id + ", name='" + name
				+ '\'' + ", screenName='" + screenName + '\'' + ", location='"
				+ location + '\'' + ", description='" + description + '\''
				+ ", profileImageUrl='" + profileImageUrl + '\'' + ", url='"
				+ url + '\'' + ", isProtected=" + isProtected
				+ ", followersCount=" + followersCount + ", statusCreatedAt="
				+ statusCreatedAt + ", statusId=" + statusId + ", statusText='"
				+ statusText + '\'' + ", statusSource='" + statusSource + '\''
				+ ", statusTruncated=" + statusTruncated
				+ ", statusInReplyToStatusId=" + statusInReplyToStatusId
				+ ", statusConversationId=" + statusConversationId
				+ ", statusAudioDuration=" + statusAudioDuration
				+ ", statusType=" + statusType
				+ ", statusReplyCount=" + statusReplyCount
				+ ", statusConversationCount=" + statusConversationCount
				+ ", statusInReplyToUserId=" + statusInReplyToUserId
				+ ", statusFavorited=" + statusFavorited
				+ ", statusInReplyToScreenName='" + statusInReplyToScreenName
				+ '\'' + ", profileBackgroundColor='" + profileBackgroundColor
				+ '\'' + ", profileTextColor='" + profileTextColor + '\''
				+ ", profileLinkColor='" + profileLinkColor + '\''
				+ ", profileSidebarFillColor='" + profileSidebarFillColor
				+ '\'' + ", profileSidebarBorderColor='"
				+ profileSidebarBorderColor + '\'' + ", friendsCount="
				+ friendsCount + ", createdAt="
				+ createdAt
				+ ", favouritesCount="
				+ favouritesCount
				+ ", utcOffset="
				+ utcOffset
				+
				// ", timeZone='" + timeZone + '\'' +
				", profileBackgroundImageUrl='" + profileBackgroundImageUrl
				+ '\'' + ", profileBackgroundTile='" + profileBackgroundTile
				+ '\'' + ", following=" + following + ", notificationEnabled="
				+ notificationEnabled + ", statusesCount=" + statusesCount
				+ ", topicCount=" + topicCount + ", replyCount=" + replyCount
				+ ", geoEnabled=" + geoEnabled + ", verified=" + verified + '}';
	}

	// TODO:增加从游标解析User的方法，用于和data里User转换一条数据
	public static User parseUser(Cursor cursor) {
		if (null == cursor || 0 == cursor.getCount() || cursor.getCount() > 1) {
			Log.w("User.ParseUser", "Cann't parse Cursor, bacause cursor is null or empty.");
		}
		cursor.moveToFirst();
		User u = new User();
		u.id = cursor.getString(cursor.getColumnIndex(UserInfoTable._ID));
		u.name = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_USER_NAME));
//		u.screenName = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_USER_SCREEN_NAME));
//		u.location = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_LOCALTION));
//		u.description = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_DESCRIPTION));
		u.profileImageUrl = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_PROFILE_IMAGE_URL));
//		u.url = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_URL));
//		u.isProtected = (0 == cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_PROTECTED))) ? false : true;
//		u.followersCount = cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_FOLLOWERS_COUNT));
//		u.friendsCount = cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_FRIENDS_COUNT));
//		u.favouritesCount = cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_FAVORITES_COUNT));
//		u.statusesCount = cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_STATUSES_COUNT));
//		u.following = (0 == cursor.getInt(cursor.getColumnIndex(UserInfoTable.FIELD_FOLLOWING))) ? false : true;

		try {
			String createAtStr = cursor.getString(cursor.getColumnIndex(UserInfoTable.FIELD_CREATED_TIME));
			if (createAtStr != null) {
				u.createdAt = TwitterDatabase.DB_DATE_FORMATTER.parse(createAtStr);
			}

		} catch (ParseException e) {
			Log.w("User", "Invalid created at data.");
		}
		return u;
	}

//	public com.codeim.coxin.data.User parseUser() {
//		com.codeim.coxin.data.User user = new com.codeim.coxin.data.User();
//		user.id = this.id;
//		user.name = this.name;
//		user.screenName = this.screenName;
//		user.location = this.location;
//		user.description = this.description;
//		user.profileImageUrl = this.profileImageUrl;
//		user.url = this.url;
//		user.isProtected = this.isProtected;
//		user.followersCount = this.followersCount;
//		user.friendsCount = this.friendsCount;
//		user.favoritesCount = this.favouritesCount;
//		user.statusesCount = this.statusesCount;
//		user.topicCount = this.topicCount;
//		user.replyCount = this.replyCount;
//		user.isFollowing = this.following;
//		user.createdAt = this.createdAt;
//		user.statusCreatedAt = this.statusCreatedAt;  // come 2013.5.18
//		user.statusId = this.statusId;  // come 2013.5.18
//		user.lastStatus = this.statusText;  // come 2013.5.18
//		user.statusInReplyToStatusId = this.statusInReplyToStatusId;  // come 2013.5.18
//		user.statusInReplyToUserId = this.statusInReplyToUserId;  // come 2013.5.18
//		user.statusHtml = this.statusHtml;
//		user.statusConversationId = this.statusConversationId;  // 2013.7.9
//		user.statusAudioDuration = this.statusAudioDuration;  // 2013.8.4
//		user.statusType = this.statusType;  // 2013.8.4
//		user.statusReplyCount = this.statusReplyCount;  // 2013.8.29
//		user.statusConversationCount = this.statusConversationCount;  // 2013.8.29
//		
//		return user;
//	}
	
	
	//the follow by ywwang. from WeiboResponse
	protected static Date parseDate(String str, String format)
			throws HttpException {
		//private static Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
		Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();
		
		if (str == null || "".equals(str)) {
			return null;
		}
		SimpleDateFormat sdf = formatMap.get(format);
		if (null == sdf) {
			sdf = new SimpleDateFormat(format, Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
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