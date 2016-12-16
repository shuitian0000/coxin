package com.codeim.weixin.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.data.Friend;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.weixin.FriendViewActivity;
import com.codeim.weixin.adapter.ContactListArrayAdapter;
import com.codeim.weixin.fragment.BaseWeixinListFragment.GetMoreTask;
import com.codeim.weixin.fragment.BaseWeixinListFragment.RetrieveTask;

public class ContactListFragment extends BaseWeixinListFragment{
	static final String TAG = "ContactListFragment";

	protected static final String[] LIST_TYPE = new String[] { "friend", "contact"};
	protected String list_type;
	
	//volatile protected ArrayList<com.codeim.coxin.data.Info> allInfoList;
	//protected NearbyInfoArrayAdapter mInfoListAdapter;
	volatile protected ArrayList<com.codeim.coxin.data.Friend> allContactList;
	protected ContactListArrayAdapter mContactMetaListAdapter;

	@Override
	protected void setAdapter() {
        //allInfoList = new ArrayList<com.codeim.coxin.data.Info>();
		//mInfoListAdapter = new NearbyInfoArrayAdapter(this.getActivity());
		//mTweetList.setAdapter(mInfoListAdapter)
		set_list_type();
		
		allContactList = new ArrayList<com.codeim.coxin.data.Friend>();
		mContactMetaListAdapter = new ContactListArrayAdapter(this.getActivity(), 1);
		mTweetList.setAdapter(mContactMetaListAdapter);
		
		mTweetList.setHeaderDividersEnabled(false);
	}
	
	protected void set_list_type() { //0: friend; 1: contacts
		list_type = LIST_TYPE[0];
	}
	
	@Override
	protected RetrieveTask getNewRetrieveTask() {
		return new myRetrieveTask();
	}
	
	@Override
	protected GetMoreTask getNewGetMoreTask() {
		return new myGetMoreTask();
	}
	
	@Override
    protected void draw() {
		mContactMetaListAdapter.refresh(allContactList); //for friend, disable this
    }
	
	
	private class myRetrieveTask extends RetrieveTask {
        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
			TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();
			
			//List<com.codeim.coxin.fanfou.Info> infosList = null;
			List<com.codeim.coxin.fanfou.Friend> friendsList = null;
			
	    	page_size=1;
	    	page_index=0;
	    	last_id=-1;
            try {
                //use Friend for friend. just use id,otherId,otherName,otherImageUrl
            	//friend relationship, pull from web
                friendsList = TwitterApplication.mApi.getFriendsFromLocal(page_size, page_index, last_id, 
    			        TwitterApplication.getMyselfId(false)); //from the local sqlite
            	//infosList = getApi().getFlyInfomsgMySend(page_size, page_index, last_id, "0", 0, 0);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, friendsList));
            allContactList.clear();
			
			for (com.codeim.coxin.fanfou.Friend friend : friendsList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				Friend u = Friend.create(friend);
				allContactList.add(u);
			}
			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}
			
			if(friendsList==null || friendsList.size()<page_size) {
				data_finish = true;
			}
			if(allContactList==null || allContactList.size()<1) {
				data_finish = true;
				no_data = true;
				
				return TaskResult.OK;
			}
			
//			com.codeim.coxin.data.Friend friend = allContactList.get(allContactList.size()-1);
//			if(friend.id.equals("-1")) {
//				allContactList.remove(allContactList.size()-1);
//				data_finish = true;
//			}

			if(allContactList.size()>0) {
				last_id = Integer.valueOf(allContactList.get(allContactList.size()-1).id);
			} else {
				no_data = true;
			}

            return TaskResult.OK;
        }
    }
	
    // GET MORE TASK
    private class myGetMoreTask extends GetMoreTask {
        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.codeim.coxin.fanfou.Friend> friendsList = null;
//            int mPage = mInfoListAdapter.getCount();
			TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();

	    	page_size=1;
	    	//page_index=0;
	    	//last_id=-1;
            try {
                //use Friend for friend. just use id,otherId,otherName,otherImageUrl
            	//friend relationship, pull from web
                friendsList = TwitterApplication.mApi.getFriendsFromLocal(page_size, page_index, last_id, 
    			        TwitterApplication.getMyselfId(false)); //from the local sqlite
//            	friendsList = getApi().getFlyInfomsgMySend(page_size, page_index, last_id, "0", 0, 0);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, friendsList));
			for (com.codeim.coxin.fanfou.Friend friend : friendsList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				Friend u = Friend.create(friend);
				allContactList.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			
			if(friendsList==null || friendsList.size()<page_size) {
				data_finish = true;
			}
			if(allContactList==null || allContactList.size()<1) {
				data_finish = true;
				no_data = true;
				
				return TaskResult.OK;
			}
			
//			com.codeim.coxin.data.Friend friend = allContactList.get(allContactList.size()-1);
//			if(friend.id.equals("-1")) {
//				allContactList.remove(allContactList.size()-1);
//				data_finish = true;
//			}

			if(allContactList.size()>0) {
				last_id = Integer.valueOf(allContactList.get(allContactList.size()-1).id);
			} else {
				no_data = true;
			}

            return TaskResult.OK;
        }
    }
    
    @Override
	protected void registerOnClickListener(ListView listView) {
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
        		Log.v(TAG, "setOnItemClickListener position: "+String.valueOf(position));
        		final Friend friend = getContextItemTweet(position);

                if (friend == null) {
	    			Log.w(TAG, "Selected item not available.");
	    			specialItemClicked(position);
		    	} else {
			    	Log.d(TAG,String.valueOf(position));

//			    	TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_ID, info.owerId.toString() ).commit();
//			    	TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_USERNAME, info.owerName.toString() ).commit();
//			    	TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_ID, info.id.toString() ).commit();
			    	Intent intent = new Intent(contextActivity, FriendViewActivity.class);
			    	intent.putExtra("slaveId", friend.otherId);
			    	intent.putExtra("serverId", friend.id);
                    startActivityForResult(intent, 200);
			    	//startActivityForResult(intent, Activity.RESULT_FIRST_USER);
			    	//startActivity(intent);
		    	}
			}
	});
	}
	
    protected Friend getContextItemTweet(int position) {
        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mContactMetaListAdapter.getCount()) {
            Friend friend = (Friend) mContactMetaListAdapter.getItem(position);
            if (friend == null) {
                return null;
            } else {
                return friend;
            }
        } else {
            return null;
        }
    }
    
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

}
