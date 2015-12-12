package com.svdroid.firebasetaskmanager.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.svdroid.firebasetaskmanager.App;
import com.svdroid.firebasetaskmanager.R;
import com.svdroid.firebasetaskmanager.model.Task;

public class TaskActivity extends AppCompatActivity implements View.OnClickListener
{
	private static final String TAG = "com.svdroid.firebaseexampletaskmanager.activity.TaskActivity";
	public static final String KEY_POSITION = TAG + ".KEY_POSITION";
	public static final String KEY_TITLE = TAG + ".KEY_TITLE";
	public static final String KEY_NOTE = TAG + ".KEY_NOTE";
	public static final String KEY_EXECUTED = TAG + ".KEY_EXECUTED";
	public static final String KEY_ITEM_KEY = TAG + ".KEY_ITEM_KEY";

	private EditText mTitle;
	private EditText mNote;
	private CheckBox mExecuted;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(this);

		mTitle = (EditText) findViewById(R.id.title);
		mNote = (EditText) findViewById(R.id.note);
		mExecuted = (CheckBox) findViewById(R.id.is_executed);

		setState(getIntent().getExtras());
	}

	@Override
	public void onClick(View v)
	{
		final Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			final int position = bundle.getInt(KEY_POSITION);
			App app = (App) getApplication();
			Firebase firebase = app.getTasksFireBase();

			String key = bundle.getString(KEY_ITEM_KEY);
			if (key != null) {
				firebase.child(key).setValue(new Task(
					mTitle.getText().toString(),
					mNote.getText().toString(),
					mExecuted.isChecked(),
					position
				));
			} else {
				firebase.push().setValue(new Task(
					mTitle.getText().toString(),
					mNote.getText().toString(),
					mExecuted.isChecked(),
					position
				));
			}
		}

		onBackPressed();
	}

	private void setState(Bundle bundle)
	{
		if (bundle == null) {
			return;
		}

		mTitle.setText(bundle.containsKey(KEY_TITLE) ? bundle.getString(KEY_TITLE) : "");
		mNote.setText(bundle.containsKey(KEY_NOTE) ? bundle.getString(KEY_NOTE) : "");
		mExecuted.setChecked(bundle.getBoolean(KEY_EXECUTED));

		setTitle(
			bundle.containsKey(KEY_TITLE) || bundle.containsKey(KEY_NOTE) || bundle.containsKey(KEY_EXECUTED)
				? getString(R.string.title_edit_note)
				: getString(R.string.title_create_note)
		);
	}
}
