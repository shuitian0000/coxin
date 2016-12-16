package com.codeim.coxin.data;

import java.util.ArrayList;
import java.util.Date;

import com.codeim.coxin.util.DateTimeHelper;

import android.os.Parcel;
import android.os.Parcelable;

public class Info implements Parcelable {

	public String id;
	public String context;
	public Date createdAt;
	public Date expireTime;
	public String status;
	public int praiseCount;
	public int user_praise;
	
	public double latitude;
	public double longitude;
	public String  location;
	public int distance;
	
	public String owerId;
	public String owerName;
	public String owerGender;
	public String owerImageUrl;
	
	public String attachmentUrl;
	public int conversationCount;
	
	public int expire = 0;
	
	public ArrayList<String> PicPathList;

	public Info() {
	}

	public static Info create(com.codeim.coxin.fanfou.Info u) {
		Info info = new Info();
		
            info.id           = u.getId();
            info.context      = u.getContext();
            info.createdAt    = u.getCreatedAt();
            info.expireTime   = u.getExpireTime();
            info.createdAt    = u.getCreatedAt();
            info.expireTime   = u.getExpireTime();
            info.status       = u.getStatus();
            info.praiseCount  = u.getPraiseCount();
            info.user_praise  = u.getUserPraise();
        
            info.latitude     = u.getLatitude();
            info.longitude    = u.getLongitude();
            info.location     = u.getLocation();
            info.distance     = u.getDistance();
          
            info.owerId       = u.getUserId();
            info.owerName     = u.getName();
            info.owerGender   = u.getGender();
            info.owerImageUrl = u.getImageURL().toString();
            
            info.attachmentUrl = u.getAttachmentUrl().toString();
            info.conversationCount=u.getConversationCount();
            info.PicPathList=u.getPicPathList();
            
            info.expire = 0;

		return info;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
	
		out.writeString(id);
		out.writeString(context);
		out.writeString(status);
		out.writeString(DateTimeHelper.dateToString(createdAt, "yyyy-MM-dd HH:mm:ss"));
		out.writeString(DateTimeHelper.dateToString(expireTime, "yyyy-MM-dd HH:mm:ss"));
		out.writeInt(praiseCount);
		out.writeInt(user_praise);
		
		out.writeString(location);
		out.writeInt(distance);
		
		out.writeString(owerId);
		out.writeString(owerName);
		out.writeString(owerGender);
		out.writeString(owerImageUrl);
		
		out.writeString(attachmentUrl);
		out.writeInt(conversationCount);
//		out.writeStringList(PicPathList);
		out.writeList(PicPathList);
	}

	public static final Parcelable.Creator<Info> CREATOR = new Parcelable.Creator<Info>() {
		@Override
		public Info createFromParcel(Parcel in) {
			return new Info(in);
		}

		@Override
		public Info[] newArray(int size) {
			 return new Info[size];
//			throw new UnsupportedOperationException();
		}
	};

	@SuppressWarnings("unchecked")
	public Info(Parcel in) {
		
		id           = in.readString();
		context      = in.readString();
		status       = in.readString();
		createdAt    = DateTimeHelper.parseDateFromStr(in.readString(), "yyyy-MM-dd HH:mm:ss");
		expireTime    = DateTimeHelper.parseDateFromStr(in.readString(), "yyyy-MM-dd HH:mm:ss");
		praiseCount  = in.readInt();
		user_praise  = in.readInt();
		 
		location     = in.readString();
		distance     = in.readInt();
		 
		owerId       = in.readString();
		owerName     = in.readString();
		owerGender   = in.readString();
		owerImageUrl = in.readString();
		
		attachmentUrl= in.readString();
		conversationCount=in.readInt();
		PicPathList = in.readArrayList(String.class.getClassLoader());

	}
	
	public ArrayList<String> getPicPathList() {
		ArrayList<String> picPath = new ArrayList<String>();
		picPath.addAll(PicPathList);
		return picPath;
	}
}
