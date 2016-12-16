package com.codeim.floorview.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.codeim.floorview.adapter.ImageFolderAdapter;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.coxin.R;

public class ListImageDirPopupWindow extends BasePopupWindowForListView<ImageFolder>
{
	private ListView mListDir;
	private ImageFolderAdapter mImageFolderAdapter;
	private ArrayList<ImageFolder> mDataList;
	private View mView;

	public ListImageDirPopupWindow(int width, int height,
			ArrayList<ImageFolder> datas, View convertView)
	{
		super(convertView, width, height, true, datas);
		this.mDataList = datas;
	}

	@Override
	public void initViews()
	{
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mView = inflater.inflate(R.layout.image_folder_pop_view, null);
        
        mListDir = (ListView) findViewById(R.id.image_list_dir);
//		mImageFolderAdapter = new ImageFolderAdapter(context, mDataList);
        mImageFolderAdapter = new ImageFolderAdapter(context, mDatas);
		
		mListDir.setAdapter(mImageFolderAdapter);
//		mListDir.setAdapter(new CommonAdapter<ImageFolder>(context, mDatas,
//				R.layout.list_dir_item)
//		{
//			@Override
//			public void convert(ViewHolder helper, ImageFolder item)
//			{
//				helper.setText(R.id.id_dir_item_name, item.getName());
//				helper.setImageByUrl(R.id.id_dir_item_image,
//						item.getFirstImagePath());
//				helper.setText(R.id.id_dir_item_count, item.getCount() + "张");
//			}
//		});
	}

	public interface OnImageDirSelected
	{
		void selected(ImageFolder floder);
	}

	private OnImageDirSelected mImageDirSelected;

	public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected)
	{
		this.mImageDirSelected = mImageDirSelected;
	}

	@Override
	public void initEvents()
	{
		mListDir.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				if (mImageDirSelected != null)
				{
					mImageDirSelected.selected(mDatas.get(position));
				}
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
	
	public void refresh()
	{
		this.mImageFolderAdapter.refresh();
	}

}
