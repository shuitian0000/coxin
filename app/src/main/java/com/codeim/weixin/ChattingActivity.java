package com.codeim.weixin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.ChatMsg;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskFeedback;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskManager;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DebugTimer;
import com.codeim.coxin.widget.PullToRefreshListView;
import com.codeim.floorview.utils.DateFormatUtils;
import com.codeim.weixin.adapter.ChattingArrayAdapter;
import com.codeim.coxin.R;

import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.codeim.coxin.widget.PullToRefreshListView.OnRefreshListener;

import static com.codeim.coxin.R.attr.comment;
//import com.codeim.floorview.view.PullToRefreshListView.OnRefreshListener;
//import com.codeim.floorview.view.PullRefreshAndLoadMoreListView.OnLoadMoreListener;

public class ChattingActivity extends BaseNoDoubleClickActivity implements OnTouchListener {
	private static final String TAG = "ChattingActivity";
	private static final String SIS_RUNNING_KEY = "running";
	private String mNameType = "";  // 不同页面的标记，用于刷新计时
	// Refresh data at startup if last refresh was this long ago or greater.
	private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;

	//private com.codeim.floorview.view.PullRefreshAndLoadMoreListView mTweetList;
	private PullToRefreshListView mTweetList;
    private LayoutInflater inflater ;
    private ArrayList < ChatMsg > allChatMsgs ;
	protected ChattingArrayAdapter mChattingListAdapter;
	private String chatGrpId;
	private String slaveId;
	private String slaveName;
	private String slaveImageUrl;
	private String myself;
    
	private NavBar mNavBar;
    protected Feedback mFeedback;
    
    protected static final int STATE_ALL = 0;
    
    // Tasks.
    protected TaskManager taskManager = new TaskManager();
    private GenericTask mRetrieveTask;
    private GenericTask mGetMoreTask;
    private boolean data_finish; //move to the PullRefreshAndLoadMoreListView
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
    
//    private int parentid;
//    private int floornum;
//    
//    volatile private ArrayList<com.codeim.floorview.view.SubComments> allSubCommentsList;
//    private ArrayList<com.codeim.floorview.bean.Comment> allChatMsgs;
//    
    private RelativeLayout comment_footer;
    private EditText edt_sendmessage;
    private TextView btn_rcd;
    private Button btn_send;

//    
//    private GenericTask mSendInfoTask;

    private GestureDetector gestureDetector;
    
	private TaskListener mSendInfoTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			//onRegisterBegin();
			TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, ChattingActivity.this).start("正在发送");
		}
		
		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			
		}
		
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
//			if (result == TaskResult.OK && mSendInfoFeedback.equals("ok")) {
			if (result == TaskResult.OK) {
				onSendInfoSuccess();
				// mLocationDisplay.setVisibility(View.VISIBLE);
			    // mLocationDisplay.setText(mLat + " " + mLon);
			} else if (result == TaskResult.IO_ERROR) {
				TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, ChattingActivity.this).failed("IO_ERROR");

				// mLocationDisplay.setText(mRegisterFeedback);
				//warnDialog(mSendInfoFeedback);
			} else {
				 //onRegisterFailure("注册失败");
				 TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, ChattingActivity.this).failed("发布失败");
			}
		}
		
		@Override
		public String getName() {
			return "AddInfo";
		}
	};
	
	private void onSendInfoSuccess() {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, ChattingActivity.this).success("");
//		updateProgress("评论成功");
		
		//成功评论之后
		Toast.makeText(ChattingActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
		
//		finish();
	}
    
	
	//---- onCreate
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "ChattingActivity OnCreate start");

		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.chatting_main);
			
	        Bundle bundle = getIntent().getExtras();
//	        Info info = (Info) bundle.getParcelable("INFO");
//			Info info = (Info) getIntent().getParcelableExtra("INFO");
	    	chatGrpId = bundle.getString("chatGrpId");
	    	slaveId = bundle.getString("slaveId");
	    	slaveName = bundle.getString("slaveName");
	    	slaveImageUrl = bundle.getString("otherImageUrl");
			myself = TwitterApplication.getMyselfId(false);
			
			setupState(isTrue(savedInstanceState, SIS_RUNNING_KEY));

			return true;
		} else {
			return false;
		}
	}
	public static boolean isTrue(Bundle bundle, String key) {
		return bundle != null && bundle.containsKey(key) && bundle.getBoolean(key);
	}
	protected void setupState(boolean isrunning) {
		init(isrunning);
		initView(isrunning);
		initListener();
	}
	protected void init(boolean isrunning) {
		mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
		mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
		mNavBar.setHeaderTitle(slaveName);
		mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);
		
		gestureDetector = new GestureDetector(ChattingActivity.this,onGestureListener);
		
		mTweetList = (PullToRefreshListView)findViewById ( R.id.container ) ;
        // Add Footer to ListView
        mListFooter = View.inflate(ChattingActivity.this, R.layout.listview_footer, null);
        mTweetList.addFooterView(mListFooter, null, true);
        
        // Find View
        loadMoreBtn = (TextView) findViewById(R.id.ask_for_more);
        loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
        loadMoreGIF.setPadding(0, 0, 15, 0);

        loadMoreBtnTop = (TextView) findViewById(R.id.ask_for_more_header);
        loadMoreGIFTop = (ProgressBar) findViewById(R.id.rectangleProgressBar_header);
		
		comment_footer = (RelativeLayout) findViewById(R.id.comment_footer);
		edt_sendmessage = (EditText) findViewById(R.id.edt_sendmessage);
		btn_rcd = (TextView) findViewById(R.id.btn_rcd);
		btn_send = (Button) findViewById(R.id.btn_send);
	}
	protected void initView(boolean isrunning) {
        inflater = this.getLayoutInflater () ;
		loadMoreBtn.setVisibility(View.GONE);  //add by ywwang for pull to getMore
		
		mChattingListAdapter = new ChattingArrayAdapter(this, slaveImageUrl);
		mTweetList.setAdapter(mChattingListAdapter);
		
	    page_size=20;
	    page_index=0;
	    last_id=0;
//	    mTweetList.data_finish = false;
	    data_finish = false;
	    no_data=false;

        Log.d ( "systemtime", DateFormatUtils.format ( new Date ( System.currentTimeMillis ()) ) ) ;
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
        //goTop(); // skip the header
	}
	protected void  initListener() {
		setupListHeader(true);
		registerOnClickListener(mTweetList);
		mTweetList.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
			}
		});
//		btn_send.setOnClickListener(new View.OnClickListener() {
//		public void onClick(View v) {
//			// doLogin();
//			doSendInfo();
//		}
//	});
	}
    public void goTop() {
        Log.d(TAG, "goTop.");
        mTweetList.setSelection(1);
    }
    /**
     * 绑定listView底部 - 载入更多 NOTE: 必须在listView#setAdapter之前调用
     */
    protected void setupListHeader(boolean addFooter) {
    }
	protected void registerOnClickListener(ListView listView) {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final ChatMsg chatMsg = getContextItemTweet(position);
				RelativeLayout pop_view;
				RadioButton reply;

				pop_view = ( RelativeLayout ) inflater.inflate(R.layout.comment_pop_menu, parent, false);
				final PopupWindow pop = new PopupWindow(pop_view,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false); 
//				pop.setBackgroundDrawable(new BitmapDrawable());
				pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bar));
				pop.setOutsideTouchable(true);
				pop.setFocusable(true);
				reply         = (RadioButton) pop_view.findViewById(R.id.comment_btn_reply);

//				pop_view.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if(v.getId()==R.id.comment_btn_reply) {
//							Intent intent = new Intent(ChattingActivity.this, CommentWriteActivity.class);
//							Bundle bundle=new Bundle();
//							intent.putExtra("parentid", comment.getId());
//							intent.putExtra("floornum", comment.getFloorNum());
//							intent.putExtras(bundle);
//							startActivity(intent);
//						}
//					}
//				});
				reply.setOnClickListener(new OnClickListener() {
				    @Override
					public void onClick(View v) {
				    	//pop.dismiss();
//						Intent intent = new Intent(ChattingActivity.this, CommentWriteActivity.class);
//						Bundle bundle=new Bundle();
//						//intent.putExtra("parentid", parentid);
//						//intent.putExtra("floornum", floornum);
//						intent.putExtras(bundle);
//						startActivity(intent);
//						pop.dismiss();
				    }
				});
			}
		});
		
		listView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				gestureDetector.onTouchEvent(event);
				return false;
			}
		});
	}
	
    protected void doRetrieve() {
        Log.d(TAG, "Attempting retrieve.");
        
//        mTweetList.data_finish=false;
        data_finish=false;
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
    
    
    protected void doGetMore() {
        Log.d(TAG, "Attempting getMore.");
        
        if(data_finish) {
        	//mTweetList.onLoadMoreComplete();
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
	
//	private void doSendInfo() {
//		String commentContext = edt_simple_comment.getText().toString();
////		commentLocation = addLocation.getText().toString();
//		String commentLocation = " ";
//		
//		if (mSendInfoTask != null && mSendInfoTask.getStatus() == GenericTask.Status.RUNNING) {
//			return;
//		} else {
//			if (!TextUtils.isEmpty(commentContext)) {
//				mSendInfoTask = new SendInfoTask();
//				mSendInfoTask.setFeedback(mFeedback);
//				mSendInfoTask.setListener(mSendInfoTaskListener);
//				TaskParams params = new TaskParams();
//				params.put("commentContext", commentContext);
//				params.put("commentPlace", commentLocation);
//				/*
//				if (mLocation != null) {
//				    params.put("latitude", mLocation.getLatitude());
//				    params.put("longitude", mLocation.getLongitude());
//				    Log.d(TAG, "latitude = " + mLocation.getLatitude());
//				    Log.d(TAG, "longitude = " + mLocation.getLongitude());
//					mLocationDisplay.setVisibility(View.VISIBLE);
//					mLocationDisplay.setText(mLocation.getLatitude() + " " + mLocation.getLongitude());
//				} else {
//				    params.put("latitude", 22.33);
//					params.put("longitude", 114.07);
//				}
//				*/
//				mSendInfoTask.execute(params);
//			} else if (TextUtils.isEmpty(commentContext)) {
//				warnDialog("请说些什么吧");
//			}
//		}
//	}
//	private class SendInfoTask extends GenericTask {
//		
//		@Override
//		protected TaskResult _doInBackground(TaskParams... params) {
//			TaskParams param = params[0];
//			TwitterApplication twitterApplication = (TwitterApplication) getApplication();
//			
//			try {
//				String commentContext = param.getString("commentContext");
//				String commentPlace = param.getString("commentPlace");
//				double latitude;
//				double longitude;
//				// double latitude = param.getDouble("latitude");
//				// double longitude = param.getDouble("longitude");
//				Weibo.Location location = null;
//				location = LocationUtils.createFoursquareLocation(twitterApplication.getLastKnownLocation());
//				if (twitterApplication.getLastKnownLocation() != null) {
//				    latitude = location.getLat();
//				    longitude = location.getLon();
//				} else {
//				    BDLocation BDLoc = twitterApplication.getBDLocation();
//					while (BDLoc == null) {
//					    BDLoc = twitterApplication.getBDLocation();
//					}
//					latitude = BDLoc.getLatitude();
//					longitude = BDLoc.getLongitude();
//				}
//				
//				//for only simulator debug
//				latitude = 31.205174;
//				longitude= 121.596926;
//				
//				// mLat = latitude;
//				// mLon = longitude;
////			    mSendInfoFeedback = TwitterApplication.mApi.sendComment(true, commentContext, info_id, parent_id, floornum,
////			    		commentPlace,latitude, longitude).asString();
//				
////			    Comment my_comment = TwitterApplication.mApi.sendComment(true, commentContext, info_id, parent_id, floornum,
////	    		commentPlace,latitude, longitude);
//			    Comment my_comment = TwitterApplication.mApi.sendComment(true, commentContext, infoId, 0, 1,
//			    		commentPlace,latitude, longitude);
//  
//				Log.d(TAG, "mRegisterFeedback = "+my_comment.toString());
//			} catch (HttpException e) {
//				Log.e(TAG, e.getMessage(), e);
//				return TaskResult.FAILED;
//			}
//			
//			return TaskResult.OK;
//		}
//	}
//	protected void warnDialog(String warn) {
//	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage(warn);
//		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//		    @Override
//			public void onClick(DialogInterface dialog, int which) {
//			    dialog.dismiss();
//			}
//		});
//		builder.show();
//	}
	
	
    public void draw() {
    	Log.v("Chatting draw", String.valueOf(allChatMsgs.size()));
    	mChattingListAdapter.refresh(allChatMsgs);
//		adapter.notifyDataSetChanged();
//    	for(Comment cm: allChatMsgs) {
//    		addComment(cm);
//    	}
    }
	
    private TaskListener mRetrieveTaskListener = new TaskAdapter() {
        @Override
        public String getName() {
            return "RetrieveTask";
        }
        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
//            // 刷新按钮停止旋转
//            loadMoreGIF.setVisibility(View.GONE);
//            mTweetList.onRefreshComplete();
        	mTweetList.onRefreshComplete();
        	if(task == mGetMoreTask) {
        		//mTweetList.onLoadMoreComplete();
        		onLoadMoreComplete();
        	}

            if (result == TaskResult.AUTH_ERROR) {
                mFeedback.failed("登录信息出错");
                logout();
            } else if (result == TaskResult.OK) {
            	Log.v("MsgType post process", "normal get list data");
                draw();
                if (task == mRetrieveTask) {
                    goTop();
                }
            } else if (result == TaskResult.IO_ERROR) {
                // FIXME: bad smell
                if (task == mRetrieveTask) {
                    mFeedback.failed(((RetrieveTask) task).getErrorMsg());
                } 
                else if (task == mGetMoreTask) {
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
            Log.v("taskadapter", "post execute!");
        }
        
        @Override
        public void onPreExecute(GenericTask task) {
            //mRetrieveCount = 0;
            //mTweetList.prepareForRefresh();
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
    public void onLoadMoreComplete() {
        mIsLoading = false;
        mListFooter.setVisibility(View.GONE);//hideFooterView();
    }
	
	private class RetrieveTask extends GenericTask {
        private String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }
        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.codeim.coxin.fanfou.ChatMsg> chatMsgList = null;
			//mRefreshFrequency = 0;
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();
            try 
            {
//            	commentsList = getComments(0, 6);
    		    page_size=13;
    		    page_index=0;
    		    last_id=0;

                chatMsgList = getChatMsg(page_size, page_index,last_id,
                                          chatGrpId,slaveId,myself);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, chatMsgList));
            //allChatMsgs.clear();
			for (com.codeim.coxin.fanfou.ChatMsg chatMsg: chatMsgList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				//Comment u = Comment.create(comment);
                ChatMsg u = ChatMsg.create(chatMsg);
				allChatMsgs.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			
			ChatMsg comment = allChatMsgs.get(allChatMsgs.size()-1);
			if(comment.id.equals("-1")) {
				allChatMsgs.remove(allChatMsgs.size()-1);
				data_finish = true;
			}
			if(allChatMsgs.size()>0) {
				last_id = Integer.valueOf(allChatMsgs.get(allChatMsgs.size()-1).id);
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
            List<com.codeim.coxin.fanfou.ChatMsg> chatMsgList = null;
			//mRefreshFrequency = 0;
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();

//			if(mTweetList.data_finish) {
			if(data_finish) {
				return TaskResult.OK;
			}
            try 
            {
//            	commentsList = getComments(0, 6);
    		    //page_size=0;
    		    page_index++;
//    		    last_id=0;
//    		    if(!data_finish)
                chatMsgList = getChatMsg(page_size, page_index,last_id,
                        chatGrpId,slaveId,myself);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, chatMsgList));
//            //allChatMsgs.clear();
			for (com.codeim.coxin.fanfou.ChatMsg chatMsg: chatMsgList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				ChatMsg u = ChatMsg.create(chatMsg);
				allChatMsgs.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

			ChatMsg comment = allChatMsgs.get(allChatMsgs.size()-1);
			if(comment.id.equals("-1")) {
				allChatMsgs.remove(allChatMsgs.size()-1);
                data_finish = true;
			}
			if(allChatMsgs.size()>0) {
				last_id = Integer.valueOf(allChatMsgs.get(allChatMsgs.size()-1).id);
			} else {
				no_data = true;
			}

            return TaskResult.OK;
        }
    }

    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy.");
        super.onDestroy();

        taskManager.cancelAll();

//        // 刷新按钮停止旋转
//        if (loadMoreGIF != null){
//        	loadMoreGIF.setVisibility(View.GONE);
//        }
//        if (mTweetList != null){
//        	mTweetList.onRefreshComplete();
//        }
    }

//    @Override
//    public boolean onCreateOptionsMenu ( Menu menu ) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater ().inflate ( R.menu.main, menu );
//        return true;
//    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		/*
		if (mDeleteTask != null && mDeleteTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
		*/
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
	    return gestureDetector.onTouchEvent(event);
	}
	final int UP_SCROLL = 0; 
	final int DOWN_SCROLL = 1; 
	private GestureDetector.OnGestureListener onGestureListener =   
	        new GestureDetector.SimpleOnGestureListener() {  
	        @Override  
	        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
	                float velocityY) {  
	            float x = e2.getX() - e1.getX();  
	            float y = e2.getY() - e1.getY();  
	  
	            if (y > 0) {  
	                doResult(UP_SCROLL);  
	            } else if (y < 0) {
	                doResult(DOWN_SCROLL);  
	            }  
	            return true;
	        }  
	    };
	 //hide or show the reply edit
	 //public void doResult(int action) {
     protected void doResult(int action) {
	        switch (action) {  
	        case UP_SCROLL:  //hide
	        	comment_footer.setVisibility(View.VISIBLE);
	            break;
	        case DOWN_SCROLL:  //show
	        	comment_footer.setVisibility(View.GONE); 
	            break;
	        }  
	    }

//    public List<Comment> getComments(int refreshFrequency, int info_id) throws HttpException {
    public List<com.codeim.coxin.fanfou.ChatMsg> getChatMsg(int page_size, int page_index,int last_id,
                                     String chatGrpId, String slaveId, String masterId) throws HttpException {
        return getApi().getChatMsgFromLocal(page_size, page_index,last_id,
                                            chatGrpId, slaveId, masterId);
	}
    
	protected ChatMsg getContextItemTweet(int position) {
        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mChattingListAdapter.getCount()) {
        	ChatMsg chatMsg = (ChatMsg) mChattingListAdapter.getItem(position);
            if (chatMsg == null) {
                return null;
            } else {
                return chatMsg;
            }
        } else {
            return null;
        }
	}
    
	protected void specialItemClicked(int position) {
	}
	
}
