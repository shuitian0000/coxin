package com.codeim.floorview;

import java.util.ArrayList;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.ImageManager;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.util.CommonUtils;
import com.codeim.floorview.adapter.AlbumGridViewAdapter;
import com.codeim.floorview.bean.ImageBucket;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.floorview.utils.AlbumHelper;
import com.codeim.floorview.view.AlbumViewPagerLayoutView;
import com.codeim.floorview.view.ListImageDirPopupWindow;
import com.codeim.floorview.view.PopupWindowForMatrixImageHeader;
import com.codeim.floorview.view.SelectForFullImagePopupWindow;
import com.codeim.floorview.view.AlbumViewPagerLayoutView.OnBeforeCreate;
//import com.xzh.sharetosina.utils.ImageManager2;



public class MatrixImageActivity extends Activity{
    private String TAG="MatrixImageActivity";
	
	private ArrayList<String> dataList = new ArrayList<String>();
	private ArrayList<String> selectedDataList = new ArrayList<String>();

	private AlbumViewPagerLayoutView mAlbumViewPagerLayoutView;
	private View contextView;
	
    
	public static final int HEADER_STYLE_UNDEF = 0;
	public static final int HEADER_TOOGLE_AND_FOOTER = 1;
	public static final int HEADER_DELETE_AND_FOOTER = 2;
	public static final int HEADER_DEFAULT_NO_FOOTER = 3;
	public static final int HEADER_ONLY_HEADER_BACK = 4;
	
    private int image_pager_style=HEADER_DELETE_AND_FOOTER;

    private View header_view;
    private View footer_view;
    
    private int popShow;
    private ImageView popup_ref;
    private boolean local_image;
    private int init_position;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
//		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);  
//		   setContentView(R.layout.main); 
		setContentView(R.layout.image_pager_layout);
		
		contextView = (View) findViewById(R.id.viewpager_contextview);
		contextView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		
		mAlbumViewPagerLayoutView = (AlbumViewPagerLayoutView) findViewById(R.id.image_viewpager);
		mAlbumViewPagerLayoutView.setVisibility(View.VISIBLE);
		
		selectedDataList.clear();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		init_position = (int)bundle.getInt("position", 0);
		image_pager_style = (int)bundle.getInt("style", HEADER_DELETE_AND_FOOTER);
		local_image = (boolean)bundle.getBoolean("local_image");
		selectedDataList = (ArrayList<String>)bundle.getSerializable("datalist");
		
		if(selectedDataList!=null && selectedDataList.size()>0) {
			init();
			initView();
			initListener();
		}
		else {
			Log.v(TAG,"ERROR, no image need to prepview");
			finish();
		}
	}

	private void init() {
		mAlbumViewPagerLayoutView.setOnBeforeCreate(new OnBeforeCreate() {
			@Override
			public boolean setLocal() {
				return local_image;
			}
		});
		mAlbumViewPagerLayoutView.setPath(selectedDataList, local_image, init_position);
	}
	
	private void initView() {
		LayoutInflater inflater = getLayoutInflater () ;
		popShow = 0;
		popup_ref = (ImageView) findViewById(R.id.popup_ref);

		header_view = inflater.inflate(R.layout.image_matrix_header, null);
		footer_view = inflater.inflate(R.layout.image_matrix_header, null);
		
		mAlbumViewPagerLayoutView.setOnSingleClick(new AlbumViewPagerLayoutView.OnSingleClickListener() {
			
			@SuppressWarnings("unused")
			@Override
			public void singleClick() {
				Log.v("AlbumViewPagerLayoutView", "singleClick()");
				
				if(popShow==0) {
					if(image_pager_style==HEADER_DEFAULT_NO_FOOTER) {
						showHeader(header_view,PopupWindowForMatrixImageHeader.HEADER_STYLE_DEFAULT, true);
					} else if(image_pager_style==HEADER_ONLY_HEADER_BACK) {
						showHeader(header_view,PopupWindowForMatrixImageHeader.HEADER_STYLE_ONLY_BACK, true);
					} else if(image_pager_style==HEADER_TOOGLE_AND_FOOTER) {
						showHeader(header_view,PopupWindowForMatrixImageHeader.HEADER_STYLE_TOOGLE, true);
						showHeader(header_view,PopupWindowForMatrixImageHeader.HEADER_STYLE_FOOTER, false);
					} else if(image_pager_style==HEADER_DELETE_AND_FOOTER) {
						showHeader(header_view,PopupWindowForMatrixImageHeader.HEADER_STYLE_DELETE, true);
						showHeader(header_view,PopupWindowForMatrixImageHeader.HEADER_STYLE_FOOTER, false);
					} else {
						Log.v(TAG, "please set the style");
						popShow = 1;
					}	
				} else if(popShow==1) {
					popShow =0;
				}
			}
		});
	}
	
	private void showHeader(View header_view,int style, boolean header_or_footer) {
		if(header_or_footer) {  //header
			final PopupWindowForMatrixImageHeader popup_header;
			popup_header = new PopupWindowForMatrixImageHeader(header_view,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
					false,style);
			popup_header.setOutsideTouchable(true);
			popup_header.showAsDropDown(popup_ref); 
			popShow = 2;
			popup_header.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					popShow = 1;
				}
			});
			final Button btn_back = (Button) popup_header.findViewById(R.id.image_matrix_header_back);
			final TextView txt = (TextView) popup_header.findViewById(R.id.image_matrix_header_txt);
			final ToggleButton toggle = (ToggleButton) popup_header.findViewById(R.id.image_matrix_header_toggle);
			final Button btn_del = (Button) popup_header.findViewById(R.id.image_matrix_header_delete);
			
			btn_back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popup_header.dismiss();
					image_matrix_back();
				}
			});
			btn_del.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = mAlbumViewPagerLayoutView.getCurrentItem();
					selectedDataList.remove(position);
					mAlbumViewPagerLayoutView.setPath(selectedDataList, local_image);
				}
			});
		} else {  //footer
			final PopupWindowForMatrixImageHeader popup_footer;
			popup_footer = new PopupWindowForMatrixImageHeader(footer_view,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
					false,style);
			popup_footer.setOutsideTouchable(true);
			popup_footer.showAtLocation(contextView, Gravity.BOTTOM, 0, 0);
			popShow = 2;
			final Button footer_back = (Button) popup_footer.findViewById(R.id.image_matrix_header_back);
			final Button btn_ok = (Button) popup_footer.findViewById(R.id.image_matrix_footer_btn);
			footer_back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popup_footer.dismiss();
					image_matrix_back();
				}
			});
			btn_ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popup_footer.dismiss();
					image_matrix_back();
				}
			});
		}
	}

	private void initListener() {
		
	}
    
	public void image_matrix_back() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("datalist",selectedDataList);
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
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

}
