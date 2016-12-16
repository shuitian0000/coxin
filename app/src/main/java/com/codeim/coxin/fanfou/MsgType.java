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

import com.codeim.coxin.db.MessageTable;
import com.codeim.coxin.db.TwitterDatabase;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;
//import com.codeim.coxin.db.UserInfoTable;

/**
 * A data class representing Basic info information element
 */
//public class Info extends WeiboResponse implements java.io.Serializable {
public class MsgType {

	private String id;
	private String MsgTypeTitle;
	private String MsgTypeStatus;
	private int MsgTypeImageUrl;

	/* package */public MsgType(JSONObject json) throws HttpException {
		super();
		Log.d("MsgType", "json before init");
		init(json);
	}

	/* package */MsgType(Response res) throws HttpException {
		super();
		init(res.asJSONObject());
	}

	MsgType() {

	}

	private void init(JSONObject json) throws HttpException {
		try {
			id = json.getString("id");
			Log.d("MsgType_id:", id);
			
			MsgTypeTitle = json.getString("title");
			MsgTypeStatus = json.getString("status");
			MsgTypeImageUrl = json.getInt("image");
		} catch (JSONException jsone) {
			throw new HttpException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/**
	 * Returns the id of the Info
	 *
	 * @return the id of the Info
	 */
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return MsgTypeTitle;
	}
	
	public String getStatus() {
		return MsgTypeStatus;
	}
	
	public int getImageUrl() {
		return MsgTypeImageUrl;
	}

	public static List<MsgType> constructMsgTypes(Response res) throws HttpException {
		try {
			JSONArray list = res.asJSONArray();
			int size = list.length();
			List<MsgType> msgTypes = new ArrayList<MsgType>(size);
			for (int i = 0; i < size; i++) {
				msgTypes.add(new MsgType(list.getJSONObject(i)));
			}
			return msgTypes;
		} catch (JSONException jsone) {
			throw new HttpException(jsone);
		} catch (HttpException te) {
			throw te;
		}
	}

	/**
	 * @param res
	 * @return
	 * @throws HttpException
	 */
	static List<MsgType> constructResult(Response res) throws HttpException {
		JSONArray list = res.asJSONArray();
		try {
			int size = list.length();
			List<MsgType> msgTypes = new ArrayList<MsgType>(size);
			for (int i = 0; i < size; i++) {
				msgTypes.add(new MsgType(list.getJSONObject(i)));
			}
			return msgTypes;
		} catch (JSONException e) {
		}
		return null;
	}


	@Override
	public String toString() {
		return "MsgType{" + ", id=" + id + ", title='" + MsgTypeTitle + ", status=" + MsgTypeStatus
		 + ", imageUrl=" + MsgTypeImageUrl +'}';
	}

	public com.codeim.coxin.data.MsgType parseMsgType() {
		com.codeim.coxin.data.MsgType msgType = new com.codeim.coxin.data.MsgType();
	        msgType.id               = this.id;          
	        msgType.MsgTypeTitle     = this.MsgTypeTitle;     
	        msgType.MsgTypeStatus    = this.MsgTypeStatus;   
	        msgType.MsgTypeImageUrl  = this.MsgTypeImageUrl;
		
		return msgType;
	}

}