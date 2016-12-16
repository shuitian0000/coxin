package com.codeim.floorview.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.codeim.coxin.TwitterApplication;
import com.codeim.floorview.adapter.ImageFolderAdapter;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.coxin.R;

public class SelectForFullImagePopupWindow extends BasePopupWindowForFullImage
{
	private String image_url;
	private View mView;
	
	private MatrixImageView mImageView;
    private ToggleButton mChecker;
    private boolean checker_state;
    
    //md2en is 0 when show local image;  1 when show the remote image
    private boolean md5en;
    
    //for image zoom
    private Matrix savedMatrix = new Matrix();
    private Matrix matrix = new Matrix();  //current matrix
    private PointF startPoint = new PointF();
	private PointF mid = new PointF();
	
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	
	private int mode = NONE;
	private float oldDist;
	float mDobleClickScale=2;
	
	private GestureDetector mGestureDetector;

    /*
     * boolean checker_state
     * boolean md5en
     */
	public SelectForFullImagePopupWindow(int width, int height,
			String image_url, View convertView, boolean checker_state, boolean md5en)
	{
		super(convertView, width, height, true, image_url);
		
		this.image_url = image_url;
		this.checker_state = checker_state;
		this.md5en = md5en;
		initViews();
		initEvents();
		init();
	}

	@Override
	public void initViews()
	{
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mView = inflater.inflate(R.layout.image_full_shower, null);

		mImageView = (MatrixImageView) findViewById(R.id.image_full);
		mChecker = (ToggleButton) findViewById(R.id.image_checker);
		mChecker.setChecked(checker_state);
		
		mImageView.setScaleType(ScaleType.FIT_CENTER);
		
//		TwitterApplication.mImageManager.displayImage(mImageView,
//				image_url, R.drawable.bg_userheader_cover);
		TwitterApplication.mImageManager.displayImage(mImageView,
				image_url, -1, md5en);
	}
	
	public void initMatrix() {
		mImageView.initMatrix();
	}

	public interface OnCheckerClickListener
	{
		void checkerSelected(boolean checked);
	}

	private OnCheckerClickListener mOnCheckerClickListener;

	public void setOnCheckerSelected(OnCheckerClickListener mImageDirSelected)
	{
		this.mOnCheckerClickListener = mImageDirSelected;
	}

	@Override
	public void initEvents()
	{
		mChecker.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnCheckerClickListener.checkerSelected(mChecker.isChecked());
				checker_state = mChecker.isChecked();
			}
		});
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void beforeInitWeNeedSomeParams(Object... params)
	{
		// TODO Auto-generated method stub
	}
	
}
