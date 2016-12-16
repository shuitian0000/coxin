package com.codeim.coxin.data;

import java.util.ArrayList;
import java.util.Date;

import com.codeim.coxin.util.DateTimeHelper;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatMsg implements Parcelable {

	public String id;
	
	public String masterId;
	public String masterName;
	public String slaveId;
	public String slaveName;

	public String content;
	public int sendOrGet;
	public int msgType; //0: text; 1:pic; 2:audio; 3:video
	public Date chatMsgTime;
	public int  status;
	public int isUnRead;
	public int isSent;

	public ChatMsg() {
	}

	public static ChatMsg create(com.codeim.coxin.fanfou.ChatMsg u) {
		ChatMsg info = new ChatMsg();
		
        info.id             = u.getId();
        
        info.masterId       = u.getMasterId();
        info.masterName     = u.getMasterName();
        info.slaveId        = u.getSlaveId();
        info.slaveName      = u.getSlaveName();
        
        info.content        = u.getContent();
        info.sendOrGet      = 0;
        info.msgType        = u.getMsgType();
        info.chatMsgTime    = u.getChatMsgTime();
        info.status         = u.getStatus();
        info.isUnRead       = 1;
        info.isSent         = 0;

		return info;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(id);
		out.writeString(masterId);
		
		out.writeString(masterName);
		out.writeString(slaveId);
		out.writeString(slaveName);

		out.writeString(content);
		out.writeInt(sendOrGet);
		out.writeInt(msgType);
		out.writeString(DateTimeHelper.dateToString(chatMsgTime, "yyyy-MM-dd HH:mm:ss"));
		out.writeInt(status);
		out.writeInt(isUnRead);
		out.writeInt(isUnRead);
		out.writeInt(isSent);
	}

	public static final Parcelable.Creator<ChatMsg> CREATOR = new Parcelable.Creator<ChatMsg>() {
		@Override
		public ChatMsg createFromParcel(Parcel in) {
			return new ChatMsg(in);
		}

		@Override
		public ChatMsg[] newArray(int size) {
			 return new ChatMsg[size];
//			throw new UnsupportedOperationException();
		}
	};

	@SuppressWarnings("unchecked")
	public ChatMsg(Parcel in) {
		id           = in.readString();
		masterId     = in.readString();
        
		masterName   = in.readString();
		slaveId      = in.readString();
		slaveName    = in.readString();

		content      = in.readString();
		sendOrGet    = in.readInt();
		msgType      = in.readInt();
        chatMsgTime  = DateTimeHelper.parseDateFromStr(in.readString(), "yyyy-MM-dd HH:mm:ss");
        status       = in.readInt();
        isUnRead     = in.readInt();
        isSent       = in.readInt();
	}
}
