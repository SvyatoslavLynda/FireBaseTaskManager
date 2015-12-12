package com.svdroid.firebasetaskmanager.model;

public class Task
{
	private String title;
	private String note;
	private boolean executed;
	private int position;

	public Task()
	{

	}

	public Task(String title, String note, boolean isExecuted, int position)
	{
		this.title = title;
		this.note = note;
		this.executed = isExecuted;
		this.position = position;
	}

	public String getTitle()
	{
		return title;
	}

	public String getNote()
	{
		return note;
	}

	public boolean isExecuted()
	{
		return executed;
	}

	public int getPosition()
	{
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}
}
