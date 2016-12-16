package com.codeim.floorview.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.codeim.coxin.TwitterApplication;
import com.codeim.floorview.view.MatrixImageViewForViewPager;
import com.codeim.floorview.view.MatrixImageViewForViewPager.OnMovingListener;
import com.codeim.floorview.view.MatrixImageViewForViewPager.OnSingleTapListener;
import com.codeim.coxin.R;

public class ImageViewPaperAdapter extends PagerAdapter {
	private ArrayList<String> paths;//大图地址 如果为网络图片 则为大图url
	Context context;
	
	/**  播放按钮点击事件 */ 
	private OnPlayVideoListener onPlayVideoListener;
	/**  界面单击事件 用以显示和隐藏菜单栏 */ 
	private OnSingleTapListener onSingleTapListener;
	private OnMovingListener moveListener;
	
	private boolean md5en;
	
	public ImageViewPaperAdapter(Context context){
		this.context=context;
	}
	
	public ImageViewPaperAdapter(Context context, ArrayList<String> paths){
		this.context=context;
		this.paths=paths;
	}
	
    public void setPath(ArrayList<String> url_paths, boolean local_image) {
    	this.paths = url_paths;
    	this.md5en = !local_image;
    	this.notifyDataSetChanged();
    }
    
    public void refreshPath(ArrayList<String> url_paths) {
    	this.paths = url_paths;
    	this.notifyDataSetChanged();
    }
	
	public Context getContext() {
		return context;
	}

	@Override
	public Object instantiateItem(ViewGroup viewGroup, int position) {
		//注意，这里不可以加inflate的时候直接添加到viewGroup下，而需要用addView重新添加
		//因为直接加到viewGroup下会导致返回的view为viewGroup
		View imageLayout = LayoutInflater.from(getContext()).inflate(R.layout.image_pager_item, null);
//		View imageLayout = inflate(getContext(),R.layout.image_pager_item, null);
		viewGroup.addView(imageLayout);
		assert imageLayout != null;
		MatrixImageViewForViewPager imageView = (MatrixImageViewForViewPager) imageLayout.findViewById(R.id.image);
		imageView.setOnMovingListener(this.moveListener);
//		imageView.setOnMovingListener(AlbumViewPager.this);
		imageView.setOnSingleTapListener(this.onSingleTapListener);
		String path=paths.get(position);
		//final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
		
		ImageButton videoIcon=(ImageButton)imageLayout.findViewById(R.id.videoicon);
		if(path.contains("video")){
			videoIcon.setVisibility(View.VISIBLE);
		}else {			
			videoIcon.setVisibility(View.GONE);
		}
		videoIcon.setOnClickListener(playVideoListener);
		videoIcon.setTag(path);
		imageLayout.setTag(path);
		
		//mImageLoader.loadImage(path, imageView, mOptions);
		//先用这个试验下
		TwitterApplication.mImageManager.displayImage(imageView,
				path, -1, md5en);
		
		return imageLayout;
	}

	OnClickListener playVideoListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			String path=v.getTag().toString();
			path=path.replace(getContext().getResources().getString(R.string.Thumbnail),
					getContext().getResources().getString(R.string.Video));
			path=path.replace(".jpg", ".3gp");
			if(onPlayVideoListener!=null)
				onPlayVideoListener.onPlay(path);
			else {
				Toast.makeText(getContext(), "onPlayVideoListener", Toast.LENGTH_SHORT).show();
//				throw new RuntimeException("onPlayVideoListener is null");
			}
		}
	};


	@Override
	public int getCount() {
		return paths.size();
	}
	
	@Override
	public int getItemPosition(Object object) {
		//在notifyDataSetChanged时返回None，重新绘制
		return POSITION_NONE;
	}

	@Override
	public void destroyItem(ViewGroup container, int arg1, Object object) {
		((ViewPager) container).removeView((View) object);  
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;			
	}

	//自定义获取当前view方法                              
	public String deleteCurrentItem(int position) {
		String path=paths.get(position);
		if(path!=null) {
			//先不用删除功能
//			FileOperateUtil.deleteSourceFile(path, getContext());
			paths.remove(path);
			notifyDataSetChanged();
			if(paths.size()>0)
				if(position>=paths.size()) {
					return (position)+"/"+paths.size();
				} else {
					return (position+1)+"/"+paths.size();
				}
				//return (getCurrentItem()+1)+"/"+paths.size();
			else {
				return "0/0";
			}
		}
		return null;
	}	
	
	public void setOnMovingListener(OnMovingListener listener){
		this.moveListener=listener;
	}
	public void setOnSingleTapListener(OnSingleTapListener onSingleTapListener) {
		this.onSingleTapListener = onSingleTapListener;
	}
	//播放点击按钮
	public interface OnPlayVideoListener{
		void onPlay(String path);
	}
	public void setOnPlayVideoListener(OnPlayVideoListener onPlayVideoListener) {
		this.onPlayVideoListener = onPlayVideoListener;
	}
}

