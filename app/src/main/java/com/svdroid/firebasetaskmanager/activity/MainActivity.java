package com.svdroid.firebasetaskmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.utilities.Pair;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.svdroid.firebasetaskmanager.App;
import com.svdroid.firebasetaskmanager.R;
import com.svdroid.firebasetaskmanager.adapter.TaskAdapter;
import com.svdroid.firebasetaskmanager.model.Task;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
	GoogleApiClient.OnConnectionFailedListener, TaskAdapter.OnItemClickListener
{
	private RecyclerView mTasksList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(this);

		final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		final TaskAdapter taskAdapter = new TaskAdapter(this, this);
		mTasksList = (RecyclerView) findViewById(R.id.tasks_list);
		mTasksList.setLayoutManager(layoutManager);
		mTasksList.setAdapter(taskAdapter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.action_logout) {
			((App) getApplication()).getBaseFireBase().unauth();
			startActivity();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v)
	{
		Intent intent = new Intent(this, TaskActivity.class);
		intent.putExtra(TaskActivity.KEY_POSITION, mTasksList.getChildCount());
		startActivity(intent);
	}

	@Override
	public void onItemClick(int position, Pair item)
	{
		Intent intent = new Intent(this, TaskActivity.class);
		intent.putExtra(TaskActivity.KEY_POSITION, ((Task) item.getSecond()).getPosition());
		intent.putExtra(TaskActivity.KEY_TITLE, ((Task) item.getSecond()).getTitle());
		intent.putExtra(TaskActivity.KEY_NOTE, ((Task) item.getSecond()).getNote());
		intent.putExtra(TaskActivity.KEY_EXECUTED, ((Task) item.getSecond()).isExecuted());
		intent.putExtra(TaskActivity.KEY_ITEM_KEY, ((String) item.getFirst()));
		startActivity(intent);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{

	}

	private void startActivity()
	{
		Intent intent = new Intent(this, AuthActivity.class);
		intent.setFlags(
			Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
		);
		startActivity(intent);
	}
}