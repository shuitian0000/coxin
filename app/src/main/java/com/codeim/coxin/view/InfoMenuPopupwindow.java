package com.codeim.coxin.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.floorview.adapter.ImageFolderAdapter;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.floorview.view.BasePopupWindowForListView;
import com.codeim.coxin.R;

public class InfoMenuPopupwindow extends BasePopupWindowForListView<String> {
	private ListView mListOption;
	private InfoMenuAdapter mMenuOptionAdapter;
	
	private ArrayList<String> mDataList;
	private View mView;

	public InfoMenuPopupwindow(int width, int height,
			ArrayList<String> datas, View convertView)
	{
		super(convertView, width, height, true, datas);
		this.mDataList = datas;
	}

	@Override
	public void initViews()
	{
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mView = inflater.inflate(R.layout.image_folder_pop_view, null);
        
		mListOption = (ListView) findViewById(R.id.info_pop_menu);
        mMenuOptionAdapter = new InfoMenuAdapter(context, mDatas);
		
        mListOption.setAdapter(mMenuOptionAdapter);
//		mListOption.setAdapter(new CommonAdapter<ImageFolder>(context, mDatas,
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

	public interface OnOptionSelected
	{
		void selected(int position, String option);
	}

	private OnOptionSelected mOnOptionSelected;

	public void setOnOptionSelected(OnOptionSelected mOnOptionSelected)
	{
		this.mOnOptionSelected = mOnOptionSelected;
	}

	@Override
	public void initEvents()
	{
		mListOption.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				if (mOnOptionSelected != null)
				{
					mOnOptionSelected.selected(position, mDatas.get(position));
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
		this.mMenuOptionAdapter.refresh();
	}
	
	public class InfoMenuAdapter extends BaseAdapter implements TweetAdapter {

		private Context mContext;
		private ArrayList<String> dataList;
		private DisplayMetrics dm;
		protected LayoutInflater mInflater;
		private ViewHolder mImageFolderHolder;
		
		private final Handler mHandler = new Handler();

		public InfoMenuAdapter(Context c, ArrayList<String> dataList) {

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
			public TextView optionName;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.info_pop_menu_item, parent, false);
				viewHolder.optionName = (TextView) convertView
						.findViewById(R.id.option_name);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			viewHolder.optionName.setText(dataList.get(position));
			viewHolder.optionName.setTag(position);

			return convertView;
		}

//		private OnItemClickListener mOnItemClickListener;
	//
//		public void setOnItemClickListener(OnItemClickListener l) {
//			mOnItemClickListener = l;
//		}
	//
//		public interface OnItemClickListener {
//			public void onItemClick(int position);
//		}
		
		public void refresh(ArrayList<String> options) {
			dataList = (ArrayList<String>) options.clone();
			
			notifyDataSetChanged();
		}
		
		@Override
		public void refresh() {
			notifyDataSetChanged();
		}

	}

}
