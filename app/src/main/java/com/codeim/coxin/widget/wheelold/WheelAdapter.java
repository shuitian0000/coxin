package com.codeim.coxin.widget.wheelold;

/**
 * WheelView 适配器接
 * @author han_shuliang(octopus_truth@163.com)
 *
 */
public interface WheelAdapter {
    /**
     * 获取条目的个
     * 
     * @return 
     * 		WheelView 的条目个
     */
    public int getItemsCount();

    /**
     * 根据索引位置获取 WheelView 的条
     * 
     * @param index
     *            条目的索
     * @return 
     * 		WheelView 上显示的条目的
     */
    public String getItem(int index);

    /**
     * 获取条目的最大长 用来定义 WheelView 的宽 如果返回 -1, 就会使用默认宽度
     * 
     * @return 
     * 		条目的最大宽或 -1
     */
    public int getMaximumLength();
}
