package com.codeim.floorview.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
//import com.xzh.sharetosina.utils.ImageManager2;
import com.codeim.coxin.app.ImageManager;

public class AlbumPreviewGridImageAdapter extends BaseAdapter implements
         OnClickListener{

	private Context mContext;
	private ArrayList<String> dataList;
	private DisplayMetrics dm;

	public AlbumPreviewGridImageAdapter(Context c, ArrayList<String> dataList) {

		mContext = c;
		this.dataList = dataList;
		dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);

	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	private class ViewHolder {
		public ImageView imageView;
		public Button btn;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		ImageView imageView;
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.comment_delete_image, parent, false);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.image_view);
			viewHolder.btn = (Button) convertView
					.findViewById(R.id.button_del_image);
			
//			imageView = new ImageView(mContext);
			convertView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, dipToPx(65)));
			viewHolder.imageView.setAdjustViewBounds(true);
			viewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_XY); 
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		String path;
		if (dataList != null && position<dataList.size() )
			path = dataList.get(position);
		else
			path = "camera_default";
		Log.i("path", "path:"+path+"::position"+position);
		if (path.contains("default")) {
			viewHolder.imageView.setImageResource(R.drawable.camera_default);
		    viewHolder.btn.setVisibility(View.GONE);
//		    viewHolder.imageView.setOnClickListener(new OnClickListener() {
//		    	@Override
//		    	public void onClick(View view) {
//		    	
//		    	}
//		    });
		    
		    viewHolder.imageView.setTag(position);
//		    viewHolder.imageView.setTag("camera_default");
		    viewHolder.imageView.setOnClickListener(this);
		}
		else {
//            ImageManager2.from(mContext).displayImage(imageView, path,R.drawable.camera_default,100,100);
			TwitterApplication.mImageManager.displayImage(viewHolder.imageView, path,R.drawable.camera_default,100,100);
		    
		    viewHolder.imageView.setTag(position);
		    viewHolder.imageView.setOnClickListener(this);
			
			viewHolder.btn.setTag(position);
			viewHolder.btn.setVisibility(View.VISIBLE);
			viewHolder.btn.setOnClickListener(this);
		}
		
//		return viewHolder.imageView;
		return convertView;
	}
	
	public int dipToPx(int dip) {
		return (int) (dip * dm.density + 0.5f);
	}
	
	@Override
	public void onClick(View view) {
		if (view instanceof Button) {
			Button btn = (Button) view;
 	        int position = (Integer) btn.getTag();
			if (dataList != null && mOnItemClickListener != null
					&& position < dataList.size()) {
				mOnItemClickListener.onItemClick(btn, position,
						dataList.get(position));
			}
		}
		else if(view instanceof ImageView) {
			ImageView imageView = (ImageView) view;
			int position = (Integer) imageView.getTag();
			mOnDefaultItemClickListener.onItemClick(position, dataList.get(position));
		}
	}
	
	private OnDefaultItemClickListener mOnDefaultItemClickListener;
	private OnItemClickListener mOnItemClickListener;

	public void setOnDefaultItemClickListener(OnDefaultItemClickListener l) {
		mOnDefaultItemClickListener = l;
	}
	public void setOnItemClickListener(OnItemClickListener l) {
		mOnItemClickListener = l;
	}
	
	public interface OnDefaultItemClickListener {
		public void onItemClick(int position, String path);
	}
	public interface OnItemClickListener {
		public void onItemClick(Button view, int position, String path);
	}

}
