package com.codeim.coxin.data;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class MsgType implements Parcelable {

	public String id;
	public String MsgTypeTitle;
	public String MsgTypeStatus;
	public int  MsgTypeImageUrl;

	public MsgType() {
	}

	public static MsgType create(com.codeim.coxin.fanfou.MsgType u) {
	    MsgType msgType = new MsgType();
		
	    msgType.id              = u.getId();
	    msgType.MsgTypeTitle    = u.getTitle();
	    msgType.MsgTypeStatus   = u.getStatus();
	    msgType.MsgTypeImageUrl = u.getImageUrl();

	    return msgType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
	
		out.writeString(id);
		out.writeString(MsgTypeTitle);
		out.writeString(MsgTypeStatus);
		out.writeInt(MsgTypeImageUrl);
		
	}

	public static final Parcelable.Creator<MsgType> CREATOR = new Parcelable.Creator<MsgType>() {
		public MsgType createFromParcel(Parcel in) {
			return new MsgType(in);
		}

		public MsgType[] newArray(int size) {
			// return new User[size];
			throw new UnsupportedOperationException();
		}
	};

	public MsgType(Parcel in) {
		
		id               = in.readString();
		MsgTypeTitle     = in.readString();
		MsgTypeStatus    = in.readString();
		MsgTypeImageUrl  = in.readInt();
	}
}
