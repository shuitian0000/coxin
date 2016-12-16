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
package com.codeim.coxin.ui.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
//import com.codeim.coxin.R;
import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.db.StatusTable;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.hardware.ShakeListener;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.location.LocationUtils;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskManager;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.module.FlingGestureListener;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DebugTimer;
import com.codeim.coxin.widget.PullToRefreshListView;
import com.codeim.coxin.widget.PullToRefreshListView.OnRefreshListener;

//import com.codeim.coxin.data.User;
//import com.codeim.coxin.fanfou.IDs;
//import com.codeim.coxin.fanfou.Status;
//import com.codeim.coxin.ui.module.MyActivityFlipper;
//import com.codeim.coxin.ui.module.NearbyUserArrayAdapter;
//import com.hlidskialf.android.hardware.ShakeListener;
//import com.markupartist.android.widget.PullToRefreshListView;
//import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

public abstract class NearbyArrayBaseActivity extends NearbyListBaseActivity {
    static final String TAG = "NearbyArrayBaseActivity";
	
	protected static final String FEMALE = "0";
	protected static final String MALE = "1";
	protected static final String ALLUSER = "2";
	
	protected static final String ALLINFO = "0";
	
	protected String mNameType = "";  // 不同页面的标记，用于刷新计时
	volatile private int mRefreshFrequency = 0;
	protected String mSexType = ALLUSER;
	protected String mInfoType = ALLINFO;
	protected double mLat = 31.20021; //维度
	protected double mLng = 121.589149;//经度
	protected static int Nearby_Place=0;
	protected boolean isFirstLoc = true;
	protected static String name_txt="附近";
	
	protected int page_size;
	protected int page_index;
	protected int last_id;
	
	private SearchLocationObserver mSearchLocationObserver = new SearchLocationObserver();
	private LocationClient mLocClient;

    // Views.
    protected PullToRefreshListView mTweetList;  // 存放附近用户的列表
//    protected NearbyUserArrayAdapter mUserListAdapter;
    protected NearbyInfoArrayAdapter mInfoListAdapter;
    protected View mListHeader;
    protected View mListFooter;
    protected TextView loadMoreBtn;
    protected ProgressBar loadMoreGIF;
    protected TextView loadMoreBtnTop;
    protected ProgressBar loadMoreGIFTop;
    
    private volatile Thread thread_timer;
    private boolean timer_close;
    private boolean timer_destory;
    
    private OnScrollListener mOnScrollListener;
    private boolean mIsLoading;
    private int mCurrentScrollState;

    protected static int lastPosition = 0;
    protected static int scrollTop = 0;

    protected ShakeListener mShaker = null;
    
    // Tasks.
    protected TaskManager taskManager = new TaskManager();
    private GenericTask mRetrieveTask;
    private GenericTask mGetMoreTask;

    private int mRetrieveCount = 0;
    
    private boolean data_finish;
    private boolean no_data;
    
//    volatile private ArrayList<com.codeim.coxin.data.Info> allInfoList;
    volatile protected ArrayList<com.codeim.coxin.data.Info> allInfoList;

    private TaskListener mRetrieveTaskListener = new TaskAdapter() {

        @Override
        public String getName() {
            return "RetrieveTask";
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            // 刷新按钮停止旋转
            loadMoreGIF.setVisibility(View.GONE);
            mTweetList.onRefreshComplete();
            
            //add by ywwang for pull to getMore
            if(task == mGetMoreTask) {
            	onLoadMoreComplete();
            }

            if (result == TaskResult.AUTH_ERROR) {
                mFeedback.failed("登录信息出错");
                logout();
            } else if (result == TaskResult.OK) {
                draw();
                if (task == mRetrieveTask) {
                    goTop();
                }
            } else if (result == TaskResult.IO_ERROR) {
                // FIXME: bad smell
                if (task == mRetrieveTask) {
                    mFeedback.failed(((RetrieveTask) task).getErrorMsg());
                } else if (task == mGetMoreTask) {
                    mFeedback.failed(((GetMoreTask) task).getErrorMsg());
                }
            } else {
                // do nothing
            }
            
            // DEBUG
            if (TwitterApplication.DEBUG) {
                DebugTimer.stop();
                Log.v("DEBUG", DebugTimer.getProfileAsString());
            }
            
            end_thread();
            Log.v("taskadapter", "postexecute!");
        }

        @Override
        public void onPreExecute(GenericTask task) {
        	start_thread();
            mRetrieveCount = 0;
            mTweetList.prepareForRefresh();
            if (TwitterApplication.DEBUG) {
                DebugTimer.start();
            }
        }

        @Override
        public void onProgressUpdate(GenericTask task, Object param) {
            // Log.d(TAG, "onProgressUpdate");
            draw();
        }
    };

    // Refresh data at startup if last refresh was this long ago or greater.
    private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;
    // Refresh followers if last refresh was this long ago or greater.
    private static final long FOLLOWERS_REFRESH_THRESHOLD = 12 * 60 * 60 * 1000;

	public abstract String getUserId();
	
    // abstract protected void markAllRead();
    // abstract protected Cursor fetchMessages();
    // public abstract int getDatabaseType();
    // public abstract String fetchMaxId();
    // public abstract String fetchMinId();
    // public abstract int addMessages(ArrayList<Tweet> tweets, boolean isUnread);
    // public abstract List<Status> getMessageSinceId(String maxId) throws HttpException;
	// public abstract List<Status> getMoreMessageFromId(String minId) throws HttpException;
	
//	public abstract List<com.codeim.coxin.fanfou.Info> getNearbyInfo(int refreshFrequency, String infoType, 
//	        double lat, double lng) throws HttpException;
	public abstract List<com.codeim.coxin.fanfou.Info> getNearbyInfo(int page_size, int page_index, int last_id, String infoType, 
    double lat, double lng) throws HttpException;

    // public static final int CONTEXT_REPLY_ID = Menu.FIRST + 1;
    // public static final int CONTEXT_AT_ID = Menu.FIRST + 2;
    // public static final int CONTEXT_RETWEET_ID = Menu.FIRST + 3;
    // public static final int CONTEXT_DM_ID = Menu.FIRST + 4;
    // public static final int CONTEXT_MORE_ID = Menu.FIRST + 5;
    // public static final int CONTEXT_ADD_FAV_ID = Menu.FIRST + 6;
    // public static final int CONTEXT_DEL_FAV_ID = Menu.FIRST + 7;

    @Override
    protected void setupState() {
        setTitle(getActivityTitle());
        mTweetList = (PullToRefreshListView) findViewById(R.id.tweet_list);
        setupListHeader(true);
        // mTweetAdapter = new TweetCursorAdapter(this, cursor);
        // mTweetList.setAdapter(mTweetAdapter);
        allInfoList = new ArrayList<com.codeim.coxin.data.Info>();
		mInfoListAdapter = new NearbyInfoArrayAdapter(this);
		mTweetList.setAdapter(mInfoListAdapter);
		mTweetList.setHeaderDividersEnabled(false);
		// mTweetList <- mUserListAdapter <- allUserList
		// ListView   <- Adapter          <- Data
        // registerOnClickListener(mTweetList);
    }

    /**
     * 绑定listView底部 - 载入更多 NOTE: 必须在listView#setAdapter之前调用
     */
    protected void setupListHeader(boolean addFooter) {
        // Add Header to ListView
        // mListHeader = View.inflate(this, R.layout.listview_header, null);
        // mTweetList.addHeaderView(mListHeader, null, true);
    	mTweetList.setOnRefreshListener(new OnRefreshListener(){
    		@Override
    		public void onRefresh(){
    			doRetrieve();
    		}
    	});

        // Add Footer to ListView
        mListFooter = View.inflate(this, R.layout.listview_footer, null);
        mTweetList.addFooterView(mListFooter, null, true);
        
        // Find View
        loadMoreBtn = (TextView) findViewById(R.id.ask_for_more);
        loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);  
        loadMoreGIF.setPadding(0, 0, 15, 0);
        
        loadMoreBtn.setVisibility(View.GONE);  //add by ywwang for pull to getMore
        
        loadMoreBtnTop = (TextView) findViewById(R.id.ask_for_more_header);
        loadMoreGIFTop = (ProgressBar) findViewById(R.id.rectangleProgressBar_header);
        
    	page_size=1;
    	page_index=0;
    	last_id=0;
        data_finish = false;
        no_data=false;
        
        //add by ywwang for pull to getMore. 2015.04.01
        mTweetList.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            	mCurrentScrollState = scrollState;
                // Avoid override when use setOnScrollListener
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrollStateChanged(view, scrollState);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (mOnScrollListener != null) {
                    mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }

                // The count of footer view will be add to visibleItemCount also are
                // added to totalItemCount
                if (visibleItemCount == totalItemCount) {
                    // If all the item can not fill screen, we should make the
                    // footer view invisible.
//                	mListFooter.setVisibility(View.GONE);//hideFooterView();
                	
                    //add by ywwang for length screen, the item can not fill screen, but the data not all
                    if(!data_finish&&!mIsLoading) {
                    	mListFooter.setVisibility(View.VISIBLE);//showFooterView();
                        mIsLoading = true;
                        doGetMore();//temp method
                    } else {
                    	mListFooter.setVisibility(View.GONE);//hideFooterView();
                    }
                } else if (!mIsLoading && (firstVisibleItem + visibleItemCount >= totalItemCount)
                        && mCurrentScrollState != SCROLL_STATE_IDLE) {
                	mListFooter.setVisibility(View.VISIBLE);//showFooterView();
                    mIsLoading = true;
//                    if (mOnLoadMoreListener != null) {
//                        mOnLoadMoreListener.onLoadMore();
//                    }
                    doGetMore();
                }
            }
        });

    }
    
    public void onLoadMoreComplete() {
        mIsLoading = false;
        mListFooter.setVisibility(View.GONE);//hideFooterView();
    }

    @Override
    protected void specialItemClicked(int position) {
        // 注意 mTweetAdapter.getCount 和 mTweetList.getCount的区别
        // 前者仅包含数据的数量（不包括foot和head），后者包含foot和head
        // 因此在同时存在foot和head的情况下，list.count = adapter.count + 2
        if (position == 0) {
            // 第一个Item(header)
            loadMoreGIFTop.setVisibility(View.VISIBLE);
            doRetrieve();
        } 
//        else if (position == mTweetList.getCount() - 1) {
//            // 最后一个Item(footer)
//            loadMoreGIF.setVisibility(View.VISIBLE);
//            doGetMore();
//        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.newmain;
    }

    @Override
    protected ListView getTweetList() {
        return mTweetList;
    }
	
	@Override
    protected TweetAdapter getTweetAdapter() {
        return mInfoListAdapter;
    }

	/*
    @Override
    protected boolean useBasicMenu() {
        return true;
    }
	*/
	
    protected Info getListItem(int position) {
//        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mInfoListAdapter.getCount()) {
            Info info = (Info) mInfoListAdapter.getItem(position);
            if (info == null) {
                return null;
            } else {
                return info;
            }
        } else {
            return null;
        }
    }
	
	@Override
    protected Info getContextItemTweet(int position) {
        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mInfoListAdapter.getCount()) {
            Info info = (Info) mInfoListAdapter.getItem(position);
            if (info == null) {
                return null;
            } else {
                return info;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void updateTweet(Tweet tweet) {
        // TODO: updateTweet() 在哪里调用的? 目前尚只支持:
        // updateTweet(String tweetId, ContentValues values)
        // setFavorited(String tweetId, String isFavorited)
        // 看是否还需要增加updateTweet(Tweet tweet)方法

        // 对所有相关表的对应消息都进行刷新（如果存在的话）
        // getDb().updateTweet(TwitterDbAdapter.TABLE_FAVORITE, tweet);
        // getDb().updateTweet(TwitterDbAdapter.TABLE_MENTION, tweet);
        // getDb().updateTweet(TwitterDbAdapter.TABLE_TWEET, tweet);
    }

    @Override
    protected boolean _onCreate(Bundle savedInstanceState) {
        if (super._onCreate(savedInstanceState)) {

		    mLocClient = ((TwitterApplication)getApplication()).mLocationClient;  // 百度位置获取客户端
			
            boolean shouldRetrieve = false;
            // FIXME：该子类页面全部使用了这个统一的计时器，导致进入Mention等分页面后经常不会自动刷新
            long lastRefreshTime = mPreferences.getLong(mNameType + Preferences.LAST_TWEET_REFRESH_KEY, 0);
            long nowTime = DateTimeHelper.getNowTime();
            long diff = nowTime - lastRefreshTime;
            // Log.d(TAG, "Last refresh was " + diff + " ms ago.");
            if (diff > REFRESH_THRESHOLD) {
                shouldRetrieve = true;
            } else if (isTrue(savedInstanceState, SIS_RUNNING_KEY)) {
                // Check to see if it was running a send or retrieve task.
                // It makes no sense to resend the send request (don't want
                // dupes)
                // so we instead retrieve (refresh) to see if the message has
                // posted.
                // Log.d(TAG, "Was last running a retrieve or send task. Let's refresh.");
                shouldRetrieve = true;
            }

            if (shouldRetrieve) {
                doRetrieve();
            }

            goTop(); // skip the header

            // long lastFollowersRefreshTime = mPreferences.getLong(Preferences.LAST_FOLLOWERS_REFRESH_KEY, 0);
            // diff = nowTime - lastFollowersRefreshTime;
            // Log.d(TAG, "Last followers refresh was " + diff + " ms ago.");

            registerGestureListener();  // 手势识别
            registerShakeListener();  // 晃动刷新
            
            start_timer();

            return true;
        } else {
            return false;
        }
    }
    
    public void start_thread() {
    	timer_close = true;
    }
    public void end_thread() {
    	timer_close = false;
    }
    public void stopThread() {
    	Thread tmpBlinker = thread_timer; 
    	thread_timer = null; 

    	if (tmpBlinker!= null) { 
    	    tmpBlinker.interrupt (); 
    	} 
    };
	public void start_timer(){
		thread_timer = new Thread(){
			boolean update_en = false;
			
			public void run() {
				if(thread_timer==null) return;
				
				Thread thisThread = Thread.currentThread (); 
				while(thread_timer==thisThread) {
				//while(true){
					try {
						sleep(5000); //the check interval time set 5s
						
						if(timer_close) continue;
						
						update_en = false;
						if(allInfoList != null) {
							for ( Info info : allInfoList ) {
					            if(DateTimeHelper.getNowTime()>info.expireTime.getTime()) { //the info Expire is already reach
//					            	allInfoList.remove(info);
					            	JSONObject jsonData;
					            	String mTime;
					            	Date mExpire;
					            	try {
					            	    jsonData = TwitterApplication.mApi.getInfoExpire(info.id);
					            		mTime = jsonData.getString("expireTime");
					            		mExpire = DateTimeHelper.parseDateFromStr(jsonData.getString("expireTime"), "yyyy-MM-dd HH:mm:ss");
					            	} catch (HttpException e) {
					    				Log.e(TAG, e.getMessage(), e);
					    				continue;
									} catch (JSONException e) {
						                Log.e(TAG, e.getMessage(), e);
//						                _errorMsg = e.getMessage();
						                continue;
						            }
					            	if(DateTimeHelper.getNowTime()>mExpire.getTime()) { //need to double check the server db latest time
					            	    info.expire = 1;
					            	    update_en = true;
					            	    
					            	    Log.v(TAG, mTime);
					            	}
					            }
					        }
							
							if(update_en) {
								update_en = false;
								Message msg = new Message();
								msg.what = 1;
								handler.sendMessage(msg);
							}
						}
						
						Thread.yield();
						if (Thread.currentThread().isInterrupted()) {
				            throw new InterruptedException("Stopped by ifInterruptedStop()");
				        }
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//repaint (); 
				}
			};

		};
		thread_timer.start();
	}
	
	   private Handler handler = new Handler() {  
	        @Override  
	        public void handleMessage(Message msg) {  
	            if (msg.what == 1) {  
	            	if(mInfoListAdapter!=null) {
	            	    mInfoListAdapter.refresh();
	            	}
	            }  
	        }  
	    }; 

	@Override
    protected void onResume() {
        Log.d(TAG, "onResume.");
//        if (lastPosition != 0) {
//            mTweetList.setSelection(lastPosition);
//        }
        if (mShaker != null){
        	mShaker.resume();
        }
        super.onResume();
        checkIsLogedIn();
		((TwitterApplication) getApplication()).requestLocationUpdates(mSearchLocationObserver);
		setLocationOption();  // 百度位置
		mLocClient.start();  // 百度位置
		
        if (lastPosition != 0) {
        	//mTweetList.setSelectionFromTop(lastPosition, scrollTop);
            mTweetList.setSelection(lastPosition);
        }
        
        start_thread();
    }
	
	@Override
    protected void onPause() {
        Log.d(TAG, "onPause.");
        if (mShaker != null){
        	mShaker.pause();
        }
        super.onPause();
        
        lastPosition = mTweetList.getFirstVisiblePosition();
        View v=mTweetList .getChildAt(0);
        scrollTop=(v==null)?0:v.getTop();
        
		((TwitterApplication) getApplication()).removeLocationUpdates(mSearchLocationObserver);
		mLocClient.stop();  // 百度位置
		mInfoListAdapter.stopPlay();  // 停止语音播放
		
		end_thread();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRetrieveTask != null && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            outState.putBoolean(SIS_RUNNING_KEY, true);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        // mTweetEdit.updateCharsRemain();
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy.");
        super.onDestroy();

        taskManager.cancelAll();

        // 刷新按钮停止旋转
        if (loadMoreGIF != null){
        	loadMoreGIF.setVisibility(View.GONE);
        }
        if (mTweetList != null){
        	mTweetList.onRefreshComplete();
        }
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart.");
        super.onRestart();
        
        start_timer();
    }

	@Override
    protected void onStart() {
        Log.d(TAG, "onStart.");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop.");
        super.onStop();
        
        stopThread();
    }

    // UI helpers.

    @Override
    protected String getActivityTitle() {
        return null;
    }
	
    public double getNowLat() {
    	return mLat;
    }
    public double getNowLng() {
    	return mLng;
    }
    public String getNameTxt() {
    	return name_txt;
    }
//	//设置相关参数
//	private void setLocationOption() {
//		LocationClientOption option = new LocationClientOption();
//		option.setOpenGps(true);  //打开gps
//		option.setCoorType("bd0911");  //设置坐标类型
//		option.setServiceName("com.baidu.location.service_v2.9");
//		option.setPoiExtraInfo(true);
//        option.setAddrType("all");
//        option.setScanSpan(1000);
//        option.setPriority(LocationClientOption.NetWorkFirst);  // LocationClientOption.GpsFirst 
//		option.setPoiNumber(10);
//		option.disableCache(true);		
//		mLocClient.setLocOption(option);
//	}
	//设置相关参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy); ////高精度，设置定位模式，高精度，低功耗，仅设备
		option.setOpenGps(true);  //打开gps
		option.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先
		option.setLocationNotify(true);//默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		option.setCoorType("bd0911");  //设置坐标类型 "gcj02";//国家测绘局标准;"bd09ll";//百度经纬度标准,"bd09";//百度墨卡托标准
//		option.setServiceName("com.baidu.location.service_v2.9");
		option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//		option.setPoiExtraInfo(true);//是否需要POI的电话和地址等详细信息   
//		option.setPoiDistance(1000); //poi查询距离       
//		option.setPoiNumber(10);//最多返回POI个数   
		option.setIsNeedAddress(true);//设置是否需要地址信息，默认不需要， 只有网络定位才可以
        option.setAddrType("all");
        option.setScanSpan(1000); //默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setPriority(LocationClientOption.NetWorkFirst);  // LocationClientOption.GpsFirst 
		option.disableCache(true);	
		//option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
		//option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
		//option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		mLocClient.setLocOption(option);
	}

    public void draw() {
        //mTweetAdapter.refresh();
		mInfoListAdapter.refresh(allInfoList);
    }

    public void goTop() {
        Log.d(TAG, "goTop.");
        mTweetList.setSelection(1);
    }

    public void doRetrieve() {
        Log.d(TAG, "Attempting retrieve.");
        
        data_finish = false;
        no_data = false;

        if (mRetrieveTask != null && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mRetrieveTask = new RetrieveTask();
            mRetrieveTask.setFeedback(mFeedback);
            mRetrieveTask.setListener(mRetrieveTaskListener);
            mRetrieveTask.execute();

            // Add Task to manager
            taskManager.addTask(mRetrieveTask);
        }
    }
	
	public void doGetMore() {
        Log.d(TAG, "Attempting getMore.");
        Log.v("doGetMore", String.valueOf(this.mRefreshFrequency));
        
        if(data_finish) {
        	onLoadMoreComplete();
        	return;
        }

        if (mGetMoreTask != null && mGetMoreTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mGetMoreTask = new GetMoreTask();
            mGetMoreTask.setFeedback(mFeedback);
            mGetMoreTask.setListener(mRetrieveTaskListener);
            mGetMoreTask.execute();

            // Add Task to manager
            taskManager.addTask(mGetMoreTask);
        }
    }

    private class RetrieveTask extends GenericTask {
        private String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.codeim.coxin.fanfou.Info> infosList = null;
			mRefreshFrequency = 0;
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();

            try {
			    double latitude;
				double longitude;
				/*
				Weibo.Location location = null;
				location = LocationUtils.createFoursquareLocation(twitterApplication.getLastKnownLocation());
				if (twitterApplication.getLastKnownLocation() != null) {
				    latitude = location.getLat();
				    longitude = location.getLon();
				} else {
				    BDLocation BDLoc = twitterApplication.getBDLocation();
					while (BDLoc == null) {
					    BDLoc = twitterApplication.getBDLocation();
					}
					latitude = BDLoc.getLatitude();
					longitude = BDLoc.getLongitude();
				}
				*/

				BDLocation BDLoc = twitterApplication.getBDLocation();
				while (BDLoc == null || (BDLoc.getLatitude() == 0 && BDLoc.getLongitude() == 0)) {
					BDLoc = twitterApplication.getBDLocation();
				}
				latitude = BDLoc.getLatitude();
				longitude = BDLoc.getLongitude();
				//Toast.makeText(getApplicationContext(), "latitude = " + latitude + " , longitude = " + longitude, Toast.LENGTH_SHORT).show();
				
				//for only simulator debug
//				latitude = 31.20517;
//				longitude= 121.596935;
				
				//if(Nearby_Place == 0) { //nearby
				if(isFirstLoc) {
				    mLat = latitude;
				    mLng = longitude;
				}
				
		    	page_size=1;
		    	page_index=0;
		    	last_id=-1;
				
                //statusList = getMessageSinceId(maxId);
//            	infosList = getNearbyInfo(mRefreshFrequency, mInfoType, mLat, mLng);
            	infosList = getNearbyInfo(page_size, page_index, last_id, mInfoType, mLat, mLng);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, infosList));
			allInfoList.clear();
			for (com.codeim.coxin.fanfou.Info info : infosList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				Info u = Info.create(info);
				allInfoList.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			
			com.codeim.coxin.data.Info info = allInfoList.get(allInfoList.size()-1);
			if(info.id.equals("-1")) {
				allInfoList.remove(allInfoList.size()-1);
				data_finish = true;
			}
			if(allInfoList.size()>0) {
				last_id = Integer.valueOf(allInfoList.get(allInfoList.size()-1).id);
			} else {
				no_data = true;
			}

            return TaskResult.OK;
        }
    }

    // GET MORE TASK
    private class GetMoreTask extends GenericTask {
        private String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }
        

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.codeim.coxin.fanfou.Info> infosList = null;
			mRefreshFrequency = mRefreshFrequency + 1;
//            mRefreshFrequency = mInfoListAdapter.getCount();
//            int mPage = mInfoListAdapter.getCount();
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();

            try {
			    double latitude;
				double longitude;
				Weibo.Location location = null;
				location = LocationUtils.createFoursquareLocation(twitterApplication.getLastKnownLocation());
				if (twitterApplication.getLastKnownLocation() != null) {
				    latitude = location.getLat();
				    longitude = location.getLon();
				} else {
				    BDLocation BDLoc = twitterApplication.getBDLocation();
					while (BDLoc == null) {
					    BDLoc = twitterApplication.getBDLocation();
					}
					latitude = BDLoc.getLatitude();
					longitude = BDLoc.getLongitude();
				}
				
//				latitude = 31.20517;
//				longitude= 121.596935;
				
				//if(Nearby_Place == 0) { //nearby
				if(isFirstLoc) {
				    mLat = latitude;
				    mLng = longitude;
				}
				
		    	//page_size=1;
		    	page_index++;
		    	//last_id=0;

                //statusList = getMessageSinceId(maxId);
//            	infosList = getNearbyInfo(mRefreshFrequency, mInfoType, mLat, mLng);
				infosList = getNearbyInfo(page_size, page_index, last_id, mInfoType, mLat, mLng);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, infosList));
			for (com.codeim.coxin.fanfou.Info info : infosList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				Info u = Info.create(info);
				allInfoList.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			
			com.codeim.coxin.data.Info info = allInfoList.get(allInfoList.size()-1);
			if(info.id.equals("-1")) {
				allInfoList.remove(allInfoList.size()-1);
				data_finish = true;
			}
			if(allInfoList.size()>0) {
				last_id = Integer.valueOf(allInfoList.get(allInfoList.size()-1).id);
			} else {
				no_data = true;
			}

            return TaskResult.OK;
        }
    }
    
    //////////////////// Gesture test /////////////////////////////////////
    private static boolean useGestrue;
    {
        useGestrue = TwitterApplication.mPref.getBoolean(Preferences.USE_GESTRUE, false);
        if (useGestrue) {
            Log.v(TAG, "Using Gestrue!");
        } else {
            Log.v(TAG, "Not Using Gestrue!");
        }
    }
    
    //////////////////// Gesture test /////////////////////////////////////
    private static boolean useShake;
    {
        useShake = TwitterApplication.mPref.getBoolean(Preferences.USE_SHAKE, false);
        if (useShake) {
            Log.v(TAG, "Using Shake to refresh!");
        } else {
            Log.v(TAG, "Not Using Shake!");
        }
    }
    
    
    protected FlingGestureListener myGestureListener = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (useGestrue && myGestureListener != null) {
            return myGestureListener.getDetector().onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    // use it in _onCreate
    private void registerGestureListener() {
        if (useGestrue) {
//            myGestureListener = new FlingGestureListener(this, MyActivityFlipper.create(this));
//            getTweetList().setOnTouchListener(myGestureListener);
        }
    }
    
    // use it in _onCreate
    private void registerShakeListener() {
    	if (useShake){
	    	mShaker = new ShakeListener(this);
	    	mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
				
				@Override
				public void onShake() {
					Log.v(TAG, "onShake");
					doRetrieve();
				}
			});
    	}
	}

    /** 
     * This is really just a dummy observer to get the GPS running
     * since this is the new splash page. After getting a fix, we
     * might want to stop registering this observer thereafter so
     * it doesn't annoy the user too much.
     */
    private class SearchLocationObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
        }
    }
    
    protected void onReturnFromCommentList(String info_id) {
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(requestCode == 100){ //从CommentWrite返回
	    //比较requestCode和REQUESTCODE，证明活动是否为REQUESTCODE相关的操作发起。
	        if(resultCode == RESULT_OK){
	        //比较resultCode和SecondActivity中的RESULTCODE，证明SecondActivity活动是否返回成功。
	            Bundle bundle = data.getExtras();
	            int info_id = bundle.getInt("infoId");
	            int comment_cnt = bundle.getInt("commentCnt");
	            
	            int len = allInfoList.size();
	            if(len>0) {
	            	for(int i=0; i<len; i++) {
	            		if(Integer.valueOf(allInfoList.get(i).id) == info_id) {
	            			allInfoList.get(i).conversationCount = comment_cnt;
	            		}
	            	}
	            }
	            mInfoListAdapter.refresh();
//	            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
	        }
	    }
	    else if(requestCode == 200) { //从CommentPinnedSectionActivity返回
	    	if(resultCode == RESULT_OK){
	            Bundle bundle = data.getExtras();
	            String info_id = bundle.getString("infoId");
	            
	    		onReturnFromCommentList(info_id);
	    	}
	    }
	    else if(requestCode == 400) {//从InfoMapActivity返回
	    	if(resultCode == RESULT_OK){
	            Bundle bundle = data.getExtras();
	            boolean change = bundle.getBoolean("change",false);
	            if(change) {
		            mLat = bundle.getDouble("latitude");
		            mLng = bundle.getDouble("longitude");
		            name_txt = bundle.getString("info");
	            } 
//	            else {
//		            mLat = bundle.getDouble("latitude");
//		            mLng = bundle.getDouble("longitude");
//		            name_txt = bundle.getString("info");
//	            }

	            //Nearby_Place = 1; //1 for return from InfoMapActivity. always 1?
	            isFirstLoc = false;

	            if(change) {
		            doRetrieve();
		            this.mNavBar.setHeaderTitle(name_txt);
	            }
	    	}
	    }
	}
	protected void removeAsInfoId(String id) {
		Log.e(TAG, "removeAsInfoId"+id);
        int len = allInfoList.size();
        if(len>0) {
        	for(int i=0; i<len; i++) {
        		if(allInfoList.get(i).id.equalsIgnoreCase(id)) {
        			allInfoList.remove(i);
        			break;
        		}
        	}
        }
	}
	protected void changeExpireAsInfoId(String id, Date afterChangeTime) {
		Log.e(TAG, "changeExpireAsInfoId"+id);
        int len = allInfoList.size();
        if(len>0) {
        	for(int i=0; i<len; i++) {
        		if(allInfoList.get(i).id.equalsIgnoreCase(id)) {
        			allInfoList.get(i).expireTime = afterChangeTime;
        			break;
        		}
        	}
        }
	}
	protected void changeInfoAsInfo(Info info) {
		String id = info.id;
		Log.e(TAG, "changeInfoAsInfo"+id);
        int len = allInfoList.size();
        if(len>0) {
        	for(int i=0; i<len; i++) {
        		if(allInfoList.get(i).id.equalsIgnoreCase(id)) {
        			allInfoList.get(i).expireTime = info.expireTime;
        			allInfoList.get(i).conversationCount = info.conversationCount;
        			allInfoList.get(i).praiseCount = info.praiseCount;
        			allInfoList.get(i).user_praise = info.user_praise;
        			break;
        		}
        	}
        }
	}

}