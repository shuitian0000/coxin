package com.codeim.weixin.adapter;

import java.util.ArrayList;
//import java.util.HashMap;

//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
//import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
// import android.widget.ListView;
import android.widget.TextView;
//import android.widget.Toast;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.data.Chat;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpClient;
import com.codeim.coxin.ui.module.TweetAdapter;
//import com.codeim.coxin.R;
import com.codeim.coxin.R;

/*
 * 用于用户的Adapter
 */
public class ChatListArrayAdapter extends BaseAdapter implements TweetAdapter, OnCompletionListener, OnErrorListener, OnClickListener {
	private static final String TAG = "ChatListArrayAdapter";
	public static final String DOWNLOAD_DEFAULT_DIR = "/coxin/download";
	protected static final int REQUEST_CODE_LAUNCH_ACTIVITY = 0;
	
	private int mType=0; //0: chat list; 1: contacts list
	
	public static final int IDLE_STATE = 0;
	public static final int PLAYING_STATE = 1;
	public static final int DOWNLOADING_STATE = 2;
	
	private int mState = IDLE_STATE;

	protected ArrayList<Chat> mChats;
	private Context mContext;
	protected LayoutInflater mInflater;
	
	private int curPosition;
	private HttpClient mClient;
	//private final Handler mHandler = new Handler();
	
//	private Runnable mUpdateSeekBar = new Runnable() {
//        @Override
//        public void run() {
//		    if (mPlayingPosition != null) {
//                //updateProgressBar(mPlayingPosition);
//		    }
//        }
//    };
	
	private ImageLoaderCallback callback = new ImageLoaderCallback() {
		@Override
		public void refresh(String url, Bitmap bitmap) {
			ChatListArrayAdapter.this.refresh();
		}
	};

	public ChatListArrayAdapter(Context context, int type) {
		mChats = new ArrayList<Chat>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mType = type;

//		File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DOWNLOAD_DEFAULT_DIR);
//        if (!downloadDir.exists()) {
//            downloadDir.mkdirs();
//        }
//        mDownloadDir = downloadDir;
//		mClient = getApi().getHttpClient();
		
	    initInfoPopView();
	}
	
	public void initInfoPopView() {
		
//		DisplayMetrics outMetrics = new DisplayMetrics(); 
////		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
//		int mScreenHeight = outMetrics.heightPixels;
//		
//		infoMenuOptionList.add("回复");
//		
//		if(Integer.valueOf(TwitterApplication.getMyselfId(true))
//				== Integer.valueOf(info.owerId)) {
//		    infoMenuOptionList.add("删除");
//		} else {
//			infoMenuOptionList.add("举报");
//		}
//		
////		LayoutInflater inflater = getLayoutInflater () ;
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View mView = inflater.inflate(R.layout.info_pop_menu_view, null);
//        final InfoMenuPopupwindow info_menu_pop_view = new InfoMenuPopupwindow(LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
//				                   infoMenuOptionList, mView);
//        info_menu_pop_view.setOutsideTouchable(true);
//        info_menu_pop_view.setFocusable(true);
	}

	@Override
	public int getCount() {
		return mChats.size();
	}

	@Override
	public Object getItem(int position) {
		return mChats.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		public ImageView other_image;  //头像
		
		public LinearLayout chat_info;
		public TextView other_name;  //第一行：用户名
		public TextView chat_time;  //第一行：最近时间
		public TextView chat_content;  //第二行：内容
		public TextView chat_meta;  //右边：可能的附加信息
		
		public LinearLayout contact_info;
		public TextView contact_name;  //第一行：用户名
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;

		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);
//		boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		
		view = convertView;
		final ViewHolder holder;

		if (view == null) {
			view = mInflater.inflate(R.layout.chat_item, parent, false);
			holder = new ViewHolder();
			holder.other_image = (ImageView) view.findViewById(R.id.other_image);
			
			holder.chat_info = (LinearLayout) view.findViewById(R.id.chat_info);
			holder.other_name = (TextView) view.findViewById(R.id.other_name);
			holder.chat_time = (TextView) view.findViewById(R.id.chat_time);
			holder.chat_content = (TextView) view.findViewById(R.id.chat_content);
			holder.chat_meta = (TextView) view.findViewById(R.id.chat_meta);
			
			holder.contact_info = (LinearLayout) view.findViewById(R.id.contact_info);
			holder.contact_name = (TextView) view.findViewById(R.id.contact_name);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		final Chat chat = mChats.get(position);
		holder.other_image.setImageBitmap(TwitterApplication.mImageLoader.get(chat.otherImageUrl.toString(), callback));
		if(mType==0) {
			holder.chat_info.setVisibility(View.VISIBLE);
			holder.other_name.setText(chat.otherName);
			holder.chat_time.setText(chat.lastChatMsgTime.toString());
			holder.chat_content.setText(chat.lastChatMsgContent);
//			holder.chat_meta.setText(chat.otherId);
			
			holder.contact_info.setVisibility(View.GONE);
		} else {
//			holder.chat_info.setVisibility(View.GONE);
//			holder.contact_name.setText(chat.otherName);
		}
		
		return view;
	}
	
	protected void launchActivity(Intent intent) {
		mContext.startActivity(intent);
	}
	
	public void refresh(ArrayList<Chat> chats) {
		mChats = (ArrayList<Chat>) chats.clone();
		
//		for (int i=0; i<mChats.size(); i++) {
//			//Log.d(TAG, "i = " + i);
//			//mButtonStatuses.get(i).put(key, value);
//			mButtonSelections.add(1);
//			mProgressbarPercent.add(0);
//		}
		
		notifyDataSetChanged();
	}

	@Override
	public void refresh() {
		notifyDataSetChanged();
	}
	
	public Weibo getApi() {
		return TwitterApplication.mApi;
	}

	private OnTimeSet mOnTimeSet;
	public void setOnTimeSet(OnTimeSet l) {
		mOnTimeSet = l;
	}
	public interface OnTimeSet {
		public void clickTimeSet(int position);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	private OnClickChatItem mOnClickChatItem;
	public void setOnClickItem(OnClickChatItem l) {
		mOnClickChatItem = l;
	}
	public interface OnClickChatItem {
		public void doChatItemClick(int position, int mType);
	}
	
	@Override
	public void onClick(View v) {
//		// TODO Auto-generated method stub
//		int position = (Integer) v.getTag();
//		mOnClickChatItem.doChatItemClick(position, mType);
	}
	
}
