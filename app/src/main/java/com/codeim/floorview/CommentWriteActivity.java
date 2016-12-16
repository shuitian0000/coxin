package com.codeim.floorview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.fanfou.Photo;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.location.LocationUtils;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskFeedback;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.floorview.adapter.AlbumPreviewGridImageAdapter;
import com.codeim.floorview.adapter.CommentWriteMoreAddAdapter;
import com.codeim.floorview.bean.Comment;
import com.codeim.floorview.bean.Emoji;
import com.codeim.floorview.utils.EmojiConversionUtil;
import com.codeim.floorview.utils.TextNumLimitWatcher;
import com.codeim.floorview.view.EmojiRelativeLayoutView;
import com.codeim.coxin.R;

public class CommentWriteActivity extends BaseNoDoubleClickActivity{
	
	static final String TAG = "WriteCommentActivity";

	protected Feedback mFeedback;
	protected NavBar mNavBar;
	InputMethodManager mInputMethodManager;
	
	protected EditText comment_content;
//	protected EditText addLocation;
	protected Button comment_menu_send;
	protected String commentContext;
	protected String commentLocation;
	
	
	private static final int REQUESTCODE_PIC_RESULT = 0;
	private static final int REQUESTCODE_CAMERA_RESULT = 1;
	
	private static final int MATRIX_IMAGE_PREVIEW = 2;
	//for camera return
    private static final int PIC_RESULT = 3;
    private static final int PIC_RESULT_KK = 4;
	//照相机
	private String picPath = "";
	//选择图片
	protected GridView grid_addPics;
	private ArrayList<String> dataList = new ArrayList<String>();
	private AlbumPreviewGridImageAdapter gridImageAdapter;
	/*判断选择表情的按钮是否打开*/
	private EmojiRelativeLayoutView emojiRelativeLayoutView;
	/*更多选项*/
	private GridView moreAddGridView;
	private CommentWriteMoreAddAdapter mMoreAddAdapter;

	private GenericTask mSendInfoTask;
	
	private String mSendInfoFeedback;
	
	protected static final int STATE_ALL = 0;
	
	private int current = 0;
	
	private int info_id;
	private int parent_id;
	private int floornum;
	
	private int new_comment_id;
	private int new_comment_cnt;
	
//	private AlbumViewPagerLayoutView mAlbumViewPagerLayoutView;
	
	private TaskListener mSendInfoTaskListener = new TaskAdapter() {
		
		@Override
		public void onPreExecute(GenericTask task) {
			//onRegisterBegin();
			TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, CommentWriteActivity.this).start("正在发送");
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
				TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, CommentWriteActivity.this).failed("IO_ERROR");

				// mLocationDisplay.setText(mRegisterFeedback);
				//warnDialog(mSendInfoFeedback);
			} else {
				 //onRegisterFailure("注册失败");
				 TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, CommentWriteActivity.this).failed("发布失败");
			}
		}
		
		@Override
		public String getName() {
			return "AddInfo";
		}
	};
	
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.comment_write);
			mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
			mNavBar.setHeaderTitle("写评论");
			mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
			mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);
			
			//activity param floornum
	        Bundle bundle = this.getIntent().getExtras();
			info_id  = Integer.valueOf(TwitterApplication.mPref.getString(Preferences.CURRENT_INFO_ID, ""));
			parent_id =bundle.getInt("parentid");
			floornum = bundle.getInt("floornum");
			floornum = floornum +1;
			
			//the footer menu, from weibo
			comment_menu_send = (Button) findViewById(R.id.comment_menu_send);
			comment_content = (EditText) findViewById(R.id.status_new_content);
//			addLocation = (EditText) findViewById(R.id.addInfoPlace);
			
			
			comment_content.addTextChangedListener(
	                new TextNumLimitWatcher((TextView) findViewById(R.id.comment_menu_send), comment_content, this));
			comment_content.setDrawingCacheEnabled(true);
			//正文中的图片
			grid_addPics=(GridView)this.findViewById(R.id.addPic);
			dataList.add("camera_default");
			gridImageAdapter=new AlbumPreviewGridImageAdapter(this, dataList);
			grid_addPics.setAdapter(gridImageAdapter);
			//表情
			emojiRelativeLayoutView = (EmojiRelativeLayoutView) findViewById(R.id.ll_facechoose);
//			mAlbumViewPagerLayoutView = (AlbumViewPagerLayoutView) findViewById(R.id.image_viewpager);
			//更多
			mMoreAddAdapter = new CommentWriteMoreAddAdapter(this);
			moreAddGridView=(GridView)this.findViewById(R.id.moreAdd_gridView);
			moreAddGridView.setAdapter(mMoreAddAdapter);
			
			//for autoCompleteAdapter, not use for temp
//	        AutoCompleteAdapter adapter = new AutoCompleteAdapter(this, comment_content,
//	                (ProgressBar) title.findViewById(R.id.have_suggest_progressbar));
//	        comment_content.setAdapter(adapter);
			
			
			//from fanfou
			setupState();
//			showInputMethod();
//			registerForContextMenu(getTweetList());
//			registerOnClickListener(getTweetList());
			
//			InputMethodManager mInputMethodManager = (InputMethodManager) 
			mInputMethodManager = (InputMethodManager) 
		    getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
			mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			
			actionBar=getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			//actionBar.setHomeButtonEnabled(true);
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//	    menu.findItem(R.id.action_search).setVisible(false);
//		menu.findItem(R.id.action_edit).setVisible(false);
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	protected void setupState() {
		//adapter
		gridImageAdapter.setOnItemClickListener(new AlbumPreviewGridImageAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(final Button btn, int position, final String path) {
				dataList.remove(position);
				gridImageAdapter.notifyDataSetChanged();
			}
		});
		gridImageAdapter.setOnDefaultItemClickListener(new AlbumPreviewGridImageAdapter.OnDefaultItemClickListener() {
			@Override
			public void onItemClick(final int position, final String path) {
				
		        LayoutInflater inflater = getLayoutInflater();
		        final View mView = inflater.inflate(R.layout.image_delete_full_shower, null);
		        
				if(path.contains("default")) {
				    Intent intent=new Intent(CommentWriteActivity.this, AlbumActivity.class);
				    Bundle bundle=new Bundle();
				    intent.putStringArrayListExtra("datalist", getIntentArrayList(dataList));
				    intent.putExtras(bundle);
				    startActivityForResult(intent, 0);
				} else  {
					/**直接弹出popupwindow**/
//					final DeleteForFullImagePopupWindow mPopupWindow;
//					mPopupWindow = new DeleteForFullImagePopupWindow(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
//							path, mView);
//					mPopupWindow.setOutsideTouchable(true);
//					mPopupWindow.setFocusable(true);
//					mPopupWindow.showAtLocation(mView, Gravity.CENTER, 0, 0);
////					mPopupWindow.showAsDropDown(mNavBar);
//					
//					mPopupWindow.setOnCheckerSelected(new com.codeim.floorview.view.DeleteForFullImagePopupWindow.OnCheckerClickListener() {
//						@Override
//						public void checkerSelected() {
//							dataList.remove(position);
//							gridImageAdapter.notifyDataSetChanged();
//							
//							mPopupWindow.dismiss();
//						}
//					});
					
					/**用viewpager**/
//				    mAlbumViewPagerLayoutView.setPath(getIntentArrayList(dataList));
//					mAlbumViewPagerLayoutView.setVisibility(View.VISIBLE);
					
					Intent intent=new Intent(CommentWriteActivity.this, MatrixImageActivity.class);
					Bundle bundle=new Bundle();
					intent.putExtra("position", position);//第几副图片
					intent.putExtra("style", MatrixImageActivity.HEADER_DELETE_AND_FOOTER);//可缩放全屏图片的按钮风格
					intent.putExtra("local_image", true);//由于MatrixImageActivity供上传时本地图片和读取时网络图片共用，两种情况在本地缓存的名字记录风格不一样
					intent.putStringArrayListExtra("datalist", getIntentArrayList(dataList));
					intent.putExtras(bundle);
					startActivityForResult(intent, MATRIX_IMAGE_PREVIEW);
				}
			}
		});
		
		
		comment_menu_send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// doLogin();
				doSendInfo();
			}
		});
		
		//for other menu button, from weibo
		findViewById(R.id.menu_add_pic).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//                if (TextUtils.isEmpty(picPath)) {
//                    //addPic();
//                      new SelectPictureDialog().show(getFragmentManager(), "");
//                } else {
                    //showPic();
                	//shouPic from weibo
//                    Intent intent = new Intent(WriteCommentActivity.this,
//                            BrowserWriteCommentLocalPicActivity.class);
//                    intent.putExtra("path", picPath);
//                    startActivityForResult(intent, BROWSER_PIC);
					Intent intent=new Intent(CommentWriteActivity.this, AlbumActivity.class);
					Bundle bundle=new Bundle();
					intent.putStringArrayListExtra("datalist", getIntentArrayList(dataList));
					intent.putExtras(bundle);
					startActivityForResult(intent, REQUESTCODE_PIC_RESULT);
                	
//                }
			}
		});
		findViewById(R.id.menu_camera).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//				startActivityForResult(intent, REQUESTCODE_CAMERA_RESULT);	
				
				Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// 判断存储卡是否可以用，可用进行存储
				if (hasSdcard()) {
					intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), picPath)));
				}
				startActivityForResult(intentFromCapture, REQUESTCODE_CAMERA_RESULT);
			}
		} );
		comment_content.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (emojiRelativeLayoutView.getVisibility() == View.VISIBLE) {
					emojiRelativeLayoutView.setVisibility(View.GONE);
				}
				if (moreAddGridView.getVisibility() == View.VISIBLE) {
					moreAddGridView.setVisibility(View.GONE);
				}
			}
			
		});
		//点击表情按钮，打开表情的GridView
		findViewById(R.id.menu_emoji).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (emojiRelativeLayoutView.getVisibility() == View.VISIBLE) {
					emojiRelativeLayoutView.setVisibility(View.GONE);
				} else {
					mInputMethodManager.hideSoftInputFromWindow(comment_content.getWindowToken(),0);
					moreAddGridView.setVisibility(View.GONE);
					
					emojiRelativeLayoutView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		emojiRelativeLayoutView.setOnItemClickListener(new EmojiRelativeLayoutView.OnItemClickListener() {
			@Override
			public void onItemClick(Emoji emoji) {
				if (emoji.getId() == R.drawable.face_del_icon) {
				int selection = comment_content.getSelectionStart();
//				int selection_end = comment_content.getSelectionEnd();
				String text = comment_content.getText().toString();
				if (selection > 0) {
					String text2 = text.substring(selection - 1);
					if ("]".equals(text2)) {
						int start = text.lastIndexOf("[");
						int end = selection;
						comment_content.getText().delete(start, end);
						return;
					}
					comment_content.getText().delete(selection - 1, selection);
				}
				return;
			}
			if (!TextUtils.isEmpty(emoji.getCharacter())) {
//				if (emojiRelativeLayoutView.mListener != null)
//					emojiRelativeLayoutView.mListener.onCorpusSelected(emoji);
				SpannableString spannableString = EmojiConversionUtil.getInstace()
						.addFace(emojiRelativeLayoutView.getContext(), emoji.getId(), emoji.getCharacter());
//				comment_content.append(spannableString);
				
				int selection = comment_content.getSelectionStart();
				Editable editable = comment_content.getText();
				editable.insert(selection, spannableString);
			}
			}
			
		});
		
		findViewById(R.id.menu_more_add).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (moreAddGridView.getVisibility() == View.VISIBLE) {
					moreAddGridView.setVisibility(GridView.GONE);
				} else {
					mInputMethodManager.hideSoftInputFromWindow(comment_content.getWindowToken(),0);
					emojiRelativeLayoutView.setVisibility(View.GONE);
					
					moreAddGridView.setVisibility(GridView.VISIBLE);
				}
			}
		});
	}
	private ArrayList<String> getIntentArrayList(ArrayList<String> dataList) {
		ArrayList<String> tDataList = new ArrayList<String>();
		for (String s : dataList) {
			if (!s.contains("default")) {
				tDataList.add(s);
			}
		}
		return tDataList;
	}
	
	
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//return from album, 选择多张图片后
		if ( requestCode == REQUESTCODE_PIC_RESULT ) {
			if(resultCode==RESULT_OK) {  //RESULT_OK: -1
        	    Bundle bundle=data.getExtras();
        	    ArrayList<String> tDataList=(ArrayList<String>) bundle.getSerializable("datalist");
        	    if(tDataList!=null){
        		    if (tDataList.size() < 12) {
					    tDataList.add("camera_default");
				    }
        		
        		    dataList.clear();
        		    dataList.addAll(tDataList);
        	        gridImageAdapter.notifyDataSetChanged();	
        	    }
			}	
		}
        //拍摄图片后
        else if(requestCode == REQUESTCODE_CAMERA_RESULT)
        {	
        	if (resultCode != RESULT_OK) 
    		{   
    	        return;   
    	    }
        	if(picPath!="") {
        	    dataList.add(Environment.getExternalStorageDirectory() + picPath);
	            gridImageAdapter.notifyDataSetChanged();
        	}
        }
		/* this is the test for picture selector
        else if(requestCode==PIC_RESULT) {

            picPath = Utility.getPicPathFromUri(data.getData(), this);
        }
        else if(requestCode==PIC_RESULT_KK) {
        	ConvertKKUriToPathFragment fragment = ConvertKKUriToPathFragment
                    .newInstance(intent.getData());
            getSupportFragmentManager().beginTransaction().add(fragment, "").commit();
        } */
		   //拍摄图片后
        else if(requestCode == MATRIX_IMAGE_PREVIEW)
        {	
			if(resultCode==RESULT_OK) {  //RESULT_OK: -1
        	    Bundle bundle=data.getExtras();
        	    ArrayList<String> tDataList=(ArrayList<String>) bundle.getSerializable("datalist");
        	    if(tDataList!=null){
        		    if (tDataList.size() < 12) {
					    tDataList.add("camera_default");
				    }
        		
        		    dataList.clear();
        		    dataList.addAll(tDataList);
        	        gridImageAdapter.notifyDataSetChanged();	
        	    }
			}	
        }
	}
	
	
	/**
	 * 显示键盘
	 * 
	 * @param context
	 * @param view
	 */
	public static void showInputMethod(Context context, View view) {
	    InputMethodManager im = (InputMethodManager) 
	    context.getSystemService(Context.INPUT_METHOD_SERVICE);
	    im.showSoftInput(view, 0);
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
	
	// send info task
	private void doSendInfo() {
		commentContext = comment_content.getText().toString();
//		commentLocation = addLocation.getText().toString();
		commentLocation = " ";
		
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
				
			    Comment my_comment = TwitterApplication.mApi.sendComment(true, commentContext, info_id, parent_id, floornum,
	    		commentPlace,latitude, longitude);
			    
			    final List <String> images = new ArrayList <String>();
			    for(String image: dataList) {
			    	images.add(image);
			    }
			    
			    if(dataList.size()>0) {
			    	new_comment_id = (int) my_comment.getId();
			    	new_comment_cnt = (int) my_comment.getLastNumForThisInfo();

//			        TwitterApplication.mImageUp.upMultiImage(images,  new_info_id, new LazyImageUp.ImageUpCallback() {
//			    	    @Override
//			    	    public void refresh(String url, int info_id) {
//			    	    }
//			        });
			    	
			    	//try to use the serial way: one by one
			    	for(String file:dataList) {
			    		if(file.contains("default")) {
			    			
			    		} else {
			    		File photo = new File(file);
			    		    Photo mPhoto=TwitterApplication.mApi.uploadPhoto(2, String.valueOf(new_comment_id), photo); //2--comment
			    		    int photo_id = mPhoto.getId();
			    		}
			    	}
			    }
  
				Log.d(TAG, "mRegisterFeedback = "+my_comment.toString());
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.FAILED;
			}
			
			return TaskResult.OK;
		}
	}
	
	// UI helpers.
	private void updateProgress(String progress) {
		// mProgressText.setText(progress);
	}
	
	private void onSendInfoSuccess() {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, CommentWriteActivity.this).success("");
		updateProgress("评论成功");
		//mUsernameEdit.setText("");
		//mPasswordEdit.setText("");

		/*
		Intent intent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
		String action = intent.getAction();

		if (intent.getAction() == null || !Intent.ACTION_SEND.equals(action)) {
			// We only want to reuse the intent if it was photo send.
			// Or else default to the main activity.
			intent = new Intent(this, MainActivity.class);
		}
		*/
//		Intent intent = new Intent(this, NearbyActivity.class);
		
		//成功评论之后
		Toast.makeText(CommentWriteActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
//		Bundle bundle = new Bundle();
		intent.putExtra("infoId", info_id);
		intent.putExtra("commentCnt", new_comment_cnt);
//		bundle.putStringArrayList("datalist",selectedDataList);
//		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		
		/*
		// 发送消息给widget
		Intent reflogin = new Intent(this.getBaseContext(), FanfouWidget.class);
		reflogin.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		PendingIntent l = PendingIntent.getBroadcast(this.getBaseContext(), 0, reflogin, PendingIntent.FLAG_UPDATE_CURRENT);
		try {
			l.send();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 发送消息给widget_small
		Intent reflogin2 = new Intent(this.getBaseContext(), FanfouWidgetSmall.class);
		reflogin2.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		PendingIntent l2 = PendingIntent.getBroadcast(this.getBaseContext(), 0, reflogin2, PendingIntent.FLAG_UPDATE_CURRENT);
		try {
			l2.send();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

//		setResult(100);
//		startActivity(intent);
		finish();
	}
	
	public boolean hasSdcard(){
		String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
		}
	}
	
	
//	//for EmojiAdapter
//	protected Emoji getContextItemTweet(int position) {
////        position = position - 1;
//        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
//        if (position >= 0 && position < mEmojiAdapter.getCount()) {
//        	Emoji emoji = (Emoji) mEmojiAdapter.getItem(position);
//            if (emoji == null) {
//                return null;
//            } else {
//                return emoji;
//            }
//        } else {
//            return null;
//        }
//	}
	
//	@Override
//	public void onBackPressed() {
//		if(mAlbumViewPagerLayoutView.getVisibility()==View.VISIBLE) {
//			mAlbumViewPagerLayoutView.setVisibility(View.GONE);
//		}
//		else {
//			super.onBackPressed();
//		}
//	}

}
