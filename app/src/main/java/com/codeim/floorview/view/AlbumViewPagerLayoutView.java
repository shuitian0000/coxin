package com.codeim.floorview.view;

import java.util.ArrayList;
import java.util.List;

import com.codeim.coxin.util.CommonUtils;
import com.codeim.floorview.adapter.EmojiAdapter;
import com.codeim.floorview.adapter.EmojiViewPagerAdapter;
import com.codeim.floorview.adapter.ImageViewPaperAdapter;
import com.codeim.floorview.bean.Emoji;
import com.codeim.floorview.utils.EmojiConversionUtil;
import com.codeim.floorview.view.MatrixImageViewForViewPager.OnSingleTapListener;
import com.codeim.floorview.view.PopupWindowForMatrixImageHeader.OnDelClickListener;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.codeim.coxin.R;

/**
 * 
 ******************************************
 * @author  : Wang Yongwei
 * @文件名称	: EmojiRelativeLayoutView.java
 * @创建时间	: 
 * @文件描述	: 带表情的自定义输入框
 * @Param   : button: R.id.menu_emoji;
 *            button: R.id.status_new_content
 * @Interface:  setOnItemClickListener
 ******************************************
 */
//public class AlbumViewPagerLayoutView extends RelativeLayout implements
//		OnItemClickListener, OnClickListener {
public class AlbumViewPagerLayoutView extends RelativeLayout implements OnSingleTapListener
{

	private Context context;

	/** 显示表情页的viewpager */
	private AlbumViewPager vp_contains;

	/** 表情页界面集合 */
	private ArrayList<View> pageViews;

	/** 游标显示布局 */
	private LinearLayout layout_point_view;

	/** 游标点集合 */
	private ArrayList<ImageView> pointViews;

	/** 表情集合 */
	private List<List<Emoji>> emojis;

	/** 表情区域 */
	private View emojiview;

	/** 输入框 */
	private EditText et_sendmessage;

	/** 表情数据填充器 */
	private List<EmojiAdapter> emojiAdapters;

	/** 当前表情页 */
	private int current = 0;
	
	private ImageViewPaperAdapter mImageViewPaperAdapter;
    private ArrayList<String> url_paths;

	public AlbumViewPagerLayoutView(Context context) {
		super(context);
		this.context = context;
	}

	public AlbumViewPagerLayoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public AlbumViewPagerLayoutView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
//		EmojiConversionUtil.getInstace().getFileText(this.context);
//		emojis = EmojiConversionUtil.getInstace().emojiLists;
		
		onCreate();
	}
	
	public interface OnBeforeCreate
	{
	    boolean setLocal();
	}
	private OnBeforeCreate mOnBeforeCreateListener;
	public void setOnBeforeCreate(OnBeforeCreate mOnBeforeCreateListener)
	{
		this.mOnBeforeCreateListener = mOnBeforeCreateListener;
	}
	
    public void setPath(ArrayList<String> url_paths, boolean local_image) {
    	this.url_paths = url_paths;
    	mImageViewPaperAdapter.setPath(url_paths, local_image);
    }
    public void setPath(ArrayList<String> url_paths, boolean local_image, int init_position) {
    	this.url_paths = url_paths;
    	mImageViewPaperAdapter.setPath(url_paths, local_image);
    	vp_contains.setCurrentItem(init_position);
    }
    public ArrayList<String> getPath() {
    	return url_paths;
    }

	private void onCreate() {
		Init_View();
//		Init_viewPager();
//		Init_Point();
		Init_Data();
	}
	
	/**
	 * 初始化控件
	 */
	private void Init_View() {
		vp_contains = (AlbumViewPager) findViewById(R.id.image_view_pager);
		
		url_paths = new ArrayList<String> ();
		mImageViewPaperAdapter = new ImageViewPaperAdapter(context,url_paths);

		layout_point_view = (LinearLayout) findViewById(R.id.image_iv);
	}

	/**
	 * 初始化显示表情的viewpager
	 */
	private void Init_viewPager() {
		pageViews = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(context);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		pageViews.add(nullView1);

		// 中间添加表情页

		emojiAdapters = new ArrayList<EmojiAdapter>();
		for (int i = 0; i < emojis.size(); i++) {
			GridView view = new GridView(context);
			EmojiAdapter adapter = new EmojiAdapter(context, emojis.get(i));
			view.setAdapter(adapter);
			emojiAdapters.add(adapter);
//			view.setOnItemClickListener(this);
			view.setNumColumns(7);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			pageViews.add(view);
		}

		// 右侧添加空页面
		View nullView2 = new View(context);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		pageViews.add(nullView2);
	}

	/**
	 * 初始化游标
	 */
	private void Init_Point() {

		pointViews = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < pageViews.size(); i++) {
			imageView = new ImageView(context);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			layout_point_view.addView(imageView, layoutParams);
			if (i == 0 || i == pageViews.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.d2);
			}
			pointViews.add(imageView);

		}
	}

	/**
	 * 填充数据
	 */
	private void Init_Data() {
		vp_contains.setAdapter(mImageViewPaperAdapter);
		mImageViewPaperAdapter.setOnMovingListener(vp_contains);
		mImageViewPaperAdapter.setOnSingleTapListener(this);
		
		vp_contains.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
			
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
//
//		vp_contains.setCurrentItem(1);
//		current = 0;
//		vp_contains.setOnPageChangeListener(new OnPageChangeListener() {
//
//			@Override
//			public void onPageSelected(int arg0) {
//				current = arg0 - 1;
//				// 描绘分页点
//				draw_Point(arg0);
//				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
//				if (arg0 == pointViews.size() - 1 || arg0 == 0) {
//					if (arg0 == 0) {
//						vp_contains.setCurrentItem(arg0 + 1);// 第二屏 会再次实现该回调方法实现跳转.
//						pointViews.get(1).setBackgroundResource(R.drawable.d2);
//					} else {
//						vp_contains.setCurrentItem(arg0 - 1);// 倒数第二屏
//						pointViews.get(arg0 - 1).setBackgroundResource(
//								R.drawable.d2);
//					}
//				}
//			}
//
//			@Override
//			public void onPageScrolled(int arg0, float arg1, int arg2) {
//
//			}
//
//			@Override
//			public void onPageScrollStateChanged(int arg0) {
//
//			}
//		});

	}

	/**
	 * 绘制游标背景
	 */
	public void draw_Point(int index) {
		for (int i = 1; i < pointViews.size(); i++) {
			if (index == i) {
				pointViews.get(i).setBackgroundResource(R.drawable.d2);
			} else {
				pointViews.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

//	@Override
//	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//
//	}
	
//	@Override
//	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//		Emoji emoji = (Emoji) emojiAdapters.get(current).getItem(arg2);
//		mOnItemClickListener.onItemClick(emoji);
//	}
	
//	@Override
//	public void onClick(View view) {
//	}
//
//	private OnItemClickListener mOnItemClickListener;
//
//	public void setOnItemClickListener(OnItemClickListener l) {
//		mOnItemClickListener = l;
//	}
//
//	public interface OnItemClickListener {
//		public void onItemClick(Emoji emoji);
//	}
	
	@Override
	public void onSingleTap() {
		Log.v("AlbumViewPagerLayoutView", "onSingleTap()");
		
		mOnSingleClickListener.singleClick();
	}
	
	public interface OnSingleClickListener
	{
		void singleClick();
	}
	private OnSingleClickListener mOnSingleClickListener;
	public void setOnSingleClick(OnSingleClickListener mOnSingleClickListener)
	{
		this.mOnSingleClickListener = mOnSingleClickListener;
	}
	
	//屏蔽连续多次
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
	
	public int getCurrentItem() {
		return vp_contains.getCurrentItem();
	}
}

