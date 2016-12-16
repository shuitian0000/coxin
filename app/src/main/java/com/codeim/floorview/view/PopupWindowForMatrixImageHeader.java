package com.codeim.floorview.view;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import com.codeim.floorview.view.SelectForFullImagePopupWindow.OnCheckerClickListener;
import com.codeim.coxin.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PopupWindowForMatrixImageHeader extends PopupWindow
{
	private String TAG="PopupWindowForMatrixImageHeader";
	
	public static final int HEADER_STYLE_UNDEF = 0;
	public static final int HEADER_STYLE_TOOGLE = 1;
	public static final int HEADER_STYLE_DELETE = 2;
	public static final int HEADER_STYLE_DEFAULT = 3;
	public static final int HEADER_STYLE_ONLY_BACK =4;
	
	public static final int HEADER_STYLE_FOOTER = 5;
	/**
	 * 布局文件的最外层View
	 */
	protected View mContentView;
	protected Context context;
	protected int header_style;
	
	private TextView txt;
	private ToggleButton btn_toggle;
	private Button btn_del;
	private Button btn_ok;

	public PopupWindowForMatrixImageHeader(View contentView, int width, int height,
			boolean focusable)
	{
		this(contentView, width, height, focusable, HEADER_STYLE_UNDEF);
	}

	public PopupWindowForMatrixImageHeader(View contentView, int width, int height,
			boolean focusable, int style)
	{
		this(contentView, width, height, focusable, style, new Object[0]);
	}

	public PopupWindowForMatrixImageHeader(View contentView, int width, int height,
			boolean focusable, int style, Object... params)
	{
		super(contentView, width, height, focusable);
		this.mContentView = contentView;
		context = contentView.getContext();
		
		header_style = style;

		if (params != null && params.length > 0)
		{
			beforeInitWeNeedSomeParams(params);
		}

		setBackgroundDrawable(new BitmapDrawable());
		setTouchable(true);
		setOutsideTouchable(true);
		setTouchInterceptor(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
				{
					dismiss();
					return true;
				}
				return false;
			}
		});
		initViews();
		initEvents();
		init();
	}

	protected void beforeInitWeNeedSomeParams(Object... params)
	{
		// TODO Auto-generated method stub
	}

	public void initViews() {
		txt = (TextView) findViewById(R.id.image_matrix_header_txt);
	    btn_toggle = (ToggleButton) findViewById(R.id.image_matrix_header_toggle);
		btn_del = (Button) findViewById(R.id.image_matrix_header_delete);
		
		btn_ok = (Button) findViewById(R.id.image_matrix_footer_btn);
		
		if(header_style == HEADER_STYLE_FOOTER) {
			btn_ok.setVisibility(View.VISIBLE);
			txt.setVisibility(View.GONE);
		} else if(header_style == HEADER_STYLE_TOOGLE) {
			btn_toggle.setVisibility(View.VISIBLE);
		} else if(header_style == HEADER_STYLE_DELETE) {
			btn_del.setVisibility(View.VISIBLE);
		} else if(header_style == HEADER_STYLE_DEFAULT) {
		} else if(header_style == HEADER_STYLE_ONLY_BACK) {
		} else {
			Log.v(TAG,"please set the header_style");
		}
		
		btn_toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				mOnCheckerClickListener.checkerSelected(isChecked);
			}
		});
		
		btn_del.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mOnDelClickListener.delClick();
			}
		});
	}
	
	public interface OnCheckerClickListener
	{
		void checkerSelected(boolean checked);
	}
	private OnCheckerClickListener mOnCheckerClickListener;
	public void setOnCheckerSelected(OnCheckerClickListener mImageSelected)
	{
		this.mOnCheckerClickListener = mImageSelected;
	}
	
	public interface OnDelClickListener
	{
		void delClick();
	}
	private OnDelClickListener mOnDelClickListener;
	public void setOnDelClick(OnDelClickListener mOnDelClickListener)
	{
		this.mOnDelClickListener = mOnDelClickListener;
	}

	public void initEvents() {
		
	}

	public void init() {
		
	}

	public View findViewById(int id)
	{
		return mContentView.findViewById(id);
	}

	protected static int dpToPx(Context context, int dp)
	{
		return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
	}

}
