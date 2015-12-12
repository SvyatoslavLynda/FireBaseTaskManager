package com.svdroid.firebasetaskmanager;

import android.app.Application;

import com.firebase.client.Firebase;

public class App extends Application
{
	private static final String FIRE_BASE_BASE_URL = "https://svtaskmanager.firebaseio.com/";

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
		return mFirebase.child("Users")
			.child(mUserId)
			.child("Tasks");
	}

	public void setUserId(String userId)
	{
		mUserId = userId;
	}
}
