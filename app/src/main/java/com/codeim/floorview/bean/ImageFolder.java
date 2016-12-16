package com.codeim.floorview.bean;

public class ImageFolder {
	/**
	 * 图片的文件夹路径
	 */
	private String dir;

	/**
	 * 第一张图片的路径
	 */
	private String firstImagePath;

	/**
	 * 文件夹的名称
	 */
	private String name;

	/**
	 * 图片的数量
	 */
	private int count;
	
	private boolean selected;
	
	public ImageFolder()
	{
	}
	
	public ImageFolder(String firstImagePath, String name, int count)
	{
		extractDir(firstImagePath);
		this.name = name;
		this.count = count;
		this.selected = false;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSelected() {
		return this.selected;
	}

	public String getDir()
	{
		return dir;
	}

	public void extractDir(String firstImagePath)
	{
		this.firstImagePath = firstImagePath;
		int lastIndexOf = firstImagePath.lastIndexOf("/");
		
		if(lastIndexOf<0) {
			this.dir = firstImagePath;
			this.name = firstImagePath;
		} else if(lastIndexOf<1) {
			this.dir = firstImagePath;
			this.name = firstImagePath;
		} else {
			this.dir = this.firstImagePath.substring(0, lastIndexOf);
			lastIndexOf = this.dir.lastIndexOf("/");
			this.name = this.dir.substring(lastIndexOf);
		}
	}
	
	public void setDir(String dir)
	{
		this.dir = dir;
		int lastIndexOf = this.dir.lastIndexOf("/");
		this.name = this.dir.substring(lastIndexOf);
	}

	public String getFirstImagePath()
	{
		return firstImagePath;
	}

	public void setFirstImagePath(String firstImagePath)
	{
		this.firstImagePath = firstImagePath;
	}

	public String getName()
	{
		return name;
	}
	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

}
