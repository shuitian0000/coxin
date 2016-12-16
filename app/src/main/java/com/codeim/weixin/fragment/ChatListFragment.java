package com.codeim.weixin.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.data.Chat;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.weixin.ChattingActivity;
import com.codeim.weixin.adapter.ChatListArrayAdapter;

public class ChatListFragment extends BaseWeixinListFragment{
	static final String TAG = "ChatListFragment";
	
	protected static final String[] LIST_TYPE = new String[] { "chat", "contact"};
	protected String list_type;
	
	//volatile protected ArrayList<com.codeim.coxin.data.Info> allInfoList;
	//protected NearbyInfoArrayAdapter mInfoListAdapter;
	volatile protected ArrayList<com.codeim.coxin.data.Chat> allChatMetaList;
	protected ChatListArrayAdapter mChatMetaListAdapter;
	
	private String last_time;

	@Override
	protected void setAdapter() {
        //allInfoList = new ArrayList<com.codeim.coxin.data.Info>();
		//mInfoListAdapter = new NearbyInfoArrayAdapter(this.getActivity());
		//mTweetList.setAdapter(mInfoListAdapter)
		set_list_type();
		
		allChatMetaList = new ArrayList<com.codeim.coxin.data.Chat>();
		mChatMetaListAdapter = new ChatListArrayAdapter(this.getActivity(), 0);
		mTweetList.setAdapter(mChatMetaListAdapter);
		
		mTweetList.setHeaderDividersEnabled(false);
	}
	
	protected void set_list_type() { //0: chat; 1: contacts
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
		mChatMetaListAdapter.refresh(allChatMetaList); //for chat, disable this
    }
	
	
	private class myRetrieveTask extends RetrieveTask {
        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
			TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();
			
			//List<com.codeim.coxin.fanfou.Info> infosList = null;
			List<com.codeim.coxin.fanfou.Chat> chatMetasList = null;
			
	    	page_size=1;
	    	page_index=0;
	    	//last_id=-1;
	    	last_time = "0";
            try {
        		//generally push method.
        		//just only when want to refresh.pull once from web -->update local database -->trigger dataset up and listview refresh
        		//when getMore, not pull from web, just directly pull from local database
            	chatMetasList = TwitterApplication.mApi.getChatsFromLocal(page_size, page_index, last_time, 
            			        TwitterApplication.getMyselfId(false)); //from the local sqlite
            	//infosList = getApi().getFlyInfomsgMySend(page_size, page_index, last_id, "0", 0, 0);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, chatMetasList));
            
            allChatMetaList.clear();
			//for (com.codeim.coxin.fanfou.Info info : infosList) {
			for (com.codeim.coxin.fanfou.Chat chat : chatMetasList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				Chat u = Chat.create(chat);
				allChatMetaList.add(u);
			}
			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}
			
			com.codeim.coxin.data.Chat chat = allChatMetaList.get(allChatMetaList.size()-1);
			if(chat.id.equals("-1")) {
				allChatMetaList.remove(allChatMetaList.size()-1);
				data_finish = true;
			}
			if(allChatMetaList.size()>0) {
				//last_id = Integer.valueOf(allChatMetaList.get(allChatMetaList.size()-1).id);
				last_time = DateTimeHelper.dateToString(allChatMetaList.get(allChatMetaList.size()-1).lastChatMsgTime, "");
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
            List<com.codeim.coxin.fanfou.Chat> chatMetasList = null;
//            int mPage = mInfoListAdapter.getCount();
			TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();

            try {
        		//generally push method.
        		//just only when want to refresh.pull once from web -->update local database -->trigger dataset up and listview refresh
        		//when getMore, not pull from web, just directly pull from local database
            	chatMetasList = TwitterApplication.mApi.getChatsFromLocal(page_size, page_index, last_time, 
            			        TwitterApplication.getMyselfId(false)); //from the local sqlite
//            	chatMetasList = getApi().getFlyInfomsgMySend(page_size, page_index, last_id, "0", 0, 0);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, chatMetasList));
			for (com.codeim.coxin.fanfou.Chat chat : chatMetasList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				Chat u = Chat.create(chat);
				allChatMetaList.add(u);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			
			com.codeim.coxin.data.Chat chat = allChatMetaList.get(allChatMetaList.size()-1);
			if(chat.id.equals("-1")) {
				allChatMetaList.remove(allChatMetaList.size()-1);
				data_finish = true;
			}
			if(allChatMetaList.size()>0) {
				//last_id = Integer.valueOf(allChatMetaList.get(allChatMetaList.size()-1).id);
				last_time = DateTimeHelper.dateToString(allChatMetaList.get(allChatMetaList.size()-1).lastChatMsgTime, "");
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
        		Log.e(TAG, "setOnItemClickListener position: "+String.valueOf(position));
        		final Chat chat = getContextItemTweet(position);
//	　　　　		Info info = (Info) parent.getAdapter().getItem(position);

                if (chat == null) {
	    			Log.w(TAG, "Selected item not available.");
	    			specialItemClicked(position);
		    	} else {
			    	Log.d(TAG,String.valueOf(position));

//			    	TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_ID, info.owerId.toString() ).commit();
//			    	TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_USERNAME, info.owerName.toString() ).commit();
//			    	TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_ID, info.id.toString() ).commit();
			    	Intent intent = new Intent(contextActivity, ChattingActivity.class);
			    	intent.putExtra("chatGrpId", chat.id);
			    	intent.putExtra("slaveId", chat.otherId);
			    	intent.putExtra("slaveName", chat.otherName);
			    	intent.putExtra("otherImageUrl", chat.otherImageUrl);
                    //startActivityForResult(intent, 200);
			    	startActivity(intent);
		    	}
			}
	});
	}
	
    protected Chat getContextItemTweet(int position) {
        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mChatMetaListAdapter.getCount()) {
            Chat chat = (Chat) mChatMetaListAdapter.getItem(position);
            if (chat == null) {
                return null;
            } else {
                return chat;
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
