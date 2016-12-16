package com.codeim.coxin.view;

import java.util.ArrayList;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public abstract class BaseDialogForListView<T> extends Dialog
{
	/**
	 * 布局文件的最外层View
	 */
	protected View mContentView;
	protected Context context;
	
	protected String mYtitle;
	protected String mYmsg;
	/**
	 * ListView的数据集
	 */
	protected ArrayList<T> mDatas;

	public BaseDialogForListView(Context context, int theme) {
		 super(context, theme);
		 this.context = context;
    }
	
	public BaseDialogForListView(Context context, int layout, int width, int height)
	{
		this(context, layout, width, height, null);
	}

	public BaseDialogForListView(Context context, int layout, int width, int height,
			ArrayList<T> mDatas)
	{
		this(context, layout, width, height, mDatas, new Object[0]);

	}

	public BaseDialogForListView(Context context, int layout, int width, int height,
			ArrayList<T> mDatas, Object... params)
	{
		super(context);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		this.context = context;
		setContentView(layout);
		
		if (mDatas != null)
			this.mDatas = mDatas;

		if (params != null && params.length > 0)
		{
			beforeInitWeNeedSomeParams(params);
		}
		
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		//set width,height by density and gravity
		float density = getDensity(context);
		lp.width = (int) (width*density);
		lp.height = (int) (height*density);
		lp.gravity = Gravity.CENTER;
		window.setAttributes(lp);

//		setBackgroundDrawable(new BitmapDrawable());
//		setTouchable(true);
//		setOutsideTouchable(true);
//		setTouchInterceptor(new OnTouchListener()
//		{
//			@Override
//			public boolean onTouch(View v, MotionEvent event)
//			{
//				if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
//				{
//					dismiss();
//					return true;
//				}
//				return false;
//			}
//		});
		
		
//		initViews();
//		initEvents();
//		init();
	}
	protected float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	 // TODO Auto-generated method stub
	 super.onCreate(savedInstanceState);
	}
	
	@Override
	public void show() {
	  super.show();
	}

	protected abstract void beforeInitWeNeedSomeParams(Object... params);

	public abstract void initViews();

	public abstract void initEvents();

	public abstract void init();

//	public View findViewById(int id)
//	{
//		return mContentView.findViewById(id);
//	}

	protected static int dpToPx(Context context, int dp)
	{
		return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
	}
	
	 public void setTitle(String title) {
		 this.mYtitle = title;
	 }
	 public void setMsg(String msg) {
		 this.mYmsg = msg;
	 }

}
