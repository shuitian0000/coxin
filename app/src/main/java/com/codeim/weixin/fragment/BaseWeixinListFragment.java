package com.codeim.weixin.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.BDLocation;
import com.codeim.byme.ListByMyActivity;
import com.codeim.byme.base.BaseFragment;
import com.codeim.byme.base.OnFragmentActivityTouchListener;
import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.Info;
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
import com.codeim.coxin.ui.base.Refreshable;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.FlingGestureListener;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DebugTimer;
import com.codeim.coxin.widget.PullToRefreshListView;
import com.codeim.coxin.widget.PullToRefreshListView.OnRefreshListener;
import com.codeim.floorview.CommentPinnedSectionActivity;
import com.codeim.weixin.MessageActivity;

/*for ChatList and ContactList
 * only loadMore is enable, refresh is disable*/
public class BaseWeixinListFragment extends BaseFragment implements Refreshable{
	static final String TAG = "BaseWeixinListFragment";
	
	private String mNameType = "";  // 不同页面的标记，用于刷新计时
	
	// Refresh data at startup if last refresh was this long ago or greater.
	private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;
    
    // Tasks.
	private TaskManager taskManager = new TaskManager();
    private GenericTask mRetrieveTask;
    private GenericTask mGetMoreTask;
    private Feedback mFeedback;
    
    protected PullToRefreshListView mTweetList;  // 存放information列表
    protected boolean data_finish;
    protected boolean no_data;
    
    private View mListFooter;
    private TextView loadMoreBtn;
    private ProgressBar loadMoreGIF;
    private TextView loadMoreBtnTop;
    protected ProgressBar loadMoreGIFTop;
    
	protected int page_size;
	protected int page_index;
	protected int last_id;
	
	private int mCurrentScrollState;
	private boolean mIsLoading;
	private OnScrollListener mOnScrollListener;
	
	//for type related
//	List<?> infosList = null;
//	volatile protected ArrayList<?> allInfoList;
	
	//the follow function must be overrided
	protected void setAdapter() {}
	protected void registerOnClickListener(ListView listView) {}
//	protected List<?> getWeixinInfo() { return null; }
	protected RetrieveTask getNewRetrieveTask() {return null;}
	protected GetMoreTask getNewGetMoreTask() {return null;}
    protected void draw() {
//		mInfoListAdapter.refresh(allInfoList); //for chat, disable this
    }


	@Override
	protected int getLayoutId() {
		return R.layout.info_list_fragment;
	}
	
	@Override
	protected void init(boolean isrunning) {
		mTweetList = (PullToRefreshListView) contextView.findViewById(R.id.tweet_list);
		mFeedback = FeedbackFactory.create(this.getActivity(), FeedbackType.PROGRESS);
		
        setupListHeader(true);
//      allInfoList = new ArrayList<com.codeim.coxin.data.Info>();
//		mInfoListAdapter = new NearbyInfoArrayAdapter(this.getActivity());
//		mTweetList.setAdapter(mInfoListAdapter);
//		mTweetList.setHeaderDividersEnabled(false);
        setAdapter();
      
        data_finish = false;
        no_data=false;

		boolean shouldRetrieve = false;
		long lastRefreshTime = mPreferences.getLong(mNameType + Preferences.LAST_TWEET_REFRESH_KEY, 0);
        long nowTime = DateTimeHelper.getNowTime();
        long diff = nowTime - lastRefreshTime;
        // Log.d(TAG, "Last refresh was " + diff + " ms ago.");
        if (diff > REFRESH_THRESHOLD) {
            shouldRetrieve = true;
        } else if (isrunning) {
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
	}
	@Override
	protected void initView() {
		
	}
	@Override
	protected void initListener() {
        registerOnClickListener(getListView());
        registerGestureListener();  // 手势识别
        registerShakeListener();  // 晃动刷新
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
    			doRetrieve(); //for chat,  disable refresh function
    		}
    	});

        // Add Footer to ListView
        mListFooter = View.inflate(this.getActivity(), R.layout.listview_footer, null);
        mTweetList.addFooterView(mListFooter, null, true);
        
        // Find View
        loadMoreBtn = (TextView) contextView.findViewById(R.id.ask_for_more);
        loadMoreGIF = (ProgressBar) contextView.findViewById(R.id.rectangleProgressBar);  
        loadMoreGIF.setPadding(0, 0, 15, 0);
        
        loadMoreBtn.setVisibility(View.GONE);  //add by ywwang for pull to getMore
        
        loadMoreBtnTop = (TextView) contextView.findViewById(R.id.ask_for_more_header);
        loadMoreGIFTop = (ProgressBar) contextView.findViewById(R.id.rectangleProgressBar_header);
        
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
	
	/*
	 * need override by extends,  item type
	 * */
//	protected void registerOnClickListener(ListView listView) {
//		listView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				Log.e(TAG, "setOnItemClickListener position: "+String.valueOf(position));
//				final Info info = getContextItemTweet(position);
////				Info info = (Info) parent.getAdapter().getItem(position);
//
//				if (info == null) {
//					Log.w(TAG, "Selected item not available.");
//					specialItemClicked(position);
//				} else if(info.expire==0){
//					Log.d(TAG,String.valueOf(position));
//					
//					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_ID, info.owerId.toString() ).commit();
//					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_USERNAME, info.owerName.toString() ).commit();
//					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_ID, info.id.toString() ).commit();
//					Intent intent = new Intent(contextActivity, CommentPinnedSectionActivity.class);
//					intent.putExtra("INFO", info);
//					startActivityForResult(intent, 200);
//					
//				}
//			}
//		});
//	}
	
	
	//get the item clicked,need change the type of item
//	protected Info getContextItemTweet(int position) {
//	    position = position - 1;
	    // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
//	    if (position >= 0 && position < mInfoListAdapter.getCount()) {
//	        Info info = (Info) mInfoListAdapter.getItem(position);
//	        if (info == null) {
//	            return null;
//	        } else {
//	            return info;
//	        }
//	    } else {
//	        return null;
//	    }
//	}
	
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
                //logout();
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
            
            Log.v("taskadapter", "postexecute!");
        }

        @Override
        public void onPreExecute(GenericTask task) {
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
    
	@Override
	public void doRetrieve() {
		// TODO Auto-generated method stub
        Log.d(TAG, "Attempting retrieve.");
        
        data_finish = false;
        no_data = false;

        if (mRetrieveTask != null && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            //mRetrieveTask = new RetrieveTask();
        	mRetrieveTask = getNewRetrieveTask();
            mRetrieveTask.setFeedback(mFeedback);
            mRetrieveTask.setListener(mRetrieveTaskListener);
            mRetrieveTask.execute();

            // Add Task to manager
            taskManager.addTask(mRetrieveTask);
        }
	}
	
	public void doGetMore() {
        Log.d(TAG, "Attempting getMore.");
        Log.v("doGetMore", String.valueOf(this.page_index));
        
        if(data_finish) {
        	onLoadMoreComplete();
        	return;
        }

        if (mGetMoreTask != null && mGetMoreTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
//            mGetMoreTask = new GetMoreTask();
        	mGetMoreTask = getNewGetMoreTask();
            mGetMoreTask.setFeedback(mFeedback);
            mGetMoreTask.setListener(mRetrieveTaskListener);
            mGetMoreTask.execute();

            // Add Task to manager
            taskManager.addTask(mGetMoreTask);
        }
    }
	
    class RetrieveTask extends GenericTask {
        protected String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
			TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();
			
	    	page_size=1;
	    	page_index=0;
	    	last_id=-1;
//            try {
//                //statusList = getMessageSinceId(maxId);
//            	infosList = getWeixinInfo();
//            } catch (HttpException e) {
//                Log.e(TAG, e.getMessage(), e);
//                _errorMsg = e.getMessage();
//                return TaskResult.IO_ERROR;
//            }
//
//            publishProgress(SimpleFeedback.calProgressBySize(40, 20, infosList));
//			allInfoList.clear();
//			for (Object info : infosList) {
//				if (isCancelled()) {
//					return TaskResult.CANCELLED;
//				}
//				// Log.d(TAG, "User: " + user.toString());
//				Info u = Info.create(info);
//				allInfoList.add(u);
//			}
//			if (isCancelled()) {
//				return TaskResult.CANCELLED;
//			}
//			
//			com.codeim.coxin.data.Info info = allInfoList.get(allInfoList.size()-1);
//			if(info.id.equals("-1")) {
//				allInfoList.remove(allInfoList.size()-1);
//				data_finish = true;
//			}
//			if(allInfoList.size()>0) {
//				last_id = Integer.valueOf(allInfoList.get(allInfoList.size()-1).id);
//			} else {
//				no_data = true;
//			}

            return TaskResult.OK;
        }
    }
	
    // GET MORE TASK
    class GetMoreTask extends GenericTask {
        protected String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }
        

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.codeim.coxin.fanfou.Info> infosList = null;
//            int mPage = mInfoListAdapter.getCount();
			TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();
//
//            try {
//                //statusList = getMessageSinceId(maxId);
//				infosList = getNearbyInfo(page_size, page_index, last_id, mInfoType, mLat, mLng);
//            } catch (HttpException e) {
//                Log.e(TAG, e.getMessage(), e);
//                _errorMsg = e.getMessage();
//                return TaskResult.IO_ERROR;
//            }
//
//            publishProgress(SimpleFeedback.calProgressBySize(40, 20, infosList));
//			for (com.codeim.coxin.fanfou.Info info : infosList) {
//				if (isCancelled()) {
//					return TaskResult.CANCELLED;
//				}
//				Info u = Info.create(info);
//				allInfoList.add(u);
//				if (isCancelled()) {
//					return TaskResult.CANCELLED;
//				}
//			}
//			
//			com.codeim.coxin.data.Info info = allInfoList.get(allInfoList.size()-1);
//			if(info.id.equals("-1")) {
//				allInfoList.remove(allInfoList.size()-1);
//				data_finish = true;
//			}
//			if(allInfoList.size()>0) {
//				last_id = Integer.valueOf(allInfoList.get(allInfoList.size()-1).id);
//			} else {
//				no_data = true;
//			}

            return TaskResult.OK;
        }
    }
	
    public void goTop() {
        Log.d(TAG, "goTop.");
        mTweetList.setSelection(1);
    }
	
    public ListView getListView() {
        return mTweetList;
    }
    
    public void onLoadMoreComplete() {
        mIsLoading = false;
        mListFooter.setVisibility(View.GONE);//hideFooterView();
    }
    
	public Weibo getApi() {
		return TwitterApplication.mApi;
	}
	
/**/
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

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (useGestrue && myGestureListener != null) {
//            return myGestureListener.getDetector().onTouchEvent(event);
//        }
//        return super.onTouchEvent(event);
//    }
    protected void setMyTouchListener() {
		/* Fragment中，注册 
 	    * 接收MainActivity的Touch回调的对象 
 	    * 重写其中的onTouchEvent函数，并进行该Fragment的逻辑处理 
 	    */  
    	OnFragmentActivityTouchListener myTouchListener = new OnFragmentActivityTouchListener() {  
 	        @Override  
 	        public void onTouchEvent(MotionEvent event) {  
 	        // 处理手势事件 
 	           if (useGestrue && myGestureListener != null) {
 	              myGestureListener.getDetector().onTouchEvent(event);
 	          }
 	        }  
 	    };  
 	          
 	    // 将myTouchListener注册到分发列表  
 	   //(this.getActivity()).registerMyTouchListener(myTouchListener);  
 	   (contextActivity).registerMyTouchListener(myTouchListener);
    }

    // use it in _onCreate
    protected void registerGestureListener() {
        if (useGestrue) {
//            myGestureListener = new FlingGestureListener(this, MyActivityFlipper.create(this));
//            getTweetList().setOnTouchListener(myGestureListener);
        }
    }
    
    // use it in _onCreate
    protected void registerShakeListener() {
    	if (useShake){
	    	mShaker = new ShakeListener(this.getActivity());
	    	mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
				
				@Override
				public void onShake() {
					Log.v(TAG, "onShake");
					doRetrieve(); //for chat, the shake function disable
				}
			});
    	}
	}
	
    private class SearchLocationObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
        }
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v("ContactListFragment", "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==200&&resultCode==100){ //return from delete friend
			doRetrieve();
		}
	}


	
}
