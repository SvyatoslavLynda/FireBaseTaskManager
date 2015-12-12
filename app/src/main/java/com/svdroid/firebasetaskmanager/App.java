package com.svdroid.firebasetaskmanager;

import android.app.Application;

import com.firebase.client.Firebase;

public class App extends Application
{
	private static final String FIRE_BASE_BASE_URL = "https://svtaskmanager.firebaseio.com/";
	private static final String USERS_METHODS = "Users";
	private static final String TASKS_METHODS = "Tasks";

	private Firebase mFirebase;
	private String mUserId;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Firebase.setAndroidContext(this);
		mFirebase = new Firebase(FIRE_BASE_BASE_URL);
	}

	public Firebase getBaseFireBase()
	{
		return mFirebase;
	}

	public Firebase getTasksFireBase()
	{
		return mFirebase.child(USERS_METHODS)
			.child(mUserId)
			.child(TASKS_METHODS);
	}

	public void setUserId(String userId)
	{
		mUserId = userId;
	}
}
