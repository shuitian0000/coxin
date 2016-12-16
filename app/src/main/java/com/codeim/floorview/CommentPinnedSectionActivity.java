package com.codeim.floorview;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.codeim.coxin.NearbyActivity;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.data.MsgType;
import com.codeim.coxin.fanfou.Photo;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.location.LocationUtils;
import com.codeim.coxin.task.CommonTask;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskFeedback;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskManager;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.task.CommonTask.ChangeTimeInterface;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.MsgTypeArrayAdapter;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.util.CommonUtils;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DebugTimer;
import com.codeim.coxin.view.InfoWheelAddTimeDialog;
import com.codeim.coxin.view.InfoWheelSetTimeDialog;
import com.codeim.floorview.adapter.CommentArrayAdapter;
import com.codeim.floorview.adapter.CommentArrayAdapter.OnClickDownArrow;
import com.codeim.floorview.adapter.CommentArrayAdapter.OnTimeSet;
import com.codeim.floorview.bean.Comment;
import com.codeim.floorview.utils.DateFormatUtils;
import com.codeim.floorview.view.FloorView;
import com.codeim.floorview.view.SubComments;
import com.codeim.floorview.view.SubFloorFactory;
import com.codeim.coxin.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

//import com.codeim.coxin.widget.PullToRefreshListView.OnRefreshListener;
import com.codeim.floorview.view.PullToRefreshListView.OnRefreshListener;
import com.codeim.floorview.widget.PinnedSectionListView.OnLoadMoreListener;

@SuppressLint("HandlerLeak")
public class CommentPinnedSectionActivity extends BaseNoDoubleClickActivity implements OnTouchListener {
	private static final String TAG = "CommentPinnedSectionActivity";

//    private ListView container ;
//	private com.codeim.coxin.widget.PullToRefreshListView container ;
//	private com.codeim.floorview.view.PullToRefreshListView container;
//	private com.codeim.floorview.view.PullRefreshAndLoadMoreListView container;
	private com.codeim.floorview.widget.PinnedSectionListView container;
    private LayoutInflater inflater ;
    private List < Comment > datas ;
	protected CommentArrayAdapter mCommentListAdapter;
	private int userId;
	private int infoId;
	private Info info;
    
	private NavBar mNavBar;
    protected Feedback mFeedback;
    
    protected static final int STATE_ALL = 0;
    
    // Tasks.
    protected TaskManager taskManager = new TaskManager();
    private GenericTask mDrawTask;
    private GenericTask mGetMoreTask;
    private int page_size;
    private int page_index;
    private int last_id;
//    private boolean data_finish; //move to the PullRefreshAndLoadMoreListView
    private boolean no_data;
    
    private int parentid;
    private int floornum;
    
    volatile private ArrayList<com.codeim.floorview.view.SubComments> allSubCommentsList;
    private ArrayList<com.codeim.floorview.bean.Comment> allCommentsList;
    
    private LinearLayout comment_footer;
    private EditText edt_simple_comment;
    private Button btn_send_simple_comment;
    
    private GenericTask mSendInfoTask;
    
    protected GenericTask mChangeTimeTask;
	private Date afterChangeTime;
	private String toChangeTimeId;
    
    private GestureDetector gestureDetector;
    
	private TaskListener mSendInfoTaskListener = new TaskAdapter() {
		
		@Override
		public void onPreExecute(GenericTask task) {
			//onRegisterBegin();
			TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, CommentPinnedSectionActivity.this).start("正在发送");
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
				TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, CommentPinnedSectionActivity.this).failed("IO_ERROR");

				// mLocationDisplay.setText(mRegisterFeedback);
				//warnDialog(mSendInfoFeedback);
			} else {
				 //onRegisterFailure("注册失败");
				 TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, CommentPinnedSectionActivity.this).failed("发布失败");
			}
		}
		
		@Override
		public String getName() {
			return "AddInfo";
		}
	};
	
	private void onSendInfoSuccess() {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, CommentPinnedSectionActivity.this).success("");
//		updateProgress("评论成功");
		
		//成功评论之后
		Toast.makeText(CommentPinnedSectionActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
		
//		finish();
	}
    
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Comment OnCreate start");

		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.comment_pinnedsection_main);
			
			info = (Info) getIntent().getParcelableExtra("INFO");
	        userId = Integer.valueOf(info.owerId);
	        infoId = Integer.valueOf(info.id);
			
			mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
			mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
			mNavBar.setHeaderTitle("跟帖");
			mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);
			
			
			gestureDetector = new GestureDetector(CommentPinnedSectionActivity.this,onGestureListener);
			allSubCommentsList = new ArrayList<com.codeim.floorview.view.SubComments>();
			allCommentsList = new ArrayList<com.codeim.floorview.bean.Comment>();
			init_comments();
			

//	        container = ( ListView ) findViewById ( R.id.container ) ;
//			container = ( com.codeim.coxin.widget.PullToRefreshListView ) findViewById ( R.id.container ) ;
//			container = ( com.codeim.floorview.view.PullToRefreshListView ) findViewById ( R.id.container ) ;
//			container = ( com.codeim.floorview.view.PullRefreshAndLoadMoreListView ) findViewById ( R.id.container ) ;
			container = ( com.codeim.floorview.widget.PinnedSectionListView ) findViewById ( R.id.container ) ;
			
			comment_footer = (LinearLayout) findViewById(R.id.comment_footer);
			edt_simple_comment = (EditText) findViewById(R.id.edt_simple_comment);
			btn_send_simple_comment = (Button) findViewById(R.id.btn_send_simple_comment);
			//comment_footer.setVisibility(View.GONE);

			setupListHeader(true);
			
			mCommentListAdapter = new CommentArrayAdapter(this);
			container.setAdapter(mCommentListAdapter);
			
			registerOnClickListener(container);
			btn_send_simple_comment.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// doLogin();
					doSendInfo();
				}
			});
			
		    page_size=20;
		    page_index=0;
		    last_id=0;
		    container.data_finish = false;
		    no_data=false;
			
			///*the old listview way*/
//			mListView = (ListView) findViewById(R.id.msgtype_list);
//			mMsgTypeListAdapter = new MsgTypeArrayAdapter(this);
//			mListView.setAdapter(mMsgTypeListAdapter);
//			registerOnClickListener(mListView);
			
			/*the new listview way*/
	        inflater = this.getLayoutInflater () ;
	        //container = ( LinearLayout ) findViewById ( R.id.container ) ;

//	        datas = new CommentData ( this ).getComments () ;
//	        for ( Comment cmt : datas ) {
//	            addComment ( cmt ) ;
//	        }
			
	        Log.d ( "systemtime", DateFormatUtils.format ( new Date ( System.currentTimeMillis ()) ) ) ;
	        
			doDraw();
			
        	if(mCommentListAdapter!=null) {
        		mCommentListAdapter.setBpItemView(false);
        	}
			
			start_timer();

			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	    Intent intent = new Intent();
	    intent.putExtra("infoId", getInfoId());
	    setResult(Activity.RESULT_OK, intent);
	    finish();
	}
	
	public void start_timer(){
		Thread thread_timer = new  Thread(){
			public void run() {
				while(true){
					try {
						sleep(10000); //the check interval time set 10s
						
						if(allCommentsList != null &&allCommentsList.get(0)!=null) {
//							info = allCommentsList.get(0).info;
							if(info!=null) {
					            if(DateTimeHelper.getNowTime()>info.expireTime.getTime()) {
					            	
					            	JSONObject jsonData;
					            	Date mExpire;
					            	try {
					            	    jsonData = TwitterApplication.mApi.getInfoExpire(info.id);
					            		mExpire = DateTimeHelper.parseDateFromStr(jsonData.getString("expireTime"), "yyyy-MM-dd HH:mm:ss");
					            		
					            		if(DateTimeHelper.getNowTime()>mExpire.getTime()) {
									        Message msg = new Message();
										    msg.what = 1;
										    handler.sendMessage(msg);
					            		}
					            	} catch (HttpException e) {
					    				Log.e(TAG, e.getMessage(), e);
									} catch (JSONException e) {
						                Log.e(TAG, e.getMessage(), e);
						            }
					            }
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		};
		thread_timer.start();
	}
	
	   private Handler handler = new Handler() {  
	        @Override
	        public void handleMessage(Message msg) {  
	            if (msg.what == 1) {  
                    CommentPinnedSectionActivity.this.finish();
	            }  
	        }  
	    }; 
	
	public void init_comments() {
		allCommentsList.clear();
		Comment comment_info = new Comment (userId, 0, "", info.owerName, info.createdAt);
		comment_info.info = info;
		allCommentsList.add(comment_info);
		Comment comment_section = new Comment (userId, 0, "", info.owerName, info.createdAt);
		comment_section.section = true;
		allCommentsList.add(comment_section);
		Comment comment_progress_bar = new Comment (userId, 0, "", info.owerName, info.createdAt);
		comment_progress_bar.progress_bar = true;
		allCommentsList.add(comment_progress_bar);
	}
	
	private void doSendInfo() {
		String commentContext = edt_simple_comment.getText().toString();
//		commentLocation = addLocation.getText().toString();
		String commentLocation = " ";
		
		if (mSendInfoTask != null && mSendInfoTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			if (!TextUtils.isEmpty(commentContext)) {
				mSendInfoTask = new SendInfoTask();
				mSendInfoTask.setFeedback(mFeedback);
				mSendInfoTask.setListener(mSendInfoTaskListener);
				TaskParams params = new TaskParams();
				params.put("commentContext", commentContext);
				params.put("commentPlace", commentLocation);
				/*
				if (mLocation != null) {
				    params.put("latitude", mLocation.getLatitude());
				    params.put("longitude", mLocation.getLongitude());
				    Log.d(TAG, "latitude = " + mLocation.getLatitude());
				    Log.d(TAG, "longitude = " + mLocation.getLongitude());
					mLocationDisplay.setVisibility(View.VISIBLE);
					mLocationDisplay.setText(mLocation.getLatitude() + " " + mLocation.getLongitude());
				} else {
				    params.put("latitude", 22.33);
					params.put("longitude", 114.07);
				}
				*/
				mSendInfoTask.execute(params);
			} else if (TextUtils.isEmpty(commentContext)) {
				warnDialog("请说些什么吧");
			}
		}
	}
	private class SendInfoTask extends GenericTask {
		
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();
			
			try {
				String commentContext = param.getString("commentContext");
				String commentPlace = param.getString("commentPlace");
				double latitude;
				double longitude;
				// double latitude = param.getDouble("latitude");
				// double longitude = param.getDouble("longitude");
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
				
				//for only simulator debug
				latitude = 31.205174;
				longitude= 121.596926;
				
				// mLat = latitude;
				// mLon = longitude;
//			    mSendInfoFeedback = TwitterApplication.mApi.sendComment(true, commentContext, info_id, parent_id, floornum,
//			    		commentPlace,latitude, longitude).asString();
				
//			    Comment my_comment = TwitterApplication.mApi.sendComment(true, commentContext, info_id, parent_id, floornum,
//	    		commentPlace,latitude, longitude);
			    Comment my_comment = TwitterApplication.mApi.sendComment(true, commentContext, infoId, 0, 1,
			    		commentPlace,latitude, longitude);
  
				Log.d(TAG, "mRegisterFeedback = "+my_comment.toString());
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.FAILED;
			}
			
			return TaskResult.OK;
		}
	}
	protected void warnDialog(String warn) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(warn);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();
			}
		});
		builder.show();
	}
	
    /**
     * 绑定listView底部 - 载入更多 NOTE: 必须在listView#setAdapter之前调用
     */
    protected void setupListHeader(boolean addFooter) {
        // Add Header to ListView
        // mListHeader = View.inflate(this, R.layout.listview_header, null);
        // mTweetList.addHeaderView(mListHeader, null, true);
    	container.setOnRefreshListener(new OnRefreshListener(){
    		@Override
    		public void onRefresh(){
//    			doRetrieve();
    			doDraw();
    		}
    	});
    	
		container.setOnLoadMoreListener(new OnLoadMoreListener() {

			@Override
			public void onLoadMore() {
				doGetMore();
			}
		});

//        // Add Footer to ListView
//        mListFooter = View.inflate(this, R.layout.listview_footer, null);
//        mTweetList.addFooterView(mListFooter, null, true);
//        
//        // Find View
//        loadMoreBtn = (TextView) findViewById(R.id.ask_for_more);
//        loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
//        loadMoreBtnTop = (TextView) findViewById(R.id.ask_for_more_header);
//        loadMoreGIFTop = (ProgressBar) findViewById(R.id.rectangleProgressBar_header);
    }
	
    public void draw() {
    	Log.v("Comment draw", String.valueOf(allCommentsList.size()));
		mCommentListAdapter.refresh(allCommentsList);
		
//		adapter.notifyDataSetChanged();
    	
//    	for(Comment cm: allCommentsList) {
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
        	if(mCommentListAdapter!=null) {
        		mCommentListAdapter.setBpItemView(false);
        	}
        	container.onRefreshComplete();
        	if(task == mGetMoreTask) {
        		container.onLoadMoreComplete();
        	}

            if (result == TaskResult.AUTH_ERROR) {
                mFeedback.failed("登录信息出错");
                logout();
            } else if (result == TaskResult.OK) {
            	Log.v("MsgType prost process", "normal get list data");
                draw();
                if (task == mDrawTask) {
                    goTop();
                }
            } else if (result == TaskResult.IO_ERROR) {
                // FIXME: bad smell
                if (task == mDrawTask) {
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
            Log.v("taskadapter", "postexecute!");
        }
        
        @Override
        public void onPreExecute(GenericTask task) {
            //mRetrieveCount = 0;
            //mTweetList.prepareForRefresh();
            if (TwitterApplication.DEBUG) {
                DebugTimer.start();
            }
            if(task == mDrawTask) {
            	if(mCommentListAdapter!=null) {
            		mCommentListAdapter.setBpItemView(true);
            	}
        	}
        }

        @Override
        public void onProgressUpdate(GenericTask task, Object param) {
            // Log.d(TAG, "onProgressUpdate");
            draw();
            if(task == mDrawTask) {
            	if(mCommentListAdapter!=null) {
//            		mCommentListAdapter.setBpItemView(true);
            	}
        	}
        }
    };
    
    public void goTop() {
        Log.d(TAG, "goTop.");
        container.setSelection(1);
    }
	
	private class RetrieveTask extends GenericTask {
        private String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.codeim.floorview.bean.Comment> commentsList = null;
			//mRefreshFrequency = 0;
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();

            try 
            {
//            	commentsList = getComments(0, 6);
    		    page_size=13;
    		    page_index=0;
    		    last_id=0;
    		    commentsList = getComments(page_size, page_index,last_id, infoId);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, commentsList));
            init_comments();
			for (com.codeim.floorview.bean.Comment comment: commentsList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				//Comment u = Comment.create(comment);
				Comment u = new Comment(comment);
				allCommentsList.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			
			com.codeim.floorview.bean.Comment comment = allCommentsList.get(allCommentsList.size()-1);
			if(comment.getId()<0) {
				allCommentsList.remove(allCommentsList.size()-1);
				container.data_finish = true;
			}
			if(allCommentsList.size()>0) {
				last_id = (int) allCommentsList.get(allCommentsList.size()-1).getId();
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
            List<com.codeim.floorview.bean.Comment> commentsList = null;
			//mRefreshFrequency = 0;
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();

			if(container.data_finish) {
				return TaskResult.OK;
			}
            try 
            {
//            	commentsList = getComments(0, 6);
    		    //page_size=0;
    		    page_index++;
//    		    last_id=0;
//    		    if(!data_finish)
    		    commentsList = getComments(page_size, page_index,last_id, infoId);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, commentsList));
//            //allCommentsList.clear();
			for (com.codeim.floorview.bean.Comment comment: commentsList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				//Comment u = Comment.create(comment);
				Comment u = new Comment(comment);
				allCommentsList.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			
			com.codeim.floorview.bean.Comment comment = allCommentsList.get(allCommentsList.size()-1);
			if(comment.getId()<0) {
				allCommentsList.remove(allCommentsList.size()-1);
				container.data_finish = true;
			}
			if(allCommentsList.size()>0) {
				last_id = (int) allCommentsList.get(allCommentsList.size()-1).getId();
			} else {
				no_data = true;
			}

            return TaskResult.OK;
        }
    }
    
    public void doDraw() {
        Log.d(TAG, "Attempting retrieve.");
        
        container.data_finish=false;
        no_data = false;

        if (mDrawTask != null && mDrawTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mDrawTask = new RetrieveTask();
            mDrawTask.setFeedback(mFeedback);
            mDrawTask.setListener(mRetrieveTaskListener);
            mDrawTask.execute();

            // Add Task to manager
            taskManager.addTask(mDrawTask);
        }
    }
    public void doGetMore() {
        Log.d(TAG, "Attempting getMore.");
        
        if(container.data_finish) {
        	container.onLoadMoreComplete();
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
    
	protected void registerOnClickListener(ListView listView) {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if(mCommentListAdapter.getItemViewType(position)!=0) {
					return;
				}
				final Comment comment = getContextItemTweet(position);
				RelativeLayout pop_view;
				RadioButton reply;
				RadioButton report;
				RadioButton enjoy;
				RadioButton owner_contact;
				RadioButton owner_ok;
				
				parentid = (int) comment.getId();
				floornum = (int) comment.getFloorNum();
				boolean isAvailable = comment.isAvailable();

//				pop_view = ( RelativeLayout ) view.findViewById ( R.id.comment_pop_menu ) ; 
				pop_view = ( RelativeLayout ) inflater.inflate(R.layout.comment_pop_menu, parent, false);
				
				final PopupWindow pop = new PopupWindow(pop_view,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false); 
//				pop.setBackgroundDrawable(new BitmapDrawable());
				pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bar));
				pop.setOutsideTouchable(true);
				pop.setFocusable(true);
				
				reply         = (RadioButton) pop_view.findViewById(R.id.comment_btn_reply);
				report        = (RadioButton) pop_view.findViewById(R.id.comment_btn_report);
				enjoy         = (RadioButton) pop_view.findViewById(R.id.comment_btn_enjoy);
				owner_contact = (RadioButton) pop_view.findViewById(R.id.comment_btn_owner_contact);
				owner_ok      = (RadioButton) pop_view.findViewById(R.id.comment_btn_owner_ok);
				
//				pop_view.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if(v.getId()==R.id.comment_btn_reply) {
//							Intent intent = new Intent(CommentTestActivity.this, CommentWriteActivity.class);
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
						Intent intent = new Intent(CommentPinnedSectionActivity.this, CommentWriteActivity.class);
						Bundle bundle=new Bundle();
						intent.putExtra("parentid", parentid);
						intent.putExtra("floornum", floornum);
						intent.putExtras(bundle);
						startActivity(intent);
						pop.dismiss();
				    }
				});
				if (comment == null) {
					Log.d(TAG,String.valueOf(position));
					Log.d(TAG,String.valueOf(id));
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else {
					//Tweet tweet;
					Log.d(TAG,String.valueOf(position));
					Log.d(TAG,String.valueOf(id));
					
					if(pop.isShowing()) {
//					    pop.dismiss();
					} else {
						//only the info belongs to the user and the comment is not itself, the user can be connect and release the order
						if(Integer.valueOf(TwitterApplication.getMyselfId(true))==Integer.valueOf(TwitterApplication.mPref.getString(Preferences.CURRENT_USER_ID, ""))
								&&Integer.valueOf(TwitterApplication.getMyselfId(true))!=comment.getUserId()) {
							owner_contact.setVisibility(View.VISIBLE);
							owner_ok.setVisibility(View.VISIBLE);
						} else {
							owner_contact.setVisibility(View.GONE);
							owner_ok.setVisibility(View.GONE);
						}
						
			            int[] arrayOfInt = new int[2];
			            //获取点击按钮的坐标
//			            view.getLocationInWindow(arrayOfInt);
			            view.getLocationOnScreen(arrayOfInt);
			            int x = arrayOfInt[0];
			            int y = arrayOfInt[1];
			            
			            DisplayMetrics  dm = new DisplayMetrics();
			            getWindowManager().getDefaultDisplay().getMetrics(dm);
			            int screenWidth = dm.widthPixels;
			            int screenHeight = dm.heightPixels;

			            if(isAvailable) {
			            pop_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//						pop.showAtLocation(parent, Gravity.CENTER_VERTICAL, 100, 100);
//						pop.showAtLocation(view, 0 , screenWidth/2-pop.getWidth()/2, y+pop.getHeight());	
			            pop.showAtLocation(view, 0 , screenWidth/2-150, y+50);
//						pop.showAsDropDown(view, 0, 0);
						pop.update(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			            }
					}
					// Log.d(TAG, "User: " + user.statusInReplyToStatusId + " " + user.statusInReplyToUserId + " " + user.statusInReplyToScreenName);
					// Log.d(TAG, "user attachmentUrl " + user.attachmentUrl);
//				    tweet = User.userSwitchToTweet(user);
					// Log.d(TAG, "tweet: " + tweet.toString());
//				    launchActivity(StatusWithCommentActivity.createIntent(tweet));
					// launchActivity(StatusActivity.createIntent(user));
				}
			}
		});
		
		mCommentListAdapter.setOnClickDownArrow(new OnClickDownArrow() {
			@Override
			public void downArrowPop(int position) {
//				final Info info = getContextItemTweet(position);
				final Info mInfo = info;

				if (mInfo == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else {
					Log.d(TAG,String.valueOf(position));
					
					//the method not use in commentActivity
					//makePopDialog(mInfo);
				}
				
			}
		});
		
		mCommentListAdapter.setOnTimeSet(new OnTimeSet() {
			@Override
			public void clickTimeSet(int position) {
				// TODO Auto-generated method stub
//				final Info info = getContextItemTweet(position);
				final Info mInfo = info;//getListItem(position);
				
				CommonTask.showAddTimeDialog(CommentPinnedSectionActivity.this, info, mChangeTimeInterface);
            
			}
		});
		
		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector.onTouchEvent(event);
//				return false;  //false --> onTouchEvent(event)
				
			}
			
		});
	}
	
	/***doChangeTime related method start***/
	ChangeTimeInterface mChangeTimeInterface = new ChangeTimeInterface() {
		@Override
		public void doChangeTime(String id, String TAG, Date expireTime, Date newTime, long addTime) {

			if(DateTimeHelper.getNowTime()>expireTime.getTime()) {
				return;
			}
			if (mChangeTimeTask != null && mChangeTimeTask.getStatus() == GenericTask.Status.RUNNING) {
				return;
			} else {
				mChangeTimeTask = new CommonTask.ChangeTimeTask();
				mChangeTimeTask.setListener(mChangeTimeTaskListener);

				TaskParams params = new TaskParams();
				params.put("id", id);
				params.put("TAG", TAG);
				params.put("newTime", DateTimeHelper.dateToString(newTime, ""));
				params.put("addTime", String.valueOf(addTime));
				mChangeTimeTask.execute(params);
				
				Log.e(TAG, "doChangeTime"+id);
				toChangeTimeId = id;
				afterChangeTime = newTime;
				
				taskManager.addTask(mChangeTimeTask);
			}
		}
	};

	private TaskListener mChangeTimeTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "ChangeTimeTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				if(task == mChangeTimeTask)
				    afterChangeTime = ((CommonTask.ChangeTimeTask)mChangeTimeTask).afterChangeTime;
				onChangeTimeSuccess(toChangeTimeId, afterChangeTime);
			} else if (result == TaskResult.IO_ERROR) {
				onChangeTimeFailure();
			}
		}
	};

	public void onChangeTimeSuccess(String toChangeTimeId, Date afterChangeTime) {
		Toast.makeText(CommentPinnedSectionActivity.this, "更新有效期成功", Toast.LENGTH_SHORT).show();
		
		Log.e(TAG, "onChangeTimeSuccess"+toChangeTimeId);
		if(allCommentsList != null &&allCommentsList.get(0)!=null && allCommentsList.get(0).info!=null) {
		    allCommentsList.get(0).info.expireTime = afterChangeTime;
		}
		info.expireTime = afterChangeTime;
		//changeExpireAsInfoId(toChangeTimeId, afterChangeTime);

		mCommentListAdapter.refresh();
	}
	public void onChangeTimeFailure() {
		Log.e(TAG, "ChangeTime failed");
	}
	/***doChangeTime related method end***/
	
	
	//this onTouch Not used,  the upper viewgroup onTouch not used
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
//	            return true;
	            return false; // false --> dispatchTouchEvent to the listview
	        }  
	 };
	 //hide or show the reply edit
	 public void doResult(int action) {
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
    public List<Comment> getComments(int page_size, int page_index,int last_id, int info_id) throws HttpException {
	    return getApi().getFlyCommentRefresh(page_size, page_index,last_id, info_id);
	}
    
	protected Comment getContextItemTweet(int position) {
        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mCommentListAdapter.getCount()) {
        	Comment comment = (Comment) mCommentListAdapter.getItem(position);
            if (comment == null) {
                return null;
            } else {
                return comment;
            }
        } else {
            return null;
        }
	}
    
	protected void specialItemClicked(int position) {
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
	            
	    		if(allCommentsList != null &&allCommentsList.get(0)!=null && allCommentsList.get(0).info!=null) {
	    		    allCommentsList.get(0).info.conversationCount = comment_cnt;
	    		}
	    		info.conversationCount = comment_cnt;
	            
	    		//doDraw();  //test
	    		
	    		mCommentListAdapter.refresh();
//	            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
	        }
	    }
	}
	
	public String getInfoId() {
		if(this.info!=null) return info.id;
		else return "-1";
	}
	
}
