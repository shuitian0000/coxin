package com.codeim.coxin.data;

import java.util.ArrayList;
import java.util.Date;

import com.codeim.coxin.util.DateTimeHelper;

import android.os.Parcel;
import android.os.Parcelable;

public class Chat implements Parcelable {

	public String id;
	
	public String ownerId;
	public String otherId;
	public String otherName;
	public String otherImageUrl;
	
	public String lastChatMsgId;
	public Date lastChatMsgTime;
	public String lastChatMsgContent;
	
	public int unreadCount;

	public Chat() {
	}

	public static Chat create(com.codeim.coxin.fanfou.Chat u) {
		Chat info = new Chat();
		
        info.id             = u.getId();
        info.ownerId        = u.getOwnerId();
        
        info.otherId        = u.getOtherId();
        info.otherName      = u.getOtherName();
        info.otherImageUrl  = u.getOtherImageUrl();
        
        info.lastChatMsgId       = u.getLatestId();
        info.lastChatMsgTime     = u.getLatestTime();
        info.lastChatMsgContent  = u.getLatestContent();
        
        info.unreadCount    = u.getUnreadCount();

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
		
		out.writeString(lastChatMsgId);
		out.writeString(DateTimeHelper.dateToString(lastChatMsgTime, "yyyy-MM-dd HH:mm:ss"));
		out.writeString(lastChatMsgContent);
		
		out.writeInt(unreadCount);
	}

	public static final Parcelable.Creator<Chat> CREATOR = new Parcelable.Creator<Chat>() {
		@Override
		public Chat createFromParcel(Parcel in) {
			return new Chat(in);
		}

		@Override
		public Chat[] newArray(int size) {
			 return new Chat[size];
//			throw new UnsupportedOperationException();
		}
	};

	@SuppressWarnings("unchecked")
	public Chat(Parcel in) {
		id            = in.readString();
        ownerId       = in.readString();
        
        otherId       = in.readString();
        otherName     = in.readString();
        otherImageUrl = in.readString();
        
        lastChatMsgId      = in.readString();
        lastChatMsgTime    = DateTimeHelper.parseDateFromStr(in.readString(), "yyyy-MM-dd HH:mm:ss");
        lastChatMsgContent = in.readString();
        
        unreadCount   = in.readInt();
	}
}
