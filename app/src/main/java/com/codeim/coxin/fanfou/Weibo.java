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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
// import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.http.HttpClient;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;
import com.codeim.floorview.bean.Comment;

//import eriji.com.oauth.OAuthStoreException;

//public class Weibo extends WeiboSupport implements java.io.Serializable {
public class Weibo implements java.io.Serializable {
	public static final String TAG = "Weibo_API";
	
	protected HttpClient http = null;
	protected String source = Configuration.getSource();
	protected final boolean USE_SSL;

	public static final String APP_SOURCE = Configuration.getSource();
	public static final String CONSUMER_KEY = Configuration.getOAuthConsumerKey();
	public static final String CONSUMER_SECRET = Configuration.getOAuthConsumerSecret();

//	private String baseURL =  "http://www.imyouliao.com/statusnet/api/";        // Configuration.getScheme() + "api.fanfou.com/"
	private String baseURL =  "http://52.33.47.51/";//"http://121.199.73.2/";//"http://webst.sinaapp.com/";//"121.199.73.2/";        // Configuration.getScheme() + "api.fanfou.com/"
	private String searchBaseURL = "http://www.imyouliao.com/statusnet/api/";   // Configuration.getScheme() + "api.fanfou.com/"
	private static final long serialVersionUID = -1486360080128882436L;

	public Weibo() {
//		super(); // In case that the user is not logged in
		USE_SSL = Configuration.useSSL();
		http = new HttpClient(); // In case that the user is not logged in
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public Weibo(String userId, String password) {
//		super(userId, password);
		USE_SSL = Configuration.useSSL();
		http = new HttpClient(userId, password);
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public Weibo(String userId, String password, String baseURL) {
		this(userId, password);
		this.baseURL = baseURL;
	}

	/**
	 * 设置HttpClient的Auth，为请求做准备
	 * 
	 * @param username
	 * @param password
	 */
	public void setCredentials(String username, String password) {
		http.setCredentials(username, password);
	}

	/**
	 * 仅判断是否为空
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public static boolean isValidCredentials(String username, String password) {
		//不再判断密码是否为空的情况
		return !TextUtils.isEmpty(username);// && !TextUtils.isEmpty(password);
	}

	/**
	 * 在服务器上验证用户名/密码是否正确，成功则返回该用户信息，失败则抛出异常。
	 * 
	 * @param username
	 * @param password
	 * @return Verified User
	 * @throws HttpException
	 *             验证失败及其他非200响应均抛出异常
	 * @throws OAuthStoreException
	 */
	public User login(String username, String password) throws HttpException {
		Log.d(TAG, "Login attempt for " + username);
		Log.d(TAG, "Login password " + password);
		
		http.setCredentials(username, password);
   
		/*
		try {
			// 进行XAuth认证。
			((XAuthClient) http.getOAuthClient()).retrieveAccessToken(username, password);
		} catch (Exception e) {
			// TODO: XAuth认证不管是userName/password错，还是appKey错都是返回401 unauthorized
			// 但是会返回一个xml格式的error信息，格式如下：
			// <hash><request></request><error></error></hash>
			throw new HttpAuthException(e.getMessage(), e);
		}
		*/
		
		// FIXME: 这里重复进行了认证，为历史遗留原因, 留下的唯一原因时该方法需要返回一个User实例
	    User user = verifyCredentials(); // Verify userName and password

		return user;
	}
	
	public JSONObject getWeiboUserInfo(String url) throws HttpException {
	    // ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		// params.add(new BasicNameValuePair("source", source));
		// params.add(new BasicNameValuePair("uid", uid));
		JSONObject jsonData = http.get(url).asJSONObject();
		return jsonData;
	}
	
	public JSONObject loginWeibo(String token, String expires_in, String uid, String userName, String gender) 
	        throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("token", token));
		params.add(new BasicNameValuePair("expires_in", expires_in));
		params.add(new BasicNameValuePair("uid", uid));
		params.add(new BasicNameValuePair("username", userName));
		params.add(new BasicNameValuePair("gender", gender));
		return get(getBaseURL() + "statusnet/account/loginsinaweibo.json", params, false).asJSONObject();
	}
	
	public Response register(boolean license, String username, String password, String confirmPassword, int gender, 
	        double latitude, double longitude) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("license", String.valueOf(license)));
		params.add(new BasicNameValuePair("nickname", username));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("confirm", confirmPassword));
		params.add(new BasicNameValuePair("gender", String.valueOf(gender)));
		// params.add(new BasicNameValuePair("location", latitude + "," + longitude));
		params.add(new BasicNameValuePair("lat", String.valueOf(latitude)));
		params.add(new BasicNameValuePair("long", String.valueOf(longitude)));
		//return  http.post(getBaseURL() + "statusnet/register.json", params);
		return  http.post(getBaseURL() + "account/register.php", params);
	}
	
//	public Response sendInfo(boolean license, String infoContext, String InfoPlace, 
//			   double latitude, double longitude) throws HttpException {
//		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("infoContext", infoContext));
//		params.add(new BasicNameValuePair("InfoPlace", InfoPlace));
//		params.add(new BasicNameValuePair("lat", String.valueOf(latitude)));
//		params.add(new BasicNameValuePair("long", String.valueOf(longitude)));
//		
//		return  http.post(getBaseURL() + "flymsg/sendinfo.php", params);
//	}
	public com.codeim.coxin.fanfou.Info sendInfo(boolean license, String infoContext, String InfoPlace, 
			   String addTime, double latitude, double longitude) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("infoContext", infoContext));
		params.add(new BasicNameValuePair("InfoPlace", InfoPlace));
		params.add(new BasicNameValuePair("addTime", addTime));
		params.add(new BasicNameValuePair("lat", String.valueOf(latitude)));
		params.add(new BasicNameValuePair("long", String.valueOf(longitude)));
		
		Response res = http.post(getBaseURL() + "flymsg/sendinfo.php", params);
		return  new Info(res.asJSONObject());
	}
	
	public Comment sendComment(boolean license, String commentContext, int info_id, int parent_id, int floornum,
			String commentPlace, double latitude, double longitude) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("commentContext", commentContext));
		params.add(new BasicNameValuePair("infoid", String.valueOf(info_id)));
		params.add(new BasicNameValuePair("parentid", String.valueOf(parent_id)));
		params.add(new BasicNameValuePair("floornum", String.valueOf(floornum)));
		params.add(new BasicNameValuePair("commentPlace", commentPlace));
		params.add(new BasicNameValuePair("lat", String.valueOf(latitude)));
		params.add(new BasicNameValuePair("long", String.valueOf(longitude)));
		
		return  Comment.constructComment(http.post(getBaseURL() + "flymsg/sendcomment.php", params));
	}
	

	/**
	 * Reset HttpClient's Credentials
	 */
	public void reset() {
		http.reset();
	}

	/**
	 * Whether Logged-in
	 * 
	 * @return
	 */
	public boolean isLoggedIn() {
		// HttpClient的userName&password是由TwitterApplication#onCreate
		// 从SharedPreferences中取出的，他们为空则表示尚未登录，因为他们只在验证
		// 账户成功后才会被储存，且注销时被清空。
		return isValidCredentials(http.getUserId(), http.getPassword());
	}

	/**
	 * Sets the base URL
	 * 
	 * @param baseURL
	 *            String the base URL
	 */
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	/**
	 * Returns the base URL
	 * 
	 * @return the base URL
	 */
	public String getBaseURL() {
		return this.baseURL;
	}

	/**
	 * Sets the search base URL
	 * 
	 * @param searchBaseURL
	 *            the search base URL
	 * @since fanfoudroid 0.5.0
	 */
	public void setSearchBaseURL(String searchBaseURL) {
		this.searchBaseURL = searchBaseURL;
	}

	/**
	 * Returns the search base url
	 * 
	 * @return search base url
	 * @since fanfoudroid 0.5.0
	 */
	public String getSearchBaseURL() {
		return this.searchBaseURL;
	}

	/**
	 * Returns authenticating userid 注意：此userId不一定等同与饭否用户的user_id参数
	 * 它可能是任意一种当前用户所使用的ID类型（如邮箱，用户名等），
	 * 
	 * @return userid
	 */
	public String getUserId() {
		return http.getUserId();
	}

	/**
	 * Returns authenticating password
	 * 
	 * @return password
	 */
	public String getPassword() {
		return http.getPassword();
	}
	
	// Low-level interface
	public HttpClient getHttpClient() {
		return http;
	}
	/**
	 * Issues an HTTP GET request.
	 * 
	 * @param url
	 *            the request url
	 * @param authenticate
	 *            if true, the request will be sent with BASIC authentication
	 *            header
	 * @return the response
	 * @throws HttpException
	 */

	protected Response get(String url, boolean authenticate) throws HttpException {
		return get(url, null, authenticate);
	}

	/**
	 * Issues an HTTP GET request.
	 * 
	 * @param url
	 *            the request url
	 * @param authenticate
	 *            if true, the request will be sent with BASIC authentication
	 *            header
	 * @param name1
	 *            the name of the first parameter
	 * @param value1
	 *            the value of the first parameter
	 * @return the response
	 * @throws HttpException
	 */

	protected Response get(String url, String name1, String value1, boolean authenticate) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(name1, HttpClient.encode(value1)));
		return get(url, params, authenticate);
	}

	/**
	 * Issues an HTTP GET request.
	 * 
	 * @param url
	 *            the request url
	 * @param name1
	 *            the name of the first parameter
	 * @param value1
	 *            the value of the first parameter
	 * @param name2
	 *            the name of the second parameter
	 * @param value2
	 *            the value of the second parameter
	 * @param authenticate
	 *            if true, the request will be sent with BASIC authentication
	 *            header
	 * @return the response
	 * @throws HttpException
	 */

	protected Response get(String url, String name1, String value1, String name2, String value2, boolean authenticate)
			throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(name1, HttpClient.encode(value1)));
		params.add(new BasicNameValuePair(name2, HttpClient.encode(value2)));
		return get(url, params, authenticate);
	}

	/**
	 * Issues an HTTP GET request.
	 * 
	 * @param url
	 *            the request url
	 * @param params
	 *            the request parameters
	 * @param authenticate
	 *            if true, the request will be sent with BASIC authentication
	 *            header
	 * @return the response
	 * @throws HttpException
	 */
	protected Response get(String url, ArrayList<BasicNameValuePair> params, boolean authenticated) throws HttpException {
		
		/*
		if (url.indexOf("?") == -1) {
			url += "?source=" + APP_SOURCE;
		} else if (url.indexOf("source") == -1) {
			url += "&source=" + APP_SOURCE;
		}
		*/

		// 以HTML格式获得数据，以便进一步处理
		//url += "?format=html"; // "&format=html"

		if (null != params && params.size() > 0) {
			url += "?" + HttpClient.encodeParameters(params); // url += "&" + HttpClient.encodeParameters(params);
		}

		return http.get(url, authenticated);
	}

	/**
	 * Issues an HTTP GET request.
	 * 
	 * @param url
	 *            the request url
	 * @param params
	 *            the request parameters
	 * @param paging
	 *            controls pagination
	 * @param authenticate
	 *            if true, the request will be sent with BASIC authentication
	 *            header
	 * @return the response
	 * @throws HttpException
	 */
	protected Response get(String url, ArrayList<BasicNameValuePair> params, Paging paging, boolean authenticate) throws HttpException {
		if (null == params) {
			params = new ArrayList<BasicNameValuePair>();
		}

		if (null != paging) {
			if ("" != paging.getMaxId()) {
				params.add(new BasicNameValuePair("max_id", String.valueOf(paging.getMaxId())));
			}
			if ("" != paging.getSinceId()) {
				params.add(new BasicNameValuePair("since_id", String.valueOf(paging.getSinceId())));
			}
			if (-1 != paging.getPage()) {
				params.add(new BasicNameValuePair("page", String.valueOf(paging.getPage())));
			}
			if (-1 != paging.getCount()) {
				params.add(new BasicNameValuePair("count", String.valueOf(paging.getCount())));
			}

			return get(url, params, authenticate);
		} else {
			return get(url, params, authenticate);
		}
	}

	/**
	 * 生成POST Parameters助手
	 * 
	 * @param nameValuePair
	 *            参数(一个或多个)
	 * @return post parameters
	 */
	public ArrayList<BasicNameValuePair> createParams(BasicNameValuePair... nameValuePair) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		for (BasicNameValuePair param : nameValuePair) {
			params.add(param);
		}
		return params;
	}

	/***************** API METHOD START *********************/

	/* 搜索相关的方法 */

	/**
	 * Returns tweets that match a specified query. <br>
	 * This method calls http://api.fanfou.com/users/search.format
	 * 
	 * @param query
	 *            - the search condition
	 * @return the result
	 * @throws HttpException
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public QueryResult search(Query query) throws HttpException {
//		try {
//			return new QueryResult(get(searchBaseURL + "search/public_timeline.json", query.asPostParameters(), false), this);
//		} catch (HttpException te) {
//			if (404 == te.getStatusCode()) {
//				return new QueryResult(query);
//			} else {
//				throw te;
//			}
//		}
//	}

	/**
	 * Returns the top ten topics that are currently trending on Weibo. The
	 * response includes the time of the request, the name of each trend.
	 * 
	 * @return the result
	 * @throws HttpException
	 * @since fanfoudroid 0.5.0
	 */
//	public Trends getTrends() throws HttpException {
//		return Trends.constructTrends(get(searchBaseURL + "trends.json", false));
//	}

	/*
	private String toDateStr(Date date) {
		if (null == date) {
			date = new Date();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
	*/

	/* 消息相关的方法 */

	/**
	 * Returns the 20 most recent statuses from non-protected users who have set
	 * a custom user icon. <br>
	 * This method calls http://api.fanfou.com/statuses/public_timeline.format
	 * 
	 * @return list of statuses of the Public Timeline
	 * @throws HttpException
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getPublicTimeline() throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/public_timeline.json", false));
//	}
//
//	public RateLimitStatus getRateLimitStatus() throws HttpException {
//		return new RateLimitStatus(get(getBaseURL() + "account/rate_limit_status.json", false), this);
//	}

	/**
	 * Returns the 20 most recent statuses, including retweets, posted by the
	 * authenticating user and that user's friends. This is the equivalent of
	 * /timeline/home on the Web. <br>
	 * This method calls http://api.fanfou.com/statuses/home_timeline.format
	 * 
	 * @return list of the home Timeline
	 * @throws HttpException
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
//	public List<Status> getHomeTimeline() throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.json", false));
//	}

	/**
	 * Returns the 20 most recent statuses, including retweets, posted by the
	 * authenticating user and that user's friends. This is the equivalent of
	 * /timeline/home on the Web. <br>
	 * This method calls http://api.fanfou.com/statuses/home_timeline.format
	 * 
	 * @param paging
	 *            controls pagination
	 * @return list of the home Timeline
	 * @throws HttpException
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
//	public List<Status> getHomeTimeline(Paging paging) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/home_timeline.json", null, paging, false));
//	}

	/**
	 * Returns the 20 most recent statuses posted in the last 24 hours from the
	 * authenticating1 user and that user's friends. It's also possible to
	 * request another user's friends_timeline via the id parameter below. <br>
	 * This method calls http://api.fanfou.com/statuses/friends_timeline.format
	 * 
	 * @return list of the Friends Timeline
	 * @throws HttpException
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getFriendsTimeline() throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json", false));
//	}

	/**
	 * Returns the 20 most recent statuses posted in the last 24 hours from the
	 * specified userid. <br>
	 * This method calls http://api.fanfou.com/statuses/friends_timeline.format
	 * 
	 * @param paging
	 *            controls pagination
	 * @return list of the Friends Timeline
	 * @throws HttpException
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getFriendsTimeline(Paging paging) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json", null, paging, false));
//	}

	/**
	 * Returns friend time line by page and count. <br>
	 * This method calls http://api.fanfou.com/statuses/friends_timeline.format
	 * 
	 * @param page
	 * @param count
	 * @return
	 * @throws HttpException
	 */
//	public List<Status> getFriendsTimeline(int page, int count) throws HttpException {
//		Paging paging = new Paging(page, count);
//		return Status.constructStatuses(get(getBaseURL() + "statuses/friends_timeline.json", null, paging, false));
//	}

	/**
	 * Returns the most recent statuses posted in the last 24 hours from the
	 * specified userid. <br>
	 * This method calls http://api.fanfou.com/statuses/user_timeline.format
	 * 
	 * @param id
	 *            specifies the ID or screen name of the user for whom to return
	 *            the user_timeline
	 * @param paging
	 *            controls pagenation
	 * @return list of the user Timeline
	 * @throws HttpException
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getUserTimeline(String id, Paging paging, int attr) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".json", 
//		        createParams(new BasicNameValuePair("attr", String.valueOf(attr))), paging, http.isAuthenticationEnabled()));
//	}

	/**
	 * Returns the most recent statuses posted in the last 24 hours from the
	 * specified userid. <br>
	 * This method calls http://api.fanfou.com/statuses/user_timeline.format
	 * 
	 * @param id
	 *            specifies the ID or screen name of the user for whom to return
	 *            the user_timeline
	 * @return the 20 most recent statuses posted in the last 24 hours from the
	 *         user
	 * @throws HttpException
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getUserTimeline(String id, int attr) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline/" + id + ".json", 
//		        createParams(new BasicNameValuePair("attr", String.valueOf(attr))), http.isAuthenticationEnabled()));
//	}

	/**
	 * Returns the most recent statuses posted in the last 24 hours from the
	 * authenticating user. <br>
	 * This method calls http://api.fanfou.com/statuses/user_timeline.format
	 * 
	 * @return the 20 most recent statuses posted in the last 24 hours from the
	 *         user
	 * @throws HttpException
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getUserTimeline() throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json", false));
//	}

	/**
	 * Returns the most recent statuses posted in the last 24 hours from the
	 * authenticating user. <br>
	 * This method calls http://api.fanfou.com/statuses/user_timeline.format
	 * 
	 * @param paging
	 *            controls pagination
	 * @return the 20 most recent statuses posted in the last 24 hours from the
	 *         user
	 * @throws HttpException
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
//	public List<Status> getUserTimeline(Paging paging) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json", null, paging, false));
//	}
//
//	public List<Status> getUserTimeline(int page, int count) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/user_timeline.json", null, new Paging(page, count), false));
//	}

	/**
	 * Returns the 20 most recent mentions (status containing @username) for the
	 * authenticating user. <br>
	 * This method calls http://api.fanfou.com/statuses/mentions.format
	 * 
	 * @return the 20 most recent replies
	 * @throws HttpException
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getMentions() throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json", null, false));
//	}
//
//	// by since_id
//	public List<Status> getMentions(String since_id) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json", "since_id", String.valueOf(since_id), false));
//	}

	/**
	 * Returns the 20 most recent mentions (status containing @username) for the
	 * authenticating user. <br>
	 * This method calls http://api.fanfou.com/statuses/mentions.format
	 * 
	 * @param paging
	 *            controls pagination
	 * @return the 20 most recent replies
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getMentions(Paging paging) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statuses/mentions.json", null, paging, false));
//	}

	/**
	 * Returns a single status, specified by the id parameter. The status's
	 * author will be returned inline. <br>
	 * This method calls http://api.fanfou.com/statuses/show/id.format
	 * 
	 * @param id
	 *            the numerical ID of the status you're trying to retrieve
	 * @return a single status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable.
	 *             可能因为“你没有通过这个用户的验证“,返回403
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public Status showStatus(String id) throws HttpException {
//		return new Status(get(getBaseURL() + "statuses/show/" + id + ".json", false));
//	}

	/**
	 * Updates the user's status. The text will be trimed if the length of the
	 * text is exceeding 160 characters. <br>
	 * This method calls http://api.fanfou.com/statuses/update.format
	 * 
	 * @param status
	 *            the text of your status update
	 * @return the latest status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public Status updateStatus(String status) throws HttpException {
//		return new Status(http.post(getBaseURL() + "statuses/update.json", 
//		        createParams(new BasicNameValuePair("status", status), new BasicNameValuePair("source", source))));
//	}

	/**
	 * Updates the user's status. The text will be trimed if the length of the
	 * text is exceeding 160 characters. <br>
	 * 发布消息 http://api.fanfou.com/statuses/update.[json|xml]
	 * 
	 * @param status
	 *            the text of your status update
	 * @param latitude
	 *            The location's latitude that this tweet refers to.
	 * @param longitude
	 *            The location's longitude that this tweet refers to.
	 * @return the latest status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
//	public Status updateStatus(String status, double latitude, double longitude) throws HttpException, JSONException {
//		return new Status(http.post(getBaseURL() + "statuses/update.json", 
//		        createParams(new BasicNameValuePair("status", status), new BasicNameValuePair("source", source), 
//				/*new BasicNameValuePair("location", latitude + "," + longitude)*/
//				new BasicNameValuePair("lat", String.valueOf(latitude)), new BasicNameValuePair("long", String.valueOf(longitude)))));
//	}

	/**
	 * Updates the user's status. 如果要使用inreplyToStatusId参数, 那么该status就必须得是@别人的.
	 * The text will be trimed if the length of the text is exceeding 160
	 * characters. <br>
	 * 发布消息 http://api.fanfou.com/statuses/update.[json|xml]
	 * 
	 * @param status
	 *            the text of your status update
	 * @param inReplyToStatusId
	 *            The ID of an existing status that the status to be posted is
	 *            in reply to. This implicitly sets the in_reply_to_user_id
	 *            attribute of the resulting status to the user ID of the
	 *            message being replied to. Invalid/missing status IDs will be
	 *            ignored.
	 * @return the latest status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public Status updateStatus(String status, String inReplyToStatusId) throws HttpException {
//		return new Status(http.post(getBaseURL() + "statuses/update.json", 
//		        createParams(new BasicNameValuePair("status", status), new BasicNameValuePair("source", source), 
//				new BasicNameValuePair("in_reply_to_status_id", inReplyToStatusId))));
//	}

	/**
	 * Updates the user's status. The text will be trimed if the length of the
	 * text is exceeding 160 characters. <br>
	 * 发布消息 http://api.fanfou.com/statuses/update.[json|xml]
	 * 
	 * @param status
	 *            the text of your status update
	 * @param inReplyToStatusId
	 *            The ID of an existing status that the status to be posted is
	 *            in reply to. This implicitly sets the in_reply_to_user_id
	 *            attribute of the resulting status to the user ID of the
	 *            message being replied to. Invalid/missing status IDs will be
	 *            ignored.
	 * @param latitude
	 *            The location's latitude that this tweet refers to.
	 * @param longitude
	 *            The location's longitude that this tweet refers to.
	 * @return the latest status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public Status updateStatus(String status, String inReplyToStatusId, double latitude, double longitude) throws HttpException {
//		return new Status(http.post(getBaseURL() + "statuses/update.json", 
//		        createParams(new BasicNameValuePair("status", status), new BasicNameValuePair("source", source), 
//				/*new BasicNameValuePair("location", latitude + "," + longitude),*/
//				new BasicNameValuePair("lat", String.valueOf(latitude)), new BasicNameValuePair("long", String.valueOf(longitude)),
//				new BasicNameValuePair( "in_reply_to_status_id", inReplyToStatusId))));
//	}

	/**
	 * upload the photo. The text will be trimed if the length of the text is
	 * exceeding 160 characters. The image suport. <br>
	 * 上传照片 http://api.fanfou.com/photos/upload.[json|xml]
	 * 
	 * @param status
	 *            the text of your status update
	 * @return the latest status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public Status uploadPhoto(String status, File file) throws HttpException {
//		return new Status(http.httpRequest(getBaseURL() + "photos/upload.json", 
//		        createParams(new BasicNameValuePair("status", status), 
//				new BasicNameValuePair("source", source)), file, false, HttpPost.METHOD_NAME));
//	}
	/****
	 * 
	 * @param info_or_comment:  1: info; 2: comment
	 * @param status
	 * @param file
	 * @return
	 * @throws HttpException
	 */
	public Photo uploadPhoto(int info_or_comment, String status, File file) throws HttpException {
	return new Photo(http.httpRequestImage(getBaseURL() + "flymsg/sendphoto.php",
	        createParams(new BasicNameValuePair("INFO_OR_COMMENT", String.valueOf(info_or_comment)),
	        		new BasicNameValuePair("status", status), 
			new BasicNameValuePair("source", source)), file, false, HttpPost.METHOD_NAME));
   }

//	public Status updateStatus(String status, File file) throws HttpException {
//		return uploadPhoto(status, file);
//	}
	
	/**
	 *上传语音
	 */
//	public Status updateStatus(String status, File file, String duration, int type, double latitude, double longitude)
//        	throws HttpException {
//		return uploadAudio(status, file, duration, type, latitude, longitude);
//	}
	
//	public Status uploadAudio(String status, File file, String duration, int type, double latitude, double longitude) 
//	        throws HttpException {
//		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("status", status));
//		params.add(new BasicNameValuePair("source", source));
//		params.add(new BasicNameValuePair("audio_duration", duration));
//		params.add(new BasicNameValuePair("type", String.valueOf(type)));
//		params.add(new BasicNameValuePair("lat", String.valueOf(latitude)));
//		params.add(new BasicNameValuePair("long", String.valueOf(longitude)));
//		return new Status(http.httpRequest(getBaseURL() + "statuses/update.json", params, file, false, HttpPost.METHOD_NAME));
//	}
	
//	public Status updateStatusComment(String status, File file, String duration, int type, String inReplyToStatusId, 
//	        double latitude, double longitude) throws HttpException {
//		return uploadAudioComment(status, file, duration, type, inReplyToStatusId, latitude, longitude);
//	}
	
//	public Status uploadAudioComment(String status, File file, String duration, int type, String inReplyToStatusId, 
//	        double latitude, double longitude) throws HttpException {
//		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("status", status));
//		params.add(new BasicNameValuePair("source", source));
//		params.add(new BasicNameValuePair("audio_duration", duration));
//		params.add(new BasicNameValuePair("type", String.valueOf(type)));
//		params.add(new BasicNameValuePair("in_reply_to_status_id", inReplyToStatusId));
//		params.add(new BasicNameValuePair("lat", String.valueOf(latitude)));
//		params.add(new BasicNameValuePair("long", String.valueOf(longitude)));
//		return new Status(http.httpRequest(getBaseURL() + "statuses/update.json", params, file, false, HttpPost.METHOD_NAME));
//	}
	
	/**
	 *上传头像
	 */
	public User updateAvatar(File imageFile) throws HttpException {
	    return new User(http.httpRequestImage(getBaseURL() + "account/upload_profile_image.php", 
		createParams(new BasicNameValuePair("source", source)), imageFile, false, HttpPost.METHOD_NAME));
	}

	/**
	 * Destroys the status specified by the required ID parameter. The
	 * authenticating user must be the author of the specified status. <br>
	 * 删除消息 http://api.fanfou.com/statuses/destroy.[json|xml]
	 * 
	 * @param statusId
	 *            The ID of the status to destroy.
	 * @return the deleted status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since 1.0.5
	 */
//	public Status destroyStatus(String statusId) throws HttpException {
//		return new Status(http.post(getBaseURL() + "statuses/destroy/" + statusId + ".json", createParams(), false));
//	}
	public JSONObject deleteOneInfo(String infoId) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("infoid", infoId));
		return  http.post(getBaseURL() + "flymsg/delete_info.php", params).asJSONObject();
    }
	public JSONObject updateInfoExpire(String infoId, String TAG, String updateTime, String addTime) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("infoid", infoId));
		params.add(new BasicNameValuePair("TAG", TAG));
		params.add(new BasicNameValuePair("updateTime", updateTime));
		params.add(new BasicNameValuePair("addTime", addTime));
		return  http.post(getBaseURL() + "flymsg/update_info_expire.php", params).asJSONObject();
    }

	public JSONObject reportInfo(String infoId, String reportContent) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("infoid", infoId));
		params.add(new BasicNameValuePair("reportContent", reportContent));
		return  http.post(getBaseURL() + "flymsg/report_info.php", params).asJSONObject();
    }
	
	public JSONObject getInfoExpire(String infoId) throws HttpException {
		return get(getBaseURL() + "flymsg/get_info_expire.php", 
		        createParams(new BasicNameValuePair("infoId", infoId)), false).asJSONObject();
	}

	/**
	 * Returns extended information of a given user, specified by ID or screen
	 * name as per the required id parameter below. This information includes
	 * design settings, so third party developers can theme their widgets
	 * according to a given user's preferences. <br>
	 * This method calls http://api.fanfou.com/users/show.format
	 * 
	 * @param id
	 *            (cann't be screenName the ID of the user for whom to request
	 *            the detail
	 * @return User
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
	public User showUser(String id) throws HttpException {
		return new User(get(getBaseURL() + "account/show.php", createParams(new BasicNameValuePair("id", id)), false));
	}

	/**
	 * Return a status of repost
	 * 
	 * @param to_user_name
	 *            repost status's user name
	 * @param repost_status_id
	 *            repost status id
	 * @param repost_status_text
	 *            repost status text
	 * @param new_status
	 *            the new status text
	 * @return a single status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public Status repost(String to_user_name, String repost_status_id, String repost_status_text, String new_status) throws HttpException {
//		StringBuilder sb = new StringBuilder();
//		sb.append(new_status);
//		sb.append(" ");
//		sb.append(R.string.pref_rt_prefix_default + "：@");
//		sb.append(to_user_name);
//		sb.append(" ");
//		sb.append(repost_status_text);
//		sb.append(" ");
//		String message = sb.toString();
//		return new Status(http.post(getBaseURL() + "statuses/update.json", 
//		        createParams(new BasicNameValuePair("status", message), 
//				new BasicNameValuePair("repost_status_id", repost_status_id)), false));
//	}

	/**
	 * Return a status of repost
	 * 
	 * @param to_user_name
	 *            repost status's user name
	 * @param repost_status_id
	 *            repost status id
	 * @param repost_status_text
	 *            repost status text
	 * @param new_status
	 *            the new status text
	 * @return a single status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public Status repost(String new_status, String repost_status_id) throws HttpException {
//		return new Status(http.post(getBaseURL() + "statuses/update.json", 
//		        createParams(new BasicNameValuePair("status", new_status), new BasicNameValuePair("source", APP_SOURCE), 
//				new BasicNameValuePair("repost_status_id", repost_status_id)), false));
//	}

	/**
	 * Return a status of repost
	 * 
	 * @param repost_status_id
	 *            repost status id
	 * @param repost_status_text
	 *            repost status text
	 * @return a single status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public Status repost(String repost_status_id, String new_status, boolean tmp) throws HttpException {
//		Status repost_to = showStatus(repost_status_id);
//		String to_user_name = repost_to.getUser().getName();
//		String repost_status_text = repost_to.getText();
//
//		return repost(to_user_name, repost_status_id, repost_status_text, new_status);
//	}

	/* User Methods */

	/**
	 * Returns the specified user's friends, each with current status inline. <br>
	 * This method calls http://api.fanfou.com/statuses/friends.format
	 * 
	 * @return the list of friends
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
	public List<User> getFriendsStatuses() throws HttpException {
		return User.constructResult(get(getBaseURL() + "users/friends.json", false));
	}

	/**
	 * Returns the specified user's friends, each with current status inline. <br>
	 * This method calls http://api.fanfou.com/statuses/friends.format <br>
	 * 分页每页显示100条
	 * 
	 * @param paging
	 *            controls pagination
	 * @return the list of friends
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * 
	 */
	public List<User> getFriendsStatuses(Paging paging) throws HttpException {
		return User.constructUsers(get(getBaseURL() + "users/friends.json", null, paging, false));
	}

	/**
	 * Returns the user's friends, each with current status inline. <br>
	 * This method calls http://api.fanfou.com/statuses/friends.format
	 * 
	 * @param id
	 *            the ID or screen name of the user for whom to request a list
	 *            of friends
	 * @return the list of friends
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
	public List<User> getFriendsStatuses(String id) throws HttpException {
		return User.constructUsers(get(getBaseURL() + "users/friends.json", 
		        createParams(new BasicNameValuePair("id", id)), false));
	}

	/**
	 * Returns the user's friends, each with current status inline. <br>
	 * This method calls http://api.fanfou.com/statuses/friends.format
	 * 
	 * @param id
	 *            the ID or screen name of the user for whom to request a list
	 *            of friends
	 * @param paging
	 *            controls pagination (饭否API 默认返回 100 条/页)
	 * @return the list of friends
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public List<User> getFriendsStatuses(String id, Paging paging) throws HttpException {
		//return User.constructUsers(get(getBaseURL() + "users/friends.json",
		//		createParams(new BasicNameValuePair("id", id)), paging, false));
		return User.constructUsers(get(getBaseURL() + "statuses/friends.json",
				createParams(new BasicNameValuePair("id", id)), paging, false));
	}

	/**
	 * Returns the authenticating user's followers, each with current status
	 * inline. They are ordered by the order in which they joined Weibo (this is
	 * going to be changed). <br>
	 * This method calls http://api.fanfou.com/statuses/followers.format
	 * 
	 * @return List
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
	public List<User> getFollowersStatuses() throws HttpException {
		return User.constructResult(get(getBaseURL() + "statuses/followers.json", false));
	}

	/**
	 * Returns the authenticating user's followers, each with current status
	 * inline. They are ordered by the order in which they joined Weibo (this is
	 * going to be changed). <br>
	 * This method calls http://api.fanfou.com/statuses/followers.format
	 * 
	 * @param paging
	 *            controls pagination
	 * @return List
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public List<User> getFollowersStatuses(Paging paging) throws HttpException {
		return User.constructUsers(get(getBaseURL() + "statuses/followers.json", null, paging, false));
	}

	/**
	 * Returns the authenticating user's followers, each with current status
	 * inline. They are ordered by the order in which they joined Weibo (this is
	 * going to be changed). <br>
	 * This method calls http://api.fanfou.com/statuses/followers.format
	 * 
	 * @param id
	 *            The ID (not screen name) of the user for whom to request a
	 *            list of followers.
	 * @return List
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public List<User> getFollowersStatuses(String id) throws HttpException {
		return User.constructUsers(get(getBaseURL() + "statuses/followers/" + id + ".json", false));
	}

	/**
	 * Returns the authenticating user's followers, each with current status
	 * inline. They are ordered by the order in which they joined Weibo (this is
	 * going to be changed). <br>
	 * This method calls http://api.fanfou.com/statuses/followers.format
	 * 
	 * @param id
	 *            The ID or screen name of the user for whom to request a list
	 *            of followers.
	 * @param paging
	 *            controls pagination
	 * @return List
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public List<User> getFollowersStatuses(String id, Paging paging) throws HttpException {
		return User.constructUsers(get(getBaseURL() + "statuses/followers/" + id + ".json", null, paging, false));
	}

	/* 私信功能 */

	/**
	 * Sends a new direct message to the specified user from the authenticating
	 * user. Requires both the user and text parameters below. The text will be
	 * trimed if the length of the text is exceeding 140 characters. <br>
	 * This method calls http://api.fanfou.com/direct_messages/new.format <br>
	 * 通过客户端只能给互相关注的人发私信
	 * 
	 * @param id
	 *            the ID of the user to whom send the direct message
	 * @param text
	 *            String
	 * @return DirectMessage
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public DirectMessage sendDirectMessage(String id, String text) throws HttpException {
		return new DirectMessage(http.post(getBaseURL() + "direct_messages/new.json", 
		        createParams(new BasicNameValuePair("user", id), new BasicNameValuePair("text", text))).asJSONObject());
	}

	// TODO: need be unit tested by in_reply_to_id.
	/**
	 * Sends a new direct message to the specified user from the authenticating
	 * user. Requires both the user and text parameters below. The text will be
	 * trimed if the length of the text is exceeding 140 characters. <br>
	 * 通过客户端只能给互相关注的人发私信
	 * 
	 * @param id
	 * @param text
	 * @param in_reply_to_id
	 * @return
	 * @throws HttpException
	 */
	public DirectMessage sendDirectMessage(String id, String text, String in_reply_to_id) throws HttpException {
		return new DirectMessage(http.post(getBaseURL() + "direct_messages/new.json", 
		        createParams(new BasicNameValuePair("user", id), new BasicNameValuePair("text", text), 
				new BasicNameValuePair("is_reply_to_id", in_reply_to_id))).asJSONObject());
	}

	/**
	 * Destroys the direct message specified in the required ID parameter. The
	 * authenticating user must be the recipient of the specified direct
	 * message. <br>
	 * This method calls http://api.fanfou.com/direct_messages/destroy/id.format
	 * 
	 * @param id
	 *            the ID of the direct message to destroy
	 * @return the deleted direct message
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
	public DirectMessage destroyDirectMessage(String id) throws HttpException {
		return new DirectMessage(http.post(getBaseURL() + "direct_messages/destroy/" + id + ".json", false).asJSONObject());
	}

	/**
	 * Returns a list of the direct messages sent to the authenticating user. <br>
	 * This method calls http://api.fanfou.com/direct_messages.format
	 * 
	 * @return List
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public List<DirectMessage> getDirectMessages() throws HttpException {
		return DirectMessage.constructDirectMessages(get(getBaseURL() + "direct_messages.json", false));
	}

	/**
	 * Returns a list of the direct messages sent to the authenticating user. <br>
	 * This method calls http://api.fanfou.com/direct_messages.format
	 * 
	 * @param paging
	 *            controls pagination
	 * @return List
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public List<DirectMessage> getDirectMessages(Paging paging) throws HttpException {
		return DirectMessage.constructDirectMessages(get(getBaseURL() + "direct_messages.json", null, paging, false));
	}

	/**
	 * Returns a list of the direct messages sent by the authenticating user. <br>
	 * This method calls http://api.fanfou.com/direct_messages/sent.format
	 * 
	 * @return List
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public List<DirectMessage> getSentDirectMessages() throws HttpException {
		return DirectMessage.constructDirectMessages(get(getBaseURL() + "direct_messages/sent.json", null, false));
	}

	/**
	 * Returns a list of the direct messages sent by the authenticating user. <br>
	 * This method calls http://api.fanfou.com/direct_messages/sent.format
	 * 
	 * @param paging
	 *            controls pagination
	 * @return List 默认返回20条, 一次最多返回60条
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public List<DirectMessage> getSentDirectMessages(Paging paging) throws HttpException {
		return DirectMessage.constructDirectMessages(get(getBaseURL() + "direct_messages/sent.json", null, paging, false));
	}

	/* 收藏功能 */

	/**
	 * Returns the 20 most recent favorite statuses for the authenticating user
	 * or user specified by the ID parameter in the requested format.
	 * 
	 * @return List<Status>
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
//	public List<Status> getFavorites() throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "favorites.json", createParams(), false));
//	}
//
//	public List<Status> getFavorites(Paging paging) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "favorites.json", createParams(), paging, false));
//	}

	/**
	 * Returns the 20 most recent favorite statuses for the authenticating user
	 * or user specified by the ID parameter in the requested format.
	 * 
	 * @param page
	 *            the number of page
	 * @return List<Status>
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
//	public List<Status> getFavorites(int page) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "favorites.json", "page", String.valueOf(page), false));
//	}

	/**
	 * Returns the 20 most recent favorite statuses for the authenticating user
	 * or user specified by the ID parameter in the requested format.
	 * 
	 * @param id
	 *            the ID or screen name of the user for whom to request a list
	 *            of favorite statuses
	 * @return List<Status>
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 * @since fanfoudroid 0.5.0
	 */
//	public List<Status> getFavorites(String id) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".json", createParams(), false));
//	}

	/**
	 * Returns the 20 most recent favorite statuses for the authenticating user
	 * or user specified by the ID parameter in the requested format.
	 * 
	 * @param id
	 *            the ID or screen name of the user for whom to request a list
	 *            of favorite statuses
	 * @param page
	 *            the number of page
	 * @return List<Status>
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public List<Status> getFavorites(String id, int page) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".json", "page", String.valueOf(page), false));
//	}
//
//	public List<Status> getFavorites(String id, Paging paging) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "favorites/" + id + ".json", null, paging, false));
//	}

	/**
	 * Favorites the status specified in the ID parameter as the authenticating
	 * user. Returns the favorite status when successful.
	 * 
	 * @param id
	 *            the ID of the status to favorite
	 * @return Status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public Status createFavorite(String id) throws HttpException {
//		return new Status(http.post(getBaseURL() + "favorites/create/" + id + ".json", false));
//	}

	/**
	 * Un-favorites the status specified in the ID parameter as the
	 * authenticating user. Returns the un-favorited status in the requested
	 * format when successful.
	 * 
	 * @param id
	 *            the ID of the status to un-favorite
	 * @return Status
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public Status destroyFavorite(String id) throws HttpException {
//		return new Status(http.post(getBaseURL() + "favorites/destroy/" + id + ".json", false));
//	}
//	
	/* 评论功能 */
	
//	public List<Status> getComment(String id, Paging paging) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statusnet/replytostatusid/" + id + ".json", createParams(), paging, false));
//	}
//	
//	public List<Status> getConversation(String id, Paging paging) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statusnet/conversation/" + id + ".json", createParams(), paging, false));
//	}

	/**
	 * Enables notifications for updates from the specified user to the
	 * authenticating user. Returns the specified user when successful.
	 * 
	 * @param id
	 *            String
	 * @return User
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @deprecated 饭否该功能暂时关闭, 等待该功能开放.
	 */
	public User enableNotification(String id) throws HttpException {
		return new User(http.post(getBaseURL() + "notifications/follow/" + id + ".json", false).asJSONObject());
	}

	/**
	 * Disables notifications for updates from the specified user to the
	 * authenticating user. Returns the specified user when successful.
	 * 
	 * @param id
	 *            String
	 * @return User
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @deprecated 饭否该功能暂时关闭, 等待该功能开放.
	 * @since fanfoudroid 0.5.0
	 */
	public User disableNotification(String id) throws HttpException {
		return new User(http.post(getBaseURL() + "notifications/leave/" + id + ".json", false).asJSONObject());
	}

	/* 黑名单 */

	/**
	 * Blocks the user specified in the ID parameter as the authenticating user.
	 * Returns the blocked user in the requested format when successful.
	 * 
	 * @param id
	 *            the ID or screen_name of the user to block
	 * @return the blocked user
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
	public User createBlock(String id) throws HttpException {
		return new User(http.post(getBaseURL() + "blocks/create/" + id + ".json", false).asJSONObject());
	}

	/**
	 * Un-blocks the user specified in the ID parameter as the authenticating
	 * user. Returns the un-blocked user in the requested format when
	 * successful.
	 * 
	 * @param id
	 *            the ID or screen_name of the user to block
	 * @return the unblocked user
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
	public User destroyBlock(String id) throws HttpException {
		return new User(http.post(getBaseURL() + "blocks/destroy/" + id + ".json", false).asJSONObject());
	}

	/**
	 * Tests if a friendship exists between two users.
	 * 
	 * @param id
	 *            The ID or screen_name of the potentially blocked user.
	 * @return if the authenticating user is blocking a target user
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @deprecated 饭否暂无此功能, 期待此功能
	 * @since fanfoudroid 0.5.0
	 */
	public boolean existsBlock(String id) throws HttpException {
		try {
			return -1 == get(getBaseURL() + "blocks/exists/" + id + ".json", false).asString().indexOf(
					"<error>You are not blocking this user.</error>");
		} catch (HttpException te) {
			if (te.getStatusCode() == 404) {
				return false;
			}
			throw te;
		}
	}

	/**
	 * Returns a list of user objects that the authenticating user is blocking.
	 * 
	 * @return a list of user objects that the authenticating user
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @deprecated 饭否暂无此功能, 期待此功能
	 * @since fanfoudroid 0.5.0
	 */
	public List<User> getBlockingUsers() throws HttpException {
		return User.constructUsers(get(getBaseURL() + "blocks/blocking.json", false));
	}

	/**
	 * Returns a list of user objects that the authenticating user is blocking.
	 * 
	 * @param page
	 *            the number of page
	 * @return a list of user objects that the authenticating user
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @deprecated 饭否暂无此功能, 期待此功能
	 * @since fanfoudroid 0.5.0
	 */
	public List<User> getBlockingUsers(int page) throws HttpException {
		return User.constructUsers(get(getBaseURL() + "blocks/blocking.json?page=" + page, false));
	}

	/**
	 * Returns an array of numeric user ids the authenticating user is blocking.
	 * 
	 * @return Returns an array of numeric user ids the authenticating user is
	 *         blocking.
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @deprecated 饭否暂无此功能, 期待此功能
	 * @since fanfoudroid 0.5.0
	 */
//	public IDs getBlockingUsersIDs() throws HttpException {
//		return new IDs(get(getBaseURL() + "blocks/blocking/ids.json", false), this);
//	}

	/* 好友关系方法 */

	/**
	 * Tests if a friendship exists between two users.
	 * 
	 * @param userA
	 *            The ID or screen_name of the first user to test friendship
	 *            for.
	 * @param userB
	 *            The ID or screen_name of the second user to test friendship
	 *            for.
	 * @return if a friendship exists between two users.
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public boolean existsFriendship(String userA, String userB) throws HttpException {
		return -1 != get(getBaseURL() + "friendships/exists.json", "user_a",
				userA, "user_b", userB, false).asString().indexOf("true");
	}

	/**
	 * Discontinues friendship with the user specified in the ID parameter as
	 * the authenticating user. Returns the un-friended user in the requested
	 * format when successful. Returns a string describing the failure condition
	 * when unsuccessful.
	 * 
	 * @param id
	 *            the ID or screen name of the user for whom to request a list
	 *            of friends
	 * @return User
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public User destroyFriendship(String id) throws HttpException {
		return new User(http.post(getBaseURL() + "friendships/destroy/" + id + ".json", createParams(), false).asJSONObject());
	}

	/**
	 * Befriends the user specified in the ID parameter as the authenticating
	 * user. Returns the befriended user in the requested format when
	 * successful. Returns a string describing the failure condition when
	 * unsuccessful.
	 * 
	 * @param id
	 *            the ID or screen name of the user to be befriended
	 * @return the befriended user
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public User createFriendship(String id) throws HttpException {
		return new User(http.post(getBaseURL() + "friendships/create/" + id + ".json", createParams(), false).asJSONObject());
	}

	/**
	 * Returns an array of numeric IDs for every user the specified user is
	 * followed by.
	 * 
	 * @param userId
	 *            Specifies the ID of the user for whom to return the followers
	 *            list.
	 * @param cursor
	 *            Specifies the page number of the results beginning at 1. A
	 *            single page contains 5000 ids. This is recommended for users
	 *            with large ID lists. If not provided all ids are returned.
	 * @return The ID or screen_name of the user to retrieve the friends ID list
	 *         for.
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since Weibo4J 2.0.10
	 * @see <a
	 *      href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids
	 *      </a>
	 */
//	public IDs getFollowersIDs(String userId) throws HttpException {
//		return new IDs(get(getBaseURL() + "followers/ids.json?user_id=" + userId, false), this);
//	}

	/**
	 * Returns an array of numeric IDs for every user the specified user is
	 * followed by.
	 * 
	 * @param cursor
	 *            Specifies the page number of the results beginning at 1. A
	 *            single page contains 5000 ids. This is recommended for users
	 *            with large ID lists. If not provided all ids are returned.
	 * @return The ID or screen_name of the user to retrieve the friends ID list
	 *         for.
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since Weibo4J 2.0.10
	 * @see <a
	 *      href="http://open.t.sina.com.cn/wiki/index.php/Followers/ids">followers/ids
	 *      </a>
	 */
//	public IDs getFollowersIDs() throws HttpException {
//		return new IDs(get(getBaseURL() + "followers/ids.json", false), this);
//	}

	public List<com.codeim.coxin.fanfou.User> getFollowersList(String userId, Paging paging) throws HttpException {
		//return User.constructUsers(get(getBaseURL() + "users/followers.json",
		//		createParams(new BasicNameValuePair("id", userId)), paging, false));
		return User.constructUsers(get(getBaseURL() + "statuses/followers.json",
				createParams(new BasicNameValuePair("id", userId)), paging, false));
	}

	public List<com.codeim.coxin.fanfou.User> getFollowersList(String userId) throws HttpException {
		return User.constructUsers(get(getBaseURL() + "users/followers.json",
				createParams(new BasicNameValuePair("id", userId)), false));
	}
	
	public List<com.codeim.coxin.fanfou.User> getFlymsgRefreshLocation(int refreshFrequency, String sexType, double lat, double lng) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("refreshFrequency", String.valueOf(refreshFrequency)));
		params.add(new BasicNameValuePair("sexType", sexType));
		params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		params.add(new BasicNameValuePair("lng", String.valueOf(lng)));
		
	    return User.constructUsers(get(getBaseURL() + "flymsg/refresh_location.json", params, false));
	}
	
	//add by wangyw
//	public List<com.codeim.coxin.fanfou.Info> getFlyInfomsgRefreshLocation(int refreshFrequency, String infoType, double lat, double lng) throws HttpException {
	public List<com.codeim.coxin.fanfou.Info> getFlyInfomsgRefreshLocation(int page_size, int page_index, int last_id, String infoType, double lat, double lng) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("page_size", String.valueOf(page_size)));
		params.add(new BasicNameValuePair("page_index", String.valueOf(page_index)));
		params.add(new BasicNameValuePair("last_id", String.valueOf(last_id)));
		params.add(new BasicNameValuePair("infoType", infoType));
		params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		params.add(new BasicNameValuePair("lng", String.valueOf(lng)));
		
	    return Info.constructInfos(get(getBaseURL() + "flymsg/refresh_location_info.php", params, false));
	}
	public List<com.codeim.coxin.fanfou.Info> getFlyInfomsgMySend(int page_size, int page_index, int last_id, String infoType, double lat, double lng) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("page_size", String.valueOf(page_size)));
		params.add(new BasicNameValuePair("page_index", String.valueOf(page_index)));
		params.add(new BasicNameValuePair("last_id", String.valueOf(last_id)));
		params.add(new BasicNameValuePair("infoType", infoType));
		params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		params.add(new BasicNameValuePair("lng", String.valueOf(lng)));
		
	    return Info.constructInfos(get(getBaseURL() + "flymsg/refresh_my_send_info.php", params, false));
	}
	public List<com.codeim.coxin.fanfou.Info> getFlyInfomsgMyReply(int page_size, int page_index, int last_id, String infoType, double lat, double lng) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("page_size", String.valueOf(page_size)));
		params.add(new BasicNameValuePair("page_index", String.valueOf(page_index)));
		params.add(new BasicNameValuePair("last_id", String.valueOf(last_id)));
		params.add(new BasicNameValuePair("infoType", infoType));
		params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		params.add(new BasicNameValuePair("lng", String.valueOf(lng)));
		
	    return Info.constructInfos(get(getBaseURL() + "flymsg/refresh_my_reply_info.php", params, false));
	}
	
	public com.codeim.coxin.fanfou.Info getInfoById(String infoId) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("infoId", infoId));
		
		JSONObject json = get(getBaseURL() + "flymsg/get_info_byid.php", params, false).asJSONObject();
		
		return (new Info(json));
	}
	
	public JSONObject toogleInfoPraise(String info_id, int now_available) throws HttpException {
		
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("info_id", info_id));
		params.add(new BasicNameValuePair("is_available", String.valueOf(now_available)));
		
//		url += "?" + HttpClient.encodeParameters(params);
		
		JSONObject jsonData = http.get(getBaseURL() + "flymsg/toggle_praise_info.php" + "?" + HttpClient.encodeParameters(params), false).asJSONObject();
		return  jsonData;
	}
	
	//add by wangyw
//	public List<com.codeim.floorview.Comment> getFlyCommentRefresh(int refreshFrequency, int info_id) throws HttpException {
	public List<com.codeim.floorview.bean.Comment> getFlyCommentRefresh(int page_size, int page_index,int last_id,int info_id) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("page_size", String.valueOf(page_size)));
		params.add(new BasicNameValuePair("page_index", String.valueOf(page_index)));
		params.add(new BasicNameValuePair("last_id", String.valueOf(last_id)));
		params.add(new BasicNameValuePair("info_id", String.valueOf(info_id)));
		
	    return Comment.constructComments(get(getBaseURL() + "flymsg/get_comment_info.php", params, false));
	}
	
    //use Friend for friend. just use id,otherId,otherName,otherImageUrl
	//friend relationship, pull from web
	public List<com.codeim.coxin.fanfou.Chat> getChatsFromLocal(int page_size, int page_index,String last_time,String user_id) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("page_size", String.valueOf(page_size)));
		params.add(new BasicNameValuePair("page_index", String.valueOf(page_index)));
		params.add(new BasicNameValuePair("last_time", String.valueOf(last_time)));
		params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
		
		//for temporary
		return Chat.constructChats(get(getBaseURL() + "flymsg/get_chat_group.php", params, false));
	}
	public List<com.codeim.coxin.fanfou.ChatMsg> getChatMsgFromLocal(int page_size, int page_index,int last_id,
																	 String chatGrpId, String slaveId, String masterId) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("page_size", String.valueOf(page_size)));
		params.add(new BasicNameValuePair("page_index", String.valueOf(page_index)));
		params.add(new BasicNameValuePair("last_id", String.valueOf(last_id)));
		params.add(new BasicNameValuePair("chatGrpId", chatGrpId));
		params.add(new BasicNameValuePair("slaveId", slaveId));
		params.add(new BasicNameValuePair("masterId", masterId));

		//for temporary
		List<com.codeim.coxin.fanfou.ChatMsg> chatMsgsList_temp=ChatMsg.parseChatMsgList(TwitterApplication.mDb.getChatMsgByIdFromLocal(page_size, page_index,last_id
				,slaveId, masterId));
		if(chatMsgsList_temp.isEmpty() || chatMsgsList_temp.size()<page_size) {
			com.codeim.coxin.fanfou.ChatMsg u = new ChatMsg();
			u.setId("-1");
			chatMsgsList_temp.add(u);
		}
		return chatMsgsList_temp;
	}
	
    //use Friend for friend. just use id,otherId,otherName,otherImageUrl
	//friend relationship, pull from web
	public List<com.codeim.coxin.fanfou.Friend> getFriendsFromLocal(int page_size, int page_index,int last_id,String user_id) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("page_size", String.valueOf(page_size)));
		params.add(new BasicNameValuePair("page_index", String.valueOf(page_index)));
		params.add(new BasicNameValuePair("last_id", String.valueOf(last_id)));
		params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
		
		return com.codeim.coxin.fanfou.Friend.parseUserList(TwitterApplication.mDb.getContactById(page_size, page_index,last_id,user_id), 
				user_id);
		//the follow is pull from  web, but this function need pull from local database
		//return Friend.constructFriends(get(getBaseURL() + "flymsg/get_friends.php", params, false));
	}
	public List<com.codeim.coxin.fanfou.Friend> getFriendsFromWeb(int page_size, int page_index,int last_id,String user_id) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("page_size", String.valueOf(page_size)));
		params.add(new BasicNameValuePair("page_index", String.valueOf(page_index)));
		params.add(new BasicNameValuePair("last_id", String.valueOf(last_id)));
		params.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
		
//		return com.codeim.coxin.fanfou.Friend.parseUserList(TwitterApplication.mDb.getContactById(page_size, page_index,last_id,user_id), 
//				user_id);
		////the follow is pull from  web, but this function need pull from local database
		return Friend.constructFriends(get(getBaseURL() + "flymsg/get_friends.php", params, false));
	}
	public JSONObject deleteOneFriend(String friendId, String userId) throws HttpException {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("friendId", friendId));
		params.add(new BasicNameValuePair("userId", userId));
		return  http.post(getBaseURL() + "flymsg/delete_friend.php", params).asJSONObject();
    }
	public com.codeim.coxin.fanfou.Friend isFriend(String ownerId, String slaveId) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("ownerId", ownerId));
		params.add(new BasicNameValuePair("slaveId", slaveId));

		JSONObject json = get(getBaseURL() + "flymsg/is_friend.php", params, false).asJSONObject();
		return (new Friend(json));
	}
	public com.codeim.coxin.fanfou.Friend createFriend(String ownerId, String slaveId) throws HttpException {
	    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("ownerId", ownerId));
		params.add(new BasicNameValuePair("slaveId", slaveId));

		JSONObject json = get(getBaseURL() + "flymsg/create_friend.php", params, false).asJSONObject();
		return (new Friend(json));
	}
	
//	public List<Status> getCategory(int category) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statusnet/categorynotice/" + category + ".json", false));
//	}
//	
//	public List<Status> getCategory(Paging paging, int category) throws HttpException {
//		return Status.constructStatuses(get(getBaseURL() + "statusnet/categorynotice/" + category + ".json", null, 
//		        paging, false));
//	}
//
//	public List<Status> getCategory(int page, int count, int category) throws HttpException {
//		Paging paging = new Paging(page, count);
//		return Status.constructStatuses(get(getBaseURL() + "statusnet/categorynotice/" + category + ".json", null, 
//		        paging, false));
//	}

	/**
	 * Returns an array of numeric IDs for every user the authenticating user is
	 * following.
	 * 
	 * @return an array of numeric IDs for every user the authenticating user is
	 *         following
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since androidroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public IDs getFriendsIDs() throws HttpException {
//		return getFriendsIDs(-1l);
//	}

	/**
	 * Returns an array of numeric IDs for every user the authenticating user is
	 * following. <br/>
	 * 饭否无cursor参数
	 * 
	 * @param cursor
	 *            Specifies the page number of the results beginning at 1. A
	 *            single page contains 5000 ids. This is recommended for users
	 *            with large ID lists. If not provided all ids are returned.
	 * @return an array of numeric IDs for every user the authenticating user is
	 *         following
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
//	public IDs getFriendsIDs(long cursor) throws HttpException {
//		return new IDs(get(getBaseURL() + "friends/ids.json?cursor=" + cursor, false), this);
//	}

	/**
	 * 获取关注者id列表
	 * 
	 * @param userId
	 * @return
	 * @throws HttpException
	 */
//	public IDs getFriendsIDs(String userId) throws HttpException {
//		return new IDs(get(getBaseURL() + "friends/ids.json?id=" + userId, false), this);
//	}

	/* 账户方法 */

	/**
	 * Returns an HTTP 200 OK response code and a representation of the
	 * requesting user if authentication was successful; returns a 401 status
	 * code and an error message if not. Use this method to test if supplied
	 * user credentials are valid. 注意： 如果使用 错误的用户名/密码 多次登录后，饭否会锁IP
	 * 返回提示为“尝试次数过多，请去 http://fandou.com 登录“,且需输入验证码
	 * 
	 * 登录成功返回 200 code 登录失败返回 401 code 使用HttpException的getStatusCode取得code
	 * 
	 * @return user
	 * @since androidroid 0.5.0
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @see <a
	 *      href="http://code.google.com/p/fanfou-api/wiki/ApiDocumentation"</a>
	 */
	public User verifyCredentials() throws HttpException {
		Response res;
		Log.d("Weibo", "before verifyCredentials get");
		res = get(getBaseURL() + "account/verify_credentials.php", false);
		//res = get(getBaseURL() + "index.php", false);
		Log.d("Weibo", "after verifyCredentials get");
		return new User(res.asJSONObject());
		//return new User(get(getBaseURL() + "account/verify_credentials.php", false).asJSONObject());  // true
		//return new User(get(getBaseURL() + "account/verify_credentials.json", false));
	}

	/* Saved Searches Methods */
	/**
	 * Returns the authenticated user's saved search queries.
	 * 
	 * @return Returns an array of numeric user ids the authenticating user is
	 *         blocking.
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public List<SavedSearch> getSavedSearches() throws HttpException {
//		return SavedSearch.constructSavedSearches(get(getBaseURL() + "saved_searches.json", false));
//	}

	/**
	 * Retrieve the data for a saved search owned by the authenticating user
	 * specified by the given id.
	 * 
	 * @param id
	 *            The id of the saved search to be retrieved.
	 * @return the data for a saved search
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public SavedSearch showSavedSearch(int id) throws HttpException {
//		return new SavedSearch(get(getBaseURL() + "saved_searches/show/" + id + ".json", false));
//	}

	/**
	 * Retrieve the data for a saved search owned by the authenticating user
	 * specified by the given id.
	 * 
	 * @return the data for a created saved search
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public SavedSearch createSavedSearch(String query) throws HttpException {
//		return new SavedSearch(http.post(getBaseURL() + "saved_searches/create.json",
//				createParams(new BasicNameValuePair("query", query)), false));
//	}

	/**
	 * Destroys a saved search for the authenticated user. The search specified
	 * by id must be owned by the authenticating user.
	 * 
	 * @param id
	 *            The id of the saved search to be deleted.
	 * @return the data for a destroyed saved search
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
//	public SavedSearch destroySavedSearch(int id) throws HttpException {
//		return new SavedSearch(http.post(getBaseURL() + "saved_searches/destroy/" + id + ".json", false));
//	}

	/* Help Methods */
	/**
	 * Returns the string "ok" in the requested format with a 200 OK HTTP status
	 * code.
	 * 
	 * @return true if the API is working
	 * @throws HttpException
	 *             when Weibo service or network is unavailable
	 * @since fanfoudroid 0.5.0
	 */
	public boolean test() throws HttpException {
		return -1 != get(getBaseURL() + "help/test.json", false).asString().indexOf("ok");
	}

	/***************** API METHOD END *********************/

	private SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Weibo weibo = (Weibo) o;

		if (!baseURL.equals(weibo.baseURL))
			return false;
		if (!format.equals(weibo.format))
			return false;
		if (!http.equals(weibo.http))
			return false;
		if (!searchBaseURL.equals(weibo.searchBaseURL))
			return false;
		if (!source.equals(weibo.source))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = http.hashCode();
		result = 31 * result + baseURL.hashCode();
		result = 31 * result + searchBaseURL.hashCode();
		result = 31 * result + source.hashCode();
		result = 31 * result + format.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "Weibo{" + "http=" + http + ", baseURL='" + baseURL + '\'' + ", searchBaseURL='" + searchBaseURL + '\'' + ", source='"
				+ source + '\'' + ", format=" + format + '}';
	}
	
	public static class Location {
        String geolat = null;
        String geolong = null;
        String geohacc = null;
        String geovacc = null;
        String geoalt = null;

        public Location() {
        }

        public Location(final String geolat, final String geolong, final String geohacc,
                final String geovacc, final String geoalt) {
            this.geolat = geolat;
            this.geolong = geolong;
            this.geohacc = geohacc;
            this.geovacc = geovacc;
            this.geoalt = geovacc;
        }

        public Location(final String geolat, final String geolong) {
            this(geolat, geolong, null, null, null);
        }
		
		public double getLat() {
		    return Double.parseDouble(geolat);
		}
		
		public double getLon() {
		    return Double.parseDouble(geolong);
		}
    }
}
