package com.codeim.floorview;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.floorview.adapter.AlbumGridViewAdapter;
import com.codeim.floorview.bean.ImageBucket;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.floorview.utils.AlbumHelper;
import com.codeim.floorview.view.ListImageDirPopupWindow;
//import com.xzh.sharetosina.utils.ImageManager2;



public class AlbumActivity extends BaseNoDoubleClickActivity{
	
	private GridView gridView;
	private ArrayList<String> dataList = new ArrayList<String>();
	private HashMap<String,ImageView> hashMap = new HashMap<String,ImageView>();
	private ArrayList<String> selectedDataList = new ArrayList<String>();
	private String cameraDir = "/DCIM/";//相片默认的存放路径
	private ProgressBar progressBar;
	private AlbumGridViewAdapter gridImageAdapter;
	private LinearLayout selectedImageLayout;
	private Button okButton;
	private HorizontalScrollView scrollview;
	
	private AlbumHelper albumHelper;
	public static List<ImageBucket> contentList;
//	private ArrayList<ImageItem> dataList;
	
	private RelativeLayout headView;
	private TextView folder_name;
	private LinearLayout  button_album_select;
	private boolean album_pop_en;
	private ListImageDirPopupWindow image_folder_pop_view;
	private ArrayList<ImageFolder> imageFolderList;
	ArrayList<String> alltmpList = new ArrayList<String>();
	private String folderName;
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_album);
		
		headView = (RelativeLayout) findViewById(R.id.headView);
		folder_name = (TextView) findViewById(R.id.folder_name);
		button_album_select = (LinearLayout) findViewById(R.id.album_select);
		album_pop_en = false;
		
		selectedDataList.clear();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		selectedDataList = (ArrayList<String>)bundle.getSerializable("datalist");
		if(selectedDataList==null) {
			selectedDataList = new ArrayList<String>();
//			selectedDataList.add("camera_default");
		}
		
		init();
		initListener();
	}

	private void init() {		
		progressBar = (ProgressBar) findViewById(R.id.comment_album_progressbar);
		progressBar.setVisibility(View.GONE);
		gridView = (GridView)findViewById(R.id.comment_album_myGrid);
		gridImageAdapter = new AlbumGridViewAdapter(this, dataList,selectedDataList);
		gridView.setAdapter(gridImageAdapter);
		
		albumHelper = AlbumHelper.getHelper();
		albumHelper.init(getApplicationContext());
//		contentList = albumHelper.getImagesBucketList(false);
		imageFolderList = new ArrayList<ImageFolder>();
		initData(); //prepare the data
		
//		DisplayMetrics outMetrics = new DisplayMetrics(); 
//		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
//		int mScreenHeight = outMetrics.heightPixels;
		
//		LayoutInflater inflater = getLayoutInflater () ;
////        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View mView = inflater.inflate(R.layout.image_folder_pop_view, null);
//		image_folder_pop_view = new ListImageDirPopupWindow(LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
//				                   imageFolderList, mView);
		
		selectedImageLayout = (LinearLayout)findViewById(R.id.selected_image_layout);
		okButton = (Button)findViewById(R.id.ok_button);
		scrollview = (HorizontalScrollView)findViewById(R.id.scrollview);
		
		initSelectImage();
		
	}

	private void initSelectImage() {
		if(selectedDataList==null)
			return;
		for(final String path:selectedDataList){
			ImageView imageView = (ImageView) LayoutInflater.from(AlbumActivity.this).inflate(R.layout.comment_choose_imageview, selectedImageLayout,false);
			selectedImageLayout.addView(imageView);			
			hashMap.put(path, imageView);
			TwitterApplication.mImageManager.displayImage(imageView, path,R.drawable.camera_default,100,100);
//			ImageManager2.from(AlbumActivity.this).displayImage(imageView, path,R.drawable.camera_default,100,100);
			imageView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					removePath(path);
					gridImageAdapter.notifyDataSetChanged();
				}
			});
		}
		okButton.setText("完成("+selectedDataList.size()+"/9)");
	}

	private void initListener() {
		
		gridImageAdapter.setOnItemClickListener(new AlbumGridViewAdapter.OnItemClickListener() {
			
			@Override
			public void onItemClick(final ToggleButton toggleButton, int position, final String path,boolean isChecked) {
				
				if(selectedDataList!=null && selectedDataList.size()>=9){
					toggleButton.setChecked(false);
					if(!removePath(path)){
						Toast.makeText(AlbumActivity.this, "一次只能选择9张图片", 200).show();
					}
					return;
				}
					
				if(isChecked){
					if(!hashMap.containsKey(path)){
						ImageView imageView = (ImageView) LayoutInflater.from(AlbumActivity.this).inflate(R.layout.comment_choose_imageview, selectedImageLayout,false);
						selectedImageLayout.addView(imageView);
						imageView.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								
								int off = selectedImageLayout.getMeasuredWidth() - scrollview.getWidth();  
							    if (off > 0) {  
							    	  scrollview.smoothScrollTo(off, 0); 
							    } 
								
							}
						}, 100);
						
						hashMap.put(path, imageView);
						selectedDataList.add(path);
//						ImageManager2.from(AlbumActivity.this).displayImage(imageView, path,R.drawable.camera_default,100,100);
						TwitterApplication.mImageManager.displayImage(imageView, path,R.drawable.camera_default,100,100);
						imageView.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								toggleButton.setChecked(false);
								removePath(path);
								
							}
						});
						okButton.setText("完成("+selectedDataList.size()+"/9)");
					}
				}else{
					removePath(path);
				}
				
				
				
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				// intent.putArrayListExtra("dataList", dataList);
				bundle.putStringArrayList("datalist",selectedDataList);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
				
			}
		});
		
//		button_album_select.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(album_pop_en) {
//					image_folder_pop_view.dismiss();
//					album_pop_en = false;
//				} else {
////					DisplayMetrics outMetrics = new DisplayMetrics(); 
////					getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
////					int mScreenHeight = outMetrics.heightPixels;
////					image_folder_pop_view = new ListImageDirPopupWindow(LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
////							                   imageFolderList ,v);
//					image_folder_pop_view.refresh();
//					album_pop_en = false;
//				}
//			}
//		});
//		image_folder_pop_view.setOnImageDirSelected(new com.codeim.floorview.view.ListImageDirPopupWindow.OnImageDirSelected() {
//			@Override
//			public void selected(ImageFolder floder) {
//				refreshData(floder.getName());
//				image_folder_pop_view.dismiss();
//				album_pop_en = false;
//			}
//		});
		
	}
	
	private void initImageFolderListPopView() {
		DisplayMetrics outMetrics = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		int mScreenHeight = outMetrics.heightPixels;
		
		LayoutInflater inflater = getLayoutInflater () ;
//      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.image_folder_pop_view, null);
		image_folder_pop_view = new ListImageDirPopupWindow(LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
				                   imageFolderList, mView);
		image_folder_pop_view.setOutsideTouchable(true);
		image_folder_pop_view.setFocusable(true);
		
		button_album_select.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v("Album", "into popview");
				if(image_folder_pop_view.isShowing()) {
					image_folder_pop_view.dismiss();
					album_pop_en = false;
				} else {
//					DisplayMetrics outMetrics = new DisplayMetrics(); 
//					getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
//					int mScreenHeight = outMetrics.heightPixels;
//					image_folder_pop_view = new ListImageDirPopupWindow(LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
//							                   imageFolderList ,v);
					
//					mListImageDirPopupWindow.setAnimationStyle(R.style.anim_popup_dir);
					image_folder_pop_view.showAsDropDown(headView,0,0);
					image_folder_pop_view.refresh();
					
					//设置背景颜色变暗
					WindowManager.LayoutParams lp = getWindow().getAttributes();
					lp.alpha = 0.3f;  
					getWindow().setAttributes(lp);
					 
					album_pop_en = true;
				}
			}
		});
		image_folder_pop_view.setOnImageDirSelected(new com.codeim.floorview.view.ListImageDirPopupWindow.OnImageDirSelected() {
			@Override
			public void selected(ImageFolder floder) {
				folderName = floder.getName();
				
				image_folder_pop_view.dismiss();
				refreshData();
				album_pop_en = false;
			}
		});
		image_folder_pop_view.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss()
			{
				// 设置背景颜色变亮
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
			}
		});
	}
	
	private boolean removePath(String path){
		if(hashMap.containsKey(path)){
			selectedImageLayout.removeView(hashMap.get(path));
			hashMap.remove(path);
			removeOneData(selectedDataList,path);
			okButton.setText("完成("+selectedDataList.size()+"/9)");
			return true;
		}else{
			return false;
		}
	}
	
	private void removeOneData(ArrayList<String> arrayList,String s){
		for(int i =0;i<arrayList.size();i++){
			if(arrayList.get(i).equals(s)){
				arrayList.remove(i);
				return;
			}
		}
	}
	
    private void initData(){
    	
    	new AsyncTask<Void, Void, ArrayList<String>>(){
    		
    		@Override
    		protected void onPreExecute() {
    			progressBar.setVisibility(View.VISIBLE);
    			super.onPreExecute();
    		}

			@Override
			protected ArrayList<String> doInBackground(Void... params) {
				ArrayList<String> tmpList = new ArrayList<String>();
				
				if(!hasSdcard()) {
//					return tmpList;
					Toast.makeText(AlbumActivity.this, "SD卡没有准备好，请插入SD卡，否则无法正确寻找所有图片", 200).show();
				} else {
					cameraDir = Environment.getExternalStorageDirectory().toString();
//					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//	                    Intent mediaScanIntent = new Intent(
//	                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//	                    Uri contentUri = Uri.fromFile(mPhotoFile); //out is your output file
//	                    mediaScanIntent.setData(contentUri);
//	                    sendBroadcast(mediaScanIntent);
//	                } else {
//	                    sendBroadcast(new Intent(
//	                            Intent.ACTION_MEDIA_MOUNTED,
//	                            Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
//	                }
					Log.v("AlbumActivity", "default scard pic directory: "+cameraDir);
				}
				
				//1.the old method,  list the file directory
				/*ArrayList<String> listDirlocal =  listAlldir( new File(cameraDir));
                ArrayList<String> listDiranjuke = new ArrayList<String>();
                listDiranjuke.addAll(listDirlocal);
                
                for (int i = 0; i < listDiranjuke.size(); i++){
                    listAllfile( new File( listDiranjuke.get(i) ),tmpList);
                } */
				
				
				//2.the new method: using the ImageBucket by Media Provider
				//contentList = albumHelper.getImagesBucketList(false);//具体会显示出哪些文件夹的照片，由这个结果决定，这个是分文件夹的
				contentList = albumHelper.getImagesBucketList(false);
				List<ImageBucket> contentList_internal = new ArrayList<ImageBucket>();
				contentList_internal = albumHelper.getImagesBucketList_internal(false);
				contentList.addAll(contentList_internal);
//				button_album_select.setText("所有图片");
				
				imageFolderList = new ArrayList<ImageFolder>(); //存储所有目录，第一个是特殊目录
				ImageFolder mImageFolder = new ImageFolder(); //第一个文件夹比较特殊：所有照片
				if(contentList!=null && contentList.size()>0) {
					mImageFolder = new ImageFolder(contentList.get(0).imageList.get(0).imagePath, //是不是应该thumbnailPath
							"所有照片", 0);
				}
				mImageFolder.setSelected(true);//进入这个界面默认显示所有照片
				imageFolderList.add(mImageFolder);

				for(int i = 0; i<contentList.size(); i++){ //构建所有含有图片的目录
					
					mImageFolder = new ImageFolder(contentList.get(i).imageList.get(0).imagePath, //是不是应该thumbnailPath
							contentList.get(i).bucketName, contentList.get(i).imageList.size());
					mImageFolder.setSelected(false);
					imageFolderList.add(mImageFolder);
					
					if(contentList.get(i).imageList != null) { //构建第一个特殊目录的 图片文件
					    for(int j=0; j<contentList.get(i).imageList.size(); j++) {
						    tmpList.add(contentList.get(i).imageList.get(j).imagePath);
					    }
					}
				}
				
				alltmpList.addAll(tmpList);
				imageFolderList.get(0).setCount(alltmpList.size()); //设置第一个的目录，所有照片的个数
				return tmpList;
			}
			
			protected void onPostExecute(ArrayList<String> tmpList) {
				
				if(AlbumActivity.this==null||AlbumActivity.this.isFinishing()){
					return;
				}
				progressBar.setVisibility(View.GONE);
				dataList.clear(); //显示所有当前选择出的目录下的图片文件，这里第一次默认是所有图片
				dataList.addAll(tmpList);
				
				initImageFolderListPopView();
				
				gridImageAdapter.notifyDataSetChanged();
				return;
				
			};
    		
    	}.execute();
    }
    
    private void refreshData(){
    	
    	new AsyncTask<Void, Void, ArrayList<String>>(){
    		
    		@Override
    		protected void onPreExecute() {
    			progressBar.setVisibility(View.VISIBLE);
    			super.onPreExecute();
    		}

			@Override
			protected ArrayList<String> doInBackground(Void... params) {
				ArrayList<String> tmpList = new ArrayList<String>();
				
				//the new method: using the ImageBucket by Media Provider
				folder_name.setText(folderName);
                if(folderName.equals("所有照片")) {
//				for(int i = 0; i<contentList.size(); i++){
//					if(contentList.get(i).imageList != null) {
//					    for(int j=0; j<contentList.get(i).imageList.size(); j++) {
//						    tmpList.add(contentList.get(i).imageList.get(j).imagePath);
//					    }
//					}
					
//				}
                    tmpList.addAll(alltmpList);
                } else {
                	int folder_num = contentList.size();
                	for(int i=0; i<folder_num; i++) {
                		if(contentList.get(i).bucketName.equals(folderName)&&
                				contentList.get(i).imageList != null) {
                			int image_num = contentList.get(i).imageList.size();
    					    for(int j=0; j<image_num; j++) {
    						    tmpList.add(contentList.get(i).imageList.get(j).imagePath);
    					    }
    					    
    					    break;
                		}
                	}
                }
                
                //set the select flag for folder
                for(ImageFolder folder: imageFolderList) {
                	if(folder.getName().equals(folderName)) {
                		folder.setSelected(true);
                	} else {
                		folder.setSelected(false);
                	}
                }				
				return tmpList;
			}
			
			protected void onPostExecute(ArrayList<String> tmpList) {
				
				if(AlbumActivity.this==null||AlbumActivity.this.isFinishing()){
					return;
				}
				progressBar.setVisibility(View.GONE);
				dataList.clear();
				dataList.addAll(tmpList);
				gridImageAdapter.notifyDataSetChanged();
				return;
				
			};
    		
    	}.execute();
    }
    
    private ArrayList<String>  listAlldir(File nowDir){
        ArrayList<String> listDir = new ArrayList<String>();
        nowDir = new File(Environment.getExternalStorageDirectory() + nowDir.getPath());
        if(!nowDir.isDirectory()){
            return listDir;
        }
                
        File[] files = nowDir.listFiles();

        for (int i = 0; i < files.length; i++){
            if(files[i].getName().substring(0,1).equals(".")){
               continue; 
            }
            File file = new File(files[i].getPath()); 
            if(file.isDirectory()){
                listDir.add(files[i].getPath());
            }
        }              
        return listDir;
    }
    
    private ArrayList<String>  listAllfile( File baseFile,ArrayList<String> tmpList){
        if(baseFile != null && baseFile.exists()){
            File[] file = baseFile.listFiles();
            for(File f : file){
                if(f.getPath().endsWith(".jpg") || f.getPath().endsWith(".jpeg")||f.getPath().endsWith(".png")){
                    tmpList.add(f.getPath());
                }
            }
        }         
        return tmpList;
    }
    
    @Override
    public void onBackPressed() {
    	finish();
//    	super.onBackPressed();
    }
    
    @Override
    public void finish() {
    	// TODO Auto-generated method stub
    	super.finish();
//    	ImageManager2.from(AlbumActivity.this).recycle(dataList);
    }
    
    @Override
    protected void onDestroy() {
    	
    	super.onDestroy();
    }
    
	public boolean hasSdcard(){
		String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
		}
	}

}
