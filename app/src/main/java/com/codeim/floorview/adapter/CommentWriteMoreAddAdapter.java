package com.codeim.floorview.adapter;

import java.util.ArrayList;
import java.util.zip.Inflater;

import com.codeim.coxin.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author ywywang 创建时间类说明
 */

public class CommentWriteMoreAddAdapter extends BaseAdapter {

	//ArrayList<Object> data;
	Context context;
	//String tag;
	View view;

	public CommentWriteMoreAddAdapter(Context context) {
//		this.data = data;
		this.context = context;
		//this.tag = tag;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mThumbIds.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View contentView, ViewGroup parent) {

		view = LayoutInflater.from(context).inflate(R.layout.comment_addmore_item, null);
		ImageButton iamge=(ImageButton) view.findViewById(R.id.iButton_item);
		TextView text=(TextView)view.findViewById(R.id.tv_item);
		//填充数据
		iamge.setImageResource(mThumbIds[position]);
		text.setText(mTextValues[position]);
		return view;
	}

	// 适配显示的图片数组
			private Integer[] mThumbIds = {R.drawable.add_tool_camera,R.drawable.add_tool_location,R.drawable.add_tool_paint,R.drawable.add_tool_photo,
					R.drawable.add_tool_voice,R.drawable.add_tool_video
					 };
			//给照片添加文字显示
			private String[] mTextValues = { "拍照", "位置", "涂鸦", "图片", "语音通话",
					"视频通话"};
	
	
}
