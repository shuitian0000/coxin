package com.codeim.byme.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.codeim.byme.ListByMyActivity;
import com.codeim.byme.base.OnFragmentActivityTouchListener;
import com.codeim.coxin.NearbyActivity;
import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.hardware.ShakeListener;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.location.LocationUtils;
import com.codeim.coxin.task.CommonTask;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskManager;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.task.CommonTask.ChangeTimeInterface;
import com.codeim.coxin.ui.base.Refreshable;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.FlingGestureListener;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter.OnTimeSet;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DebugTimer;
import com.codeim.coxin.view.InfoMenuDialog;
import com.codeim.coxin.view.MultiChoiceDialog;
import com.codeim.coxin.view.MultiChoiceDialog.OnNegativeButton;
import com.codeim.coxin.view.MultiChoiceDialog.OnPositiveButton;
import com.codeim.coxin.widget.PullToRefreshListView;
import com.codeim.coxin.widget.PullToRefreshListView.OnRefreshListener;
import com.codeim.floorview.CommentPinnedSectionActivity;
import com.codeim.floorview.CommentWriteActivity;

public class InfoListFragment extends Fragment implements Refreshable{
	static final String TAG = "InfoListFragment";
	private String TYPE;
	
	private Context mContext;
	private View contextView;
	private Activity contextActivity;
	
	protected SharedPreferences mPreferences;
	protected String mNameType = "";  // 不同页面的标记，用于刷新计时
	volatile private int mRefreshFrequency = 0;
	protected static final int STATE_ALL = 0;
	protected static final String SIS_RUNNING_KEY = "running";
	
	// Refresh data at startup if last refresh was this long ago or greater.
    private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;
    
    protected static int lastPosition = 0;
    protected static int scrollTop = 0;
    protected ShakeListener mShaker = null;
    
    private LocationClient mLocClient;
	private SearchLocationObserver mSearchLocationObserver = new SearchLocationObserver();
    
    // Tasks.
    protected TaskManager taskManager = new TaskManager();
    private GenericTask mRetrieveTask;
    private GenericTask mGetMoreTask;
    protected Feedback mFeedback;
    
	protected GenericTask mDeleteTask;
	protected GenericTask mChangeTimeTask;
	protected GenericTask mReportTask;
    
    protected NearbyInfoArrayAdapter mInfoListAdapter;
    protected PullToRefreshListView mTweetList;  // ��Ÿ����û����б�
    protected View mListFooter;
    protected TextView loadMoreBtn;
    protected ProgressBar loadMoreGIF;
    protected TextView loadMoreBtnTop;
    protected ProgressBar loadMoreGIFTop;
    
	protected int page_size;
	protected int page_index;
	protected int last_id;
    
    private boolean data_finish;
    private boolean no_data;
    private int mRetrieveCount = 0;
    
    private int mCurrentScrollState;
    private boolean mIsLoading;
    private OnScrollListener mOnScrollListener;
    
	protected static final String ALLINFO = "0";
    
    protected String mInfoType = ALLINFO;
	protected double mLat = 31.20021; //ά��
	protected double mLng = 121.589149;//����
	protected static int Nearby_Place=0;
	protected boolean isFirstLoc = true;
	
    private Thread thread_timer;
    private boolean timer_close;
    
	private String toDeleteId;
	private String toChangeTimeId;
	private Date afterChangeTime;
	
	private InfoMenuDialog info_menu_alertDialog;
	ArrayList<String> infoMenuOptionList = new ArrayList<String>();
    
    volatile protected ArrayList<com.codeim.coxin.data.Info> allInfoList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		contextView = inflater.inflate(R.layout.info_list_fragment, container, false);
		
		//��ȡActivity���ݹ����Ĳ���
		Bundle mBundle = getArguments();
		TYPE = mBundle.getString("TYPE");
		Log.v(TAG, TYPE);
		
		mPreferences = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(this);
		
		setMyTouchListener();   
		
		return contextView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		contextActivity = this.getActivity();
		mContext = this.getActivity();
		mFeedback = FeedbackFactory.create(this.getActivity(), FeedbackType.PROGRESS);
		mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);
		setupState();
		
		boolean shouldRetrieve = false;
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

        registerOnClickListener(getListView());
        registerGestureListener();  // ����ʶ��
        registerShakeListener();  // �ζ�ˢ��
        
        start_timer();
	}
	
	protected void setupState() {
		mLocClient = ((TwitterApplication)this.getActivity().getApplication()).mLocationClient;  // �ٶ�λ�û�ȡ�ͻ���
		
        mTweetList = (PullToRefreshListView) contextView.findViewById(R.id.tweet_list);
        setupListHeader(true);
        allInfoList = new ArrayList<com.codeim.coxin.data.Info>();
		mInfoListAdapter = new NearbyInfoArrayAdapter(this.getActivity());
		mTweetList.setAdapter(mInfoListAdapter);
		mTweetList.setHeaderDividersEnabled(false);
        
        data_finish = false;
        no_data=false;
		
	}
	
    /**
     * ��listView�ײ� - ������� NOTE: ������listView#setAdapter֮ǰ����
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
	
    private TaskListener mRetrieveTaskListener = new TaskAdapter() {

        @Override
        public String getName() {
            return "RetrieveTask";
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            // ˢ�°�ťֹͣ��ת
            loadMoreGIF.setVisibility(View.GONE);
            mTweetList.onRefreshComplete();
            
            //add by ywwang for pull to getMore
            if(task == mGetMoreTask) {
            	onLoadMoreComplete();
            }

            if (result == TaskResult.AUTH_ERROR) {
                mFeedback.failed("��¼��Ϣ����");
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
    
    public void draw() {
        //mTweetAdapter.refresh();
		mInfoListAdapter.refresh(allInfoList);
    }
	
	/****
	 * get the information
	 */
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
	
	private void doDelete(String id) {

		if (mDeleteTask != null && mDeleteTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
//			mDeleteTask = new TweetCommonTask.DeleteTask(this);
			mDeleteTask = new CommonTask.DeleteTask();
			mDeleteTask.setListener(mDeleteTaskListener);

			TaskParams params = new TaskParams();
			params.put("id", id);
			mDeleteTask.execute(params);
			
			Log.e(TAG, "doDelete"+id);
			toDeleteId = id;
			
			taskManager.addTask(mDeleteTask);
		}
	}
	
	public void doReport(String info_id, String reportContent) {
		if (mReportTask != null && mReportTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
//			mDeleteTask = new TweetCommonTask.DeleteTask(this);
			mReportTask = new CommonTask.ReportTask();
			mReportTask.setListener(mReportTaskListener);

			TaskParams params = new TaskParams();
			params.put("infoId", info_id);
			params.put("report", reportContent);
			mReportTask.execute(params);
			
			taskManager.addTask(mReportTask);
		}
	}
    
    public void goTop() {
        Log.d(TAG, "goTop.");
        mTweetList.setSelection(1);
    }
    
    public ListView getListView() {
        return mTweetList;
    }
    
	protected void registerOnClickListener(ListView listView) {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.e(TAG, "setOnItemClickListener position: "+String.valueOf(position));
				final Info info = getContextItemTweet(position);
//				Info info = (Info) parent.getAdapter().getItem(position);

				if (info == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else if(info.expire==0){
					Log.d(TAG,String.valueOf(position));
					
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_ID, info.owerId.toString() ).commit();
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_USERNAME, info.owerName.toString() ).commit();
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_ID, info.id.toString() ).commit();
					Intent intent = new Intent(contextActivity, CommentPinnedSectionActivity.class);
					intent.putExtra("INFO", info);
					startActivityForResult(intent, 200);
					
				}
			}
		});
		
		//�������� ----  ��ɫ����������,��������һ��
//		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//				Info info = getContextItemTweet(position);
//
//				if (info == null) {
//					Log.w(TAG, "Selected item not available.");
//					specialItemClicked(position);
//				} else {
//					Tweet tweet;
//					Log.d(TAG,String.valueOf(position));
//					
//					RelativeLayout pop_view;
//					RadioButton reply;
//					RadioButton report;
//					RadioButton dele;
// 
//					pop_view = ( RelativeLayout ) inflater.inflate(R.layout.info_pop_menu, parent, false);
//					
//					final PopupWindow pop = new PopupWindow(pop_view,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false); 
////					pop.setBackgroundDrawable(new BitmapDrawable());
//					pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bar));
//					pop.setOutsideTouchable(true);
//					pop.setFocusable(true);
//					
//					reply         = (RadioButton) pop_view.findViewById(R.id.info_btn_reply);
//					report        = (RadioButton) pop_view.findViewById(R.id.info_btn_report);
//					dele         = (RadioButton) pop_view.findViewById(R.id.info_btn_delete);
//					
//					if (info == null) {
//						Log.d(TAG,String.valueOf(position));
//						Log.d(TAG,String.valueOf(id));
//						Log.w(TAG, "Selected item not available.");
//						specialItemClicked(position);
//					} else {
//						//Tweet tweet;
//						Log.d(TAG,String.valueOf(position));
//						Log.d(TAG,String.valueOf(id));
//						
//						if(pop.isShowing()) {
////						    pop.dismiss();
//						} else {
//							//only the info belongs to the user, the user can be delete the info
//							if(Integer.valueOf(TwitterApplication.getMyselfId(true))==Integer.valueOf(info.owerId)) {
//								reply.setVisibility(View.VISIBLE);
//								report.setVisibility(View.VISIBLE);
//								dele.setVisibility(View.VISIBLE);
//							} else {
//								reply.setVisibility(View.VISIBLE);
//								report.setVisibility(View.VISIBLE);
//								dele.setVisibility(View.GONE);
//							}
//							
//				            int[] arrayOfInt = new int[2];
//				            //��ȡ�����ť������
////				            view.getLocationInWindow(arrayOfInt);
//				            view.getLocationOnScreen(arrayOfInt);
//				            int x = arrayOfInt[0];
//				            int y = arrayOfInt[1];
//				            
//				            DisplayMetrics  dm = new DisplayMetrics();
//				            getWindowManager().getDefaultDisplay().getMetrics(dm);
//				            int screenWidth = dm.widthPixels;
//				            int screenHeight = dm.heightPixels;
//
//				            pop_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
////							pop.showAtLocation(parent, Gravity.CENTER_VERTICAL, 100, 100);
////							pop.showAtLocation(view, 0 , screenWidth/2-pop.getWidth()/2, y+pop.getHeight());	
//				            pop.showAtLocation(view, 0 , screenWidth/2-150, y+50);
////							pop.showAsDropDown(view, 0, 0);
//							pop.update(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//						}
//					}
//				}
//				
//				return true; //no call onItemClick
//			}
//		});
		
		//�������� ----  dialog ����  popupwindow
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final Info info = getContextItemTweet(position);

				if (info == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else {
					Log.d(TAG,String.valueOf(position));
					
					makePopDialog(info);
				}
				
				return true; //no call onItemClick
			}
		});
		
		mInfoListAdapter.setOnClickDownArrow(new com.codeim.coxin.ui.module.NearbyInfoArrayAdapter.OnClickDownArrow() {
			@Override
			public void downArrowPop(int position) {
//				final Info info = getContextItemTweet(position);
				final Info info = getListItem(position);

				if (info == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else {
					Log.d(TAG,String.valueOf(position));
					
					makePopDialog(info);
				}
				
			}
		});
		
		mInfoListAdapter.setOnTimeSet(new OnTimeSet() {
			@Override
			public void clickTimeSet(int position) {
				// TODO Auto-generated method stub
//				final Info info = getContextItemTweet(position);
				final Info info = getListItem(position);
				
				//showAddTimeDialog(info);
				CommonTask.showAddTimeDialog(contextActivity, info, mChangeTimeInterface);
            
			}
		});
	}
	
    protected Info getContextItemTweet(int position) {
        position = position - 1;
        // ��ΪList����Header��footer������Ҫ������һ���Լ��������һ��
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
    
    protected void specialItemClicked(int position) {
        // ע�� mTweetAdapter.getCount �� mTweetList.getCount������
        // ǰ�߽��������ݵ�������������foot��head�������߰���foot��head
        // �����ͬʱ����foot��head������£�list.count = adapter.count + 2
        if (position == 0) {
            // ��һ��Item(header)
            loadMoreGIFTop.setVisibility(View.VISIBLE);
            doRetrieve();
        } 
//        else if (position == mTweetList.getCount() - 1) {
//            // ���һ��Item(footer)
//            loadMoreGIF.setVisibility(View.VISIBLE);
//            doGetMore();
//        }
    }
    
    protected Info getListItem(int position) {
//      position = position - 1;
      // ��ΪList����Header��footer������Ҫ������һ���Լ��������һ��
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
//			    mChangeTimeTask = new TweetCommonTask.DeleteTask(this);
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
//				logout();
			} else if (result == TaskResult.OK) {
				if(task == mChangeTimeTask)
					afterChangeTime = ((CommonTask.ChangeTimeTask)mChangeTimeTask).afterChangeTime;
				onChangeTimeSuccess(toChangeTimeId, afterChangeTime);
			} else if (result == TaskResult.IO_ERROR) {
				onChangeTimeFailure();
			}
		}
	};
	private TaskListener mDeleteTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "DeleteTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
//				logout();
			} else if (result == TaskResult.OK) {
				onDeleteSuccess(toDeleteId);
			} else if (result == TaskResult.IO_ERROR) {
				onDeleteFailure();
			}
		}
	};
	private TaskListener mReportTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "ReportTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
//				logout();
			} else if (result == TaskResult.OK) {
//				onReportSuccess();
			} else if (result == TaskResult.IO_ERROR) {
				onReportFailure();
			}
		}
	};
	public void onChangeTimeSuccess(String toChangeTimeId, Date afterChangeTime) {
		Toast.makeText(mContext, "������Ч�ڳɹ�", Toast.LENGTH_SHORT).show();
		
		Log.e(TAG, "onChangeTimeSuccess"+toChangeTimeId);
		changeExpireAsInfoId(toChangeTimeId, afterChangeTime);
		//mTweetAdapter.refresh();
		mInfoListAdapter.refresh();
	}
	public void onChangeTimeFailure() {
		Log.e(TAG, "ChangeTime failed");
	}
	public void onDeleteSuccess(String toDeleteId) {
		Toast.makeText(mContext, "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
		
		Log.e(TAG, "onDeleteSuccess"+toDeleteId);
		removeAsInfoId(toDeleteId);
		//mTweetAdapter.refresh();
		mInfoListAdapter.refresh(allInfoList);
	}
	public void onDeleteFailure() {
		Log.e(TAG, "Delete failed");
	}
	public void onReportFailure() {
		Log.e(TAG, "Report process failed!");
	}
    
	protected void makePopDialog(final Info info) {
		DisplayMetrics outMetrics = new DisplayMetrics(); 
		this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		int mScreenHeight = outMetrics.heightPixels;
		int mScreenWidth = outMetrics.widthPixels;
		
		infoMenuOptionList.clear();
		infoMenuOptionList.add("�ظ�");
		
		if(Integer.valueOf(TwitterApplication.getMyselfId(true))
				== Integer.valueOf(info.owerId)) {
		    infoMenuOptionList.add("ɾ��");
		} else {
			infoMenuOptionList.add("�ٱ�");
		}
		
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View mView = inflater.inflate(R.layout.info_pop_menu_view, null);
		
        info_menu_alertDialog = new InfoMenuDialog((int) (mScreenWidth * 0.7), android.view.WindowManager.LayoutParams.WRAP_CONTENT,
        		infoMenuOptionList, mContext, R.layout.info_pop_menu_view);
		info_menu_alertDialog.show();
		info_menu_alertDialog.refresh();
		WindowManager.LayoutParams lp = info_menu_alertDialog.getWindow().getAttributes();
		lp.width = (int) (mScreenWidth * 0.7);
		lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity = Gravity.CENTER;
		info_menu_alertDialog.getWindow().setAttributes(lp);
		
		info_menu_alertDialog.setOnOptionSelected(new com.codeim.coxin.view.InfoMenuDialog.OnOptionSelected() {
			@Override
			public void selected(int position, String option) { //д����ҳ�棬����һ��Activity
				if(option.contains("�ظ�")) {
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_ID, info.owerId.toString() ).commit();
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_USERNAME, info.owerName.toString() ).commit();
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_ID, info.id.toString() ).commit();
					
					Intent intent = new Intent(mContext, CommentWriteActivity.class);
					Bundle bundle=new Bundle();
					intent.putExtra("parentid", 0);
					intent.putExtra("floornum", 0);
					intent.putExtras(bundle);
					startActivityForResult(intent, 100);
					
					info_menu_alertDialog.dismiss();
				} else if(option.contains("ɾ��")) { //ɾ��ҳ�棬�ص���һ���Ƿ�ɾ����dialog,���õ���setContentView�Զ��巽ʽ
					LayoutInflater inflater = (LayoutInflater) contextActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.base_dialog, null);
//					final WindowManager.LayoutParams lp = getWindow().getAttributes();
					
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogStyle);
					final AlertDialog myDialog = builder.create();
					myDialog.show();
					myDialog.getWindow().setContentView(R.layout.base_dialog);
//					lp.alpha = 0.3f;
//					lp.dimAmount=0.5f;
//					getWindow().setAttributes(lp);
//					getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					
					final TextView v= (TextView) myDialog.getWindow().findViewById(R.id.title);
					final Button positiveButton = (Button) myDialog.getWindow().findViewById(R.id.positiveButton);
					final Button negativeButton = (Button) myDialog.getWindow().findViewById(R.id.negativeButton);
					v.setText("��ȷ��ɾ����");
					positiveButton.setText("ȷ��");
					negativeButton.setText("ȡ��");
					positiveButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							myDialog.dismiss();
							doDelete(info.id);
						}
					});
					negativeButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
//							doDelete(info.id);
							myDialog.dismiss();
						}
					});
                    myDialog.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							// TODO Auto-generated method stub
//							lp.alpha = 1.0f;
//							lp.dimAmount=0.0f;
//							getWindow().setAttributes(lp);
//							getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
						}
                    	
                    });
					
					info_menu_alertDialog.dismiss();
				} else if(option.contains("�ٱ�")) { //�ٱ�ҳ�棬�ص����ٱ������Լ��ٱ�ȷ�ϵ�dialog,���õ����Զ���view�ķ�ʽ���������layout

					DisplayMetrics outMetrics = new DisplayMetrics(); 
					contextActivity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
					int mScreenHeight = outMetrics.heightPixels;
					int mScreenWidth = outMetrics.widthPixels;
					
					ArrayList<String> arrayReportList = new ArrayList<String>();
					arrayReportList.clear();
					arrayReportList.add("����Ӫ��");
					arrayReportList.add("Υ������");
					arrayReportList.add("���ױ���");
					arrayReportList.add("��ʵ��Ϣ");
					arrayReportList.add("������Ϣ");
					arrayReportList.add("��ʵ��Ϣ");
					arrayReportList.add("��ʵ��Ϣ");
					arrayReportList.add("��ʵ��Ϣ");
					arrayReportList.add("��ʵ��Ϣ");
					arrayReportList.add("��ʵ��Ϣ");
					arrayReportList.add("����");
					
					//1.aleartDialog method
//					final String ReportList[]={"����Ӫ��","Υ������","���ױ���","��ʵ��Ϣ","������Ϣ","��ʵ��Ϣ",
//							"��ʵ��Ϣ","��ʵ��Ϣ","��ʵ��Ϣ","��ʵ��Ϣ","����"};  
//			        final boolean selected[]={false,false,false,false,false,false,false,false,false,false,false};  
//					final AlertDialog.Builder myDialogBuilder = new AlertDialog.Builder(NearbyActivity.this);
//					myDialogBuilder.setTitle("ѡ��ٱ�����");
//			        myDialogBuilder.setMultiChoiceItems(ReportList,selected,new DialogInterface.OnMultiChoiceClickListener() {  
//			            @Override  
//			            public void onClick(DialogInterface dialog, int which, boolean isChecked) {  
//			               // dialog.dismiss(); 
//			                String select_item = ReportList[which].toString();
//	                        Toast.makeText(NearbyActivity.this,
//	                                 "ѡ����--->>" + select_item, Toast.LENGTH_SHORT)
//	                                 .show();
//			            }  
//			        }); 
//			        myDialogBuilder.setPositiveButton("ȡ��", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							String re="";
//							for(int i=0; i<6;i++) {
//								if(selected[i]) {
//									re=re+ReportList[i];
//								}
//							}
//							doReport(info.id, re);
//							dialog.dismiss();
//						}
//					});
//			        myDialogBuilder.setNegativeButton("ȷ��", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							dialog.dismiss();
//						}
//					});
//			        myDialogBuilder.create().show();
					
					//2.custom Dialog method
					final MultiChoiceDialog myDialog = new MultiChoiceDialog((int) (mScreenWidth * 0.7), android.view.WindowManager.LayoutParams.WRAP_CONTENT,
							arrayReportList, contextActivity, R.layout.base_dialog);
					myDialog.setTitleText("ѡ��ٱ�����");
					myDialog.show();
					myDialog.refresh();
					WindowManager.LayoutParams lp = myDialog.getWindow().getAttributes();
					lp.width = (int) (mScreenWidth * 0.7);
//					lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
					lp.height = (int) ((mScreenHeight*0.8)<650?(mScreenHeight*0.8):650);
					lp.gravity = Gravity.CENTER;
					myDialog.getWindow().setAttributes(lp);
					
					myDialog.setOnPositiveButton(new OnPositiveButton() {
						@Override
						public void positiveButtonselected() {
							// TODO Auto-generated method stub
							doReport(info.id, myDialog.getAllSelected());
							myDialog.dismiss();
						}
					});
					myDialog.setOnNegativeButton(new OnNegativeButton() {
						@Override
						public void negativeButtonselected() {
							// TODO Auto-generated method stub
							myDialog.dismiss();
						}
					});

					
					info_menu_alertDialog.dismiss();
				}
			}
		});
	}
    
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
            List<com.codeim.coxin.fanfou.Info> infosList = null;
			mRefreshFrequency = 0;
			TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();

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
			TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();

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
    
    
	public List<com.codeim.coxin.fanfou.Info> getNearbyInfo(int page_size, int page_index, int last_id, String infoType, 
	        double lat, double lng) throws HttpException {
		if(TYPE.contains("my_send")) {
			return getApi().getFlyInfomsgMySend(page_size, page_index, last_id, infoType, lat, lng);
		} else if (TYPE.contains("my_reply")) {
			return getApi().getFlyInfomsgMyReply(page_size, page_index, last_id, infoType, lat, lng);
		} else if (TYPE.contains("location")) {
			return getApi().getFlyInfomsgRefreshLocation(page_size, page_index, last_id, infoType, lat, lng);
		} else {
			return getApi().getFlyInfomsgRefreshLocation(page_size, page_index, last_id, infoType, lat, lng);
		}
	}
    
    public void start_thread() {
    	timer_close = true;
    }
    public void end_thread() {
    	timer_close = false;
    }
	public void start_timer(){
		thread_timer = new  Thread(){
			boolean update_en = false;
			public void run() {
				while(true){
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
	            	if(mInfoListAdapter!=null) {
	            	    mInfoListAdapter.refresh();
	            	}
	            }  
	        }  
	    }; 
	
	public static boolean isTrue(Bundle bundle, String key) {
		return bundle != null && bundle.containsKey(key) && bundle.getBoolean(key);
	}
	
	public Weibo getApi() {
		return TwitterApplication.mApi;
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

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (useGestrue && myGestureListener != null) {
//            return myGestureListener.getDetector().onTouchEvent(event);
//        }
//        return super.onTouchEvent(event);
//    }
    private void setMyTouchListener() {
		/* Fragment�У�ע�� 
 	    * ����MainActivity��Touch�ص��Ķ��� 
 	    * ��д���е�onTouchEvent�����������и�Fragment���߼����� 
 	    */  
    	OnFragmentActivityTouchListener myTouchListener = new OnFragmentActivityTouchListener() {  
 	        @Override  
 	        public void onTouchEvent(MotionEvent event) {  
 	        // ���������¼� 
 	           if (useGestrue && myGestureListener != null) {
 	              myGestureListener.getDetector().onTouchEvent(event);
 	          }
 	        }  
 	    };  
 	          
 	    // ��myTouchListenerע�ᵽ�ַ��б�  
 	   ((ListByMyActivity)this.getActivity()).registerMyTouchListener(myTouchListener);   
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
	    	mShaker = new ShakeListener(this.getActivity());
	    	mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
				
				@Override
				public void onShake() {
					Log.v(TAG, "onShake");
					doRetrieve();
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
	public void onResume() {
        Log.d(TAG, "onResume.");
//        if (lastPosition != 0) {
//            mTweetList.setSelection(lastPosition);
//        }
        if (mShaker != null){
        	mShaker.resume();
        }
        super.onResume();
        //checkIsLogedIn();
		((TwitterApplication) this.getActivity().getApplication()).requestLocationUpdates(mSearchLocationObserver);
		setLocationOption();  // �ٶ�λ��
		mLocClient.start();  // �ٶ�λ��
		
        if (lastPosition != 0) {
        	mTweetList.setSelectionFromTop(lastPosition, scrollTop);
        }
    }
	
	@Override
	public void onPause() {
        Log.d(TAG, "onPause.");
        if (mShaker != null){
        	mShaker.pause();
        }
        super.onPause();
        
        lastPosition = mTweetList.getFirstVisiblePosition();
        View v=mTweetList .getChildAt(0);
        scrollTop=(v==null)?0:v.getTop();
        
		((TwitterApplication) this.getActivity().getApplication()).removeLocationUpdates(mSearchLocationObserver);
		mLocClient.stop();  // �ٶ�λ��
		mInfoListAdapter.stopPlay();  // ֹͣ��������
    }

    @Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRetrieveTask != null && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            outState.putBoolean(SIS_RUNNING_KEY, true);
        }
    }
    @Override
	public void onDestroyView() {
        super.onDestroyView();

//        if (mRetrieveTask != null && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
//            outState.putBoolean(SIS_RUNNING_KEY, true);
//        }
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle bundle) {
//        super.onRestoreInstanceState(bundle);
//        // mTweetEdit.updateCharsRemain();
//    }

    @Override
	public void onDestroy() {
        Log.d(TAG, "onDestroy.");
        super.onDestroy();

        taskManager.cancelAll();

        // ˢ�°�ťֹͣ��ת
        if (loadMoreGIF != null){
        	loadMoreGIF.setVisibility(View.GONE);
        }
        if (mTweetList != null){
        	mTweetList.onRefreshComplete();
        }
    }

    @Override
	public void onStart() {
        Log.d(TAG, "onStart.");
        super.onStart();
    }

    @Override
	public void onStop() {
        Log.d(TAG, "onStop.");
        super.onStop();
    }
    
	//������ز���
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy); ////�߾��ȣ����ö�λģʽ���߾��ȣ��͹��ģ����豸
		option.setOpenGps(true);  //��gps
		option.setPriority(LocationClientOption.GpsFirst); // ����GPS����
		option.setLocationNotify(true);//Ĭ��false�������Ƿ�gps��Чʱ����1S1��Ƶ�����GPS���
		option.setCoorType("bd0911");  //������������ "gcj02";//���Ҳ��ֱ�׼;"bd09ll";//�ٶȾ�γ�ȱ�׼,"bd09";//�ٶ�ī���б�׼
//		option.setServiceName("com.baidu.location.service_v2.9");
		option.setIsNeedLocationPoiList(true);//��ѡ��Ĭ��false�������Ƿ���ҪPOI�����������BDLocation.getPoiList��õ�
//		option.setPoiExtraInfo(true);//�Ƿ���ҪPOI�ĵ绰�͵�ַ����ϸ��Ϣ   
//		option.setPoiDistance(1000); //poi��ѯ����       
//		option.setPoiNumber(10);//��෵��POI����   
		option.setIsNeedAddress(true);//�����Ƿ���Ҫ��ַ��Ϣ��Ĭ�ϲ���Ҫ�� ֻ�����綨λ�ſ���
        option.setAddrType("all");
        option.setScanSpan(1000); //Ĭ��0��������λһ�Σ����÷���λ����ļ����Ҫ���ڵ���1000ms������Ч��
        option.setPriority(LocationClientOption.NetWorkFirst);  // LocationClientOption.GpsFirst 
		option.disableCache(true);	
		//option.setIgnoreKillProcess(true);//��ѡ��Ĭ��true����λSDK�ڲ���һ��SERVICE�����ŵ��˶������̣������Ƿ���stop��ʱ��ɱ��������̣�Ĭ�ϲ�ɱ��
		//option.setEnableSimulateGps(false);//��ѡ��Ĭ��false�������Ƿ���Ҫ����gps��������Ĭ����Ҫ
		//option.setIsNeedLocationDescribe(true);//��ѡ��Ĭ��false�������Ƿ���Ҫλ�����廯�����������BDLocation.getLocationDescribe��õ�����������ڡ��ڱ����찲�Ÿ�����
		mLocClient.setLocOption(option);
	}
	
	
	
    protected void onReturnFromCommentList(String info_id) {
    }
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(requestCode == 100){ //��CommentWrite����
	    //�Ƚ�requestCode��REQUESTCODE��֤����Ƿ�ΪREQUESTCODE��صĲ�������
	        if(resultCode == Activity.RESULT_OK){
	        //�Ƚ�resultCode��SecondActivity�е�RESULTCODE��֤��SecondActivity��Ƿ񷵻سɹ���
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
	    else if(requestCode == 200) { //��CommentPinnedSectionActivity����
	    	if(resultCode == Activity.RESULT_OK){
	            Bundle bundle = data.getExtras();
	            String info_id = bundle.getString("infoId");
	            
	    		onReturnFromCommentList(info_id);
	    	}
	    }
	    else if(requestCode == 400) {//��InfoMapActivity����
	    	if(resultCode == Activity.RESULT_OK){
	            Bundle bundle = data.getExtras();
	            boolean change = bundle.getBoolean("change",false);
	            if(change) {
		            mLat = bundle.getDouble("latitude");
		            mLng = bundle.getDouble("longitude");
		            //name_txt = bundle.getString("info");
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
		            //this.mNavBar.setHeaderTitle(name_txt);
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
