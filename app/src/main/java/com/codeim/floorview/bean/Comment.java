/**
 * 
 */
package com.codeim.floorview.bean;

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

import android.util.Log;

import com.codeim.coxin.data.Info;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;
import com.codeim.coxin.http.ResponseException;

/**
 * @ClassName: 	Comment
 * @Description:The basic class of Comment
 * @author 	codeimShieh
 * @date	Feb 14, 2014		3:14:16 PM
 */
public class Comment {
    
    public static final  long NULL_PARENT = ( long ) - 1 ;
    
    private long parentId ;
    private long userId ;
    private long id ;
    private String content ;
    private String userName ;
    private Date date ;
    /*private int floorCount ;    //妤煎眰鏁扮洰
*/    private int floorNum ;      //楼层，从1开始
    private int lastNumForThisInfo;
    private boolean is_available;
    
    private ArrayList<String> PicPathList;
    
    public Info info=null;
    //for pinned section listview
    public boolean section=false;
    public boolean progress_bar  = false;
    
    public Comment ( long userId, long id, String content, String userName, Date date) {
        this.userId = userId ;
        this.id = id ;
        this.content = content ;
        this.userName = userName ;
        this.date = date ;
        this.parentId =  NULL_PARENT ;
        this.floorNum = 1 ;
        this.lastNumForThisInfo=1;
        this.is_available = true;
        this.PicPathList = new ArrayList<String> ();
        
        this.info = null;
        this.section = false;
        this.progress_bar = false;
    }
    
    public Comment ( long parent_id, long user_id, long ID, String content, String userName, Date date, int floorNum
    		, int lastNumForThisInfo, boolean is_available) {
        this.parentId = parent_id ;
        this.userId = user_id ;
        this.id = ID ;
        this.content = content ;
        this.userName = userName ;
        this.date = date ;
        this.floorNum = floorNum ;
        this.lastNumForThisInfo = floorNum;
        this.is_available = is_available;
        
        this.info = null;
        this.section = false;
        this.progress_bar = false;
    }
    
    public Comment (Comment u) {
        this.parentId = u.parentId ;
        this.userId = u.userId ;
        this.id = u.id ;
        this.content = u.content ;
        this.userName = u.userName ;
        this.date = u.date ;
        this.floorNum = u.floorNum ;
        this.lastNumForThisInfo = u.lastNumForThisInfo;
        this.is_available = u.is_available;
        this.PicPathList = u.PicPathList;
        
        this.info = null;
        this.section = false;
        this.progress_bar = false;
    }
    
	/* package */public Comment(JSONObject json) throws HttpException {
		super();
		Log.d("Comment", "json before init");
		init(json);
	}
	private void init(JSONObject json) throws HttpException {
		try {
			id = json.getInt("id");
			Log.d("comment_id:", String.valueOf(id));
			
			parentId = json.getInt("parentId");
			content = json.getString("content");
			userName = json.getString("userName");
			floorNum = json.getInt("floorNum");
			lastNumForThisInfo = json.getInt("lastNumForThisInfo");
			userId = json.getInt("userId");
			date = parseDate(json.getString("date"), "yyyy-MM-dd HH:mm:ss");
			is_available = (json.getInt("available")>0);
			
			JSONArray PathList =json.getJSONArray("PicPathList");
			PicPathList = new ArrayList<String> ();
			if(PathList!=null && PathList.length()>0) {
			    int size = PathList.length();
			    for(int i=0; i<size; i++) {
				    PicPathList.add(PathList.get(i).toString());
			    }
			}
			
		} catch (JSONException jsone) {
			throw new HttpException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}
    
    public long getParentId () {
        return parentId ;
    }
    public long getUserId () {
        return userId ;
    }
    public long getId () {
        return id ;
    }
    public String getContent () {
        return content ;
    }
    public String getUserName () {
        return userName ;
    }
    public Date getDate () {
        return date ;
    }
    public int getFloorNum () {
        return floorNum ;
    }
    public int getLastNumForThisInfo() {
    	return lastNumForThisInfo;
    }
    public boolean isAvailable () {
        return is_available ;
    }
    public ArrayList<String> getPicPath () {
    	return PicPathList;
    }
    
    public Info getInfoFromComment () {
    	return info;
    }
    public boolean isSection() {
    	return section;
    }
    
	public static List<Comment> constructComments(Response res) throws HttpException {
		try {
			JSONArray list = res.asJSONArray();
			int size = list.length();
			List<Comment> comments = new ArrayList<Comment>(size);
			for (int i = 0; i < size; i++) {
				comments.add(new Comment(list.getJSONObject(i)));
			}
			return comments;
		} catch (JSONException jsone) {
			throw new HttpException(jsone);
		} catch (HttpException te) {
			throw te;
		}
	}
	
	public static Comment constructComment(Response res) throws HttpException {
		try {
			Comment comment = new Comment(res.asJSONObject());
			

			return comment;
		} catch (ResponseException jsone) {
			throw new HttpException(jsone);
		} catch (HttpException te) {
			throw te;
		}
	}
	
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
}
