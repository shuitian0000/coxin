package com.codeim.coxin.ui.module;

//import com.codeim.coxin.R;
import com.codeim.coxin.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * ???
 * 
 * @author Luo.Yunlongx
 * 
 */
public class GenderChoose extends LinearLayout {

	private Context mContext;

	private View mView;
	private RadioGroup mRadioGroup;
	private RadioButton mLeftRadioButton;
	private RadioButton mRightRadioButton;

	private LayoutParams mLayoutParams;

	/**
	 * ??????
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GenderChoose(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	/**
	 * ??????
	 * 
	 * @param context
	 * @param attrs
	 */
	public GenderChoose(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	/**
	 * ??????
	 * 
	 * @param context
	 */
	public GenderChoose(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	/**
	 * ?????
	 */
	private void init() {
		mView = LayoutInflater.from(mContext).inflate(R.layout.view_genderchoose, null);
		mLeftRadioButton = (RadioButton) mView.findViewById(R.id.mychoose_radioButton1);
		mRightRadioButton = (RadioButton) mView.findViewById(R.id.mychoose_radioButton2);
		mRadioGroup = (RadioGroup) mView.findViewById(R.id.mychoose_radioGroup);
		mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		mView.setLayoutParams(mLayoutParams);

		this.setLayoutParams(mLayoutParams);
		this.addView(mView);
	}

	/**
	 * ?????????????
	 * 
	 * @param leftText
	 *            ???????
	 * @param rightText
	 *            ???????
	 */
	public void setTexts(String leftText, String rightText) {
		mLeftRadioButton.setText(leftText);
		mRightRadioButton.setText(rightText);
	}

	/**
	 * ?????????
	 * 
	 * @param mOnCheckedChange
	 */
	public void setOnClick(final IOnCheckedChange mOnCheckedChange, final int id) {
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				switch (checkedId) {
				case R.id.mychoose_radioButton1:
					/* ????????? */
					mLeftRadioButton.setTextColor(mContext.getResources()
							.getColor(android.R.color.white));
					mRightRadioButton.setTextColor(mContext.getResources()
							.getColor(android.R.color.black));
					/**
					 * ????????????
					 */
					if (mOnCheckedChange != null) {
						mOnCheckedChange.leftOnClick(id);
					}
					break;
				case R.id.mychoose_radioButton2:
					/* ????????? */
					mRightRadioButton.setTextColor(mContext.getResources()
							.getColor(android.R.color.white));
					mLeftRadioButton.setTextColor(mContext.getResources()
							.getColor(android.R.color.black));
					/**
					 * ????????????
					 */
					if (mOnCheckedChange != null) {
						mOnCheckedChange.rightOnClick(id);
					}
					break;
				}
			}
		});
	}

	/**
	 * ??????????
	 * 
	 * @param leftSelected
	 */
	public void setChoose(boolean leftSelected) {
		mLeftRadioButton.setSelected(leftSelected);
		mRightRadioButton.setSelected(!leftSelected);
	}

	/**
	 * 
	 * ???д??????????RadioGroup.setOnCheckedChangeListener???
	 * 
	 * @author Luo.Yunlongx
	 * 
	 */
	public interface IOnCheckedChange {
		/**
		 * ??????
		 * 
		 * @param id
		 *            ???????
		 */
		public void leftOnClick(int id);

		/**
		 * ??????
		 * 
		 * @param id
		 *            ???????
		 */
		public void rightOnClick(int id);
	}

}
