package com.codeim.floorview.adapter;

import java.util.List;







import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.floorview.bean.Emoji;
import com.codeim.coxin.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * 
 ******************************************
 * @author
 * @文件名称	:  EmojiAdapter.java
 * @创建时间	: 2015-4-7
 * @文件描述	: 表情填充器
 ******************************************
 */
public class EmojiAdapter extends BaseAdapter implements TweetAdapter{

    private List<Emoji> data;

    private LayoutInflater inflater;

    private int size=0;
    
	public EmojiAdapter(Context context) {
//		this.data = data;
//		this.context = context;
		//this.tag = tag;
		
		this.inflater=LayoutInflater.from(context);
	}

    public EmojiAdapter(Context context, List<Emoji> list) {
        this.inflater=LayoutInflater.from(context);
        this.data=list;
        this.size=list.size();
    }

    @Override
    public int getCount() {
        return this.size;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {

        public ImageView iv_face;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Emoji emoji=data.get(position);
        ViewHolder viewHolder=null;
        if(convertView == null) {
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.comment_emoji_item, null);
            viewHolder.iv_face=(ImageView)convertView.findViewById(R.id.item_iv_face);
            convertView.setTag(viewHolder);
        } else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        if(emoji.getId() == R.drawable.face_del_icon) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_face.setImageResource(emoji.getId());
        } else if(TextUtils.isEmpty(emoji.getCharacter())) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_face.setImageDrawable(null);
        } else {
            viewHolder.iv_face.setTag(emoji);
            viewHolder.iv_face.setImageResource(emoji.getId());
        }

        return convertView;
    }

	@Override
	public void refresh() {
		notifyDataSetChanged();
	}
}