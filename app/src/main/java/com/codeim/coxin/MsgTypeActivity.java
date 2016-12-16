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

package com.codeim.coxin;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.BDLocation;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.MsgType;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.fanfou.Info;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskManager;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.base.NearbyArrayBaseActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.MsgTypeArrayAdapter;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.util.DebugTimer;
import com.codeim.coxin.widget.PullToRefreshListView;
// import com.codeim.coxin.data.Tweet;
// import com.codeim.coxin.db.StatusTable;
// import com.codeim.coxin.fanfou.Paging;
// import com.codeim.coxin.fanfou.Status;
// import com.codeim.coxin.task.GenericTask;
// import com.codeim.coxin.task.TaskAdapter;
// import com.codeim.coxin.task.TaskListener;
// import com.codeim.coxin.task.TaskParams;
// import com.codeim.coxin.task.TaskResult;
// import com.codeim.coxin.task.TweetCommonTask;
//import com.codeim.coxin.R;
import com.codeim.coxin.R;

//1. List<com.codeim.coxin.fanfou.MsgType> msgTypesList
//2. getMsgTypes()
//3. com.codeim.coxin.data.MsgType    ----    allMsgTypeList;
//4. com.codeim.coxin.ui.module.MsgTypeArrayAdapter    ----   mMsgTypeListAdapter
//5. xml ---- MsgType ---msgtype.xml
//6. xml ---- MsgTypeItem --- msgtype_item.xml
public class MsgTypeActivity extends BaseNoDoubleClickActivity {
	private static final String TAG = "MsgTypeActivity";

	private static final String LAUNCH_ACTION = "com.codeim.coxin.TWEETS";
	
	private NavBar mNavBar;
	protected Feedback mFeedback;
	protected ListView mListView;
	protected MsgTypeArrayAdapter mMsgTypeListAdapter;
	
	protected static final int STATE_ALL = 0;
	
    // Tasks.
    protected TaskManager taskManager = new TaskManager();
    private GenericTask mDrawTask;
    
    volatile private ArrayList<com.codeim.coxin.data.MsgType> allMsgTypeList;

    public void draw() {
    	Log.v("MsgType draw", String.valueOf(allMsgTypeList.size()));
		mMsgTypeListAdapter.refresh(allMsgTypeList);
    }
//    public void goTop() {
//        Log.d(TAG, "goTop.");
//        mTweetList.setSelection(1);
//    }
    
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

            if (result == TaskResult.AUTH_ERROR) {
                mFeedback.failed("登录信息出错");
                logout();
            } else if (result == TaskResult.OK) {
            	Log.v("MsgType prost process", "normal get list data");
                draw();
                if (task == mDrawTask) {
                    //goTop();
                }
            } else if (result == TaskResult.IO_ERROR) {
                // FIXME: bad smell
                if (task == mDrawTask) {
                    mFeedback.failed(((RetrieveTask) task).getErrorMsg());
                } 
                //else if (task == mGetMoreTask) {
                //    mFeedback.failed(((GetMoreTask) task).getErrorMsg());
                //}
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
        }

        @Override
        public void onProgressUpdate(GenericTask task, Object param) {
            // Log.d(TAG, "onProgressUpdate");
            draw();
        }
    };
    
    private class RetrieveTask extends GenericTask {
        private String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.codeim.coxin.fanfou.MsgType> msgTypesList = null;
			//mRefreshFrequency = 0;
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();

            try 
            {
            	msgTypesList = getMsgTypes();
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, msgTypesList));
			allMsgTypeList.clear();
			for (com.codeim.coxin.fanfou.MsgType msgType : msgTypesList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				MsgType u = MsgType.create(msgType);
				allMsgTypeList.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

            return TaskResult.OK;
        }
    }
	
    public void doDraw() {
        Log.d(TAG, "Attempting retrieve.");

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
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "MsgType OnCreate start");

		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.msgtype);
			
			mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
			mNavBar = new NavBar(NavBar.HEADER_STYLE_TITLE, this);
			mNavBar.setHeaderTitle("消息");
			mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);
			
			allMsgTypeList = new ArrayList<com.codeim.coxin.data.MsgType>();
			
			mListView = (ListView) findViewById(R.id.msgtype_list);
			mMsgTypeListAdapter = new MsgTypeArrayAdapter(this);
			mListView.setAdapter(mMsgTypeListAdapter);
			
			registerOnClickListener(mListView);
			
			doDraw();

			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		/*
		if (mDeleteTask != null && mDeleteTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
		*/
	}
//	public abstract List<com.codeim.coxin.fanfou.Info> getNearbyInfo(int refreshFrequency, String infoType, 
//	        double lat, double lng) throws HttpException;
	public List<com.codeim.coxin.fanfou.MsgType> getMsgTypes() throws HttpException {
		//List<com.codeim.coxin.fanfou.MsgType> myMsgTypeList=new ArrayList<com.codeim.coxin.fanfou.MsgType>();
		//com.codeim.coxin.fanfou.MsgType myMsgType;
		try{
			List<com.codeim.coxin.fanfou.MsgType> myMsgTypeList=new ArrayList<com.codeim.coxin.fanfou.MsgType>();
			
			for (int j=0; j<2; j++) {
				JSONObject myJson=new JSONObject();
			    myJson.put("id", j+1);
			    if(j==0) {
			        myJson.put("title", "系统消息");
			        myJson.put("status", "没有新的系统消息");
			        int i = R.drawable.child_image;
			        myJson.put("image", i);
			    } else {
			        myJson.put("title", "订单消息");
			        myJson.put("status", "没有新的订单消息");
			        int i = R.drawable.child_image;
			        myJson.put("image", i);
			    }
			    //myMsgType = new com.codeim.coxin.fanfou.MsgType(myJson);
			    myMsgTypeList.add(new com.codeim.coxin.fanfou.MsgType(myJson));
		    }
			return myMsgTypeList;
		} catch (JSONException jsone) {
			throw new HttpException(jsone);
		} catch (HttpException te) {
			throw te;
		}

	} 
	
	protected void registerOnClickListener(ListView listView) {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MsgType msgType = getContextItemTweet(position);

				if (msgType == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else {
					//Tweet tweet;
					Log.d(TAG,String.valueOf(position));
					// Log.d(TAG, "User: " + user.statusInReplyToStatusId + " " + user.statusInReplyToUserId + " " + user.statusInReplyToScreenName);
					// Log.d(TAG, "user attachmentUrl " + user.attachmentUrl);
//				    tweet = User.userSwitchToTweet(user);
					// Log.d(TAG, "tweet: " + tweet.toString());
//				    launchActivity(StatusWithCommentActivity.createIntent(tweet));
					// launchActivity(StatusActivity.createIntent(user));
				}
			}
		});
	}
	
	protected MsgType getContextItemTweet(int position) {
//        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mMsgTypeListAdapter.getCount()) {
            MsgType msgType = (MsgType) mMsgTypeListAdapter.getItem(position);
            if (msgType == null) {
                return null;
            } else {
                return msgType;
            }
        } else {
            return null;
        }
	}
	
	protected void specialItemClicked(int position) {
	}

	public void onDeleteFailure() {
		Log.e(TAG, "Delete failed");
	}

	public void onDeleteSuccess() {
		mMsgTypeListAdapter.refresh();
	}
	
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy.");
        super.onDestroy();

        taskManager.cancelAll();
    }
    
    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart.");
        super.onRestart();
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
    }

}