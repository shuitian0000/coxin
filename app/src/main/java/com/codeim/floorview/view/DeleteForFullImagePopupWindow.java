package com.codeim.floorview.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.codeim.coxin.TwitterApplication;
import com.codeim.floorview.adapter.ImageFolderAdapter;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.coxin.R;

public class DeleteForFullImagePopupWindow extends BasePopupWindowForFullImage
{
	private String image_url;
	private View mView;
	
	private ImageView mImageView;
    private Button image_del;

	public DeleteForFullImagePopupWindow(int width, int height,
			String image_url, View convertView)
	{
		super(convertView, width, height, true, image_url);
		
		this.image_url = image_url;
		initViews();
		initEvents();
		init();
	}

	@Override
	public void initViews()
	{
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mView = inflater.inflate(R.layout.image_full_shower, null);

		mImageView = (ImageView) findViewById(R.id.image_full);
		image_del = (Button) findViewById(R.id.button_del_image);
		
//		TwitterApplication.mImageManager.displayImage(mImageView,
//				image_url, R.drawable.bg_userheader_cover);
		TwitterApplication.mImageManager.displayImage(mImageView,
				image_url, -1, false);
	}

	public interface OnCheckerClickListener
	{
		void checkerSelected();
	}

	private OnCheckerClickListener mOnCheckerClickListener;

	public void setOnCheckerSelected(OnCheckerClickListener mImageDirSelected)
	{
		this.mOnCheckerClickListener = mImageDirSelected;
	}

	@Override
	public void initEvents()
	{
		image_del.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnCheckerClickListener.checkerSelected();
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
