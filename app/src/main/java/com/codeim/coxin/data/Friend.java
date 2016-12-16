package com.codeim.coxin.data;

import java.util.ArrayList;
import java.util.Date;

import com.codeim.coxin.util.DateTimeHelper;

import android.os.Parcel;
import android.os.Parcelable;

public class Friend implements Parcelable {

	public String id;
	
	public String ownerId;
	public String otherId;
	public String otherName;
	public String otherImageUrl;
	
	public Date create_time;
	public int is_delete;
	
//	public String lastChatMsgId;
//	public Date lastChatMsgTime;
//	public String lastChatMsgContent;
//	
//	public int unreadCount;

	public Friend() {
	}

	public static Friend create(com.codeim.coxin.fanfou.Friend u) {
		Friend info = new Friend();
		
        info.id             = u.getId();
        info.ownerId        = u.getOwnerId();
        
        info.otherId        = u.getOtherId();
        info.otherName      = u.getOtherName();
        info.otherImageUrl  = u.getOtherImageUrl();
        
    	info.create_time   = u.getCreateTime();
    	info.is_delete      = u.getIsDelete();
        
//        info.lastChatMsgId       = u.getLatestId();
//        info.lastChatMsgTime     = u.getLatestTime();
//        info.lastChatMsgContent  = u.getLatestContent();
//        
//        info.unreadCount    = u.getUnreadCount();

		return info;
	}
	public static Friend createWithoutURL(com.codeim.coxin.fanfou.Friend u) {
		Friend info = new Friend();
		
        info.id             = u.getId();
        info.ownerId        = u.getOwnerId();
        
        info.otherId        = u.getOtherId();
        info.otherName      = u.getOtherName();
        info.otherImageUrl  = u.getOtherImageUrlWithoutURL();
        
    	info.create_time   = u.getCreateTime();
    	info.is_delete      = u.getIsDelete();
        
//        info.lastChatMsgId       = u.getLatestId();
//        info.lastChatMsgTime     = u.getLatestTime();
//        info.lastChatMsgContent  = u.getLatestContent();
//        
//        info.unreadCount    = u.getUnreadCount();

		return info;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(id);
		out.writeString(ownerId);
		
		out.writeString(otherId);
		out.writeString(otherName);
		out.writeString(otherImageUrl);
		
		out.writeString(DateTimeHelper.dateToString(create_time, "yyyy-MM-dd HH:mm:ss"));
		out.writeInt(is_delete);
		
//		out.writeString(lastChatMsgId);
//		out.writeString(DateTimeHelper.dateToString(lastChatMsgTime, "yyyy-MM-dd HH:mm:ss"));
//		out.writeString(lastChatMsgContent);
//		
//		out.writeInt(unreadCount);
	}

	public static final Parcelable.Creator<Friend> CREATOR = new Parcelable.Creator<Friend>() {
		@Override
		public Friend createFromParcel(Parcel in) {
			return new Friend(in);
		}

		@Override
		public Friend[] newArray(int size) {
			 return new Friend[size];
//			throw new UnsupportedOperationException();
		}
	};

	@SuppressWarnings("unchecked")
	public Friend(Parcel in) {
		id            = in.readString();
        ownerId       = in.readString();
        
        otherId       = in.readString();
        otherName     = in.readString();
        otherImageUrl = in.readString();
        
        create_time    = DateTimeHelper.parseDateFromStr(in.readString(), "yyyy-MM-dd HH:mm:ss");
        is_delete     = in.readInt();
        
//        lastChatMsgId      = in.readString();
//        lastChatMsgTime    = DateTimeHelper.parseDateFromStr(in.readString(), "yyyy-MM-dd HH:mm:ss");
//        lastChatMsgContent = in.readString();
//        
//        unreadCount   = in.readInt();
	}
}
