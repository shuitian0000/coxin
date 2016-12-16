package com.codeim.coxin;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
// import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
// import android.database.Cursor;
import android.graphics.Bitmap;
// import android.net.Uri;
import android.os.Bundle;
// import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
// import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.data.User;
import com.codeim.coxin.fanfou.Paging;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.HttpRefusedException;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskManager;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.widget.MyListView;
import com.codeim.coxin.widget.MyListView.OnRefreshListener;
// import com.codeim.coxin.db.TwitterDatabase;
// import com.codeim.coxin.db.UserInfoTable;
// import com.codeim.coxin.fanfou.User;
//import com.codeim.coxin.ui.module.UserNoAvatarTweetArrayAdapter;
//import com.codeim.coxin.R;
import com.codeim.coxin.R;
//import com.markupartist.android.widget.MyListView;
//import com.markupartist.android.widget.MyListView.OnRefreshListener;

/**
 * 
 * @author Dino 2011-02-26
 */
// public class ProfileActivity extends WithHeaderActivity {
public class ProfileActivity extends BaseNoDoubleClickActivity {
	private static final String TAG = "ProfileActivity";
	private static final String LAUNCH_ACTION = "com.codeim.coxin.PROFILE";
	// private static final String STATUS_COUNT = "status_count";
	private static final String EXTRA_USER = "user";
	private static final String USER_ID = "userid";
	// private static final String USER_NAME = "userName";
	private static final int ATTR_TYPE_ALL = 0;
	private static final int ATTR_TYPE_TOPIC = 1;
	private static final int ATTR_TYPE_REPLY = 2;
	
	private int mAttrType;  // 0:all; 1:topic; 2:reply
	
	// 记录服务器拒绝访问的信息
	// private String msg;
	
	protected View mListHeader;
    protected View mListFooter;
	protected TextView loadMoreBtn;
    protected ProgressBar loadMoreGIF;
    protected TextView loadMoreBtnTop;
    protected ProgressBar loadMoreGIFTop;

    protected TaskManager taskManager = new TaskManager();
    private GenericTask mRetrieveTask;
	private GenericTask mLoadMoreTask;
	private GenericTask setFollowingTask;
	private GenericTask cancelFollowingTask;

	private String userId;
	// private String userName;
	private String myself;
	private User profileInfo;  // 用户信息

	private ImageView profileImageView;  // 头像
	// private TextView profileName;  // 名称
	private TextView profileScreenName;  // 昵称
	// private TextView userLocation;  // 地址
	// private TextView userUrl;  // url
	// private TextView userInfo;  // 自述
	private TextView friendsCount;  // 好友
	private TextView followersCount;  // 收听
	// private TextView statusCount;  // 消息
	private Button topicBtn;
	private Button replyBtn;
	// private TextView favouritesCount;  // 收藏
	// private TextView isFollowingText;  // 是否关注
	private Button followingBtn;  // 收听/取消关注按钮
	// private Button sendMentionBtn;  // 发送留言按钮
	private Button sendDmBtn;  // 发送私信按钮
	private LinearLayout bottomBar;
	private View dividerLine;
	private ProgressDialog dialog;  // 请稍候
	
	private MyListView mUserTweetList;
//	private UserNoAvatarTweetArrayAdapter mUserTweetAdapter;
	private ArrayList<Tweet> allUserTweetList;

	// private RelativeLayout friendsLayout;
	// private LinearLayout followersLayout;
	// private LinearLayout statusesLayout;
	// private LinearLayout favouritesLayout;

	private NavBar mNavBar;
	private Feedback mFeedback;

	// private TwitterDatabase db;
	
	private String mMaxId = "";
	
	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveBegin();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			loadMoreGIF.setVisibility(View.GONE);
			mUserTweetList.onRefreshComplete();
			
			if (result == TaskResult.AUTH_ERROR) {
				mFeedback.failed("登录失败, 请重新登录.");
				return;
			} else if (result == TaskResult.OK) {
				drawList();
		    } else if (result == TaskResult.IO_ERROR) {
				mFeedback.failed("更新失败.");
			}
			mFeedback.success("");
		}

		@Override
		public String getName() {
			return "UserTimelineRetrieve";
		}
	};
	
	private TaskListener mLoadMoreTaskListener = new TaskAdapter() {

		@Override
		public void onPreExecute(GenericTask task) {
	        loadMoreGIF.setVisibility(View.VISIBLE);
			onLoadMoreBegin();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
	        loadMoreGIF.setVisibility(View.GONE);
			mUserTweetList.onMoreRefreshComplete();
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				mFeedback.success("");
				drawList();
			}
		}

		@Override
		public String getName() {
			return "UserTimelineLoadMoreTask";
		}
	};

	public static Intent createIntent(String userId) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.putExtra(USER_ID, userId);
		return intent;
	}
	
	public static Intent createIntent(User user) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.putExtra(EXTRA_USER, user);
		return intent;
	}

	private ImageLoaderCallback callback = new ImageLoaderCallback() {
		@Override
		public void refresh(String url, Bitmap bitmap) {
			profileImageView.setImageBitmap(bitmap);
		}
	};

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "OnCreate start");

		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.profile);
			// mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);

			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			
			myself = TwitterApplication.getMyselfId(false);
			
			if (extras != null) {
			    mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
			    this.profileInfo = extras.getParcelable(EXTRA_USER);
			    userId = profileInfo.id;
			    // userName = profileInfo.screenName;
			    if (!userId.equals(myself)) {
			    	mNavBar.setHeaderTitle(profileInfo.screenName);
			    } else {
			    	mNavBar.setHeaderTitle("我");
			    }
			}
		
			/*
			if (extras != null) {
				this.userId = extras.getString(USER_ID);
				this.userName = extras.getString(USER_NAME);
			} else {
				this.userId = myself;
				this.userName = TwitterApplication.getMyselfName(false);
			}
			Uri data = intent.getData();
			if (data != null) {
				userId = data.getLastPathSegment();
			}
			*/
			
			mAttrType = ATTR_TYPE_TOPIC;

			// 初始化控件
			initControls();
			// db = this.getDb();
			draw();

			return true;
		} else {
			return false;
		}
	}
	
	@Override
    protected void onPause() {
	    super.onPause();
//	    mUserTweetAdapter.stopPlay();  // 停止语音播放
	}

	private void initControls() {
		// mNavBar = new NavBar(NavBar.HEADER_STYLE_HOME, this);
		// mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
		// mNavBar.setHeaderTitle("");

		mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
		// sendMentionBtn = (Button) findViewById(R.id.sendmetion_btn);
		sendDmBtn = (Button) findViewById(R.id.senddm_btn);
        bottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
		dividerLine = (View) findViewById(R.id.divider_line);
		profileImageView = (ImageView) findViewById(R.id.profile_image);
		// profileName = (TextView) findViewById(R.id.profilename);
		profileScreenName = (TextView) findViewById(R.id.screen_name);
		// userLocation = (TextView) findViewById(R.id.user_location);
		// userUrl = (TextView) findViewById(R.id.user_url);
		// userInfo = (TextView) findViewById(R.id.tweet_user_info);
		friendsCount = (TextView) findViewById(R.id.following_count);   //关注
		followersCount = (TextView) findViewById(R.id.follower_count);  //粉丝
		
		topicBtn = (Button) findViewById(R.id.topic_btn);
		replyBtn = (Button) findViewById(R.id.reply_btn);
		
		topicBtn.setEnabled(false);
		replyBtn.setEnabled(true);
		
		topicBtn.setText("提问 " + profileInfo.topicCount);
		replyBtn.setText("回答 " + profileInfo.replyCount);
		
		topicBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
			public void onClick(View v) {
				if (mAttrType != ATTR_TYPE_TOPIC) {
					mAttrType = ATTR_TYPE_TOPIC;
					topicBtn.setEnabled(false);
					replyBtn.setEnabled(true);
					doRetrieve();
				}
			}
		});
		
		replyBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
			public void onClick(View v) {
				if (mAttrType != ATTR_TYPE_REPLY) {
					mAttrType = ATTR_TYPE_REPLY;
					topicBtn.setEnabled(true);
					replyBtn.setEnabled(false);
					doRetrieve();
				}
			}
		});
		
		/*
		TextView friendsCountTitle = (TextView) findViewById(R.id.friends_count_title);
		TextView followersCountTitle = (TextView) findViewById(R.id.followers_count_title);
		String who;
		if (userId.equals(myself)) {
			who = "我";
		} else {
			who = "ta";
		}
		friendsCountTitle.setText(MessageFormat.format(getString(R.string.profile_friends_count_title), who));
		followersCountTitle.setText(MessageFormat.format(getString(R.string.profile_followers_count_title), who));
		*/

		// statusCount = (TextView) findViewById(R.id.status_count);
		// favouritesCount = (TextView) findViewById(R.id.favourites_count);

		/*
		friendsLayout = (RelativeLayout) findViewById(R.id.friendsLayout);
		followersLayout = (LinearLayout) findViewById(R.id.followersLayout);
		statusesLayout = (LinearLayout) findViewById(R.id.statusesLayout);
		favouritesLayout = (LinearLayout) findViewById(R.id.favouritesLayout);
		*/

		// isFollowingText = (TextView) findViewById(R.id.isfollowing_text);
		followingBtn = (Button) findViewById(R.id.following_btn);
		
		mUserTweetList = (MyListView) findViewById(R.id.user_noavatar_status_list);
		setupListHeader(true);
//		mUserTweetAdapter = new UserNoAvatarTweetArrayAdapter(this);
//		mUserTweetList.setAdapter(mUserTweetAdapter);
		allUserTweetList = new ArrayList<Tweet>();
//		registerOnClickListener(mUserTweetList);
	}
	
	/**
     * 绑定listView底部 - 载入更多 NOTE: 必须在listView#setAdapter之前调用
     */
    protected void setupListHeader(boolean addFooter) {
        // Add Header to ListView
        // mListHeader = View.inflate(this, R.layout.listview_header, null);
        // mTweetList.addHeaderView(mListHeader, null, true);
    	mUserTweetList.setOnRefreshListener(new OnRefreshListener(){
    		@Override
    		public void onRefresh(){
    			doRetrieve();
    		}
    	});

        // Add Footer to ListView
        mListFooter = View.inflate(this, R.layout.listview_footer, null);
        mUserTweetList.addFooterView(mListFooter, null, true);
        
        // Find View
        loadMoreBtn = (TextView) findViewById(R.id.ask_for_more);
        loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
        loadMoreBtnTop = (TextView) findViewById(R.id.ask_for_more_header);
        loadMoreGIFTop = (ProgressBar) findViewById(R.id.rectangleProgressBar_header);
    }

	private void draw() {
		Log.d(TAG, "draw");
		// bindControl();
		bindProfileInfo();
		// doGetProfileInfo();
	}
	
	private void bindProfileInfo() {
		// dialog = ProgressDialog.show(ProfileActivity.this, "请稍候", "正在加载信息...");  // 2013.8.29
		bindControl();
		if (dialog != null) {
			dialog.dismiss();
		}
		doRetrieve();
	}
	
	public void drawList() {
//		mUserTweetAdapter.refresh(allUserTweetList);
    }
	
//	protected void registerOnClickListener(ListView listView) {
//		listView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////				Tweet tweet = getContextItemTweet(position);
//
//				if (tweet == null) {
//					Log.w(TAG, "Selected item not available.");
//					specialItemClicked(position);
//				} else {
////				    launchActivity(StatusWithCommentActivity.createIntent(tweet));
//					// launchActivity(StatusActivity.createIntent(user));
//				}
//			}
//		});
//	}
	
//	protected Tweet getContextItemTweet(int position) {
//        position = position - 1;
//        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
//        if (position >= 0 && position < mUserTweetAdapter.getCount()) {
//            Tweet tweet = (Tweet) mUserTweetAdapter.getItem(position);
//            if (tweet == null) {
//                return null;
//            } else {
//                return tweet;
//            }
//        } else {
//            return null;
//        }
//    }
	
	protected void specialItemClicked(int position) {
        // 注意 mTweetAdapter.getCount 和 mTweetList.getCount的区别
        // 前者仅包含数据的数量（不包括foot和head），后者包含foot和head
        // 因此在同时存在foot和head的情况下，list.count = adapter.count + 2
        if (position == mUserTweetList.getCount() - 1) {
            // 最后一个Item(footer)
            loadMoreGIF.setVisibility(View.VISIBLE);
            doGetMore();
        }
    }

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume.");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart.");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop.");
	}

	private void bindControl() {
		if (profileInfo.id.equals(myself)) {
		    bottomBar.setVisibility(View.GONE);
			dividerLine.setVisibility(View.GONE);
			sendDmBtn.setVisibility(View.GONE);
		} else {
			// 发送私信
			bottomBar.setVisibility(View.VISIBLE);
			dividerLine.setVisibility(View.VISIBLE);
			sendDmBtn.setVisibility(View.VISIBLE);
			sendDmBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Intent intent = WriteDmActivity.createIntent(profileInfo.id);
//					Intent intent = WriteDmActivity.createIntent(profileInfo.screenName);
//					startActivity(intent);
				}
			});
		}

		if (userId.equals(myself)) {
			// mNavBar.setHeaderTitle("我" + getString(R.string.cmenu_user_profile_prefix));
		} else {
			// mNavBar.setHeaderTitle(profileInfo.getScreenName() + getString(R.string.cmenu_user_profile_prefix));
		}
		profileImageView.setImageBitmap(TwitterApplication.mImageLoader.get(profileInfo.profileImageUrl.toString(), callback));

		profileScreenName.setText(profileInfo.screenName);

		if (profileInfo.id.equals(myself)) {
			// isFollowingText.setText(R.string.profile_isyou);
			followingBtn.setVisibility(View.GONE);
		} else if (profileInfo.isFollowing) {
			// isFollowingText.setText(R.string.profile_isfollowing);
			followingBtn.setVisibility(View.VISIBLE);
			followingBtn.setText(R.string.user_label_unfollow);
			followingBtn.setOnClickListener(cancelFollowingListener);
			// followingBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_unfollow), null, null, null);
		} else {
			// isFollowingText.setText(R.string.profile_notfollowing);
			followingBtn.setVisibility(View.VISIBLE);
			followingBtn.setText(R.string.user_label_follow);
			followingBtn.setOnClickListener(setfollowingListener);
			// followingBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_follow), null, null, null);
		}

		friendsCount.setText(String.valueOf(profileInfo.friendsCount));
		followersCount.setText(String.valueOf(profileInfo.followersCount));
		// statusCount.setText(String.valueOf(profileInfo.statusesCount));
		// favouritesCount.setText(String.valueOf(profileInfo.favoritesCount));
	}
	
	private void onRetrieveBegin() {
		mFeedback.start("");
		// mUserTweetList.prepareForRefresh();
		// 更新查询状态显示
	}
	
	private void onLoadMoreBegin() {
		mFeedback.start("");
	}
	
	public void doRetrieve() {
        Log.d(TAG, "Attempting retrieve.");

        if (mRetrieveTask != null && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mRetrieveTask = new UserTimelineRetrieveTask();
            mRetrieveTask.setFeedback(mFeedback);
            mRetrieveTask.setListener(mRetrieveTaskListener);
            mRetrieveTask.execute();

            // Add Task to manager
            taskManager.addTask(mRetrieveTask);
        }
    }
	
	private class UserTimelineRetrieveTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
//			List<com.codeim.coxin.fanfou.Status> statusList;
//			try {
////				statusList = getApi().getUserTimeline(userId, mAttrType);
//				mFeedback.update(60);
//			} catch (HttpException e) {
//				Log.e(TAG, e.getMessage(), e);
//				Throwable cause = e.getCause();
//				if (cause instanceof HttpRefusedException) {
//					// AUTH ERROR
//					// msg = ((HttpRefusedException) cause).getError().getMessage();
//					return TaskResult.AUTH_ERROR;
//				} else {
//					return TaskResult.IO_ERROR;
//				}
//			}
//			mFeedback.update(100 - (int) Math.floor(statusList.size() * 2)); // 60~100
//			allUserTweetList.clear();
//			for (com.codeim.coxin.fanfou.Status status : statusList) {
//				if (isCancelled()) {
//					return TaskResult.CANCELLED;
//				}
//				Tweet tweet;
//				tweet = Tweet.create(status);
//				mMaxId = tweet.id;
//				allUserTweetList.add(tweet);
//				if (isCancelled()) {
//					return TaskResult.CANCELLED;
//				}
//			}
//			if (isCancelled()) {
//				return TaskResult.CANCELLED;
//			}
			return TaskResult.OK;
		}
	}
	
	public void doGetMore() {
		doLoadMore();
	}
	
	private void doLoadMore() {
		Log.d(TAG, "Attempting load more.");

		if (mLoadMoreTask != null && mLoadMoreTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mLoadMoreTask = new UserTimelineLoadMoreTask();
			mLoadMoreTask.setListener(mLoadMoreTaskListener);
			mLoadMoreTask.execute();
		}
	}
	
	private class UserTimelineLoadMoreTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
//			List<com.codeim.coxin.fanfou.Status> statusList;
//			try {
//				// Paging paging = new Paging();
//				Paging paging = new Paging(1, 20);
//				Log.d(TAG, "mMaxId = " + mMaxId);
//				if (mMaxId.equals("1")) {
//				    return TaskResult.OK;
//				}
//				
//				if (!TextUtils.isEmpty(mMaxId)) {
//					// paging.setMaxId(mMaxId-1);
//					String maxId = String.valueOf(Integer.parseInt(mMaxId)-1);
//					paging.setMaxId(maxId);
//				}
////				statusList = getApi().getUserTimeline(userId, paging, mAttrType);
//			} catch (HttpException e) {
//				Log.e(TAG, e.getMessage(), e);
//				Throwable cause = e.getCause();
//				if (cause instanceof HttpRefusedException) {
//					// AUTH ERROR
//					// msg = ((HttpRefusedException) cause).getError().getMessage();
//					return TaskResult.AUTH_ERROR;
//				} else {
//					return TaskResult.IO_ERROR;
//				}
//			}
//
//			for (com.codeim.coxin.fanfou.Status status : statusList) {
//				if (isCancelled()) {
//					return TaskResult.CANCELLED;
//				}
//				Tweet tweet;
//				tweet = Tweet.create(status);
//				mMaxId = tweet.id;
//				allUserTweetList.add(tweet);
//			}
//			if (isCancelled()) {
//				return TaskResult.CANCELLED;
//			}
			return TaskResult.OK;
		}
	}

	/**
	 * 设置关注监听
	 */
	private OnClickListener setfollowingListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Builder diaBuilder = new AlertDialog.Builder(ProfileActivity.this).setTitle("关注提示").setMessage("确实要添加关注吗?");
			diaBuilder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (setFollowingTask != null && setFollowingTask.getStatus() == GenericTask.Status.RUNNING) {
								return;
							} else {
								setFollowingTask = new SetFollowingTask();
								setFollowingTask.setListener(setFollowingTaskLinstener);
								TaskParams params = new TaskParams();
								setFollowingTask.execute(params);
							}
						}
					});
			diaBuilder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			Dialog dialog = diaBuilder.create();
			dialog.show();
		}
	};

	/**
	 * 取消关注监听
	 */
	private OnClickListener cancelFollowingListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Builder diaBuilder = new AlertDialog.Builder(ProfileActivity.this).setTitle("关注提示").setMessage("确实要取消关注吗?");
			diaBuilder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (cancelFollowingTask != null && cancelFollowingTask.getStatus() == GenericTask.Status.RUNNING) {
								return;
							} else {
								cancelFollowingTask = new CancelFollowingTask();
								cancelFollowingTask.setListener(cancelFollowingTaskLinstener);
								TaskParams params = new TaskParams();
								cancelFollowingTask.execute(params);
							}
						}
					});
			diaBuilder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			Dialog dialog = diaBuilder.create();
			dialog.show();
		}
	};

	/**
	 * 设置关注
	 * 
	 * @author Dino
	 * 
	 */
	private class SetFollowingTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			try {
				getApi().createFriendship(userId);
			} catch (HttpException e) {
				Log.w(TAG, "create friend ship error");
				return TaskResult.FAILED;
			}
			return TaskResult.OK;
		}
	}

	private TaskListener setFollowingTaskLinstener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				followingBtn.setText("取消关注");
				// isFollowingText.setText(getResources().getString(R.string.profile_isfollowing));
				followingBtn.setOnClickListener(cancelFollowingListener);
				Toast.makeText(getBaseContext(), "关注成功", Toast.LENGTH_SHORT).show();
			} else if (result == TaskResult.FAILED) {
				Toast.makeText(getBaseContext(), "关注失败", Toast.LENGTH_SHORT).show();
			}
		}
		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	/**
	 * 取消关注
	 * 
	 * @author Dino
	 * 
	 */
	private class CancelFollowingTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			try {
				getApi().destroyFriendship(userId);
			} catch (HttpException e) {
				Log.w(TAG, "create friend ship error");
				return TaskResult.FAILED;
			}
			return TaskResult.OK;
		}
	}

	private TaskListener cancelFollowingTaskLinstener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				followingBtn.setText("添加关注");
				// isFollowingText.setText(getResources().getString(R.string.profile_notfollowing));
				followingBtn.setOnClickListener(setfollowingListener);
				Toast.makeText(getBaseContext(), "取消关注成功", Toast.LENGTH_SHORT).show();
			} else if (result == TaskResult.FAILED) {
				Toast.makeText(getBaseContext(), "取消关注失败", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
	};
}
