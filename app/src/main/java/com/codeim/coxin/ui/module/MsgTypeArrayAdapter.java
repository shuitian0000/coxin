package com.codeim.coxin.ui.module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.io.InputStream;




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
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
// import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
//import android.widget.Toast;




import com.codeim.coxin.ProfileActivity;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.data.MsgType;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpClient;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DistanceHelper;
//import com.codeim.coxin.R;
import com.codeim.coxin.R;
import com.codeim.coxin.R.drawable;
//import com.codeim.coxin.data.User;

//import com.codeim.coxin.NearbyActivity;

/*
 * 用于用户的Adapter
 */
//public class MsgTypeArrayAdapter extends BaseAdapter implements TweetAdapter, OnCompletionListener, OnErrorListener {
public class MsgTypeArrayAdapter extends BaseAdapter implements TweetAdapter {
	private static final String TAG = "MsgTypeArrayAdapter";
	public static final String DOWNLOAD_DEFAULT_DIR = "/coxin/download";
	protected static final int REQUEST_CODE_LAUNCH_ACTIVITY = 0;
	
	public static final int IDLE_STATE = 0;
	public static final int PLAYING_STATE = 1;
	public static final int DOWNLOADING_STATE = 2;
	
	private int mState = IDLE_STATE;

	protected ArrayList<MsgType> mMsgTypes;
	private Context mContext;
	protected LayoutInflater mInflater;
	
	//protected List<HashMap<String, Boolean>> mButtonStatuses;
	protected List<Integer> mButtonSelections;
	protected List<Integer> mProgressbarPercent;
	
	//private MediaPlayer mItemPlayer = null;
	private String mPlayLink = null;
	
	//private int lastPosition;
	private int curPosition;
	//private View lastView = null;
	//private View curView = null;
	
	//private File mDownloadFile = null;
	private File mDownloadDir = null;
	
	//private View mPlayingView;
	private Integer mPlayingPosition;
	
	private ViewHolder mPlayingHolder;
	
	// ArrayList<HashMap<Integer, View>> mlistPositionView = new ArrayList<HashMap<Integer, View>>();
	// HashMap<Integer, ViewHolder> mPositionViewHolder = new HashMap<Integer, ViewHolder>();
	List<Integer> mPositionDownloadingList = new ArrayList<Integer>();
	
	private HttpClient mClient;
	
	//private DownloadAudioTask mDownloadAudioTask;
	
	private final Handler mHandler = new Handler();
	
	private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
		    if (mPlayingPosition != null) {
                //updateProgressBar(mPlayingPosition);
		    }
        }
    };
	
//	private ImageLoaderCallback callback = new ImageLoaderCallback() {
//		@Override
//		public void refresh(String url, Bitmap bitmap) {
//			NearbyInfoArrayAdapter.this.refresh();
//		} 
//	};

	public MsgTypeArrayAdapter(Context context) {
		mMsgTypes = new ArrayList<MsgType>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		//mButtonStatuses = new ArrayList<HashMap<String, Boolean>>();
		mButtonSelections = new ArrayList<Integer>();
		mProgressbarPercent = new ArrayList<Integer>();
		
//		File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DOWNLOAD_DEFAULT_DIR);
//        if (!downloadDir.exists()) {
//            downloadDir.mkdirs();
//        }
//        mDownloadDir = downloadDir;
//		mClient = getApi().getHttpClient();
	}

	@Override
	public int getCount() {
		Log.v("list_count", String.valueOf(mMsgTypes.size()));
		return mMsgTypes.size();
	}

	@Override
	public Object getItem(int position) {
		return mMsgTypes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		public ImageView profileImage;  //头像
		public TextView title;  //第一行：用户名
		public TextView status;  //第一行：距离与时间
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;

		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);
		//boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		
		view = convertView;
		final ViewHolder holder;
		
		Log.v("getView", "into getView");
		
		if (view == null) {
//			view = mInflater.inflate(R.layout.user_item, parent, false);
			view = mInflater.inflate(R.layout.msgtype_item, parent, false);
			holder = new ViewHolder();
			holder.profileImage = (ImageView) view.findViewById(R.id.profile_image);
			
			holder.title  = (TextView) view.findViewById(R.id.type_name);
			holder.status = (TextView) view.findViewById(R.id.type_status);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final MsgType msgType = mMsgTypes.get(position);
		int msgTypeImageid = msgType.MsgTypeImageUrl;
		//mPlayLink = info.attachmentUrl;
		//Log.d(TAG, "mPlayLink = " + mPlayLink);
//		if(profileImage) {
		    holder.profileImage.setBackgroundResource(msgTypeImageid);
		    //holder.profileImage.setBackgroundResource(R.drawable.child_image);
//		}
		
		holder.title.setText(msgType.MsgTypeTitle);
		holder.status.setText(msgType.MsgTypeStatus);

		return view;
	}
	
	public void refresh(ArrayList<MsgType> msgTypes) {
		mMsgTypes = (ArrayList<MsgType>) msgTypes.clone();
		
		Log.v("adapter", String.valueOf(mMsgTypes.size()));
		
		for (int i=0; i<mMsgTypes.size(); i++) {
			//Log.d(TAG, "i = " + i);
			//mButtonStatuses.get(i).put(key, value);
			mButtonSelections.add(1);
			mProgressbarPercent.add(0);
		}
		
		notifyDataSetChanged();
	}
	
	@Override
	public void refresh() {
		notifyDataSetChanged();
	}
	
	protected void launchActivity(Intent intent) {
		mContext.startActivity(intent);
	}
	
    }

