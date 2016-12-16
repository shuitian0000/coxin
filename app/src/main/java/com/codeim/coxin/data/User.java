package com.codeim.coxin.data;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

	public String id;
	public String name;
	public String gender;
	public String screenName;
	public String location;
	public String description;
	public String profileImageUrl;
	public String url;
	public boolean isProtected;
	public int followersCount;
	public double latitude;  // come 2013.5.18
	public double longitude;  // come 2013.5.18
	public int distance;  // come 2013.5.18
	
	public Date statusCreatedAt;  // come 2013.5.18
	public String statusId;  // come 2013.5.18
	public String lastStatus;
	public String statusInReplyToStatusId;  // come 2013.5.18
	public String statusInReplyToUserId;  // come 2013.5.18
	public String statusInReplyToScreenName;  // come 2013.7.3
	public String statusHtml;  // come 2013.5.18
	public String statusFavorited;  // come 2013.7.3
	public String statusTruncated;  // come 2013.7.3
	public String statusRepostStatusId;  // come 2013.7.3
	public String statusRepostUserId;  // come 2013.7.3
	public String statusConversationId;  // come 2013.7.9
	public int statusAudioDuration;  // 2013.8.4
	public int statusType;  // 2013.8.4
	public int statusReplyCount;  // 2013.8.29
	public int statusConversationCount;  // 2013.8.29
	
	public String attachmentUrl;

	public int friendsCount;
	public int favoritesCount;
	public int statusesCount;
	public int topicCount;
	public int replyCount;
	public Date createdAt;
	public boolean isFollowing;

	// public boolean notifications;
	// public utc_offset

	public User() {
	}

	public static User create(com.codeim.coxin.fanfou.User u) {
		User user = new User();

		user.id = u.getId();
		user.name = u.getName();
		user.gender = u.getGender();
//		user.screenName = u.getScreenName();
//		user.location = u.getLocation();
//		user.description = u.getDescription();
		user.profileImageUrl = u.getProfileImageURL().toString();
//		if (u.getURL() != null) {
//			user.url = u.getURL().toString();
//		}
//		user.isProtected = u.isProtected();
//		user.followersCount = u.getFollowersCount();
		user.latitude = u.getLatitude();  //come 2013.5.18
		user.longitude = u.getLongitude();  //come 2013.5.18
//		user.distance = u.getDistance();  //come 2013.5.18
		
//		user.statusCreatedAt = u.getStatusCreatedAt();  //come 2013.5.18
//		user.statusId = u.getStatusId();  //come 2013.5.18
//		user.lastStatus = u.getStatusText();
//		user.statusConversationId = u.getStatusConversationId();  //come 2013.7.9
//		user.statusAudioDuration = u.getStatusAudioDuration();  // 2013.8.4
//		user.statusType = u.getStatusType();  // 2013.8.4
//		user.statusReplyCount = u.getStatusReplyCount();  // 2013.8.29
//		user.statusConversationCount = u.getStatusConversationCount();  // 2013.8.29
//		user.statusInReplyToStatusId = u.getStatusInReplyToStatusId();  //come 2013.5.18
//		user.statusInReplyToUserId = u.getStatusInReplyToUserId();  //come 2013.5.18
//		user.statusInReplyToScreenName = u.getStatusInReplyToScreenName();  //come 2013.7.3
//		user.statusRepostStatusId = u.getStatusRepostStatusId();  //come 2013.7.3
//		user.statusRepostUserId = u.getStatusRepostUserId();  //come 2013.7.3
//		user.statusHtml = u.getStatusHtml();  //come 2013.5.18
//		user.statusFavorited = u.isStatusFavorited() ? "true" : "false";
//		user.statusTruncated = u.isStatusTruncated() ? "true" : "false";
		
//		user.attachmentUrl = u.getAttachmentUrl();  //come 2013.5.18
//
//		user.friendsCount = u.getFriendsCount();
//		user.favoritesCount = u.getFavouritesCount();
//		user.statusesCount = u.getStatusesCount();
//		user.topicCount = u.getTopicCount();
//		user.replyCount = u.getReplyCount();
//		user.createdAt = u.getCreatedAt();
//		user.isFollowing = u.isFollowing();

		return user;
	}
	
	public static Tweet userSwitchToTweet(User user) {
	    Tweet tweet = new Tweet();
		
//		tweet.id = user.statusId;
//		tweet.screenName = user.screenName;
//		tweet.text = user.lastStatus;
		tweet.profileImageUrl = user.profileImageUrl;
//		tweet.createdAt = user.statusCreatedAt;
		tweet.userId = user.id;
//		tweet.favorited = user.statusFavorited;
//		tweet.truncated = user.statusTruncated;
//		tweet.conversationId = user.statusConversationId;
//		tweet.audioDuration = user.statusAudioDuration;  // 2013.8.4
//		tweet.type = user.statusType;  // 2013.8.4
//		tweet.replyCount = user.statusReplyCount;  // 2013.8.29
//		tweet.conversationCount = user.statusConversationCount;  // 2013.8.29
//		tweet.inReplyToStatusId = user.statusInReplyToStatusId;
//		tweet.inReplyToUserId = user.statusInReplyToUserId;
//		tweet.inReplyToScreenName = user.statusInReplyToScreenName;
//		tweet.repostStatusId = user.statusRepostStatusId;
//		tweet.repostUserId = user.statusRepostUserId;
//		tweet.attachmentUrl = user.attachmentUrl;
//		tweet.followersCount = user.followersCount;  // 2013.7.19
//		tweet.friendsCount = user.friendsCount;  // 2013.7.19
//		tweet.favoritesCount = user.favoritesCount;  // 2013.7.19
//		tweet.statusesCount = user.statusesCount;  // 2013.7.19
//		tweet.isFollowing = user.isFollowing;  // 2013.7.20
		tweet.thumbnail_pic = null;
		tweet.bmiddle_pic = null;
		tweet.original_pic = null;
		
		return tweet;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
//		boolean[] boolArray = new boolean[] { isProtected, isFollowing };
		out.writeString(id);
		out.writeString(name);
		out.writeString(gender);
//		out.writeString(screenName);
//		out.writeString(location);
//		out.writeString(description);
		out.writeString(profileImageUrl);
//		out.writeString(url);
//		out.writeBooleanArray(boolArray);
//		out.writeInt(friendsCount);
//		out.writeInt(followersCount);
//		out.writeInt(statusesCount);
//		out.writeInt(topicCount);
//		out.writeInt(replyCount);
	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			// return new User[size];
			throw new UnsupportedOperationException();
		}
	};

	public User(Parcel in) {
//		boolean[] boolArray = new boolean[] { isProtected, isFollowing };
		id = in.readString();
		name = in.readString();
		gender = in.readString();
//		screenName = in.readString();
//		location = in.readString();
//		description = in.readString();
		profileImageUrl = in.readString();
//		url = in.readString();
//		in.readBooleanArray(boolArray);
//		friendsCount = in.readInt();
//		followersCount = in.readInt();
//		statusesCount = in.readInt();
//		topicCount = in.readInt();
//		replyCount = in.readInt();

//		isProtected = boolArray[0];
//		isFollowing = boolArray[1];
	}
}
