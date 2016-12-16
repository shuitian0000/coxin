package com.codeim.floorview.view;

import java.util.ArrayList;

import com.codeim.floorview.adapter.ImageViewPaperAdapter;
import com.codeim.floorview.view.MatrixImageViewForViewPager.OnMovingListener;
import com.codeim.floorview.view.MatrixImageViewForViewPager.OnSingleTapListener;
import com.codeim.coxin.R;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

public class AlbumViewPager extends ViewPager implements
    OnChildMovingListener,OnMovingListener{
    /**  当前子控件是否处理拖动状态  */ 
    private boolean mChildIsBeingDragged=false;
    
    private Context context;
//    private ImageViewPaperAdapter adapter;
//    private ArrayList<String> url_paths;
//    private boolean md5en;
    
//    private AlbumViewPager vp_contains;
    
	/**  界面单击事件 用以显示和隐藏菜单栏 */ 
//	private OnSingleTapListener onSingleTapListener;
//	private OnMovingListener moveListener;
    
    public AlbumViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		
//		initView();
	}

	public AlbumViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

//	public AlbumViewPager(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		this.context = context;
//	}
    
//    public AlbumViewPager(Context context, ArrayList<String> url_paths) {
//		super(context);
//		// TODO Auto-generated constructor stub
//		this.context = context;
//		this.url_paths = url_paths;
//		this.md5en = false;
//		
////		initView();
//	}
//    
//    public AlbumViewPager(Context context, ArrayList<String> url_paths, boolean md5en) {
//		super(context);
//		// TODO Auto-generated constructor stub
//		this.context = context;
//		this.url_paths = url_paths;
//		this.md5en = md5en;
//		
////		initView();
//	}
    
//    public void initView() {
////    	vp_contains = (AlbumViewPager) (() context).findViewById(R.id.vp_contains);
//    	vp_contains = (AlbumViewPager) ((Activity) context).findViewById(R.id.image_view_pager);
//    			
//    	if(url_paths==null)
//    		url_paths = new ArrayList<String>();
//    	adapter = new ImageViewPaperAdapter(context, url_paths);
//    	
//    	vp_contains.setAdapter(adapter);
//    	adapter.notifyDataSetChanged();	
//    }
    
//    public void setPath(ArrayList<String> url_paths) {
//    	this.url_paths = url_paths;
//    }
	
//	public void setOnMovingListener(OnMovingListener listener){
    public void setOnMovingListener(){
		((ImageViewPaperAdapter)getAdapter()).setOnMovingListener(AlbumViewPager.this);
	}
//	public void setOnSingleTapListener() {
//		((ImageViewPaperAdapter)getAdapter()).setOnSingleTapListener();
//	}
    
	/**  
	 *  删除当前项
	 *  @return  “当前位置/总数量”
	 */
	public String deleteCurrentPath(){
		return ((ImageViewPaperAdapter)getAdapter()).deleteCurrentItem(getCurrentItem());

	}

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if(mChildIsBeingDragged)
            return false;
        return super.onInterceptTouchEvent(arg0);
    }
    @Override
    public void startDrag() {
        // TODO Auto-generated method stub
        mChildIsBeingDragged=true;
    }


    @Override
    public void stopDrag() {
        // TODO Auto-generated method stub
        mChildIsBeingDragged=false;
    }
    
//    public void refresh() {
//    	adapter.notifyDataSetChanged();
//    }
}

