/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codeim.coxin.data;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
//import com.codeim.coxin.fanfou.Status;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.TextHelper;

public class Tweet extends Message implements Parcelable {
	private static final String TAG = "Tweet";
	
	public String attachmentUrl;
	public String conversationId;
	
	public int followersCount;  // 2013.7.19   用户粉丝数量
	public int friendsCount;  // 2013.7.19     用户关注数量
	public int favoritesCount;  // 2013.7.19   用户收藏语音数量
	public int statusesCount;  // 2013.7.19    用户语音数量
	public int userTopicCount;  // 2013.11.7   用户提问数量
	public int userReplyCount;  // 2013.11.7   用户回答数量，这个是一个用户所有的回答数量
	public boolean isFollowing;  // 2013.7.20  用户是否关注
	
	public int audioDuration;  // 2013.8.4 语音时间长度
	public int type;  // 2013.8.4 语音类型，一共有9种语音类型，默认是0，不包含在类型中
	public int replyCount;  // 2013.8.29 语音回复数量，这个是对一条语音信息的回答数量
	public int conversationCount;  // 2013.8.29 语音会话数量

	public com.codeim.coxin.fanfou.User user;
	public String source;
	public String prevId;
	private int statusType = -1; // @see StatusTable#TYPE_*

	public void setStatusType(int type) {
		statusType = type;
	}

	public int getStatusType() {
		return statusType;
	}

	public Tweet() {
	}

//	public static Tweet create(Status status) {
//		Tweet tweet = new Tweet();
//
//		tweet.id = status.getId();
//		// 转义符放到getSimpleTweetText里去处理，这里不做处理
//		tweet.text = status.getText();
//		tweet.createdAt = status.getCreatedAt();
//		tweet.favorited = status.isFavorited() ? "true" : "false";
//		tweet.truncated = status.isTruncated() ? "true" : "false";
//		tweet.conversationId = status.getConversationId();  //2013.7.9
//		tweet.inReplyToStatusId = status.getInReplyToStatusId();
//		tweet.inReplyToUserId = status.getInReplyToUserId();
//		tweet.inReplyToScreenName = status.getInReplyToScreenName();
//		tweet.repostStatusId = status.getRepostStatusId();
//		tweet.repostUserId = status.getRepostUserId();
//		tweet.screenName = TextHelper.getSimpleTweetText(status.getUser().getScreenName());
//		tweet.profileImageUrl = status.getUser().getProfileImageURL().toString();
//		tweet.userId = status.getUser().getId();
//		tweet.user = status.getUser();
//		tweet.thumbnail_pic = status.getThumbnail_pic();
//		tweet.bmiddle_pic = status.getBmiddle_pic();
//		tweet.original_pic = status.getOriginal_pic();
//		tweet.source = TextHelper.getSimpleTweetText(status.getSource());
//		tweet.attachmentUrl = status.getAttachmentUrl();
//		tweet.followersCount = status.getUser().getFollowersCount();  // 2013.7.19
//		tweet.friendsCount = status.getUser().getFriendsCount();  // 2013.7.19
//		tweet.favoritesCount = status.getUser().getFavouritesCount();  // 2013.7.19
//		tweet.statusesCount = status.getUser().getStatusesCount();  // 2013.7.19
//		tweet.userTopicCount = status.getUser().getTopicCount();  // 2013.11.7
//		tweet.userReplyCount = status.getUser().getReplyCount();  // 2013.11.7
//		tweet.isFollowing = status.getUser().isFollowing();  // 2013.7.20
//		tweet.audioDuration = status.getAudioDuration();  // 2013.8.4
//		tweet.type = status.getType();  // 2013.8.4
//		tweet.replyCount = status.getReplyCount();  // 2013.8.29
//		tweet.conversationCount = status.getConversationCount();  // 2013.8.29
//
//		return tweet;
//	}

	public static Tweet createFromSearchApi(JSONObject jsonObject) throws JSONException {
		Tweet tweet = new Tweet();

		tweet.id = jsonObject.getString("id") + "";
		// 转义符放到getSimpleTweetText里去处理，这里不做处理
		tweet.text = jsonObject.getString("text");
		tweet.createdAt = DateTimeHelper.parseSearchApiDateTime(jsonObject.getString("created_at"));
		tweet.favorited = jsonObject.getString("favorited");
		tweet.truncated = jsonObject.getString("truncated");
		tweet.conversationId = jsonObject.getString("statusnet_conversation_id");
		tweet.inReplyToStatusId = jsonObject.getString("in_reply_to_status_id");
		tweet.inReplyToUserId = jsonObject.getString("in_reply_to_user_id");
		tweet.inReplyToScreenName = jsonObject.getString("in_reply_to_screen_name");
		tweet.screenName = TextHelper.getSimpleTweetText(jsonObject.getString("from_user"));
		tweet.profileImageUrl = jsonObject.getString("profile_image_url");
		tweet.userId = jsonObject.getString("from_user_id");
		tweet.source = TextHelper.getSimpleTweetText(jsonObject.getString("source"));
		if (!jsonObject.isNull("attachments")) {
			JSONArray attachments = jsonObject.getJSONArray("attachments");
		    tweet.attachmentUrl = attachments.getJSONObject(0).getString("url");
		} else {
			Log.d("SearchApi", "no attachments");
	    }

		return tweet;
	}

	public static String buildMetaText(StringBuilder builder, Date createdAt, String source, String replyTo, String repostUserId) {
		builder.setLength(0);

		builder.append(DateTimeHelper.getRelativeDate(createdAt));
		builder.append(" ");
		builder.append(TwitterApplication.mContext.getString(R.string.tweet_source_prefix));
		builder.append(source);

		if (!TextUtils.isEmpty(replyTo)) {
			builder.append(" " + TwitterApplication.mContext.getString(R.string.tweet_reply_to_prefix));
			builder.append(replyTo);
			builder.append(TwitterApplication.mContext.getString(R.string.tweet_reply_to_suffix));
		}
		
		if (!TextUtils.isEmpty(repostUserId)) {
			builder.append(" " + TwitterApplication.mContext.getString(R.string.tweet_repost_prefix));
			builder.append(repostUserId);
			builder.append(TwitterApplication.mContext.getString(R.string.tweet_repost_suffix));
		}
		
		return builder.toString();
	}
	
	/*
	public static Tweet userSwitchToTweet(User user) {
	    Tweet tweet = new Tweet();
		
		tweet.id = user.statusId;
		tweet.screenName = user.screenName;
		tweet.text = user.lastStatus;
		tweet.profileImageUrl = user.profileImageUrl;
		tweet.createdAt = user.statusCreatedAt;
		tweet.userId = user.id;
		tweet.favorited = user.statusFavorited;
		tweet.truncated = user.statusTruncated;
		tweet.conversationId = user.statusConversationId;
		tweet.inReplyToStatusId = user.statusInReplyToStatusId;
		tweet.inReplyToUserId = user.statusInReplyToUserId;
		tweet.inReplyToScreenName = user.statusInReplyToScreenName;
		tweet.repostStatusId = user.statusRepostStatusId;
		tweet.repostUserId = user.statusRepostUserId;
		tweet.attachmentUrl = user.attachmentUrl;
		tweet.followersCount = user.followersCount;  // 2013.7.19
		tweet.friendsCount = user.friendsCount;  // 2013.7.19
		tweet.favoritesCount = user.favoritesCount;  // 2013.7.19
		tweet.statusesCount = user.statusesCount;  // 2013.7.19
		tweet.thumbnail_pic = null;
		tweet.bmiddle_pic = null;
		tweet.original_pic = null;
		
		return tweet;
	}
	*/
	
//	public static User tweetSwitchToUser(Tweet tweet) {
//	    User user = new User();
//		
//		user.statusId = tweet.id;
//		user.screenName = tweet.screenName;
//		user.lastStatus = tweet.text;
//		user.profileImageUrl = tweet.profileImageUrl;
//		user.statusCreatedAt = tweet.createdAt;
//		user.id = tweet.userId;
//		user.statusFavorited = tweet.favorited;
//		user.statusFavorited = tweet.truncated;
//		user.statusConversationId = tweet.conversationId;
//		user.statusInReplyToStatusId = tweet.inReplyToStatusId;
//		user.statusInReplyToUserId = tweet.inReplyToUserId;
//		user.statusInReplyToScreenName = tweet.inReplyToScreenName;
//		user.statusRepostStatusId = tweet.repostStatusId;
//		user.statusRepostUserId = tweet.repostUserId;
//		user.attachmentUrl = tweet.attachmentUrl;
//		user.followersCount = tweet.followersCount;  // 2013.7.19
//		user.friendsCount = tweet.friendsCount;  // 2013.7.19
//		user.favoritesCount = tweet.favoritesCount;  // 2013.7.19
//		user.statusesCount = tweet.statusesCount;  // 2013.7.19
//		user.topicCount = tweet.userTopicCount;  // 2013.11.7
//		user.replyCount = tweet.userReplyCount;  // 2013.11.7
//		user.isFollowing = tweet.isFollowing;  // 2013.7.19
//		user.statusAudioDuration = tweet.audioDuration;  // 2013.8.4
//		user.statusType = tweet.type;  // 2013.8.4
//		user.statusReplyCount = tweet.replyCount;  // 2013.8.29
//		user.statusConversationCount = tweet.conversationCount;  // 2013.8.29
//		user.distance = -1;  // 2013.11.7
//		
//		return user;
//	}

	// For interface Parcelable

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
	    boolean[] boolArray = new boolean[] { isFollowing };
		out.writeString(id);
		out.writeString(text);
		out.writeValue(createdAt); // Date
		out.writeString(screenName);
		out.writeString(favorited);
		out.writeString(conversationId);  // 2013.7.9
		out.writeString(inReplyToStatusId);
		out.writeString(inReplyToUserId);
		out.writeString(inReplyToScreenName);
		out.writeString(repostStatusId);
		out.writeString(repostUserId);
		out.writeString(screenName);
		out.writeString(profileImageUrl);
		out.writeString(thumbnail_pic);
		out.writeString(bmiddle_pic);
		out.writeString(original_pic);
		out.writeString(userId);
		out.writeString(source);
		out.writeString(attachmentUrl);  // 2013.7.14
		out.writeInt(followersCount);  // 2013.7.20
		out.writeInt(friendsCount);  // 2013.7.20
		out.writeInt(favoritesCount);  // 2013.7.20
		out.writeInt(statusesCount);  // 2013.7.20
		out.writeInt(userTopicCount);  // 2013.11.7
		out.writeInt(userReplyCount);  // 2013.11.7
		out.writeBooleanArray(boolArray);  // 2013.7.20
		out.writeInt(audioDuration);  // 2013.8.4
		out.writeInt(type);  // 2013.8.4
		out.writeInt(replyCount);  // 2013.8.29
		out.writeInt(conversationCount);  // 2013.8.29
	}

	public static final Parcelable.Creator<Tweet> CREATOR = new Parcelable.Creator<Tweet>() {
		public Tweet createFromParcel(Parcel in) {
			return new Tweet(in);
		}

		public Tweet[] newArray(int size) {
			// return new Tweet[size];
			throw new UnsupportedOperationException();
		}
	};

	public Tweet(Parcel in) {
	    boolean[] boolArray = new boolean[] { isFollowing };
		id = in.readString();
		text = in.readString();
		createdAt = (Date) in.readValue(Date.class.getClassLoader());
		screenName = in.readString();
		favorited = in.readString();
		conversationId = in.readString();
		inReplyToStatusId = in.readString();
		inReplyToUserId = in.readString();
		inReplyToScreenName = in.readString();
		repostStatusId = in.readString();
		repostUserId = in.readString();
		screenName = in.readString();
		profileImageUrl = in.readString();
		thumbnail_pic = in.readString();
		bmiddle_pic = in.readString();
		original_pic = in.readString();
		userId = in.readString();
		source = in.readString();
		attachmentUrl = in.readString();  // 2013.7.14
		followersCount = in.readInt();  // 2013.7.20
		friendsCount = in.readInt();  // 2013.7.20
		favoritesCount = in.readInt();  // 2013.7.20
		statusesCount = in.readInt();  // 2013.7.20
		userTopicCount = in.readInt();  // 2013.11.7
		userReplyCount = in.readInt();  // 2013.11.7
		in.readBooleanArray(boolArray);  // 2013.7.20
		isFollowing = boolArray[0];  // 2013.7.20
		audioDuration = in.readInt();  // 2013.8.4
		type = in.readInt();  // 2013.8.4
		replyCount = in.readInt();  // 2013.8.29
		conversationCount = in.readInt();  // 2013.8.29
	}

	@Override
	public String toString() {
		return "Tweet [source=" + source 
		        + ", id=" + id 
				+ ", screenName=" + screenName 
				+ ", text=" + text 
				+ ", profileImageUrl=" + profileImageUrl 
				+ ", createdAt=" + createdAt 
				+ ", userId=" + userId 
				+ ", favorited=" + favorited 
				+ ", conversationId=" + conversationId 
				+ ", attachmentUrl=" + attachmentUrl 
				+ ", audioDuration=" + audioDuration
				+ ", type=" + type
				+ ", replyCount=" + replyCount
				+ ", conversationCount=" + conversationCount
				+ ", inReplyToStatusId=" + inReplyToStatusId 
				+ ", inReplyToUserId=" + inReplyToUserId
				+ ", inReplyToScreenName=" + inReplyToScreenName + "]";
	}
}
