package com.codeim.floorview.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.LazyImageLoader;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.floorview.bean.Comment;
import com.codeim.floorview.bean.ImageFolder;
//import com.web.utils.ImageManager2;

public class ImageFolderAdapter extends BaseAdapter implements TweetAdapter {

	private Context mContext;
	private ArrayList<ImageFolder> dataList;
	private DisplayMetrics dm;
	protected LayoutInflater mInflater;
	private ViewHolder mImageFolderHolder;
	
	private final Handler mHandler = new Handler();

	public ImageFolderAdapter(Context c, ArrayList<ImageFolder> dataList) {

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
		public TextView folderName;
		public TextView imageCount;
		public ImageView imageSelect;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.image_folder_list_item, parent, false);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.image_view);
			viewHolder.folderName = (TextView) convertView
					.findViewById(R.id.folder_name);
			viewHolder.imageCount = (TextView) convertView
					.findViewById(R.id.imageCount);
			viewHolder.imageSelect = (ImageView) convertView
					.findViewById(R.id.image_select);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		String path;
		if (dataList != null && dataList.size() > position)
			path = dataList.get(position).getFirstImagePath();
		else
			path = "camera_default";
		if (path.contains("default")) {
			viewHolder.imageView.setImageResource(R.drawable.pic_dir);
		} else {
			TwitterApplication.mImageManager.displayImage(viewHolder.imageView,
					path, R.drawable.pic_dir, 100, 100);
		}
		if(dataList.get(position).isSelected()) {
			viewHolder.imageSelect.setVisibility(View.VISIBLE);
		} else {
			viewHolder.imageSelect.setVisibility(View.GONE);
		}
		viewHolder.folderName.setText(dataList.get(position).getName());
		viewHolder.imageCount.setText(dataList.get(position).getCount()+"张");
		
		viewHolder.folderName.setTag(position);
		viewHolder.imageCount.setTag(position);

		return convertView;
	}

//	private OnItemClickListener mOnItemClickListener;
//
//	public void setOnItemClickListener(OnItemClickListener l) {
//		mOnItemClickListener = l;
//	}
//
//	public interface OnItemClickListener {
//		public void onItemClick(int position);
//	}
	
	public void refresh(ArrayList<ImageFolder> imageFolders) {
		dataList = (ArrayList<ImageFolder>) imageFolders.clone();
		
		notifyDataSetChanged();
	}
	
	@Override
	public void refresh() {
		notifyDataSetChanged();
	}

}
